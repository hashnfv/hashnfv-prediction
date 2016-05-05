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

package model;

import java.util.ArrayList;

import weka.core.Instances;

import predictor.*;


public interface ModelInterface {
	public String getDatapath();
	public ArrayList<PredictorInterface> getPredictors();
	
	public void loadTrainingData(String path);
	public void loadRawLog(String path);
	public Instances getTrainingInstances();
	public void setPreprocessedInstances(Instances instances);
	public Instances getPreprocessedInstances();
	public void savePreprocessedInstances(String path);
	public void addPredictor(String shortName);
	public void crossValidatePredictors(int numFold);
	public void crossValidatePredictors(int numFold, long seed);
	public void selectTrainingMethod();
	public void trainPredictors() throws Exception;
	public void benchmark(int rounds, String filename) throws Exception;

	public String getPredictorNames();
	public String toString();
	public void saveSettings(String filename) throws Exception;
	public void saveResults(String filename) throws Exception;
}

