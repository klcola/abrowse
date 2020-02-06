package com.colorseq.abrowse.cmd;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 本程序目前可以处理 STAR2 和 HISAT2 程序的输出 SAM 文件，SAM 文件需要按照染色体位置排序
 *
 * @author Lei Kong
 */
public class Sam2SpliceSite {

    private static final Pattern CIGAR_PATTERN_N = Pattern.compile("\\d+N");
    private static final Pattern CIGAR_PATTERN_ALL = Pattern.compile("(\\d+)([MIDNSHP=X])");

    public Sam2SpliceSite() {
    }

    public static void main(String[] args) {

        String samPath = args[0];
        String spliceSitePath = args[1];

        // STAR2 或者 HISAT2
        String program = args[2];

        if (null == program) {
            program = "HISAT2";
        }

        try {
            BufferedReader reader = new BufferedReader(new FileReader(samPath));

            BufferedWriter writer = new BufferedWriter(new FileWriter(spliceSitePath));


            Map<String, SpliceSite> spliceSiteMap = new HashMap<>();
            int rightMostPrevPos = 0;
            String prevChrName = null;

            String line = null;
            int size = 0;
            while ((line = reader.readLine()) != null) {

                if (line.startsWith("@")) {
                    continue;
                }

                // 在HISAT2的输出中，NH:i:1 表示uniquely mapping
                if (program.equals("HISAT2") && ! line.endsWith("NH:i:1")) {
                    continue;
                }

                String[] fields = line.trim().split("\\t");

                //比对的质量分数，在STAR2的输出中，255表示 uniquely mapping
                int MAPQ = Integer.valueOf(fields[4]);

                if (program.equals("STAR2") && 255 != MAPQ) {
                    // 忽略非 uniquely mapping reads
                    continue;
                }

                /* 比对到参考序列上的染色体号 */
                String RNAME = fields[2];
                if (!RNAME.startsWith("chr")) {
                    RNAME = new StringBuilder("chr").append(RNAME).toString();
                }
                if (null == prevChrName) {
                    prevChrName = RNAME;
                }

                if (! prevChrName.equals(RNAME) && spliceSiteMap.size() > 0 ) {


                    Collection<SpliceSite> spliceSites = spliceSiteMap.values();
                    for (SpliceSite spliceSite : spliceSites) {
                        writer.write(spliceSite.toRecordLine());
                        writer.newLine();
                    }
                    size += spliceSiteMap.size();
                    System.out.println("LOADING SPLICE SITES FROM SAM DATA - " + size + " RECORDS WRITTEN.");
                    spliceSiteMap.clear();

                    prevChrName = RNAME;
                }

                /* 1-based leftmost mapping POSition */
                int POS = Integer.valueOf(fields[3]);

                if (POS > rightMostPrevPos && spliceSiteMap.size() > 0) {
                    Collection<SpliceSite> spliceSites = spliceSiteMap.values();
                    for (SpliceSite spliceSite : spliceSites) {
                        writer.write(spliceSite.toRecordLine());
                        writer.newLine();
                    }
                    size += spliceSiteMap.size();
                    System.out.println("LOADING SPLICE SITES FROM SAM DATA - " + size + " RECORDS WRITTEN.");
                    spliceSiteMap.clear();
                }

                /*
                    Op BAM Description                                            Consumes_query  Consumes_reference
                    M  0   alignment match (can be a sequence match or mismatch)  yes             yes
                    I  1   insertion to the reference                             yes             no
                    D  2   deletion from the reference                            no              yes
                    N  3   skipped region from the reference                      no              yes
                    S  4   soft clipping (clipped sequences present in SEQ)       yes             no
                    H  5   hard clipping (clipped sequences NOT present in SEQ)   no              no
                    P  6   padding (silent deletion from padded reference)        no              no
                    =  7   sequence match                                         yes             yes
                    X  8   sequence mismatch                                      yes             yes

                    https://www.biostars.org/p/289583/
                    The CIGAR reports the operations performed on the reference FORWARD strand also if
                    the read comes from the reverse strand.
                    The sequence reported in the SEQ field is the one on the FORWARD strand,
                    even if the original read comes from the reverse strand (but the FLAG field has a 16 instead of 0)
                 */
                String CIGAR = fields[5];

                /* 只有出现 N 才会有剪切位点 */
                //Pattern patternN = Pattern.compile("\\d+N");
                //Matcher matcherN = patternN.matcher(CIGAR);
                Matcher matcherN = CIGAR_PATTERN_N.matcher(CIGAR);
                if (! matcherN.find()) {
                    continue;
                }


                //Pattern pattern = Pattern.compile("(\\d+)([MIDNSHP=X])");
                //Matcher matcher = pattern.matcher(CIGAR);
                Matcher matcher = CIGAR_PATTERN_ALL.matcher(CIGAR);
                int prevPos = POS;
                //String prevOp = null;

                while (matcher.find()) {
                    int len = Integer.valueOf(matcher.group(1));
                    String op = matcher.group(2);
                    if (op.equals("M") || op.equals("D") || op.equals("=") || op.equals("X") ) {
                        prevPos += len;
                    } else if (op.equals("N")) {
                        //Document spliceSite = new Document();
                        int left = prevPos;
                        int right = prevPos + len;
                        prevPos = right;


                        String mapKey = new StringBuilder().append(left).append(":").append(right).toString();
                        SpliceSite spliceSite = spliceSiteMap.get(mapKey);

                        if (null == spliceSite) {
                            spliceSite = new SpliceSite(RNAME, left, right, 1);
                            spliceSiteMap.put(mapKey, spliceSite);
                        } else {
                            spliceSite.setDepth(spliceSite.getDepth() + 1);
                        }
                    }
                }

                if (rightMostPrevPos < prevPos) {
                    rightMostPrevPos = prevPos;
                }

                /* RNEXT，下一个片段(mate)比对上的参考序列的编号，没有另外的片段，这里是’*‘，同一个片段，用’=‘；*/
                String RNEXT = fields[6];
                /* 忽略没有map到同一条染色体上的 read pair */
                if(! RNEXT.equals("=")) {
                    continue;
                }
            }

            if (spliceSiteMap.size() > 0) {


                Collection<SpliceSite> spliceSites = spliceSiteMap.values();
                for (SpliceSite spliceSite : spliceSites) {
                    writer.write(spliceSite.toRecordLine());
                    writer.newLine();
                }
                size += spliceSiteMap.size();
                System.out.println("LOADING SPLICE SITES FROM SAM DATA - " + size + " RECORDS WRITTEN.");
                spliceSiteMap.clear();
            }

            writer.flush();
            writer.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
