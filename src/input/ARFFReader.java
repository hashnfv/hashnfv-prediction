/**
Copyright 2016 Huawei Technologies Co. Ltd.

Licensed to the Apache Software Foundation (ASF) under one or more
contributor license agreements.  See the NOTICE file distributed with
this work for additional information regarding copyright ownership.
The ASF licenses this file to You under the Apache License, Version 2.0
(the "License"); you may not use this file except in compliance with
the License.  You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
**/
package input;

import java.io.File;

import org.apache.log4j.*;

import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;



public class ARFFReader {
	protected static Logger logger = Logger.getLogger(ARFFReader.class);
	
	public static Instances read(String path) throws Exception {
		Instances instances = null;
		
		if (path.length() == 0) {
			logger.error("Empty file path");
			throw new Exception("Empty file path");
		}
		if (!(new File(path)).exists()) {
			logger.error("File not found");
			throw new Exception("File not found");
		}
		try {
			DataSource source = new DataSource(path);
			instances = source.getDataSet();
			if (instances.classIndex() == -1) {
				instances.setClassIndex(instances.numAttributes() - 1);
			}
		} catch (Exception e) {
			logger.error(e.toString());
			throw e;
		}

		return instances;
	}
}
