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

package co.cask.cdap.common.conf;

import java.util.concurrent.TimeUnit;

/**
 * Constants used by different systems are all defined here.
 */
public final class Constants {

  public static final String[] FEATURE_TOGGLE_PROPS = {
    Security.SSL_ENABLED,
    Security.ENABLED,
    Explore.EXPLORE_ENABLED,
  };

  public static final String[] PORT_PROPS = {
    Router.ROUTER_PORT,
    Router.ROUTER_SSL_PORT,
    Dashboard.BIND_PORT,
    Dashboard.SSL_BIND_PORT,
    Security.AUTH_SERVER_BIND_PORT,
    Security.AuthenticationServer.SSL_PORT,
  };

  public static final String ARCHIVE_DIR = "archive";
  public static final String ROOT_NAMESPACE = "root.namespace";
  public static final String COLLECT_CONTAINER_LOGS = "master.collect.containers.log";
  public static final String COLLECT_APP_CONTAINER_LOG_LEVEL = "master.collect.app.containers.log.level";
  public static final String HTTP_CLIENT_TIMEOUT_MS = "http.client.connection.timeout.ms";
  /** Uniquely identifies a CDAP instance */
  public static final String INSTANCE_NAME = "instance.name";

  /**
   * Configuration for Master startup.
   */
  public static final class Startup {
    public static final String CHECKS_ENABLED = "master.startup.checks.enabled";
    public static final String CHECK_PACKAGES = "master.startup.checks.packages";
    public static final String CHECK_CLASSES = "master.startup.checks.classes";
    public static final String YARN_CONNECT_TIMEOUT_SECONDS = "master.startup.checks.yarn.connect.timeout.seconds";
    public static final String STARTUP_SERVICE_TIMEOUT = "master.startup.service.timeout.seconds";
  }

  /**
   * Global Service names.
   */
  public static final class Service {
    public static final String ACL = "acl";
    public static final String APP_FABRIC_HTTP = "appfabric";
    public static final String TRANSACTION = "transaction";
    public static final String METRICS = "metrics";
    public static final String LOGSAVER = "log.saver";
    public static final String GATEWAY = "gateway";
    public static final String STREAMS = "streams";
    public static final String MASTER_SERVICES = "master.services";
    public static final String METRICS_PROCESSOR = "metrics.processor";
    public static final String DATASET_MANAGER = "dataset.service";
    public static final String DATASET_EXECUTOR = "dataset.executor";
    public static final String EXTERNAL_AUTHENTICATION = "external.authentication";
    public static final String EXPLORE_HTTP_USER_SERVICE = "explore.service";
    public static final String SERVICE_INSTANCE_TABLE_NAME = "cdap.services.instances";
    /** Scheduler queue name to submit the master service app. */
    public static final String SCHEDULER_QUEUE = "master.services.scheduler.queue";
    public static final String METADATA_SERVICE = "metadata.service";
  }

  /**
   * ZooKeeper Configuration.
   */
  public static final class Zookeeper {
    public static final String QUORUM = "zookeeper.quorum";
    public static final String CFG_SESSION_TIMEOUT_MILLIS = "zookeeper.session.timeout.millis";
    public static final int DEFAULT_SESSION_TIMEOUT_MILLIS = 40000;
  }

  /**
   * HBase configurations.
   */
  public static final class HBase {
    public static final String AUTH_KEY_UPDATE_INTERVAL = "hbase.auth.key.update.interval";
  }

  /**
   * Dangerous Options.
   */
  public static final class Dangerous {
    public static final String UNRECOVERABLE_RESET = "enable.unrecoverable.reset";
    public static final boolean DEFAULT_UNRECOVERABLE_RESET = false;
  }

  /**
   * App Fabric Configuration.
   */
  public static final class AppFabric {

    /**
     * App Fabric Server.
     */
    public static final String SERVER_ADDRESS = "app.bind.address";
    public static final String OUTPUT_DIR = "app.output.dir";
    public static final String TEMP_DIR = "app.temp.dir";
    public static final String REST_PORT = "app.rest.port";
    public static final String PROGRAM_JVM_OPTS = "app.program.jvm.opts";
    public static final String BACKLOG_CONNECTIONS = "app.connection.backlog";
    public static final String EXEC_THREADS = "app.exec.threads";
    public static final String BOSS_THREADS = "app.boss.threads";
    public static final String WORKER_THREADS = "app.worker.threads";
    public static final String APP_SCHEDULER_QUEUE = "apps.scheduler.queue";
    public static final String MAPREDUCE_JOB_CLIENT_CONNECT_MAX_RETRIES = "mapreduce.jobclient.connect.max.retries";
    public static final String MAPREDUCE_INCLUDE_CUSTOM_CLASSES = "mapreduce.include.custom.format.classes";
    public static final String PROGRAM_RUNID_CORRECTOR_INTERVAL_SECONDS = "app.program.runid.corrector.interval";
    public static final String SYSTEM_ARTIFACTS_DIR = "app.artifact.dir";
    public static final String PROGRAM_EXTRA_CLASSPATH = "app.program.extra.classpath";
    public static final String SPARK_YARN_CLIENT_REWRITE = "app.program.spark.yarn.client.rewrite.enabled";
    public static final String RUNTIME_EXT_DIR = "app.program.runtime.extensions.dir";

    /**
     * Guice named bindings.
     */
    public static final String HANDLERS_BINDING = "appfabric.http.handler";

    /**
     * Defaults.
     */
    public static final int DEFAULT_BACKLOG = 20000;
    public static final int DEFAULT_EXEC_THREADS = 20;
    public static final int DEFAULT_BOSS_THREADS = 1;
    public static final int DEFAULT_WORKER_THREADS = 10;

    /**
     * Query parameter to indicate start time.
     */
    public static final String QUERY_PARAM_START_TIME = "start";

    /**
     * Query parameter to indicate status of a program {active, completed, failed}
     */
    public static final String QUERY_PARAM_STATUS = "status";

    /**
     * Query parameter to indicate end time.
     */
    public static final String QUERY_PARAM_END_TIME = "end";

    /**
     * Query parameter to indicate limits on results.
     */
    public static final String QUERY_PARAM_LIMIT = "limit";

    public static final String SERVICE_DESCRIPTION = "Service for managing application lifecycle.";

    /**
     * Configuration setting to set the maximum size of a workflow token in MB
     */
    public static final String WORKFLOW_TOKEN_MAX_SIZE_MB = "workflow.token.max.size.mb";
  }

  /**
   * Scheduler options.
   */
  public class Scheduler {
    public static final String CFG_SCHEDULER_MAX_THREAD_POOL_SIZE = "scheduler.max.thread.pool.size";
  }

  /**
   * Application metadata store.
   */
  public static final class AppMetaStore {
    public static final String TABLE = "app.meta";
  }

  /**
   * Plugin Artifacts constants.
   */
  public static final class Plugin {
    // Key to be used in hConf to store location of the plugin artifact jar
    public static final String ARCHIVE = "cdap.program.plugin.archive";
  }

  /**
   * Configuration Store.
   */
  public static final class ConfigStore {
    public static final String CONFIG_TABLE = "config.store.table";
    public static final Byte VERSION = 0;
  }

  /**
   * Transactions.
   */
  public static final class Transaction {
    /**
     * Twill Runnable configuration.
     */
    public static final class Container {
      // TODO: This is duplicated from TxConstants. Needs to be removed.
      public static final String ADDRESS = "data.tx.bind.address";
      public static final String NUM_INSTANCES = "data.tx.num.instances";
      public static final String NUM_CORES = "data.tx.num.cores";
      public static final String MEMORY_MB = "data.tx.memory.mb";
      public static final String MAX_INSTANCES = "data.tx.max.instances";
    }

    public static final String SERVICE_DESCRIPTION = "Service that maintains transaction states.";
  }

  /**
   * Datasets.
   */
  public static final class Dataset {

    public static final String TABLE_PREFIX = "dataset.table.prefix";

    // Table dataset property that defines whether table is transactional or not.
    // Currently it is hidden from user as only supported for specially treated Metrics System's HBase
    // tables. Constant could be moved to Table after that is changed. See CDAP-1193 for more info
    public static final String TABLE_TX_DISABLED = "dataset.table.tx.disabled";

    public static final String DATA_DIR = "dataset.data.dir";
    public static final String DEFAULT_DATA_DIR = "data";

    public static final String DATASET_UNCHECKED_UPGRADE = "dataset.unchecked.upgrade";

    /**
     * Constants for PartitionedFileSet's DynamicPartitioner
     */
    public static final class Partitioned {
      public static final String HCONF_ATTR_OUTPUT_DATASET = "output.dataset.name";
      public static final String HCONF_ATTR_OUTPUT_FORMAT_CLASS_NAME = "output.format.class.name";
    }

    /**
     * DatasetManager service configuration.
     */
    public static final class Manager {
      /** for the address (hostname) of the dataset server. */
      public static final String ADDRESS = "dataset.service.bind.address";

      public static final String BACKLOG_CONNECTIONS = "dataset.service.connection.backlog";
      public static final String EXEC_THREADS = "dataset.service.exec.threads";
      public static final String BOSS_THREADS = "dataset.service.boss.threads";
      public static final String WORKER_THREADS = "dataset.service.worker.threads";
      public static final String OUTPUT_DIR = "dataset.service.output.dir";

      // Defaults
      public static final int DEFAULT_BACKLOG = 20000;
      public static final int DEFAULT_EXEC_THREADS = 10;
      public static final int DEFAULT_BOSS_THREADS = 1;
      public static final int DEFAULT_WORKER_THREADS = 4;
    }

    /**
     * DatasetUserService configuration.
     */
    public static final class Executor {
      /** for the port of the dataset user service server. */
      public static final String PORT = "dataset.executor.bind.port";

      /** for the address (hostname) of the dataset server. */
      public static final String ADDRESS = "dataset.executor.bind.address";

      public static final String EXEC_THREADS = "dataset.executor.exec.threads";
      public static final String WORKER_THREADS = "dataset.executor.worker.threads";
      public static final String OUTPUT_DIR = "dataset.executor.output.dir";

      /** Twill Runnable configuration **/
      public static final String CONTAINER_VIRTUAL_CORES = "dataset.executor.container.num.cores";
      public static final String CONTAINER_MEMORY_MB = "dataset.executor.container.memory.mb";
      public static final String CONTAINER_INSTANCES = "dataset.executor.container.instances";

      //max-instances of dataset executor service
      public static final String MAX_INSTANCES = "dataset.executor.max.instances";

      public static final String SERVICE_DESCRIPTION = "Service to perform dataset operations.";
    }

    /**
     * Dataset extensions.
     */
    public static final class Extensions {
      public static final String DIR = "dataset.extensions.dir";
      public static final String MODULES = "dataset.extensions.modules";

      /** Over-rides for default table bindings- use with caution! **/
      public static final String DISTMODE_TABLE = "dataset.extensions.distributed.mode.table";
      public static final String STREAM_CONSUMER_FACTORY = "stream.extension.consumer.factory";
      public static final String DISTMODE_METRICS_TABLE = "dataset.extensions.distributed.mode.metrics.table";
      public static final String DISTMODE_QUEUE_TABLE = "dataset.extensions.distributed.mode.queue.table";
    }
  }

  /**
   * Stream configurations and constants.
   */
  public static final class Stream {
    /* Begin CConfiguration keys */
    public static final String BASE_DIR = "stream.base.dir";
    public static final String TTL = "stream.event.ttl";
    public static final String PARTITION_DURATION = "stream.partition.duration";
    public static final String INDEX_INTERVAL = "stream.index.interval";
    public static final String FILE_PREFIX = "stream.file.prefix";
    public static final String INSTANCE_FILE_PREFIX = "stream.instance.file.prefix";
    public static final String CONSUMER_TABLE_PRESPLITS = "stream.consumer.table.presplits";
    public static final String FILE_CLEANUP_PERIOD = "stream.file.cleanup.period";
    public static final String BATCH_BUFFER_THRESHOLD = "stream.batch.buffer.threshold";
    public static final String NOTIFICATION_THRESHOLD = "stream.notification.threshold";

    // Stream http service configurations.
    public static final String STREAM_HANDLER = "stream.handler";
    public static final String ADDRESS = "stream.bind.address";
    public static final String WORKER_THREADS = "stream.worker.threads";
    public static final String ASYNC_WORKER_THREADS = "stream.async.worker.threads";
    public static final String ASYNC_QUEUE_SIZE = "stream.async.queue.size";

    // YARN container configurations.
    public static final String CONTAINER_VIRTUAL_CORES = "stream.container.num.cores";
    public static final String CONTAINER_MEMORY_MB = "stream.container.memory.mb";
    public static final String CONTAINER_INSTANCES = "stream.container.instances";

    // Tell the instance id of the YARN container. Set by the StreamHandlerRunnable only, not in default.xml
    public static final String CONTAINER_INSTANCE_ID = "stream.container.instance.id";
    /* End CConfiguration keys */

    /* Begin constants used by stream */

    /** How often to check for new file when reading from stream in milliseconds. **/
    public static final long NEW_FILE_CHECK_INTERVAL = TimeUnit.SECONDS.toMillis(10);
    public static final int HBASE_WRITE_BUFFER_SIZE = 4 * 1024 * 1024;

    public static final String URL_PREFIX = "stream://";
    public static final String DESCRIPTION = "stream.description";

    /**
     * Contains HTTP headers used by Stream handler.
     */
    public static final class Headers {
      public static final String SCHEMA = "schema";
      public static final String SCHEMA_HASH = "schema.hash";
    }

    // max instances of stream handler service
    public static final String MAX_INSTANCES = "stream.container.instances";

    public static final String SERVICE_DESCRIPTION = "Service that handles stream data ingestion.";
    /* End constants used by stream */

    // Period in seconds between two heartbeats in a stream service
    public static final int HEARTBEAT_INTERVAL = 2;

    // ZooKeeper namespace in which to keep the coordination metadata
    public static final String STREAM_ZK_COORDINATION_NAMESPACE = String.format("/%s/coordination", Service.STREAMS);

    /**
     * Stream view constants.
     */
    public static final class View {
      public static final String STORE_TABLE = "explore.stream.view.table";
    }
  }

  /**
   * Gateway Configurations.
   */
  public static final class Gateway {

    /**
     * v3 API.
     */
    public static final String API_VERSION_3_TOKEN = "v3";
    public static final String API_VERSION_3 = "/" + API_VERSION_3_TOKEN;
    public static final String STREAM_HANDLER_NAME = "stream_rest";
    public static final String METRICS_CONTEXT = "gateway";
    public static final String API_KEY = "X-ApiKey";
  }

  /**
   * Router Configuration.
   */
  public static final class Router {
    public static final String ADDRESS = "router.bind.address";
    public static final String ROUTER_PORT = "router.bind.port";
    public static final String WEBAPP_PORT = "router.webapp.bind.port";
    public static final String WEBAPP_ENABLED = "router.webapp.enabled";
    public static final String ROUTER_SSL_PORT = "router.ssl.bind.port";
    public static final String WEBAPP_SSL_PORT = "router.ssl.webapp.bind.port";
    public static final String BACKLOG_CONNECTIONS = "router.connection.backlog";
    public static final String SERVER_BOSS_THREADS = "router.server.boss.threads";
    public static final String SERVER_WORKER_THREADS = "router.server.worker.threads";
    public static final String CLIENT_BOSS_THREADS = "router.client.boss.threads";
    public static final String CLIENT_WORKER_THREADS = "router.client.worker.threads";
    public static final String CONNECTION_TIMEOUT_SECS = "router.connection.idle.timeout.secs";

    /**
     * Defaults.
     */
    public static final String DEFAULT_ROUTER_PORT = "10000";

    public static final String GATEWAY_DISCOVERY_NAME = Service.GATEWAY;
    public static final String WEBAPP_DISCOVERY_NAME = "webapp/$HOST";
  }

  /**
   * Webapp Configuration.
   */
  public static final class Webapp {
    public static final String WEBAPP_DIR = "webapp";
  }

  /**
   * Metrics constants.
   */
  public static final class Metrics {
    public static final String ADDRESS = "metrics.bind.address";
    public static final String CLUSTER_NAME = "metrics.cluster.name";
    public static final String CONFIG_AUTHENTICATION_REQUIRED = "metrics.authenticate";
    public static final String BACKLOG_CONNECTIONS = "metrics.connection.backlog";
    public static final String EXEC_THREADS = "metrics.exec.threads";
    public static final String BOSS_THREADS = "metrics.boss.threads";
    public static final String WORKER_THREADS = "metrics.worker.threads";
    public static final String NUM_INSTANCES = "metrics.num.instances";
    public static final String NUM_CORES = "metrics.num.cores";
    public static final String MEMORY_MB = "metrics.memory.mb";
    public static final String MAX_INSTANCES = "metrics.max.instances";
    public static final String SERVICE_DESCRIPTION = "Service to handle metrics requests.";

    public static final String ENTITY_TABLE_NAME = "metrics.data.entity.tableName";
    public static final String METRICS_TABLE_PREFIX = "metrics.data.table.prefix";
    public static final String TIME_SERIES_TABLE_ROLL_TIME = "metrics.data.table.ts.rollTime";

    // Key prefix for retention seconds. The actual key is suffixed by the table resolution.
    public static final String RETENTION_SECONDS = "metrics.data.table.retention.resolution";

    public static final String SERVER_ADDRESS = "metrics.query.bind.address";
    public static final String SERVER_PORT = "metrics.query.bind.port";

    public static final String KAFKA_TOPIC_PREFIX = "metrics.kafka.topic.prefix";
    public static final String KAFKA_PARTITION_SIZE = "metrics.kafka.partition.size";
    public static final String KAFKA_CONSUMER_PERSIST_THRESHOLD = "metrics.kafka.consumer.persist.threshold";
    public static final String KAFKA_META_TABLE = "metrics.kafka.meta.table";

    public static final String DEFAULT_KAFKA_META_TABLE = "metrics.kafka.meta";
    public static final String DEFAULT_KAFKA_TOPIC_PREFIX = "metrics";

    // NOTE: "v2" to avoid conflict with data of older metrics system
    public static final String DEFAULT_ENTITY_TABLE_NAME = "metrics.v2.entity";
    public static final String DEFAULT_METRIC_TABLE_PREFIX = "metrics.v2.table";
    public static final int DEFAULT_TIME_SERIES_TABLE_ROLL_TIME = 3600;
    public static final long DEFAULT_RETENTION_HOURS = 2;

    public static final int DEFAULT_KAFKA_CONSUMER_PERSIST_THRESHOLD = 100;
    public static final int DEFAULT_KAFKA_PARTITION_SIZE = 1;

    /**
     * Metric's dataset related constants.
     */
    public static final class Dataset {
      /** Defines reporting interval for HBase stats, in seconds */
      public static final String HBASE_STATS_REPORT_INTERVAL = "metrics.dataset.hbase.stats.report.interval";
      /** Defines reporting interval for LevelDB stats, in seconds */
      public static final String LEVELDB_STATS_REPORT_INTERVAL = "metrics.dataset.leveldb.stats.report.interval";
    }

    /**
     * Metrics context tags
     */
    public static final class Tag {
      // NOTES:
      //   * tag names must be unique (keeping all possible here helps to ensure that)
      //   * tag names better be short to reduce the serialized metric value size

      public static final String NAMESPACE = "ns";

      public static final String RUN_ID = "run";
      public static final String INSTANCE_ID = "ins";

      public static final String COMPONENT = "cmp";
      public static final String HANDLER = "hnd";
      public static final String METHOD = "mtd";
      public static final String THREAD = "thd";

      public static final String STREAM = "str";

      public static final String DATASET = "ds";

      public static final String APP = "app";

      public static final String SERVICE = "srv";

      public static final String WORKER = "wrk";

      public static final String FLOW = "fl";
      public static final String FLOWLET = "flt";
      public static final String FLOWLET_QUEUE = "flq";

      public static final String MAPREDUCE = "mr";
      public static final String MR_TASK_TYPE = "mrt";

      public static final String WORKFLOW = "wf";
      public static final String NODE = "nd";

      public static final String SPARK = "sp";

      // who emitted: user vs system (scope is historical name)
      public static final String SCOPE = "scp";

      public static final String PRODUCER = "pr";
      public static final String CONSUMER = "co";
    }

    /**
     * Metric names
     */
    public static final class Name {
      /**
       * Flow metrics
       */
      public static final class Flow {
        public static final String FLOWLET_INPUT = "system.process.tuples.read";
        public static final String FLOWLET_PROCESSED = "system.process.events.processed";
        public static final String FLOWLET_EXCEPTIONS = "system.process.errors";
      }

      /**
       * Service metrics
       */
      public static final class Service {
        public static final String SERVICE_INPUT = "system.requests.count";
        public static final String SERVICE_PROCESSED = "system.response.successful.count";
        public static final String SERVICE_EXCEPTIONS = "system.response.server.error.count";
      }

      /**
       * Dataset metrics
       */
      public static final class Dataset {
        public static final String READ_COUNT = "dataset.store.reads";
        public static final String OP_COUNT = "dataset.store.ops";
        public static final String WRITE_COUNT = "dataset.store.writes";
        public static final String WRITE_BYTES = "dataset.store.bytes";
      }

      /**
       * Logs metrics
       */
      public static final class Log {
        public static final String PROCESS_DELAY = "log.process.delay";
        public static final String PROCESS_MESSAGES_COUNT = "log.process.message.count";
      }
    }

    /**
     * Metrics query constants and defaults
     */
    public static final class Query {
      public static final long MAX_HOUR_RESOLUTION_QUERY_INTERVAL = 36000;
      public static final long MAX_MINUTE_RESOLUTION_QUERY_INTERVAL = 600;
      // Number of seconds to subtract from current timestamp when query without "end" time.
      public static final long QUERY_SECOND_DELAY = 2;
    }
  }

  /**
   * Configurations for metrics processor.
   */
  public static final class MetricsProcessor {
    public static final String NUM_INSTANCES = "metrics.processor.num.instances";
    public static final String NUM_CORES = "metrics.processor.num.cores";
    public static final String MEMORY_MB = "metrics.processor.memory.mb";
    public static final String MAX_INSTANCES = "metrics.processor.max.instances";

    public static final String METRICS_PROCESSOR_STATUS_HANDLER = "metrics.processor.status.handler";
    public static final String ADDRESS = "metrics.processor.status.bind.address";

    public static final String SERVICE_DESCRIPTION = "Service to process application and system metrics.";
  }

  /**
   * Configurations for metrics collector.
   */
  public static final class MetricsCollector {
    public static final long DEFAULT_FREQUENCY_SECONDS = 1;
  }

  /**
   * Configurations for log saver.
   */
  public static final class LogSaver {
    public static final String NUM_INSTANCES = "log.saver.num.instances";
    public static final String MEMORY_MB = "log.saver.run.memory.megs";
    public static final String NUM_CORES = "log.saver.run.num.cores";
    public static final String MAX_INSTANCES = "log.saver.max.instances";

    public static final String LOG_SAVER_STATUS_HANDLER = "log.saver.status.handler";
    public static final String ADDRESS = "log.saver.status.bind.address";

    public static final String SERVICE_DESCRIPTION = "Service to collect and store logs.";
    public static final String MESSAGE_PROCESSORS =  "log.saver.message.processors";
  }

  /**
   * Monitor constants.
   */
  public static final class Monitor {
    public static final String STATUS_OK = "OK";
    public static final String STATUS_NOTOK = "NOTOK";
    public static final String DISCOVERY_TIMEOUT_SECONDS = "monitor.handler.service.discovery.timeout.seconds";
  }

  /**
   * Logging constants.
   */
  public static final class Logging {
    public static final String COMPONENT_NAME = "services";
    public static final String KAFKA_TOPIC = "log.kafka.topic";
  }

  /**
   * Security configuration.
   */
  public static final class Security {
    /** Enables security. */
    public static final String ENABLED = "security.enabled";
    /** Enables Kerberos authentication. */
    public static final String KERBEROS_ENABLED = "kerberos.auth.enabled";
    /** Kerberos keytab relogin interval. */
    public static final String KERBEROS_KEYTAB_RELOGIN_INTERVAL = "kerberos.auth.relogin.interval.seconds";
    /** Algorithm used to generate the digest for access tokens. */
    public static final String TOKEN_DIGEST_ALGO = "security.token.digest.algorithm";
    /** Key length for secret key used by token digest algorithm. */
    public static final String TOKEN_DIGEST_KEY_LENGTH = "security.token.digest.keylength";
    /** Time duration in milliseconds after which an active secret key should be retired. */
    public static final String TOKEN_DIGEST_KEY_EXPIRATION = "security.token.digest.key.expiration.ms";
    /** Parent znode used for secret key distribution in ZooKeeper. */
    public static final String DIST_KEY_PARENT_ZNODE = "security.token.distributed.parent.znode";
    /** Deprecated. Use AUTH_SERVER_BIND_ADDRESS instead. **/
    @Deprecated
    public static final String AUTH_SERVER_ADDRESS = "security.auth.server.address";
    /**
     * Address that clients should use to communicate with the Authentication Server.
     * Leave empty to use default, generated by the Authentication Server.
     */
    public static final String AUTH_SERVER_ANNOUNCE_ADDRESS = "security.auth.server.announce.address";
    /** Address the Authentication Server should bind to. */
    public static final String AUTH_SERVER_BIND_ADDRESS = "security.auth.server.bind.address";
    /** Configuration for External Authentication Server. */
    public static final String AUTH_SERVER_BIND_PORT = "security.auth.server.bind.port";
    /** Maximum number of handler threads for the Authentication Server embedded Jetty instance. */
    public static final String MAX_THREADS = "security.server.maxthreads";
    /** Access token expiration time in milliseconds. */
    public static final String TOKEN_EXPIRATION = "security.server.token.expiration.ms";
    /** Long lasting Access token expiration time in milliseconds. */
    public static final String EXTENDED_TOKEN_EXPIRATION = "security.server.extended.token.expiration.ms";
    public static final String CFG_FILE_BASED_KEYFILE_PATH = "security.data.keyfile.path";
    /** Configuration for security realm. */
    public static final String CFG_REALM = "security.realm";
    /** Authentication Handler class name */
    public static final String AUTH_HANDLER_CLASS = "security.authentication.handlerClassName";
    /** Prefix for all configurable properties of an Authentication handler. */
    public static final String AUTH_HANDLER_CONFIG_BASE = "security.authentication.handler.";
    /** Authentication Login Module class name */
    public static final String LOGIN_MODULE_CLASS_NAME = "security.authentication.loginmodule.className";
    /** Realm file for Basic Authentication */
    public static final String BASIC_REALM_FILE = "security.authentication.basic.realmfile";
    /** Enables SSL */
    public static final String SSL_ENABLED = "ssl.enabled";

    /**
     * Authorization.
     */
    public static final class Authorization {
      /** Enables authorization */
      public static final String ENABLED = "security.authorization.enabled";
      /** Extension jar path */
      public static final String EXTENSION_JAR_PATH = "security.authorization.extension.jar.path";
      /** Prefix for extension properties */
      public static final String EXTENSION_CONFIG_PREFIX =
        "security.authorization.extension.config.";
    }

    /**
     * Headers for security.
     */
    public static final class Headers {
      /** Internal user ID header passed from Router to downstream services */
      public static final String USER_ID = "CDAP-UserId";
      /** User IP header passed from Router to downstream services */
      public static final String USER_IP = "CDAP-UserIP";
    }

    /**
     * Security configuration for Router.
     */
    public static final class Router {
      /** SSL keystore location */
      public static final String SSL_KEYSTORE_PATH = "router.ssl.keystore.path";
      /** SSL keystore type */
      public static final String SSL_KEYSTORE_TYPE = "router.ssl.keystore.type";
      /** SSL keystore key password */
      public static final String SSL_KEYPASSWORD = "router.ssl.keystore.keypassword";
      /** SSL keystore password */
      public static final String SSL_KEYSTORE_PASSWORD = "router.ssl.keystore.password";

      /** Default SSL keystore type */
      public static final String DEFAULT_SSL_KEYSTORE_TYPE = "JKS";

      /** Paths to exclude from authentication, given by a single regular expression */
      public static final String BYPASS_AUTHENTICATION_REGEX = "router.bypass.auth.regex";
    }

    /**
     * Security configuration for ExternalAuthenticationServer.
     */
    public static final class AuthenticationServer {
      /** SSL port */
      public static final String SSL_PORT = "security.auth.server.ssl.bind.port";
      /** SSL keystore location */
      public static final String SSL_KEYSTORE_PATH = "security.auth.server.ssl.keystore.path";
      /** SSL keystore type */
      public static final String SSL_KEYSTORE_TYPE = "security.auth.server.ssl.keystore.type";
      /** SSL keystore key password */
      public static final String SSL_KEYPASSWORD = "security.auth.server.ssl.keystore.keypassword";
      /** SSL keystore password */
      public static final String SSL_KEYSTORE_PASSWORD = "security.auth.server.ssl.keystore.password";

      /** Default SSL keystore type */
      public static final String DEFAULT_SSL_KEYSTORE_TYPE = "JKS";
    }

    /** Path to the Kerberos keytab file used by CDAP */
    public static final String CFG_CDAP_MASTER_KRB_KEYTAB_PATH = "cdap.master.kerberos.keytab";
    /** Kerberos principal used by CDAP */
    public static final String CFG_CDAP_MASTER_KRB_PRINCIPAL = "cdap.master.kerberos.principal";
  }

  /**
   * Explore module configuration.
   */
  public static final class Explore {
    public static final String CCONF_KEY = "explore.cconfiguration";
    public static final String HCONF_KEY = "explore.hconfiguration";
    public static final String TX_QUERY_KEY = "explore.hive.query.tx.id";
    public static final String TX_QUERY_CLOSED = "explore.hive.query.tx.commited";
    public static final String QUERY_ID = "explore.query.id";
    public static final String FORMAT_SPEC = "explore.format.specification";

    public static final String START_ON_DEMAND = "explore.start.on.demand";
    public static final String DATASET_NAME = "explore.dataset.name";
    public static final String DATASET_NAMESPACE = "explore.dataset.namespace";
    public static final String VIEW_NAME = "explore.view.name";
    public static final String STREAM_NAME = "explore.stream.name";
    public static final String STREAM_NAMESPACE = "explore.stream.namespace";
    public static final String EXPLORE_CLASSPATH = "explore.classpath";
    public static final String EXPLORE_CONF_FILES = "explore.conf.files";
    public static final String PREVIEWS_DIR_NAME = "explore.previews.dir";
    // a marker so that we know which tables are created by CDAP
    public static final String CDAP_NAME = "cdap.name";
    public static final String CDAP_VERSION = "cdap.version";

    public static final String SERVER_ADDRESS = "explore.service.bind.address";

    public static final String BACKLOG_CONNECTIONS = "explore.service.connection.backlog";
    public static final String EXEC_THREADS = "explore.service.exec.threads";
    public static final String WORKER_THREADS = "explore.service.worker.threads";

    /** Twill Runnable configuration **/
    public static final String CONTAINER_VIRTUAL_CORES = "explore.executor.container.num.cores";
    public static final String CONTAINER_MEMORY_MB = "explore.executor.container.memory.mb";
    public static final String CONTAINER_INSTANCES = "explore.executor.container.instances";

    public static final String LOCAL_DATA_DIR = "explore.local.data.dir";
    public static final String EXPLORE_ENABLED = "explore.enabled";
    public static final String WRITES_ENABLED = "explore.writes.enabled";

    //max-instances of explore HTTP service
    public static final String MAX_INSTANCES = "explore.executor.max.instances";

    public static final String ACTIVE_OPERATION_TIMEOUT_SECS = "explore.active.operation.timeout.secs";
    public static final String INACTIVE_OPERATION_TIMEOUT_SECS = "explore.inactive.operation.timeout.secs";
    public static final String CLEANUP_JOB_SCHEDULE_SECS = "explore.cleanup.job.schedule.secs";

    public static final String SERVICE_DESCRIPTION = "Service to run ad-hoc queries.";

    /**
     * Explore JDBC constants.
     */
    public static final class Jdbc {
      public static final String URL_PREFIX = "jdbc:cdap://";
    }
  }

  /**
   * Notification system configuration.
   */
  public static final class Notification {
    public static final String TRANSPORT_SYSTEM = "notification.transport.system";
    public static final String KAFKA_TOPIC = "notification.kafka.topic";

    /**
     * Notifications in Streams constants.
     */
    public static final class Stream {
      public static final String STREAM_FEED_CATEGORY = "stream";
      public static final String STREAM_INTERNAL_FEED_CATEGORY = "streamInternal";
      public static final String STREAM_HEARTBEAT_FEED_NAME = "heartbeat";
      public static final String STREAM_SIZE_SCHEDULE_POLLING_DELAY = "stream.size.schedule.polling.delay";
    }
  }

  public static final String CFG_LOCAL_DATA_DIR = "local.data.dir";
  public static final String CFG_HDFS_USER = "hdfs.user";
  public static final String CFG_HDFS_NAMESPACE = "hdfs.namespace";
  public static final String CFG_HDFS_LIB_DIR = "hdfs.lib.dir";

  public static final String CFG_TWILL_ZK_NAMESPACE = "twill.zookeeper.namespace";
  public static final String CFG_TWILL_RESERVED_MEMORY_MB = "twill.java.reserved.memory.mb";
  public static final String CFG_TWILL_NO_CONTAINER_TIMEOUT = "twill.no.container.timeout";

  /**
   * Data Fabric.
   */
  public enum InMemoryPersistenceType {
    MEMORY,
    LEVELDB,
    HSQLDB
  }
  /** defines which persistence engine to use when running all in one JVM. **/
  public static final String CFG_DATA_INMEMORY_PERSISTENCE = "data.local.inmemory.persistence.type";
  public static final String CFG_DATA_LEVELDB_DIR = "data.local.storage";
  public static final String CFG_DATA_LEVELDB_BLOCKSIZE = "data.local.storage.blocksize";
  public static final String CFG_DATA_LEVELDB_CACHESIZE = "data.local.storage.cachesize";
  public static final String CFG_DATA_LEVELDB_FSYNC = "data.local.storage.fsync";

  /**
   * Defaults for Data Fabric.
   */
  public static final String DEFAULT_DATA_LEVELDB_DIR = "data";
  public static final int DEFAULT_DATA_LEVELDB_BLOCKSIZE = 1024;
  public static final long DEFAULT_DATA_LEVELDB_CACHESIZE = 1024 * 1024 * 100;
  public static final boolean DEFAULT_DATA_LEVELDB_FSYNC = true;

  /**
   * Config for Log Collection.
   */
  public static final String CFG_LOG_COLLECTION_ROOT = "log.collection.root";
  public static final String DEFAULT_LOG_COLLECTION_ROOT = "data/logs";

  /**
   * Used for upgrade and backwards compatability
   */
  public static final String DEVELOPER_ACCOUNT = "developer";

  /**
   * Constants related to external systems.
   */
  public static final class External {
    /**
     * Constants used by Java security.
     */
    public static final class JavaSecurity {
      public static final String ENV_AUTH_LOGIN_CONFIG = "java.security.auth.login.config";
    }

    /**
     * Constants used by ZooKeeper.
     */
    public static final class Zookeeper {
      public static final String ENV_AUTH_PROVIDER_1 = "zookeeper.authProvider.1";
      public static final String ENV_ALLOW_SASL_FAILED_CLIENTS = "zookeeper.allowSaslFailedClients";
    }
  }

  /**
   * Constants for the dashboard/frontend.
   */
  public static final class Dashboard {
    /**
     * Port for the dashboard to bind to in non-SSL mode.
     */
    public static final String BIND_PORT = "dashboard.bind.port";

    /**
     * Port for the dashboard to bind to in SSL mode.
     */
    public static final String SSL_BIND_PORT = "dashboard.ssl.bind.port";

    /**
     * True to allow self-signed SSL certificates for endpoints accessed by the dashboard.
     */
    public static final String SSL_ALLOW_SELFSIGNEDCERT = "dashboard.selfsignedcertificate.enabled";
  }

  /**
   * Constants for endpoints
   */
  public static final class EndPoints {
    /**
    * Status endpoint
    */
    public static final String STATUS = "/status";
  }

  /**
   * Constants for namespaces
   */
  public static final class Namespace {
    public static final String NAMESPACES_DIR = "namespaces.dir";
  }

  /**
   * Constants for metadata service
   */
  public static final class Metadata {
    public static final String SERVICE_DESCRIPTION = "Service to perform metadata operations.";
    public static final String SERVICE_BIND_ADDRESS = "metadata.service.bind.address";
    public static final String SERVICE_WORKER_THREADS = "metadata.service.worker.threads";
    public static final String SERVICE_EXEC_THREADS = "metadata.service.exec.threads";
    public static final String HANDLERS_NAME = "metadata.handlers";
    public static final String UPDATES_KAFKA_TOPIC = "metadata.updates.kafka.topic";
    public static final String UPDATES_PUBLISH_ENABLED = "metadata.updates.publish.enabled";
    public static final String UPDATES_KAFKA_BROKER_LIST = "metadata.updates.kafka.broker.list";
    public static final String MAX_CHARS_ALLOWED = "metadata.max.allowed.chars";
  }

  /**
   * Constants for publishing audit
   */
  public static final class Audit {
    public static final String ENABLED = "audit.enabled";
    public static final String KAFKA_TOPIC = "audit.kafka.topic";
  }
}
