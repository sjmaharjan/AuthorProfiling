package edu.uab.jobs.vectorize;

import org.apache.mahout.math.function.ObjectIntProcedure;
import org.apache.mahout.math.map.OpenObjectIntHashMap;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by suraj on 4/17/14.
 */
public class Pan14Label implements LabelExtractor {

    private OpenObjectIntHashMap<String> labels;

    public Pan14Label() {
        this.labels = new OpenObjectIntHashMap<String>(10);
        labels.put("18-24_male", 0);
        labels.put("18-24_female", 1);
        labels.put("25-34_male", 2);
        labels.put("25-34_female", 3);
        labels.put("35-49_male", 4);
        labels.put("35-49_female", 5);
        labels.put("50-64_male", 6);
        labels.put("50-64_female", 7);
        labels.put("65-xx_male", 8);
        labels.put("65-xx_female", 9);
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
        String label = null;
        try {
            label = key.substring(key.indexOf("_",key.indexOf("_") + 1)+1, key.indexOf(".", key.indexOf("_",key.indexOf("_")+ 1)));
        } catch (StringIndexOutOfBoundsException e) {
            label = "xxx";
        }
        return label;
    }

    public static void main(String args[]){
        Pan14Label g= new Pan14Label();
        System.out.println( g.extractLabel("/49d61fe719d98c6cbedbd430945336b4_en_65-xx_male.xml"));
        System.out.println ( g.getPredefinedLabels().get("18-24_female"));


    }
}
