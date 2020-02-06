package com.colorseq.abrowse;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ConfigTrack implements Comparable<ConfigTrack>, Serializable {

    private String name;
    private String displayName;
    private String description;
    private String defaultNormalView;
    private int displayYIndex;
    private List<ConfigTrackView> views;

    public ConfigTrack() {
    }

    @Override
    public int compareTo(ConfigTrack o) {
        return Integer.compare(this.displayYIndex, o.getDisplayYIndex());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDefaultNormalView() {
        return defaultNormalView;
    }

    public void setDefaultNormalView(String defaultNormalView) {
        this.defaultNormalView = defaultNormalView;
    }

    public int getDisplayYIndex() {
        return displayYIndex;
    }

    public void setDisplayYIndex(int displayYIndex) {
        this.displayYIndex = displayYIndex;
    }

    public List<ConfigTrackView> getViews() {
        return views;
    }

    public void setViews(List<ConfigTrackView> views) {
        this.views = views;
    }

    public void addView(ConfigTrackView trackView) {
        if (null == views) {
            views = new ArrayList<>();
        }
        this.views.add(trackView);
    }
}
