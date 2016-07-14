package edu.uab.jobs.vectorize;

import org.apache.mahout.math.function.ObjectIntProcedure;
import org.apache.mahout.math.map.OpenObjectIntHashMap;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: suraj
 * Date: 11/19/13
 * Time: 1:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class AgeGroupLabel implements LabelExtractor {

    private OpenObjectIntHashMap<String> labels;

    public AgeGroupLabel() {
        this.labels = new OpenObjectIntHashMap<String>(3);
        labels.put("10s", 0);
        labels.put("20s", 1);
        labels.put("30s", 2);
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
        String label = key.substring(key.indexOf("_") + 1, key.indexOf("_", key.indexOf("_") + 1));
        return label;
    }
}
