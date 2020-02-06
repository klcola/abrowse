package com.colorseq.abrowse;


import java.io.Serializable;
import java.util.Hashtable;
import java.util.Map;

/**
 * @author Lei Kong
 */
public class ConfigTrackGroup implements Comparable<ConfigTrackGroup>, Serializable {

    private String name;
    private String displayName;
    private int displayYIndex;
    private Map<String, ConfigTrack> trackMap;

    public ConfigTrackGroup() {
        trackMap = new Hashtable<>();
    }

    @Override
    public int compareTo(ConfigTrackGroup o) {
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

    public int getDisplayYIndex() {
        return displayYIndex;
    }

    public void setDisplayYIndex(int displayYIndex) {
        this.displayYIndex = displayYIndex;
    }

    public Map<String, ConfigTrack> getTrackMap() {
        return trackMap;
    }

    public void setTrackMap(Map<String, ConfigTrack> trackMap) {
        this.trackMap = trackMap;
    }

    public void addTrack(ConfigTrack track) {

        trackMap.put(track.getName(), track);
    }
}
