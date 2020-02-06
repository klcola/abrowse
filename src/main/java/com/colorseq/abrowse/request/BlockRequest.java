package com.colorseq.abrowse.request;

import java.io.Serializable;

public class BlockRequest implements Serializable {

    private int start;
    private int end;

    public BlockRequest() {
    }

    public BlockRequest(int start, int end) {
        this.start = start;
        this.end = end;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }
}
