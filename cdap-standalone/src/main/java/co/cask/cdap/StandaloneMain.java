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

package co.cask.cdap;

import co.cask.cdap.api.metrics.MetricsCollectionService;
import co.cask.cdap.app.guice.AppFabricServiceRuntimeModule;
import co.cask.cdap.app.guice.AuthorizationModule;
import co.cask.cdap.app.guice.ProgramRunnerRuntimeModule;
import co.cask.cdap.app.guice.ServiceStoreModules;
import co.cask.cdap.app.store.ServiceStore;
import co.cask.cdap.common.ServiceBindException;
import co.cask.cdap.common.conf.CConfiguration;
import co.cask.cdap.common.conf.Constants;
import co.cask.cdap.common.guice.ConfigModule;
import co.cask.cdap.common.guice.DiscoveryRuntimeModule;
import co.cask.cdap.common.guice.IOModule;
import co.cask.cdap.common.guice.KafkaClientModule;
import co.cask.cdap.common.guice.LocationRuntimeModule;
import co.cask.cdap.common.guice.ZKClientModule;
import co.cask.cdap.common.io.URLConnections;
import co.cask.cdap.common.namespace.guice.NamespaceClientRuntimeModule;
import co.cask.cdap.common.startup.ConfigurationLogger;
import co.cask.cdap.common.utils.DirUtils;
import co.cask.cdap.common.utils.OSDetector;
import co.cask.cdap.data.runtime.DataFabricModules;
import co.cask.cdap.data.runtime.DataSetServiceModules;
import co.cask.cdap.data.runtime.DataSetsModules;
import co.cask.cdap.data.stream.StreamAdminModules;
import co.cask.cdap.data.stream.service.StreamService;
import co.cask.cdap.data.stream.service.StreamServiceRuntimeModule;
import co.cask.cdap.data.view.ViewAdminModules;
import co.cask.cdap.data2.audit.AuditModule;
import co.cask.cdap.data2.datafabric.dataset.service.DatasetService;
import co.cask.cdap.explore.client.ExploreClient;
import co.cask.cdap.explore.executor.ExploreExecutorService;
import co.cask.cdap.explore.guice.ExploreClientModule;
import co.cask.cdap.explore.guice.ExploreRuntimeModule;
import co.cask.cdap.explore.service.ExploreServiceUtils;
import co.cask.cdap.gateway.router.NettyRouter;
import co.cask.cdap.gateway.router.RouterModules;
import co.cask.cdap.internal.app.services.AppFabricServer;
import co.cask.cdap.logging.appender.LogAppenderInitializer;
import co.cask.cdap.logging.guice.LoggingModules;
import co.cask.cdap.metadata.MetadataService;
import co.cask.cdap.metadata.MetadataServiceModule;
import co.cask.cdap.metrics.guice.MetricsClientRuntimeModule;
import co.cask.cdap.metrics.guice.MetricsHandlerModule;
import co.cask.cdap.metrics.query.MetricsQueryService;
import co.cask.cdap.notifications.feeds.guice.NotificationFeedServiceRuntimeModule;
import co.cask.cdap.notifications.guice.NotificationServiceRuntimeModule;
import co.cask.cdap.security.guice.SecurityModules;
import co.cask.cdap.security.server.ExternalAuthenticationServer;
import co.cask.cdap.store.guice.NamespaceStoreModule;
import co.cask.tephra.inmemory.InMemoryTransactionService;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.Service;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.counters.Limits;
import org.apache.twill.kafka.client.KafkaClientService;
import org.apache.twill.zookeeper.ZKClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Standalone Main.
 * NOTE: Use AbstractIdleService
 */
public class StandaloneMain {

  // A special key in the CConfiguration to disable UI. It's mainly used for unit-tests that start Standalone.
  public static final String DISABLE_UI = "standalone.disable.ui";

  private static final Logger LOG = LoggerFactory.getLogger(StandaloneMain.class);

  private final Injector injector;
  private final UserInterfaceService userInterfaceService;
  private final NettyRouter router;
  private final MetricsQueryService metricsQueryService;
  private final AppFabricServer appFabricServer;
  private final ServiceStore serviceStore;
  private final StreamService streamService;
  private final MetricsCollectionService metricsCollectionService;
  private final LogAppenderInitializer logAppenderInitializer;
  private final InMemoryTransactionService txService;
  private final MetadataService metadataService;
  private final boolean securityEnabled;
  private final boolean sslEnabled;
  private final CConfiguration cConf;

  private ExternalAuthenticationServer externalAuthenticationServer;
  private final DatasetService datasetService;

  private ExploreExecutorService exploreExecutorService;
  private final ExploreClient exploreClient;

  private final ZKClientService zkClient;
  private final KafkaClientService kafkaClient;

  private StandaloneMain(List<Module> modules, CConfiguration cConf) {
    this.cConf = cConf;

    injector = Guice.createInjector(modules);

    // Start ZK client and Kafka client only when audit is enabled
    boolean auditEnabled = cConf.getBoolean(Constants.Audit.ENABLED);
    if (auditEnabled) {
      zkClient = injector.getInstance(ZKClientService.class);
      kafkaClient = injector.getInstance(KafkaClientService.class);
    } else {
      zkClient = null;
      kafkaClient = null;
    }

    txService = injector.getInstance(InMemoryTransactionService.class);
    router = injector.getInstance(NettyRouter.class);
    metricsQueryService = injector.getInstance(MetricsQueryService.class);
    appFabricServer = injector.getInstance(AppFabricServer.class);
    logAppenderInitializer = injector.getInstance(LogAppenderInitializer.class);

    metricsCollectionService = injector.getInstance(MetricsCollectionService.class);
    datasetService = injector.getInstance(DatasetService.class);
    serviceStore = injector.getInstance(ServiceStore.class);
    streamService = injector.getInstance(StreamService.class);

    if (cConf.getBoolean(DISABLE_UI, false)) {
      userInterfaceService = null;
    } else {
      userInterfaceService = injector.getInstance(UserInterfaceService.class);
    }

    sslEnabled = cConf.getBoolean(Constants.Security.SSL_ENABLED);
    securityEnabled = cConf.getBoolean(Constants.Security.ENABLED);
    if (securityEnabled) {
      externalAuthenticationServer = injector.getInstance(ExternalAuthenticationServer.class);
    }

    boolean exploreEnabled = cConf.getBoolean(Constants.Explore.EXPLORE_ENABLED);
    if (exploreEnabled) {
      ExploreServiceUtils.checkHiveSupport(getClass().getClassLoader());
      exploreExecutorService = injector.getInstance(ExploreExecutorService.class);
    }

    exploreClient = injector.getInstance(ExploreClient.class);
    metadataService = injector.getInstance(MetadataService.class);

    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        try {
          shutDown();
        } catch (Throwable e) {
          LOG.error("Failed to shutdown", e);
          // Because shutdown hooks execute concurrently, the logger may be closed already: thus also print it.
          System.err.println("Failed to shutdown: " + e.getMessage());
          e.printStackTrace(System.err);
        }
      }
    });
  }

  /**
   * INTERNAL METHOD. Returns the guice injector of Standalone. It's for testing only. Use with extra caution.
   */
  @VisibleForTesting
  public Injector getInjector() {
    return injector;
  }

  /**
   * Start the service.
   */
  public void startUp() throws Exception {
    // Workaround for release of file descriptors opened by URLClassLoader - https://issues.cask.co/browse/CDAP-2841
    URLConnections.setDefaultUseCaches(false);

    cleanupTempDir();

    ConfigurationLogger.logImportantConfig(cConf);

    // Start all the services.
    if (zkClient != null) {
      zkClient.startAndWait();
    }

    if (kafkaClient != null) {
      kafkaClient.startAndWait();
    }

    txService.startAndWait();
    metricsCollectionService.startAndWait();
    datasetService.startAndWait();
    serviceStore.startAndWait();
    streamService.startAndWait();

    // It is recommended to initialize log appender after datasetService is started,
    // since log appender instantiates a dataset.
    logAppenderInitializer.initialize();

    Service.State state = appFabricServer.startAndWait();
    if (state != Service.State.RUNNING) {
      throw new Exception("Failed to start Application Fabric");
    }

    metricsQueryService.startAndWait();
    router.startAndWait();

    if (userInterfaceService != null) {
      userInterfaceService.startAndWait();
    }

    if (securityEnabled) {
      externalAuthenticationServer.startAndWait();
    }

    if (exploreExecutorService != null) {
      exploreExecutorService.startAndWait();
    }
    metadataService.startAndWait();

    String protocol = sslEnabled ? "https" : "http";
    int dashboardPort = sslEnabled ?
      cConf.getInt(Constants.Dashboard.SSL_BIND_PORT) :
      cConf.getInt(Constants.Dashboard.BIND_PORT);
    System.out.println("Standalone CDAP started successfully.");
    System.out.printf("Connect to the CDAP UI at %s://%s:%d\n", protocol, "localhost", dashboardPort);
  }

  /**
   * Shutdown the service.
   */
  public void shutDown() {
    LOG.info("Shutting down Standalone CDAP");

    try {
      // order matters: first shut down UI 'cause it will stop working after router is down
      if (userInterfaceService != null) {
        userInterfaceService.stopAndWait();
      }
      //  shut down router to stop all incoming traffic
      router.stopAndWait();
      // now the stream writer and the explore service (they need tx)
      streamService.stopAndWait();
      if (exploreExecutorService != null) {
        exploreExecutorService.stopAndWait();
      }
      exploreClient.close();
      metadataService.stopAndWait();
      serviceStore.stopAndWait();
      // app fabric will also stop all programs
      appFabricServer.stopAndWait();
      // all programs are stopped: dataset service, metrics, transactions can stop now
      datasetService.stopAndWait();
      metricsQueryService.stopAndWait();
      txService.stopAndWait();

      if (securityEnabled) {
        // auth service is on the side anyway
        externalAuthenticationServer.stopAndWait();
      }
      logAppenderInitializer.close();

      if (kafkaClient != null) {
        kafkaClient.stopAndWait();
      }

      if (zkClient != null) {
        zkClient.startAndWait();
      }
    } catch (Throwable e) {
      LOG.error("Exception during shutdown", e);
      // We can't do much but exit. Because there was an exception, some non-daemon threads may still be running.
      // Therefore System.exit() won't do it, we need to force a halt.
      Runtime.getRuntime().halt(1);
    } finally {
      cleanupTempDir();
    }
  }

  private void cleanupTempDir() {
    File tmpDir = new File(cConf.get(Constants.CFG_LOCAL_DATA_DIR),
                           cConf.get(Constants.AppFabric.TEMP_DIR)).getAbsoluteFile();

    if (!tmpDir.isDirectory()) {
      return;
    }

    try {
      DirUtils.deleteDirectoryContents(tmpDir, true);
    } catch (IOException e) {
      // It's ok not able to cleanup temp directory.
      LOG.debug("Failed to cleanup temp directory {}", tmpDir, e);
    }
  }

  public static void main(String[] args) {
    StandaloneMain main = create();
    try {
      if (args.length > 0) {
        System.out.printf("%s takes no arguments\n", StandaloneMain.class.getSimpleName());
        System.out.println("These arguments are being ignored:");
        for (int i = 0; i <= args.length - 1; i++) {
          System.out.printf("Parameter #%d: %s\n", i, args[i]);
        }
      }
      main.startUp();
    } catch (Throwable e) {
      Throwable rootCause = Throwables.getRootCause(e);
      if (rootCause instanceof ServiceBindException) {
        LOG.error("Failed to start Standalone CDAP: {}", rootCause.getMessage());
        System.err.println("Failed to start Standalone CDAP: " + rootCause.getMessage());
      } else {
        // exception stack trace will be logged by
        // UncaughtExceptionIdleService.UNCAUGHT_EXCEPTION_HANDLER
        LOG.error("Failed to start Standalone CDAP");
        System.err.println("Failed to start Standalone CDAP");
        e.printStackTrace(System.err);
      }
      Runtime.getRuntime().halt(-2);
    }
  }

  /**
   * The root of all goodness!
   */
  public static StandaloneMain create() {
    return create(CConfiguration.create(), new Configuration());
  }

  public static StandaloneMain create(CConfiguration cConf, Configuration hConf) {
    // This is needed to use LocalJobRunner with fixes (we have it in app-fabric).
    // For the modified local job runner
    hConf.addResource("mapred-site-local.xml");
    hConf.reloadConfiguration();
    // Due to incredibly stupid design of Limits class, once it is initialized, it keeps its settings. We
    // want to make sure it uses our settings in this hConf, so we have to force it initialize here before
    // someone else initializes it.
    Limits.init(hConf);

    File localDataDir = new File(cConf.get(Constants.CFG_LOCAL_DATA_DIR));
    hConf.set(Constants.CFG_LOCAL_DATA_DIR, localDataDir.getAbsolutePath());
    hConf.set(Constants.AppFabric.OUTPUT_DIR, cConf.get(Constants.AppFabric.OUTPUT_DIR));
    hConf.set("hadoop.tmp.dir", new File(localDataDir, cConf.get(Constants.AppFabric.TEMP_DIR)).getAbsolutePath());

    // Windows specific requirements
    if (OSDetector.isWindows()) {
      // not set anywhere by the project, expected to be set from IDEs if running from the project instead of sdk
      // hadoop.dll is at cdap-unit-test\src\main\resources\hadoop.dll for some reason
      String hadoopDLLPath = System.getProperty("hadoop.dll.path");
      if (hadoopDLLPath != null) {
        System.load(hadoopDLLPath);
      } else {
        // this is where it is when the standalone sdk is built
        String userDir = System.getProperty("user.dir");
        System.load(Joiner.on(File.separator).join(userDir, "lib", "native", "hadoop.dll"));
      }
    }

    //Run dataset service on random port
    List<Module> modules = createPersistentModules(cConf, hConf);

    return new StandaloneMain(modules, cConf);
  }

  private static List<Module> createPersistentModules(CConfiguration cConf, Configuration hConf) {
    cConf.setIfUnset(Constants.CFG_DATA_LEVELDB_DIR, Constants.DEFAULT_DATA_LEVELDB_DIR);

    cConf.set(Constants.CFG_DATA_INMEMORY_PERSISTENCE, Constants.InMemoryPersistenceType.LEVELDB.name());

    // configure all services except for router to bind to 127.0.0.1
    cConf.set(Constants.AppFabric.SERVER_ADDRESS, "127.0.0.1");
    cConf.set(Constants.Transaction.Container.ADDRESS, "127.0.0.1");
    cConf.set(Constants.Dataset.Manager.ADDRESS, "127.0.0.1");
    cConf.set(Constants.Dataset.Executor.ADDRESS, "127.0.0.1");
    cConf.set(Constants.Stream.ADDRESS, "127.0.0.1");
    cConf.set(Constants.Metrics.ADDRESS, "127.0.0.1");
    cConf.set(Constants.Metrics.SERVER_ADDRESS, "127.0.0.1");
    cConf.set(Constants.MetricsProcessor.ADDRESS, "127.0.0.1");
    cConf.set(Constants.LogSaver.ADDRESS, "127.0.0.1");
    cConf.set(Constants.Security.AUTH_SERVER_BIND_ADDRESS, "127.0.0.1");
    cConf.set(Constants.Explore.SERVER_ADDRESS, "127.0.0.1");
    cConf.set(Constants.Metadata.SERVICE_BIND_ADDRESS, "127.0.0.1");

    return ImmutableList.of(
      new ConfigModule(cConf, hConf),
      new IOModule(),
      new ZKClientModule(),
      new KafkaClientModule(),
      new MetricsHandlerModule(),
      new DiscoveryRuntimeModule().getStandaloneModules(),
      new LocationRuntimeModule().getStandaloneModules(),
      new AppFabricServiceRuntimeModule().getStandaloneModules(),
      new ProgramRunnerRuntimeModule().getStandaloneModules(),
      new DataFabricModules().getStandaloneModules(),
      new DataSetsModules().getStandaloneModules(),
      new DataSetServiceModules().getStandaloneModules(),
      new MetricsClientRuntimeModule().getStandaloneModules(),
      new LoggingModules().getStandaloneModules(),
      new RouterModules().getStandaloneModules(),
      new SecurityModules().getStandaloneModules(),
      new StreamServiceRuntimeModule().getStandaloneModules(),
      new ExploreRuntimeModule().getStandaloneModules(),
      new ServiceStoreModules().getStandaloneModules(),
      new ExploreClientModule(),
      new NotificationFeedServiceRuntimeModule().getStandaloneModules(),
      new NotificationServiceRuntimeModule().getStandaloneModules(),
      new ViewAdminModules().getStandaloneModules(),
      new StreamAdminModules().getStandaloneModules(),
      new NamespaceClientRuntimeModule().getStandaloneModules(),
      new NamespaceStoreModule().getStandaloneModules(),
      new MetadataServiceModule(),
      new AuditModule().getStandaloneModules(),
      new AuthorizationModule()
    );
  }
}
