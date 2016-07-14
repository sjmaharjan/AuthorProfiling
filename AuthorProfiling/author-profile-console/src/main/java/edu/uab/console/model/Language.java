package edu.uab.console.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by suraj on 3/25/14.
 */
public enum Language {
    ENGLISH("en"), SPANISH("es");
    private final String text;

    private static final Map<String, Language> stringToEnum = new HashMap<String, Language>();

    static {
        for (Language lang : values()) {
            stringToEnum.put(lang.toString(), lang);
        }
    }

    Language(String text) {
        this.text = text;
    }

    public static Language fromString(String lang) {
        return stringToEnum.get(lang);
    }

    @Override
    public String toString() {
        return text;
    }
}
