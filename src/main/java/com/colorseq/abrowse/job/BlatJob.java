package com.colorseq.abrowse.job;

import com.colorseq.abrowse.dao.AbrowseJobDao;
import com.colorseq.abrowse.dao.BlatResultPSLDao;
import com.colorseq.abrowse.entity.AbrowseJob;
import com.colorseq.abrowse.entity.BlatResultPSL;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

public class BlatJob extends Thread{

    private String jobId;

    private File queryFile;

    private String seq;

    private BlatResultPSLDao blatResultPSLDao;

    private AbrowseJobDao jobDao;

    public BlatJob(String jobId, File queryFile, String seq, BlatResultPSLDao blatResultPSLDao, AbrowseJobDao jobDao){
        this.jobId = jobId;
        this.queryFile = queryFile;
        this.seq = seq;
        this.blatResultPSLDao = blatResultPSLDao;
        this.jobDao = jobDao;
    }

    @Override
    public void run() {
        if (jobId == null){
            return;
        }
        try {
//            FileOutputStream fos = new FileOutputStream(queryFile);
//            fos.write(seq.getBytes());
//            fos.flush();
//            fos.close();
//            String cmd = "/root/bin/x86_64/blat /project/user/zhouhan/data/Homo_sapiens.GRCh38.dna.chromosome.fa " + queryFile.getCanonicalPath() + " /project/user/zhouhan/data/output.psl";
//            System.out.println(cmd);
//            Process proc = Runtime.getRuntime().exec(cmd);
//            int i = proc.waitFor();
//            System.out.println(i);
//            BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
//            String line = null;
//            while ((line = in.readLine()) != null) {
//                System.out.println(line);
//            }
//            in.close();
//            File output = new File("/project/user/zhouhan/data/output.psl");
            File output = new File("D:\\data\\abrowse\\output.psl");
            if (!output.exists()){
                return;
            }
            BufferedReader outReader = new BufferedReader(new InputStreamReader(new FileInputStream(output)));
            String outLine = null;
            boolean isData = false;
            Pattern p = Pattern.compile("\\s+");
            List<BlatResultPSL> retList = new ArrayList<>();

            while ((outLine = outReader.readLine()) != null){
                if (outLine.startsWith("------------------")){
                    isData = true;
                    continue;
                }
                if (isData){
                    String[] item = p.split(outLine);
                    int index = 0;
                    BlatResultPSL blatResultPSL = new BlatResultPSL();
                    blatResultPSL.setId(UUID.randomUUID().toString().replace("-",""));
                    blatResultPSL.setJobid(jobId);
                    blatResultPSL.setBmatch(item[index++]);
                    blatResultPSL.setMismatch(item[index++]);
                    blatResultPSL.setRepmatch(item[index++]);
                    blatResultPSL.setNs(item[index++]);
                    blatResultPSL.setQgapbases(item[index++]);
                    blatResultPSL.setQgapcount(item[index++]);
                    blatResultPSL.setTgapbases(item[index++]);
                    blatResultPSL.setTgapcount(item[index++]);
                    blatResultPSL.setStrand(item[index++]);
                    blatResultPSL.setQname(item[index++]);
                    blatResultPSL.setQsize(item[index++]);
                    blatResultPSL.setQstart(item[index++]);
                    blatResultPSL.setQend(item[index++]);
                    blatResultPSL.setTname(item[index++]);
                    blatResultPSL.setTsize(item[index++]);
                    blatResultPSL.setTstart(item[index++]);
                    blatResultPSL.setTend(item[index++]);
                    blatResultPSL.setBlockcount(item[index++]);
                    blatResultPSL.setBlocksize(item[index++]);
                    blatResultPSL.setQstarts(item[index++]);
                    blatResultPSL.setTstarts(item[index++]);
                    blatResultPSL.setCreateTime(new Date());
                    blatResultPSL.setCompleteTime(new Date());
                    blatResultPSL.setCurStatu("02");
                    blatResultPSLDao.saveAndFlush(blatResultPSL);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public File getQueryFile() {
        return queryFile;
    }

    public void setQueryFile(File queryFile) {
        this.queryFile = queryFile;
    }

    public String getSeq() {
        return seq;
    }

    public void setSeq(String seq) {
        this.seq = seq;
    }
}
