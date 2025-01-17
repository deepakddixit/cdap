.. meta::
    :author: Cask Data, Inc.
    :description: Cask Data Application Platform SparkPageRank Application
    :copyright: Copyright © 2015 Cask Data, Inc.

.. _examples-log-analysis:

====================
Log Analysis Example
====================

A Cask Data Application Platform (CDAP) example demonstrating Spark and MapReduce
running in parallel inside a Workflow through fork.


Overview
========
This example demonstrates Spark and MapReduce performing log analysis, computing
total number of hits for every unique URL, total number of responses for every
unique response code, and total number of requests made by every unique IP address,
based on Apache usage log.

Logs are sent to CDAP and ingested into the *logStream*, which stores the log
information event in its entirety.

After these events are streamed, they are taken up by the *ResponseCounterSpark*, which
goes through the entries, calculates the total number of responses for every unique
response code, and tabulates results in an ``KeyValueTable`` dataset, *responseCount*.
The Spark program also computes the total number of requests made by every unique IP
address and writes it to ``TimePartitionedFileSet``, *reqCount*.

In parallel, these events are also taken up by the *HitCounterProgram*, which goes
through the entries, calculates the total number hits for every unique URL and
tabulates results in a ``KeyValueTable`` dataset, *hitCount*.

The *LogAnalysisWorkflow* ties the Spark and MapReduce programs to run in parallel.

Once the application completes, you can query the *responseCount* dataset by using
the ``rescount`` endpoint of the *ResponseCounterService*. It will send back a
string result with the total number of responses on the ``rescount`` query parameter.
You can also query the *hitCount* dataset by using the ``url`` endpoint of the
*HitCounterService*. It will send the total number of hits for the queried url.
You can query the ``reqCount`` ``TimePartitionedFileSet`` by using the ``reqcount``
endpoint of the *RequestCounterService* which will return a set of all available
partitions. Using one of partitions from the above set, you can query for the total
number of requests made by every unique IP address in last 60 minutes. The ``reqfile``
endpoint of the *RequestCounterService* returns a map of IP addresses to the total
number of requests made by them.

Let's look at some of these components, and then run the application and see the results.

The *LogAnalysis* Application
-----------------------------
As in the other `examples <index.html>`__, the components
of the application are tied together by the class ``LogAnalysisApp``:

.. literalinclude:: /../../../cdap-examples/LogAnalysis/src/main/java/co/cask/cdap/examples/loganalysis/LogAnalysisApp.java
   :language: java
   :lines: 60-94
   :append: . . .

The *hitCount* and *responseCount* KeyValueTables and *reqCount* TimePartitionedFileSet
---------------------------------------------------------------------------------------
The calculated hit count for every unique URL is stored in a ``KeyValueTable`` dataset,
*hitCount* and the total number of responses for a response code is stored in another
``KeyValueTable`` dataset, *responseCount*. The total number of requests made by every
unique IP address is written to a ``TimePartitionedFileSet``, *ipCount*.

The *HitCounterService*, *ResponseCounterService*, and *RequestCounterService*
------------------------------------------------------------------------------
These services provide convenient endpoints:

- ``HitCounterService:`` ``hitcount`` endpoint to obtain the total number of hits for a given URL;
- ``ResponseCounterService:`` ``rescount`` endpoint to obtain the total number of responses for a given response code;
- ``RequestCounterService:`` ``reqcount`` endpoint to obtain a set of all the available partitions in the TimePartitionedFileSet; and
- ``RequestCounterService:`` ``reqfile`` endpoint to retrieve data from a particular partition.


.. Building and Starting
.. =====================
.. |example| replace:: LogAnalysis
.. |example-italic| replace:: *LogAnalysis*
.. |application-overview-page| replace:: :cdap-ui-apps-programs:`application overview page, programs tab <LogAnalysis>`

.. include:: _includes/_building-starting-running.txt


Running the Example
===================

.. Starting the Services
.. ---------------------
.. |example-service1| replace:: HitCounterService
.. |example-service1-italic| replace:: *HitCounterService*

.. |example-service2| replace:: ResponseCounterService
.. |example-service2-italic| replace:: *ResponseCounterService*

.. |example-service3| replace:: RequestCounterService
.. |example-service3-italic| replace:: *RequestCounterService*

.. include:: _includes/_starting-services.txt

Injecting Access Logs
---------------------
Inject a file of Apache access log to the stream *logStream* by running this command from the
Standalone CDAP SDK directory, using the Command Line Interface::
  
  $ cdap-cli.sh load stream logStream examples/LogAnalysis/resources/apache.accesslog "text/plain"
  Successfully sent stream event to stream 'logStream'

.. Starting the Workflow
.. ---------------------
.. |example-workflow| replace:: LogAnalysisWorkflow
.. |example-workflow-italic| replace:: *LogAnalysisWorkflow*

.. include:: _includes/_starting-workflow.txt

Querying the Results
--------------------
- To query the *hitCount* KeyValueTable through the ``HitCounterService``, send a query via an HTTP
  request using the ``curl`` command. For example:
  
  .. container:: highlight

    .. parsed-literal::
      |$| curl -w'\\n' -X POST -d'{"url":"/index.html"}' '\http://localhost:10000/v3/namespaces/default/apps/|example|/services/|example-service1|/methods/hitcount'

  You can also use the Command Line Interface:
  
  .. container:: highlight

    .. parsed-literal::
      |$| cdap-cli.sh call service |example|.\ |example-service1| POST 'hitcount' body '{"url":"/index.html"}'

  On success, this command will return the hit count for the above URL, such as ``4``.

- Similarly, to query the *responseCount* ``KeyValueTable`` through the *ResponseCounterService*, the *reqCount*
  ``TimePartitionedFileSet`` through the *RequestCounterService*, and to retrieve data from a particular partition of
  the ``TimePartitionedFileSet``, use either ``curl`` or the Command Line Interface:

  .. container:: highlight

    .. parsed-literal::
      |$| curl -w'\\n' '\http://localhost:10000/v3/namespaces/default/apps/|example|/services/|example-service2|/methods/rescount/200'

      |$| cdap-cli.sh call service |example|.\ |example-service2| GET 'rescount/200'

  On success, this command will return the total number of responses sent with the queried response code, ``30``.

- To query the set of all the available partitions, use either of these commands:

  .. container:: highlight

    .. parsed-literal::
      |$| curl -w'\\n' '\http://localhost:10000/v3/namespaces/default/apps/|example|/services/|example-service3|/methods/reqcount'

      |$| cdap-cli.sh call service |example|.\ |example-service3| GET 'reqcount'

  A possible successful response::
  
    ["7/29/15 7:47 PM"]

- To return a map of all the unique IP addresses with the number of requests made by them, use one of the available partitions:

  .. container:: highlight

    .. parsed-literal::
      |$| curl -w'\\n' -X POST -d'{"time":"7/29/15 7:47 PM"}' '\http://localhost:10000/v3/namespaces/default/apps/|example|/services/|example-service3|/methods/reqfile'

      |$| cdap-cli.sh call service |example|.\ |example-service3| POST 'reqfile' body '{"time":"7/29/15 7:47 PM"}'

  A possible successful response::

    {"255.255.255.109":1,255.255.255.121":1,"255.255.255.211":1}


.. Stopping and Removing the Application
.. =====================================
.. include:: _includes/_stopping-removing-application-title.txt

.. include:: _includes/_stopping-workflow.txt

.. include:: _includes/_stopping-services.txt

.. include:: _includes/_removing-application.txt
