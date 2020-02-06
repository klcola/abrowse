package com.colorseq.abrowse;


import java.io.Serializable;

public class ConfigTrackView implements Serializable {

    private String viewName;
    private String entryLink;

    public ConfigTrackView() {
    }

    public ConfigTrackView(String viewName, String entryLink) {
        this.viewName = viewName;
        this.entryLink = entryLink;
    }

    public String getViewName() {
        return viewName;
    }

    public void setViewName(String viewName) {
        this.viewName = viewName;
    }

    public String getEntryLink() {
        return entryLink;
    }

    public void setEntryLink(String entryLink) {
        this.entryLink = entryLink;
    }
}
