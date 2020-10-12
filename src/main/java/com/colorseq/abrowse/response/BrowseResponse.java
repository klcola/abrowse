package com.colorseq.abrowse.response;

import com.colorseq.abrowse.entity.AbrowseJob;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class BrowseResponse implements ServerResponse {

    private String genome;
    private String chrName;
    private int chrLength;
    private List<TrackResponse> trackResponses;
    private int requestIndex;
    private List<AbrowseJob> jobs;

    public BrowseResponse() {
        this.trackResponses = new ArrayList<>();
    }

    public String getGenome() {
        return genome;
    }

    public void setGenome(String genome) {
        this.genome = genome;
    }

    public String getChrName() {
        return chrName;
    }

    public void setChrName(String chrName) {
        this.chrName = chrName;
    }

    public List<TrackResponse> getTrackResponses() {
        return trackResponses;
    }

    public void setTrackResponses(List<TrackResponse> trackResponses) {
        this.trackResponses = trackResponses;
    }

    public void addTrackResponse(TrackResponse trackResponse) {
        this.trackResponses.add(trackResponse);
    }

    public int getChrLength() {
        return chrLength;
    }

    public void setChrLength(int chrLength) {
        this.chrLength = chrLength;
    }

    public int getRequestIndex() {
        return requestIndex;
    }

    public void setRequestIndex(int requestIndex) {
        this.requestIndex = requestIndex;
    }

    public List<AbrowseJob> getJobs() {
        return jobs;
    }

    public void setJobs(List<AbrowseJob> jobs) {
        this.jobs = jobs;
    }

    @Override
    public boolean isError() {
        return false;
    }
}
