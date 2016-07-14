package edu.uab.jobs.writables;

import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: suraj
 * Date: 11/13/13
 * Time: 12:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class FeaturesLinkedHashMap implements WritableComparable<FeaturesLinkedHashMap> {

    private Map<String, Float> features = new LinkedHashMap<String, Float>();


    public FeaturesLinkedHashMap() {

    }

    public FeaturesLinkedHashMap(Map<String, Float> entries) {
        for (Map.Entry<String, Float> entry : entries.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    public int size() {
        return features.size();
    }


    public Float get(String key) {
        return features.get(key);
    }

    public void put(String key, Float value) {
        this.features.put(key, value);
    }

    public Map<String, Float> getFeatures() {
        return this.features;
    }

    public Set<Map.Entry<String, Float>> getEntrySet() {
        return features.entrySet();
    }


    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append('{');

        boolean first = true;
        for (Map.Entry<String, Float> entry : this.features.entrySet()) {
            if (first) {
                first = false;
            } else {
                result.append(',');
            }
            result.append(entry.getKey());
            result.append(':');
            result.append(entry.getValue());
        }
        result.append('}');
        return result.toString();

    }

    @Override
    public int compareTo(FeaturesLinkedHashMap other) {
        int thisLength = size();
        int otherLength = other.size();
        int min = Math.min(thisLength, otherLength);
        //convert maps values to ArrayList
        List<Float> thisFeatures = new ArrayList<Float>(this.features.values());
        List<Float> otherFeatures = new ArrayList<Float>(other.getFeatures().values());

        for (int i = 0; i < min; i++) {
            int ret = thisFeatures.get(i).toString().compareTo(otherFeatures.get(i).toString());
            if (ret != 0) {
                return ret;
            }
        }
        if (thisLength < otherLength) {
            return -1;
        } else if (thisLength > otherLength) {
            return 1;
        } else {
            return 0;
        }
    }


    public boolean containsKey(String key) {
        return this.features.containsKey(key);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FeaturesLinkedHashMap)) return false;

        FeaturesLinkedHashMap that = (FeaturesLinkedHashMap) o;

        if (features != null ? !features.equals(that.features) : that.features != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return features != null ? features.hashCode() : 0;
    }

    @Override
    public void write(DataOutput dataOutput) throws IOException {
        //write the size of map first
        dataOutput.writeInt(size());
        if (size() == 0)
            return;

        Set<Map.Entry<String, Float>> entries = getEntrySet();
        //write all key value
        Text key = new Text();
        FloatWritable value = new FloatWritable();
        for (Map.Entry<String, Float> entry : entries) {
            key.set(entry.getKey());
            key.write(dataOutput);
            value.set(entry.getValue());
            value.write(dataOutput);


        }
    }

    @Override
    public void readFields(DataInput dataInput) throws IOException {
        int len = dataInput.readInt();
        if (len == 0)
            return;
        features = new HashMap<String, Float>();
        Text key = new Text();
        FloatWritable value = new FloatWritable();
        for (int i = 0; i < len; i++) {
            key.readFields(dataInput);
            value.readFields(dataInput);
            features.put(key.toString(), Float.parseFloat(value.toString()));
        }

    }


}
