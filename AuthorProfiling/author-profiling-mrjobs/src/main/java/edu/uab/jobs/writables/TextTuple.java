/**
 * 
 */
package edu.uab.jobs.writables;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;

/**
 * Mahout's StringTuple class replication
 * 
 */
public class TextTuple implements WritableComparable<TextTuple> {

	private List<String> tuple = new ArrayList<String>();

	public TextTuple() {
	}

	public TextTuple(String firstEntry) {
		add(firstEntry);
	}

	public TextTuple(Iterable<String> entries) {
		for (String entry : entries) {
			add(entry);
		}
	}

	public TextTuple(String[] entries) {
		for (String entry : entries) {
			add(entry);
		}
	}


	public boolean add(String entry) {
		return tuple.add(entry);
	}


	public String stringAt(int index) {
		return tuple.get(index);
	}


	public String replaceAt(int index, String newString) {
		return tuple.set(index, newString);
	}


	public List<String> getEntries() {
		return Collections.unmodifiableList(this.tuple);
	}


	public int length() {
		return this.tuple.size();
	}

	@Override
	public String toString() {
		String str= "";
		for(String s : tuple){
			str+=s+",,, ";
		}
		return str;
	}

	@Override
	public int hashCode() {
		return tuple.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		TextTuple other = (TextTuple) obj;
		if (tuple == null) {
			if (other.tuple != null) {
				return false;
			}
		} else if (!tuple.equals(other.tuple)) {
			return false;
		}
		return true;
	}

	public void readFields(DataInput in) throws IOException {
		int len = in.readInt();
		tuple = new ArrayList<String>();
		Text value = new Text();
		for (int i = 0; i < len; i++) {
			value.readFields(in);
			tuple.add(value.toString());
		}
	}

	public void write(DataOutput out) throws IOException {
		out.writeInt(tuple.size());
		Text value = new Text();
		for (String entry : tuple) {
			value.set(entry);
			value.write(out);
		}
	}

	public int compareTo(TextTuple otherTuple) {
		int thisLength = length();
		int otherLength = otherTuple.length();
		int min = Math.min(thisLength, otherLength);
		for (int i = 0; i < min; i++) {
			int ret = this.tuple.get(i).compareTo(otherTuple.stringAt(i));
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

}
