package com.formulasearchengine.mathosphere.mlp;

import com.formulasearchengine.mathosphere.mlp.cli.MachineLearningDefinienExtractionConfig;
import com.formulasearchengine.mathosphere.mlp.contracts.JsonSerializerMapper;
import com.formulasearchengine.mathosphere.mlp.contracts.TextAnnotatorMapper;
import com.formulasearchengine.mathosphere.mlp.contracts.TextExtractorMapper;
import com.formulasearchengine.mathosphere.mlp.ml.EvaluationResult;
import com.formulasearchengine.mathosphere.mlp.ml.WekaLearner;
import com.formulasearchengine.mathosphere.mlp.pojos.ParsedWikiDocument;
import com.formulasearchengine.mathosphere.mlp.pojos.WikiDocumentOutput;
import com.formulasearchengine.mathosphere.mlp.text.SimpleFeatureExtractorMapper;
import com.formulasearchengine.mlp.evaluation.Evaluator;
import com.formulasearchengine.mlp.evaluation.pojo.GoldEntry;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.operators.DataSource;
import org.apache.flink.core.fs.FileSystem.WriteMode;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.*;

public class MachineLearningModelGenerator {

  public static void main(String[] args) throws Exception {
    MachineLearningDefinienExtractionConfig config = MachineLearningDefinienExtractionConfig.from(args);
    find(config);
  }

  public static void test() throws Exception {
    find(MachineLearningDefinienExtractionConfig.test());
  }

  public static void find(MachineLearningDefinienExtractionConfig config) throws Exception {
    if (config.getInstancesFile() == null) {
      //parse wikipedia (subset) and process afterwards
      ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
      env.setParallelism(config.getParallelism());
      DataSource<String> source = readWikiDump(config, env);
      DataSet<ParsedWikiDocument> documents = source.flatMap(new TextExtractorMapper())
        .map(new TextAnnotatorMapper(config));
      Logger.getRootLogger().setLevel(Level.ERROR);
      ArrayList<GoldEntry> gold = (new Evaluator()).readGoldEntries(new File(config.getGoldFile()));
      DataSet<WikiDocumentOutput> instances = documents.map(new SimpleFeatureExtractorMapper(config, gold));
      //process parsed wikipedia
      DataSet<EvaluationResult> result = instances.reduceGroup(new WekaLearner(config));
      //write to kick off flink execution
      result.map(new JsonSerializerMapper<>())
        .writeAsText(config.getOutputDir() + "\\tmp", WriteMode.OVERWRITE);
      env.execute();
    } else {
      //just process
      findFromInstances(config);
    }
  }

  public static List<EvaluationResult> findFromInstances(MachineLearningDefinienExtractionConfig config) throws Exception {
    WekaLearner wekaLearner = new WekaLearner(config);
    return wekaLearner.processFromInstances();
  }

  public static DataSource<String> readWikiDump(MachineLearningDefinienExtractionConfig config, ExecutionEnvironment env) {
    return FlinkMlpRelationFinder.readWikiDump(config, env);
  }

  private static void generateSatuationData() throws Exception {
    MachineLearningDefinienExtractionConfig config = MachineLearningDefinienExtractionConfig.test();
    config.setPercent(Arrays.asList(new Double[]{10d, 20d, 30d, 40d, 50d, 60d, 70d, 80d, 90d, 100d}));
    find(config);
  }

  private static void generateCoraseParameterGrid() throws Exception {
    MachineLearningDefinienExtractionConfig config = MachineLearningDefinienExtractionConfig.test();
    config.setSvmCost(Arrays.asList(WekaLearner.C_coarse));
    config.setSvmGamma(Arrays.asList(WekaLearner.Y_coarse));
    find(config);
  }

  private static void generateFineParameterGrid() throws Exception {
    MachineLearningDefinienExtractionConfig config = MachineLearningDefinienExtractionConfig.testfine();
    config.setSvmCost(Arrays.asList(WekaLearner.C_fine));
    config.setSvmGamma(Arrays.asList(WekaLearner.Y_fine));
    find(config);
  }


}