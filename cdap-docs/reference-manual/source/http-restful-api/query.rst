.. meta::
    :author: Cask Data, Inc.
    :description: HTTP RESTful Interface to the Cask Data Application Platform
    :copyright: Copyright © 2014 Cask Data, Inc.

.. _http-restful-api-query:

===========================================================
Query HTTP RESTful API
===========================================================

.. highlight:: console

This interface supports submitting SQL queries over  datasets. Queries are
processed asynchronously; to obtain query results, perform these steps:

- first, **submit** the query;
- then poll for the query's **status** until it is finished;
- once finished, retrieve the **result schema** and the **results**;
- finally, **close the query** to free the resources that it holds.


.. _http-restful-api-query-submitting:

Submitting a Query
------------------
To submit a SQL query, post the query string to the ``queries`` URL::

  POST <base-url>/namespaces/<namespace>/data/explore/queries

.. list-table::
   :widths: 20 80
   :header-rows: 1

   * - Parameter
     - Description
   * - ``<namespace>``
     - Namespace ID

The body of the request must contain a JSON string of the form::

  {
    "query": "<SQL-query-string>"
  }

where ``<SQL-query-string>`` is the actual SQL query.

.. rubric:: HTTP Responses
.. list-table::
   :widths: 20 80
   :header-rows: 1

   * - Status Codes
     - Description
   * - ``200 OK``
     - The query execution was successfully initiated, and the body will contain the query-handle
       used to identify the query in subsequent requests
   * - ``400 Bad Request``
     - The query is not well-formed or contains an error, such as a nonexistent table name

.. rubric:: Comments

If the query execution was successfully initiated, the body will contain a handle 
used to identify the query in subsequent requests::

  { "handle":"<query-handle>" }

.. rubric:: Example
.. list-table::
   :widths: 20 80
   :stub-columns: 1

   * - HTTP Request
     - ``PUT <base-url>/namespaces/default/data/explore/queries``
   * - HTTP Body
     - ``{"query":"SELECT * FROM dataset_mydataset LIMIT 5"}``
   * - HTTP Response
     - ``{"handle":"57cf1b01-8dba-423a-a8b4-66cd29dd75e2"}``
   * - Description
     - Submit a query in the namespace *default* to get the first 5 entries from the
       dataset, *mydataset* in the namespace *default*


.. _http-restful-api-query-status:

Status of a Query
-----------------
The status of a query is obtained using a HTTP GET request to the query's URL::

  GET <base-url>/data/explore/queries/<query-handle>/status
  
**Note:** this endpoint is *not* namespaced, as all query-handles are globally unique.

.. list-table::
   :widths: 20 80
   :header-rows: 1

   * - Parameter
     - Description
   * - ``<query-handle>``
     - Handle obtained when the query was submitted

.. rubric:: HTTP Responses
.. list-table::
   :widths: 20 80
   :header-rows: 1

   * - Status Codes
     - Description
   * - ``200 OK``
     - The query exists and the body contains its status
   * - ``404 Not Found``
     - The query handle does not match any current query

.. rubric:: Comments

If the query exists, the body will contain the status of its execution
and whether the query has a results set::

  {
    "status":"<status-code>",
    "hasResults":<boolean>
   }

Status can be one of the following: ``INITIALIZED``, ``RUNNING``, ``FINISHED``, ``CANCELED``, ``CLOSED``,
``ERROR``, ``UNKNOWN``, and ``PENDING``.

.. rubric:: Example
.. list-table::
   :widths: 20 80
   :stub-columns: 1

   * - HTTP Request
     - ``GET <base-url>/data/explore/queries/57cf1b01-8dba-423a-a8b4-66cd29dd75e2/status``
   * - HTTP Response
     - ``{"status":"FINISHED","hasResults":true}``
   * - Description
     - Retrieve the status of the query in the namespace *default* which has the handle
       ``57cf1b01-8dba-423a-a8b4-66cd29dd75e2``


.. _http-restful-api-query-obtaining-results:

Obtaining the Result Schema
---------------------------
If the query's status is ``FINISHED`` and it has results, you can obtain the schema of the results::

  GET <base-url>/namespaces/<namespace>/data/explore/queries/<query-handle>/schema

.. list-table::
   :widths: 20 80
   :header-rows: 1

   * - Parameter
     - Description
   * - ``<namespace>``
     - Namespace ID
   * - ``<query-handle>``
     - Handle obtained when the query was submitted

.. rubric:: HTTP Responses
.. list-table::
   :widths: 20 80
   :header-rows: 1

   * - Status Codes
     - Description
   * - ``200 OK``
     - The query was successfully received and the query schema was returned in the body
   * - ``400 Bad Request``
     - The query is not well-formed or contains an error, or the query status is not ``FINISHED``
   * - ``404 Not Found``
     - The query handle does not match any current query

.. rubric:: Comments

The query's result schema is returned in a JSON body as a list of columns,
each given by its name, type and position; if the query has no result set, this list is empty::

  [
    {"name":"<name>", "type":"<type>", "position":<int>},
    ...
  ]

The type of each column is a data type as defined in the `Hive language manual
<https://cwiki.apache.org/confluence/display/Hive/LanguageManual+DDL>`_.

.. rubric:: Example
.. list-table::
   :widths: 20 80
   :stub-columns: 1

   * - HTTP Request
     - ``GET <base-url>/namespaces/default/data/explore/queries/57cf1b01-8dba-423a-a8b4-66cd29dd75e2/schema``
   * - HTTP Response
     - ``[{"name":"dataset_mydataset.key","type":"array<tinyint>","position":1},``
       ``{"name":"dataset_mydataset.value","type":"array<tinyint>","position":2}]``
   * - Description
     - Retrieve the schema of the result of the query in the namespace *default* which has
       the handle ``57cf1b01-8dba-423a-a8b4-66cd29dd75e2``


.. _http-restful-api-query-retrieving-results:

Retrieving Query Results
------------------------
Query results can be retrieved in batches after the query is finished, optionally specifying the batch
size in the body of the request::

  POST <base-url>/namespaces/<namespace>/data/explore/queries/<query-handle>/next

The body of the request can contain a JSON string specifying the batch size::

  {
    "size":<int>
  }

If the batch size is not specified, the default is 20.

.. list-table::
   :widths: 20 80
   :header-rows: 1

   * - Parameter
     - Description
   * - ``<namespace>``
     - Namespace ID
   * - ``<query-handle>``
     - Handle obtained when the query was submitted

.. rubric:: HTTP Responses
.. list-table::
   :widths: 20 80
   :header-rows: 1

   * - Status Codes
     - Description
   * - ``200 OK``
     - The event was successfully received and the result of the query was returned in the body
   * - ``404 Not Found``
     - The query handle does not match any current query

.. rubric:: Comments

The results are returned in a JSON body as a list of columns,
each given as a structure containing a list of column values::

  [
    { "columns": [ <value-1>, <value-2>, ..., ] },
    ...
  ]

The value at each position has the type that was returned in the result schema for that position.
For example, if the returned type was ``INT``, then the value will be an integer literal,
whereas for ``STRING`` or ``VARCHAR`` the value will be a string literal.

Repeat the query to retrieve subsequent results. If all results of the query have already 
been retrieved, then the returned list is empty. 

.. rubric:: Example
.. list-table::
   :widths: 20 80
   :stub-columns: 1

   * - HTTP Request
     - ``POST <base-url>/namespaces/default/data/explore/queries/57cf1b01-8dba-423a-a8b4-66cd29dd75e2/next``
   * - HTTP Response
     - | ``[{"columns": [ 10, 5]},``
       | `` {"columns": [ 20, 27]},``
       | `` {"columns": [ 50, 6]},``
       | `` {"columns": [ 90, 30]},``
       | `` {"columns": [ 95, 91]}]``
   * - Description
     - Retrieve the results of the query which has the handle 57cf1b01-8dba-423a-a8b4-66cd29dd75e2


.. _http-restful-api-query-closing:

Closing a Query
---------------
The query can be closed by issuing an HTTP DELETE against its URL::

  DELETE <base-url>/namespaces/<namespace>/data/explore/queries/<query-handle>

This frees all resources that are held by this query.

.. list-table::
   :widths: 20 80
   :header-rows: 1

   * - Parameter
     - Description
   * - ``<namespace>``
     - Namespace ID
   * - ``<query-handle>``
     - Handle obtained when the query was submitted

.. rubric:: HTTP Responses
.. list-table::
   :widths: 20 80
   :header-rows: 1

   * - Status Codes
     - Description
   * - ``200 OK``
     - The query was closed
   * - ``400 Bad Request``
     - The query was not in a state that could be closed; either wait until it is finished, or cancel it
   * - ``404 Not Found``
     - The query handle does not match any current query

.. rubric:: Example
.. list-table::
   :widths: 20 80
   :stub-columns: 1

   * - HTTP Request
     - ``DELETE <base-url>/namespaces/default/data/explore/queries/57cf1b01-8dba-423a-a8b4-66cd29dd75e2``
   * - Description
     - Close the query in the namespace *default* which has the handle ``57cf1b01-8dba-423a-a8b4-66cd29dd75e2``


.. _http-restful-api-query-listing:

List of Queries
---------------
To return a list of queries, use::

   GET <base-url>/namespaces/<namespace>/data/explore/queries?limit=<limit>&cursor=<cursor>&offset=<offset>

.. list-table::
   :widths: 20 80
   :header-rows: 1

   * - Parameter
     - Description
   * - ``<namespace>``
     - Namespace ID
   * - ``<limit>``
     - Optional number indicating how many results to return in the response; by default, 50 results are returned
   * - ``<cursor>``
     - Optional string specifying if the results returned should be in the forward or reverse direction;
       should be one of ``next`` or ``prev``
   * - ``<offset>``
     - Optional offset for pagination; returns the results that are greater than offset if the cursor is ``next`` or
       results that are less than offset if cursor is ``prev``

.. rubric:: Comments

The results are returned as a JSON array, with each element containing information about a query::

  [
    {
        "timestamp": 1407192465183,
        "statement": "SHOW TABLES",
        "status": "FINISHED",
        "query_handle": "319d9438-903f-49b8-9fff-ac71cf5d173d",
        "has_results": true,
        "is_active": false
    },
    ...
  ]

.. rubric:: Example
.. list-table::
   :widths: 20 80
   :stub-columns: 1

   * - HTTP Request
     - ``GET <base-url>/namespaces/default/data/explore/queries``
   * - HTTP Response
     - | ``[{``
       | `` "timestamp": 1411266478717,``
       | `` "statement": "SELECT * FROM dataset_mydataset",``
       | `` "status": "FINISHED",``
       | `` "query_handle": "57cf1b01-8dba-423a-a8b4-66cd29dd75e2",``
       | `` "has_results": true,``
       | `` "is_active": false``
       | ``}]``
   * - Description
     - Retrieves all queries


.. _http-restful-api-query-counting:

Count of Active Queries
-----------------------
To return the count of active queries, use::

   GET <base-url>/namespaces/<namespace>/data/explore/queries/count

.. list-table::
   :widths: 20 80
   :header-rows: 1

   * - Parameter
     - Description
   * - ``<namespace>``
     - Namespace ID

The results are returned in the body as a JSON string::

  { "count":6 }


.. _http-restful-api-query-downloading:

Download Query Results
----------------------
To download the results of a query, use::

  POST <base-url>/namespaces/<namespace>/data/explore/queries/<query-handle>/download

The results of the query are returned in CSV format.

.. list-table::
   :widths: 20 80
   :header-rows: 1

   * - Parameter
     - Description
   * - ``<namespace>``
     - Namespace ID
   * - ``<query-handle>``
     - Handle obtained when the query was submitted or via a list of queries

.. rubric:: Comments

The query results can be downloaded only once. The RESTful API will return a Status Code ``409 Conflict`` 
if results for the ``query-handle`` are attempted to be downloaded again.

.. rubric:: HTTP Responses
.. list-table::
   :widths: 20 80
   :header-rows: 1

   * - Status Codes
     - Description
   * - ``200 OK``
     - The HTTP call was successful
   * - ``404 Not Found``
     - The query handle does not match any current query
   * - ``409 Conflict``
     - The query results were already downloaded


.. _http-restful-api-query-enable-disable:

Enabling and Disabling Querying
-------------------------------
Querying (or exploring) of datasets and streams can be enabled and disabled using these endpoints.

Exploration of data in CDAP is governed by a combination of enabling the CDAP Explore
Service and then creating datasets and streams that are explorable. The CDAP Explore
Service is enabled by a :ref:`setting in the CDAP configuration file
<appendix-cdap-default-explore-service>` (``explore.enabled`` in ``cdap-site.xml`` file).

Datasets and streams |---| that were created while the Explore Service was not enabled
|---| can, once the service is enabled and CDAP restarted, be enabled for exploration by
using these endpoints.

You can also use these endpoints to disable exploration of a specific dataset or
stream. The dataset or stream will still be accessible programmatically; it just won't
respond to queries or be available for exploration using the CDAP UI.

For datasets::

  POST <base-url>/namespaces/<namespace>/data/explore/datasets/<dataset-name>/enable
  POST <base-url>/namespaces/<namespace>/data/explore/datasets/<dataset-name>/disable

For streams::

  POST <base-url>/namespaces/<namespace>/data/explore/streams/<stream-name>/tables/<table-name>/enable
  POST <base-url>/namespaces/<namespace>/data/explore/streams/<stream-name>/tables/<table-name>/disable

Each of these endpoints returns a query handle that can be used to submit requests
tracking the :ref:`status of the query <http-restful-api-query-status>`.

.. list-table::
   :widths: 20 80
   :header-rows: 1

   * - Parameter
     - Description
   * - ``<namespace>``
     - Namespace ID
   * - ``<dataset-name>``
     - Name of the dataset
   * - ``<stream-name>``
     - Name of the stream
   * - ``<table-name>``
     - Name of the table
     
.. rubric:: HTTP Responses
.. list-table::
   :widths: 20 80
   :header-rows: 1

   * - Status Codes
     - Description
   * - ``200 OK``
     - The query execution was successfully initiated, and the body will contain the query-handle
       used to identify the query in subsequent requests
   * - ``404 Not Found``
     - The query is not well-formed or contains an error such as a nonexistent table name

.. rubric:: Comments

If the request was successful, the body will contain a query handle that can be used to
identify the query in subsequent requests, such as a :ref:`status request
<http-restful-api-query-status>`::

  { "handle":"<query-handle>" }

.. rubric:: Example
.. list-table::
   :widths: 20 80
   :stub-columns: 1

   * - HTTP Request
     - ``POST <base-url>/namespaces/default/data/explore/datasets/logEventStream_converted/disable``
   * - HTTP Response
     - ``{"handle":"57cf1b01-8dba-423a-a8b4-66cd29dd75e2"}``
   * - Description
     - Submits a request in the namespace *default* to disable the dataset
       *logEventStream_converted* from being explored. The handle can be used to check the
       status.
