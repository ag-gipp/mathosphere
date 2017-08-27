package com.formulasearchengine.mathosphere.mlp;

import com.formulasearchengine.mathosphere.mlp.cli.TagsCommandConfig;
import com.formulasearchengine.mathosphere.mlp.contracts.TagExtractionMapper;
import com.formulasearchengine.mathosphere.mlp.contracts.TextExtractorMapper;
import com.formulasearchengine.mathosphere.mlp.pojos.MathTag;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.operators.DataSource;

public class WikiTagExtractor {


    public static void run(TagsCommandConfig config) throws Exception {
        ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
        final DataSource<String> dump = FlinkMlpRelationFinder.readWikiDump(config, env);
        //DataSet<String> text = env.fromElements("[ ");
        dump
                .flatMap(new TextExtractorMapper())
                .flatMap(new TagExtractionMapper(config))
                .distinct(MathTag::getContentHash)
                .map(MathTag::toJson)
                .writeAsFormattedText(config.getOutputDir() + "/formulae.txt", s -> s + ",")
                .setParallelism(1);
        env.execute();
    }
}