package com.colorseq.abrowse.response;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TrackResponse implements Serializable {

    private String trackName;
    private String trackDisplayName;
    private String viewName;
    private String maxEntryScore;
    private int yIndex;
    private List<BlockResponse> blockResponses;

    public TrackResponse() {
        this.blockResponses = new ArrayList<>();
    }

    public String getTrackName() {
        return trackName;
    }

    public void setTrackName(String trackName) {
        this.trackName = trackName;
    }

    public String getTrackDisplayName() {
        return trackDisplayName;
    }

    public void setTrackDisplayName(String trackDisplayName) {
        this.trackDisplayName = trackDisplayName;
    }

    public String getViewName() {
        return viewName;
    }

    public void setViewName(String viewName) {
        this.viewName = viewName;
    }

    public String getMaxEntryScore() {
        return maxEntryScore;
    }

    public void setMaxEntryScore(String maxEntryScore) {
        this.maxEntryScore = maxEntryScore;
    }

    public int getyIndex() {
        return yIndex;
    }

    public void setyIndex(int yIndex) {
        this.yIndex = yIndex;
    }

    public List<BlockResponse> getBlockResponses() {
        return blockResponses;
    }

    public void setBlockResponses(List<BlockResponse> blockResponses) {
        this.blockResponses = blockResponses;
    }

    public void addBlockResponse(BlockResponse blockResponse) {
        this.blockResponses.add(blockResponse);
    }
}
