package com.colorseq.abrowse.response;

import com.colorseq.abrowse.entity.BlatResultPSL;

import java.io.Serializable;
import java.util.List;

public class SearchResponse implements Serializable {

    /**
     * 搜索结果集
     */
    private List<BlatResultPSL> allResults;

    /**
     * 0-没有，1-有
     */
    private String hasResult;

    /**
     * jobId
     */
    private String jobId;

    public List<BlatResultPSL> getAllResults() {
        return allResults;
    }

    public void setAllResults(List<BlatResultPSL> allResults) {
        this.allResults = allResults;
    }

    public String getHasResult() {
        return hasResult;
    }

    public void setHasResult(String hasResult) {
        this.hasResult = hasResult;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }
}
