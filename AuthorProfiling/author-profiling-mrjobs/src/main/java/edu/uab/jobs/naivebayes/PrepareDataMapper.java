package edu.uab.jobs.naivebayes;

import edu.uab.jobs.writables.TextTuple;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.io.Text;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;


import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: suraj
 * Date: 11/7/13
 * Time: 2:47 PM
 * To change this template use File | Settings | File Templates.
 */
public class PrepareDataMapper extends Mapper<Text, Text, Text, Text> {

    protected void map(Text key, Text value, Context context)
            throws IOException, InterruptedException {
        //conver the key  ffffdf09a4c76ff65b6190e2f1050d7a_en_20s_male.xml to  /en_20s_male/ffffdf09a4c76ff65b6190e2f1050d7a.xml
        Text newKey = getLabeledKey(key.toString());
        context.write(newKey, value);
    }


    private Text getLabeledKey(String key) {
        String label = key.substring(key.indexOf("_") + 1, key.indexOf(".", key.indexOf("_") + 1));
        return new Text("/" + label + "/" + key.substring(0, key.indexOf("_")) + ".txt");
    }


}
