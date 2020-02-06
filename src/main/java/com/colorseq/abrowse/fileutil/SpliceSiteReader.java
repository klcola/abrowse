package com.colorseq.abrowse.fileutil;

import org.bson.Document;

import java.io.BufferedReader;
import java.io.FileReader;

/**
 * @Author: Zeshan Cheng
 * @Date: 2018/7/2 上午11:52
 * @ClassDescription:SpliceSite格式的文件处理类
 */
public class SpliceSiteReader extends BioDataReader{

    public SpliceSiteReader(FileReader file){
        super(file);
    }

    @Override
    public Document readEntry() throws Exception {
        return null;
    }
}
