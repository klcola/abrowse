package com.colorseq.abrowse;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Id;
import java.io.Serializable;
import java.util.*;

@Document(collection="config_genome")
public class ConfigGenome implements Serializable {

    @Id
    private String id;

    @Indexed
    private String name;
    private String displayName;
    private Map<String, ConfigChromosome> chromosomeMap;
    private Map<String, ConfigView> viewMap;
    private Map<String, ConfigTrackGroup> trackGroupMap;

    public ConfigGenome() {

        this.trackGroupMap = new Hashtable<>();
        this.viewMap = new HashMap<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public Map<String, ConfigChromosome> getChromosomeMap() {
        return chromosomeMap;
    }

    public void setChromosomeMap(Map<String, ConfigChromosome> chromosomeMap) {
        this.chromosomeMap = chromosomeMap;
    }

    public Map<String, ConfigView> getViewMap() {
        return viewMap;
    }

    public void setViewMap(Map<String, ConfigView> viewMap) {
        this.viewMap = viewMap;
    }

    public Map<String, ConfigTrackGroup> getTrackGroupMap() {
        return trackGroupMap;
    }

    public void setTrackGroupMap(Map<String, ConfigTrackGroup> trackGroupMap) {
        this.trackGroupMap = trackGroupMap;
    }

    public void addTrackGroup(ConfigTrackGroup configTrackGroup) {
        this.trackGroupMap.put(configTrackGroup.getName(), configTrackGroup);
    }

    public void addTrackGroups(Map<String, ConfigTrackGroup> anotherTrackGroups) {

        this.trackGroupMap.putAll(anotherTrackGroups);
    }

    public void addViews(Map<String, ConfigView> anotherViewsMap) {

        this.viewMap.putAll(anotherViewsMap);
    }
}
