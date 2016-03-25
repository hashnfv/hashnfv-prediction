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
