package edu.uab.console.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by suraj on 3/25/14.
 */
public enum Gender {
    MALE("male"), FEMALE("female"), GENDER_UNKNOWN("xxx");

    private String text;

    private static final Map<String, Gender> stringToEnum = new HashMap<String, Gender>();

    static {
        for (Gender gender : values()) {
            stringToEnum.put(gender.toString(), gender);
        }
    }

    Gender(String text) {
        this.text = text;
    }

    public static Gender fromString(String gender) {
        if (stringToEnum.containsKey(gender.toLowerCase()))
            return stringToEnum.get(gender);
        else
            return stringToEnum.get("xxx");
    }

    @Override
    public String toString() {
        return text;
    }

}
