package edu.uab.jobs.vectorize;

import org.apache.mahout.math.function.ObjectIntProcedure;
import org.apache.mahout.math.map.OpenObjectIntHashMap;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: suraj
 * Date: 11/19/13
 * Time: 1:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class GenderAndAgeGroupLabel implements LabelExtractor {
    private OpenObjectIntHashMap<String> labels;

    public GenderAndAgeGroupLabel() {
        this.labels = new OpenObjectIntHashMap<String>(6);
        labels.put("10s_male", 0);
        labels.put("10s_female", 1);
        labels.put("20s_male", 2);
        labels.put("20s_female", 3);
        labels.put("30s_male", 4);
        labels.put("30s_female", 5);

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
            label = "error";
        }
        return label;
    }

    public static void main(String args[]){
        GenderAndAgeGroupLabel g= new GenderAndAgeGroupLabel();
       System.out.println( g.extractLabel("/15283f593b54ec6b2c903e1e0ef56dd1_en_30s_male.txt"));
    }
}
