package edu.uab.jobs.writables;

import org.apache.hadoop.io.Writable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: suraj
 * Date: 11/13/13
 * Time: 11:43 AM
 * To change this template use File | Settings | File Templates.
 * @Reference cloud9 project
 *
 */
public class HashMapWritable<K extends Writable, V extends Writable> extends HashMap<K, V> implements Writable {


    public HashMapWritable() {
        super();
    }

    public HashMapWritable(HashMap<K, V> map) {
        super(map);
    }

    @Override
    public void write(DataOutput dataOutput) throws IOException {
        //write the size of map first
        dataOutput.writeInt(size());
        if (size() == 0)
            return;

        Set<Map.Entry<K, V>> entries = entrySet();
        Map.Entry<K, V> first = entries.iterator().next();
        K Objkey = first.getKey();
        V ObjVal = first.getValue();
        //write out the class name
        dataOutput.writeUTF(Objkey.getClass().getCanonicalName());
        dataOutput.writeUTF(ObjVal.getClass().getCanonicalName());

        //write all key value
        for (Map.Entry<K, V> entry : entries) {
            entry.getKey().write(dataOutput);
            entry.getValue().write(dataOutput);
        }


    }

    @Override
    public void readFields(DataInput dataInput) throws IOException {
        this.clear();
        int numberOfElements = dataInput.readInt();
        if (numberOfElements == 0)
            return;
        String keyClassName = dataInput.readUTF();
        String valClassName = dataInput.readUTF();

        K objkey;
        V objVal;
        //use java reflection to create instance of type K and V


        try {
            Class<K> keyClass = (Class<K>) Class.forName(keyClassName);
            Class<V> valClass = (Class<V>) Class.forName(valClassName);


            for (int i = 0; i < numberOfElements; i++) {
                objkey = (K) keyClass.newInstance();
                objkey.readFields(dataInput);
                objVal = (V) valClass.newInstance();
                objVal.readFields(dataInput);
                //add to map
                put(objkey, objVal);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InstantiationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IllegalAccessException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


    }
}
