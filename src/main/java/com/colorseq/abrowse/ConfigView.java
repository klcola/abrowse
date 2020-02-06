package com.colorseq.abrowse;

import java.io.Serializable;

public class ConfigView implements Serializable {

    private String name;
    private String type;
    private String dataType;
    private boolean isBasePair;

    public ConfigView() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public boolean isBasePair() {
        return isBasePair;
    }

    public void setBasePair(boolean basePair) {
        isBasePair = basePair;
    }
}
