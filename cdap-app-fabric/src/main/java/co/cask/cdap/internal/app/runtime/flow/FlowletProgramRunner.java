/*
 * Copyright © 2014-2016 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package co.cask.cdap.internal.app.runtime.flow;

import co.cask.cdap.api.annotation.Batch;
import co.cask.cdap.api.annotation.ProcessInput;
import co.cask.cdap.api.annotation.Tick;
import co.cask.cdap.api.app.ApplicationSpecification;
import co.cask.cdap.api.data.schema.Schema;
import co.cask.cdap.api.data.schema.UnsupportedTypeException;
import co.cask.cdap.api.flow.FlowSpecification;
import co.cask.cdap.api.flow.FlowletConnection;
import co.cask.cdap.api.flow.FlowletDefinition;
import co.cask.cdap.api.flow.flowlet.Callback;
import co.cask.cdap.api.flow.flowlet.FailurePolicy;
import co.cask.cdap.api.flow.flowlet.FailureReason;
import co.cask.cdap.api.flow.flowlet.Flowlet;
import co.cask.cdap.api.flow.flowlet.FlowletSpecification;
import co.cask.cdap.api.flow.flowlet.InputContext;
import co.cask.cdap.api.flow.flowlet.OutputEmitter;
import co.cask.cdap.api.flow.flowlet.StreamEvent;
import co.cask.cdap.api.metrics.MetricsCollectionService;
import co.cask.cdap.api.metrics.MetricsContext;
import co.cask.cdap.api.stream.StreamEventData;
import co.cask.cdap.app.program.Program;
import co.cask.cdap.app.queue.QueueReader;
import co.cask.cdap.app.queue.QueueSpecification;
import co.cask.cdap.app.queue.QueueSpecificationGenerator.Node;
import co.cask.cdap.app.runtime.ProgramController;
import co.cask.cdap.app.runtime.ProgramOptions;
import co.cask.cdap.app.runtime.ProgramRunner;
import co.cask.cdap.common.async.ExecutorUtils;
import co.cask.cdap.common.conf.Constants;
import co.cask.cdap.common.io.BinaryDecoder;
import co.cask.cdap.common.lang.InstantiatorFactory;
import co.cask.cdap.common.lang.PropertyFieldSetter;
import co.cask.cdap.common.logging.common.LogWriter;
import co.cask.cdap.common.logging.logback.CAppender;
import co.cask.cdap.common.queue.QueueName;
import co.cask.cdap.common.utils.ImmutablePair;
import co.cask.cdap.data.stream.StreamCoordinatorClient;
import co.cask.cdap.data.stream.StreamPropertyListener;
import co.cask.cdap.data2.dataset2.DatasetFramework;
import co.cask.cdap.data2.metadata.writer.ProgramContextAware;
import co.cask.cdap.data2.queue.ConsumerConfig;
import co.cask.cdap.data2.queue.ConsumerGroupConfig;
import co.cask.cdap.data2.queue.DequeueStrategy;
import co.cask.cdap.data2.queue.QueueClientFactory;
import co.cask.cdap.data2.queue.QueueConsumer;
import co.cask.cdap.data2.registry.UsageRegistry;
import co.cask.cdap.data2.transaction.queue.QueueMetrics;
import co.cask.cdap.data2.transaction.stream.StreamConsumer;
import co.cask.cdap.internal.app.queue.QueueReaderFactory;
import co.cask.cdap.internal.app.queue.RoundRobinQueueReader;
import co.cask.cdap.internal.app.queue.SimpleQueueSpecificationGenerator;
import co.cask.cdap.internal.app.runtime.AbstractListener;
import co.cask.cdap.internal.app.runtime.DataFabricFacade;
import co.cask.cdap.internal.app.runtime.DataFabricFacadeFactory;
import co.cask.cdap.internal.app.runtime.DataSetFieldSetter;
import co.cask.cdap.internal.app.runtime.MetricsFieldSetter;
import co.cask.cdap.internal.app.runtime.ProgramOptionConstants;
import co.cask.cdap.internal.io.DatumWriterFactory;
import co.cask.cdap.internal.io.ReflectionDatumReader;
import co.cask.cdap.internal.io.SchemaGenerator;
import co.cask.cdap.internal.lang.Reflections;
import co.cask.cdap.internal.specification.FlowletMethod;
import co.cask.cdap.proto.Id;
import co.cask.cdap.proto.ProgramType;
import co.cask.common.io.ByteBufferInputStream;
import co.cask.tephra.TransactionSystemClient;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import com.google.common.reflect.TypeToken;
import com.google.common.util.concurrent.AbstractService;
import com.google.common.util.concurrent.Service;
import com.google.inject.Inject;
import org.apache.twill.api.RunId;
import org.apache.twill.common.Cancellable;
import org.apache.twill.common.Threads;
import org.apache.twill.discovery.DiscoveryServiceClient;
import org.apache.twill.internal.RunIds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.annotation.Nullable;

/**
 *
 */
public final class FlowletProgramRunner implements ProgramRunner {

  private static final Logger LOG = LoggerFactory.getLogger(FlowletProgramRunner.class);

  private final SchemaGenerator schemaGenerator;
  private final DatumWriterFactory datumWriterFactory;
  private final DataFabricFacadeFactory dataFabricFacadeFactory;
  private final StreamCoordinatorClient streamCoordinatorClient;
  private final QueueReaderFactory queueReaderFactory;
  private final MetricsCollectionService metricsCollectionService;
  private final DiscoveryServiceClient discoveryServiceClient;
  private final TransactionSystemClient txClient;
  private final DatasetFramework dsFramework;
  private final UsageRegistry usageRegistry;

  @Inject
  public FlowletProgramRunner(SchemaGenerator schemaGenerator,
                              DatumWriterFactory datumWriterFactory,
                              DataFabricFacadeFactory dataFabricFacadeFactory,
                              StreamCoordinatorClient streamCoordinatorClient,
                              QueueReaderFactory queueReaderFactory,
                              MetricsCollectionService metricsCollectionService,
                              DiscoveryServiceClient discoveryServiceClient,
                              TransactionSystemClient txClient,
                              DatasetFramework dsFramework,
                              UsageRegistry usageRegistry) {
    this.schemaGenerator = schemaGenerator;
    this.datumWriterFactory = datumWriterFactory;
    this.dataFabricFacadeFactory = dataFabricFacadeFactory;
    this.streamCoordinatorClient = streamCoordinatorClient;
    this.queueReaderFactory = queueReaderFactory;
    this.metricsCollectionService = metricsCollectionService;
    this.discoveryServiceClient = discoveryServiceClient;
    this.txClient = txClient;
    this.dsFramework = dsFramework;
    this.usageRegistry = usageRegistry;
  }

  @SuppressWarnings("unused")
  @Inject(optional = true)
  void setLogWriter(LogWriter logWriter) {
    CAppender.logWriter = logWriter;
  }

  @SuppressWarnings("unchecked")
  @Override
  public ProgramController run(Program program, ProgramOptions options) {
    BasicFlowletContext flowletContext = null;
    try {
      // Extract and verify parameters
      String flowletName = options.getName();

      int instanceId = Integer.parseInt(options.getArguments().getOption(ProgramOptionConstants.INSTANCE_ID, "-1"));
      Preconditions.checkArgument(instanceId >= 0, "Missing instance Id");

      int instanceCount = Integer.parseInt(options.getArguments().getOption(ProgramOptionConstants.INSTANCES, "0"));
      Preconditions.checkArgument(instanceCount > 0, "Invalid or missing instance count");

      String runIdOption = options.getArguments().getOption(ProgramOptionConstants.RUN_ID);
      Preconditions.checkNotNull(runIdOption, "Missing runId");
      RunId runId = RunIds.fromString(runIdOption);

      ApplicationSpecification appSpec = program.getApplicationSpecification();
      Preconditions.checkNotNull(appSpec, "Missing application specification.");

      ProgramType processorType = program.getType();
      Preconditions.checkNotNull(processorType, "Missing processor type.");
      Preconditions.checkArgument(processorType == ProgramType.FLOW, "Only FLOW process type is supported.");

      String processorName = program.getName();
      Preconditions.checkNotNull(processorName, "Missing processor name.");

      FlowSpecification flowSpec = appSpec.getFlows().get(processorName);
      FlowletDefinition flowletDef = flowSpec.getFlowlets().get(flowletName);
      Preconditions.checkNotNull(flowletDef, "Definition missing for flowlet \"%s\"", flowletName);

      Class<?> clz = Class.forName(flowletDef.getFlowletSpec().getClassName(), true,
                                   program.getClassLoader());
      Preconditions.checkArgument(Flowlet.class.isAssignableFrom(clz), "%s is not a Flowlet.", clz);

      // Setup dataset framework context, if required
      Id.Program programId = program.getId();
      Id.Flow.Flowlet flowletId = Id.Flow.Flowlet.from(programId.getApplication(), programId.getId(), flowletName);
      Id.Run run = new Id.Run(programId, runId.getId());
      if (dsFramework instanceof ProgramContextAware) {
        ((ProgramContextAware) dsFramework).initContext(run, flowletId);
      }

      Class<? extends Flowlet> flowletClass = (Class<? extends Flowlet>) clz;

      // Creates flowlet context
      flowletContext = new BasicFlowletContext(program, flowletName, instanceId,
                                               runId, instanceCount,
                                               flowletDef.getDatasets(),
                                               options.getUserArguments(), flowletDef.getFlowletSpec(),
                                               metricsCollectionService, discoveryServiceClient, txClient, dsFramework);

      // Creates tx related objects
      DataFabricFacade dataFabricFacade =
        dataFabricFacadeFactory.create(program, flowletContext.getDatasetCache());
      if (dataFabricFacade instanceof ProgramContextAware) {
        ((ProgramContextAware) dataFabricFacade).initContext(run, flowletId);
      }

      // Creates QueueSpecification
      Table<Node, String, Set<QueueSpecification>> queueSpecs =
        new SimpleQueueSpecificationGenerator(Id.Application.from(program.getNamespaceId(), program.getApplicationId()))
          .create(flowSpec);

      Flowlet flowlet = new InstantiatorFactory(false).get(TypeToken.of(flowletClass)).create();
      TypeToken<? extends Flowlet> flowletType = TypeToken.of(flowletClass);

      // Set the context classloader to the cdap classloader. It is needed for the DatumWriterFactory be able
      // to load cdap classes
      Thread.currentThread().setContextClassLoader(FlowletProgramRunner.class.getClassLoader());

      // Inject DataSet, OutputEmitter, Metric fields
      ImmutableList.Builder<ProducerSupplier> queueProducerSupplierBuilder = ImmutableList.builder();
      Reflections.visit(flowlet, flowlet.getClass(),
                        new PropertyFieldSetter(flowletDef.getFlowletSpec().getProperties()),
                        new DataSetFieldSetter(flowletContext),
                        new MetricsFieldSetter(flowletContext.getMetrics()),
                        new OutputEmitterFieldSetter(outputEmitterFactory(flowletContext, flowletName,
                                                                          dataFabricFacade,
                                                                          queueProducerSupplierBuilder,
                                                                          queueSpecs)));

      ImmutableList.Builder<ConsumerSupplier<?>> queueConsumerSupplierBuilder = ImmutableList.builder();
      Collection<ProcessSpecification<?>> processSpecs =
        createProcessSpecification(flowletContext, flowletType,
                                   processMethodFactory(flowlet),
                                   processSpecificationFactory(flowletContext, dataFabricFacade, queueReaderFactory,
                                                               flowletName, queueSpecs, queueConsumerSupplierBuilder,
                                                               createSchemaCache(program)),
                                   Lists.<ProcessSpecification<?>>newLinkedList());
      List<ConsumerSupplier<?>> consumerSuppliers = queueConsumerSupplierBuilder.build();

      // Create the flowlet driver
      AtomicReference<FlowletProgramController> controllerRef = new AtomicReference<>();
      Service serviceHook = createServiceHook(flowletName, consumerSuppliers, controllerRef);
      FlowletRuntimeService driver = new FlowletRuntimeService(flowlet, flowletContext, processSpecs,
                                                             createCallback(flowlet, flowletDef.getFlowletSpec()),
                                                             dataFabricFacade, serviceHook);

      FlowletProgramController controller = new FlowletProgramController(program.getId(), flowletName,
                                                                         flowletContext, driver,
                                                                         queueProducerSupplierBuilder.build(),
                                                                         consumerSuppliers);
      controllerRef.set(controller);

      LOG.info("Starting flowlet: {}", flowletContext);
      driver.start();
      LOG.info("Flowlet started: {}", flowletContext);

      return controller;

    } catch (Exception e) {
      // something went wrong before the flowlet even started. Make sure we release all resources (datasets, ...)
      // of the flowlet context.
      if (flowletContext != null) {
        flowletContext.close();
      }
      throw Throwables.propagate(e);
    }
  }

  /**
   * Creates all {@link ProcessSpecification} for the process methods of the flowlet class.
   *
   * @param flowletType Type of the flowlet class represented by {@link TypeToken}.
   * @param processMethodFactory A {@link ProcessMethodFactory} for creating {@link ProcessMethod}.
   * @param processSpecFactory A {@link ProcessSpecificationFactory} for creating {@link ProcessSpecification}.
   * @param result A {@link Collection} for storing newly created {@link ProcessSpecification}.
   * @return The same {@link Collection} as the {@code result} parameter.
   */
  @SuppressWarnings("unchecked")
  private <T extends Collection<ProcessSpecification<?>>> T createProcessSpecification(
    BasicFlowletContext flowletContext, TypeToken<? extends Flowlet> flowletType,
    ProcessMethodFactory processMethodFactory, ProcessSpecificationFactory processSpecFactory, T result)
    throws NoSuchMethodException {

    Set<FlowletMethod> seenMethods = Sets.newHashSet();

    // Walk up the hierarchy of flowlet class to get all ProcessInput and Tick methods
    for (TypeToken<?> type : flowletType.getTypes().classes()) {
      if (type.getRawType().equals(Object.class)) {
        break;
      }

      // Extracts all process and tick methods
      for (Method method : type.getRawType().getDeclaredMethods()) {
        if (method.isSynthetic() || method.isBridge()) {
          continue;
        }
        if (!seenMethods.add(FlowletMethod.create(method, flowletType.getType()))) {
          // The method is already seen. It can only happen if a children class override a parent class method and
          // is visting the parent method, since the method visiting order is always from the leaf class walking
          // up the class hierarchy.
          continue;
        }

        ProcessInput processInputAnnotation = method.getAnnotation(ProcessInput.class);
        Tick tickAnnotation = method.getAnnotation(Tick.class);

        if (processInputAnnotation == null && tickAnnotation == null) {
          // Neither a process nor a tick method.
          continue;
        }

        int maxRetries = (tickAnnotation == null) ? processInputAnnotation.maxRetries() : tickAnnotation.maxRetries();

        ProcessMethod processMethod = processMethodFactory.create(method, maxRetries);
        Set<String> inputNames;
        Schema schema;
        TypeToken<?> dataType;
        ConsumerConfig consumerConfig;
        int batchSize = 1;

        if (tickAnnotation != null) {
          inputNames = ImmutableSet.of();
          consumerConfig = new ConsumerConfig(0, 0, 1, DequeueStrategy.FIFO, null);
          schema = Schema.of(Schema.Type.NULL);
          dataType = TypeToken.of(void.class);
        } else {
          inputNames = Sets.newHashSet(processInputAnnotation.value());
          if (inputNames.isEmpty()) {
            // If there is no input name, it would be ANY_INPUT
            inputNames.add(FlowletDefinition.ANY_INPUT);
          }
          // If batch mode then generate schema for Iterator's parameter type
          dataType = flowletType.resolveType(method.getGenericParameterTypes()[0]);
          consumerConfig = getConsumerConfig(flowletContext, method);
          Integer processBatchSize = getBatchSize(method);

          if (processBatchSize != null) {
            if (dataType.getRawType().equals(Iterator.class)) {
              Preconditions.checkArgument(dataType.getType() instanceof ParameterizedType,
                                          "Only ParameterizedType is supported for batch Iterator.");
              dataType = flowletType.resolveType(((ParameterizedType) dataType.getType()).getActualTypeArguments()[0]);
            }
            batchSize = processBatchSize;
          }

          try {
            schema = schemaGenerator.generate(dataType.getType());
          } catch (UnsupportedTypeException e) {
            throw Throwables.propagate(e);
          }
        }

        ProcessSpecification processSpec = processSpecFactory.create(inputNames, schema, dataType, processMethod,
                                                                     consumerConfig, batchSize, tickAnnotation);
        // Add processSpec
        if (processSpec != null) {
          result.add(processSpec);
        }
      }
    }
    Preconditions.checkArgument(!result.isEmpty(),
                                "No inputs found for flowlet '%s' of flow '%s' of application '%s' (%s)",
                                flowletContext.getFlowletId(), flowletContext.getFlowId(),
                                flowletContext.getApplicationId(), flowletType);
    return result;
  }

  /**
   * Creates a {@link ConsumerConfig} based on the method annotation and the flowlet context.
   * @param flowletContext Runtime context of the flowlet.
   * @param method The process method to inspect.
   * @return A new instance of {@link ConsumerConfig}.
   */
  private ConsumerConfig getConsumerConfig(BasicFlowletContext flowletContext, Method method) {
    ConsumerGroupConfig groupConfig = FlowUtils.createConsumerGroupConfig(flowletContext.getGroupId(),
                                                                          flowletContext.getInstanceCount(), method);
    return new ConsumerConfig(groupConfig, flowletContext.getInstanceId());
  }

  /**
   * Returns the user specify batch size or {@code null} if not specified.
   */
  private Integer getBatchSize(Method method) {
    // Determine queue batch size, if any
    Batch batch = method.getAnnotation(Batch.class);
    if (batch != null) {
      int batchSize = batch.value();
      Preconditions.checkArgument(batchSize > 0, "Batch size should be > 0: %s", method.getName());
      return batchSize;
    }
    return null;
  }

  private int getNumGroups(Iterable<QueueSpecification> queueSpecs, QueueName queueName) {
    int numGroups = 0;
    for (QueueSpecification queueSpec : queueSpecs) {
      if (queueName.equals(queueSpec.getQueueName())) {
        numGroups++;
      }
    }
    return numGroups;
  }

  private Callback createCallback(Flowlet flowlet, FlowletSpecification flowletSpec) {
    if (flowlet instanceof Callback) {
      return (Callback) flowlet;
    }
    final FailurePolicy failurePolicy = flowletSpec.getFailurePolicy();
    return new Callback() {
      @Override
      public void onSuccess(Object input, InputContext inputContext) {
        // No-op
      }

      @Override
      public FailurePolicy onFailure(Object input, InputContext inputContext, FailureReason reason) {
        return failurePolicy;
      }
    };
  }

  private OutputEmitterFactory outputEmitterFactory(final BasicFlowletContext flowletContext,
                                                    final String flowletName,
                                                    final QueueClientFactory queueClientFactory,
                                                    final ImmutableList.Builder<ProducerSupplier> producerBuilder,
                                                    final Table<Node, String, Set<QueueSpecification>> queueSpecs) {
    return new OutputEmitterFactory() {
      @Override
      public <T> OutputEmitter<T> create(String outputName, TypeToken<T> type) {
        try {
          // first iterate over all queue specifications to find the queue name and all consumer flowlet ids
          QueueName queueName = null;
          List<String> consumerFlowlets = Lists.newLinkedList();
          Node flowlet = Node.flowlet(flowletName);
          Schema schema = schemaGenerator.generate(type.getType());
          for (Map.Entry<String, Set<QueueSpecification>> entry : queueSpecs.row(flowlet).entrySet()) {
            for (QueueSpecification queueSpec : entry.getValue()) {
              if (queueSpec.getQueueName().getSimpleName().equals(outputName)
                && queueSpec.getOutputSchema().equals(schema)) {

                queueName = queueSpec.getQueueName();
                consumerFlowlets.add(entry.getKey());
                break;
              }
            }
          }
          if (queueName == null) {
            throw new IllegalArgumentException(String.format("No queue specification found for %s, %s",
                                                             flowletName, type));
          }

          // create a metric collector for this queue, and also one for each consumer flowlet
          final MetricsContext metrics = flowletContext.getProgramMetrics()
            .childContext(Constants.Metrics.Tag.FLOWLET_QUEUE, outputName);
          final MetricsContext producerMetrics = metrics.childContext(
            Constants.Metrics.Tag.PRODUCER, flowletContext.getFlowletId());
          final Iterable<MetricsContext> consumerMetrics =
            Iterables.transform(consumerFlowlets, new Function<String, MetricsContext>() {
              @Override
              public MetricsContext apply(String consumer) {
                return producerMetrics.childContext(
                  Constants.Metrics.Tag.CONSUMER, consumer);
              }});

          // create a queue metrics emitter that emit to all of the above collectors
          ProducerSupplier producerSupplier = new ProducerSupplier(queueName, queueClientFactory, new QueueMetrics() {
            @Override
            public void emitEnqueue(int count) {
              metrics.increment("process.events.out", count);
              for (MetricsContext collector : consumerMetrics) {
                collector.increment("queue.pending", count);
              }
            }
            @Override
            public void emitEnqueueBytes(int bytes) {
              // no-op
            }
          });
          producerBuilder.add(producerSupplier);
          return new DatumOutputEmitter<>(producerSupplier, schema, datumWriterFactory.create(type, schema));
        } catch (Exception e) {
          throw Throwables.propagate(e);
        }
      }
    };
  }

  private ProcessMethodFactory processMethodFactory(final Flowlet flowlet) {
    return new ProcessMethodFactory() {
      @Override
      public <T> ProcessMethod<T> create(Method method, int maxRetries) {
        return ReflectionProcessMethod.create(flowlet, method, maxRetries);
      }
    };
  }

  private ProcessSpecificationFactory processSpecificationFactory(
    final BasicFlowletContext flowletContext, final DataFabricFacade dataFabricFacade,
    final QueueReaderFactory queueReaderFactory, final String flowletName,
    final Table<Node, String, Set<QueueSpecification>> queueSpecs,
    final ImmutableList.Builder<ConsumerSupplier<?>> queueConsumerSupplierBuilder,
    final SchemaCache schemaCache) {

    final Id.Program program = Id.Flow.from(flowletContext.getNamespaceId(),
                                            flowletContext.getApplicationId(),
                                            ProgramType.FLOW,
                                            flowletContext.getFlowId());
    return new ProcessSpecificationFactory() {
      @Override
      public <T> ProcessSpecification create(Set<String> inputNames, Schema schema, TypeToken<T> dataType,
                                             ProcessMethod<T> method, ConsumerConfig consumerConfig, int batchSize,
                                             Tick tickAnnotation) {
        List<QueueReader<T>> queueReaders = Lists.newLinkedList();

        for (Map.Entry<Node, Set<QueueSpecification>> entry : queueSpecs.column(flowletName).entrySet()) {
          for (QueueSpecification queueSpec : entry.getValue()) {
            final QueueName queueName = queueSpec.getQueueName();

            if (queueSpec.getInputSchema().equals(schema)
              && (inputNames.contains(queueName.getSimpleName())
              || inputNames.contains(FlowletDefinition.ANY_INPUT))) {

              if (entry.getKey().getType() == FlowletConnection.Type.STREAM) {
                ConsumerSupplier<StreamConsumer> consumerSupplier = ConsumerSupplier.create(program.getNamespace(),
                                                                                            flowletContext.getOwners(),
                                                                                            usageRegistry,
                                                                                            dataFabricFacade,
                                                                                            queueName, consumerConfig);
                queueConsumerSupplierBuilder.add(consumerSupplier);
                // No decoding is needed, as a process method can only have StreamEvent as type for consuming stream
                Function<StreamEvent, T> decoder = wrapInputDecoder(flowletContext, null,
                                                                    queueName, new Function<StreamEvent, T>() {
                  @Override
                  @SuppressWarnings("unchecked")
                  public T apply(StreamEvent input) {
                    return (T) input;
                  }
                });

                queueReaders.add(queueReaderFactory.createStreamReader(consumerSupplier, batchSize, decoder));

              } else {
                int numGroups = getNumGroups(Iterables.concat(queueSpecs.row(entry.getKey()).values()), queueName);
                Function<ByteBuffer, T> decoder =
                  wrapInputDecoder(flowletContext, entry.getKey().getName(), // the producer flowlet,
                                   queueName, createInputDatumDecoder(dataType, schema, schemaCache));

                ConsumerSupplier<QueueConsumer> consumerSupplier = ConsumerSupplier.create(program.getNamespace(),
                                                                                           flowletContext.getOwners(),
                                                                                           usageRegistry,
                                                                                           dataFabricFacade, queueName,
                                                                                           consumerConfig, numGroups);
                queueConsumerSupplierBuilder.add(consumerSupplier);
                queueReaders.add(queueReaderFactory.createQueueReader(consumerSupplier, batchSize, decoder));
              }
            }
          }
        }

        // If inputs is needed but there is no available input queue, return null
        if (!inputNames.isEmpty() && queueReaders.isEmpty()) {
          return null;
        }
        return new ProcessSpecification<>(new RoundRobinQueueReader<>(queueReaders), method, tickAnnotation);
      }
    };
  }

  private <T> Function<ByteBuffer, T> createInputDatumDecoder(final TypeToken<T> dataType, final Schema schema,
                                                              final SchemaCache schemaCache) {
    final ReflectionDatumReader<T> datumReader = new ReflectionDatumReader<>(schema, dataType);
    final ByteBufferInputStream byteBufferInput = new ByteBufferInputStream(null);
    final BinaryDecoder decoder = new BinaryDecoder(byteBufferInput);

    return new Function<ByteBuffer, T>() {
      @Nullable
      @Override
      public T apply(ByteBuffer input) {
        byteBufferInput.reset(input);
        try {
          final Schema sourceSchema = schemaCache.get(input);
          Preconditions.checkNotNull(sourceSchema, "Fail to find source schema.");
          return datumReader.read(decoder, sourceSchema);
        } catch (IOException e) {
          throw Throwables.propagate(e);
        }
      }

      @Override
      public String toString() {
        return Objects.toStringHelper(this)
          .add("dataType", dataType)
          .add("schema", schema)
          .toString();
      }
    };
  }

  private <S, T> Function<S, T> wrapInputDecoder(final BasicFlowletContext context,
                                                 final String producerName,
                                                 final QueueName queueName,
                                                 final Function<S, T> inputDecoder) {
    final String eventsMetricsName = "process.events.in";
    final String queue = queueName.getSimpleName();
    final ImmutablePair<String, String> producerAndQueue = producerName == null ? null :
      new ImmutablePair<>(producerName, queue);
    return new Function<S, T>() {
      @Override
      public T apply(S source) {
        context.getQueueMetrics(queue).increment(eventsMetricsName, 1);
        context.getQueueMetrics(queue).increment("process.tuples.read", 1);
        if (producerAndQueue != null) {
          context.getProducerMetrics(producerAndQueue).increment("queue.pending", -1);
        }
        return inputDecoder.apply(source);
      }
    };
  }

  private SchemaCache createSchemaCache(Program program) throws Exception {
    ImmutableSet.Builder<Schema> schemas = ImmutableSet.builder();

    for (FlowSpecification flowSpec : program.getApplicationSpecification().getFlows().values()) {
      for (FlowletDefinition flowletDef : flowSpec.getFlowlets().values()) {
        schemas.addAll(Iterables.concat(flowletDef.getInputs().values()));
        schemas.addAll(Iterables.concat(flowletDef.getOutputs().values()));
      }
    }

    // Temp fix for ENG-3949. Always add old stream event schema.
    // TODO: Remove it later. The right thing to do is to have schemas history being stored to support schema
    // evolution. By design, as long as the schema cache is populated with old schema, the type projection logic
    // in the decoder would handle it correctly.
    schemas.add(schemaGenerator.generate(StreamEventData.class));


    return new SchemaCache(schemas.build(), program.getClassLoader());
  }

  /**
   * Create a initializer to be executed during the flowlet driver initialization.
   */
  private Service createServiceHook(String flowletName, Iterable<ConsumerSupplier<?>> consumerSuppliers,
                                    AtomicReference<FlowletProgramController> controller) {
    final List<Id.Stream> streams = Lists.newArrayList();
    for (ConsumerSupplier<?> consumerSupplier : consumerSuppliers) {
      QueueName queueName = consumerSupplier.getQueueName();
      if (queueName.isStream()) {
        streams.add(queueName.toStreamId());
      }
    }

    // If no stream, returns a no-op Service
    if (streams.isEmpty()) {
      return new AbstractService() {
        @Override
        protected void doStart() {
          notifyStarted();
        }

        @Override
        protected void doStop() {
          notifyStopped();
        }
      };
    }
    return new FlowletServiceHook(flowletName, streamCoordinatorClient, streams, controller);
  }

  private interface ProcessMethodFactory {
    <T> ProcessMethod<T> create(Method method, int maxRetries);
  }

  private interface ProcessSpecificationFactory {
    /**
     * Returns a {@link ProcessSpecification} for invoking the given process method. {@code null} is returned if
     * no input is available for the given method.
     */
    <T> ProcessSpecification create(Set<String> inputNames, Schema schema, TypeToken<T> dataType,
                                    ProcessMethod<T> method, ConsumerConfig consumerConfig, int batchSize,
                                    Tick tickAnnotation);
  }

  /**
   * This service is for start/stop listening to changes in stream property, through the help of
   * {@link StreamCoordinatorClient}, so that it can react to changes and properly reconfigure stream consumers used by
   * the flowlet. This hook is provided to {@link FlowletRuntimeService} and being start/stop
   * when the driver start/stop.
   */
  private static final class FlowletServiceHook extends AbstractService {

    private final StreamCoordinatorClient streamCoordinatorClient;
    private final List<Id.Stream> streams;
    private final AtomicReference<FlowletProgramController> controller;
    private final Executor executor;
    private final Lock suspendLock = new ReentrantLock();
    private final StreamPropertyListener propertyListener;
    private Cancellable cancellable;

    private FlowletServiceHook(final String flowletName, StreamCoordinatorClient streamCoordinatorClient,
                               List<Id.Stream> streams, AtomicReference<FlowletProgramController> controller) {
      this.streamCoordinatorClient = streamCoordinatorClient;
      this.streams = streams;
      this.controller = controller;
      this.executor = ExecutorUtils.newThreadExecutor(Threads.createDaemonThreadFactory("flowlet-stream-update-%d"));
      this.propertyListener = new StreamPropertyListener() {
        @Override
        public void ttlChanged(Id.Stream streamId, long ttl) {
          LOG.debug("TTL for stream '{}' changed to {} for flowlet '{}'", streamId, ttl, flowletName);
          suspendAndResume();
        }

        @Override
        public void generationChanged(Id.Stream streamId, int generation) {
          LOG.debug("Generation for stream '{}' changed to {} for flowlet '{}'", streamId, generation, flowletName);
          suspendAndResume();
        }

        @Override
        public void deleted(Id.Stream streamId) {
          LOG.debug("Properties deleted for stream '{}'", streamId);
          suspendAndResume();
        }
      };
    }

    @Override
    protected void doStart() {
      final List<Cancellable> cancellables = Lists.newArrayList();
      this.cancellable = new Cancellable() {
        @Override
        public void cancel() {
          for (Cancellable c : cancellables) {
            c.cancel();
          }
        }
      };

      for (Id.Stream stream : streams) {
        cancellables.add(streamCoordinatorClient.addListener(stream, propertyListener));
      }
      notifyStarted();
    }

    @Override
    protected void doStop() {
      if (cancellable != null) {
        cancellable.cancel();
      }
      notifyStopped();
    }

    private void suspendAndResume() {
      // This method only get called from the property change listener, which it won't start listening
      // until the flowlet runtime service started, which the FlowletProgramRunner guarantee that
      // the program controller is never null since it sets the program controller reference before
      // starting the flowlet runtime service.
      final FlowletProgramController flowletController = controller.get();
      flowletController.addListener(new AbstractListener() {
        @Override
        public void init(ProgramController.State currentState, @Nullable Throwable cause) {
          // If the flowlet is already running, perform suspend and resume to close and open consumers
          // so that it will pickup the right stream file again
          if (currentState == ProgramController.State.ALIVE) {
            alive();
          }
        }

        @Override
        public void alive() {
          // Only do suspend and resume when the flowlet transit into alive state.
          // It's ok for the flowlet tries to read from entries that are in older stream file (since it's part
          // of the contract on truncating a stream) before this method gets triggered.
          suspendLock.lock();
          try {
            flowletController.suspend().get();
            flowletController.resume().get();
          } catch (Exception e) {
            LOG.error("Failed to suspend and resume flowlet.", e);
          } finally {
            suspendLock.unlock();
          }
        }
      }, executor);
    }
  }
}
