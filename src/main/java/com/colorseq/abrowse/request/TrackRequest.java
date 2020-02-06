package com.colorseq.abrowse.request;

import java.io.Serializable;
import java.util.List;

public class TrackRequest implements Serializable {

    private String trackGroupName;
    private String trackName;
    private String viewName;
    private int yIndex;

    private List<BlockRequest> blockRequests;

    public TrackRequest() {
    }

    public String getTrackGroupName() {
        return trackGroupName;
    }

    public void setTrackGroupName(String trackGroupName) {
        this.trackGroupName = trackGroupName;
    }

    public String getTrackName() {
        return trackName;
    }

    public void setTrackName(String trackName) {
        this.trackName = trackName;
    }

    public String getViewName() {
        return viewName;
    }

    public void setViewName(String viewName) {
        this.viewName = viewName;
    }

    public int getyIndex() {
        return yIndex;
    }

    public void setyIndex(int yIndex) {
        this.yIndex = yIndex;
    }

    public List<BlockRequest> getBlockRequests() {
        return blockRequests;
    }

    public void setBlockRequests(List<BlockRequest> blockRequests) {
        this.blockRequests = blockRequests;
    }
}
