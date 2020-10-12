package com.colorseq.abrowse.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "abrowse_job")
public class AbrowseJob {

    @Id
    private String id;
    private String jobName;
    private String jobType;
    private String jobStatu;
    private Date createTime;
    private Date completeTime;
    private String jobDesc;
    private String sessionId;

    public AbrowseJob() {
        this.id = "";
        this.jobName = "";
        this.jobType = "";
        this.jobStatu = "";
        this.createTime = new Date();
        this.completeTime = new Date();
        this.jobDesc = "";
        this.sessionId = "";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getJobType() {
        return jobType;
    }

    public void setJobType(String jobType) {
        this.jobType = jobType;
    }

    public String getJobStatu() {
        return jobStatu;
    }

    public void setJobStatu(String jobStatu) {
        this.jobStatu = jobStatu;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getCompleteTime() {
        return completeTime;
    }

    public void setCompleteTime(Date completeTime) {
        this.completeTime = completeTime;
    }

    public String getJobDesc() {
        return jobDesc;
    }

    public void setJobDesc(String jobDesc) {
        this.jobDesc = jobDesc;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
}
