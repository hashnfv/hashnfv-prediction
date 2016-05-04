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
package experiments;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import weka.filters.Filter;
import model.Model;
import org.apache.log4j.Logger;
import weka.core.Instances;
import weka.core.OptionHandler;

/**
 * Created by hailiu on 2016/2/22.
 */
public class RunPredictionServer {
    protected Logger logger = Logger.getLogger(RunPredictionServer.class);

    final String configFile = "data/config.txt";//input file path
    String[] option = new String[50];
    String filter;
    ArrayList filterList;
    String DataFile;
    protected static DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
    protected static Date date = new Date();
    protected static String resultFilename = dateFormat.format(date)+"_results";
    Map map;

    public RunPredictionServer() {
        this.filterList = new ArrayList();
        setClassName();
    }

    public static void main(String[] args)
    {
        RunPredictionServer runServer = new RunPredictionServer();
        //read config file
        runServer.loadConfigFile();
        Model tempModel = new Model();
        tempModel.loadTrainingData(runServer.DataFile);
        tempModel.addPredictor("ZEROR");
        tempModel.addPredictor("PART");
        tempModel.addPredictor("ONER");
        tempModel.addPredictor("JRIP");
        tempModel.addPredictor("IBK");
        tempModel.addPredictor("NBM");
        tempModel.addPredictor("RF");
        tempModel.addPredictor("LWL");
        tempModel.addPredictor("NBC");
        tempModel.addPredictor("BN");
        tempModel.addPredictor("REPTREE");
        tempModel.addPredictor("DT");
        tempModel.addPredictor("J48");
        tempModel.addPredictor("SMO");
        tempModel.addPredictor("MP");
        tempModel.addPredictor("SL");
        tempModel.addPredictor("LOG");
        tempModel.addPredictor("SGD");
        tempModel.addPredictor("VP");
        tempModel.addPredictor("SVM");
        tempModel.addPredictor("KSTAR");

        Instances tempInstances=tempModel.getTrainingInstances();

        Iterator ite=runServer.filterList.iterator();

        while (ite.hasNext())
        {
            ArrayList filterOption=(ArrayList) ite.next();
            Iterator tempIte = filterOption.iterator();
            String tFilter=null;
            if (tempIte.hasNext())
            {
                tFilter=(String) tempIte.next();
            }
            String[] option = new String[filterOption.size()-1];
            int i=0;
            while (tempIte.hasNext())
            {
                option[i]=(String) tempIte.next();
                i++;
            }
            tempInstances = runServer.addFilter(tempInstances,tFilter,option);
        }

        //tempInstances.setClassIndex(tempInstances.numAttributes()-2);
        tempModel.setPreprocessedInstances(tempInstances);
        tempModel.savePreprocessedInstances("preprocessed.arff");

        try {
            tempModel.benchmark(2,resultFilename);

        }catch (Exception e)
        {
            runServer.logger.warn(e.toString());
        }

        tempModel.crossValidatePredictors(10);
        try {
            tempModel.saveResults(resultFilename);
        }catch (Exception e)
        {
            runServer.logger.warn(e.toString());
        }
    }

    //read config file
    private void loadConfigFile() {
        logger.debug("Reading configure from " + configFile);
        try {
            BufferedReader in = new BufferedReader(new FileReader(configFile));
            DataFile = in.readLine();
            System.out.println(DataFile);

            String tempStr=null;
            while((tempStr=in.readLine())!=null)
            {
                String[] tempString = tempStr.split(" ");
                ArrayList<String> filterOption = new ArrayList<>();
                for(String ts:tempString)
                {
                    filterOption.add(ts);
                }
                this.filterList.add(filterOption);
            }
        } catch (Exception e) {
            logger.warn(e.toString());
        }
    }

    private Instances addFilter(Instances tempInstances,String filter, String[] option)
    {
        Class<?> tempClass=null;
        try {
            tempClass = Class.forName(this.map.get(filter).toString());
        }catch (ClassNotFoundException e)
        {
            logger.warn(e.toString());
        }

        if(tempClass==null) return null;
        Object tempFilter=null;
        try {
            tempFilter = tempClass.newInstance();
        }catch (InstantiationException e)
        {
            logger.warn(e.toString());
        }
        catch (IllegalAccessException e)
        {
            logger.warn(e.toString());
        }

        try {
            ((OptionHandler)tempFilter).setOptions(option);
            ((Filter)tempFilter).setInputFormat(tempInstances);
        }catch (Exception e)
        {
            logger.warn(e.toString());
        }

        Instances newInstances=null;
        try {
            newInstances = Filter.useFilter(tempInstances, (Filter) (tempFilter));
        }catch (Exception e)
        {
            logger.warn(e.toString());
        }
        return newInstances;
    }

    private void setClassName()
    {
        map=new HashMap();
        map.put("addexpression","weka.filters.unsupervised.attribute.AddExpression");
        map.put("remove","weka.filters.unsupervised.attribute.Remove");
        map.put("classassigner","weka.filters.unsupervised.attribute.ClassAssigner");
        map.put("numerictonominal","weka.filters.unsupervised.attribute.NumericToNominal");
        map.put("mathexpression","weka.filters.unsupervised.attribute.MathExpression");
    }
}

