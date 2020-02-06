package com.colorseq.abrowse.response;

import org.bson.Document;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class BlockResponse implements Serializable {

    private int start;
    private int end;
    private List<Document> entryList;

    public BlockResponse(int start, int end) {
        this.start = start;
        this.end = end;
        this.entryList = new ArrayList<>();
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

    public List<Document> getEntryList() {
        return entryList;
    }

    public void setEntryList(List<Document> entryList) {
        this.entryList = entryList;
    }

    public void addEntry(Document document) {
        this.entryList.add(document);
    }
}
