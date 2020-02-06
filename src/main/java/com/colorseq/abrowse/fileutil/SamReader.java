package com.colorseq.abrowse.fileutil;

import org.bson.Document;

import java.io.FileReader;

/**
 * @Author: Zeshan Cheng
 * @Date: 2018/7/2 上午10:20
 * @ClassDescription:SAM格式的文件处理类
 */
public class SamReader extends BioDataReader{

    public SamReader(FileReader file){
        super(file);
    }

    @Override
    Document readEntry() throws Exception {
        return null;
    }
}
