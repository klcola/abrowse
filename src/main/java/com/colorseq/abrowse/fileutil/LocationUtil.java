package com.colorseq.abrowse.fileutil;

import com.colorseq.abrowse.GeoSpatialUtils;
import com.colorseq.abrowse.GtfField;
import com.mongodb.client.model.geojson.LineString;
import org.bson.Document;
import org.springframework.stereotype.Service;


import java.io.BufferedReader;
import java.util.Map;

/**
 * @Author: Zeshan Cheng
 * @Date: 2018/6/29 上午9:19
 * @ClassDescription:位置信息处理工具类
 */
@Service
public class LocationUtil {

    public Document setLocationInfo(Document geneDoc ,Map<String,Integer> chrCodeMap,Map<String,Integer> chrLenMap) throws Exception{
        if(geneDoc == null){
	    System.out.println("DDDDDDDDDDDDDDDDDDD");
            return null;
        }
        int start = Integer.parseInt(geneDoc.get(GtfField.start).toString());
        int end = Integer.parseInt(geneDoc.get(GtfField.end).toString());
        String chrName = geneDoc.get(GtfField.seqname).toString();
        if (!chrName.startsWith("chr")) {
            // for Ensembl format gtf
            chrName = new StringBuilder("chr").append(chrName).toString();
        }
        if(chrCodeMap == null){
            System.out.println("chrCodeMap不能为空");
            throw new Exception("chrCodeMap不能为空");
        }
        //java.util.Set s = chrCodeMap.keySet();
        //for (Object k:s) {
	//    System.out.print(k + ",");
	//}
        Integer chrCode = chrCodeMap.get(chrName);
        if (null == chrCode) {
            //忽略不支持的染色体
            return null;
        }
        if(chrLenMap == null){
            System.out.println("chrLenMap不能为空");
            throw new Exception("chrLenMap不能为空");
        }
        int chrLen = chrLenMap.get(chrName);

        LineString location = GeoSpatialUtils.getLineString(start, end, chrLen, chrCode);
        geneDoc.append(GtfField.location,location);

        return geneDoc;
    }
}
