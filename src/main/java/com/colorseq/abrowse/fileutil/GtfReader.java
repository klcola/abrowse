package com.colorseq.abrowse.fileutil;

import com.colorseq.abrowse.GencodeGtfTag;
import com.colorseq.abrowse.GtfFeatureType;
import com.colorseq.abrowse.GtfField;
import org.bson.Document;


import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: Zeshan Cheng
 * @Date: 2018/6/26 上午10:48
 * @ClassDescription:GTF格式的文件处理类
 */

public class GtfReader extends BioDataReader{

    private String lastLine;

    public String getLastLine() {
        return lastLine;
    }

    public void setLastLine(String lastLine) {
        this.lastLine = lastLine;
    }

    public  GtfReader(FileReader reader){
        super(reader);
    }

    @Override
    public Document readEntry() throws Exception {
        Document geneDoc = null;

        List<Document> transcriptDocList = null;
        Document transcriptDoc = null;

        List<Document> blockDocList = null;
        String line = null;

        String lastLine = this.getLastLine();
        String previousGeneId = null;


        int geneNum = 0;
        int transcriptNum = 0;
        int exonNum = 0;
        try {
            if(lastLine == null){//第一次读取GTF文件
                System.out.println("第一次读取文件");
                while ((line = reader.readLine()) != null){
                    System.out.println("The line is ======"+line);
                    String[] fields = line.trim().split("\\t");
                    if (line.startsWith("#")) {
                        continue;
                    }
                    if (fields[2].equals(GtfFeatureType.gene)) {
                        //跳过gene列，直接从transcript中读取gene信息
                        continue;
                    }
                    String chrName = fields[0];
                    if (!chrName.startsWith("chr")) {
                        // for Ensembl format gtf
                        chrName = new StringBuilder("chr").append(chrName).toString();
                    }
                    int start = Integer.valueOf(fields[3]);
                    int end = Integer.valueOf(fields[4]);
                    String scoreStr = fields[5];
                    float score = 0;
                    if (!scoreStr.equals(".")) {
                        score = Float.valueOf(scoreStr);
                    }
                    if (fields[2].equals(GtfFeatureType.transcript)){
                        Map<String,String> attributes = new HashMap<>();
                        String attributeStr = fields[8].trim().replaceAll("\"", "");
                        if (attributeStr.charAt(attributeStr.length() - 1) == ';') {
                            attributeStr = attributeStr.substring(0, attributeStr.length() - 1);
                        }
                        String[] attrPairStrArray = attributeStr.split(";");
                        for (String attrPairStr : attrPairStrArray) {
                            attrPairStr = attrPairStr.trim();
                            String[] attrPair = attrPairStr.split(" ");
                            //System.err.println("AAA3:[" + attrPair[0] + "],[" + attrPair[1] + "]" );
                            attributes.put(attrPair[0], attrPair[1]);
                        }
                        blockDocList = new ArrayList<>();
                        transcriptDocList = new ArrayList<>();
                        transcriptDoc = new Document();
                        transcriptDoc.append(GencodeGtfTag.transcript_id,attributes.get(GencodeGtfTag.transcript_id))
                                .append(GtfField.seqname, chrName)
                                .append(GtfField.source, fields[1])
                                .append(GtfField.feature, fields[2])
                                .append(GtfField.start, start)
                                .append(GtfField.end, end)
                                .append(GtfField.score, score)
                                .append(GtfField.strand, fields[6])
                                .append(GtfField.frame, fields[7])
                                .append(GtfField.attributes, attributes)
                                .append(GtfField.blocks, blockDocList);
                        String geneId = attributes.get(GencodeGtfTag.gene_id);
                        if(!geneId.equals(previousGeneId)){//发现新的geneId
                            geneNum++;
                            if(geneNum > 1){//发现新的geneId 标注位置并返回
                                //System.out.println("第一次读取文件发现新的geneId 标注位置并返回 GeneId =" + geneId);
                                this.setLastLine(line);
                                return geneDoc;
                            }
                            transcriptNum ++;
                            transcriptDocList.add(transcriptDoc);
                            geneDoc = new Document()
                                    .append(GencodeGtfTag.gene_id, attributes.get(GencodeGtfTag.gene_id))
                                    .append(GtfField.seqname, chrName)
                                    .append(GtfField.source, fields[1])
                                    .append(GtfField.feature, fields[2])
                                    .append(GtfField.start, start)
                                    .append(GtfField.end, end)
                                    .append(GtfField.score, score)
                                    .append(GtfField.strand, fields[6])
                                    .append(GtfField.frame, fields[7])
                                    .append(GtfField.attributes, attributes)
                                    .append(GtfField.transcripts, transcriptDocList);
//                            System.out.println("geneDoc=========A:"+geneDoc);
                            previousGeneId = geneId;
                        }else {
                            transcriptNum ++;
                            transcriptDocList.add(transcriptDoc);
                        }
                    } else if (fields[2].equals(GtfFeatureType.regulatory_region)){
                        Map<String,String> attributes = new HashMap<>();
                        String attributeStr = fields[8].trim().replaceAll("\"", "");
                        if (attributeStr.charAt(attributeStr.length() - 1) == ';') {
                            attributeStr = attributeStr.substring(0, attributeStr.length() - 1);
                        }
                        String[] attrPairStrArray = attributeStr.split(";");
                        for (String attrPairStr : attrPairStrArray) {
                            attrPairStr = attrPairStr.trim();
                            String[] attrPair = attrPairStr.split("=");
                            //System.err.println("AAA3:[" + attrPair[0] + "],[" + attrPair[1] + "]" );
                            attributes.put(attrPair[0], attrPair[1]);
                        }
                        geneDoc = new Document()
                                .append(GencodeGtfTag.ID, attributes.get(GencodeGtfTag.ID))
                                .append(GtfField.seqname, chrName)
                                .append(GtfField.source, fields[1])
                                .append(GtfField.feature, fields[2])
                                .append(GtfField.start, start)
                                .append(GtfField.end, end)
                                .append(GtfField.score, score)
                                .append(GtfField.strand, fields[6])
                                .append(GtfField.frame, fields[7])
                                .append(GtfField.attributes, attributes);
                        return geneDoc;
                    } else {
                        Document blockDocument = new Document()
                                .append(GtfField.seqname, chrName)
                                .append(GtfField.source, fields[1])
                                .append(GtfField.feature, fields[2])
                                .append(GtfField.start, start)
                                .append(GtfField.end, end)
                                .append(GtfField.score, score)
                                .append(GtfField.strand, fields[6])
                                .append(GtfField.frame, fields[7]);
                        blockDocList.add(blockDocument);
                    }
                }
            }else {//后续读取文件
                //处理标记行的信息
                line = this.getLastLine();
                String[] fields = line.trim().split("\\t");
                String chrName = fields[0];
                if (!chrName.startsWith("chr")) {
                    // for Ensembl format gtf
                    chrName = new StringBuilder("chr").append(chrName).toString();
                }

                int start = Integer.valueOf(fields[3]);
                int end = Integer.valueOf(fields[4]);

                String scoreStr = fields[5];
                float score = 0;
                if (!scoreStr.equals(".")) {
                    score = Float.valueOf(scoreStr);
                }
                if (fields[2].equals(GtfFeatureType.transcript)) {
                    Map<String,String> attributes = new HashMap<>();
                    String attributeStr = fields[8].trim().replaceAll("\"", "");
                    if (attributeStr.charAt(attributeStr.length() - 1) == ';') {
                        attributeStr = attributeStr.substring(0, attributeStr.length() - 1);
                    }
                    String[] attrPairStrArray = attributeStr.split(";");
                    for (String attrPairStr : attrPairStrArray) {
                        attrPairStr = attrPairStr.trim();
                        String[] attrPair = attrPairStr.split(" ");
                        //System.err.println("AAA3:[" + attrPair[0] + "],[" + attrPair[1] + "]" );
                        attributes.put(attrPair[0], attrPair[1]);
                    }
                    blockDocList = new ArrayList<>();
                    transcriptDocList = new ArrayList<>();
                    transcriptDoc = new Document();
                    transcriptDoc.append(GencodeGtfTag.transcript_id,attributes.get(GencodeGtfTag.transcript_id))
                            .append(GtfField.seqname, chrName)
                            .append(GtfField.source, fields[1])
                            .append(GtfField.feature, fields[2])
                            .append(GtfField.start, start)
                            .append(GtfField.end, end)
                            .append(GtfField.score, score)
                            .append(GtfField.strand, fields[6])
                            .append(GtfField.frame, fields[7])
                            .append(GtfField.attributes, attributes)
                            .append(GtfField.blocks, blockDocList);
                    transcriptDocList.add(transcriptDoc);

                    String geneId = attributes.get(GencodeGtfTag.gene_id);
                    if(!geneId.equals(previousGeneId)){//发现新的geneId
                        geneDoc = new Document()
                                .append(GencodeGtfTag.gene_id, attributes.get(GencodeGtfTag.gene_id))
                                .append(GtfField.seqname, chrName)
                                .append(GtfField.source, fields[1])
                                .append(GtfField.feature, fields[2])
                                .append(GtfField.start, start)
                                .append(GtfField.end, end)
                                .append(GtfField.score, score)
                                .append(GtfField.strand, fields[6])
                                .append(GtfField.frame, fields[7])
                                .append(GtfField.attributes, attributes)
                                .append(GtfField.transcripts, transcriptDocList);
                        //geneList.add(geneDoc);
                        previousGeneId = geneId;
                    }else {
                        throw new Exception("Lastline存储信息有误:"+lastLine);
                    }
                }else if (fields[2].equals(GtfFeatureType.regulatory_region)){
                    Map<String,String> attributes = new HashMap<>();
                    String attributeStr = fields[8].trim().replaceAll("\"", "");
                    if (attributeStr.charAt(attributeStr.length() - 1) == ';') {
                        attributeStr = attributeStr.substring(0, attributeStr.length() - 1);
                    }
                    String[] attrPairStrArray = attributeStr.split(";");
                    for (String attrPairStr : attrPairStrArray) {
                        attrPairStr = attrPairStr.trim();
                        String[] attrPair = attrPairStr.split("=");
                        //System.err.println("AAA3:[" + attrPair[0] + "],[" + attrPair[1] + "]" );
                        attributes.put(attrPair[0], attrPair[1]);
                    }
                    geneDoc = new Document()
                            .append(GencodeGtfTag.ID, attributes.get(GencodeGtfTag.ID))
                            .append(GtfField.seqname, chrName)
                            .append(GtfField.source, fields[1])
                            .append(GtfField.feature, fields[2])
                            .append(GtfField.start, start)
                            .append(GtfField.end, end)
                            .append(GtfField.score, score)
                            .append(GtfField.strand, fields[6])
                            .append(GtfField.frame, fields[7])
                            .append(GtfField.attributes, attributes);
                    return geneDoc;
                }else {
                    throw new Exception("Lastline存储信息有误:"+lastLine);
                }
                //处理标记行之后的信息
                while ((line = reader.readLine())!=null){
//                    System.out.println("处理标记行之后的信息");
                    fields = line.trim().split("\\t");
                    if (line.startsWith("#")) {
                        continue;
                    }
                    if (fields[2].equals(GtfFeatureType.gene)) {//跳过gene列，直接从transcript中读取gene信息
                        continue;
                    }
                    chrName = fields[0];
                    if (!chrName.startsWith("chr")) {
                        // for Ensembl format gtf
                        chrName = new StringBuilder("chr").append(chrName).toString();
                    }

                    start = Integer.valueOf(fields[3]);
                    end = Integer.valueOf(fields[4]);

                    scoreStr = fields[5];
                    score = 0;
                    if (!scoreStr.equals(".")) {
                        score = Float.valueOf(scoreStr);
                    }
                    if (fields[2].equals(GtfFeatureType.transcript)){
                        Map<String,String> attributes = new HashMap<>();
                        String attributeStr = fields[8].trim().replaceAll("\"", "");
                        if (attributeStr.charAt(attributeStr.length() - 1) == ';') {
                            attributeStr = attributeStr.substring(0, attributeStr.length() - 1);
                        }
                        String[] attrPairStrArray = attributeStr.split(";");
                        for (String attrPairStr : attrPairStrArray) {
                            attrPairStr = attrPairStr.trim();
                            String[] attrPair = attrPairStr.split(" ");
                            //System.err.println("AAA3:[" + attrPair[0] + "],[" + attrPair[1] + "]" );
                            attributes.put(attrPair[0], attrPair[1]);
                        }
                        blockDocList = new ArrayList<>();
//                        transcriptDocList = new ArrayList<>();
                        transcriptDoc = new Document();
                        transcriptDoc.append(GencodeGtfTag.transcript_id,attributes.get(GencodeGtfTag.transcript_id))
                                .append(GtfField.seqname, chrName)
                                .append(GtfField.source, fields[1])
                                .append(GtfField.feature, fields[2])
                                .append(GtfField.start, start)
                                .append(GtfField.end, end)
                                .append(GtfField.score, score)
                                .append(GtfField.strand, fields[6])
                                .append(GtfField.frame, fields[7])
                                .append(GtfField.attributes, attributes)
                                .append(GtfField.blocks, blockDocList);

                        String geneId = attributes.get(GencodeGtfTag.gene_id);
                        if(!geneId.equals(previousGeneId)){//发现新的geneId
                            geneNum++;
                            if(geneNum >= 1){//发现新的geneId 标注位置并返回
				if (geneNum % 1000 == 0) {
                                    System.out.println("读取" + geneNum + " 个 GENE");
                                }
                                this.setLastLine(line);
                                //return geneList;
                                return geneDoc;
                            }

                        }else {
                            transcriptNum ++;
                            transcriptDocList = (List<Document>) geneDoc.get(GtfField.transcripts);
                            transcriptDocList.add(transcriptDoc);
                        }

                    }else{
                        Document blockDocument = new Document()
                                .append(GtfField.seqname, chrName)
                                .append(GtfField.source, fields[1])
                                .append(GtfField.feature, fields[2])
                                .append(GtfField.start, start)
                                .append(GtfField.end, end)
                                .append(GtfField.score, score)
                                .append(GtfField.strand, fields[6])
                                .append(GtfField.frame, fields[7]);
                        blockDocList.add(blockDocument);
                    }

                }

                }
        }catch (Exception e){
            e.printStackTrace();
        }
//        return geneList;
        return geneDoc;
    }



}
