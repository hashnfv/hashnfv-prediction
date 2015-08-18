4 Gap analysis in upstream projects
===================================

This section presents the findings of gaps on existing VIM platforms. The focus was to identify gaps based on the features and requirements specified in Section 3.3. The analysis work performed resulted in the identification of gaps of which are herein presented.

4.1 Monasca
-----------

Monasca is an open-source monitoring-as-a-service (MONaaS) solution that integrates with OpenStack. Even though it is still in its early days, it is the interest of the community that the platform be multi-tenant, highly scalable, performant and fault-tolerant. Companion with a streaming alarm engine and a notification engine, is a northbound REST API users can use to interact with Monasca. Hundreds of thousands of metrics per second can be processed [8].

4.1.1 Memory usage in HyperV
^^^^^^^^^^^^^^^^^^^^^^^^^^^^

* Type: 'missing'
* Description:

  + To-be

    - Monasca should collect memory usage in HyperV.

  + As-is:

    - Monasca does not support querying memory usage of HyperV.

  + Gap

    - Monasca does not support querying memory usage of HyperV.

4.1.2 Memory usage in Libvirt
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

* Type: 'missing'
* Description:

  + To-be

    - Monasca should collect memory usage in Libvirt.

  + As-is:

    - Monasca does not support querying memory usage of Libvirt.

  + Gap

    - Monasca does not support querying memory usage of Libvirt.

4.2 Heat
--------

Heat is an orchestration project of openstack. It can be used for deploy application. Its cfn-hup tool can monitor metadata in VM and notify to heat. But this function is poor now, we can extend cfn-hup to collect more information inside VM and make failure prediction.

4.2.1 Cfn-hup
^^^^^^^^^^^^^

* Type: 'monitoring issue'
* Description:

  + To-be

    - Cfn-hup is able to collect more metrics than metadata inside VM. These metrics can be used for failure prediction.

  + As-is:

    - Cfn-hup just can monitor metadata inside VM.

  + Gap

    - Cfn-hup should monitor more information inside VM.

**Documentation tracking**

Revision: _sha1

Build date:  _date

