package edu.uab.console.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by suraj on 3/25/14.
 */
public enum AgeGroup {
//    AG_10s("10s"),
//    AG_20s("20s"),
//    AG_30s("30s"),
    AG_18_24("18-24"),
    AG_25_34("25-34"),
    AG_35_49("35-49"),
    AG_50_64("50-64"),
    AG_65_PLUS("65-plus"),  //change for daily strength
    AG_UNKNOWN("xxx");

    private final String text;

    private static final Map<String, AgeGroup> stringToEnum = new HashMap<String, AgeGroup>();

    static {
        for (AgeGroup ag : values()) {
            stringToEnum.put(ag.toString(), ag);
        }
    }


    AgeGroup(String text) {
        this.text = text;
    }


    public static AgeGroup fromString(String ag) {
        if (stringToEnum.containsKey(ag.toLowerCase()))
            return stringToEnum.get(ag);
        else
            return stringToEnum.get("xxx");
    }

    @Override
    public String toString() {
        return text;
    }
}
