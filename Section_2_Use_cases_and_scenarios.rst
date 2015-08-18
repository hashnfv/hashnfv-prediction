2 Use cases and scenarios
=========================

Telecom services often have high available requirements. Failure prediction is one of the importance features for high available requirements. Operator can handle faults in advance based on failure prediction. This project focuses on data collection of failure prediction.

The data collector consists of Ceilometer and Monasca which can be extended to plugin some other open source data collectors, e.g. Zabbix, Nagios, Cacti. Based on real-time analytics techniques and machine learning techniques, the failure predictor analyses the data gathered by the data collector to automatically determine whether a failure will happen. If a failure is judged, then the failure predictor sends failure notifications to the failure management module (e.g. the Doctor module), which could handle these notifications.

Use case 1
----------

Based on infrastructure metrics, it is possible to predict failure of infrastructure, e.g. Nova, Neutron, MQ.

Use case 2
----------

Based on metrics of infrastructure and VM inside, it is possible to predict failure of VNF.

**Documentation tracking**

Revision: _sha1

Build date:  _date

