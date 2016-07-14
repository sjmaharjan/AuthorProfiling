package edu.uab.console.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by suraj on 3/25/14.
 */
public enum Type {
    BLOG("blog"), TWITTER("twitter"), SOCIALMEDIA("socialmedia"), REVIEW("reviews");

    private static final Map<String, Type> stringToEnum = new HashMap<String, Type>();

    static {
        for (Type t : values()) {
            stringToEnum.put(t.toString(), t);
        }
    }

    private final String text;

    Type(String text) {
        this.text = text;
    }

    public static Type fromString(String type) {
        if (type.equalsIgnoreCase("review")) {
            return stringToEnum.get("reviews");
        }
        else{
            return stringToEnum.get(type);
        }
    }

    @Override
    public String toString() {
        return text;
    }
}
