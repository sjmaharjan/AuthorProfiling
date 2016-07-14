/**
 * 
 */
package edu.uab.jobs.writables;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;

/**
 * @author sjmaharjan
 * 
 */
public class TextPair implements WritableComparable<TextPair> {

	private Text first;
	private Text second;

	public TextPair() {
		set(new Text(), new Text());

	}

	/**
	 * @param text
	 * @param text2
	 */
	private void set(Text first, Text second) {
		this.first = first;
		this.second = second;

	}

	public TextPair(Text first, Text second) {
		this.first = first;
		this.second = second;
	}

	public TextPair(String first, String second) {
		set(new Text(first), new Text(second));
	}
	
	
	public Text getFirst() {
		return first;
	}

	public Text getSecond() {
		return second;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.hadoop.io.Writable#write(java.io.DataOutput)
	 */
	public void write(DataOutput out) throws IOException {
		first.write(out);
		second.write(out);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.hadoop.io.Writable#readFields(java.io.DataInput)
	 */
	public void readFields(DataInput in) throws IOException {
		first.readFields(in);
		second.readFields(in);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(TextPair other) {
		int cmp= first.compareTo(other.first);
		if(cmp!=0){
			return cmp;
		}
		return second.compareTo(other.second);
	}

	@Override
	public int hashCode() {
		
		return first.hashCode() *163 +second.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof TextPair){
			TextPair tp = (TextPair) obj;
			return first.equals(tp.first)&&second.equals(tp.second);
		}
		return false;
	}

	@Override
	public String toString() {
		return "("+first+", "+second+")";
	}

}
