package com.colorseq.abrowse.fileutil;

import com.colorseq.abrowse.BedGraphField;
import com.colorseq.abrowse.GeoSpatialUtils;
import com.colorseq.abrowse.HiCField;
import com.mongodb.client.model.geojson.LineString;
import org.bson.Document;

import java.io.FileReader;
import java.util.Map;

public class HiCReader  extends BioDataReader{

    public HiCReader(FileReader file) {
        super(file);
    }

    @Override
    Document readEntry() throws Exception {
        return null;
    }

    public Document readEntry (Map<String, Integer> chrCodeMap, Map<String, Integer> chrLenMap) throws Exception{
        String line = null;
        Document recordDoc = null;
        String binIndex = null;

        try {
            int count = 0;
            while ((line = reader.readLine()) != null){
                String[] fields = line.split("\\t");
                String chrName = fields[3];
                int start = Integer.valueOf(fields[4]);
                int end = Integer.valueOf(fields[5]);

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
                        .append(HiCField.chr, chrName)
                        .append(HiCField.start, start)
                        .append(HiCField.end, end)
                        .append(HiCField.location,location);

                count++;
                return recordDoc;
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        return recordDoc;
    }
}
