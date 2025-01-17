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

package co.cask.cdap.internal.app.runtime.batch;

import co.cask.cdap.api.Resources;
import co.cask.cdap.api.data.batch.Input;
import co.cask.cdap.api.data.batch.InputFormatProvider;
import co.cask.cdap.api.data.batch.OutputFormatProvider;
import co.cask.cdap.api.data.batch.Split;
import co.cask.cdap.api.data.stream.StreamBatchReadable;
import co.cask.cdap.api.dataset.Dataset;
import co.cask.cdap.api.mapreduce.MapReduceContext;
import co.cask.cdap.api.mapreduce.MapReduceSpecification;
import co.cask.cdap.api.metrics.Metrics;
import co.cask.cdap.api.metrics.MetricsCollectionService;
import co.cask.cdap.api.metrics.MetricsContext;
import co.cask.cdap.api.metrics.MultiMetricsContext;
import co.cask.cdap.api.plugin.Plugin;
import co.cask.cdap.app.metrics.ProgramUserMetrics;
import co.cask.cdap.app.program.Program;
import co.cask.cdap.app.runtime.Arguments;
import co.cask.cdap.common.conf.Constants;
import co.cask.cdap.common.logging.LoggingContext;
import co.cask.cdap.data.stream.StreamInputFormatProvider;
import co.cask.cdap.data2.dataset2.DatasetFramework;
import co.cask.cdap.data2.metadata.lineage.AccessType;
import co.cask.cdap.data2.transaction.stream.StreamAdmin;
import co.cask.cdap.internal.app.runtime.AbstractContext;
import co.cask.cdap.internal.app.runtime.batch.dataset.DatasetInputFormatProvider;
import co.cask.cdap.internal.app.runtime.batch.dataset.DatasetOutputFormatProvider;
import co.cask.cdap.internal.app.runtime.batch.dataset.input.MapperInput;
import co.cask.cdap.internal.app.runtime.distributed.LocalizeResource;
import co.cask.cdap.internal.app.runtime.plugin.PluginInstantiator;
import co.cask.cdap.internal.app.runtime.workflow.BasicWorkflowToken;
import co.cask.cdap.internal.app.runtime.workflow.WorkflowProgramInfo;
import co.cask.cdap.logging.context.MapReduceLoggingContext;
import co.cask.cdap.proto.Id;
import co.cask.tephra.TransactionContext;
import co.cask.tephra.TransactionSystemClient;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.twill.api.RunId;
import org.apache.twill.discovery.DiscoveryServiceClient;

import java.io.File;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * Mapreduce job runtime context
 */
public class BasicMapReduceContext extends AbstractContext implements MapReduceContext {

  private final MapReduceSpecification spec;
  private final LoggingContext loggingContext;
  private final WorkflowProgramInfo workflowProgramInfo;
  private final Metrics userMetrics;
  private final Map<String, Plugin> plugins;
  private final Map<String, OutputFormatProvider> outputFormatProviders;
  private final TransactionContext txContext;
  private final StreamAdmin streamAdmin;
  private final File pluginArchive;
  private final Map<String, LocalizeResource> resourcesToLocalize;

  // key is input name, value is the MapperInput (configuration info) for that input
  private Map<String, MapperInput> inputs;
  private Job job;
  private Resources mapperResources;
  private Resources reducerResources;

  public BasicMapReduceContext(Program program,
                               RunId runId,
                               Arguments runtimeArguments,
                               MapReduceSpecification spec,
                               @Nullable WorkflowProgramInfo workflowProgramInfo,
                               DiscoveryServiceClient discoveryServiceClient,
                               MetricsCollectionService metricsCollectionService,
                               TransactionSystemClient txClient,
                               DatasetFramework dsFramework,
                               StreamAdmin streamAdmin,
                               @Nullable File pluginArchive,
                               @Nullable PluginInstantiator pluginInstantiator) {
    super(program, runId, runtimeArguments, Collections.<String>emptySet(),
          createMetricsContext(program, runId.getId(), metricsCollectionService, workflowProgramInfo),
          dsFramework, txClient, discoveryServiceClient, false, pluginInstantiator);
    this.workflowProgramInfo = workflowProgramInfo;
    this.userMetrics = new ProgramUserMetrics(getProgramMetrics());
    this.loggingContext = createLoggingContext(program.getId(), runId);
    this.spec = spec;
    this.mapperResources = spec.getMapperResources();
    this.reducerResources = spec.getReducerResources();

    this.plugins = Maps.newHashMap(program.getApplicationSpecification().getPlugins());
    this.txContext = getDatasetCache().newTransactionContext();
    this.streamAdmin = streamAdmin;
    this.pluginArchive = pluginArchive;
    this.resourcesToLocalize = new HashMap<>();

    this.inputs = new HashMap<>();
    this.outputFormatProviders = new HashMap<>();

    if (spec.getInputDataSet() != null) {
      addInput(Input.ofDataset(spec.getInputDataSet()));
    }
    if (spec.getOutputDataSet() != null) {
      setOutput(spec.getOutputDataSet());
    }
  }

  public TransactionContext getTransactionContext() {
    return txContext;
  }

  private LoggingContext createLoggingContext(Id.Program programId, RunId runId) {
    return new MapReduceLoggingContext(programId.getNamespaceId(), programId.getApplicationId(),
                                       programId.getId(), runId.getId());
  }

  @Override
  public String toString() {
    return String.format("job=%s,=%s", spec.getName(), super.toString());
  }

  @Override
  public Map<String, Plugin> getPlugins() {
    return plugins;
  }

  @Override
  public MapReduceSpecification getSpecification() {
    return spec;
  }

  /**
   * Returns the WorkflowToken if the MapReduce program is executed as a part of the Workflow.
   */
  @Override
  @Nullable
  public BasicWorkflowToken getWorkflowToken() {
    return workflowProgramInfo == null ? null : workflowProgramInfo.getWorkflowToken();
  }

  public void setJob(Job job) {
    this.job = job;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T getHadoopJob() {
    return (T) job;
  }

  @Override
  public void setInput(StreamBatchReadable stream) {
    setInput(new StreamInputFormatProvider(getProgram().getId().getNamespace(), stream, streamAdmin));
  }

  @Override
  public void setInput(String datasetName) {
    setInput(datasetName, ImmutableMap.<String, String>of());
  }

  @Override
  public void setInput(String datasetName, Map<String, String> arguments) {
    setInput(createInputFormatProvider(datasetName, arguments, null));
  }

  @Override
  public void setInput(String datasetName, List<Split> splits) {
    setInput(datasetName, ImmutableMap.<String, String>of(), splits);
  }

  @Override
  public void setInput(String datasetName, Map<String, String> arguments, List<Split> splits) {
    setInput(createInputFormatProvider(datasetName, arguments, splits));
  }

  @Override
  public void setInput(String inputDatasetName, Dataset dataset) {
    setInput(new DatasetInputFormatProvider(inputDatasetName, Collections.<String, String>emptyMap(),
                                            dataset, null, MapReduceBatchReadableInputFormat.class));
  }

  @Override
  public void setInput(InputFormatProvider inputFormatProvider) {
    // with the setInput method, only 1 input will be set, and so the name does not matter much.
    // make it immutable to prevent calls to addInput after setting a single input.
    inputs = ImmutableMap.of(inputFormatProvider.getInputFormatClassName(), new MapperInput(inputFormatProvider));
  }

  @Override
  public void addInput(Input input) {
    addInput(input, null);
  }

  @SuppressWarnings("unchecked")
  private void addInput(String inputName, InputFormatProvider inputFormatProvider, @Nullable Class<?> mapperClass) {
    // prevent calls to addInput after setting a single input.
    if (inputs instanceof ImmutableMap) {
      throw new IllegalStateException("Can not add inputs after setting a single input.");
    }

    if (mapperClass != null && !Mapper.class.isAssignableFrom(mapperClass)) {
      throw new IllegalArgumentException("Specified mapper class must extend Mapper.");
    }
    inputs.put(inputName, new MapperInput(inputFormatProvider, (Class<? extends Mapper>) mapperClass));
  }

  @Override
  public void addInput(Input input, @Nullable Class<?> mapperCls) {
    if (input instanceof Input.DatasetInput) {
      Input.DatasetInput datasetInput = (Input.DatasetInput) input;
      // the createInput method call will translate the input name from stream uri to the stream's name
      // see the implementation of createInput for more information on the hack
      Input.InputFormatProviderInput createdInput = createInput(datasetInput);
      addInput(createdInput.getAlias(), createdInput.getInputFormatProvider(), mapperCls);
    } else if (input instanceof Input.StreamInput) {
      StreamBatchReadable streamBatchReadable = ((Input.StreamInput) input).getStreamBatchReadable();
      addInput(input.getAlias(),
               new StreamInputFormatProvider(getProgram().getId().getNamespace(), streamBatchReadable, streamAdmin),
               mapperCls);
    } else if (input instanceof Input.InputFormatProviderInput) {
      addInput(input.getAlias(), ((Input.InputFormatProviderInput) input).getInputFormatProvider(), mapperCls);
    }
  }

  @Override
  public void setOutput(String datasetName) {
    clearOutputs();
    addOutput(datasetName);
  }

  //TODO: update this to allow a BatchWritable once the DatasetOutputFormat can support taking an instance
  //      and not just the name
  @Override
  public void setOutput(String datasetName, Dataset dataset) {
    clearOutputs();
    addOutput(datasetName, new DatasetOutputFormatProvider(datasetName, Collections.<String, String>emptyMap(),
                                                           dataset, MapReduceBatchWritableOutputFormat.class));
  }

  @Override
  public void addOutput(String datasetName) {
    addOutput(datasetName, Collections.<String, String>emptyMap());
  }

  @Override
  public void addOutput(String datasetName, Map<String, String> arguments) {
    // we can delay the instantiation of the Dataset to later, but for now, we still have to maintain backwards
    // compatibility for the #setOutput(String, Dataset) method, so delaying the instantiation of this dataset will
    // bring about code complexity without much benefit. Once #setOutput(String, Dataset) is removed, we can postpone
    // this dataset instantiation
    addOutput(datasetName, new DatasetOutputFormatProvider(datasetName, arguments,
                                                           getDataset(datasetName, arguments, AccessType.WRITE),
                                                           MapReduceBatchWritableOutputFormat.class));
  }

  @Override
  public void addOutput(String outputName, OutputFormatProvider outputFormatProvider) {
    this.outputFormatProviders.put(outputName, outputFormatProvider);
  }

  private void clearOutputs() {
    this.outputFormatProviders.clear();
  }

  /**
   * Gets the MapperInputs for this MapReduce job.
   *
   * @return a mapping from input name to the MapperInputs for that input
   */
  Map<String, MapperInput> getMapperInputs() {
    return ImmutableMap.copyOf(inputs);
  }

  /**
   * Gets the OutputFormatProviders for this MapReduce job.
   *
   * @return the OutputFormatProviders for the MapReduce job
   */
  Map<String, OutputFormatProvider> getOutputFormatProviders() {
    return ImmutableMap.copyOf(outputFormatProviders);
  }

  @Override
  public void setMapperResources(Resources resources) {
    this.mapperResources = resources;
  }

  @Override
  public void setReducerResources(Resources resources) {
    this.reducerResources = resources;
  }

  @Override
  public Metrics getMetrics() {
    return userMetrics;
  }

  public LoggingContext getLoggingContext() {
    return loggingContext;
  }

  public Resources getMapperResources() {
    return mapperResources;
  }

  public Resources getReducerResources() {
    return reducerResources;
  }

  public File getPluginArchive() {
    return pluginArchive;
  }

  /**
   * Returns the information about Workflow if the MapReduce program is executed
   * as a part of it, otherwise {@code null} is returned.
   */
  @Override
  @Nullable
  public WorkflowProgramInfo getWorkflowInfo() {
    return workflowProgramInfo;
  }

  @Override
  public void localize(String name, URI uri) {
    localize(name, uri, false);
  }

  @Override
  public void localize(String name, URI uri, boolean archive) {
    resourcesToLocalize.put(name, new LocalizeResource(uri, archive));
  }

  Map<String, LocalizeResource> getResourcesToLocalize() {
    return resourcesToLocalize;
  }


  private Input.InputFormatProviderInput createInput(Input.DatasetInput datasetInput) {
    String datasetName = datasetInput.getName();
    Map<String, String> datasetArgs = datasetInput.getArguments();
    // keep track of the original alias to set it on the created Input before returning it
    String originalAlias = datasetInput.getAlias();

    // TODO: It's a hack for stream. It was introduced in Reactor 2.2.0. Fix it when addressing CDAP-4158.
    // This check is needed due to the implementation of AbstractMapReduce#useStreamInput(StreamBatchReadable).
    // It can probably be removed once that method is removed (deprecated currently).
    if (datasetName.startsWith(Constants.Stream.URL_PREFIX)) {
      StreamBatchReadable streamBatchReadable = new StreamBatchReadable(URI.create(datasetName));
      Input input = Input.of(streamBatchReadable.getStreamName(),
                             new StreamInputFormatProvider(getProgram().getId().getNamespace(),
                                                           streamBatchReadable, streamAdmin));
      return (Input.InputFormatProviderInput) input.alias(originalAlias);
    }
    DatasetInputFormatProvider datasetInputFormatProvider =
      new DatasetInputFormatProvider(datasetName, datasetArgs, getDataset(datasetName, datasetArgs, AccessType.READ),
                                     datasetInput.getSplits(), MapReduceBatchReadableInputFormat.class);
    return (Input.InputFormatProviderInput) Input.of(datasetName, datasetInputFormatProvider).alias(originalAlias);
  }

  private InputFormatProvider createInputFormatProvider(String datasetName,
                                                        Map<String, String> datasetArgs,
                                                        @Nullable List<Split> splits) {
    return createInput((Input.DatasetInput) Input.ofDataset(datasetName, datasetArgs, splits)).getInputFormatProvider();
  }

  private static MetricsContext createMetricsContext(Program program, String runId, MetricsCollectionService service,
                                                     @Nullable WorkflowProgramInfo workflowProgramInfo) {
    Map<String, String> tags = Maps.newHashMap();
    tags.putAll(getMetricsContext(program, runId));

    MetricsContext programMetricsContext = service.getContext(tags);

    if (workflowProgramInfo == null) {
      return programMetricsContext;
    }

    // If running inside Workflow, add the WorkflowMetricsContext as well
    tags = Maps.newHashMap();
    tags.put(Constants.Metrics.Tag.NAMESPACE, program.getNamespaceId());
    tags.put(Constants.Metrics.Tag.APP, program.getApplicationId());
    tags.put(Constants.Metrics.Tag.WORKFLOW, workflowProgramInfo.getName());
    tags.put(Constants.Metrics.Tag.RUN_ID, workflowProgramInfo.getRunId().getId());
    tags.put(Constants.Metrics.Tag.NODE, workflowProgramInfo.getNodeId());

    return new MultiMetricsContext(programMetricsContext, service.getContext(tags));
  }
}
