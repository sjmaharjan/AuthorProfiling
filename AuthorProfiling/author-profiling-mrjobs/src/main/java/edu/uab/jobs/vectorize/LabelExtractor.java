package edu.uab.jobs.vectorize;

import java.util.Map;

import org.apache.mahout.math.map.OpenObjectIntHashMap;

/**
 * Created with IntelliJ IDEA. User: sjmaharjan Date: 11/18/13 Time: 8:47 PM To
 * change this template use File | Settings | File Templates.
 */
public interface LabelExtractor {

	public OpenObjectIntHashMap<String> getPredefinedLabels();

	public String extractLabel(String key);

	public Map<Integer, String> getSwappedKeyValueMap();
}
