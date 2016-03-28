# Copyright 2016 Huawei Technologies Co. Ltd.
#
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

"""
Collect the wanted data by using ceilometer client to send HTTP request to
ceilometer server
"""


# Import modules
from os import environ as env
import subprocess
import ceilometerclient.client
import json

# First source openrc * *; Otherwise, there is a error message.
source = 'source /opt/stack/devstack/openrc admin admin'
dumps = '/usr/bin/python -c '\
      + '"import os, json; print json.dumps(dict(os.environ))"'
command = ['/bin/bash', '-c', source + '&&' + dumps]
pipe = subprocess.Popen(command, stdout=subprocess.PIPE)
env = json.loads(pipe.stdout.read())


# Create an authenticated ceilometer client
c_client = ceilometerclient.client.get_client(
    "2", os_username=env['OS_USERNAME'],
    os_password=env['OS_PASSWORD'],
    os_tenant_name=env['OS_TENANT_NAME'],
    os_auth_url=env['OS_AUTH_URL'])

resource_list = c_client.resources.list()
resource_id_list = []
print 'List resource ids based on query filter================='
for each in resource_list:
    print each.resource_id
    resource_id_list.append(each.resource_id)
print 'End==================\n'

# resource_id_list[0:3]: image id
# resource_id_list[3]: instance id
# resource_id_list[4:6]: disk id
# resource_id_list[6]: interface id
query_meter = [dict(field='resource_id', op='eq', value=resource_id_list[3])]
meter_list = c_client.meters.list(q=query_meter)
meter_name_list = []
print 'List meter names related with the resource id--', resource_id_list[3]
for each in meter_list:
    print each.name
    meter_name_list.append(each.name)
print 'End++++++++++++++++++++\n'

compute_instances = [
    'disk.read.requests.rate', 'disk.write.requests.rate',
    'disk.read.bytes.rate', 'disk.write.bytes.rate', 'cpu_util'
]

compute_instance_samples = []
for each in compute_instances:
    query = [
        dict(field='resource_id', op='eq', value=resource_id_list[3]),
        dict(field='timestamp', op='ge', value='2016-02-28T00:00:00'),
        dict(field='timestamp', op='lt', value='2016-02-29T00:00:00'),
        dict(field='meter', op='eq', value=each)
    ]
    compute_instance_samples.\
        append(c_client.new_samples.list(q=query, limit=1000))
#        append(c_client.samples.list(each, limit=1000))


fout = open('instance_samples.arff', 'w')
head_info = "% ARFF file for the collected instance samples" \
          + " with some numeric feature from ceilometer API. \n \n" \
          + "@relation    collected samples for VMs on host \n \n" \
          + "@attribute timestample     datetime       " \
          + " UTC date and time when the measurement was made  \n" \
          + "@attribute resource_id      unicode       " \
          + " The ID of the Resource for which the measurements are taken  \n" \
          + "@attribute disk.read.requests.rate     request/s         " \
          + "Average rate of read requests  \n" \
          + "@attribute disk.write.requests.rate     request/s         " \
          + "Average rate of write requests  \n" \
          + "@attribute disk.read.bytes.rate     B/s         " \
          + "Average rate of reads     \n" \
          + "@attribute disk.write.bytes.rate     B/s         " \
          + "Average rate of writes  \n" \
          + "@attribute cpu_util      %        Average CPU utilization\n \n"\
          + "@data \n \n"
fout.write(head_info)

count = 0
compute_instance_sample_value = []
for each in compute_instance_samples[1:]:
    each_sample_value = []
    for i in each:
        # each_sample_value.append(str(i.counter_volume))
        each_sample_value.append(str(i.volume))
    compute_instance_sample_value.append(each_sample_value)

for each in compute_instance_samples[0]:
    fout.write(each.timestamp + ', ' + each.resource_id)
    # fout.write(', ' + str(each.counter_volume))
    fout.write(', ' + str(each.volume))
    for i in compute_instance_sample_value:
        fout.write(', ' + i[count])
    fout.write('\n')
    count += 1


fout.close()
print count
