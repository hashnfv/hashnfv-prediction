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
package predictor;

import java.util.Random;

import org.apache.log4j.*;

import weka.core.*;
import weka.classifiers.*;

public class ClassifierAdapter implements PredictorInterface {
	protected Logger logger = Logger.getLogger(ClassifierAdapter.class);

	protected String name;
	protected Classifier classifier;
	protected Evaluation eval;

	public ClassifierAdapter(Classifier classifier, String name) {
		this.classifier = classifier;
		this.name = name;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public void crossValidate(Instances instances, int numFold, Random rand) throws Exception {
		eval = new Evaluation(instances);
		eval.crossValidateModel(this.classifier, instances, numFold, rand);
	}

	public String getEvaluationSummaryString() {
		return this.eval.toSummaryString();
	}

	public String getEvaluationMatrixString() throws Exception {
		return this.eval.toMatrixString();
	}
	
	public FastVector getEvaluationPredictions() {
		return eval.predictions();
	}

	@Override
	public void train(Instances instances) throws Exception {
		this.classifier.buildClassifier(instances);
	}
	
	@Override
	public int predict(Instance instance) throws Exception {
		this.classifier.classifyInstance(instance);
		return 0;
	}

	@Override
	public int predict(Instances instances) throws Exception {
		for (int i=0;i<instances.numInstances();i++) {
			this.predict(instances.instance(i));
		}
		return 0;
	}
	
	@Override
	public String getEvaluationResults() throws Exception {
		String results = "\n-- ";
		results += this.getName();
		results += " --\n";
		results += this.eval.toSummaryString();
		results += this.eval.toMatrixString();
		results += this.eval.toClassDetailsString();
		results += "\n";
		return results;
	}
}

