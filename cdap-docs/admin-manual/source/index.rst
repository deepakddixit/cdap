.. meta::
    :author: Cask Data, Inc.
    :copyright: Copyright © 2014-2016 Cask Data, Inc.

.. _admin-index:

==========================
CDAP Administration Manual
==========================

Covers putting CDAP into production, with **components, system requirements, deployment
architectures, Hadoop compatibility, installation, configuration, security setup, and
operations.** Appendices describe the **XML files** used to configure the CDAP
installation and its security configuration.


.. |cdap-components| replace:: **CDAP Components**
.. _cdap-components: cdap-components.html

- |cdap-components|_


.. |deployment-architectures| replace:: **Deployment Architectures:**
.. _deployment-architectures: deployment-architectures.html

- |deployment-architectures|_ **Minimal** and **high availability, highly scalable** deployments.


.. |hadoop-compatibility| replace:: **Hadoop Compatibility:**
.. _hadoop-compatibility: hadoop-compatibility.html

- |hadoop-compatibility|_ The **Hadoop/HBase environment** that CDAP requires.


.. |system-requirements| replace:: **System Requirements:**
.. _system-requirements: system-requirements.html

- |system-requirements|_ Hardware, memory, core, and network **requirements** and software **prerequisites**.


.. |installation| replace:: **Installation:**
.. _installation: installation/index.html

- |installation|_ Installation and configuration instructions for either **specific
  distributions** using a distribution manager or **generic Apache Hadoop** clusters using
  RPM or Debian Package Managers:

    .. |cloudera| replace:: **Cloudera Manager:**
    .. _cloudera: installation/cloudera.html

    - |cloudera|_ Installing on `CDH (Cloudera Data Hub) <http://www.cloudera.com/>`__ 
      clusters managed with `Cloudera Manager
      <http://www.cloudera.com/content/cloudera/en/products-and-services/cloudera-enterprise/cloudera-manager.html>`__.

    .. |ambari| replace:: **Apache Ambari:**
    .. _ambari: installation/ambari.html

    - |ambari|_ Installing on `HDP (Hortonworks Data Platform)
      <http://hortonworks.com/>`__ clusters managed with `Apache Ambari
      <https://ambari.apache.org/>`__.

    .. |mapr| replace:: **MapR:**
    .. _mapr: installation/mapr.html

    - |mapr|_ Installing on the `MapR Converged Data Platform <https://www.mapr.com>`__.

    .. |packages| replace:: **Manual Installation using Packages**
    .. _packages: installation/packages.html

    - |packages|_


.. |verification| replace:: **Verification:**
.. _verification: verification.html

- |verification|_ How to verify the CDAP installation on your Hadoop cluster by using an
  **example application** and **health checks**.


.. |upgrading| replace:: **Upgrading:**
.. _upgrading: upgrading/index.html

- |upgrading|_ Instructions for upgrading both CDAP and its underlying Hadoop distribution.


.. |security| replace:: **Security:**
.. _security: security.html

- |security|_ CDAP supports **securing clusters using a perimeter security model.** This
  section describes enabling security, configuring authentication, testing security, and 
  includes an example configuration file.


.. |operations| replace:: **Operations:**
.. _operations: operations/index.html

- |operations|_

    .. |logging| replace:: **Logging and Monitoring:**
    .. _logging: operations/logging.html

    - |logging|_ CDAP collects **logs** for all of its internal services and user
      applications; at the same time, CDAP can be **monitored through external systems**.
      Covers **log location**, **logging messages**, the **master services logback
      configuration** and **CDAP support for logging** through standard SLF4J (Simple
      Logging Facade for Java) APIs.

    .. |metrics| replace:: **Metrics:**
    .. _metrics: operations/metrics.html

    - |metrics|_ CDAP collects **metrics about the application’s behavior and performance**.
  
    .. |preferences| replace:: **Preferences and Runtime Arguments:**
    .. _preferences: operations/preferences.html

    - |preferences|_ Flows, MapReduce and Spark programs, services, workers, and workflows can receive **runtime arguments.**

    .. |scaling-instances| replace:: **Scaling Instances:**
    .. _scaling-instances: operations/scaling-instances.html

    - |scaling-instances|_ Covers **querying and setting the number of instances of flowlets and services.** 

    .. |resource-guarantees| replace:: **Resource Guarantees:**
    .. _resource-guarantees: operations/resource-guarantees.html

    - |resource-guarantees|_ Providing resource guarantees **for CDAP programs in YARN.**

    .. |tx-maintenance| replace:: **Transaction Service Maintenance:**
    .. _tx-maintenance: operations/tx-maintenance.html

    - |tx-maintenance|_ Periodic maintenance of the **Transaction Service.**

    .. |cdap-ui| replace:: **CDAP UI:**
    .. _cdap-ui: operations/cdap-ui.html

    - |cdap-ui|_ The CDAP UI is available for **deploying, querying, and managing CDAP.** 


.. |appendices| replace:: **Appendices:**
.. _appendices: appendices/index.html

- |appendices|_ Two XML files are used to configure a CDAP installation: ``cdap-site.xml`` and
  ``cdap-security.xml``:

  - :ref:`Minimal cdap-site.xml <appendix-minimal-cdap-site.xml>`
  - :ref:`Complete list of cdap-site.xml properties <appendix-cdap-site.xml>`
  - :ref:`List of cdap-security.xml properties <appendix-cdap-security.xml>`

