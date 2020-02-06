package com.colorseq.abrowse.cmd;

/**
 * @author Lei Kong
 */
public class SpliceSite {

    private String chrName;
    private int left;
    private int right;
    private int depth;

    public SpliceSite(String chrName, int left, int right, int depth) {
        this.chrName = chrName;
        this.left = left;
        this.right = right;
        this.depth = depth;
    }

    public SpliceSite() {
    }

    public String getChrName() {
        return chrName;
    }

    public void setChrName(String chrName) {
        this.chrName = chrName;
    }

    public int getLeft() {
        return left;
    }

    public void setLeft(int left) {
        this.left = left;
    }

    public int getRight() {
        return right;
    }

    public void setRight(int right) {
        this.right = right;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public String toRecordLine() {
        String recordLine = String.join("\t", this.chrName, String.valueOf(this.left),
                String.valueOf(this.right), String.valueOf(this.depth));
        return recordLine;
    }
}
