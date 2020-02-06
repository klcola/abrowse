package com.colorseq.abrowse.fileutil;

import com.colorseq.abrowse.BedGraphField;
import com.colorseq.abrowse.GeoSpatialUtils;
import com.mongodb.client.model.geojson.LineString;
import org.bson.Document;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Map;

/**
 * @Author: Zeshan Cheng
 * @Date: 2018/7/2 上午11:36
 * @ClassDescription:BedGraph格式的文件处理类
 */
public class BedGraphReader extends BioDataReader{

    public BedGraphReader(FileReader file){
        super(file);
    }

    @Override
    Document readEntry() throws Exception {
        return null;
    }

    public Document readEntry (Map<String, Integer> chrCodeMap,Map<String, Integer> chrLenMap,String fileType) throws Exception{
        String line = null;
        Document recordDoc = null;

        try {
            int count = 0;
            while ((line = reader.readLine()) != null){
                String[] fields = line.split("\\t");
                String chrName = fields[0];
                int start = Integer.valueOf(fields[1]);
                int end = Integer.valueOf(fields[2]);
                float value = Integer.valueOf(fields[3]);



                Integer chrCode = chrCodeMap.get(chrName);
                if (null == chrCode) {

                    chrName = "chr" + chrName;
                    chrCode = chrCodeMap.get(chrName);

                    if (null == chrCode) {
                        // 忽略不支持的染色体
                        continue;
                    }
                }
                int chrLen = chrLenMap.get(chrName);
                LineString location = GeoSpatialUtils.getLineString(start, end, chrLen, chrCode);
                //if(count%5000 == 0){
                //    System.out.println("location========"+location);
                //}
                recordDoc = new Document()
                        .append(BedGraphField.chr, chrName)
                        .append(BedGraphField.start, start)
                        .append(BedGraphField.end, end)
                        .append(BedGraphField.value, value)
                        .append(BedGraphField.location,location);
                if("1".equals(fileType)){
                    recordDoc.append(BedGraphField.strand,".");
                }else if("2".equals(fileType)){
                    recordDoc.append(BedGraphField.strand,"+");
                }else {
                    recordDoc.append(BedGraphField.strand,"-");
                }


                count++;
                return recordDoc;
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        return recordDoc;
    }

}
