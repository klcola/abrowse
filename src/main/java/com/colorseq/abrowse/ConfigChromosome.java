package com.colorseq.abrowse;

import java.io.Serializable;

public class ConfigChromosome implements Serializable {

    private String name;
    private int length;
    private int code;

    public ConfigChromosome() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
