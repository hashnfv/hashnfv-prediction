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

