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

import org.apache.log4j.*;

import weka.classifiers.bayes.*;
import weka.classifiers.trees.*;
import weka.classifiers.rules.*;
import weka.classifiers.functions.*;
import weka.classifiers.functions.LibSVM;
import weka.classifiers.lazy.*;

import predictor.PredictorInterface;

public class PredictorFactory {
	public static Logger logger = Logger.getLogger(PredictorFactory.class);
	
	public static enum PredictionTechnique {
		NAIVE_BAYES ("NBC", "Naive Bayes Classifier"),
		BAYES_NET ("BN", "Bayesian Network"),
		M5P ("M5P", "M5P Decision Tree"),
		J48 ("J48", "C4.5 Decision Tree"),
		DT ("DT", "Decision Table"),
		ZEROR ("ZEROR", "ZeroR"),
		REPTREE ("REPTREE", "REPTree"),
		SMO ("SMO", "Sequential Minimal Optimization"),
		RBFN ("RBFN", "RBF Network"),
		MP ("MP", "Multilayer Perceptron"),
		SLR ("SLR", "Simple Linear Regression"),
		SL ("SL", "Simple Logistic"),
		SVM ("SVM", "Support Vector Machine"),
		LOG ("LOG", "Logistic"),
		SGD ("SGD", "Stochastic Gradient Descent"),
		VP ("VP", "VotedPerceptron"),
		SMOR ("SMOR", "Sequential Minimal Optimization Regression"),
		KSTAR ("KSTAR", "KStar"),
		LWL ("LWL", "Locally weighted learning"),
		RF ("RF", "Random Forest"),
		NBM ("NBM", "Naive Bayes Multinomial"),
		IBK ("IBK", "Instance-based Learning"),
		JRIP ("JRIP", "JRip"),
		M5R ("M5R", "M5Rules"),
		ONER ("ONER", "OneR"),
		PART ("PART", "PART"),
		;
		
		private final String shortName;
		private final String name;
		
		PredictionTechnique(String shortName, String name) {
			this.shortName = shortName;
			this.name = name;
		}
		
		public String getShortName() {
			return this.shortName;
		}
		
		public String getName() {
			return this.name;
		}
	}
	
	public static PredictorInterface createPredictor(PredictionTechnique pTechnique) {
		String name = pTechnique.getName();
		logger.debug("Creating predictor: " + name);
		
		switch(pTechnique) {
		case NAIVE_BAYES:	return new ClassifierAdapter(new NaiveBayes(),name);
		case BAYES_NET:		return new ClassifierAdapter(new BayesNet(),name);
		case M5P:			return new ClassifierAdapter(new M5P(), name);
		case J48:			return new ClassifierAdapter(new J48(), name);
		case DT:			return new ClassifierAdapter(new DecisionTable(), name);
		case ZEROR:			return new ClassifierAdapter(new ZeroR(), name);
		case REPTREE:		return new ClassifierAdapter(new REPTree(), name);
		case SMO:			return new ClassifierAdapter(new SMO(), name);
		//case RBFN:			return new ClassifierAdapter(new RBFNetwork(), name);
		case MP:			return new ClassifierAdapter(new MultilayerPerceptron(), name);
		case SLR:			return new ClassifierAdapter(new SimpleLinearRegression(), name);
		case SL:			return new ClassifierAdapter(new SimpleLogistic(), name);
		case SVM:			return new ClassifierAdapter(new LibSVM(), name);
		case LOG:			return new ClassifierAdapter(new Logistic(), name);
		//case SGD:			return new ClassifierAdapter(new SGD(), name);
		case VP:			return new ClassifierAdapter(new VotedPerceptron(), name);
		case SMOR:			return new ClassifierAdapter(new SMOreg(), name);
		case KSTAR:			return new ClassifierAdapter(new KStar(), name);
		case LWL:			return new ClassifierAdapter(new LWL(), name);
		case RF:			return new ClassifierAdapter(new RandomForest(), name);
		case NBM:			return new ClassifierAdapter(new NaiveBayesMultinomial(), name);
		case IBK:			return new ClassifierAdapter(new IBk(), name);
		case JRIP:			return new ClassifierAdapter(new JRip(), name);
		case M5R:			return new ClassifierAdapter(new M5Rules(), name);
		case ONER:			return new ClassifierAdapter(new OneR(), name);
		case PART:			return new ClassifierAdapter(new PART(), name);
		default:			return new ClassifierAdapter(new NaiveBayes(),name);
		}
	}
}

