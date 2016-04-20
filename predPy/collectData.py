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
import web
import subprocess
import ceilometerclient.client
import json
import readline  # It automatically wraps studin

urls = (
    '/', 'Index',
    '/meter', 'Meter',
    '/sample', 'Sample'
)

app = web.application(urls, globals())

# Global variables
c_client = None
resource_id_list = []
resource_id = None

render = web.template.render('templates', base="layout")


class Index(object):
    def GET(self):
        return render.source()

    def POST(self):
        form = web.input(content=None)
        dumps = ('/usr/bin/python -c '
                 '"import os, json; print json.dumps(dict(os.environ))"')
        command = ['/bin/bash', '-c', form.content + '&&' + dumps]
        pipe = subprocess.Popen(command, stdout=subprocess.PIPE)
        env = json.loads(pipe.stdout.read())

        # Create an authenticated ceilometer client
        global c_client
        c_client = ceilometerclient.client.get_client(
            "2", os_username=env['OS_USERNAME'],
            os_password=env['OS_PASSWORD'],
            os_tenant_name=env['OS_TENANT_NAME'],
            os_auth_url=env['OS_AUTH_URL'])

        resource_list = c_client.resources.list()
        resourceInfo = open("templates/resourceInfo.html", "w")
        index = 0
        resourceInfo.write("<h2>All resource ids which can be measured "
                           "are listed as follows:</h2>")
        resourceInfo.write("<ol>\n")
        for each in resource_list:
            resourceInfo.write("  <li>resource_id_list[%d]: %s</li>\n" %
                               (index, each.resource_id))
            index += 1
            global resource_id_list
            resource_id_list.append(each.resource_id)
        resourceInfo.write("</ol>\n")
        resourceInfo.write(
            "<h2>The following shows resources corresponding to resource ids: "
            "</h2><ul>\n"
            "  <li>resource_id_list[0:3] represents image ids</li> \n"
            "  <li>resource_id_list[3] represents instance ids</li> \n"
            "  <li>resource_id_list[4:6] represents disk ids</li> \n"
            "  <li>resource_id_list[6] represents interface ids</li> \n"
            "</ul><h2>So you can collect what kind of data you like."
            "Just type the number from 0 to 6</h2>"
        )

        resourceInfo.write(
            '<form action="/meter" method="POST" height=29px> \n'
            '<input type="text" name="number" width=200px>\n'
            '<input type="submit" value="Submit Number" class="button">\n'
            '</form>'
        )
        resourceInfo.close()
        return render.resourceInfo()


class Meter(object):
    def POST(self):
        form = web.input(number=None)
        input_number = int(form.number)
        global resource_id
        resource_id = resource_id_list[input_number]
        query_meter = [dict(field='resource_id', op='eq', value=resource_id)]
        meter_list = c_client.meters.list(q=query_meter)
        meter_name_list = []
        metersInfo = open("templates/metersInfo.html", "w")
        metersInfo.write(
            '<h2>List meter names related with the resource id--%s as '
            'follows:</h2>\n' % resource_id)
        metersInfo.write("<ol>\n")
        for each in meter_list:
            metersInfo.write("  <li>%s</li>\n" % each.name)
            meter_name_list.append(each.name)
        metersInfo.write("</ol>\n")
        metersInfo.write(
            "\n<p>You can collect whatever meters you like just by typing "
            "the meter names and using ',' as the separator,\n e.g., "
            "disk.read.requests.rate, disk.write.requests.rate, "
            "disk.read.bytes.rate, disk.write.bytes.rate, cpu_util.</p>")
        metersInfo.write(
            "<p>At the same time, you need to specify the beginning time and "
            "the end time for the collection. \nThe time format is fixed, "
            "e.g., 2016-02-28T00:00:00.</p>")

        metersInfo.write(
            '<form action="/sample" method="POST" height=29px>\n'
            'Meters:<input type="text" name="sample" class="input-box">\n'
            '<br> \n'
            'Begin Time:<input type="text" name="begin_time" class="input-box">'
            '<br> \n'
            'End Time:<input type="text" name="end_time" class="input-box"> \n'
            '<br> \n'
            '<input type="submit" value="Submit" class="button">'
        )
        metersInfo.close()
        return render.metersInfo()


class Sample(object):
    def POST(self):
        form = web.input(sample=None, begin_time=None, end_time=None)
        input_meters = form.sample
        collect_meters = input_meters.split(",")
        collect_meters = [meter.strip() for meter in collect_meters]

        collect_meter_samples = []
        for each in collect_meters:
            query = [
                dict(field='resource_id', op='eq', value=resource_id),
                dict(field='timestamp', op='ge', value=form.begin_time),
                dict(field='timestamp', op='lt', value=form.end_time),
                dict(field='meter', op='eq', value=each)
            ]
            (collect_meter_samples.
                append(c_client.new_samples.list(q=query, limit=1000)))
        # append(c_client.samples.list(each, limit=1000)))
        fout = open('collectMeterSamples.arff', 'w')
        head_info = ("% ARFF file for the collected meter samples "
                     "with some numeric feature from ceilometer API. \n \n"
                     "@relation  collected samples for VMs on host \n \n"
                     "@attribute timestample \n"
                     "@attribute resource id \n")

        for each in collect_meters:
            head_info = head_info + "@attribute  %s \n" % each

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
        return render.meterSamples(count=count)


if __name__ == "__main__":
    app.run()
