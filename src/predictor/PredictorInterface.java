package predictor;

import java.util.Random;

import weka.core.*;

public interface PredictorInterface {
	public String getName();
	public void crossValidate(Instances instances, int numFold, Random rand) throws Exception;
	//public String getEvaluationSummaryString();
	//public String getEvaluationMatrixString() throws Exception;
	public FastVector getEvaluationPredictions();
	public void train(Instances instances) throws Exception;
	public int predict(Instance instance) throws Exception;
	public int predict(Instances instances) throws Exception;
	public String getEvaluationResults() throws Exception;
}

