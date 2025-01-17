.. meta::
    :author: Cask Data, Inc.
    :copyright: Copyright © 2016 Cask Data, Inc.

.. _admin-upgrading-packages:

=============================
Upgrading CDAP using Packages
=============================

.. _admin-upgrading-packages-upgrading-cdap:

Upgrading CDAP
==============
When upgrading an existing CDAP installation from a previous version, you will need
to make sure the CDAP table definitions in HBase are up-to-date.

These steps will stop CDAP, update the installation, run an upgrade tool for the table definitions,
and then restart CDAP.

**These steps will upgrade from CDAP** |bold-previous-short-version|\ **.x to**
|bold-version|\ **.** If you are on an earlier version of CDAP, please follow the
upgrade instructions for the earlier versions and upgrade first to
|previous-short-version|\.x before proceeding.

.. highlight:: console

1. Stop all flows, services, and other programs in all your applications.

#. Stop all CDAP processes::

     $ for i in `ls /etc/init.d/ | grep cdap` ; do sudo service $i stop ; done

#. Update the CDAP repository definition by running either of these methods:
 
   - On RPM using Yum:

     .. include:: ../_includes/installation/installation.txt 
        :start-after: Download the Cask Yum repo definition file:
        :end-before:  .. end_install-rpm-using-yum

   - On Debian using APT:

     .. include:: ../_includes/installation/installation.txt 
        :start-after: Download the Cask APT repo definition file:
        :end-before:  .. end_install-debian-using-apt

#. Update the CDAP packages by running either of these methods:

   - On RPM using Yum (on one line)::

       $ sudo yum install cdap cdap-gateway \
             cdap-hbase-compat-0.96 cdap-hbase-compat-0.98 cdap-hbase-compat-1.0 \
             cdap-hbase-compat-1.0-cdh cdap-hbase-compat-1.1 \
             cdap-kafka cdap-master cdap-security cdap-ui

   - On Debian using APT (on one line)::

       $ sudo apt-get install cdap cdap-gateway \
             cdap-hbase-compat-0.96 cdap-hbase-compat-0.98 cdap-hbase-compat-1.0 \
             cdap-hbase-compat-1.0-cdh cdap-hbase-compat-1.1 \
             cdap-kafka cdap-master cdap-security cdap-ui

#. If you are upgrading a secure Hadoop cluster, you should authenticate with ``kinit``
   as the user that runs CDAP Master (the CDAP user)
   before the next step (the running of the upgrade tool)::

     $ kinit -kt <keytab> <principal>

#. Run the upgrade tool, as the user that runs CDAP Master (the CDAP user)::

     $ /opt/cdap/master/bin/svc-master run co.cask.cdap.data.tools.UpgradeTool upgrade
     
   Note that once you have upgraded an instance of CDAP, you cannot reverse the process; down-grades
   to a previous version are not possible.
   
   The Upgrade Tool will produce output similar to the following, prompting you to continue with the upgrade:
   
    .. container:: highlight

      .. parsed-literal::    
    
        UpgradeTool - version |version|-<build timestamp>.

        upgrade - Upgrades CDAP to |version|
          The upgrade tool upgrades the following:
          1. User Datasets
              - Upgrades the coprocessor jars for tables
              - Migrates the metadata for PartitionedFileSets
          2. System Datasets
          3. UsageRegistry Dataset Type
          Note: Once you run the upgrade tool you cannot rollback to the previous version.
        Do you want to continue (y/n)
        y
        Starting upgrade ...

   You can run the tool in a non-interactive fashion by using the ``force`` flag, in which case
   it will run unattended and not prompt for continuing::
   
     $ /opt/cdap/master/bin/svc-master run co.cask.cdap.data.tools.UpgradeTool upgrade force
     
#. To upgrade existing ETL applications created using the 3.2.x versions of ``cdap-etl-batch`` or 
   ``cdap-etl-realtime``, there are :ref:`separate instructions on doing so <cdap-apps-etl-upgrade>`.

#. Restart the CDAP processes::

     $ for i in `ls /etc/init.d/ | grep cdap` ; do sudo service $i start ; done


.. _admin-upgrading-packages-upgrading-hadoop:

Upgrading Hadoop
================
These tables list different versions of CDAP and the Hadoop distributions for which they are
supported. If your particular distribution is not listed here, you can determine its
components and from that determine which version of CDAP may be compatible. `Our blog
lists <http://blog.cask.co/2015/06/hadoop-components-versions-in-distros-matrix/>`__ the
different components of the common Hadoop distributions.

.. CDH
.. ---
.. include:: /installation/cloudera.rst
    :start-after: .. _cloudera-compatibility-matrix:
    :end-before: .. _cloudera-compatibility-matrix-end:

+----------------+-------------------------------+
|                |                               |
+----------------+-------------------------------+

.. HDP
.. ---
.. include:: /installation/ambari.rst
    :start-after: .. _ambari-compatibility-matrix:
    :end-before: .. _ambari-compatibility-matrix-end:

+----------------+-------------------------------+
|                |                               |
+----------------+-------------------------------+

.. MapR
.. ----
.. include:: /installation/mapr.rst
    :start-after: .. _mapr-compatibility-matrix:
    :end-before: .. _mapr-compatibility-matrix-end:


Upgrade Steps
-------------
These steps cover what to do when upgrading the version of Hadoop of an existing CDAP installation.
As the different versions of Hadoop can use different versions of HBase, upgrading from
one version to the next can require that the HBase coprocessors be upgraded to the correct
version. The steps below will, if required, update the coprocessors appropriately.

**It is important to perform these steps as described, otherwise the coprocessors may not
get upgraded correctly and HBase regionservers may crash.**

1. Upgrade CDAP to a version that will support the new Hadoop version, following the usual
   :ref:`CDAP upgrade procedure for packages <admin-upgrading-packages-upgrading-cdap>`. 

#. After upgrading CDAP, start CDAP and check that it is working correctly.

#. Stop all CDAP applications and services::
   
    $ for i in `ls /etc/init.d/ | grep cdap` ; do sudo service $i stop ; done

#. Disable all CDAP tables; from an HBase shell, run the command::

    > disable_all 'cdap.*'
    
#. Upgrade to the new version of Hadoop.

#. Run the *Post-Hadoop Upgrade Tasks* |---| to upgrade CDAP for the new version of Hadoop |---| by running
   the *CDAP Upgrade Tool*, as the user that runs CDAP Master (the CDAP user)::

    $ /opt/cdap/master/bin/svc-master run co.cask.cdap.data.tools.UpgradeTool upgrade_hbase

#. Enable all CDAP tables; from an HBase shell, run this command::

    > enable_all 'cdap.*'
    
#. Restart CDAP::

    $ for i in `ls /etc/init.d/ | grep cdap` ; do sudo service $i start ; done

