package com.colorseq.abrowse.fileutil;

import org.bson.Document;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * @Author: Zeshan Cheng
 * @Date: 2018/7/2 下午3:14
 * @ClassDescription:生物信息文件处理接口
 */
public abstract class BioDataReader {

        public BufferedReader reader ;

        public BioDataReader(FileReader file){
                this.reader = new BufferedReader(file);
        }

        public boolean ready(){
                try {
                    return reader.ready();
                }catch (IOException e){
                    e.printStackTrace();
                }
                return false;
        }

        abstract Document readEntry() throws Exception;

}
