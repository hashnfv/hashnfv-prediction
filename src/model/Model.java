package model;

import java.io.*;
import java.util.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.apache.log4j.*;

import org.apache.commons.math3.stat.descriptive.moment.*;
import org.apache.commons.math3.distribution.NormalDistribution;

import weka.core.*;
import weka.classifiers.evaluation.*;
import weka.core.converters.*;

import input.ARFFReader;
import predictor.*;


public class Model implements ModelInterface {
	protected Logger logger = Logger.getLogger(Model.class);
	
	protected String datapath;
	protected Instances trainingInstances;
	protected Instances preprocessedInstances;
	protected ArrayList<PredictorInterface> predictors;

	protected static DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
	protected static Date date = new Date();
	protected static String resultFilename = dateFormat.format(date);

	public Model(){
		this.datapath = "";
		this.trainingInstances = null;
		this.preprocessedInstances = null;
		this.predictors = new ArrayList<PredictorInterface>();
	}

	public Model (ModelInterface model) {
		this.datapath = model.getDatapath();
		this.trainingInstances = new Instances(model.getTrainingInstances());
		this.preprocessedInstances = new Instances(model.getPreprocessedInstances());
		this.predictors = new ArrayList<PredictorInterface>(model.getPredictors());
	}

	@Override
	public Instances getPreprocessedInstances() {
		return this.preprocessedInstances;
	}

	@Override
	public Instances getTrainingInstances() {
		return this.trainingInstances;
	}


	@Override
	public String getDatapath() {
		return this.datapath;
	}


	@Override
	public ArrayList<PredictorInterface> getPredictors() {
		return this.predictors;
	}


	@Override
	public void loadTrainingData(String path) {
		logger.debug("Reading training data from " + path);
		this.datapath = path;
		try {
			trainingInstances = ARFFReader.read(path);
			preprocessedInstances = trainingInstances;
			logger.debug("Training data is read");
			logger.trace(trainingInstances);
		} catch (Exception e) {
			logger.warn(e.toString());
		}
	}

	@Override
	public void loadRawLog(String path) {
		logger.debug("Reading logfile from " + path);
		try {
			BufferedReader in = new BufferedReader(new FileReader(path));
			String line = in.readLine();
			System.out.println(line);
		} catch (Exception e) {
			logger.warn(e.toString());
		}
	}



	@Override
	public void setPreprocessedInstances(Instances instances){
		this.preprocessedInstances=new Instances(instances);
	}

	@Override
	public void savePreprocessedInstances(String path) {
		ArffSaver saver = new ArffSaver();
		saver.setInstances(this.preprocessedInstances);
		try {
			saver.setFile(new File(path));
			saver.writeBatch();
		} catch (Exception e) {
			logger.error("Cannot save preprocessed instances to file " + path);
		}
	}

	@Override
	public void addPredictor(String shortName) {
		for (PredictorFactory.PredictionTechnique pTechnique : PredictorFactory.PredictionTechnique.values()) {
			if (pTechnique.getShortName().equals(shortName)) {
				predictors.add(PredictorFactory.createPredictor(pTechnique));
				logger.info("Added predictor: " + pTechnique.getName());
				return;
			}
		}
		logger.warn("Added predictor: None");
		logger.warn(shortName + " is not in the list.");
	}

	@Override
	public void selectTrainingMethod() {

	}

	@Override
	public void trainPredictors() throws Exception{
		if (preprocessedInstances == null) {
			throw new Exception("No training data");
		}
		if (predictors.size() == 0) {
			throw new Exception("No predictors selected");
		}
		for (PredictorInterface p : predictors) {
			try {
				logger.info("Training " + p.getName());
				p.train(preprocessedInstances);
				logger.debug(p.toString());
			} catch (Exception e) {
				logger.error(e.toString());
			}
		}
	}

	@Override
	public void crossValidatePredictors(int numFold) {
		long seed = 1;
		this.crossValidatePredictors(numFold, seed);
	}

	@Override
	public void crossValidatePredictors(int numFold, long seed) {
		for (PredictorInterface p : predictors) {
			try {
				logger.debug(numFold + "-fold cross-validating: " + p.getName());
				Random rand = new Random(seed);
				p.crossValidate(preprocessedInstances, numFold, rand);

				///* 
				ThresholdCurve tc = new ThresholdCurve();
				int classIndex = 1;
				Instances result = tc.getCurve(p.getEvaluationPredictions(), classIndex);


				// Save ROC

				BufferedWriter br = new BufferedWriter(new FileWriter(resultFilename + "_" + p.getName().replace(" ", "_") + "_ROC.arff"));
				br.write(result.toString());
				br.close();
			} catch (Exception e) {
				logger.error(e.toString());
			}
		}
	}

	@Override
	public void benchmark(int rounds, String filename) throws Exception {
		ArrayList<ArrayList<Double>> trainingTime = new ArrayList<ArrayList<Double>>(this.predictors.size());
		ArrayList<ArrayList<Double>> predictionTime = new ArrayList<ArrayList<Double>>(this.predictors.size());

		Runtime runtime = Runtime.getRuntime();

		for (int i=0; i<this.predictors.size(); i++) {
			trainingTime.add(new ArrayList<Double>(rounds));
			predictionTime.add(new ArrayList<Double>(rounds));
		}

		// Benchmark - using preprocessed instances
		long startTime;
		long endTime;
		double elapsedTime;
		for (int pIndex=0; pIndex<this.predictors.size(); pIndex++) {
			for (int rIndex=0; rIndex<rounds; rIndex++) {
				logger.debug("Benchmarking " + this.predictors.get(pIndex).getName() + " round " + rIndex);

				// Training time
				startTime = System.currentTimeMillis();
				this.predictors.get(pIndex).train(this.preprocessedInstances);
				endTime = System.currentTimeMillis();
				elapsedTime = ((double)(endTime - startTime))/1000;
				trainingTime.get(pIndex).add(elapsedTime);
				logger.debug("Training time = " + elapsedTime + " seconds");

				// Prediction time
				startTime = System.currentTimeMillis();
				this.predictors.get(pIndex).predict(this.preprocessedInstances);
				endTime = System.currentTimeMillis();
				elapsedTime = ((double)(endTime - startTime))/1000;
				predictionTime.get(pIndex).add(elapsedTime);
				logger.debug("Prediction time = " + elapsedTime + " seconds");
			}
		}

		// Save time results to file
		BufferedWriter br = new BufferedWriter(new FileWriter(filename+"_benchmark"));
		// Write header
		String header = "";
		for (int pIndex=0; pIndex<this.predictors.size(); pIndex++) {
			header += "\"" + this.predictors.get(pIndex).getName() + " Training\" ";
			header += "\"" + this.predictors.get(pIndex).getName() + " Prediction\" ";
		}
		header += "\n";
		br.write(header);

		for (int rIndex=0; rIndex<rounds; rIndex++) {
			String line = "";
			for (int pIndex=0; pIndex<this.predictors.size(); pIndex++) {
				line += trainingTime.get(pIndex).get(rIndex).toString() + " " + predictionTime.get(pIndex).get(rIndex).toString() + " ";
			}
			line += "\n";
			br.write(line);
		}
		br.close();

		// TODO: move to another function
		// refactor
		// Calculate and save summary results
		BufferedWriter brSummary = new BufferedWriter(new FileWriter(filename+"_benchmark_time_summary"));
		//header = "Algorithms tMean tError pMean pError\n";
		//brSummary.write(header);
		for (int pIndex=0; pIndex<this.predictors.size(); pIndex++) {
			String line = "\"" + this.predictors.get(pIndex).getName() + "\" ";
			// Calculate mean
			double [] training = new double[rounds];
			// Convert ArrayList to array for Mean.evaluate
			for (int rIndex=0; rIndex<rounds; rIndex++) {
				//System.out.println("pIndex = " + pIndex + " rIndex = " + rIndex);
				//System.out.println("array size = " + training.length);
				training[rIndex] = trainingTime.get(pIndex).get(rIndex);
			}
			double meanTraining = new Mean().evaluate(training);
			double varTraining = new Variance().evaluate(training);
			double stdTraining = Math.sqrt(varTraining);
			double errorTraining = new NormalDistribution().inverseCumulativeProbability(0.975)*(stdTraining/Math.sqrt(rounds));
			double lowerCITraining = meanTraining - errorTraining;
			double upperCITraining = meanTraining + errorTraining;
			line += meanTraining + " " + errorTraining + " ";
			//brSummary.write(line);

			//line = "\"" + this.predictors.get(pIndex).getName() + " Prediction\" ";
			// Calculate mean
			double [] prediction = new double[rounds];
			// Convert ArrayList to array for Mean.evaluate
			for (int rIndex=0; rIndex<rounds; rIndex++) {
				prediction[rIndex] = predictionTime.get(pIndex).get(rIndex);
			}
			double meanPrediction = new Mean().evaluate(prediction);
			double varPrediction = new Variance().evaluate(prediction);
			double stdPrediction = Math.sqrt(varPrediction);
			double errorPrediction = new NormalDistribution().inverseCumulativeProbability(0.975)*(stdPrediction/Math.sqrt(rounds));
			double lowerCIPrediction = meanPrediction - errorPrediction;
			double upperCIPrediction = meanPrediction + errorPrediction;
			line += meanPrediction + " " + errorPrediction + "\n";
			brSummary.write(line);
		}
		brSummary.close();
	}


	@Override
	public String getPredictorNames() {
		String str = "";
		for (PredictorInterface p : this.predictors) {
			str += p.getName() + "\n";
		}
		return str;
	}

	@Override
	public String toString() {
		String str = "";
		str += "Training data:\n" + this.datapath + "\n";
		str += "Training data summary:\n" + this.getTrainingInstances().toSummaryString() + "\n";
		str += "Preprocessed data summary:\n" + this.getPreprocessedInstances().toSummaryString() + "\n";
		str += "Predictors:\n" + this.getPredictorNames() + "\n";
		return str;
	}

	@Override
	public void saveSettings(String filename) throws Exception {
		BufferedWriter br = new BufferedWriter(new FileWriter(filename,true));
		br.write(this.toString());
		br.close();
	}

	@Override
	public void saveResults(String filename) throws Exception {
		BufferedWriter br = new BufferedWriter(new FileWriter(filename,true));

		for (PredictorInterface p : this.predictors) {
			logger.info(p.getEvaluationResults());
			br.write(p.getEvaluationResults());
		}

		br.close();
	}
}

