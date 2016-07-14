package edu.uab.jobs.vectorize;

import org.apache.mahout.math.function.ObjectIntProcedure;
import org.apache.mahout.math.map.OpenObjectIntHashMap;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: sjmaharjan
 * Date: 11/18/13
 * Time: 8:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class GenderLabel implements LabelExtractor {

    private OpenObjectIntHashMap<String> labels;

    public GenderLabel() {
        this.labels = new OpenObjectIntHashMap<String>(2);
        labels.put("male", 0);
        labels.put("female", 1);

    }


    @Override
    public OpenObjectIntHashMap<String> getPredefinedLabels() {
        return this.labels;
    }

    public Map<Integer, String> getSwappedKeyValueMap() {
        final Map<Integer, String> map = new HashMap<Integer, String>();
        this.labels.forEachPair(new ObjectIntProcedure<String>() {
            @Override
            public boolean apply(String first, int second) {
                map.put(second, first);
                return true;
            }
        });
        return map;
    }

    @Override
    public String extractLabel(String key) {
        String label = key.substring(key.lastIndexOf("_") + 1, key.indexOf(".", key.lastIndexOf("_") + 1));
        return label;
    }
}
