/*
 * Copyright © 2016 Cask Data, Inc.
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

package co.cask.cdap.app.runtime.spark;

import co.cask.cdap.api.app.ApplicationSpecification;
import co.cask.cdap.api.metrics.MetricsCollectionService;
import co.cask.cdap.api.spark.Spark;
import co.cask.cdap.api.spark.SparkSpecification;
import co.cask.cdap.app.program.Program;
import co.cask.cdap.app.runtime.Arguments;
import co.cask.cdap.app.runtime.ProgramClassLoaderFilterProvider;
import co.cask.cdap.app.runtime.ProgramController;
import co.cask.cdap.app.runtime.ProgramOptions;
import co.cask.cdap.app.runtime.ProgramRunner;
import co.cask.cdap.app.runtime.spark.submit.DistributedSparkSubmitter;
import co.cask.cdap.app.runtime.spark.submit.LocalSparkSubmitter;
import co.cask.cdap.app.runtime.spark.submit.SparkSubmitter;
import co.cask.cdap.app.store.Store;
import co.cask.cdap.common.app.RunIds;
import co.cask.cdap.common.conf.CConfiguration;
import co.cask.cdap.common.conf.Constants;
import co.cask.cdap.common.lang.FilterClassLoader;
import co.cask.cdap.common.lang.InstantiatorFactory;
import co.cask.cdap.common.lang.PropertyFieldSetter;
import co.cask.cdap.data2.dataset2.DatasetFramework;
import co.cask.cdap.data2.metadata.writer.ProgramContextAware;
import co.cask.cdap.data2.transaction.stream.StreamAdmin;
import co.cask.cdap.internal.app.runtime.AbstractProgramRunnerWithPlugin;
import co.cask.cdap.internal.app.runtime.DataSetFieldSetter;
import co.cask.cdap.internal.app.runtime.MetricsFieldSetter;
import co.cask.cdap.internal.app.runtime.ProgramOptionConstants;
import co.cask.cdap.internal.app.runtime.ProgramRunners;
import co.cask.cdap.internal.app.runtime.plugin.PluginInstantiator;
import co.cask.cdap.internal.app.runtime.workflow.NameMappedDatasetFramework;
import co.cask.cdap.internal.app.runtime.workflow.WorkflowProgramInfo;
import co.cask.cdap.internal.lang.Reflections;
import co.cask.cdap.proto.Id;
import co.cask.cdap.proto.ProgramRunStatus;
import co.cask.cdap.proto.ProgramType;
import co.cask.tephra.TransactionSystemClient;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.io.Closeables;
import com.google.common.reflect.TypeToken;
import com.google.common.util.concurrent.Service;
import com.google.common.util.concurrent.Uninterruptibles;
import com.google.inject.Inject;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.twill.api.RunId;
import org.apache.twill.common.Threads;
import org.apache.twill.discovery.DiscoveryServiceClient;
import org.apache.twill.internal.ServiceListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;

/**
 * The {@link ProgramRunner} that executes Spark program.
 */
final class SparkProgramRunner extends AbstractProgramRunnerWithPlugin implements ProgramClassLoaderFilterProvider {

  private static final Logger LOG = LoggerFactory.getLogger(SparkProgramRunner.class);
  static final FilterClassLoader.Filter SPARK_PROGRAM_CLASS_LOADER_FILTER = new FilterClassLoader.Filter() {

    final FilterClassLoader.Filter defaultFilter = FilterClassLoader.defaultFilter();

    @Override
    public boolean acceptResource(String resource) {
      return resource.startsWith("co/cask/cdap/api/spark/") || resource.startsWith("scala/")
        || resource.startsWith("org/apache/spark/") || resource.startsWith("akka/")
        || defaultFilter.acceptResource(resource);
    }

    @Override
    public boolean acceptPackage(String packageName) {
      if (packageName.equals("co.cask.cdap.api.spark") || packageName.startsWith("co.cask.cdap.api.spark.")) {
        return true;
      }
      if (packageName.equals("scala") || packageName.startsWith("scala.")) {
        return true;
      }
      if (packageName.equals("org.apache.spark") || packageName.startsWith("org.apache.spark.")) {
        return true;
      }
      if (packageName.equals("akka") || packageName.startsWith("akka.")) {
        return true;
      }
      return defaultFilter.acceptResource(packageName);
    }
  };

  private final CConfiguration cConf;
  private final Configuration hConf;
  private final TransactionSystemClient txClient;
  private final DatasetFramework datasetFramework;
  private final MetricsCollectionService metricsCollectionService;
  private final DiscoveryServiceClient discoveryServiceClient;
  private final StreamAdmin streamAdmin;
  private final Store store;

  @Inject
  SparkProgramRunner(CConfiguration cConf, Configuration hConf, TransactionSystemClient txClient,
                     DatasetFramework datasetFramework, MetricsCollectionService metricsCollectionService,
                     DiscoveryServiceClient discoveryServiceClient, StreamAdmin streamAdmin, Store store) {
    super(cConf);
    this.cConf = cConf;
    this.hConf = hConf;
    this.txClient = txClient;
    this.datasetFramework = datasetFramework;
    this.metricsCollectionService = metricsCollectionService;
    this.discoveryServiceClient = discoveryServiceClient;
    this.streamAdmin = streamAdmin;
    this.store = store;
  }

  @Override
  public ProgramController run(Program program, ProgramOptions options) {
    // Get the RunId first. It is used for the creation of the ClassLoader closing thread.
    Arguments arguments = options.getArguments();
    RunId runId = RunIds.fromString(arguments.getOption(ProgramOptionConstants.RUN_ID));

    Deque<Closeable> closeables = new LinkedList<>();
    closeables.addFirst(createClassLoaderCloseable(program.getId(), runId));

    try {
      // Extract and verify parameters
      ApplicationSpecification appSpec = program.getApplicationSpecification();
      Preconditions.checkNotNull(appSpec, "Missing application specification.");

      ProgramType processorType = program.getType();
      Preconditions.checkNotNull(processorType, "Missing processor type.");
      Preconditions.checkArgument(processorType == ProgramType.SPARK, "Only Spark process type is supported.");

      SparkSpecification spec = appSpec.getSpark().get(program.getName());
      Preconditions.checkNotNull(spec, "Missing SparkSpecification for %s", program.getName());

      // Get the WorkflowProgramInfo if it is started by Workflow
      WorkflowProgramInfo workflowInfo = WorkflowProgramInfo.create(arguments);
      DatasetFramework programDatasetFramework = workflowInfo == null ?
        datasetFramework :
        NameMappedDatasetFramework.createFromWorkflowProgramInfo(datasetFramework, workflowInfo, appSpec);

      // Setup dataset framework context, if required
      if (programDatasetFramework instanceof ProgramContextAware) {
        Id.Program programId = program.getId();
        ((ProgramContextAware) programDatasetFramework).initContext(new Id.Run(programId, runId.getId()));
      }

      PluginInstantiator pluginInstantiator = createPluginInstantiator(options, program.getClassLoader());
      if (pluginInstantiator != null) {
        closeables.addFirst(pluginInstantiator);
      }

      SparkRuntimeContext runtimeContext = new SparkRuntimeContext(new Configuration(hConf), program, runId,
                                                                   options.getUserArguments().asMap(),
                                                                   txClient, programDatasetFramework,
                                                                   discoveryServiceClient,
                                                                   metricsCollectionService, streamAdmin, workflowInfo,
                                                                   pluginInstantiator);
      closeables.addFirst(runtimeContext);

      Spark spark;
      try {
        spark = new InstantiatorFactory(false).get(TypeToken.of(program.<Spark>getMainClass())).create();

        // Fields injection
        Reflections.visit(spark, spark.getClass(),
                          new PropertyFieldSetter(spec.getProperties()),
                          new DataSetFieldSetter(runtimeContext.getDatasetCache()),
                          new MetricsFieldSetter(runtimeContext));
      } catch (Exception e) {
        LOG.error("Failed to instantiate Spark class for {}", spec.getClassName(), e);
        throw Throwables.propagate(e);
      }

      SparkSubmitter submitter = SparkRuntimeContextConfig.isLocal(hConf)
        ? new LocalSparkSubmitter()
        : new DistributedSparkSubmitter(hConf,
                                        options.getArguments().getOption(Constants.AppFabric.APP_SCHEDULER_QUEUE));

      Service sparkRuntimeService = new SparkRuntimeService(cConf, spark, getPluginArchive(options),
                                                            runtimeContext, submitter);

      sparkRuntimeService.addListener(
        createRuntimeServiceListener(program.getId(), runId, arguments, options.getUserArguments(), closeables, store),
        Threads.SAME_THREAD_EXECUTOR);
      ProgramController controller = new SparkProgramController(sparkRuntimeService, runtimeContext);

      LOG.info("Starting Spark Job: {}", runtimeContext);
      if (SparkRuntimeContextConfig.isLocal(hConf) || UserGroupInformation.isSecurityEnabled()) {
        sparkRuntimeService.start();
      } else {
        ProgramRunners.startAsUser(cConf.get(Constants.CFG_HDFS_USER), sparkRuntimeService);
      }
      return controller;
    } catch (Throwable t) {
      closeAll(closeables);
      throw Throwables.propagate(t);
    }
  }

  @Override
  public FilterClassLoader.Filter getFilter() {
    return SPARK_PROGRAM_CLASS_LOADER_FILTER;
  }

  /**
   * Creates a {@link Closeable} for closing the ClassLoader of this {@link SparkProgramRunner}. The
   * ClassLoader needs to be closed because there is one such ClassLoader created per program execution by
   * the {@link SparkProgramRuntimeProvider} to support concurrent Spark program execution in the same JVM. The
   * {@link Closeable#close()} method will be called when the program being executed through this program runner
   * is completed.
   */
  private Closeable createClassLoaderCloseable(final Id.Program programId, final RunId runId) {
    final ClassLoader classLoader = getClass().getClassLoader();
    return new Closeable() {
      @Override
      public void close() throws IOException {
        Thread t = new Thread("delay-close-thread-" + programId + "-" + runId) {
          @Override
          public void run() {
            // Delay the closing of the ClassLoader because Spark, which uses akka, has an async cleanup process
            // for shutting down threads. During shutdown, there are new classes being loaded.
            Uninterruptibles.sleepUninterruptibly(2, TimeUnit.SECONDS);
            if (classLoader instanceof Closeable) {
              Closeables.closeQuietly((Closeable) classLoader);
              LOG.debug("Closed ClassLoader for SparkProgramRunner of {}, run {}.", programId, runId);
            }
          }
        };
        t.setDaemon(true);
        t.start();
      }
    };
  }

  private void closeAll(Iterable<Closeable> closeables) {
    for (Closeable closeable : closeables) {
      Closeables.closeQuietly(closeable);
    }
  }

  @Nullable
  private File getPluginArchive(ProgramOptions options) {
    if (!options.getArguments().hasOption(ProgramOptionConstants.PLUGIN_ARCHIVE)) {
      return null;
    }
    return new File(options.getArguments().getOption(ProgramOptionConstants.PLUGIN_ARCHIVE));
  }

  /**
   * Creates a service listener to reactor on state changes on {@link SparkRuntimeService}.
   */
  private Service.Listener createRuntimeServiceListener(final Id.Program programId, final RunId runId,
                                                        final Arguments arguments, final Arguments userArgs,
                                                        final Iterable<Closeable> closeables, final Store store) {

    final String twillRunId = arguments.getOption(ProgramOptionConstants.TWILL_RUN_ID);

    return new ServiceListenerAdapter() {
      @Override
      public void starting() {
        //Get start time from RunId
        long startTimeInSeconds = RunIds.getTime(runId, TimeUnit.SECONDS);
        if (startTimeInSeconds == -1) {
          // If RunId is not time-based, use current time as start time
          startTimeInSeconds = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
        }
        store.setStart(programId, runId.getId(), startTimeInSeconds, twillRunId, userArgs.asMap(), arguments.asMap());
      }

      @Override
      public void terminated(Service.State from) {
        closeAll(closeables);
        ProgramRunStatus runStatus = ProgramController.State.COMPLETED.getRunStatus();
        if (from == Service.State.STOPPING) {
          // Service was killed
          runStatus = ProgramController.State.KILLED.getRunStatus();
        }
        store.setStop(programId, runId.getId(), TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()),
                      runStatus);
      }

      @Override
      public void failed(Service.State from, @Nullable Throwable failure) {
        closeAll(closeables);
        store.setStop(programId, runId.getId(), TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()),
                      ProgramController.State.ERROR.getRunStatus(), failure);
      }
    };
  }
}
