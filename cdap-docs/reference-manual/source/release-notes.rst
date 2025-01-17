.. meta::
    :author: Cask Data, Inc 
    :description: Release notes for the Cask Data Application Platform
    :copyright: Copyright © 2014-2015 Cask Data, Inc.

:hide-nav: true
:orphan:

.. _overview_release-notes:

.. index::
   single: Release Notes

.. _release-notes:

============================================
Cask Data Application Platform Release Notes
============================================

.. contents::
   :local:
   :class: faq
   :backlinks: none
   :depth: 2

`Release 3.4.0 <http://docs.cask.co/cdap/3.4.0/index.html>`__
=============================================================

New Features
------------
- `CDAP-9999 <https://issues.cask.co/browse/CDAP-9999>`__ - Added...

Improvements
------------
- `CDAP-9999 <https://issues.cask.co/browse/CDAP-9999>`__ - Added...

Bug Fixes
---------
- `CDAP-9999 <https://issues.cask.co/browse/CDAP-9999>`__ - Fixed a problem with...

API Changes
-----------
- `CDAP-9999 <https://issues.cask.co/browse/CDAP-9999>`__ - Added...

Deprecated and Removed Features
-------------------------------
- See the :ref:`CDAP 3.4.0 Javadocs <javadocs>` for a list of deprecated and removed APIs.

- `CDAP-9999 <https://issues.cask.co/browse/CDAP-9999>`__ - Removed...

Known Issues
------------
- After upgrading CDAP from a pre-3.0 version, any unprocessed metrics data in Kafka will
  be lost and *WARN* log messages will be logged that tell about the inability to process
  old data in the old format.

- `CDAP-797 <https://issues.cask.co/browse/CDAP-797>`__ - 
  When running secure Hadoop clusters, debug logs from MapReduce programs are not
  available.

- `CDAP-1007 <https://issues.cask.co/browse/CDAP-1007>`__ -
  If the Hive Metastore is restarted while the CDAP Explore Service is running, the
  Explore Service remains alive, but becomes unusable. To correct, restart the CDAP Master
  |---| which will restart all services |---| as described under "Starting CDAP Services"
  for your particular Hadoop distribution in the :ref:`Installation documentation <installation-index>`.

- `CDAP-1587 <https://issues.cask.co/browse/CDAP-1587>`__ - 
  CDAP internally creates tables in the "user" space that begin with the word
  ``"system"``. User datasets with names starting with ``"system"`` can conflict if they
  were to match one of those names. To avoid this, do not start any datasets with the word
  ``"system"``. 

- `CDAP-2632 <https://issues.cask.co/browse/CDAP-2632>`__ -
  The application in the `cdap-kafka-ingest-guide 
  <https://github.com/cdap-guides/cdap-kafka-ingest-guide/tree/release/cdap-3.0-compatible>`__ 
  does not run on Ubuntu 14.x as of CDAP 3.0.x.

- `CDAP-2721 <https://issues.cask.co/browse/CDAP-2721>`__ -
  Metrics for :ref:`FileSets <datasets-fileset>` can show zero values even if there is
  data present, because FileSets do not emit metrics (`CDAP-587
  <https://issues.cask.co/browse/CDAP-587>`__).
  
- `CDAP-2831 <https://issues.cask.co/browse/CDAP-2831>`__ -
  A workflow that is scheduled by time will not be run between the failure of the primary
  master and the time that the secondary takes over. This scheduled run will not be
  triggered at all.

- `CDAP-2945 <https://issues.cask.co/browse/CDAP-2945>`__ -
  If the input partition filter for a PartitionedFileSet does not match any partitions,
  MapReduce jobs can fail.

- `CDAP-3000 <https://issues.cask.co/browse/CDAP-3000>`__ -
  The Workflow token is in an inconsistent state for nodes in a fork while the nodes of
  the fork are still running. It becomes consistent after the join.

- `CDAP-3221 <https://issues.cask.co/browse/CDAP-3221>`__ -
  When running in CDAP Standalone mode, if a MapReduce job fails repeatedly, then the SDK
  hits an out-of-memory exception due to ``perm gen``. The Standalone needs restarting at
  this point.

- `CDAP-3262 <https://issues.cask.co/browse/CDAP-3262>`__ -
  For Microsoft Windows, the CDAP Standalone scripts can fail when used with a JAVA_HOME
  that is defined as a path with spaces in it. A workaround is to use a definition of
  JAVA_HOME that does not include spaces, such as ``C:\PROGRA~1\Java\jdk1.7.0_79\bin`` or
  ``C:\ProgramData\Oracle\Java\javapath``.

- `CDAP-3492 <https://issues.cask.co/browse/CDAP-3492>`__ -
  In the CDAP CLI, executing ``'select *'`` from a dataset with many fields generates an error.
  
- `CDAP-3641 <https://issues.cask.co/browse/CDAP-3641>`__ -
  A RESTful API call to retrieve workflow statistics hangs if units (such as "s" for
  seconds) are not provided as part of the query.

- `CDAP-3750 <https://issues.cask.co/browse/CDAP-3750>`__ -
  If a table schema contains a field name that is a reserved word in the Hive DDL, ``'enable explore'`` fails.


`Release 3.3.2 <http://docs.cask.co/cdap/3.3.2/index.html>`__

=============================================================

Improvements
------------
- `CDAP-5047 <https://issues.cask.co/browse/CDAP-5047>`__ - Added a :ref:`Batch Source
  Plugin <cdap-apps-etl-plugins-batchsources>` to read from Microsoft Azure Blob Storage.

- `CDAP-5134 <https://issues.cask.co/browse/CDAP-5134>`__ - Added support for CDH 5.6 to CDAP.

Bug Fixes
---------
- `CDAP-4967 <https://issues.cask.co/browse/CDAP-4967>`__ - Fixed a schema-parsing bug
  that prevented the use of schemas where a record is used both as a top-level field and
  also used inside a different record field.

- `CDAP-5019 <https://issues.cask.co/browse/CDAP-5019>`__ - Worked around two issues
  (`SPARK-13441 <https://issues.apache.org/jira/browse/SPARK-13441>`__
  and `YARN-4727 <https://issues.apache.org/jira/browse/YARN-4727>`__) that prevented
  launching Spark jobs on Cloudera Data Hub clusters managed with Cloudera Manager 
  when using Spark 1.4 or greater.

- `CDAP-5063 <https://issues.cask.co/browse/CDAP-5063>`__ - Fixed a problem with 
  the CDAP Master not starting when CDAP and the HiveServer2 services are running on the
  same node in an Ambari cluster.

- `CDAP-5076 <https://issues.cask.co/browse/CDAP-5076>`__ - Fixed a problem with the CDAP
  CLI command "update app" that was parsing the application config incorrectly.
  
- `CDAP-5094 <https://issues.cask.co/browse/CDAP-5094>`__ - Fixed a problem where the explore
  schema fileset property was being ignored unless an explore format was also present.

- `CDAP-5137 <https://issues.cask.co/browse/CDAP-5137>`__ - Fix a problem with Spark jobs
  not being submitted to the appropriate YARN scheduler queue set for the namespace.


`Release 3.3.1 <http://docs.cask.co/cdap/3.3.1/index.html>`__
=============================================================

Improvements
------------
- `CDAP-4602 <https://issues.cask.co/browse/CDAP-4602>`__ - Updated CDAP to use
  Tephra 0.6.5.

- `CDAP-4708 <https://issues.cask.co/browse/CDAP-4708>`__ - Added system metadata to
  existing entities.

- `CDAP-4723 <https://issues.cask.co/browse/CDAP-4723>`__ - Improved the Hydrator plugin
  archetypes to include build steps to build the deployment JSON for the artifact.

- `CDAP-4773 <https://issues.cask.co/browse/CDAP-4773>`__ - Improved the error logging for
  the Master Stream service when it can't connect to the CDAP AppFabric server.

Bug Fixes
---------
- `CDAP-4117 <https://issues.cask.co/browse/CDAP-4117>`__ - Fixed an issue of not using
  the correct user to access HDFS when submitting a YARN application through Apache Twill,
  which caused cleanup failure on application termination.

- `CDAP-4613 <https://issues.cask.co/browse/CDAP-4613>`__ - Fixed a problem with tooltips
  not appearing in Flow and Workflow diagrams displayed in the Firefox browser.

- `CDAP-4679 <https://issues.cask.co/browse/CDAP-4679>`__ - The Hydrator UI now prevents
  drafts from being created with a name of an already-existing draft. This prevents
  overwriting of existing drafts.

- `CDAP-4688 <https://issues.cask.co/browse/CDAP-4688>`__ - Improved the metadata search
  to return matching entities from both the specified namespace and the system namespace.

- `CDAP-4689 <https://issues.cask.co/browse/CDAP-4689>`__ - Fixed a problem when using an
  Hbase sink as one of multiple sinks in a Hydrator pipeline.

- `CDAP-4720 <https://issues.cask.co/browse/CDAP-4720>`__ - Fixed an issue where system
  metadata updates were not being published to Kafka.

- `CDAP-4721 <https://issues.cask.co/browse/CDAP-4721>`__ - Fixed an issue where metadata
  updates wouldn't be sent when certain entities were deleted.

- `CDAP-4740 <https://issues.cask.co/browse/CDAP-4740>`__ - Added validation to the JSON
  imported in the Hydrator UI.

- `CDAP-4741 <https://issues.cask.co/browse/CDAP-4741>`__ - Fixed a bug with deleting
  artifact metadata when an artifact was deleted.

- `CDAP-4743 <https://issues.cask.co/browse/CDAP-4743>`__ - Fixed the Node.js server proxy
  to handle all backend errors (with and without statusCodes).

- `CDAP-4745 <https://issues.cask.co/browse/CDAP-4745>`__ - Fixed a bug in the Hydrator
  upgrade tool which caused drafts to not get upgraded.

- `CDAP-4753 <https://issues.cask.co/browse/CDAP-4753>`__ - Fixed the Hydrator Stream
  source to not assume an output schema. This is valid when a pipeline is created outside
  Hydrator UI.

- `CDAP-4754 <https://issues.cask.co/browse/CDAP-4754>`__ - Fixed ObjectStore to work when
  parameterized with custom classes.

- `CDAP-4767 <https://issues.cask.co/browse/CDAP-4767>`__ - Fixed an issue where delegation token
  cancellation of CDAP program was affecting CDAP master services.

- `CDAP-4770 <https://issues.cask.co/browse/CDAP-4770>`__ - Fixed the Cask Hydrator UI to
  automatically reconnect with the CDAP backend when the backend restarts.

- `CDAP-4771 <https://issues.cask.co/browse/CDAP-4771>`__ - Fixed an issue in Cloudera
  Manager installations where CDAP container logs would go to the stdout file instead of the
  master log.

- `CDAP-4784 <https://issues.cask.co/browse/CDAP-4784>`__ - Fixed an issue where the
  IndexedTable was dropping indices upon row updates.

- `CDAP-4785 <https://issues.cask.co/browse/CDAP-4785>`__ - Fixed a problem in the upgrade
  tool where deleted datasets would cause it to throw a NullPointerException.

- `CDAP-4790 <https://issues.cask.co/browse/CDAP-4790>`__ - Fixed an issue where the Hbase
  implementation of the Table API returned all rows, when the correct response should have
  been an empty set of columns.

- `CDAP-4800 <https://issues.cask.co/browse/CDAP-4800>`__ - Fixed a problem with the error
  message returned when loading an artifact with an invalid range.

- `CDAP-4806 <https://issues.cask.co/browse/CDAP-4806>`__ - Fixed the PartitionedFileSet's
  DynamicPartitioner to work with Avro OutputFormats.

- `CDAP-4829 <https://issues.cask.co/browse/CDAP-4829>`__ - Fixed a Validator Transform
  function generator in the Hydrator UI.

- `CDAP-4831 <https://issues.cask.co/browse/CDAP-4831>`__ - Allows user-scoped plugins to
  surface the correct widget JSON in the Hydrator UI.

- `CDAP-4832 <https://issues.cask.co/browse/CDAP-4832>`__ - Added the ErrorDataset as an
  option on widget JSON in Hydrator plugins.

- `CDAP-4836 <https://issues.cask.co/browse/CDAP-4836>`__ - Fixed a spacing issue for
  metrics showing in Pipeline diagrams of the Hydrator UI.

- `CDAP-4853 <https://issues.cask.co/browse/CDAP-4853>`__ - Fixed issues with the Hydrator
  UI widgets for the Hydrator Kafka real-time source, JMS real-time source, and CloneRecord
  transform.

- `CDAP-4865 <https://issues.cask.co/browse/CDAP-4865>`__ - Enhanced the CDAP SDK to be
  able to publish metadata updates to an external Kafka, identified by the configuration
  property ``metadata.updates.kafka.broker.list``. Publishing can be enabled by setting
  ``metadata.updates.publish.enabled`` to true. Updates are published to the Kafka topic
  identified by the property ``metadata.updates.kafka.topic``.

- `CDAP-4877 <https://issues.cask.co/browse/CDAP-4877>`__ - Fixed errors in Cask Hydrator
  Plugins. Two plugin documents (``core-plugins/docs/Database-batchsink.md`` and
  ``core-plugins/docs/Database-batchsource.md``) were removed, as the plugins have been moved
  from *core-plugins* to *database-plugins* (to ``database-plugins/docs/Database-batchsink.md``
  and ``database-plugins/docs/Database-batchsource.md``).

- `CDAP-4889 <https://issues.cask.co/browse/CDAP-4889>`__ - Fixed an issue with upgrading
  HBase tables while using the CDAP Upgrade Tool.

- `CDAP-4894 <https://issues.cask.co/browse/CDAP-4894>`__ - Fixed an issue with CDAP
  coprocessors that caused HBase tables to be disabled after upgrading the cluster to a
  highly-available file system.

- `CDAP-4906 <https://issues.cask.co/browse/CDAP-4906>`__ - Fixed the CDAP Upgrade Tool to
  return a non-zero exit status upon error during upgrade.

- `CDAP-4924 <https://issues.cask.co/browse/CDAP-4924>`__ - Fixed a PermGen memory leak
  that occurred while deploying multiple applications with database plugins.

- `CDAP-4927 <https://issues.cask.co/browse/CDAP-4927>`__ - Fixed the CDAP Explore
  Service JDBC driver to do nothing instead of throwing an exception when a commit is
  called. 
  
- `CDAP-4950 <https://issues.cask.co/browse/CDAP-4950>`__ - Added an ``'enableAutoCommit'``
  property to the Cask Hydrator database plugins to enable the use of JDBC drivers that,
  similar to the Hive JDBC driver, do not allow commits.

- `CDAP-4951 <https://issues.cask.co/browse/CDAP-4951>`__ - Changed the upload timeout from the
  CDAP CLI from 15 seconds to unlimited.

- `CDAP-4975 <https://issues.cask.co/browse/CDAP-4975>`__ - Pass ResourceManager delegation tokens
  in the proper format in secure Hadoop HA clusters.

Deprecated and Removed Features
-------------------------------

- See the :ref:`CDAP 3.3.1 Javadocs <javadocs>` for a list of deprecated and removed APIs.

- The properties ``router.ssl.webapp.bind.port``, ``router.webapp.bind.port``,
  ``router.webapp.enabled`` have been deprecated and will be removed in a future version.


Known Issues
------------
- After upgrading CDAP from a pre-3.0 version, any unprocessed metrics data in Kafka will
  be lost and *WARN* log messages will be logged that tell about the inability to process
  old data in the old format.

- `CDAP-797 <https://issues.cask.co/browse/CDAP-797>`__ - 
  When running secure Hadoop clusters, debug logs from MapReduce programs are not
  available.

- `CDAP-1007 <https://issues.cask.co/browse/CDAP-1007>`__ -
  If the Hive Metastore is restarted while the CDAP Explore Service is running, the
  Explore Service remains alive, but becomes unusable. To correct, restart the CDAP Master
  |---| which will restart all services |---| as described under "Starting CDAP Services"
  for your particular Hadoop distribution in the :ref:`Installation documentation <installation-index>`.

- `CDAP-1587 <https://issues.cask.co/browse/CDAP-1587>`__ - 
  CDAP internally creates tables in the "user" space that begin with the word
  ``"system"``. User datasets with names starting with ``"system"`` can conflict if they
  were to match one of those names. To avoid this, do not start any datasets with the word
  ``"system"``. 

- `CDAP-2632 <https://issues.cask.co/browse/CDAP-2632>`__ -
  The application in the `cdap-kafka-ingest-guide 
  <https://github.com/cdap-guides/cdap-kafka-ingest-guide/tree/release/cdap-3.0-compatible>`__ 
  does not run on Ubuntu 14.x as of CDAP 3.0.x.

- `CDAP-2721 <https://issues.cask.co/browse/CDAP-2721>`__ -
  Metrics for :ref:`FileSets <datasets-fileset>` can show zero values even if there is
  data present, because FileSets do not emit metrics (`CDAP-587
  <https://issues.cask.co/browse/CDAP-587>`).
  
- `CDAP-2831 <https://issues.cask.co/browse/CDAP-2831>`__ -
  A workflow that is scheduled by time will not be run between the failure of the primary
  master and the time that the secondary takes over. This scheduled run will not be
  triggered at all.

- `CDAP-2945 <https://issues.cask.co/browse/CDAP-2945>`__ -
  If the input partition filter for a PartitionedFileSet does not match any partitions,
  MapReduce jobs can fail.

- `CDAP-3000 <https://issues.cask.co/browse/CDAP-3000>`__ -
  The Workflow token is in an inconsistent state for nodes in a fork while the nodes of
  the fork are still running. It becomes consistent after the join.

- `CDAP-3221 <https://issues.cask.co/browse/CDAP-3221>`__ -
  When running in CDAP Standalone mode, if a MapReduce job fails repeatedly, then the SDK
  hits an out-of-memory exception due to ``perm gen``. The Standalone needs restarting at
  this point.

- `CDAP-3262 <https://issues.cask.co/browse/CDAP-3262>`__ -
  For Microsoft Windows, the CDAP Standalone scripts can fail when used with a JAVA_HOME
  that is defined as a path with spaces in it. A workaround is to use a definition of
  JAVA_HOME that does not include spaces, such as ``C:\PROGRA~1\Java\jdk1.7.0_79\bin`` or
  ``C:\ProgramData\Oracle\Java\javapath``.

- `CDAP-3492 <https://issues.cask.co/browse/CDAP-3492>`__ -
  In the CDAP CLI, executing ``'select *'`` from a dataset with many fields generates an error.
  
- `CDAP-3641 <https://issues.cask.co/browse/CDAP-3641>`__ -
  A RESTful API call to retrieve workflow statistics hangs if units (such as "s" for
  seconds) are not provided as part of the query.

- `CDAP-3750 <https://issues.cask.co/browse/CDAP-3750>`__ -
  If a table schema contains a field name that is a reserved word in the Hive DDL, ``'enable explore'`` fails.
  

`Release 3.3.0 <http://docs.cask.co/cdap/3.3.0/index.html>`__
=============================================================

New Features
------------
- `CDAP-961 <https://issues.cask.co/browse/CDAP-961>`__ -
  Added on demand (dynamic) dataset instantiation through program runtime context.

- `CDAP-2303 <https://issues.cask.co/browse/CDAP-2303>`__ -
  Added lookup capability in context that can be used in existing Script, ScriptFilter and Validator transforms.

- `CDAP-3514 <https://issues.cask.co/browse/CDAP-3514>`__ -
  Added an endpoint to get a count of active queries: ``/v3/namespaces/<namespace-id>/data/explore/queries/count``.

- `CDAP-3857 <https://issues.cask.co/browse/CDAP-3857>`__ -
  Added experimental support for running ETL Batch applications on Spark. Introduced an 'engine' setting in the
  configuration that defaults to ``'mapreduce'``, but can be set to ``'spark'``.

- `CDAP-3944 <https://issues.cask.co/browse/CDAP-3944>`__ -
  Added support to PartitionConsumer for concurrency, plus a limit and filter on read.

- `CDAP-3945 <https://issues.cask.co/browse/CDAP-3945>`__ -
  Added support for limiting the number of concurrent schedule runs.

- `CDAP-4016 <https://issues.cask.co/browse/CDAP-4016>`__ -
  Added Java-8 support for Script transforms.

- `CDAP-4022 <https://issues.cask.co/browse/CDAP-4022>`__ -
  Added RESTful APIs to start or stop multiple programs.

- `CDAP-4023 <https://issues.cask.co/browse/CDAP-4023>`__ -
  Added CLI commands to stop, start, restart, or get status of programs in an application.

- `CDAP-4043 <https://issues.cask.co/browse/CDAP-4043>`__ -
  Added support for ETL transforms written in Python.

- `CDAP-4128 <https://issues.cask.co/browse/CDAP-4128>`__ -
  Added a new JavaScript transform that can emit records using an emitter.

- `CDAP-4135 <https://issues.cask.co/browse/CDAP-4135>`__ -
  Added the capability for MapReduce and Spark programs to localize additional resources during setup.

- `CDAP-4228 <https://issues.cask.co/browse/CDAP-4228>`__ -
  Added the ability to configure which artifact a Hydrator plugin should use.

- `CDAP-4230 <https://issues.cask.co/browse/CDAP-4230>`__ -
  Added DAGs to ETL pipelines, which will allow users to fork and merge. ETLConfig has been
  updated to allow representing a DAG.

- `CDAP-4235 <https://issues.cask.co/browse/CDAP-4235>`__ -
  Added AuthorizationPlugin, for pluggable authorization.

- `CDAP-4263 <https://issues.cask.co/browse/CDAP-4263>`__ -
  Added metadata support for stream views.

- `CDAP-4270 <https://issues.cask.co/browse/CDAP-4270>`__ -
  Added CLI support for metadata and lineage.

- `CDAP-4280 <https://issues.cask.co/browse/CDAP-4280>`__ -
  Added the ability to add metadata to artifacts.

- `CDAP-4289 <https://issues.cask.co/browse/CDAP-4289>`__ -
  Added RESTful APIs to set and get properties for an artifact.

- `CDAP-4264 <https://issues.cask.co/browse/CDAP-4264>`__ -
  Added support for automatically annotating CDAP entities with system metadata when they are created or updated.

- `CDAP-4285 <https://issues.cask.co/browse/CDAP-4285>`__ -
  Added an authorization plugin that uses a system dataset to manage ACLs.

- `CDAP-4403 <https://issues.cask.co/browse/CDAP-4403>`__ -
  Moved Hydrator plugins from the CDAP repository as cdap-etl-lib into its own repository.

- `CDAP-4591 <https://issues.cask.co/browse/CDAP-4591>`__ -
  Improved Metadata Indexing and Search to support searches on words in value and tags.

- `CDAP-4592 <https://issues.cask.co/browse/CDAP-4592>`__ -
  Schema fields are stored as Metadata and are searchable.

- `CDAP-4658 <https://issues.cask.co/browse/CDAP-4658>`__ -
  Added capability in CDAP UI to display system tags.

Improvements
------------
- `CDAP-3079 <https://issues.cask.co/browse/CDAP-3079>`__ -
  Table datasets, and any other dataset that implements ``RecordWritable<StructuredRecord>``,
  can now be written to using Hive.

- `CDAP-3887 <https://issues.cask.co/browse/CDAP-3887>`__ -
  The CDAP Router now has a configurable timeout for idle connections, with a default
  timeout of 15 seconds.

- `CDAP-4045 <https://issues.cask.co/browse/CDAP-4045>`__ -
  A new property master.collect.containers.log has been added to cdap-site.xml, which
  determines if container logs are streamed back to the cdap-master process log. (This has
  always been the default behavior). For MapR installations, this must be turned off (set
  to false).

- `CDAP-4133 <https://issues.cask.co/browse/CDAP-4133>`__ -
  Added ability to retrieve the live-info for the AppFabric system service.

- `CDAP-4209 <https://issues.cask.co/browse/CDAP-4209>`__ -
  Added a method to ``ObjectMappedTable`` and ``ObjectStore`` to retrieve a specific
  number of splits between a start and end keys.

- `CDAP-4233 <https://issues.cask.co/browse/CDAP-4233>`__ -
  Messages logged by Hydrator are now prefixed with the name of the stage that logged them.

- `CDAP-4301 <https://issues.cask.co/browse/CDAP-4301>`__ -
  Added support for CDH5.5

- `CDAP-4392 <https://issues.cask.co/browse/CDAP-4392>`__ -
  Upgraded netty-http dependency in CDAP to 0.14.0.

- `CDAP-4444 <https://issues.cask.co/browse/CDAP-4444>`__ -
  Make ``xmllint`` dependency optional and allow setting variables to skip configuration
  file parsing.

- `CDAP-4453 <https://issues.cask.co/browse/CDAP-4453>`__ -
  Added a schema validation |---| for sources, transforms, and sinks |---| that will
  validate the pipeline stages schema during deployment, and report any issues.

- `CDAP-4518 <https://issues.cask.co/browse/CDAP-4518>`__ -
  CDAP Master service will now log important configuration settings on startup.

- `CDAP-4523 <https://issues.cask.co/browse/CDAP-4523>`__ -
  Added the config setting ``master.startup.checks.enabled`` to control whether CDAP
  Master startup checks are run or not.

- `CDAP-4536 <https://issues.cask.co/browse/CDAP-4536>`__ -
  Improved the installation experience by adding to the CDAP Master service checks of
  pre-requisites such as file system permissions, availability of components such as YARN
  and HBase, resource availability during startup, and to error out if any of the
  pre-requisites fail.

- `CDAP-4548 <https://issues.cask.co/browse/CDAP-4548>`__ -
  Added a config setting 'master.collect.app.containers.log' that can be set to 'false' to
  disable streaming of application logs back to the CDAP Master log.

- `CDAP-4598 <https://issues.cask.co/browse/CDAP-4598>`__ -
  Added an error message when a required field is not provided when configuring Hydrator
  pipeline.
  
Bug Fixes
---------
- `CDAP-1174 <https://issues.cask.co/browse/CDAP-1174>`__ -
  Prefix start script functions with ``'cdap'`` to prevent namespace collisions.

- `CDAP-2470 <https://issues.cask.co/browse/CDAP-2470>`__ -
  Added a check to cause a DB (source or sink) pipeline to fail during deployment if the
  table (source or sink) was not found, or if an incorrect connection string was provided.

- `CDAP-3345 <https://issues.cask.co/browse/CDAP-3345>`__ -
  Fixed a bug where the TTL for datasets was incorrect; it was reduced by (a factor of
  1000) after an upgrade. After running the upgrade tool, please make sure the TTL values
  of tables are as expected.

- `CDAP-3542 <https://issues.cask.co/browse/CDAP-3542>`__ -
  Fixed an issue where the failure of a program running in a workflow fork node was
  causing other programs in the same fork node to remain in the RUNNING state, even after
  the Workflow was completed.

- `CDAP-3694 <https://issues.cask.co/browse/CDAP-3694>`__ -
  Fixed test failures in the PurchaseHistory, StreamConversion, and WikipediaPipeline
  example apps included in the CDAP SDK.

- `CDAP-3742 <https://issues.cask.co/browse/CDAP-3742>`__ -
  Fixed a bug where certain MapReduce metrics were not being properly emitted when using
  multiple outputs.

- `CDAP-3761 <https://issues.cask.co/browse/CDAP-3761>`__ -
  Fixed a problem with DBSink column names not being used to filter input record fields
  before writing to a DBSink.

- `CDAP-3807 <https://issues.cask.co/browse/CDAP-3807>`__ -
  Added a fix for case sensitivity handling in DBSink.

- `CDAP-3815 <https://issues.cask.co/browse/CDAP-3815>`__ -
  Fixed an issue where the regex filter for S3 Batch Source wasn't getting applied correctly.

- `CDAP-3861 <https://issues.cask.co/browse/CDAP-3861>`__ -
  Fixed an issue about stopping all dependent services when a service is stopped.

- `CDAP-3900 <https://issues.cask.co/browse/CDAP-3900>`__ -
  Fixed a bug when querying for logs of deleted program runs.

- `CDAP-3902 <https://issues.cask.co/browse/CDAP-3902>`__ -
  Fixed a problem with dataset performance degradation because of making multiple remote
  calls for each "get dataset" request.

- `CDAP-3924 <https://issues.cask.co/browse/CDAP-3924>`__ -
  Fixed QueryClient to work against HTTPS.

- `CDAP-4000 <https://issues.cask.co/browse/CDAP-4000>`__ -
  Fixed an issue where a stream that has a view could not be deleted cleanly.

- `CDAP-4067 <https://issues.cask.co/browse/CDAP-4067>`__ -
  Fixed an issue where socket connections to the TransactionManager were not being closed.

- `CDAP-4092 <https://issues.cask.co/browse/CDAP-4092>`__ -
  Fixes an issue that causes worker threads to go into an infinite recursion while
  exceptions are being thrown in channel handlers.

- `CDAP-4112 <https://issues.cask.co/browse/CDAP-4112>`__ -
  Fixed a bug that prevented applications from using HBase directly.

- `CDAP-4119 <https://issues.cask.co/browse/CDAP-4119>`__ -
  Fixed a problem where when CDAP Master switched from active to standby, the programs
  that were running were marked as failed.

- `CDAP-4240 <https://issues.cask.co/browse/CDAP-4240>`__ -
  Fixed a problem in the CLI command used to load an artifact, where the wrong artifact name
  and version was used if the artifact name ends with a number.

- `CDAP-4294 <https://issues.cask.co/browse/CDAP-4294>`__ -
  Fixed a problem where plugins from another namespace were visible when creating an
  application using a system artifact.

- `CDAP-4316 <https://issues.cask.co/browse/CDAP-4316>`__ -
  Fixed a problem with the CLI attempting to connect to CDAP when the hostname and port
  were incorrect.

- `CDAP-4366 <https://issues.cask.co/browse/CDAP-4366>`__ -
  Improved error message when stream views were not found.

- `CDAP-4393 <https://issues.cask.co/browse/CDAP-4393>`__ -
  Fixed an issue where tags search were failing for certain tags.

- `CDAP-4141 <https://issues.cask.co/browse/CDAP-4141>`__ -
  Fixed node.js version checking for the ``cdap.sh`` script in the CDAP SDK.

- `CDAP-4373 <https://issues.cask.co/browse/CDAP-4373>`__ -
  Fixed a problem that prevented MapReduce jobs from being run when the Resource Manager
  switches from active to standby in a Kerberos-enabled HA cluster.

- `CDAP-4384 <https://issues.cask.co/browse/CDAP-4384>`__ -
  Fixed an issue that prevents streams from being read in HA HDFS mode.

- `CDAP-4526 <https://issues.cask.co/browse/CDAP-4526>`__ -
  Fixed init scripts to print service status when stopped.

- `CDAP-4534 <https://issues.cask.co/browse/CDAP-4534>`__ -
  Added configuration 'router.bypass.auth.regex' to exempt certain URLs from authentication.

- `CDAP-4539 <https://issues.cask.co/browse/CDAP-4539>`__ -
  Fixed a problem in the init scripts that forced ``cdap-kafka-server``, ``cdap-router``,
  and ``cdap-auth-server`` to have the Hive client installed.

- `CDAP-4678 <https://issues.cask.co/browse/CDAP-4678>`__ -
  Fixed an issue where the logs and history list on a Hydrator pipeline view was not
  updating on new runs.

Deprecated and Removed Features
-------------------------------

- See the :ref:`CDAP 3.3.0 Javadocs <javadocs>` for a list of deprecated and removed APIs.

- `CDAP-2481 <https://issues.cask.co/browse/CDAP-2481>`__ -
  Removed a deprecated endpoint to retrieve the status of a currently running node in a workflow.

- `CDAP-2943 <https://issues.cask.co/browse/CDAP-2943>`__ -
  Removed the deprecated builder-style Flow API.

- `CDAP-4128 <https://issues.cask.co/browse/CDAP-4128>`__ -
  Deprecated the Script transform.
  
- `CDAP-4217 <https://issues.cask.co/browse/CDAP-4217>`__ -
  Deprecated createDataSchedule and createTimeSchedule methods in Schedules class and removed
  deprecated Schedule constructor.

- `CDAP-4251 <https://issues.cask.co/browse/CDAP-4251>`__ -
  Removed deprecated fluent style API for Flow configuration. The only supported API is now the configurer style.

Known Issues
------------
- After upgrading CDAP from a pre-3.0 version, any unprocessed metrics data in Kafka will
  be lost and *WARN* log messages will be logged that tell about the inability to process
  old data in the old format.

- `CDAP-797 <https://issues.cask.co/browse/CDAP-797>`__ - 
  When running secure Hadoop clusters, debug logs from MapReduce programs are not
  available.

- `CDAP-1007 <https://issues.cask.co/browse/CDAP-1007>`__ -
  If the Hive Metastore is restarted while the CDAP Explore Service is running, the
  Explore Service remains alive, but becomes unusable. To correct, restart the CDAP Master
  |---| which will restart all services |---| as described under "Starting CDAP Services"
  for your particular Hadoop distribution in the :ref:`Installation documentation <installation-index>`.

- `CDAP-1587 <https://issues.cask.co/browse/CDAP-1587>`__ - 
  CDAP internally creates tables in the "user" space that begin with the word
  ``"system"``. User datasets with names starting with ``"system"`` can conflict if they
  were to match one of those names. To avoid this, do not start any datasets with the word
  ``"system"``. 

- `CDAP-2632 <https://issues.cask.co/browse/CDAP-2632>`__ -
  The application in the `cdap-kafka-ingest-guide 
  <https://github.com/cdap-guides/cdap-kafka-ingest-guide/tree/release/cdap-3.0-compatible>`__ 
  does not run on Ubuntu 14.x as of CDAP 3.0.x.

- `CDAP-2721 <https://issues.cask.co/browse/CDAP-2721>`__ -
  Metrics for :ref:`FileSets <datasets-fileset>` can show zero values even if there is
  data present, because FileSets do not emit metrics (`CDAP-587
  <https://issues.cask.co/browse/CDAP-587>`).
  
- `CDAP-2831 <https://issues.cask.co/browse/CDAP-2831>`__ -
  A workflow that is scheduled by time will not be run between the failure of the primary
  master and the time that the secondary takes over. This scheduled run will not be
  triggered at all.

- `CDAP-2945 <https://issues.cask.co/browse/CDAP-2945>`__ -
  If the input partition filter for a PartitionedFileSet does not match any partitions,
  MapReduce jobs can fail.

- `CDAP-3000 <https://issues.cask.co/browse/CDAP-3000>`__ -
  The Workflow token is in an inconsistent state for nodes in a fork while the nodes of
  the fork are still running. It becomes consistent after the join.

- `CDAP-3221 <https://issues.cask.co/browse/CDAP-3221>`__ -
  When running in CDAP Standalone mode, if a MapReduce job fails repeatedly, then the SDK
  hits an out-of-memory exception due to ``perm gen``. The Standalone needs restarting at
  this point.

- `CDAP-3262 <https://issues.cask.co/browse/CDAP-3262>`__ -
  For Microsoft Windows, the CDAP Standalone scripts can fail when used with a JAVA_HOME
  that is defined as a path with spaces in it. A workaround is to use a definition of
  JAVA_HOME that does not include spaces, such as ``C:\PROGRA~1\Java\jdk1.7.0_79\bin`` or
  ``C:\ProgramData\Oracle\Java\javapath``.

- `CDAP-3492 <https://issues.cask.co/browse/CDAP-3492>`__ -
  In the CDAP CLI, executing ``'select *'`` from a dataset with many fields generates an error.
  
- `CDAP-3641 <https://issues.cask.co/browse/CDAP-3641>`__ -
  A RESTful API call to retrieve workflow statistics hangs if units (such as "s" for
  seconds) are not provided as part of the query.

- `CDAP-3750 <https://issues.cask.co/browse/CDAP-3750>`__ -
  If a table schema contains a field name that is a reserved word in the Hive DDL, ``'enable explore'`` fails.
  

`Release 3.2.1 <http://docs.cask.co/cdap/3.2.1/index.html>`__
=============================================================

New Features
------------

- `CDAP-3951 <https://issues.cask.co/browse/CDAP-3951>`__ -
  Added the ability for S3 batch sources and sinks to set additional file system properties.
  
Improvements
------------

- `CDAP-3870 <https://issues.cask.co/browse/CDAP-3870>`__ -
  Added logging and metrics support for *Script*, *ScriptFilter*, and *Validator* transforms.
  
- `CDAP-3939 <https://issues.cask.co/browse/CDAP-3939>`__ -
  Improved artifact and application deployment failure handling.

Bug Fixes
---------

- `CDAP-3342 <https://issues.cask.co/browse/CDAP-3342>`__ -
  Fixed a problem with the CDAP SDK unable to start on certain Windows machines by updating
  the Hadoop native library in CDAP with a version that does not have a dependency on a
  debug version of the Microsoft ``msvcr100.dll``.
  
- `CDAP-3815 <https://issues.cask.co/browse/CDAP-3815>`__ -
  Fixed an issue where the regex filter for S3 batch sources wasn't being applied correctly.
  
- `CDAP-3829 <https://issues.cask.co/browse/CDAP-3829>`__ -
  Fixed snapshot sinks so that the data is explorable as a ``PartitionedFileSet``.
  
- `CDAP-3833 <https://issues.cask.co/browse/CDAP-3833>`__ -
  Fixed snapshot sinks so that they can be read safely.
  
- `CDAP-3859 <https://issues.cask.co/browse/CDAP-3859>`__ -
  Fixed a compilation error in the Maven application archetype.
  
- `CDAP-3860 <https://issues.cask.co/browse/CDAP-3860>`__ -
  Fixed a bug where plugins, packaged in the same artifact as an application class, could not be used by that application class.
  
- `CDAP-3891 <https://issues.cask.co/browse/CDAP-3891>`__ -
  Updated the documentation to remove references to application templates and adaptors that were removed as of CDAP 3.2.0.
  
- `CDAP-3949 <https://issues.cask.co/browse/CDAP-3949>`__ -
  Fixed a problem with running certain examples on Linux systems by increasing the maximum
  Java heap size of the Standalone SDK on Linux systems to 2048m.

- `CDAP-3961 <https://issues.cask.co/browse/CDAP-3961>`__ -
  Fixed a missing dependency on ``cdap-hbase-compat-1.1`` package in the CDAP Master package. 

Known Issues
------------
- After upgrading CDAP from a pre-3.0 version, any unprocessed metrics data in Kafka will
  be lost and *WARN* log messages will be logged that tell about the inability to process
  old data in the old format.

- `CDAP-797 <https://issues.cask.co/browse/CDAP-797>`__ - 
  When running secure Hadoop clusters, debug logs from MapReduce programs are not
  available.

- `CDAP-1007 <https://issues.cask.co/browse/CDAP-1007>`__ -
  If the Hive Metastore is restarted while the CDAP Explore Service is running, the
  Explore Service remains alive, but becomes unusable. To correct, restart the CDAP Master
  |---| which will restart all services |---| as described under "Starting CDAP Services"
  for your particular Hadoop distribution in the :ref:`Installation documentation <installation-index>`.

- `CDAP-1587 <https://issues.cask.co/browse/CDAP-1587>`__ - 
  CDAP internally creates tables in the "user" space that begin with the word
  ``"system"``. User datasets with names starting with ``"system"`` can conflict if they
  were to match one of those names. To avoid this, do not start any datasets with the word
  ``"system"``. 

- `CDAP-2632 <https://issues.cask.co/browse/CDAP-2632>`__ -
  The application in the `cdap-kafka-ingest-guide 
  <https://github.com/cdap-guides/cdap-kafka-ingest-guide/tree/release/cdap-3.0-compatible>`__ 
  does not run on Ubuntu 14.x as of CDAP 3.0.x.

- `CDAP-2721 <https://issues.cask.co/browse/CDAP-2721>`__ -
  Metrics for :ref:`FileSets <datasets-fileset>` can show zero values even if there is
  data present, because FileSets do not emit metrics (`CDAP-587
  <https://issues.cask.co/browse/CDAP-587>`).
  
- `CDAP-2831 <https://issues.cask.co/browse/CDAP-2831>`__ -
  A workflow that is scheduled by time will not be run between the failure of the primary
  master and the time that the secondary takes over. This scheduled run will not be
  triggered at all.

- `CDAP-2945 <https://issues.cask.co/browse/CDAP-2945>`__ -
  If the input partition filter for a PartitionedFileSet does not match any partitions,
  MapReduce jobs can fail.

- `CDAP-3000 <https://issues.cask.co/browse/CDAP-3000>`__ -
  The Workflow token is in an inconsistent state for nodes in a fork while the nodes of
  the fork are still running. It becomes consistent after the join.

- `CDAP-3221 <https://issues.cask.co/browse/CDAP-3221>`__ -
  When running in CDAP Standalone mode, if a MapReduce job fails repeatedly, then the SDK
  hits an out-of-memory exception due to ``perm gen``. The Standalone needs restarting at
  this point.

- `CDAP-3262 <https://issues.cask.co/browse/CDAP-3262>`__ -
  For Microsoft Windows, the CDAP Standalone scripts can fail when used with a JAVA_HOME
  that is defined as a path with spaces in it. A workaround is to use a definition of
  JAVA_HOME that does not include spaces, such as ``C:\PROGRA~1\Java\jdk1.7.0_79\bin`` or
  ``C:\ProgramData\Oracle\Java\javapath``.

- `CDAP-3492 <https://issues.cask.co/browse/CDAP-3492>`__ -
  In the CDAP CLI, executing ``'select *'`` from a dataset with many fields generates an error.
  
- `CDAP-3641 <https://issues.cask.co/browse/CDAP-3641>`__ -
  A RESTful API call to retrieve workflow statistics hangs if units (such as "s" for
  seconds) are not provided as part of the query.

- `CDAP-3750 <https://issues.cask.co/browse/CDAP-3750>`__ -
  If a table schema contains a field name that is a reserved word in the Hive DDL, ``'enable explore'`` fails.
  

`Release 3.2.0 <http://docs.cask.co/cdap/3.2.0/index.html>`__
=============================================================

New Features
------------
  
- `CDAP-2556 <https://issues.cask.co/browse/CDAP-2556>`__ -
  Added support for HBase1.1.
  
- `CDAP-2666 <https://issues.cask.co/browse/CDAP-2666>`__ -
  Added a new API for creating an application from an artifact.
  
- `CDAP-2756 <https://issues.cask.co/browse/CDAP-2756>`__ -
  Added the ability to write to multiple outputs from a MapReduce job.
  
- `CDAP-2757 <https://issues.cask.co/browse/CDAP-2757>`__ -
  Added the ability to dynamically write to multiple partitions of a PartitionedFileSet
  dataset as the output of a MapReduce job.
  
- `CDAP-3253 <https://issues.cask.co/browse/CDAP-3253>`__ -
  Added a Stream and Dataset Widget to the CDAP-UI.
  
- `CDAP-3390 <https://issues.cask.co/browse/CDAP-3390>`__ -
  Added stream views, enabling reading from a single stream using various formats and
  schemas.
  
- `CDAP-3476 <https://issues.cask.co/browse/CDAP-3476>`__ -
  Added a Validator Transform that can be used to validate records based on a set of
  available validators and configured to write invalid records to an error
  dataset.
  
- `CDAP-3516 <https://issues.cask.co/browse/CDAP-3516>`__ -
  Added a service to manage the metadata of CDAP entities.
  
- `CDAP-3518 <https://issues.cask.co/browse/CDAP-3518>`__ -
  Added the publishing of metadata change notifications to Apache Kafka.
  
- `CDAP-3519 <https://issues.cask.co/browse/CDAP-3519>`__ -
  Added the ability to compute lineage of a CDAP dataset or stream in a given time window.
  
- `CDAP-3520 <https://issues.cask.co/browse/CDAP-3520>`__ -
  Added RESTful APIs for adding/retrieving/deleting of metadata for apps/programs/datasets/streams.
  
- `CDAP-3521 <https://issues.cask.co/browse/CDAP-3521>`__ -
  Added the ability to record a dataset or stream access by a CDAP program.
  
- `CDAP-3522 <https://issues.cask.co/browse/CDAP-3522>`__ -
  Added the capability to search CDAP entities based on their metadata.
  
- `CDAP-3523 <https://issues.cask.co/browse/CDAP-3523>`__ -
  Added RESTful APIs for searching CDAP entities based on business metadata.
  
- `CDAP-3527 <https://issues.cask.co/browse/CDAP-3527>`__ -
  Added a data store to manage business metadata of CDAP entities.

- `CDAP-3549 <https://issues.cask.co/browse/CDAP-3549>`__ -
  Added SSH port forwarding to the CDAP virtual machine.
  
- `CDAP-3556 <https://issues.cask.co/browse/CDAP-3556>`__ -
  Added a data store for recording data accesses by CDAP programs and computing lineage.
  
- `CDAP-3590 <https://issues.cask.co/browse/CDAP-3590>`__ -
  Added the ability to write to multiple sinks in ETL real-time and batch applications.
  
- `CDAP-3591 <https://issues.cask.co/browse/CDAP-3591>`__ -
  Added the ability for real-time ETL pipelines to write to multiple sinks.
  
- `CDAP-3592 <https://issues.cask.co/browse/CDAP-3592>`__ -
  Added the ability for batch ETL pipelines to write to multiple sinks.

- `CDAP-3626 <https://issues.cask.co/browse/CDAP-3626>`__ -
  For the CSV and TSV stream formats, a "mapping" setting can now be specified, mapping
  stream event columns to schema columns.
  
- `CDAP-3693 <https://issues.cask.co/browse/CDAP-3693>`__ -
  Added support for CDAP to work with HDP 2.3.


Improvements
------------

- `CDAP-1914 <https://issues.cask.co/browse/CDAP-1914>`__ -
  Added documentation of the RESTful endpoint to retrieve the properties of a stream.

- `CDAP-2514 <https://issues.cask.co/browse/CDAP-2514>`__ -
  Added an interface to load a file into a stream from the CDAP-UI.
  
- `CDAP-2809 <https://issues.cask.co/browse/CDAP-2809>`__ -
  The CDAP-UI "Errors" pop-up in the main screen now displays the time and date for each
  error.
    
- `CDAP-2872 <https://issues.cask.co/browse/CDAP-2872>`__ -
  Updated the Cloudera Manager CSD to use support for logback.
  
- `CDAP-2950 <https://issues.cask.co/browse/CDAP-2950>`__ -
  Cleaned up the messages shown in the errors dropdown in the CDAP-UI.

- `CDAP-3147 <https://issues.cask.co/browse/CDAP-3147>`__ -
  Added a  CDAP-CLI command to stop a workflow.
  
- `CDAP-3179 <https://issues.cask.co/browse/CDAP-3179>`__ -
  Added support for upgrading the Hadoop distribution or the HBase version that CDAP is
  running on.
  
- `CDAP-3257 <https://issues.cask.co/browse/CDAP-3257>`__ -
  Revised the documentation of the file ``cdap-default.xml``, removed properties no longer
  in use, and corrected discrepancies between the documentation and the shipped XML
  file.

- `CDAP-3270 <https://issues.cask.co/browse/CDAP-3270>`__ -
  Improved the help provided in the CDAP-CLI for the setting of stream formats.
  
- `CDAP-3275 <https://issues.cask.co/browse/CDAP-3275>`__ -
  Upgraded netty-http version to 0.12.0.
  
- `CDAP-3282 <https://issues.cask.co/browse/CDAP-3282>`__ -
  Added a HTTP RESTful API to update the application configuration and artifact version.
  
- `CDAP-3332 <https://issues.cask.co/browse/CDAP-3332>`__ -
  Added a "clear" button in the CDAP-UI for cases where a user decides to not used a
  pre-populated schema.
  
- `CDAP-3351 <https://issues.cask.co/browse/CDAP-3351>`__ -
  Defined a directory structure to be used for predefined applications.
  
- `CDAP-3357 <https://issues.cask.co/browse/CDAP-3357>`__ -
  Added documentation in the source code on adding new commands and completers to the CDAP-CLI.

- `CDAP-3393 <https://issues.cask.co/browse/CDAP-3393>`__ -
  In the CDAP-UI, added visualization for Workflow tokens in Workflows.
  
- `CDAP-3419 <https://issues.cask.co/browse/CDAP-3419>`__ -
  HBaseQueueDebugger now shows the minimum queue event transaction write pointer both for
  each queue and for all queues.
  
- `CDAP-3443 <https://issues.cask.co/browse/CDAP-3443>`__ -
  Added an example cdap-env.sh to the shipped packages.
  
- `CDAP-3464 <https://issues.cask.co/browse/CDAP-3464>`__ -
  Added an example in the documentation explaining how to prune invalid transactions from
  the transaction manager.

- `CDAP-3490 <https://issues.cask.co/browse/CDAP-3490>`__ -
  Modified the CDAP upgrade tool to delete all adapters and the ETLBatch and ETLRealtime
  ApplicationTemplates.
  
- `CDAP-3495 <https://issues.cask.co/browse/CDAP-3495>`__ -
  Added the ability to persist the runtime arguments with which a program was run.

- `CDAP-3550 <https://issues.cask.co/browse/CDAP-3550>`__ -
  Added support for writing to Amazon S3 in Avro and Parquet formats from batch ETL
  applications.

- `CDAP-3564 <https://issues.cask.co/browse/CDAP-3564>`__ -
  Updated CDAP to use Tephra 0.6.2.

- `CDAP-3610 <https://issues.cask.co/browse/CDAP-3610>`__ -
  Updated the transaction debugger client to print checkpoint information.

Bug Fixes
---------

- `CDAP-1697 <https://issues.cask.co/browse/CDAP-1697>`__ -
  Fixed an issue where failed dataset operations via Explore queries did not invalidate
  the associated transaction.
  
- `CDAP-1864 <https://issues.cask.co/browse/CDAP-1864>`__ -
  Fixed a problem where users got an incorrect message while creating a dataset in a
  non-existent namespace.
  
- `CDAP-1892 <https://issues.cask.co/browse/CDAP-1892>`__ -
  Fixed a problem with services returning the same message for all failures.
  
- `CDAP-1984 <https://issues.cask.co/browse/CDAP-1984>`__ -
  Fixed a problem where a dataset could be created in a non-existent namespace in
  standalone mode.
  
- `CDAP-2428 <https://issues.cask.co/browse/CDAP-2428>`__ -
  Fixed a problem with the CDAP-CLI creating file logs.
  
- `CDAP-2521 <https://issues.cask.co/browse/CDAP-2521>`__ -
  Fixed a problem with the CDAP-CLI not auto-completing when setting a stream format.
  
- `CDAP-2785 <https://issues.cask.co/browse/CDAP-2785>`__ -
  Fixed a problem with the CDAP-UI of buttons staying 'in focus' after clicking.
  
- `CDAP-2809 <https://issues.cask.co/browse/CDAP-2809>`__ -
  The CDAP-UI "Errors" pop-up in the main screen now displays the time and date for each error.
  
- `CDAP-2892 <https://issues.cask.co/browse/CDAP-2892>`__ -
  Fixed a problem with schedules not being deployed in suspended mode.
  
- `CDAP-3014 <https://issues.cask.co/browse/CDAP-3014>`__ -
  Fixed a problem where failure of a spark node would cause a workflow to restart indefinitely.
  
- `CDAP-3073 <https://issues.cask.co/browse/CDAP-3073>`__ -
  Fixed an issue with the CDAP standalone process periodically crashing with Out-of-Memory
  errors when writing to an Oracle table.
  
- `CDAP-3101 <https://issues.cask.co/browse/CDAP-3101>`__ -
  Fixed a problem with workflow runs not getting scheduled due to Quartz exceptions.
  
- `CDAP-3121 <https://issues.cask.co/browse/CDAP-3121>`__ -
  Fixed a problem with discrepancies between the documentation and the defaults actually used by CDAP.
  
- `CDAP-3200 <https://issues.cask.co/browse/CDAP-3200>`__ -
  Fixed a problem in the CDAP-UI with the clone button in an incorrect position when using Firefox.
  
- `CDAP-3201 <https://issues.cask.co/browse/CDAP-3201>`__ -
  Fixed a problem in the CDAP-UI with an incorrect tabbing order when using Firefox.
  
- `CDAP-3219 <https://issues.cask.co/browse/CDAP-3219>`__ -
  Fixed a problem when specifying the HBase version using the HBASE_VERSION environment variable.
  
- `CDAP-3233 <https://issues.cask.co/browse/CDAP-3233>`__ -
  Fixed a problem in the CDAP-UI error pop-ups not having a default focus on a button.
  
- `CDAP-3243 <https://issues.cask.co/browse/CDAP-3243>`__ -
  Fixed a problem in the CDAP-UI with the default schema shown for streams.
  
- `CDAP-3260 <https://issues.cask.co/browse/CDAP-3260>`__ -
  Fixed a problem in the CDAP-UI with scrolling on the namespaces dropdown on certain pages.
  
- `CDAP-3261 <https://issues.cask.co/browse/CDAP-3261>`__ -
  Fixed a problem on CDAP distributed mode with the serializing of the metadata artifact
  causing a stack overflow.
  
- `CDAP-3305 <https://issues.cask.co/browse/CDAP-3305>`__ -
  Fixed a problem in the CDAP-UI not warning users if they exit or close their browser without saving.
  
- `CDAP-3313 <https://issues.cask.co/browse/CDAP-3313>`__ -
  Fixed a problem in the CDAP-UI with refreshing always returning to the overview page.
  
- `CDAP-3326 <https://issues.cask.co/browse/CDAP-3326>`__ -
  Fixed a problem with the table batch source requiring a row key to be set.
  
- `CDAP-3343 <https://issues.cask.co/browse/CDAP-3343>`__ -
  Fixed a problem with the application deployment for apps that contain Spark.
  
- `CDAP-3349 <https://issues.cask.co/browse/CDAP-3349>`__ -
  Fixed a problem with the display of ETL application metrics in the CDAP-UI.
  
- `CDAP-3355 <https://issues.cask.co/browse/CDAP-3355>`__ -
  Fixed a problem in the CDAP examples with the use of a runtime argument, ``min.pages.threshold``.
  
- `CDAP-3362 <https://issues.cask.co/browse/CDAP-3362>`__ -
  Fixed a problem with the ``logback-container.xml`` not being copied into master services.
  
- `CDAP-3374 <https://issues.cask.co/browse/CDAP-3374>`__ -
  Fixed a problem with warning messages in the logs indicating that programs were running
  that actually were not running.
  
- `CDAP-3376 <https://issues.cask.co/browse/CDAP-3376>`__ -
  Fixed a problem with being unable to deploy the SparkPageRank example application on a cluster.
  
- `CDAP-3386 <https://issues.cask.co/browse/CDAP-3386>`__ -
  Fixed a problem with the Spark classes not being found when running a Spark program
  through a Workflow in CDAP Distributed mode on HDP 2.2.
  
- `CDAP-3394 <https://issues.cask.co/browse/CDAP-3394>`__ -
  Fixed a problem with the deployment of applications through the CDAP-UI.
  
- `CDAP-3399 <https://issues.cask.co/browse/CDAP-3399>`__ -
  Fixed a problem with the SparkPageRankApp example spawning multiple containers in
  distributed mode due to its number of services.
  
- `CDAP-3400 <https://issues.cask.co/browse/CDAP-3400>`__ -
  Fixed an issue with warning messages about the notification system every time the CDAP
  Standalone is restarted.
  
- `CDAP-3408 <https://issues.cask.co/browse/CDAP-3408>`__ -
  Fixed a problem with running the CDAP Explore Service on CDH 5.[2,3].
  
- `CDAP-3432 <https://issues.cask.co/browse/CDAP-3432>`__ -
  Fixed a bug where connecting with a certain namespace from the CLI would not immediately
  display that namespace in the CLI prompt.
  
- `CDAP-3435 <https://issues.cask.co/browse/CDAP-3435>`__ -
  Fixed an issue where the program status was shown as running even after it is stopped.
  
- `CDAP-3442 <https://issues.cask.co/browse/CDAP-3442>`__ -
  Fixed a problem that caused application creation to fail if a config setting was given
  to an application that does not use a config.
  
- `CDAP-3449 <https://issues.cask.co/browse/CDAP-3449>`__ -
  Fixed a problem with the readless increment co-processor not handling multiple readless
  increment columns in the same row.
  
- `CDAP-3452 <https://issues.cask.co/browse/CDAP-3452>`__ -
  Fixed a problem that prevented explore service working on clusters with secure hive 0.14.
  
- `CDAP-3458 <https://issues.cask.co/browse/CDAP-3458>`__ -
  Fixed a problem where streams events that had already been processed were re-processed in flows.
  
- `CDAP-3470 <https://issues.cask.co/browse/CDAP-3470>`__ -
  Fixed an issue with error messages being logged during a master process restart.
  
- `CDAP-3472 <https://issues.cask.co/browse/CDAP-3472>`__ -
  Fixed the error message returned when trying to stop a program started by a workflow.
  
- `CDAP-3473 <https://issues.cask.co/browse/CDAP-3473>`__ -
  Fixed a problem with a workflow failure not updating a run record for the inner program.
    
- `CDAP-3530 <https://issues.cask.co/browse/CDAP-3530>`__ -
  Fixed a problem with the CDAP-UI performance when rendering flow diagrams with a large number of nodes.
  
- `CDAP-3563 <https://issues.cask.co/browse/CDAP-3563>`__ -
  Removed faulty and unused metrics around CDAP file resource usage.
  
- `CDAP-3574 <https://issues.cask.co/browse/CDAP-3574>`__ -
  Fix an issue with Explore not working on HDP Hive 0.12.
  
- `CDAP-3603 <https://issues.cask.co/browse/CDAP-3603>`__ -
  Fixed an issue with configuration properties for ETL Transforms being validated at
  runtime instead of when an application is created.
  
- `CDAP-3618 <https://issues.cask.co/browse/CDAP-3618>`__ -
  Fix a problem where suspended schedules were lost when CDAP master was restarted.
  
- `CDAP-3660 <https://issues.cask.co/browse/CDAP-3660>`__ -
  Fixed and issue where the Hadoop filesystem object was getting instantiated before the
  Kerberos keytab login was completed, leading to CDAP processes failing after the initial
  ticket expired.
  
- `CDAP-3700 <https://issues.cask.co/browse/CDAP-3700>`__ -
  Fixed an issue with the log saver having numerous open connections to HBase, causing it
  to go Out-of-Memory.

- `CDAP-3711 <https://issues.cask.co/browse/CDAP-3711>`__ -
  Fixed an issue that prevented the downloading of Explore results on a secure cluster.
  
- `CDAP-3713 <https://issues.cask.co/browse/CDAP-3713>`__ -
  Fixed an issue where certain RESTful APIs were not returning appropriate error messages
  for internal server errors.
  
- `CDAP-3716 <https://issues.cask.co/browse/CDAP-3716>`__ -
  Fixed a possible deadlock when CDAP master is restarted with an existing app running on a cluster.

API Changes
-----------

- `CDAP-2763 <https://issues.cask.co/browse/CDAP-2763>`__ -
  Added RESTful APIs for managing artifacts.
  
- `CDAP-2956 <https://issues.cask.co/browse/CDAP-2956>`__ -
  Deprecated the existing API for configuring a workflow action, replacing it with a
  simpler API.
  
- `CDAP-3063 <https://issues.cask.co/browse/CDAP-3063>`__ -
  Added CLI commands for managing artifacts.
  
- `CDAP-3064 <https://issues.cask.co/browse/CDAP-3064>`__ -
  Added an ArtifactClient to interact with Artifact HTTP RESTful APIs.
  
- `CDAP-3283 <https://issues.cask.co/browse/CDAP-3283>`__ -
  Added artifact information to Application RESTful APIs and the means to filter
  applications by artifact name and version.
  
- `CDAP-3324 <https://issues.cask.co/browse/CDAP-3324>`__ -
  Added a RESTful API for creating an application from an artifact.
  
- `CDAP-3367 <https://issues.cask.co/browse/CDAP-3367>`__ -
  Added the ability to delete an artifact.
  
- `CDAP-3488 <https://issues.cask.co/browse/CDAP-3488>`__ -
  Changed the ETLBatchTemplate from an ApplicationTemplate to an Application.

- `CDAP-3535 <https://issues.cask.co/browse/CDAP-3535>`__ -
  Added an API for programs to retrieve their application specification at runtime.
  
- `CDAP-3554 <https://issues.cask.co/browse/CDAP-3554>`__ -
  Changed the plugin types from 'source' to either 'batchsource' or 'realtimesource', and
  from 'sink' to either 'batchsink' or 'realtimesink' to reflect that the plugins
  implement different interfaces.

- `CDAP-1554 <https://issues.cask.co/browse/CDAP-1554>`__ -
  Moved constants for default and system namespaces from Common to Id.

- `CDAP-3388 <https://issues.cask.co/browse/CDAP-3388>`__ -
  Added interfaces to ``cdap-spi`` that abstract StreamEventRecordFormat (and dependent
  interfaces) so users can extend the ``cdap-spi`` interfaces.

- `CDAP-3583 <https://issues.cask.co/browse/CDAP-3583>`__ -
  Added a RESTful API for retrieving the metadata associated with a particular run of a
  CDAP program.
  
- `CDAP-3632 <https://issues.cask.co/browse/CDAP-3632>`__ -
  Added a RESTful API for computing lineage of a CDAP dataset or stream.

Deprecated and Removed Features
-------------------------------

- See the :ref:`CDAP 3.2.0 Javadocs <javadocs>` for a list of deprecated and removed APIs.

- `CDAP-2667 <https://issues.cask.co/browse/CDAP-2667>`__ -
  Removed application templates and adapters RESTful APIs, as these templates and adapters
  have been replaced with applications that can be controlled with the 
  :ref:`Lifecycle HTTP RESTful API <http-restful-api-lifecycle>`.

- `CDAP-2951 <https://issues.cask.co/browse/CDAP-2951>`__ -
  Removed deprecated methods in cdap-client.
  
- `CDAP-3596 <https://issues.cask.co/browse/CDAP-3596>`__ -
  Replaced the ETL ApplicationTemplates with the new ETL Applications.

Known Issues
------------
- After upgrading CDAP from a pre-3.0 version, any unprocessed metrics data in Kafka will
  be lost and *WARN* log messages will be logged that tell about the inability to process
  old data in the old format.

- `CDAP-797 <https://issues.cask.co/browse/CDAP-797>`__ - 
  When running secure Hadoop clusters, debug logs from MapReduce programs are not
  available.

- `CDAP-1007 <https://issues.cask.co/browse/CDAP-1007>`__ -
  If the Hive Metastore is restarted while the CDAP Explore Service is running, the
  Explore Service remains alive, but becomes unusable. To correct, restart the CDAP Master
  |---| which will restart all services |---| as described under "Starting CDAP Services"
  for your particular Hadoop distribution in the :ref:`Installation documentation <installation-index>`.

- `CDAP-1587 <https://issues.cask.co/browse/CDAP-1587>`__ - 
  CDAP internally creates tables in the "user" space that begin with the word
  ``"system"``. User datasets with names starting with ``"system"`` can conflict if they
  were to match one of those names. To avoid this, do not start any datasets with the word
  ``"system"``. 

- `CDAP-2632 <https://issues.cask.co/browse/CDAP-2632>`__ -
  The application in the `cdap-kafka-ingest-guide 
  <https://github.com/cdap-guides/cdap-kafka-ingest-guide/tree/release/cdap-3.0-compatible>`__ 
  does not run on Ubuntu 14.x as of CDAP 3.0.x.

- `CDAP-2721 <https://issues.cask.co/browse/CDAP-2721>`__ -
  Metrics for :ref:`FileSets <datasets-fileset>` can show zero values even if there is
  data present, because FileSets do not emit metrics (`CDAP-587
  <https://issues.cask.co/browse/CDAP-587>`).
  
- `CDAP-2831 <https://issues.cask.co/browse/CDAP-2831>`__ -
  A workflow that is scheduled by time will not be run between the failure of the primary
  master and the time that the secondary takes over. This scheduled run will not be
  triggered at all.

- `CDAP-2945 <https://issues.cask.co/browse/CDAP-2945>`__ -
  If the input partition filter for a PartitionedFileSet does not match any partitions,
  MapReduce jobs can fail.

- `CDAP-3000 <https://issues.cask.co/browse/CDAP-3000>`__ -
  The Workflow token is in an inconsistent state for nodes in a fork while the nodes of
  the fork are still running. It becomes consistent after the join.

- `CDAP-3221 <https://issues.cask.co/browse/CDAP-3221>`__ -
  When running in CDAP Standalone mode, if a MapReduce job fails repeatedly, then the SDK
  hits an out-of-memory exception due to ``perm gen``. The Standalone needs restarting at
  this point.

- `CDAP-3262 <https://issues.cask.co/browse/CDAP-3262>`__ -
  For Microsoft Windows, the CDAP Standalone scripts can fail when used with a JAVA_HOME
  that is defined as a path with spaces in it. A workaround is to use a definition of
  JAVA_HOME that does not include spaces, such as ``C:\PROGRA~1\Java\jdk1.7.0_79\bin`` or
  ``C:\ProgramData\Oracle\Java\javapath``.

- `CDAP-3492 <https://issues.cask.co/browse/CDAP-3492>`__ -
  In the CDAP CLI, executing ``'select *'`` from a dataset with many fields generates an error.
  
- `CDAP-3641 <https://issues.cask.co/browse/CDAP-3641>`__ -
  A RESTful API call to retrieve workflow statistics hangs if units (such as "s" for
  seconds) are not provided as part of the query.

- `CDAP-3697 <https://issues.cask.co/browse/CDAP-3697>`__ -
  CDAP Explore is broken on secure CDH 5.1.
  
- `CDAP-3698 <https://issues.cask.co/browse/CDAP-3698>`__ -
  CDAP Explore is unable to get a delegation token while fetching next results on HDP2.0.
  
- `CDAP-3749 <https://issues.cask.co/browse/CDAP-3749>`__ -
  The DBSource plugin does not allow a username with an empty password.
  
- `CDAP-3750 <https://issues.cask.co/browse/CDAP-3750>`__ -
  If a table schema contains a field name that is a reserved word in the Hive DDL, ``'enable explore'`` fails.

- `CDAP-3819 <https://issues.cask.co/browse/CDAP-3819>`__ -
  The Cassandra source does not handles spaces properly in column fields which require a comma-separated list.


`Release 3.1.0 <http://docs.cask.co/cdap/3.1.0/index.html>`__
=============================================================

New Features
------------

**MapR 4.1 Support, HDP 2.2 Support, CDH 5.4 Support**

- `CDAP-1614 <https://issues.cask.co/browse/CDAP-1614>`__ -
  Added HBase 1.0 support.

- `CDAP-2318 <https://issues.cask.co/browse/CDAP-2318>`__ -
  Made CDAP work on the HDP 2.2 distribution.

- `CDAP-2786 <https://issues.cask.co/browse/CDAP-2786>`__ -
  Added support to CDAP 3.1.0 for the MapR 4.1 distro.

- `CDAP-2798 <https://issues.cask.co/browse/CDAP-2798>`__ -
  Added Hive 0.14 support.

- `CDAP-2801 <https://issues.cask.co/browse/CDAP-2801>`__ -
  Added CDH 5.4 Hive 1.1 support.

- `CDAP-2836 <https://issues.cask.co/browse/CDAP-2836>`__ -
  Added support for restart of specific CDAP System Services Instances.

- `CDAP-2853 <https://issues.cask.co/browse/CDAP-2853>`__ -
  Completed certification process for MapR on CDAP.

- `CDAP-2879 <https://issues.cask.co/browse/CDAP-2879>`__ -
  Added Hive 1.0 in Standalone.

- `CDAP-2881 <https://issues.cask.co/browse/CDAP-2881>`__ -
  Added support for HDP 2.2.x.

- `CDAP-2891 <https://issues.cask.co/browse/CDAP-2891>`__ -
  Documented cdap-env.sh and settings OPTS for HDP 2.2.

- `CDAP-2898 <https://issues.cask.co/browse/CDAP-2898>`__ -
  Added Hive 1.1 in Standalone.

- `CDAP-2953 <https://issues.cask.co/browse/CDAP-2953>`__ -
  Added HiveServer2 support in a secure cluster.


**Spark**

- `CDAP-344 <https://issues.cask.co/browse/CDAP-344>`__ -
  Users can now run Spark in distributed mode.

- `CDAP-1993 <https://issues.cask.co/browse/CDAP-1993>`__ -
  Added ability to manipulate the SparkConf.

- `CDAP-2700 <https://issues.cask.co/browse/CDAP-2700>`__ -
  Added the ability to Spark programs of discovering CDAP services in distributed mode.

- `CDAP-2701 <https://issues.cask.co/browse/CDAP-2701>`__ -
  Spark programs are able to collect Metrics in distributed mode.

- `CDAP-2703 <https://issues.cask.co/browse/CDAP-2703>`__ -
  Users are able to collect/view logs from Spark programs in distributed mode.

- `CDAP-2705 <https://issues.cask.co/browse/CDAP-2705>`__ -
  Added examples, guides and documentation for Spark in distributed mode. LogAnalysis
  application demonstrating parallel execution of the Spark and MapReduce programs using
  Workflows.

- `CDAP-2923 <https://issues.cask.co/browse/CDAP-2923>`__ -
  Added support for the WorkflowToken in the Spark programs.

- `CDAP-2936 <https://issues.cask.co/browse/CDAP-2936>`__ -
  Spark program can now specify resources usage for driver and executor process in distributed mode.


**Workflows**

- `CDAP-1983 <https://issues.cask.co/browse/CDAP-1983>`__ -
  Added example application for processing and analyzing Wikipedia data using Workflows.

- `CDAP-2709 <https://issues.cask.co/browse/CDAP-2709>`__ -
  Added ability to add generic keys to the WorkflowToken.

- `CDAP-2712 <https://issues.cask.co/browse/CDAP-2712>`__ -
  Added ability to update the WorkflowToken in MapReduce and Spark programs.

- `CDAP-2713 <https://issues.cask.co/browse/CDAP-2713>`__ -
  Added ability to persist the WorkflowToken per run of the Workflow.

- `CDAP-2714 <https://issues.cask.co/browse/CDAP-2714>`__ -
  Added ability to query the WorkflowToken for the past as well as currently running Workflow runs.

- `CDAP-2752 <https://issues.cask.co/browse/CDAP-2752>`__ -
  Added ability for custom actions to access the CDAP datasets and services.

- `CDAP-2894 <https://issues.cask.co/browse/CDAP-2894>`__ -
  Added an API to retreive the system properties (e.g. MapReduce counters in case of
  MapReduce program) from the WorkflowToken.

- `CDAP-2923 <https://issues.cask.co/browse/CDAP-2923>`__ -
  Added support for the WorkflowToken in the Spark programs.

- `CDAP-2982 <https://issues.cask.co/browse/CDAP-2982>`__ -
  Added verification that the Workflow contains all programs/custom actions with a unique name.


**Datasets**

- `CDAP-347 <https://issues.cask.co/browse/CDAP-347>`__ -
  User can use datasets in beforeSubmit and afterFinish.

- `CDAP-585 <https://issues.cask.co/browse/CDAP-585>`__ -
  Changes to Spark program runner to use File dataset in Spark.
  Spark programs can now use file-based datasets.

- `CDAP-2734 <https://issues.cask.co/browse/CDAP-2734>`__ -
  Added PartitionedFileSet support to setting/getting properties at the Partition level.

- `CDAP-2746 <https://issues.cask.co/browse/CDAP-2746>`__ -
  PartitionedFileSets now record the creation time of each partition in the metadata.

- `CDAP-2747 <https://issues.cask.co/browse/CDAP-2747>`__ -
  PartitionedFileSets now index the creation time of partitions to allow selection of
  partitions that were created after a given time. Introduced BatchPartitionConsumer as a
  way to incrementally consume new data in a PartitionedFileSet.

- `CDAP-2752 <https://issues.cask.co/browse/CDAP-2752>`__ -
  Added ability for custom actions to access the CDAP datasets and services.

- `CDAP-2758 <https://issues.cask.co/browse/CDAP-2758>`__ -
  FileSet now support existing HDFS locations. 
  
  Treat base paths that start with "/" as absolute in the file system. An absolute base
  path for a (Partitioned)FileSet was interpreted as relative to the namespace's data
  directory. Newly created FileSets interpret absolute base paths as absolute in the file
  system.
  
  Introduced a new property for (Partitioned)FileSets name "data.external". If true, the
  base path of the FileSet is assumed to be managed by some external process. That is, the
  FileSet will not attempt to create the directory, it will not delete any files when the
  FileSet is dropped or truncated, and it will not allow adding or deleting files or
  partitions. In other words, the FileSet is read-only. 
  
- `CDAP-2784 <https://issues.cask.co/browse/CDAP-2784>`__ -
  Added support to write to PartitionedFileSet Partition metadata from MapReduce.

- `CDAP-2822 <https://issues.cask.co/browse/CDAP-2822>`__ -
  IndexedTable now supports scans on the indexed field.


**Metrics**

- `CDAP-2975 <https://issues.cask.co/browse/CDAP-2975>`__ -
  Added pre-split FactTables.

- `CDAP-2326 <https://issues.cask.co/browse/CDAP-2326>`__ -
  Added better unit-test coverage for Cube dataset.

- `CDAP-1853 <https://issues.cask.co/browse/CDAP-1853>`__ -
  Metrics processor scaling no longer needs a master services restart.

- `CDAP-2844 <https://issues.cask.co/browse/CDAP-2844>`__ -
  MapReduce metrics collection no longer use counters, and instead report directly to Kafka.

- `CDAP-2701 <https://issues.cask.co/browse/CDAP-2701>`__ -
  Spark programs are able to collect Metrics in distributed mode.

- `CDAP-2466 <https://issues.cask.co/browse/CDAP-2466>`__ -
  Added CLI for metrics search and query.

- `CDAP-2236 <https://issues.cask.co/browse/CDAP-2236>`__ -
  New CDAP UI switched over to using newer search/query APIs.

- `CDAP-1998 <https://issues.cask.co/browse/CDAP-1998>`__ -
  Removed deprecated Context - Query param in Metrics v3 API.


**Miscellaneous New Features**

- `CDAP-332 <https://issues.cask.co/browse/CDAP-332>`__ -
  Added a Restful end-point for deleting Streams.

- `CDAP-1483 <https://issues.cask.co/browse/CDAP-1483>`__ -
  QueueAdmin now uses Id.Namespace instead of simply String.

- `CDAP-1584 <https://issues.cask.co/browse/CDAP-1584>`__ -
  CDAP CLI now shows the username in the CLI prompt.

- `CDAP-2139 <https://issues.cask.co/browse/CDAP-2139>`__ -
  Removed a duplicate Table of Contents on the Documentation Search page.

- `CDAP-2515 <https://issues.cask.co/browse/CDAP-2515>`__ -
  Added a metrics client for search and query by tags.

- `CDAP-2582 <https://issues.cask.co/browse/CDAP-2582>`__ -
  Documented the licenses of the shipped CDAP-UI components.

- `CDAP-2595 <https://issues.cask.co/browse/CDAP-2595>`__ -
  Added data modelling of flows.

- `CDAP-2596 <https://issues.cask.co/browse/CDAP-2596>`__ -
  Added data modelling of MapReduce.

- `CDAP-2617 <https://issues.cask.co/browse/CDAP-2617>`__ -
  Added the capability to get logs for a given time range from CLI.

- `CDAP-2618 <https://issues.cask.co/browse/CDAP-2618>`__ -
  Simplified the Cube sink configurations.

- `CDAP-2670 <https://issues.cask.co/browse/CDAP-2670>`__ -
  Added Parquet sink with time partitioned file dataset.

- `CDAP-2739 <https://issues.cask.co/browse/CDAP-2739>`__ -
  Added S3 batch source for ETLbatch.

- `CDAP-2802 <https://issues.cask.co/browse/CDAP-2802>`__ -
  Stopped using HiveConf.ConfVars.defaultValue, to support Hive >0.13.

- `CDAP-2847 <https://issues.cask.co/browse/CDAP-2847>`__ -
  Added ability to add custom filters to FileBatchSource.

- `CDAP-2893 <https://issues.cask.co/browse/CDAP-2893>`__ -
  Custom Transform now parses log formats for ETL.

- `CDAP-2913 <https://issues.cask.co/browse/CDAP-2913>`__ -
  Provided installation method for EMR.

- `CDAP-2915 <https://issues.cask.co/browse/CDAP-2915>`__ -
  Added an SQS real-time plugin for ETL.

- `CDAP-3022 <https://issues.cask.co/browse/CDAP-3022>`__ -
  Added Cloudfront format option to LogParserTransform.

- `CDAP-3032 <https://issues.cask.co/browse/CDAP-3032>`__ -
  Documented TestConfiguration class usage in unit-test framework.


Improvements
------------

- `CDAP-593 <https://issues.cask.co/browse/CDAP-593>`__ -
  Spark no longer determines the mode through MRConfig.FRAMEWORK_NAME.

- `CDAP-595 <https://issues.cask.co/browse/CDAP-595>`__ -
  Refactored SparkRuntimeService and SparkProgramWrapper.

- `CDAP-665 <https://issues.cask.co/browse/CDAP-665>`__ -
  Documentation received a product-specifc 404 Page.

- `CDAP-683 <https://issues.cask.co/browse/CDAP-683>`__ -
  Changed all README files from markdown to rst format.

- `CDAP-1132 <https://issues.cask.co/browse/CDAP-1132>`__ -
  Improved the CDAP Doc Search Result Sorting.

- `CDAP-1416 <https://issues.cask.co/browse/CDAP-1416>`__ -
  Added links to upper level pages on Docs.

- `CDAP-1572 <https://issues.cask.co/browse/CDAP-1572>`__ -
  Standardized Id classes.

- `CDAP-1583 <https://issues.cask.co/browse/CDAP-1583>`__ -
  Refactored InMemoryWorkerRunner and ServiceProgramRunnner after ServiceWorkers were removed.

- `CDAP-1918 <https://issues.cask.co/browse/CDAP-1918>`__ -
  Switched to using the Spark 1.3.0 release.

- `CDAP-1926 <https://issues.cask.co/browse/CDAP-1926>`__ -
  Streams endpoint accept "now", "now-30s", etc., for time ranges.

- `CDAP-2007 <https://issues.cask.co/browse/CDAP-2007>`__ -
  CLI output for "call service" is rendered in a copy-pastable manner.

- `CDAP-2310 <https://issues.cask.co/browse/CDAP-2310>`__ -
  Kafka Source now able to apply a Schema to the Payload received.

- `CDAP-2388 <https://issues.cask.co/browse/CDAP-2388>`__ -
  Added Java 8 support to CDAP.

- `CDAP-2422 <https://issues.cask.co/browse/CDAP-2422>`__ -
  Removed redundant catch blocks in AdapterHttpHandler.

- `CDAP-2455 <https://issues.cask.co/browse/CDAP-2455>`__ -
  Version in CDAP-UI footer is dynamic.

- `CDAP-2482 <https://issues.cask.co/browse/CDAP-2482>`__ -
  Reduced excessive capitalisation in documentation.

- `CDAP-2531 <https://issues.cask.co/browse/CDAP-2531>`__ -
  Adapter details made available through CDAP-UI.

- `CDAP-2539 <https://issues.cask.co/browse/CDAP-2539>`__ -
  Added a build identifier (branch, commit) in header of Documentation HTML pages.

- `CDAP-2552 <https://issues.cask.co/browse/CDAP-2552>`__ -
  Documentation Build script now flags errors.

- `CDAP-2554 <https://issues.cask.co/browse/CDAP-2554>`__ -
  Documented that streams can now be deleted.

- `CDAP-2557 <https://issues.cask.co/browse/CDAP-2557>`__ -
  Non-handler logic moved out of DatasetInstanceHandler.

- `CDAP-2570 <https://issues.cask.co/browse/CDAP-2570>`__ -
  CLI prompt changes to 'DISCONNECTED' after CDAP is stopped.

- `CDAP-2578 <https://issues.cask.co/browse/CDAP-2578>`__ -
  Ability to look at configs of created adapters.

- `CDAP-2585 <https://issues.cask.co/browse/CDAP-2585>`__ -
  Use Id in cdap-client rather than Id.Namespace + String.

- `CDAP-2588 <https://issues.cask.co/browse/CDAP-2588>`__ -
  Improvements to the MetricsClient APIs.

- `CDAP-2590 <https://issues.cask.co/browse/CDAP-2590>`__ -
  Switching namespaces when in CDAP-UI Operations screens.

- `CDAP-2620 <https://issues.cask.co/browse/CDAP-2620>`__ -
  CDAP clients now use Id classes from cdap proto, instead of plain strings.

- `CDAP-2628 <https://issues.cask.co/browse/CDAP-2628>`__ -
  CDAP-UI: Breadcrumbs in Workflow/Mapreduce work as expected.

- `CDAP-2644 <https://issues.cask.co/browse/CDAP-2644>`__ -
  In cdap-clients, no longer need to retrieve runtime arguments before starting a program.

- `CDAP-2651 <https://issues.cask.co/browse/CDAP-2651>`__ -
  CDAP-UI: the Namespace is made more prominent.

- `CDAP-2681 <https://issues.cask.co/browse/CDAP-2681>`__ -
  CDAP-UI: scrolling no longer enlarges the workflow diagram instead of scrolling through.

- `CDAP-2683 <https://issues.cask.co/browse/CDAP-2683>`__ -
  CDAP-UI: added a remove icons for fork and Join.

- `CDAP-2684 <https://issues.cask.co/browse/CDAP-2684>`__ -
  CDAP-UI: workflow diagrams are directed graphs.

- `CDAP-2688 <https://issues.cask.co/browse/CDAP-2688>`__ -
  CDAP-UI: added search & pagination for lists of apps and datasets.

- `CDAP-2689 <https://issues.cask.co/browse/CDAP-2689>`__ -
  CDAP-UI: shows which application is a part of which dataset.

- `CDAP-2691 <https://issues.cask.co/browse/CDAP-2691>`__ -
  CDAP-UI: added ability to delete streams.

- `CDAP-2692 <https://issues.cask.co/browse/CDAP-2692>`__ -
  CDAP-UI: added pagination for logs.

- `CDAP-2694 <https://issues.cask.co/browse/CDAP-2694>`__ -
  CDAP-UI: added a loading icon/UI element when creating an adapter.

- `CDAP-2695 <https://issues.cask.co/browse/CDAP-2695>`__ -
  CDAP-UI: long names of adapters are replaced by a short version ending in an ellipsis.

- `CDAP-2697 <https://issues.cask.co/browse/CDAP-2697>`__ -
  CDAP-UI: added tab names during adapter creation.

- `CDAP-2716 <https://issues.cask.co/browse/CDAP-2716>`__ -
  CDAP-UI: when creating an adapter, the tabbing order shows correctly.

- `CDAP-2733 <https://issues.cask.co/browse/CDAP-2733>`__ -
  Implemented a TimeParitionedFileSet source.

- `CDAP-2811 <https://issues.cask.co/browse/CDAP-2811>`__ -
  Improved Hive version detection.

- `CDAP-2921 <https://issues.cask.co/browse/CDAP-2921>`__ -
  Removed backward-compatibility for pre-2.8 TPFS.

- `CDAP-2938 <https://issues.cask.co/browse/CDAP-2938>`__ -
  Implemented new ETL application template creation.

- `CDAP-2983 <https://issues.cask.co/browse/CDAP-2983>`__ -
  Spark program runner now calls onFailure() of the DatasetOutputCommitter.

- `CDAP-2986 <https://issues.cask.co/browse/CDAP-2986>`__ -
  Spark program now are able to specify runtime arguments when reading or writing a datset.

- `CDAP-2987 <https://issues.cask.co/browse/CDAP-2987>`__ -
  Added an example for Spark using datasets directly.

- `CDAP-2989 <https://issues.cask.co/browse/CDAP-2989>`__ -
  Added an example for Spark using FileSets.

- `CDAP-3018 <https://issues.cask.co/browse/CDAP-3018>`__ -
  Updated workflow guides for workflow token.

- `CDAP-3028 <https://issues.cask.co/browse/CDAP-3028>`__ -
  Improved the system service restart endpoint to handle illegal instance IDs and "service not available".

- `CDAP-3053 <https://issues.cask.co/browse/CDAP-3053>`__ -
  Added schema javadocs that explain how to write the schema to JSON.

- `CDAP-3077 <https://issues.cask.co/browse/CDAP-3077>`__ -
  Add the ability in TableSink to find schema.row.field case-insensitively.

- `CDAP-3144 <https://issues.cask.co/browse/CDAP-3144>`__ -
  Changed CLI command descriptions to use consistent element case.

- `CDAP-3152 <https://issues.cask.co/browse/CDAP-3152>`__ -
  Refactored ETLBatch sources and sinks.
  
Bug Fixes
---------

- `CDAP-23 <https://issues.cask.co/browse/CDAP-23>`__ -
  Fixed a problem with the DatasetFramework not loading a given dataset with the same classloader across calls.

- `CDAP-68 <https://issues.cask.co/browse/CDAP-68>`__ -
  Made sure all network services in Singlenode only bind to localhost.

- `CDAP-376 <https://issues.cask.co/browse/CDAP-376>`__ -
  Fixed a problem with HBaseOrderedTable never calling HTable.close().

- `CDAP-550 <https://issues.cask.co/browse/CDAP-550>`__ -
  Consolidated Examples, Guides, and Tutorials styles.

- `CDAP-598 <https://issues.cask.co/browse/CDAP-598>`__ -
  Fixed problems with the CDAP ClassLoading model.

- `CDAP-674 <https://issues.cask.co/browse/CDAP-674>`__ -
  Fixed problems with CDAP code examples and versioning.

- `CDAP-814 <https://issues.cask.co/browse/CDAP-814>`__ -
  Resolved issues in the documentation about element versus program.

- `CDAP-1042 <https://issues.cask.co/browse/CDAP-1042>`__ -
  Fixed a problem with specifying dataset selection as input for Spark job.

- `CDAP-1145 <https://issues.cask.co/browse/CDAP-1145>`__ -
  Fixed the PurchaseAppTest.

- `CDAP-1184 <https://issues.cask.co/browse/CDAP-1184>`__ -
  Fixed a problem with the DELETE call not clearing queue metrics.

- `CDAP-1273 <https://issues.cask.co/browse/CDAP-1273>`__ -
  Fixed a problem with the ProgramClassLoader getResource.

- `CDAP-1457 <https://issues.cask.co/browse/CDAP-1457>`__ -
  Fixed a memory leak of user class after running Spark program.

- `CDAP-1552 <https://issues.cask.co/browse/CDAP-1552>`__ -
  Fixed a problem with Mapreduce progress metrics not being interpolated.

- `CDAP-1868 <https://issues.cask.co/browse/CDAP-1868>`__ -
  Fixed a problem with Java Client and CLI not setting set dataset properties on existing datasets.

- `CDAP-1873 <https://issues.cask.co/browse/CDAP-1873>`__ -
  Fixed a problem with warnings and errors when CDAP-Master starts up.

- `CDAP-1967 <https://issues.cask.co/browse/CDAP-1967>`__ -
  Fixed a problem with CDAP-Master failing to start up due to conflicting dependencies.

- `CDAP-1976 <https://issues.cask.co/browse/CDAP-1976>`__ -
  Fixed a problem with examples not following the same pattern.

- `CDAP-1988 <https://issues.cask.co/browse/CDAP-1988>`__ -
  Fixed a problem with creating a Dataset through REST API failing if no properties are provided.

- `CDAP-2081 <https://issues.cask.co/browse/CDAP-2081>`__ -
  Fixed a problem with StreamSizeSchedulerTest failing randomly.

- `CDAP-2140 <https://issues.cask.co/browse/CDAP-2140>`__ -
  Fixed a problem with the CDAP-UI not showing system service status when system services are down.

- `CDAP-2177 <https://issues.cask.co/browse/CDAP-2177>`__ -
  Fixed a problem with Enable and Fix LogSaverPluginTest.

- `CDAP-2208 <https://issues.cask.co/browse/CDAP-2208>`__ -
  Fixed a problem with CDAP-Explore service failing on wrapped indexedTable with Avro (specific record) contents.

- `CDAP-2228 <https://issues.cask.co/browse/CDAP-2228>`__ -
  Fixed a problem with Mapreduce not working in Hadoop 2.2.

- `CDAP-2254 <https://issues.cask.co/browse/CDAP-2254>`__ -
  Fixed a problem with an incorrect error message returned by HTTP RESTful Handler.

- `CDAP-2258 <https://issues.cask.co/browse/CDAP-2258>`__ -
  Fixed a problem with an internal error when attempting to start a non-existing program.

- `CDAP-2279 <https://issues.cask.co/browse/CDAP-2279>`__ -
  Fixed a problem with namespace and gear widgets disappearing when the browser window is too narrow.

- `CDAP-2280 <https://issues.cask.co/browse/CDAP-2280>`__ -
  Fixed a problem when starting a flow from the GUI that the GUI does not fully refresh the page.

- `CDAP-2341 <https://issues.cask.co/browse/CDAP-2341>`__ -
  Fixed a problem that when a MapReduce fails to start, it cannot be started or stopped any more.

- `CDAP-2343 <https://issues.cask.co/browse/CDAP-2343>`__ -
  Fixed a problem in the CDAP-UI that Mapreduce logs are convoluted with system logs.

- `CDAP-2344 <https://issues.cask.co/browse/CDAP-2344>`__ -
  Fixed a problem with the formatting of logs in the CDAP-UI.

- `CDAP-2355 <https://issues.cask.co/browse/CDAP-2355>`__ -
  Fixed a problem with an Adapter CLI help error.

- `CDAP-2356 <https://issues.cask.co/browse/CDAP-2356>`__ -
  Fixed a problem with CLI autocompletion results not sorted in alphabetical order.

- `CDAP-2365 <https://issues.cask.co/browse/CDAP-2365>`__ -
  Fixed a problem that when restarting CDAP-Master, the CDAP-UI oscillates between being up and down.

- `CDAP-2376 <https://issues.cask.co/browse/CDAP-2376>`__ -
  Fixed a problem with logs from mapper and reducer not being collected.

- `CDAP-2444 <https://issues.cask.co/browse/CDAP-2444>`__ -
  Fixed a problem with Cloudera Configuring doc needs fixing.

- `CDAP-2446 <https://issues.cask.co/browse/CDAP-2446>`__ -
  Fixed a problem with that examples needing to be updated for new CDAP-UI.

- `CDAP-2454 <https://issues.cask.co/browse/CDAP-2454>`__ -
  Fixed a problem with Proto class RunRecord containing the Twill RunId when serialized in REST API response.

- `CDAP-2459 <https://issues.cask.co/browse/CDAP-2459>`__ -
  Fixed a problem with the CDAP-UI going into a loop when the Router returns 200 and App Fabric is not up.

- `CDAP-2474 <https://issues.cask.co/browse/CDAP-2474>`__ -
  Fixed a problem with changing the format of the name for the connectionfactory in JMS source plugin.

- `CDAP-2475 <https://issues.cask.co/browse/CDAP-2475>`__ -
  Fixed a problem with JMS source accepting the type and name of the JMS provider plugin.

- `CDAP-2480 <https://issues.cask.co/browse/CDAP-2480>`__ -
  Fixed a problem with the Workflow current run info endpoint missing a /runs/ in the path.

- `CDAP-2489 <https://issues.cask.co/browse/CDAP-2489>`__ -
  Fixed a problem when, in distributed mode and CDAP master restarted, status of the running PROGRAM is always returned as STOPPED.

- `CDAP-2490 <https://issues.cask.co/browse/CDAP-2490>`__ -
  Fixed a problem with checking if invalid Run Records for Spark and MapReduce are part of run from Workflow child programs.

- `CDAP-2491 <https://issues.cask.co/browse/CDAP-2491>`__ -
  Fixed a problem with the MapReduce program in standalone mode not always using LocalJobRunnerWithFix.

- `CDAP-2493 <https://issues.cask.co/browse/CDAP-2493>`__ -
  After the fix for `CDAP-2474 <https://issues.cask.co/browse/CDAP-2474>`__ (ConnectionFactory in JMS source), 
  the JSON file requires updating for the change to reflect in CDAP-UI.

- `CDAP-2496 <https://issues.cask.co/browse/CDAP-2496>`__ -
  Fixed a problem with CDAP using its own transaction snapshot codec.

- `CDAP-2498 <https://issues.cask.co/browse/CDAP-2498>`__ -
  Fixed a problem with validation while creating adapters only by types and not also by values.

- `CDAP-2517 <https://issues.cask.co/browse/CDAP-2517>`__ -
  Fixed a problem with Explore docs not mentioning partitioned file sets.

- `CDAP-2520 <https://issues.cask.co/browse/CDAP-2520>`__ -
  Fixed a problem with StreamSource not liking values of '0m'.

- `CDAP-2522 <https://issues.cask.co/browse/CDAP-2522>`__ -
  Fixed a problem with TransactionStateCache needing to reference Tephra SnapshotCodecV3.

- `CDAP-2529 <https://issues.cask.co/browse/CDAP-2529>`__ -
  Fixed a problem with CLI not printing an error if it can't connect to CDAP.

- `CDAP-2530 <https://issues.cask.co/browse/CDAP-2530>`__ -
  Fixed a problem with Custom RecordScannable<StructuredRecord> datasets not be explorable.

- `CDAP-2535 <https://issues.cask.co/browse/CDAP-2535>`__ -
  Fixed a problem with IntegrationTestManager deployApplication not being namespaced.

- `CDAP-2538 <https://issues.cask.co/browse/CDAP-2538>`__ -
  Fixed a problem with handling extra whitespace in CLI input.

- `CDAP-2540 <https://issues.cask.co/browse/CDAP-2540>`__ -
  Fixed a problem with the Preferences Namespace CLI help having errors.

- `CDAP-2541 <https://issues.cask.co/browse/CDAP-2541>`__ -
  Added the ability to stop the particular run of a program. Allows concurrent runs of the
  MapReduce and Workflow programs and the ability to stop programs at a per-run level.

- `CDAP-2547 <https://issues.cask.co/browse/CDAP-2547>`__ -
  Fixed a problem with Kakfa Source - not using the persisted offset when the Adapter is restarted.

- `CDAP-2549 <https://issues.cask.co/browse/CDAP-2549>`__ -
  Fixed a problem with a suspended workflow run record not being removed upon app/namespace delete.

- `CDAP-2562 <https://issues.cask.co/browse/CDAP-2562>`__ -
  Fixed a problem with the automated Doc Build failing in develop.

- `CDAP-2564 <https://issues.cask.co/browse/CDAP-2564>`__ -
  Improved the management of dataset resources.

- `CDAP-2565 <https://issues.cask.co/browse/CDAP-2565>`__ -
  Fixed a problem with the transaction latency metric being of incorrect type.

- `CDAP-2569 <https://issues.cask.co/browse/CDAP-2569>`__ -
  Fixed a problem with master process not being resilient to zookeeper exceptions.

- `CDAP-2571 <https://issues.cask.co/browse/CDAP-2571>`__ -
  Fixed a problem with the RunRecord thread not resilient to errors.

- `CDAP-2587 <https://issues.cask.co/browse/CDAP-2587>`__ -
  Fixed a problem with being unable to create default namespaces on starting up SDK.

- `CDAP-2635 <https://issues.cask.co/browse/CDAP-2635>`__ -
  Fixed a problem with Namespace Create ignoring the properties' config field.

- `CDAP-2636 <https://issues.cask.co/browse/CDAP-2636>`__ -
  Fixed a problem with "out of perm gen" space in CDAP Explore service.

- `CDAP-2654 <https://issues.cask.co/browse/CDAP-2654>`__ -
  Fixed a problem with False values showing up as 'false null' in the CDAP Explore UI.

- `CDAP-2685 <https://issues.cask.co/browse/CDAP-2685>`__ -
  Fixed a problem with the CDAP-UI: no empty box for transforms.

- `CDAP-2729 <https://issues.cask.co/browse/CDAP-2729>`__ -
  Fixed a problem with CDAP-UI not handling downstream system services gracefully.

- `CDAP-2740 <https://issues.cask.co/browse/CDAP-2740>`__ -
  Fixed a problem with CDAP-UI not gracefully handling when the nodejs server goes down.

- `CDAP-2748 <https://issues.cask.co/browse/CDAP-2748>`__ -
  Fixed a problem with the currently running and completed status of Spark programs in a 
  workflow not highlighted in the UI.

- `CDAP-2765 <https://issues.cask.co/browse/CDAP-2765>`__ -
  Fixed a problem with security warnings when CLI starts up.

- `CDAP-2766 <https://issues.cask.co/browse/CDAP-2766>`__ -
  Fixed a problem with CLI asking for the user/password twice.

- `CDAP-2767 <https://issues.cask.co/browse/CDAP-2767>`__ -
  Fixed a problem with incorrect error messages for namespace deletion.

- `CDAP-2768 <https://issues.cask.co/browse/CDAP-2768>`__ -
  Fixed a problem with CLI and UI listing system.queue as a dataset.

- `CDAP-2769 <https://issues.cask.co/browse/CDAP-2769>`__ -
  Fixed a problem with Use co.cask.cdap.common.app.RunIds instead of 
  org.apache.twill.internal.RunIds for InMemoryServiceProgramRunner.

- `CDAP-2787 <https://issues.cask.co/browse/CDAP-2787>`__ -
  Fixed a problem when the number of MapReduce task metrics going over limit and causing MapReduce to fail.

- `CDAP-2796 <https://issues.cask.co/browse/CDAP-2796>`__ -
  Fixed a problem with emitting duplicate metrics for dataset ops.

- `CDAP-2803 <https://issues.cask.co/browse/CDAP-2803>`__ -
  Fixed a problem with scan operations not reflecting in dataset ops metrics.

- `CDAP-2804 <https://issues.cask.co/browse/CDAP-2804>`__ -
  Fixed a problem with DataSetRecordReader incorrectly reporting dataset ops metrics.

- `CDAP-2810 <https://issues.cask.co/browse/CDAP-2810>`__ -
  Fixed a problem with IncrementAndGet, CompareAndSwap, and Delete ops on Table incorrectly
  reporting two writes each.

- `CDAP-2821 <https://issues.cask.co/browse/CDAP-2821>`__ -
  Fixed a problem with a Spark native library linkage error causing CDAP standalone to stop.

- `CDAP-2823 <https://issues.cask.co/browse/CDAP-2823>`__ -
  Fixed a problem with the conversion from Avro and to Avro not taking into account nested records.

- `CDAP-2830 <https://issues.cask.co/browse/CDAP-2830>`__ -
  Fixed a problem with CDAP-UI dying when CDAP Master is killed.

- `CDAP-2832 <https://issues.cask.co/browse/CDAP-2832>`__ -
  Fixed a problem where suspending a schedule takes a long time and the CDAP-UI does not provide any indication.

- `CDAP-2838 <https://issues.cask.co/browse/CDAP-2838>`__ -
  Fixed a problem with poor error message when there is a mistake in security configration.

- `CDAP-2839 <https://issues.cask.co/browse/CDAP-2839>`__ -
  Fixed a problem with the CDAP start script needing updating for the correct Node.js version.

- `CDAP-2848 <https://issues.cask.co/browse/CDAP-2848>`__ -
  Fixed a problem with the Preferences Client test.

- `CDAP-2849 <https://issues.cask.co/browse/CDAP-2849>`__ -
  Fixed a problem with the FileBatchSource reading files in twice if it takes longer that 
  one workflow cycle to complete the job.

- `CDAP-2851 <https://issues.cask.co/browse/CDAP-2851>`__ -
  Fixed a problem with RPM and DEB release artifacts being uploaded to incorrect staging directory.

- `CDAP-2854 <https://issues.cask.co/browse/CDAP-2854>`__ -
  Fixed a problem with the instructions for using Docker.

- `CDAP-2855 <https://issues.cask.co/browse/CDAP-2855>`__ -
  Fixed a problem with the example builds in VM failing with a Maven dependency error.

- `CDAP-2860 <https://issues.cask.co/browse/CDAP-2860>`__ -
  Fixed a problem with the documentation for updating dataset properties.

- `CDAP-2861 <https://issues.cask.co/browse/CDAP-2861>`__ -
  Fixed a problem with CDAP-UI not mentioning required fields in all entry forms.

- `CDAP-2862 <https://issues.cask.co/browse/CDAP-2862>`__ -
  Fixed a problem with CDAP-UI creating multiple namespaces with the same name.

- `CDAP-2866 <https://issues.cask.co/browse/CDAP-2866>`__ -
  Fixed a problem with FileBatchSource not reattempting to read in files if there is a failure.

- `CDAP-2870 <https://issues.cask.co/browse/CDAP-2870>`__ -
  Fixed a problem with Workflow Diagrams.

- `CDAP-2871 <https://issues.cask.co/browse/CDAP-2871>`__ -
  Fixed a problem with the Cloudera Manager Hbase Gateway dependency.

- `CDAP-2895 <https://issues.cask.co/browse/CDAP-2895>`__ -
  Fixed a problem with a put operation on the WorkflowToken not throwing an exception.

- `CDAP-2899 <https://issues.cask.co/browse/CDAP-2899>`__ -
  Fixed a problem with Mapreduce local dirs not getting cleaned up.

- `CDAP-2900 <https://issues.cask.co/browse/CDAP-2900>`__ -
  Fixed a problem with exposing app.template.dir as a config option.

- `CDAP-2904 <https://issues.cask.co/browse/CDAP-2904>`__ -
  Fixed a problem with "Make Request" button overlapping with paths when a path is long.

- `CDAP-2912 <https://issues.cask.co/browse/CDAP-2912>`__ -
  Fixed a problem with HBaseQueueDebugger not sorting queue barriers correctly.

- `CDAP-2922 <https://issues.cask.co/browse/CDAP-2922>`__ -
  Fixed a problem with datasets created through DynamicDatasetContext not having metrics context.
  Datasets in MapReduce and Spark programs, and workers, were not emitting metrics.

- `CDAP-2925 <https://issues.cask.co/browse/CDAP-2925>`__ -
  Fixed a problem with the documentation on how to create datasets with properties.

- `CDAP-2932 <https://issues.cask.co/browse/CDAP-2932>`__ -
  Fixed a problem with the AdapterClient getRuns method constructing a malformed URL.

- `CDAP-2935 <https://issues.cask.co/browse/CDAP-2935>`__ -
  Fixed a problem with the logs endpoint to retrieve the latest entry not working correctly.

- `CDAP-2940 <https://issues.cask.co/browse/CDAP-2940>`__ -
  Fixed a problem with the test case ArtifactStoreTest#testConcurrentSnapshotWrite.

- `CDAP-2941 <https://issues.cask.co/browse/CDAP-2941>`__ -
  Fixed a problem with the ScriptTransform failing to initialize.

- `CDAP-2942 <https://issues.cask.co/browse/CDAP-2942>`__ -
  Fixed a problem with the CDAP-UI namespace dropdown failing on standalone restart.

- `CDAP-2948 <https://issues.cask.co/browse/CDAP-2948>`__ -
  Fixed a problem with creating Adapters.

- `CDAP-2952 <https://issues.cask.co/browse/CDAP-2952>`__ -
  Fixed a problem with the plugin avro library not being accessible to MapReduce.

- `CDAP-2955 <https://issues.cask.co/browse/CDAP-2955>`__ -
  Fixed a problem with a NoSuchMethodException when trying to explore Datasets/Stream.

- `CDAP-2971 <https://issues.cask.co/browse/CDAP-2971>`__ -
  Fixed a problem with the dataset registration not registering datasets for applications upon deploy.

- `CDAP-2972 <https://issues.cask.co/browse/CDAP-2972>`__ -
  Fixed a problem with being unable to instantiate dataset in ETLWorker initialization.

- `CDAP-2981 <https://issues.cask.co/browse/CDAP-2981>`__ -
  Fixed a problem with undoing a FileSets upgrade in favor of versioning and backward-compatibility.

- `CDAP-2991 <https://issues.cask.co/browse/CDAP-2991>`__ -
  Fixed a problem with Explore not working when it launches a MapReduce job.

- `CDAP-2992 <https://issues.cask.co/browse/CDAP-2992>`__ -
  Fixed a problem with CLI broken for secure CDAP.

- `CDAP-2996 <https://issues.cask.co/browse/CDAP-2996>`__ -
  Fixed a problem with CDAP-UI: Stop Run and Suspend Run buttons needed styling updates.

- `CDAP-2997 <https://issues.cask.co/browse/CDAP-2997>`__ -
  Fixed a problem with SparkProgramRunnerTest failing randomly.

- `CDAP-2999 <https://issues.cask.co/browse/CDAP-2999>`__ -
  Fixed a problem with MapReduce jobs showing the duration for tasks as 17 days before the mapper starts.

- `CDAP-3001 <https://issues.cask.co/browse/CDAP-3001>`__ -
  Fixed a problem with truncating a custom dataset failing with internal server error.

- `CDAP-3002 <https://issues.cask.co/browse/CDAP-3002>`__ -
  Fixed a problem with tick initialDelay not working properly.

- `CDAP-3003 <https://issues.cask.co/browse/CDAP-3003>`__ -
  Fixed a problem with user metrics emitted from flowlets not being queryable using the flow's tags.

- `CDAP-3006 <https://issues.cask.co/browse/CDAP-3006>`__ -
  Fixed a problem with updating cdap-spark-* archetypes.

- `CDAP-3007 <https://issues.cask.co/browse/CDAP-3007>`__ -
  Fixed a problem with testing all Spark apps/guides to work with 3.1 (in dist mode).

- `CDAP-3009 <https://issues.cask.co/browse/CDAP-3009>`__ -
  Fixed a problem with the stream conversion upgrade being in the upgrade tool in 3.1.

- `CDAP-3010 <https://issues.cask.co/browse/CDAP-3010>`__ -
  Fixed a problem with a Bower Dependency Error.

- `CDAP-3011 <https://issues.cask.co/browse/CDAP-3011>`__ -
  Fixed a problem with the IncrementSummingScannerTest failing intermittently.

- `CDAP-3012 <https://issues.cask.co/browse/CDAP-3012>`__ -
  Fixed a problem with the DistributedWorkflowProgramRunner localizing the
  spark-assembly.jar even if the workflow does not contain a Spark program.

- `CDAP-3013 <https://issues.cask.co/browse/CDAP-3013>`__ -
  Fixed a problem with excluding a Spark assembly jar when building a MapReduce job jar.

- `CDAP-3019 <https://issues.cask.co/browse/CDAP-3019>`__ -
  Fixed a problem with the PartitionedFileSet dropPartition not deleting files under the partition.

- `CDAP-3021 <https://issues.cask.co/browse/CDAP-3021>`__ -
  Fixed a problem with allowing Cloudfront data to use BatchFileFilter.

- `CDAP-3023 <https://issues.cask.co/browse/CDAP-3023>`__ -
  Fixed a problem with flowlet instance count defaulting to 1.

- `CDAP-3024 <https://issues.cask.co/browse/CDAP-3024>`__ -
  Fixed a problem with surfacing more logs in CDAP-UI for System Services.

- `CDAP-3026 <https://issues.cask.co/browse/CDAP-3026>`__ -
  Fixed a problem with updating SparkPageRank example docs.

- `CDAP-3027 <https://issues.cask.co/browse/CDAP-3027>`__ -
  Fixed a problem with the DFSStreamHeartbeatsTest failing on clusters.

- `CDAP-3030 <https://issues.cask.co/browse/CDAP-3030>`__ -
  Fixed a problem with the loading of custom datasets being broken after upgrading.

- `CDAP-3031 <https://issues.cask.co/browse/CDAP-3031>`__ -
  Fixed a problem with deploying an app with a dataset with an invalid base path returning an "internal error".

- `CDAP-3037 <https://issues.cask.co/browse/CDAP-3037>`__ -
  Fixed a problem with not being able to use a PartitionedFileSet in a custom dataset. If
  a custom dataset embedded a Table and a PartitionedFileSet, loading the dataset at
  runtime would fail. 

- `CDAP-3038 <https://issues.cask.co/browse/CDAP-3038>`__ -
  Fixed a problem with logs not showing up in UI when using Spark.

- `CDAP-3039 <https://issues.cask.co/browse/CDAP-3039>`__ -
  Fixed a problem with worker not stopping at the end of a run method in standalone.

- `CDAP-3040 <https://issues.cask.co/browse/CDAP-3040>`__ -
  Fixed a problem with flowlet and stream metrics not being available in distributed mode.

- `CDAP-3042 <https://issues.cask.co/browse/CDAP-3042>`__ -
  Fixed a problem with the BufferingTable not merging buffered writes with multi-get results.

- `CDAP-3043 <https://issues.cask.co/browse/CDAP-3043>`__ -
  Fixed a problem with the Javadocs being broken.

- `CDAP-3044 <https://issues.cask.co/browse/CDAP-3044>`__ -
  Fixed a problem with the user service 'methods' field in service specifications being inaccurate.

- `CDAP-3058 <https://issues.cask.co/browse/CDAP-3058>`__ -
  Fixed a problem with the NamespacedLocationFactory not appending correctly.

- `CDAP-3066 <https://issues.cask.co/browse/CDAP-3066>`__ -
  Fixed a problem with FileBatchSource not failing properly.

- `CDAP-3067 <https://issues.cask.co/browse/CDAP-3067>`__ -
  Fixed a problem with the UpgradeTool throwing a NullPointerException during UsageRegistry.upgrade().

- `CDAP-3070 <https://issues.cask.co/browse/CDAP-3070>`__ -
  Fixed a problem on Ubuntu 14.10 where removing JSON files from templates/plugins/ETLBatch breaks adapters.

- `CDAP-3072 <https://issues.cask.co/browse/CDAP-3072>`__ -
  Fixed a problem with a documentation JavaScript bug.

- `CDAP-3073 <https://issues.cask.co/browse/CDAP-3073>`__ -
  Fixed a problem with out-of-memory perm gen space.

- `CDAP-3085 <https://issues.cask.co/browse/CDAP-3085>`__ -
  Fixed a problem with adding integration tests for datasets.

- `CDAP-3086 <https://issues.cask.co/browse/CDAP-3086>`__ -
  Fixed a problem with the CDAP-UI current adapter UI.

- `CDAP-3087 <https://issues.cask.co/browse/CDAP-3087>`__ -
  Fixed a problem with CDAP-UI: a session timeout on secure mode.

- `CDAP-3088 <https://issues.cask.co/browse/CDAP-3088>`__ -
  Fixed a problem with CDAP-UI: application types need to be updated.

- `CDAP-3092 <https://issues.cask.co/browse/CDAP-3092>`__ -
  Fixed a problem with reading multiple files with one mapper in FileBatchSource.

- `CDAP-3096 <https://issues.cask.co/browse/CDAP-3096>`__ -
  Fixed a problem with running MapReduce on HDP 2.2.

- `CDAP-3098 <https://issues.cask.co/browse/CDAP-3098>`__ -
  Fixed problems with the CDAP-UI Adapter UI.

- `CDAP-3099 <https://issues.cask.co/browse/CDAP-3099>`__ -
  Fixed a problem with CDAP-UI and that settings icons shift 2px when you click on them.

- `CDAP-3104 <https://issues.cask.co/browse/CDAP-3104>`__ -
  Fixed a problem with CDAP Explore throwing an exception if a Table dataset does not set schema.

- `CDAP-3105 <https://issues.cask.co/browse/CDAP-3105>`__ -
  Fixed a problem with LogParserTransform needing to emit HTTP status code info.

- `CDAP-3106 <https://issues.cask.co/browse/CDAP-3106>`__ -
  Fixed a problem with Hive query - local MapReduce task failure on CDH-5.4.

- `CDAP-3125 <https://issues.cask.co/browse/CDAP-3125>`__ -
  Fixed a problem with the WorkerProgramRunnerTest failing intermittently.

- `CDAP-3127 <https://issues.cask.co/browse/CDAP-3127>`__ -
  Fixed a problem with the Kafka guide not working with CDAP 3.1.0.

- `CDAP-3132 <https://issues.cask.co/browse/CDAP-3132>`__ -
  Fixed a problem with the ProgramLifecycleHttpHandlerTest failing intermittently.

- `CDAP-3145 <https://issues.cask.co/browse/CDAP-3145>`__ -
  Fixed a problem with the Metrics processor not processing metrics.

- `CDAP-3146 <https://issues.cask.co/browse/CDAP-3146>`__ -
  Fixed a problem with the CDAP VM build failing to instal the Eclipse plugin.

- `CDAP-3148 <https://issues.cask.co/browse/CDAP-3148>`__ -
  Fixed a problem with CDAP Explore MapReduce queries failing due to MR-framework being 
  localized in the mapper container.

- `CDAP-3149 <https://issues.cask.co/browse/CDAP-3149>`__ -
  Fixed a problem with cycles in an adapter create page causing the browser to freeze.

- `CDAP-3151 <https://issues.cask.co/browse/CDAP-3151>`__ -
  Fixed a problem with CDAP examples shipped with SDK using JDK 1.6.

- `CDAP-3161 <https://issues.cask.co/browse/CDAP-3161>`__ -
  Fixed a problem with MapReduce no longer working with default Cloudera manager settings.

- `CDAP-3173 <https://issues.cask.co/browse/CDAP-3173>`__ -
  Fixed a problem with upgrading to 3.1.0 crashing the HBase co-processor.

- `CDAP-3174 <https://issues.cask.co/browse/CDAP-3174>`__ -
  Fixed a problem with the ETL source/transform/sinks descriptions and documentation.

- `CDAP-3175 <https://issues.cask.co/browse/CDAP-3175>`__ -
  Fixed a problem with the AbstractFlowlet constructors being deprecated when they should not be.


.. API Changes
.. -----------


Deprecated and Removed Features
-------------------------------

- See the :ref:`CDAP 3.1.0 Javadocs <javadocs>` for a list of deprecated and removed APIs.


.. _known-issues-310:

Known Issues
------------

- See the above section (*API Changes*) for alterations that can affect existing installations.
- CDAP has been tested on and supports CDH 4.2.x through CDH 5.4.4, HDP 2.0 through 2.6, and
  Apache Bigtop 0.8.0. It has not been tested on more recent versions of CDH. 
  See `our Hadoop/HBase Environment configurations 
  <http://docs.cask.co/cdap/3.1.0/en/admin-manual/installation/installation.html#install-hadoop-hbase>`__.
- After upgrading CDAP from a pre-3.0 version, any unprocessed metrics data in Kafka will
  be lost and *WARN* log messages will be logged that tell about the inability to process
  old data in the old format.

- Retrieving multiple metrics |---| by issuing an HTTP POST request with a JSON list as
  the request body that enumerates the name and attributes for each metric |---| is currently not
  supported in the `Metrics HTTP RESTful API v3 
  <http://docs.cask.co/cdap/2.8.0/en/reference-manual/http-restful-api/http-restful-api-v3/metrics.html#query-tips>`__.
  Instead, use the v2 API. It will be supported in a future release.

- `CDAP-797 <https://issues.cask.co/browse/CDAP-797>`__ -
  When running secure Hadoop clusters, metrics and debug logs from MapReduce programs are
  not available.
  
- `CDAP-1007 <https://issues.cask.co/browse/CDAP-1007>`__ -
  If the Hive Metastore is restarted while the CDAP Explore Service is running, the
  Explore Service remains alive, but becomes unusable. To correct, restart the CDAP Master
  |---| which will restart all services |---| as described under "Starting CDAP Services"
  for your particular Hadoop distribution in the `Installation documentation
  <http://docs.cask.co/cdap/3.0.0/en/admin-manual/installation/installation.html#starting-services>`__.

- `CDAP-1587 <https://issues.cask.co/browse/CDAP-1587>`__ - 
  CDAP internally creates tables in the "user" space that begin with the word
  ``"system"``. User datasets with names starting with ``"system"`` can conflict if they
  were to match one of those names. To avoid this, do not start any datasets with the word
  ``"system"``. 
  
- `CDAP-1864 <https://issues.cask.co/browse/CDAP-1864>`__ - 
  Creating a dataset in a non-existent namespace manifests in the RESTful API with an
  incorrect error message.
  
- `CDAP-2632 <https://issues.cask.co/browse/CDAP-2632>`__ -
  The application in the `cdap-kafka-ingest-guide 
  <https://github.com/cdap-guides/cdap-kafka-ingest-guide/tree/release/cdap-3.0-compatible>`__ 
  does not run on Ubuntu 14.x as of CDAP 3.0.x.
    
- `CDAP-2785 <https://issues.cask.co/browse/CDAP-2785>`__ -
  In the CDAP UI, many buttons will remain in focus after being clicked, even if they
  should not retain focus.
  
- `CDAP-2831 <https://issues.cask.co/browse/CDAP-2831>`__ -
  A workflow that is scheduled by time will not be run between the failure of the primary
  master and the time that the secondary takes over. This scheduled run will not be
  triggered at all.

- `CDAP-2878 <https://issues.cask.co/browse/CDAP-2878>`__ -
  The semantics for TTL are confusing, in that the Table TTL property is
  interpreted as milliseconds in some contexts: ``DatasetDefinition.confgure()`` and
  ``getAdmin()``.

- `CDAP-2945 <https://issues.cask.co/browse/CDAP-2945>`__ -
  If the input partition filter for a PartitionedFileSet does not match any partitions,
  MapReduce jobs can fail.

- `CDAP-3000 <https://issues.cask.co/browse/CDAP-3000>`__ -
  The Workflow token is in an inconsistent state for nodes in a fork while the nodes of
  the fork are still running. It becomes consistent after the join.

- `CDAP-3101 <https://issues.cask.co/browse/CDAP-3101>`__ -
  If there are more than 30 concurrent runs of a workflow, the runs will not be scheduled
  due to a Quartz exception.

- `CDAP-3179 <https://issues.cask.co/browse/CDAP-3179>`__ -
  If you are using CDH 5.3 (CDAP 3.0.0) and are upgrading to CDH 5.4 (CDAP 3.1.0), you
  must first upgrade the underlying HBase before you upgrade CDAP. This means that you perform the
  CDH upgrade before upgrading the CDAP.
  
- `CDAP-3189 <https://issues.cask.co/browse/CDAP-3189>`__ -
  Large MapReduce jobs can cause excessive logging in the CDAP logs. 
  
- `CDAP-3221 <https://issues.cask.co/browse/CDAP-3221>`__ -
  When running in CDAP Standalone mode, if a MapReduce job fails repeatedly, then the SDK
  hits an out-of-memory exception due to ``perm gen``. The Standalone needs restarting at
  this point.


`Release 3.0.3 <http://docs.cask.co/cdap/3.0.3/index.html>`__
=============================================================

Bug Fix
-------

- Fixed a Bower dependency error in the CDAP UI
  (`CDAP-3010 <https://issues.cask.co/browse/CDAP-3010>`__).

Known Issues
------------
- See the *Known Issues* of `version 3.0.1 <#known-issues-301>`_\ .

`Release 3.0.2 <http://docs.cask.co/cdap/3.0.2/index.html>`__
=============================================================

Bug Fixes
---------

- Fixed problems with the dataset upgrade tool
  (`CDAP-2962 <https://issues.cask.co/browse/CDAP-2962>`__, 
  `CDAP-2897 <https://issues.cask.co/browse/CDAP-2897>`__).

Known Issues
------------
- See the *Known Issues* of `version 3.0.1 <#known-issues-301>`_\ .

`Release 3.0.1 <http://docs.cask.co/cdap/3.0.1/index.html>`__
=============================================================

New Features
------------

- In the CDAP UI, mandatory parameters for Application Template creation are marked with
  asterisks, and if a user tries to create a template without one of those parameters, the
  missing parameter is highlighted
  (`CDAP-2499 <https://issues.cask.co/browse/CDAP-2499>`__).


Improvements
------------

**Tools**

- Added a tool (`HBaseQueueDebugger 
  <https://github.com/caskdata/cdap/blob/release/3.0/cdap-master/src/main/java/co/cask/cdap/data/tools/HBaseQueueDebugger.java>`__)
  that counts consumed and unconsumed entries in a flowlet queue
  (`CDAP-2105 <https://issues.cask.co/browse/CDAP-2105>`__).

**CDAP UI**

- The currently executing node of a workflow is now highlighted in the CDAP UI
  (`CDAP-2615 <https://issues.cask.co/browse/CDAP-2615>`__).
  
- The list of datasets and the run histories in the CDAP UI are now paginated 
  (`CDAP-2626 <https://issues.cask.co/browse/CDAP-2626>`__,
  `CDAP-2627 <https://issues.cask.co/browse/CDAP-2627>`__).
  
- Added improvements to the CDAP UI when creating Application Templates
  (`CDAP-2601 <https://issues.cask.co/browse/CDAP-2601>`__,
  `CDAP-2602 <https://issues.cask.co/browse/CDAP-2602>`__,
  `CDAP-2603 <https://issues.cask.co/browse/CDAP-2603>`__,
  `CDAP-2605 <https://issues.cask.co/browse/CDAP-2605>`__,
  `CDAP-2606 <https://issues.cask.co/browse/CDAP-2606>`__,
  `CDAP-2607 <https://issues.cask.co/browse/CDAP-2607>`__,
  `CDAP-2610 <https://issues.cask.co/browse/CDAP-2610>`__).
  
- Improved the error messages returned when there are problems creating Application 
  Templates in the CDAP UI
  (`CDAP-2597 <https://issues.cask.co/browse/CDAP-2597>`__).
  
**CDAP SDK VM**

- Added the Apache Flume agent flume-ng to the CDAP SDK VM
  (`CDAP-2612 <https://issues.cask.co/browse/CDAP-2612>`__).

- Added the ability to copy and paste to the CDAP SDK VM
  (`CDAP-2611 <https://issues.cask.co/browse/CDAP-2611>`__).

- Pre-downloaded the example dependencies into the CDAP SDK VM to speed building of the 
  CDAP examples
  (`CDAP-2613 <https://issues.cask.co/browse/CDAP-2613>`__).


Bug Fixes
---------

**General**

- Fixed a problem with the HBase store and flows with multiple queues, where one queue name
  is a prefix of another queue name
  (`CDAP-1996 <https://issues.cask.co/browse/CDAP-1996>`__).
  
- Fixed a problem with namespaces with underscores in the name crashing the Hadoop HBase 
  region servers
  (`CDAP-2110 <https://issues.cask.co/browse/CDAP-2110>`__).
  
- Removed the requirement to specify the JDBC driver class property twice in the adaptor
  configuration for Database Sources and Sinks
  (`CDAP-2453 <https://issues.cask.co/browse/CDAP-2453>`__).

- Fixed a problem in CDAP Distributed where the status of running program always returns 
  as "STOPPED" when the CDAP Master is restarted
  (`CDAP-2489 <https://issues.cask.co/browse/CDAP-2489>`__).
  
- Fixed a problem with invalid RunRecords for Spark and MapReduce programs that are run as 
  part of a Workflow (`CDAP-2490 <https://issues.cask.co/browse/CDAP-2490>`__).
  
- Fixed a problem with the CDAP Master not being HA (highly available) when a leadership 
  change happens
  (`CDAP-2495 <https://issues.cask.co/browse/CDAP-2495>`__).
  
- Fixed a problem with upgrading of queues with the UpgradeTool
  (`CDAP-2502 <https://issues.cask.co/browse/CDAP-2502>`__).
  
- Fixed a problem with ObjectMappedTables not deleting missing fields when updating a row
  (`CDAP-2523 <https://issues.cask.co/browse/CDAP-2523>`__,
  `CDAP-2524 <https://issues.cask.co/browse/CDAP-2524>`__).
  
- Fixed a problem with a stream not being created properly when deploying an application
  after the default namespace was deleted
  (`CDAP-2537 <https://issues.cask.co/browse/CDAP-2537>`__).
  
- Fixed a problem with the Applicaton Template Kafka Source not using the persisted offset
  when the Adapter is restarted
  (`CDAP-2547 <https://issues.cask.co/browse/CDAP-2547>`__).
  
- A problem with CDAP using its own transaction snapshot codec, leading to huge snapshot
  files and OutOfMemory exceptions, and transaction snapshots that can't be read using
  Tephra's tools, has been resolved by replacing the codec with Tephra's SnapshotCodecV3
  (`CDAP-2563 <https://issues.cask.co/browse/CDAP-2563>`__,
  `CDAP-2946 <https://issues.cask.co/browse/CDAP-2946>`__,
  `TEPHRA-101 <https://issues.cask.co/browse/TEPHRA-101>`__).
  
- Fixed a problem with CDAP Master not being resilient in the handling of ZooKeeper 
  exceptions
  (`CDAP-2569 <https://issues.cask.co/browse/CDAP-2569>`__).
  
- Fixed a problem with RunRecords not being cleaned up correctly after certain exceptions
  (`CDAP-2584 <https://issues.cask.co/browse/CDAP-2584>`__).
  
- Fixed a problem with the CDAP Maven archetype having an incorrect CDAP version in it
  (`CDAP-2634 <https://issues.cask.co/browse/CDAP-2634>`__).
  
- Fixed a problem with the description of the TwitterSource not describing its output
  (`CDAP-2648 <https://issues.cask.co/browse/CDAP-2648>`__).
  
- Fixed a problem with the Twitter Source not handling missing fields correctly and as a
  consequence producing tweets (with errors) that were then not stored on disk
  (`CDAP-2653 <https://issues.cask.co/browse/CDAP-2653>`__).
  
- Fixed a problem with the TwitterSource not calculating the time of tweet correctly
  (`CDAP-2656 <https://issues.cask.co/browse/CDAP-2656>`__).
  
- Fixed a problem with the JMS Real-time Source failing to load required plugin sources
  (`CDAP-2661 <https://issues.cask.co/browse/CDAP-2661>`__).

- Fixed a problem with executing Hive queries on a distributed CDAP due to a failure to 
  load Grok classes
  (`CDAP-2678 <https://issues.cask.co/browse/CDAP-2678>`__).
  
- Fixed a problem with CDAP Program jars not being cleaned up from the temporary directory
  (`CDAP-2698 <https://issues.cask.co/browse/CDAP-2698>`__).
  
- Fixed a problem with ProjectionTransforms not handling input data fields with null 
  values correctly
  (`CDAP-2719 <https://issues.cask.co/browse/CDAP-2719>`__).

- Fixed a problem with the CDAP SDK running out of memory when MapReduce jobs are run repeatedly
  (`CDAP-2743 <https://issues.cask.co/browse/CDAP-2743>`__).

- Fixed a problem with not using CDAP RunIDs in the in-memory version of the CDAP SDK
  (`CDAP-2769 <https://issues.cask.co/browse/CDAP-2769>`__).

**CDAP CLI**

- Fixed a problem with the CDAP CLI not printing an error if it is unable to connect to a 
  CDAP instance
  (`CDAP-2529 <https://issues.cask.co/browse/CDAP-2529>`__).
  
- Fixed a problem with extra whitespace in commands entered into the CDAP CLI causing errors
  (`CDAP-2538 <https://issues.cask.co/browse/CDAP-2538>`__).
  
**CDAP SDK Standalone**

- Updated the messages displayed when starting the CDAP Standalone SDK as to components 
  and the JVM required (`CDAP-2445 <https://issues.cask.co/browse/CDAP-2445>`__).
  
- Fixed a problem with the creation of the default namespace upon starting the CDAP SDK
  (`CDAP-2587 <https://issues.cask.co/browse/CDAP-2587>`__).
  
**CDAP SDK VM**

- Fixed a problem with using the default namespace on the CDAP SDK Virtual Machine Image
  (`CDAP-2500 <https://issues.cask.co/browse/CDAP-2500>`__).

- Fixed a problem with the VirtualBox VM retaining a MAC address obtained from the build host
  (`CDAP-2640 <https://issues.cask.co/browse/CDAP-2640>`__).
  
**CDAP UI**

- Fixed a problem with incorrect flow metrics showing in the CDAP UI
  (`CDAP-2494 <https://issues.cask.co/browse/CDAP-2494>`__).
  
- Fixed a problem in the CDAP UI with the properties in the Projection Transform being 
  displayed inconsistently
  (`CDAP-2525 <https://issues.cask.co/browse/CDAP-2525>`__).
  
- Fixed a problem in the CDAP UI not automatically updating the number of flowlet instances
  (`CDAP-2534 <https://issues.cask.co/browse/CDAP-2534>`__).
  
- Fixed a problem in the CDAP UI with a window resize preventing clicking of the Adapter 
  Template drop down menu
  (`CDAP-2573 <https://issues.cask.co/browse/CDAP-2573>`__).
  
- Fixed a problem with the CDAP UI not performing validation of mandatory parameters 
  before the creation of an adapter
  (`CDAP-2575 <https://issues.cask.co/browse/CDAP-2575>`__).
  
- Fixed a problem with an incorrect version of CDAP being shown in the CDAP UI
  (`CDAP-2586 <https://issues.cask.co/browse/CDAP-2586>`__).

- Reduced the number of clicks required to navigate and perform actions within the CDAP UI
  (`CDAP-2622 <https://issues.cask.co/browse/CDAP-2622>`__,
  `CDAP-2625 <https://issues.cask.co/browse/CDAP-2625>`__).
  
- Fixed a problem with an additional forward-slash character in the URL causing a "page 
  not found error" in the CDAP UI
  (`CDAP-2624 <https://issues.cask.co/browse/CDAP-2624>`__).
  
- Fixed a problem with the error dropdown of the CDAP UI not scrolling when it has a 
  large number of errors
  (`CDAP-2633 <https://issues.cask.co/browse/CDAP-2633>`__).
  
- Fixed a problem in the CDAP UI with the Twitter Source's consumer key secret not being 
  treated as a password field
  (`CDAP-2649 <https://issues.cask.co/browse/CDAP-2649>`__).
  
- Fixed a problem with the CDAP UI attempting to create an adapter without a name
  (`CDAP-2652 <https://issues.cask.co/browse/CDAP-2652>`__).
  
- Fixed a problem with the CDAP UI not being able to find the ETL plugin templates on
  distributed CDAP
  (`CDAP-2655 <https://issues.cask.co/browse/CDAP-2655>`__).
  
- Fixed a problem with the CDAP UI's System Dashboard chart having a y-axis starting at "-200"
  (`CDAP-2699 <https://issues.cask.co/browse/CDAP-2699>`__).

- Fixed a problem with the rendering of stack trace logs in the CDAP UI
  (`CDAP-2745 <https://issues.cask.co/browse/CDAP-2745>`__).
  
- Fixed a problem with the CDAP UI not working with secure CDAP instances, either clusters 
  or standalone
  (`CDAP-2770 <https://issues.cask.co/browse/CDAP-2770>`__).
  
- Fixed a problem with the coloring of completed runs of Workflow DAGs in the CDAP UI
  (`CDAP-2781 <https://issues.cask.co/browse/CDAP-2781>`__).
  
**Documentation**

- Fixed errors with the documentation examples of the ETL Plugins
  (`CDAP-2503 <https://issues.cask.co/browse/CDAP-2503>`__).
  
- Documented the licenses of all shipped CDAP UI components
  (`CDAP-2582 <https://issues.cask.co/browse/CDAP-2582>`__).

- Corrected issues with the building of Javadocs used on the website and removed Javadocs
  previously included in the SDK
  (`CDAP-2730 <https://issues.cask.co/browse/CDAP-2730>`__).
  
- Added a recommended version (v.12.0) of Node.js to the documentation
  (`CDAP-2762 <https://issues.cask.co/browse/CDAP-2762>`__).
  

API Changes
-----------


Deprecated and Removed Features
-------------------------------


.. _known-issues-301:

Known Issues
------------

- The application in the `cdap-kafka-ingest-guide 
  <https://github.com/cdap-guides/cdap-kafka-ingest-guide/tree/release/cdap-3.0-compatible>`__ 
  does not run on Ubuntu 14.x and CDAP 3.0.x
  (`CDAP-2632 <https://issues.cask.co/browse/CDAP-2632>`__,
  `CDAP-2749 <https://issues.cask.co/browse/CDAP-2749>`__).

- Metrics for :ref:`TimePartitionedFileSets <datasets-timepartitioned-fileset>` can show 
  zero values even if there is data present
  (`CDAP-2721 <https://issues.cask.co/browse/CDAP-2721>`__).
  
- In the CDAP UI: many buttons will remain in focus after being clicked, even if they
  should not retain focus
  (`CDAP-2785 <https://issues.cask.co/browse/CDAP-2785>`__).
  
- When the CDAP-Master dies, the CDAP UI does not repsond appropriately, and instead of 
  waiting for routing to the secondary master to begin, it loses its connection
  (`CDAP-2830 <https://issues.cask.co/browse/CDAP-2830>`__).

- A workflow that is scheduled by time will not be run between the failure of the primary
  master and the time that the secondary takes over. This scheduled run will not be
  triggered at all. There is no warnings or messages about the missed run of the
  workflow. (`CDAP-2831 <https://issues.cask.co/browse/CDAP-2831>`__)

- CDAP has been tested on and supports CDH 4.2.x through CDH 5.3.x, HDP 2.0 through 2.1, and
  Apache Bigtop 0.8.0. It has not been tested on more recent versions of CDH. 
  See `our Hadoop/HBase Environment configurations 
  <http://docs.cask.co/cdap/3.0.0/en/admin-manual/installation/installation.html#install-hadoop-hbase>`__.
  
- After upgrading CDAP from a pre-3.0 version, any unprocessed metrics data in Kafka will
  be lost and *WARN* log messages will be logged that tell about the inability to process
  old data in the old format.

- See the above section (*API Changes*) for alterations that can affect existing installations.

- When running secure Hadoop clusters, metrics and debug logs from MapReduce programs are
  not available (`CDAP-797 <https://issues.cask.co/browse/CDAP-797>`__).
- If the Hive Metastore is restarted while the CDAP Explore Service is running, the 
  Explore Service remains alive, but becomes unusable. To correct, `restart the CDAP Master
  <http://docs.cask.co/cdap/3.0.0/en/admin-manual/installation/installation.html#starting-services>`__, 
  which will restart all services (`CDAP-1007 <https://issues.cask.co/browse/CDAP-1007>`__).
- User datasets with names starting with ``"system"`` can potentially cause conflicts
  (`CDAP-1587 <https://issues.cask.co/browse/CDAP-1587>`__).
- Scaling the number of metrics processor instances doesn't automatically distribute the
  processing load to the newer instances of the metrics processor. The CDAP Master needs to be
  restarted to effectively distribute the processing across all metrics processor instances
  (`CDAP-1853 <https://issues.cask.co/browse/CDAP-1853>`__).
- Creating a dataset in a non-existent namespace manifests in the RESTful API with an
  incorrect error message (`CDAP-1864 <https://issues.cask.co/browse/CDAP-1864>`__).
- Retrieving multiple metrics |---| by issuing an HTTP POST request with a JSON list as
  the request body that enumerates the name and attributes for each metric |---| is currently not
  supported in the 
  `Metrics HTTP RESTful API v3 <http://docs.cask.co/cdap/2.8.0/en/reference-manual/http-restful-api/http-restful-api-v3/metrics.html#query-tips>`__.
  Instead, use the v2 API. It will be supported in a future release.
- Typically, datasets are bundled as part of applications. When an application is upgraded and redeployed,
  any changes in datasets will not be redeployed. This is because datasets can be shared across applications,
  and an incompatible schema change can break other applications that are using the dataset.
  A workaround (`CDAP-1253 <https://issues.cask.co/browse/CDAP-1253>`__) is to allow *unchecked dataset upgrades*.
  Upgrades cause the dataset metadata, i.e. its specification including properties, to be updated. The dataset
  runtime code is also updated. To prevent data loss the existing data and the underlying HBase tables remain as-is.

  You can allow *unchecked dataset upgrades* by setting the configuration property ``dataset.unchecked.upgrade``
  to ``true`` in ``cdap-site.xml``. This will ensure that datasets are upgraded when the application is redeployed.
  When this configuration is set, the recommended process to deploy an upgraded dataset is to first stop
  all applications that are using the dataset before deploying the new version of the application.
  This lets all containers (flows, services, etc) to pick up the new dataset changes.
  When datasets are upgraded using ``dataset.unchecked.upgrade``, no schema compatibility checks are performed by the
  system. Hence it is very important that the developer verify the backward-compatibility, and makes sure that
  other applications that are using the dataset can work with the new changes.


`Release 3.0.0 <http://docs.cask.co/cdap/3.0.0/index.html>`__
=============================================================

New Features
------------

- Support for application templates has been added (`CDAP-1753 <https://issues.cask.co/browse/CDAP-1753>`__).

- Built-in ETL application templates and plugins have been added (`CDAP-1767 <https://issues.cask.co/browse/CDAP-1767>`__).

- New :ref:`CDAP UI <cdap-ui>`, supports creating ETL applications directly in the web UI.
  See section below (:ref:`New User Interface <new-user-interface-300>`) for details.

- Workflow logs can now be retrieved using the :ref:`CDP HTTP Logging RESTful API 
  <http-restful-api-logging>` (`CDAP-1089 <https://issues.cask.co/browse/CDAP-1089>`__).
  
- Support has been added for suspending and resuming of a workflow (`CDAP-1610
  <https://issues.cask.co/browse/CDAP-1610>`__).
  
- Condition nodes in a workflow now allow branching based on a boolean predicate
  (`CDAP-1928 <https://issues.cask.co/browse/CDAP-1928>`__).
  
- Condition nodes in a workflow now allow passing the Hadoop counters from a MapReduce
  program to following Condition nodes in the workflow (`CDAP-1611
  <https://issues.cask.co/browse/CDAP-1611>`__).
  
- Logs can now be fetched based on the ``run-id`` (`CDAP-1582
  <https://issues.cask.co/browse/CDAP-1582>`__).
  
- CDAP Tables are :ref:`now explorable <table-exploration>` (`CDAP-946
  <https://issues.cask.co/browse/CDAP-946>`__).

- The :ref:`CDAP CLI <cli>` supports the new `application template and adapters APIs 
  <http://docs.cask.co/cdap/3.0.0/en/application-templates/index.html>`__. (`CDAP-1773 <https://issues.cask.co/browse/CDAP-1773>`__).
  
- The :ref:`CDAP CLI <cli>` startup options have been changed to accommodate a new option
  of executing a file containing a series of CLI commands, line-by-line.
  
- Both `grok <http://logstash.net/docs/1.4.2/filters/grok>`__ and 
  `syslog <http://en.wikipedia.org/wiki/Syslog>`__ record formats can now be used when 
  :ref:`setting the format of a stream <http-restful-api-stream-setting-properties>`
  (`CDAP-1949 <https://issues.cask.co/browse/CDAP-1949>`__).
  
- Added HTTP RESTful endpoints for listing datasets and streams as used by adapters, 
  programs, and applications, and vice-versa 
  (`CDAP-2214 <https://issues.cask.co/browse/CDAP-2214>`__).
  
- Created a `queue introspection tool <https://github.com/caskdata/cdap/pull/2290>`__, 
  for counting processed and unprocessed entries in a 
  flowlet queue (`CDAP-2105 <https://issues.cask.co/browse/CDAP-2105>`__).

- Support for CDAP SDK VM build automation has been added (`CDAP-2030 <https://issues.cask.co/browse/CDAP-2030>`__).

- A Cube dataset has been added (`CDAP-1520 <https://issues.cask.co/browse/CDAP-1520>`__).

- A Batch and Real-Time Cube dataset sink has been added (`CDAP-1520 <https://issues.cask.co/browse/CDAP-1966>`__).

- Metrics and status information for MapReduce on a task level is now exposed (`CDAP-1520 <https://issues.cask.co/browse/CDAP-1958>`__).


.. _new-user-interface-300:

New User Interface
------------------
- Introduced a new UI, organization based on namespaces and users.
- Users can switch between namespaces. 
- Uses web sockets to retrieve updates from the backend.
- **Development Section**

  - Introduces a UI for programs based on run-ids.
  - Users can view logs and, in certain cases |---| flows |---| flowlets, of a program based on run ids.
  - Shows a list of datasets and streams used by a program, and shows programs using a specific dataset and stream.
  - Shows the history of a program (list of runs).
  - Datasets or streams are explorable on a dataset/stream level or on a global level.
  - Shows program level metrics on under each program.
  
- **Operations section**

  - Introduces an operations section to explore metrics.
  - Allows users to create custom dashboard with custom metrics.
  - Users can add different types of charts (line, bar, area, pie, donut, scatter, spline,
    area-spline, area-spline-stacked, area-stacked, step, table).
  - Users can add multiple metrics on a single dashboard, or on a single widget on a single dashboard.
  - Users can organize the widgets in either a two, three, or four-column layout.
  - Users can toggle the frequency at which data is polled for a metric.
  - Users can toggle the resolution of data displayed in a graph.
  
- **Admin Section**

  - Users can manage different objects of CDAP (applications, programs, datasets, and streams).
  - Users can create namespaces.
  - Through the Admin view, users can configure their preferences at the CDAP level, namespace level, or application level.
  - Users can manage the system services, applications, and streams through the Admin view.
  
- **Adapters**

  - Users can create ETLBatch and ETLRealtime adapters from within the UI.
  - Users can choose from a list of plugins that comes by default with CDAP when creating an adapter.
  - Users can save an adapter as a draft, to be created at a later point-in-time.
  - Users can configure plugin properties with appropriate editors from within the UI when creating an adapter.
  
- The Old CDAP Console has been deprecated.

Improvement
-----------

- The :ref:`metrics system APIs<http-restful-api-metrics>` have been revised and improved
  (`CDAP-1596 <https://issues.cask.co/browse/CDAP-1596>`__).
- The metrics system performance has been improved
  (`CDAP-2124 <https://issues.cask.co/browse/CDAP-2124>`__, 
  `CDAP-2125 <https://issues.cask.co/browse/CDAP-2125>`__).

Bug Fixes
---------

- The CDAP Authentication server now reports the port correctly when the port is set to 0
  (`CDAP-614 <https://issues.cask.co/browse/CDAP-614>`__).

- History of the programs running under workflow (Spark and MapReduce) is now updated correctly
  (`CDAP-1293 <https://issues.cask.co/browse/CDAP-1293>`__).

- Programs running under a workflow now receive a unique ``run-id``
  (`CDAP-2025 <https://issues.cask.co/browse/CDAP-2025>`__).

- RunRecords are now updated with the RuntimeService to account for node failures
  (`CDAP-2202 <https://issues.cask.co/browse/CDAP-2202>`__).

- MapReduce metrics are now available on a secure cluster
  (`CDAP-64 <https://issues.cask.co/browse/CDAP-64>`__).

API Changes
-----------

- The endpoint (``POST '<base-url>/metrics/search?target=childContext[&context=<context>]'``)
  that searched for the available contexts of metrics has been deprecated, pending removal
  in a later version of CDAP (`CDAP-1998 <https://issues.cask.co/browse/CDAP-1998>`__). A
  :ref:`replacement endpoint <http-restful-api-metrics-search-for-contexts>` is available.

- The endpoint (``POST '<base-url>/metrics/search?target=metric&context=<context>'``)
  that searched for metrics in a specified context has been deprecated, pending removal
  in a later version of CDAP (`CDAP-1998 <https://issues.cask.co/browse/CDAP-1998>`__). A
  :ref:`replacement endpoint <http-restful-api-metrics-search-for-metrics>` is available.

- The endpoint (``POST '<base-url>/metrics/query?context=<context>[&groupBy=<tags>]&metric=<metric>&<time-range>'``)
  that queried for a metric has been deprecated, pending removal
  in a later version of CDAP (`CDAP-1998 <https://issues.cask.co/browse/CDAP-1998>`__). A
  :ref:`replacement endpoint <http-restful-api-metrics-querying-a-metric>` is available.
  
- Metrics: The tag name for service handlers in previous releases was wrongly ``"runnable"``,
  and internally represented as ``"srn"``. These metrics are now tagged as ``"handler"`` (``"hnd"``), and
  metrics queries will only account for this tag name. If you need to query historic metrics
  that were emitted with the old tag ``"runnable"``, use ``"srn"`` to query them (instead of either
  ``"runnable"`` or ``"handler"``).

- The :ref:`CDAP CLI <cli>` startup options have been changed to accommodate a new option
  of executing a file containing a series of CLI commands, line-by-line.

- The metrics system APIs have been improved (`CDAP-1596 <https://issues.cask.co/browse/CDAP-1596>`__).

- The rules for :ref:`resolving resolution <http-restful-api-metrics-time-range>`
  when using ``resolution=auto`` in metrics query have been changed
  (`CDAP-1922 <https://issues.cask.co/browse/CDAP-1922>`__).

- Backward incompatible changes in ``InputFormatProvider`` and ``OutputFormatProvider``. 
  It won't affect user code that uses ``FileSet`` or ``PartitionedFileSet``. 
  It only affects classes who implement the ``InputFormatProvider`` or ``OutputFormatProvider``:

  - ``InputFormatProvider.getInputFormatClass()`` is removed and
  
    - replaced with ``InputFormatProvider.getInputFormatClassName()``;
    
  - ``OutputFormatProvider.getOutputFormatClass()`` is removed and
  
    - replaced with ``OutputFormatProvider.getOutputFormatClassName()``.


Deprecated and Removed Features
-------------------------------

- The `File DropZone <http://docs.cask.co/cdap/2.8.0/en/developers-manual/ingesting-tools/cdap-file-drop-zone.html>`__ 
  and `File Tailer <http://docs.cask.co/cdap/2.8.0/en/developers-manual/ingesting-tools/cdap-file-tailer.html>`__
  are no longer supported as of Release 3.0.
- Support for *procedures* has been removed. After upgrading, an application that
  contained a procedure must be redeployed. 
- Support for *service workers* have been removed. After upgrading, an application that
  contained a service worker must be redeployed.  
- The old CDAP Console has been deprecated.
- Support for JDK/JRE 1.6 (Java 6) has ended; JDK/JRE 1.7 (Java 7) is 
  `now required for CDAP <http://docs.cask.co/cdap/3.0.0/en/admin-manual/installation/installation.html#install-java-runtime>`__ or the 
  `CDAP SDK <http://docs.cask.co/cdap/3.0.0/en/developers-manual/getting-started/standalone/index.html#standalone-index>`__.


.. _known-issues-300:

Known Issues
------------

- CDAP has been tested on and supports CDH 4.2.x through CDH 5.3.x, HDP 2.0 through 2.1, and
  Apache Bigtop 0.8.0. It has not been tested on more recent versions of CDH. 
  See `our Hadoop/HBase Environment configurations 
  <http://docs.cask.co/cdap/3.0.0/en/admin-manual/installation/installation.html#install-hadoop-hbase>`__.
  
- After upgrading CDAP from a pre-3.0 version, any unprocessed metrics data in Kafka will
  be lost and *WARN* log messages will be logged that tell about the inability to process
  old data in the old format.

- See the above section (*API Changes*) for alterations that can affect existing installations.

- When running secure Hadoop clusters, metrics and debug logs from MapReduce programs are
  not available (`CDAP-797 <https://issues.cask.co/browse/CDAP-797>`__).
- If the Hive Metastore is restarted while the CDAP Explore Service is running, the 
  Explore Service remains alive, but becomes unusable. To correct, `restart the CDAP Master
  <http://docs.cask.co/cdap/3.0.0/en/admin-manual/installation/installation.html#starting-services>`__, 
  which will restart all services (`CDAP-1007 <https://issues.cask.co/browse/CDAP-1007>`__).
- User datasets with names starting with ``"system"`` can potentially cause conflicts
  (`CDAP-1587 <https://issues.cask.co/browse/CDAP-1587>`__).
- Scaling the number of metrics processor instances doesn't automatically distribute the
  processing load to the newer instances of the metrics processor. The CDAP Master needs to be
  restarted to effectively distribute the processing across all metrics processor instances
  (`CDAP-1853 <https://issues.cask.co/browse/CDAP-1853>`__).
- Creating a dataset in a non-existent namespace manifests in the RESTful API with an
  incorrect error message (`CDAP-1864 <https://issues.cask.co/browse/CDAP-1864>`__).
- Retrieving multiple metrics |---| by issuing an HTTP POST request with a JSON list as
  the request body that enumerates the name and attributes for each metric |---| is currently not
  supported in the 
  `Metrics HTTP RESTful API v3 <http://docs.cask.co/cdap/2.8.0/en/reference-manual/http-restful-api/http-restful-api-v3/metrics.html#query-tips>`__.
  Instead, use the v2 API. It will be supported in a future release.
- Typically, datasets are bundled as part of applications. When an application is upgraded and redeployed,
  any changes in datasets will not be redeployed. This is because datasets can be shared across applications,
  and an incompatible schema change can break other applications that are using the dataset.
  A workaround (`CDAP-1253 <https://issues.cask.co/browse/CDAP-1253>`__) is to allow *unchecked dataset upgrades*.
  Upgrades cause the dataset metadata, i.e. its specification including properties, to be updated. The dataset
  runtime code is also updated. To prevent data loss the existing data and the underlying HBase tables remain as-is.

  You can allow *unchecked dataset upgrades* by setting the configuration property ``dataset.unchecked.upgrade``
  to ``true`` in ``cdap-site.xml``. This will ensure that datasets are upgraded when the application is redeployed.
  When this configuration is set, the recommended process to deploy an upgraded dataset is to first stop
  all applications that are using the dataset before deploying the new version of the application.
  This lets all containers (flows, services, etc) to pick up the new dataset changes.
  When datasets are upgraded using ``dataset.unchecked.upgrade``, no schema compatibility checks are performed by the
  system. Hence it is very important that the developer verify the backward-compatibility, and makes sure that
  other applications that are using the dataset can work with the new changes.
  

`Release 2.8.0 <http://docs.cask.co/cdap/2.8.0/index.html>`__
=============================================================

General
-------

- The HTTP RESTful API v2 was deprecated, replaced with the
  :ref:`namespaced HTTP RESTful API v3 <http-restful-api-v3>`.

- Added log rotation for CDAP programs running in YARN containers
  (`CDAP-1295 <https://issues.cask.co/browse/CDAP-1295>`__).

- Added the ability to submit to non-default YARN queues to provide 
  :ref:`resource guarantees <resource-guarantees>`
  for CDAP Master services, CDAP programs, and Explore Queries
  (`CDAP-1417 <https://issues.cask.co/browse/CDAP-1417>`__).

- Added the ability to :ref:`prune invalid transactions <tx-maintenance>`
  (`CDAP-1540 <https://issues.cask.co/browse/CDAP-1540>`__).

- Added the ability to specify 
  :ref:`custom logback file for CDAP programs <application-logback>`
  (`CDAP-1100 <https://issues.cask.co/browse/CDAP-1100>`__).

- System HTTP services now bind to all interfaces (0.0.0.0), rather than 127.0.0.1.

New Features
------------

- **Command Line Interface (CLI)**

  - CLI can now directly connect to a CDAP instance of your choice at startup by using
    ``cdap-cli.sh --uri <uri>``.
  - Support for runtime arguments, which can be listed by running ``"cdap-cli.sh --help"``.
  - Table rendering can be configured using ``"cli render as <alt|csv>"``. 
    The option ``"alt"`` is the default, with ``"csv"`` available for copy & pasting.
  - Stream statistics can be computed using ``"get stream-stats <stream-id>"``.
  
- **Datasets**

  - Added an ObjectMappedTable dataset that maps object fields to table columns and that is also explorable.
  - Added a PartitionedFileSet dataset that allows addressing files by meta data and that is also explorable.  
  - Table datasets now support a multi-get operation for batched reads.
  - Allow an unchecked dataset upgrade upon application deployment
    (`CDAP-1574 <https://issues.cask.co/browse/CDAP-1574>`__).

- **Metrics**

  - Added new APIs for exploring available metrics, including drilling down into the context of emitted metrics
  - Added the ability to explore (search) all metrics; previously, this was restricted to custom user metrics
  - There are new APIs for querying metrics
  - New capability to break down a metrics time series using the values of one or more tags in its context
  
- **Namespaces**

  - Applications and programs are now managed within namespaces.
  - Application logs are available within namespaces.
  - Metrics are now collected and queried within namespaces.
  - Datasets can now created and managed within namespaces.
  - Streams are now namespaced for ingestion, fetching, and consuming by programs.
  - Explore operations are now namespaced.
  
- **Preferences**

  - Users can store preferences (a property map) at the instance, namespace, application, 
    or program level.
  
- **Spark**

  - Spark now uses a configurer-style API for specifying
    (`CDAP-382 <https://issues.cask.co/browse/CDAP-1134>`__).
  
- **Workflows**

  - Users can schedule a workflow based on increments of data being ingested into a stream.
  - Workflows can be stopped.
  - The execution of a workflow can be forked into parallelized branches.
  - The runtime arguments for workflow can be scoped.
  
- **Workers**

  - Added :ref:`Worker <workers>`, a new program type that can be added to CDAP applications, 
    used to run background processes and (beta feature) can write to streams through the
    WorkerContext.
    
- **Upgrade and Data Migration Tool**

  - Added an `automated upgrade tool 
    <http://docs.cask.co/cdap/2.8.0/en/admin-manual/installation/installation.html#upgrading-an-existing-version>`__ 
    which supports upgrading from
    2.6.x to 2.8.0. (**Note:** Apps need to be both recompiled and re-deployed.)
    Upgrade from 2.7.x to 2.8.0 is not currently supported. If you have a use case for it, 
    please reach out to us at `cdap-user@googlegroups.com <https://groups.google.com/d/forum/cdap-user>`__.
  - Added a metric migration tool which migrates old metrics to the new 2.8 format.


Improvement
-----------

- Improved flow performance and scalability with a new distributed queue implementation.


API Changes
-----------

- The endpoint (``GET <base-url>/data/explore/datasets/<dataset-name>/schema``) that
  retrieved the schema of a dataset's underlying Hive table has been removed
  (`CDAP-1603 <https://issues.cask.co/browse/CDAP-1603>`__).
- Endpoints have been added to retrieve the CDAP version and the current configurations of
  CDAP and HBase (:ref:`Configuration HTTP RESTful API <http-restful-api-configuration>`).


.. _known-issues-280:

Known Issues
------------
- When running secure Hadoop clusters, metrics and debug logs from MapReduce programs are
  not available (`CDAP-64 <https://issues.cask.co/browse/CDAP-64>`__ and `CDAP-797
  <https://issues.cask.co/browse/CDAP-797>`__).
- If the Hive Metastore is restarted while the CDAP Explore Service is running, the 
  Explore Service remains alive, but becomes unusable. To correct, `restart the CDAP Master
  <http://docs.cask.co/cdap/2.8.0/en/admin-manual/installation/installation.html#starting-services>`__, 
  which will restart all services (`CDAP-1007 <https://issues.cask.co/browse/CDAP-1007>`__).
- User datasets with names starting with ``"system"`` can potentially cause conflicts
  (`CDAP-1587 <https://issues.cask.co/browse/CDAP-1587>`__).
- Scaling the number of metrics processor instances doesn't automatically distribute the
  processing load to the newer instances of the metrics processor. The CDAP Master needs to be
  restarted to effectively distribute the processing across all metrics processor instances
  (`CDAP-1853 <https://issues.cask.co/browse/CDAP-1853>`__).
- Creating a dataset in a non-existent namespace manifests in the RESTful API with an
  incorrect error message (`CDAP-1864 <https://issues.cask.co/browse/CDAP-1864>`__).
- Retrieving multiple metrics |---| by issuing an HTTP POST request with a JSON list as
  the request body that enumerates the name and attributes for each metric |---| is currently not
  supported in the 
  `Metrics HTTP RESTful API v3 <http://docs.cask.co/cdap/2.8.0/en/reference-manual/http-restful-api/http-restful-api-v3/metrics.html#query-tips>`__.
  Instead, use the v2 API. It will be supported in a future release.
- Typically, datasets are bundled as part of applications. When an application is upgraded and redeployed,
  any changes in datasets will not be redeployed. This is because datasets can be shared across applications,
  and an incompatible schema change can break other applications that are using the dataset.
  A workaround (`CDAP-1253 <https://issues.cask.co/browse/CDAP-1253>`__) is to allow *unchecked dataset upgrades*.
  Upgrades cause the dataset metadata, i.e. its specification including properties, to be updated. The dataset
  runtime code is also updated. To prevent data loss the existing data and the underlying HBase tables remain as-is.

  You can allow *unchecked dataset upgrades* by setting the configuration property ``dataset.unchecked.upgrade``
  to ``true`` in ``cdap-site.xml``. This will ensure that datasets are upgraded when the application is redeployed.
  When this configuration is set, the recommended process to deploy an upgraded dataset is to first stop
  all applications that are using the dataset before deploying the new version of the application.
  This lets all containers (flows, services, etc) to pick up the new dataset changes.
  When datasets are upgraded using ``dataset.unchecked.upgrade``, no schema compatibility checks are performed by the
  system. Hence it is very important that the developer verify the backward-compatibility, and makes sure that
  other applications that are using the dataset can work with the new changes.
- A race condition resulting in a deadlock can occur when a TwillRunnable container
  shutdowns while it still has ZooKeeper events to process. This occasionally surfaces when
  running with OpenJDK or JDK7, though not with Oracle JDK6. It is caused by a change in the
  ``ThreadPoolExecutor`` implementation between Oracle JDK6 and OpenJDK/JDK7. Until Twill is
  updated in a future version of CDAP, a work-around is to kill the errant process. The Yarn
  command to list all running applications and their ``app-id``\s is::
  
    yarn application -list -appStates RUNNING

  The command to kill a process is::
  
    yarn application -kill <app-id>
    
  All versions of CDAP running Twill version 0.4.0 with this configuration can exhibit this
  problem (`TWILL-110 <https://issues.apache.org/jira/browse/TWILL-110>`__).
  

`Release 2.7.1 <http://docs.cask.co/cdap/2.7.1/index.html>`__
=============================================================

API Changes
-----------
-  The property ``security.auth.server.address`` has been deprecated and replaced with
   ``security.auth.server.bind.address`` (`CDAP-639 <https://issues.cask.co/browse/CDAP-639>`__,
   `CDAP-1078 <https://issues.cask.co/browse/CDAP-1078>`__).

New Features
------------

- **Spark**

  - Spark now uses a configurer-style API for specifying (`CDAP-382 <https://issues.cask.co/browse/CDAP-382>`__).
  - Spark can now run as a part of a workflow (`CDAP-465 <https://issues.cask.co/browse/CDAP-465>`__).

- **Security**

  - CDAP Master now obtains and refreshes Kerberos tickets programmatically (`CDAP-1134 <https://issues.cask.co/browse/CDAP-1134>`__).

- **Datasets**

  - A new, experimental dataset type to support time-partitioned File sets has been added.
  - Time-partitioned File sets can be queried with Impala on CDH distributions (`CDAP-926 <https://issues.cask.co/browse/CDAP-926>`__).
  - Streams can be made queryable with Impala by deploying an adapter that periodically
    converts it into partitions of a time-partitioned File set (`CDAP-1129 <https://issues.cask.co/browse/CDAP-1129>`__).
  - Support for different levels of conflict detection: ``ROW``, ``COLUMN``, or ``NONE`` (`CDAP-1016 <https://issues.cask.co/browse/CDAP-1016>`__).
  - Removed support for ``@DisableTransaction`` (`CDAP-1279 <https://issues.cask.co/browse/CDAP-1279>`__).
  - Support for annotating a stream with a schema (`CDAP-606 <https://issues.cask.co/browse/CDAP-606>`__).
  - A new API for uploading entire files to a stream has been added (`CDAP-411 <https://issues.cask.co/browse/CDAP-411>`__).

- **Workflow**

  - Workflow now uses a configurer-style API for specifying (`CDAP-1207 <https://issues.cask.co/browse/CDAP-1207>`__).
  - Multiple instances of a workflow can be run concurrently (`CDAP-513 <https://issues.cask.co/browse/CDAP-513>`__).
  - Programs are no longer part of a workflow; instead, they are added in the application
    and are referenced by a workflow using their names (`CDAP-1116 <https://issues.cask.co/browse/CDAP-1116>`__).
  - Schedules are now at the application level and properties can be specified for
    Schedules; these properties will be passed to the scheduled program as runtime
    arguments (`CDAP-1148 <https://issues.cask.co/browse/CDAP-1148>`__).

.. _known-issues-271:

Known Issues
------------
- When upgrading an existing CDAP installation to 2.7.1, all metrics are reset.
- When running secure Hadoop clusters, metrics and debug logs from MapReduce programs are
  not available (`CDAP-64 <https://issues.cask.co/browse/CDAP-64>`__ and `CDAP-797
  <https://issues.cask.co/browse/CDAP-797>`__).
- When upgrading a cluster from an earlier version of CDAP, warning messages may appear in
  the master log indicating that in-transit (emitted, but not yet processed) metrics
  system messages could not be decoded (*Failed to decode message to MetricsRecord*). This
  is because of a change in the format of emitted metrics, and can result in a small
  amount of metrics data points being lost (`CDAP-745 <https://issues.cask.co/browse/CDAP-745>`__).
- A race condition resulting in a deadlock can occur when a TwillRunnable container
  shutdowns while it still has ZooKeeper events to process. This occasionally surfaces when
  running with OpenJDK or JDK7, though not with Oracle JDK6. It is caused by a change in the
  ``ThreadPoolExecutor`` implementation between Oracle JDK6 and OpenJDK/JDK7. Until Twill is
  updated in a future version of CDAP, a work-around is to kill the errant process. The Yarn
  command to list all running applications and their ``app-id``\s is::
  
    yarn application -list -appStates RUNNING

  The command to kill a process is::
  
    yarn application -kill <app-id>
    
  All versions of CDAP running Twill version 0.4.0 with this configuration can exhibit this
  problem (`TWILL-110 <https://issues.apache.org/jira/browse/TWILL-110>`__).

- Typically, datasets are bundled as part of applications. When an application is upgraded and redeployed,
  any changes in datasets will not be redeployed. This is because datasets can be shared across applications,
  and an incompatible schema change can break other applications that are using the dataset.
  A workaround (`CDAP-1253 <https://issues.cask.co/browse/CDAP-1253>`__) is to allow *unchecked dataset upgrades*.
  Upgrades cause the dataset metadata, i.e. its specification including properties, to be updated. The dataset
  runtime code is also updated. To prevent data loss the existing data and the underlying HBase tables remain as-is.

  You can allow *unchecked dataset upgrades* by setting the configuration property ``dataset.unchecked.upgrade``
  to ``true`` in ``cdap-site.xml``. This will ensure that datasets are upgraded when the application is redeployed.
  When this configuration is set, the recommended process to deploy an upgraded dataset is to first stop
  all applications that are using the dataset before deploying the new version of the application.
  This lets all containers (flows, services, etc) to pick up the new dataset changes.
  When datasets are upgraded using ``dataset.unchecked.upgrade``, no schema compatibility checks are performed by the
  system. Hence it is very important that the developer verify the backward-compatibility, and makes sure that
  other applications that are using the dataset can work with the new changes.


`Release 2.6.1 <http://docs.cask.co/cdap/2.6.1/index.html>`__
=============================================================

CDAP Bug Fixes
--------------
- Allow an *unchecked dataset upgrade* upon application deployment
  (`CDAP-1253 <https://issues.cask.co/browse/CDAP-1253>`__).
- Update the Hive dataset table when a dataset is updated
  (`CDAP-71 <https://issues.cask.co/browse/CDAP-71>`__).
- Use Hadoop configuration files bundled with the Explore Service
  (`CDAP-1250 <https://issues.cask.co/browse/CDAP-1250>`__).

.. _known-issues-261:

Known Issues
------------
- When running secure Hadoop clusters, metrics and debug logs from MapReduce programs are
  not available (`CDAP-64 <https://issues.cask.co/browse/CDAP-64>`__ and `CDAP-797
  <https://issues.cask.co/browse/CDAP-797>`__).
- When upgrading a cluster from an earlier version of CDAP, warning messages may appear in
  the master log indicating that in-transit (emitted, but not yet processed) metrics
  system messages could not be decoded (*Failed to decode message to MetricsRecord*). This
  is because of a change in the format of emitted metrics, and can result in a small
  amount of metrics data points being lost (`CDAP-745 <https://issues.cask.co/browse/CDAP-745>`__).
- A race condition resulting in a deadlock can occur when a TwillRunnable container
  shutdowns while it still has ZooKeeper events to process. This occasionally surfaces when
  running with OpenJDK or JDK7, though not with Oracle JDK6. It is caused by a change in the
  ``ThreadPoolExecutor`` implementation between Oracle JDK6 and OpenJDK/JDK7. Until Twill is
  updated in a future version of CDAP, a work-around is to kill the errant process. The Yarn
  command to list all running applications and their ``app-id``\s is::
  
    yarn application -list -appStates RUNNING

  The command to kill a process is::
  
    yarn application -kill <app-id>
    
  All versions of CDAP running Twill version 0.4.0 with this configuration can exhibit this
  problem (`TWILL-110 <https://issues.apache.org/jira/browse/TWILL-110>`__).

- Typically, datasets are bundled as part of applications. When an application is upgraded and redeployed,
  any changes in datasets will not be redeployed. This is because datasets can be shared across applications,
  and an incompatible schema change can break other applications that are using the dataset.
  A workaround (`CDAP-1253 <https://issues.cask.co/browse/CDAP-1253>`__) is to allow *unchecked dataset upgrades*.
  Upgrades cause the dataset metadata, i.e. its specification including properties, to be updated. The dataset
  runtime code is also updated. To prevent data loss the existing data and the underlying HBase tables remain as-is.

  You can allow *unchecked dataset upgrades* by setting the configuration property ``dataset.unchecked.upgrade``
  to ``true`` in ``cdap-site.xml``. This will ensure that datasets are upgraded when the application is redeployed.
  When this configuration is set, the recommended process to deploy an upgraded dataset is to first stop
  all applications that are using the dataset before deploying the new version of the application.
  This lets all containers (flows, services, etc) to pick up the new dataset changes.
  When datasets are upgraded using ``dataset.unchecked.upgrade``, no schema compatibility checks are performed by the
  system. Hence it is very important that the developer verify the backward-compatibility, and makes sure that
  other applications that are using the dataset can work with the new changes.


`Release 2.6.0 <http://docs.cask.co/cdap/2.6.0/index.html>`__
=============================================================

API Changes
-----------
-  API for specifying services and MapReduce programs has been changed to use a "configurer" 
   style; this will require modification of user classes implementing either MapReduce
   or service as the interfaces have changed (`CDAP-335
   <https://issues.cask.co/browse/CDAP-335>`__).


New Features
------------

- **General**

  - Health checks are now available for CDAP system services
    (`CDAP-663 <https://issues.cask.co/browse/CDAP-663>`__).

- **Applications**

  -  Jar deployment now uses a chunked request and writes to a local temp file
     (`CDAP-91 <https://issues.cask.co/browse/CDAP-91>`__).

- **MapReduce**

  -  MapReduce programs can now read binary stream data
     (`CDAP-331 <https://issues.cask.co/browse/CDAP-331>`__).

- **Datasets**

  - Added :ref:`FileSet <datasets-fileset>`, a new core dataset type for working with sets of files
    (`CDAP-1 <https://issues.cask.co/browse/CDAP-1>`__).

- **Spark**

  - Spark programs now emit system and custom user metrics
    (`CDAP-346 <https://issues.cask.co/browse/CDAP-346>`__).
  - Services can be called from Spark programs and its worker nodes
    (`CDAP-348 <https://issues.cask.co/browse/CDAP-348>`__).
  - Spark programs can now read from streams
    (`CDAP-403 <https://issues.cask.co/browse/CDAP-403>`__).
  - Added Spark support to the CDAP CLI (Command-line Interface)
    (`CDAP-425 <https://issues.cask.co/browse/CDAP-425>`__).
  - Improved speed of Spark unit tests
    (`CDAP-600 <https://issues.cask.co/browse/CDAP-600>`__).
  - Spark programs now display system metrics in the CDAP Console
    (`CDAP-652 <https://issues.cask.co/browse/CDAP-652>`__).

- **Procedures**

  - Procedures have been deprecated in favor of services
    (`CDAP-413 <https://issues.cask.co/browse/CDAP-413>`__).

- **Services**

  - Added an HTTP endpoint that returns the endpoints a particular service exposes
    (`CDAP-412 <https://issues.cask.co/browse/CDAP-412>`__).
  - Added an HTTP endpoint that lists all services
    (`CDAP-469 <https://issues.cask.co/browse/CDAP-469>`__).
  - Default metrics for services have been added to the CDAP Console
    (`CDAP-512 <https://issues.cask.co/browse/CDAP-512>`__).
  - The annotations ``@QueryParam`` and ``@DefaultValue`` are now supported in custom service handlers
    (`CDAP-664 <https://issues.cask.co/browse/CDAP-664>`__).

- **Metrics**

  - System and user metrics now support gauge metrics
    (`CDAP-484 <https://issues.cask.co/browse/CDAP-484>`__).
  - Metrics can be queried using a program’s run-ID
    (`CDAP-620 <https://issues.cask.co/browse/CDAP-620>`__).

- **Documentation**

  - A `Quick Start Guide <http://docs.cask.co/cdap/2.6.0/en/admin-manual/installation/quick-start.html#installation-quick-start>`__ has been added to the 
    `CDAP Administration Manual <http://docs.cask.co/cdap/2.6.0/en/admin-manual/index.html#admin-index>`__ 
    (`CDAP-695 <https://issues.cask.co/browse/CDAP-695>`__).

CDAP Bug Fixes
--------------

- Fixed a problem with readless increments not being used when they were enabled in a dataset
  (`CDAP-383 <https://issues.cask.co/browse/CDAP-383>`__).
- Fixed a problem with applications, whose Spark or Scala user classes were not extended
  from either ``JavaSparkProgram`` or ``ScalaSparkProgram``, failing with a class loading error
  (`CDAP-599 <https://issues.cask.co/browse/CDAP-599>`__).
- Fixed a problem with the `CDAP upgrade tool 
  <http://docs.cask.co/cdap/2.6.0/en/admin-manual/installation/installation.html#upgrading-an-existing-version>`__ 
  not preserving |---| for tables with readless increments enabled |---| the coprocessor
  configuration during an upgrade (`CDAP-1044 <https://issues.cask.co/browse/CDAP-1044>`__).
- Fixed a problem with the readless increment implementation dropping increment cells when 
  a region flush or compaction occurred (`CDAP-1062 <https://issues.cask.co/browse/CDAP-1062>`__).

.. _known-issues-260:

Known Issues
------------

- When running secure Hadoop clusters, metrics and debug logs from MapReduce programs are
  not available (`CDAP-64 <https://issues.cask.co/browse/CDAP-64>`__ and `CDAP-797
  <https://issues.cask.co/browse/CDAP-797>`__).
- When upgrading a cluster from an earlier version of CDAP, warning messages may appear in
  the master log indicating that in-transit (emitted, but not yet processed) metrics
  system messages could not be decoded (*Failed to decode message to MetricsRecord*). This
  is because of a change in the format of emitted metrics, and can result in a small
  amount of metrics data points being lost (`CDAP-745
  <https://issues.cask.co/browse/CDAP-745>`__).
- A race condition resulting in a deadlock can occur when a TwillRunnable container
  shutdowns while it still has ZooKeeper events to process. This occasionally surfaces when
  running with OpenJDK or JDK7, though not with Oracle JDK6. It is caused by a change in the
  ``ThreadPoolExecutor`` implementation between Oracle JDK6 and OpenJDK/JDK7. Until Twill is
  updated in a future version of CDAP, a work-around is to kill the errant process. The Yarn
  command to list all running applications and their ``app-id``\s is::
  
    yarn application -list -appStates RUNNING

  The command to kill a process is::
  
    yarn application -kill <app-id>
    
  All versions of CDAP running Twill version 0.4.0 with this configuration can exhibit this
  problem (`TWILL-110 <https://issues.apache.org/jira/browse/TWILL-110>`__).


`Release 2.5.2 <http://docs.cask.co/cdap/2.5.2/index.html>`__
=============================================================

CDAP Bug Fixes
--------------

- Fixed a problem with a Coopr-provisioned secure cluster failing to start due to a classpath
  issue (`CDAP-478 <https://issues.cask.co/browse/CDAP-478>`__).
- Fixed a problem with the WISE app zip distribution not packaged correctly; a new version
  (0.2.1) has been released (`CDAP-533 <https://issues.cask.co/browse/CDAP-533>`__).
- Fixed a problem with the examples and tests incorrectly using the ByteBuffer.array
  method when reading a stream event (`CDAP-549 <https://issues.cask.co/browse/CDAP-549>`__).
- Fixed a problem with the Authentication Server so that it can now communicate with an LDAP
  instance over SSL (`CDAP-556 <https://issues.cask.co/browse/CDAP-556>`__).
- Fixed a problem with the program class loader to allow applications to use a different
  version of a library than the one that the CDAP platform uses; for example, a different
  Kafka library (`CDAP-559 <https://issues.cask.co/browse/CDAP-559>`__).
- Fixed a problem with CDAP master not obtaining new delegation tokens after running for 
  ``hbase.auth.key.update.interval`` milliseconds (`CDAP-562 <https://issues.cask.co/browse/CDAP-562>`__).
- Fixed a problem with the transaction not being rolled back when a user service handler throws an exception 
  (`CDAP-607 <https://issues.cask.co/browse/CDAP-607>`__).

Other Changes
-------------

- Improved the CDAP documentation:

  - Re-organized the documentation into three manuals |---| Developers' Manual, Administration
    Manual, Reference Manual |---| and a set of examples, how-to guides and tutorials;
  - Documents are now in smaller chapters, with numerous updates and revisions;
  - Added a link for downloading an archive of the documentation for offline use;
  - Added links to examples relevant to a particular component;
  - Added suggested deployment architectures for Distributed CDAP installations;
  - Added a glossary;
  - Added navigation aids at the bottom of each page; and
  - Tested and updated the Standalone CDAP examples and their documentation.

Known Issues
------------
- Currently, applications that include Spark or Scala classes in user classes not extended
  from either ``JavaSparkProgram`` or ``ScalaSparkProgram`` (depending upon the language)
  fail with a class loading error. Spark or Scala classes should not be used outside of the
  Spark program. (`CDAP-599 <https://issues.cask.co/browse/CDAP-599>`__)
- Metrics for MapReduce programs aren't populated on secure Hadoop clusters
- The metric for the number of cores shown in the Resources view of the CDAP Console will be zero
  unless YARN has been configured to enable virtual cores
- Writing to datasets through Hive is not supported in CDH4.x
  (`CDAP-988 <https://issues.cask.co/browse/CDAP-988>`__).
- A race condition resulting in a deadlock can occur when a TwillRunnable container
  shutdowns while it still has ZooKeeper events to process. This occasionally surfaces when
  running with OpenJDK or JDK7, though not with Oracle JDK6. It is caused by a change in the
  ``ThreadPoolExecutor`` implementation between Oracle JDK6 and OpenJDK/JDK7. Until Twill is
  updated in a future version of CDAP, a work-around is to kill the errant process. The Yarn
  command to list all running applications and their ``app-id``\s is::
  
    yarn application -list -appStates RUNNING

  The command to kill a process is::
  
    yarn application -kill <app-id>
    
  All versions of CDAP running Twill version 0.4.0 with this configuration can exhibit this
  problem (`TWILL-110 <https://issues.apache.org/jira/browse/TWILL-110>`__).


`Release 2.5.1 <http://docs.cask.co/cdap/2.5.1/index.html>`__
=============================================================

CDAP Bug Fixes
--------------

- Improved the documentation of the CDAP authentication and stream clients, both Java and Python APIs.
- Fixed problems with the CDAP Command Line Interface (CLI):

  - Did not work in non-interactive mode;
  - Printed excessive debug log messages;
  - Relative paths did not work as expected; and 
  - Failed to execute SQL queries.
  
- Removed dependencies on SNAPSHOT artifacts for *netty-http* and *auth-clients*. 
- Corrected an error in the message printed by the startup script ``cdap.sh``.
- Resolved a problem with the reading of the properties file by the CDAP Flume Client of CDAP Ingest library
  without first checking if authentication was enabled.

Other Changes
-------------

- The scripts ``send-query.sh``, ``access-token.sh`` and ``access-token.bat`` has been replaced by the 
  :ref:`CDAP Command Line Interface, <cli>` ``cdap-cli.sh``.
- The CDAP Command Line Interface now uses and saves access tokens when connecting to a secure CDAP instance.
- The CDAP Java Stream Client now allows empty String events to be sent.
- The CDAP Python Authentication Client's ``configure()`` method now takes a dictionary rather than a filepath.

Known Issues
------------
- Metrics for MapReduce programs aren't populated on secure Hadoop clusters
- The metric for the number of cores shown in the Resources view of the CDAP Console will be zero
  unless YARN has been configured to enable virtual cores
- A race condition resulting in a deadlock can occur when a TwillRunnable container
  shutdowns while it still has ZooKeeper events to process. This occasionally surfaces when
  running with OpenJDK or JDK7, though not with Oracle JDK6. It is caused by a change in the
  ``ThreadPoolExecutor`` implementation between Oracle JDK6 and OpenJDK/JDK7. Until Twill is
  updated in a future version of CDAP, a work-around is to kill the errant process. The Yarn
  command to list all running applications and their ``app-id``\s is::
  
    yarn application -list -appStates RUNNING

  The command to kill a process is::
  
    yarn application -kill <app-id>
    
  All versions of CDAP running Twill version 0.4.0 with this configuration can exhibit this
  problem (`TWILL-110 <https://issues.apache.org/jira/browse/TWILL-110>`__).


`Release 2.5.0 <http://docs.cask.co/cdap/2.5.0/index.html>`__
=============================================================

New Features
------------

Ad-hoc querying
.................
- Capability to write to datasets using SQL
- Added a CDAP JDBC driver allowing connections from Java applications and third-party business intelligence tools
- Ability to perform ad-hoc queries from the CDAP Console:

  - Execute a SQL query from the Console
  - View list of active, completed queries
  - Download query results

Datasets
.................
- Datasets can be tested with TestBase outside of the context of an application
- CDAP now checks datasets for compatibility in a verification stage
- The Transaction engine uses server-side filtering for efficient transactional reads
- Dataset specifications can now be dynamically reconfigured through the use of RESTful endpoints
- The Bundle jar format is now used for dataset libs
- Increments on datasets are now read-less

Services
.................
- Added simplified APIs for using services from other programs such as MapReduce, flows and Procedures
- Added an API for creating services and handlers that can use datasets transactionally
- Added a RESTful API to make requests to a service via the Router

Security
.................
- Added authorization logging
- Added Kerberos authentication to ZooKeeper secret keys
- Added support for SSL

Spark Integration
.................
- Supports running Spark programs as a part of CDAP applications in Standalone mode
- Supports running Spark programs written with Spark versions 1.0.1 or 1.1.0 
- Supports Spark's *MLib* and *GraphX* modules
- Includes three examples demonstrating CDAP Spark programs
- Adds display of Spark program logs and history in the CDAP Console

Streams
.................
- Added a collection of applications, tools and APIs specifically for the ETL (Extract, Transform and Loading) of data
- Added support for asynchronously writing to streams

Clients
.................
- Added a Command Line Interface
- Added a Java Client Interface


Major CDAP Bug Fixes
--------------------
- Fixed a problem with a HADOOP_HOME exception stacktrace when unit-testing an application
- Fixed an issue with Hive creating directories in /tmp in the Standalone and unit-test frameworks
- Fixed a problem with type inconsistency of service API calls, where numbers were showing up as strings
- Fixed an issue with the premature expiration of long-term Authentication Tokens
- Fixed an issue with the dataset size metric showing data operations size instead of resource usage

.. _known-issues-250:

Known Issues
------------
- Metrics for MapReduce programs aren't populated on secure Hadoop clusters
- The metric for the number of cores shown in the Resources view of the CDAP Console will be zero
  unless YARN has been configured to enable virtual cores
- A race condition resulting in a deadlock can occur when a TwillRunnable container
  shutdowns while it still has ZooKeeper events to process. This occasionally surfaces when
  running with OpenJDK or JDK7, though not with Oracle JDK6. It is caused by a change in the
  ``ThreadPoolExecutor`` implementation between Oracle JDK6 and OpenJDK/JDK7. Until Twill is
  updated in a future version of CDAP, a work-around is to kill the errant process. The Yarn
  command to list all running applications and their ``app-id``\s is::
  
    yarn application -list -appStates RUNNING

  The command to kill a process is::
  
    yarn application -kill <app-id>
    
  All versions of CDAP running Twill version 0.4.0 with this configuration can exhibit this
  problem (`TWILL-110 <https://issues.apache.org/jira/browse/TWILL-110>`__).
