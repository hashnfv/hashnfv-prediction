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
import subprocess
import ceilometerclient.client
import json
import readline  # It automatically wraps studin

# First source openrc * *; Otherwise, there is a error message.
print ("source your openrc file, for example, "
       "source /opt/stack/devstack/openrc admin admin")
source = raw_input(">  ")
dumps = ('/usr/bin/python -c '
         '"import os, json; print json.dumps(dict(os.environ))"')
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
print '\nList all resource ids which can be measured:'
index = 0
for each in resource_list:
    print "\t* resource_id_list[%d]: " % index, each.resource_id
    index += 1
    resource_id_list.append(each.resource_id)
print 'End', '=' * 15

print ("\nThe following shows resources corresponding to resource ids: \n"
       "\t* resource_id_list[0:3] represents image ids \n"
       "\t* resource_id_list[3] represents instance ids \n"
       "\t* resource_id_list[4:6] represents disk ids \n"
       "\t* resource_id_list[6] represents interface ids \n"
       "So you can collect what kind of data you like. Just type the number "
       "from 0 to 6")
input_number = int(raw_input("> Please type the number: "))
resource_id = resource_id_list[input_number]

query_meter = [dict(field='resource_id', op='eq', value=resource_id)]
meter_list = c_client.meters.list(q=query_meter)
meter_name_list = []
print '\nList meter names related with the resource id--' + resource_id + ":"
for each in meter_list:
    print "\t-", each.name
    meter_name_list.append(each.name)
print 'End', '+' * 15

print ("\nYou can collect whatever meters you like just by typing the meter "
       "names and using ',' as the separator,\n e.g., disk.read.requests.rate, "
       "disk.write.requests.rate, disk.read.bytes.rate, "
       "disk.write.bytes.rate, cpu_util.")

try:
    input_meters = raw_input(">  ")
    collect_meters = input_meters.split(",")
    collect_meters = [meter.strip() for meter in collect_meters]
except EOFError:
    print "\n Good Bye! Welcome again next time!"


collect_meter_samples = []
print ("At the same time, you need to specify the beginning time and the "
       "end time for the collection. \nThe time format is fixed, "
       "e.g., 2016-02-28T00:00:00. ")
begin_time = raw_input("> Please input the beginning time: ")
end_time = raw_input("> Please input the end time: ")
for each in collect_meters:
    query = [
        dict(field='resource_id', op='eq', value=resource_id),
        dict(field='timestamp', op='ge', value=begin_time),
        dict(field='timestamp', op='lt', value=end_time),
        dict(field='meter', op='eq', value=each)
    ]
#    (collect_meter_samples.
#        append(c_client.new_samples.list(q=query, limit=1000)))
    jsonData = c_client.new_samples.list(q=query, limit=1000)
    collect_meter_samples.append(i for i in jsonData)
#        append(c_client.samples.list(q=query, limit=1000)))

fout = open('collectMeterSamples.arff', 'w')
head_info = ("% ARFF file for the collected meter sample"
             " with some numeric feature from ceilometer API. \n \n"
             "@relation    collected samples for VMs on host \n \n"
             "@attribute timestample   \n"
             "@attribute resource id   \n ")

for each in collect_meters:
    head_info = head_info + "@attribute " + each + "\n"

head_info = head_info + "@data \n \n"
fout.write(head_info)

count = 0
collect_meter_sample_values = []
for each in collect_meter_samples[1:]:
    each_sample_value = []
    for i in each:
        # each_sample_value.append(str(i.counter_volume))
        each_sample_value.append(str(i.volume))
    collect_meter_sample_values.append(each_sample_value)

for each in collect_meter_samples[0]:
    fout.write(each.timestamp + ', ' + each.resource_id)
    # fout.write(', ' + str(each.counter_volume))
    fout.write(', ' + str(each.volume))
    for i in collect_meter_sample_values:
        fout.write(', ' + i[count])
    fout.write('\n')
    count += 1

fout.close()
print ("\nGreat! Collection is done. \n%d rows of meter samples have been "
       "written in the file named 'collectMeterSamples.arff' in your current"
       " directory. \nPlease check!" % count)
