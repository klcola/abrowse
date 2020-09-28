package com.colorseq.abrowse.entity;


import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "blatresultpsl")
public class BlatResultPSL {
    @Id
    private String id;
    private String jobid;
    private String bmatch;
    private String mismatch;
    private String repmatch;
    private String Ns;
    private String qgapcount;
    private String qgapbases;
    private String tgapcount;
    private String tgapbases;
    private String strand;
    private String qname;
    private String qsize;
    private String qstart;
    private String qend;
    private String tname;
    private String tsize;
    private String tstart;
    private String tend;
    private String blockcount;
    private String blocksize;
    private String qstarts;
    private String tstarts;
    private Date createTime;
    private Date completeTime;
    private String curStatu;
    private String gene;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getJobid() {
        return jobid;
    }

    public void setJobid(String jobid) {
        this.jobid = jobid;
    }

    public String getBmatch() {
        return bmatch;
    }

    public void setBmatch(String bmatch) {
        this.bmatch = bmatch;
    }

    public String getMismatch() {
        return mismatch;
    }

    public void setMismatch(String mismatch) {
        this.mismatch = mismatch;
    }

    public String getRepmatch() {
        return repmatch;
    }

    public void setRepmatch(String repmatch) {
        this.repmatch = repmatch;
    }

    public String getNs() {
        return Ns;
    }

    public void setNs(String ns) {
        Ns = ns;
    }

    public String getQgapcount() {
        return qgapcount;
    }

    public void setQgapcount(String qgapcount) {
        this.qgapcount = qgapcount;
    }

    public String getQgapbases() {
        return qgapbases;
    }

    public void setQgapbases(String qgapbases) {
        this.qgapbases = qgapbases;
    }

    public String getTgapcount() {
        return tgapcount;
    }

    public void setTgapcount(String tgapcount) {
        this.tgapcount = tgapcount;
    }

    public String getTgapbases() {
        return tgapbases;
    }

    public void setTgapbases(String tgapbases) {
        this.tgapbases = tgapbases;
    }

    public String getStrand() {
        return strand;
    }

    public void setStrand(String strand) {
        this.strand = strand;
    }

    public String getQname() {
        return qname;
    }

    public void setQname(String qname) {
        this.qname = qname;
    }

    public String getQsize() {
        return qsize;
    }

    public void setQsize(String qsize) {
        this.qsize = qsize;
    }

    public String getQstart() {
        return qstart;
    }

    public void setQstart(String qstart) {
        this.qstart = qstart;
    }

    public String getQend() {
        return qend;
    }

    public void setQend(String qend) {
        this.qend = qend;
    }

    public String getTname() {
        return tname;
    }

    public void setTname(String tname) {
        this.tname = tname;
    }

    public String getTsize() {
        return tsize;
    }

    public void setTsize(String tsize) {
        this.tsize = tsize;
    }

    public String getTstart() {
        return tstart;
    }

    public void setTstart(String tstart) {
        this.tstart = tstart;
    }

    public String getTend() {
        return tend;
    }

    public void setTend(String tend) {
        this.tend = tend;
    }

    public String getBlockcount() {
        return blockcount;
    }

    public void setBlockcount(String blockcount) {
        this.blockcount = blockcount;
    }

    public String getBlocksize() {
        return blocksize;
    }

    public void setBlocksize(String blocksize) {
        this.blocksize = blocksize;
    }

    public String getQstarts() {
        return qstarts;
    }

    public void setQstarts(String qstarts) {
        this.qstarts = qstarts;
    }

    public String getTstarts() {
        return tstarts;
    }

    public void setTstarts(String tstarts) {
        this.tstarts = tstarts;
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

    public String getCurStatu() {
        return curStatu;
    }

    public void setCurStatu(String curStatu) {
        this.curStatu = curStatu;
    }

    public String getGene() {
        return gene;
    }

    public void setGene(String gene) {
        this.gene = gene;
    }
}
