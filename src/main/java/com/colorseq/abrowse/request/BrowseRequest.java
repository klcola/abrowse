package com.colorseq.abrowse.request;

import java.io.Serializable;
import java.util.List;

public class BrowseRequest implements Serializable {

    private String genome;
    private String chrName;
    private List<TrackRequest> trackRequests;

    public BrowseRequest() {
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

    public List<TrackRequest> getTrackRequests() {
        return trackRequests;
    }

    public void setTrackRequests(List<TrackRequest> trackRequests) {
        this.trackRequests = trackRequests;
    }
}
