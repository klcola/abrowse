package com.colorseq.abrowse.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "abrowse_blatdatabase")
public class BlatDatabase {
    @Id
    private String id;
    private String Genome;
    private String databasePathAndName;
    private String seqPath;
    private String outputPath;
    private String shellName;
    private String statu;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGenome() {
        return Genome;
    }

    public void setGenome(String genome) {
        Genome = genome;
    }

    public String getDatabasePathAndName() {
        return databasePathAndName;
    }

    public void setDatabasePathAndName(String databasePathAndName) {
        this.databasePathAndName = databasePathAndName;
    }

    public String getSeqPath() {
        return seqPath;
    }

    public void setSeqPath(String seqPath) {
        this.seqPath = seqPath;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    public String getShellName() {
        return shellName;
    }

    public void setShellName(String shellName) {
        this.shellName = shellName;
    }

    public String getStatu() {
        return statu;
    }

    public void setStatu(String statu) {
        this.statu = statu;
    }
}
