package com.colorseq.abrowse;

import com.colorseq.abrowse.fileutil.BedGraphReader;
import com.colorseq.abrowse.fileutil.GtfReader;
import com.colorseq.abrowse.fileutil.LocationUtil;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.geojson.LineString;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author: Zeshan Cheng
 * @Date: 2018/10/18 下午4:39
 * @ClassDescription:
 */
@Service
public class DataUtilService {

    private static final Pattern CIGAR_PATTERN_N = Pattern.compile("\\d+N");
    private static final Pattern CIGAR_PATTERN_ALL = Pattern.compile("(\\d+)([MIDNSHP=X])");

    private String global = "global";

    @Value("${abrowse.default-genome}")
    private String defaultGenome;

    @Value("${abrowse.mongodb-name-prefix}")
    private String mongodbNamePrefix;

    @Value("${abrowse.mongodb-uri}")
    private String mongodbURI;

    private ConfigGenomeDao configGenomeDao;

    @Autowired
    LocationUtil locationUtil;

    @Autowired
    public void setConfigGenomeDao(ConfigGenomeDao configGenomeDao) {
        this.configGenomeDao = configGenomeDao;
    }

    private UserConfigGenomeDao userConfigGenomeDao;

    @Autowired
    public void setUserConfigGenomeDao(UserConfigGenomeDao userConfigGenomeDao) {
        this.userConfigGenomeDao = userConfigGenomeDao;
    }

    /**
     * 普通用戶加載頁面
     * @param genome
     * @param userId
     * @param session
     * @return
     */
    public ConfigGenome loadCommonUserPage(String genome,int userId,HttpSession session){
        Map<String, ConfigGenome> configGenomeMap = null;
        if (null == genome) {
            genome = this.defaultGenome;
        }
        UserConfigGenome userConfigGenome = this.userConfigGenomeDao.findByUserIdAndName(userId,genome);
        if (null == configGenomeMap) {

            ConfigGenomeMapRetriever configGenomeMapRetriever = new ConfigGenomeMapRetriever();
            configGenomeMap = configGenomeMapRetriever.retrieve(userId, configGenomeDao, userConfigGenomeDao);
            session.setAttribute(SessionKeys.CONFIG_GENOME_MAP, configGenomeMap);
        }

        ConfigGenome userCommonConfigGenome = userConfigGenome.getConfigGenome();
        return userCommonConfigGenome;
    }

    /**
     * 管理員加載頁面
     * @param genome
     * @param userId
     * @param session
     * @return
     */
    public ConfigGenome loadAdminPage(String genome,int userId,HttpSession session){
        Map<String, ConfigGenome> configGenomeMap = null;
        try {
            configGenomeMap = (Map<String, ConfigGenome>) session.getAttribute(SessionKeys.CONFIG_GENOME_MAP);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (null == configGenomeMap) {

            ConfigGenomeMapRetriever configGenomeMapRetriever = new ConfigGenomeMapRetriever();
            configGenomeMap = configGenomeMapRetriever.retrieve(userId, configGenomeDao, userConfigGenomeDao);
            session.setAttribute(SessionKeys.CONFIG_GENOME_MAP, configGenomeMap);
        }
        if (null == genome) {
            genome = this.defaultGenome;
        }
        ConfigGenome currentConfigGenome = configGenomeMap.get(genome);

        return currentConfigGenome;
    }




    public ConfigGenome loadBedGraphTrack(String genome, int userId,
                                          String trackName,
                                          String trackGroupName,
                                          String trackDisplayName,
                                          String trackDescription,
                                          String dataPath,
                                          boolean defaultOn,HttpSession session){

        Map<String, ConfigGenome> configGenomeMap = null;
        try {
            configGenomeMap = (Map<String, ConfigGenome>) session.getAttribute(SessionKeys.CONFIG_GENOME_MAP);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (null == configGenomeMap) {

            ConfigGenomeMapRetriever configGenomeMapRetriever = new ConfigGenomeMapRetriever();
            configGenomeMap = configGenomeMapRetriever.retrieve(userId, configGenomeDao, userConfigGenomeDao);
            session.setAttribute(SessionKeys.CONFIG_GENOME_MAP, configGenomeMap);
        }
        ConfigGenome currentConfigGenome = configGenomeMap.get(genome);
        ConfigGenome globalConfigGenome = configGenomeMap.get(global);
        if (null != globalConfigGenome) {
            currentConfigGenome.addViews(globalConfigGenome.getViewMap());
        }

        Map<String, Integer> chrCodeMap = new HashMap<>();
        for (ConfigChromosome chr : currentConfigGenome.getChromosomeMap().values()) {
            chrCodeMap.put(chr.getName(), chr.getCode());
        }

        Map<String, Integer> chrLenMap = new HashMap<>();
        for (ConfigChromosome chr : currentConfigGenome.getChromosomeMap().values()) {
            chrLenMap.put(chr.getName(), chr.getLength());
        }


        MongoClientURI mongoClientURI = new MongoClientURI(mongodbURI);
        MongoClient mongoClient = new MongoClient(mongoClientURI);

        String dbName = new StringBuilder(mongodbNamePrefix).append(genome).toString();
        MongoDatabase database = mongoClient.getDatabase(dbName);
        MongoCollection<Document> collection = database.getCollection(trackName);

        //drop the old before load data
        collection.drop();

        System.out.println("LOADING BEDGRAPH DATA - BEGIN. Track NAME: " + trackName);
        try {
            Document pair = null;
            //Document leftRead = null;

            BedGraphReader bedGraphReader = new BedGraphReader(new FileReader(dataPath));

            String line = null;
            int size = 0;

            int insertMultiDocsNum = 5000;
            ArrayList<Document> documentList = new ArrayList<>(insertMultiDocsNum);

            int count = 0;
            Document recordDoc = new Document();

//            while (bedGraphReader.ready()) {
                while ((recordDoc = bedGraphReader.readEntry(chrCodeMap,chrLenMap,"1"))!=null) {

                if (documentList.size() >= insertMultiDocsNum) {
                    collection.insertMany(documentList);
                    documentList.clear();
                    documentList.ensureCapacity(insertMultiDocsNum);
                    size += insertMultiDocsNum;
                    System.out.println("LOADED RECORDS:" + size);
                }

//                recordDoc = bedGraphReader.readEntry(chrCodeMap,chrLenMap,"1");

                if(count%50000 == 0){
                    System.out.println("recordDoc========"+recordDoc);
                }

                /*String[] fields = line.split("\\t");
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

                Document recordDoc = new Document()
                        .append(BedGraphField.chr, chrName)
                        .append(BedGraphField.start, start)
                        .append(BedGraphField.end, end)
                        .append(BedGraphField.value, value)
                        .append(BedGraphField.location, location);*/

                documentList.add(recordDoc);
                count++;

            }

            if (documentList.size() > 0) {
                System.out.println("最后一次====="+documentList.size());
                System.out.println("最后一个====="+documentList.get(documentList.size()-1));
                collection.insertMany(documentList);
                documentList.clear();
            }

            collection.createIndex(Indexes.geo2dsphere(BedGraphField.location));

        } catch (Exception e) {
            e.printStackTrace();
        }

        ConfigTrack configTrack = new ConfigTrack();
        configTrack.setName(trackName);
        configTrack.setDisplayName(trackDisplayName);
        configTrack.setDescription(trackDescription);
        if (defaultOn) {
            configTrack.setDefaultNormalView(ViewType.BedGraphView);
        }
        ConfigTrackView generalTrackView = new ConfigTrackView(ViewType.BedGraphView, null);
        configTrack.addView(generalTrackView);

        ConfigTrackGroup trackGroup = currentConfigGenome.getTrackGroupMap().get(trackGroupName);
        trackGroup.addTrack(configTrack);

        return currentConfigGenome;

    }


    public ConfigGenome loadBedGraphTrackPlusAndMinus(String genome, int userId,
                                          String trackName,
                                          String trackGroupName,
                                          String trackDisplayName,
                                          String trackDescription,
                                          String plusPath,
                                          String minusPath,
                                          boolean defaultOn,HttpSession session){

        Map<String, ConfigGenome> configGenomeMap = null;
        try {
            configGenomeMap = (Map<String, ConfigGenome>) session.getAttribute(SessionKeys.CONFIG_GENOME_MAP);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (null == configGenomeMap) {

            ConfigGenomeMapRetriever configGenomeMapRetriever = new ConfigGenomeMapRetriever();
            configGenomeMap = configGenomeMapRetriever.retrieve(userId, configGenomeDao, userConfigGenomeDao);
            session.setAttribute(SessionKeys.CONFIG_GENOME_MAP, configGenomeMap);
        }
        ConfigGenome currentConfigGenome = configGenomeMap.get(genome);
        ConfigGenome globalConfigGenome = configGenomeMap.get(global);
        if (null != globalConfigGenome) {
            currentConfigGenome.addViews(globalConfigGenome.getViewMap());
        }

        Map<String, Integer> chrCodeMap = new HashMap<>();
        for (ConfigChromosome chr : currentConfigGenome.getChromosomeMap().values()) {
            chrCodeMap.put(chr.getName(), chr.getCode());
        }

        Map<String, Integer> chrLenMap = new HashMap<>();
        for (ConfigChromosome chr : currentConfigGenome.getChromosomeMap().values()) {
            chrLenMap.put(chr.getName(), chr.getLength());
        }


        MongoClientURI mongoClientURI = new MongoClientURI(mongodbURI);
        MongoClient mongoClient = new MongoClient(mongoClientURI);

        String dbName = new StringBuilder(mongodbNamePrefix).append(genome).toString();
        MongoDatabase database = mongoClient.getDatabase(dbName);
        MongoCollection<Document> collection = database.getCollection(trackName);

        //drop the old before load data
        collection.drop();

        System.out.println("LOADING BEDGRAPH DATA - BEGIN. Track NAME: " + trackName);
        try {
            Document pair = null;
            //Document leftRead = null;

            //写入第一个文件
            BedGraphReader bedGraphPlusReader = new BedGraphReader(new FileReader(plusPath));

            String line = null;
            int size = 0;

            int insertMultiDocsNum = 5000;
            ArrayList<Document> documentList = new ArrayList<>(insertMultiDocsNum);

            Document recordDoc = new Document();
            while ((recordDoc = bedGraphPlusReader.readEntry(chrCodeMap,chrLenMap,"2"))!=null) {

                if (documentList.size() >= insertMultiDocsNum) {
                    collection.insertMany(documentList);
                    documentList.clear();
                    documentList.ensureCapacity(insertMultiDocsNum);
                    size += insertMultiDocsNum;
                    System.out.println("LOADED RECORDS:" + size);
                }



                documentList.add(recordDoc);

            }

            if (documentList.size() > 0) {
                collection.insertMany(documentList);
                documentList.clear();
            }

            //写入第二个文件

            recordDoc = new Document();
            BedGraphReader bedGraphMinusPathReader = new BedGraphReader(new FileReader(minusPath));

            line = null;
            size = 0;

            insertMultiDocsNum = 5000;
            documentList = new ArrayList<>(insertMultiDocsNum);

            recordDoc = new Document();
            while ((recordDoc = bedGraphMinusPathReader.readEntry(chrCodeMap,chrLenMap,"3"))!=null) {

                if (documentList.size() >= insertMultiDocsNum) {
                    collection.insertMany(documentList);
                    documentList.clear();
                    documentList.ensureCapacity(insertMultiDocsNum);
                    size += insertMultiDocsNum;
                    System.out.println("LOADED RECORDS:" + size);
                }

                documentList.add(recordDoc);

            }

            if (documentList.size() > 0) {
                collection.insertMany(documentList);
                documentList.clear();
            }



            collection.createIndex(Indexes.geo2dsphere(BedGraphField.location));

        } catch (Exception e) {
            e.printStackTrace();
        }

        ConfigTrack configTrack = new ConfigTrack();
        configTrack.setName(trackName);
        configTrack.setDisplayName(trackDisplayName);
        configTrack.setDescription(trackDescription);
        if (defaultOn) {
            configTrack.setDefaultNormalView(ViewType.StrandBedGraphView);
        }
        ConfigTrackView generalTrackView = new ConfigTrackView(ViewType.StrandBedGraphView, null);
        configTrack.addView(generalTrackView);

        ConfigTrackGroup trackGroup = currentConfigGenome.getTrackGroupMap().get(trackGroupName);
        trackGroup.addTrack(configTrack);

        return currentConfigGenome;

    }


    public ConfigGenome loadSplicesiteTrack(String genome, int userId,
                                          String trackName,
                                          String trackGroupName,
                                          String trackDisplayName,
                                          String trackDescription,
                                          String dataPath,
                                          HttpSession session){

        Map<String, ConfigGenome> configGenomeMap = null;
        try {
            configGenomeMap = (Map<String, ConfigGenome>) session.getAttribute(SessionKeys.CONFIG_GENOME_MAP);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (null == configGenomeMap) {

            ConfigGenomeMapRetriever configGenomeMapRetriever = new ConfigGenomeMapRetriever();
            configGenomeMap = configGenomeMapRetriever.retrieve(userId, configGenomeDao, userConfigGenomeDao);
            session.setAttribute(SessionKeys.CONFIG_GENOME_MAP, configGenomeMap);
        }
        ConfigGenome currentConfigGenome = configGenomeMap.get(genome);
        ConfigGenome globalConfigGenome = configGenomeMap.get(global);
        if (null != globalConfigGenome) {
            currentConfigGenome.addViews(globalConfigGenome.getViewMap());
        }

        Map<String, Integer> chrCodeMap = new HashMap<>();
        for (ConfigChromosome chr : currentConfigGenome.getChromosomeMap().values()) {
            chrCodeMap.put(chr.getName(), chr.getCode());
        }

        Map<String, Integer> chrLenMap = new HashMap<>();
        for (ConfigChromosome chr : currentConfigGenome.getChromosomeMap().values()) {
            chrLenMap.put(chr.getName(), chr.getLength());
        }

        MongoClientURI mongoClientURI = new MongoClientURI(mongodbURI);
        MongoClient mongoClient = new MongoClient(mongoClientURI);

        String dbName = new StringBuilder(mongodbNamePrefix).append(genome).toString();
        MongoDatabase database = mongoClient.getDatabase(dbName);
        MongoCollection<Document> collection = database.getCollection(trackName);

        //drop the old before load data
        collection.drop();

        System.out.println("LOADING SPLICE SITES FROM ABROWSE SS FORMAT DATA - BEGIN. Track NAME: " + trackName);
        try {
            Document pair = null;
            //Document leftRead = null;

            BufferedReader reader = new BufferedReader(new FileReader(dataPath));
            //List<Document> siteDocumentList = new ArrayList<>(5000);
            Map<String, Document> siteDocumentMap = new HashMap<>();



            String line = null;
            int entryNum = 0;
            int batchSize = 5000;
            ArrayList<Document> documentArrayList = new ArrayList<>(batchSize);
            while ((line = reader.readLine()) != null) {

                if (line.startsWith("#")) {
                    continue;
                }

                Document spliceSite = new Document();
                ++ entryNum;
                String[] fields = line.trim().split("\\t");
                String RNAME = fields[0];

                if (!RNAME.startsWith("chr")) {
                    RNAME = new StringBuilder("chr").append(RNAME).toString();
                }

                Integer chrCode = chrCodeMap.get(RNAME);
                if (null == chrCode) {
                    // 忽略不支持的染色体
                    continue;
                }

                int chrLen = chrLenMap.get(RNAME);

                int left = Integer.valueOf(fields[1]);
                int right = Integer.valueOf(fields[2]);
                spliceSite.append(SpliceSiteField.left, left);
                spliceSite.append(SpliceSiteField.right, right);
                spliceSite.append(SpliceSiteField.depth, Integer.valueOf(fields[3]));

                LineString location = GeoSpatialUtils.getLineString(left, right, chrLen, chrCode);
                spliceSite.append(SpliceSiteField.location, location);

                documentArrayList.add(spliceSite);

                if (entryNum % batchSize == 0) {
                    collection.insertMany(documentArrayList);
                    documentArrayList.clear();
                    System.out.println("DEBUG - " + entryNum + " ENTRIES INSERTED." );
                }
            }

            if (documentArrayList.size() > 0) {
                collection.insertMany(documentArrayList);
                documentArrayList.clear();
            }



            System.out.println("DEBUG - CREATING LOCATION INDEX ...");
            collection.createIndex(Indexes.geo2dsphere(SpliceSiteField.location));
            System.out.println("DEBUG - FINISH CREATING LOCATION INDEX");

        } catch (Exception e) {
            e.printStackTrace();
        }

        ConfigTrack configTrack = new ConfigTrack();
        configTrack.setName(trackName);
        configTrack.setDisplayName(trackDisplayName);
        configTrack.setDescription(trackDescription);
        ConfigTrackView generalTrackView = new ConfigTrackView(ViewType.RNASeqSpliceSiteView, null);
        configTrack.addView(generalTrackView);

        ConfigTrackGroup trackGroup = currentConfigGenome.getTrackGroupMap().get(trackGroupName);
        trackGroup.addTrack(configTrack);

        return currentConfigGenome;

    }

    public ConfigGenome loadSplicesiteTrackBySam(String genome, int userId,
                                            String trackName,
                                            String trackGroupName,
                                            String trackDisplayName,
                                            String trackDescription,
                                            String dataPath,
                                            HttpSession session){

        Map<String, ConfigGenome> configGenomeMap = null;
        try {
            configGenomeMap = (Map<String, ConfigGenome>) session.getAttribute(SessionKeys.CONFIG_GENOME_MAP);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (null == configGenomeMap) {

            ConfigGenomeMapRetriever configGenomeMapRetriever = new ConfigGenomeMapRetriever();
            configGenomeMap = configGenomeMapRetriever.retrieve(userId, configGenomeDao, userConfigGenomeDao);
            session.setAttribute(SessionKeys.CONFIG_GENOME_MAP, configGenomeMap);
        }
        ConfigGenome currentConfigGenome = configGenomeMap.get(genome);
        ConfigGenome globalConfigGenome = configGenomeMap.get(global);
        if (null != globalConfigGenome) {
            currentConfigGenome.addViews(globalConfigGenome.getViewMap());
        }

        Map<String, Integer> chrCodeMap = new HashMap<>();
        for (ConfigChromosome chr : currentConfigGenome.getChromosomeMap().values()) {
            chrCodeMap.put(chr.getName(), chr.getCode());
        }

        Map<String, Integer> chrLenMap = new HashMap<>();
        for (ConfigChromosome chr : currentConfigGenome.getChromosomeMap().values()) {
            chrLenMap.put(chr.getName(), chr.getLength());
        }


        MongoClientURI mongoClientURI = new MongoClientURI(mongodbURI);
        MongoClient mongoClient = new MongoClient(mongoClientURI);

        String dbName = new StringBuilder(mongodbNamePrefix).append(genome).toString();
        MongoDatabase database = mongoClient.getDatabase(dbName);
        MongoCollection<Document> collection = database.getCollection(trackName);

        //drop the old before load data
        collection.drop();

        System.out.println("LOADING SPLICE SITES FROM SAM DATA - BEGIN. Track NAME: " + trackName);
        try {
            Document pair = null;
            //Document leftRead = null;

            BufferedReader reader = new BufferedReader(new FileReader(dataPath));
            //List<Document> siteDocumentList = new ArrayList<>(5000);
            Map<String, Document> siteDocumentMap = new HashMap<>();

            int rightMostPrevPos = 0;
            int prevChrCode = 1;

            String line = null;
            int size = 0;
            while ((line = reader.readLine()) != null) {

                if (line.startsWith("@")) {
                    continue;
                }

                String[] fields = line.trim().split("\\t");

                //比对的质量分数，在STAR2的输出中，255表示 uniquely mapping
                int MAPQ = Integer.valueOf(fields[4]);

                if (255 != MAPQ) {
                    // 忽略非 uniquely mapping reads
                    continue;
                }

                /* 比对到参考序列上的染色体号 */
                String RNAME = fields[2];
                if (!RNAME.startsWith("chr")) {
                    RNAME = new StringBuilder("chr").append(RNAME).toString();
                }

                Integer chrCode = chrCodeMap.get(RNAME);
                if (null == chrCode) {
                    // 忽略不支持的染色体
                    continue;
                }

                if (prevChrCode != chrCode && siteDocumentMap.size() > 0 ) {
                    Collection<Document> documents = siteDocumentMap.values();
                    collection.insertMany(new ArrayList<>(documents));

                    size += siteDocumentMap.size();
                    System.out.println("LOADING SPLICE SITES FROM SAM DATA - " + size + " RECORDS WRITTEN.");
                    siteDocumentMap.clear();

                    prevChrCode = chrCode;
                }

                int chrLen = chrLenMap.get(RNAME);

                /* 1-based leftmost mapping POSition */
                int POS = Integer.valueOf(fields[3]);

                if (POS > rightMostPrevPos && siteDocumentMap.size() > 0) {
                    Collection<Document> documents = siteDocumentMap.values();
                    collection.insertMany(new ArrayList<>(documents));

                    size += siteDocumentMap.size();
                    System.out.println("LOADING SPLICE SITES FROM SAM DATA - " + size + " RECORDS WRITTEN.");
                    siteDocumentMap.clear();
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
                        Document spliceSite = siteDocumentMap.get(mapKey);
                        if (null == spliceSite) {
                            spliceSite = new Document();
                            spliceSite.append(SpliceSiteField.left, left);
                            spliceSite.append(SpliceSiteField.right, right);
                            spliceSite.append(SpliceSiteField.depth, 1);

                            LineString location = GeoSpatialUtils.getLineString(left, right, chrLen, chrCode);
                            spliceSite.append(SpliceSiteField.location, location);

                            siteDocumentMap.put(mapKey, spliceSite);
                        } else {
                            int depth = spliceSite.getInteger(SpliceSiteField.depth);
                            spliceSite.append(SpliceSiteField.depth, depth + 1);
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

            if (siteDocumentMap.size() > 0) {
                Collection<Document> documents = siteDocumentMap.values();
                collection.insertMany(new ArrayList<>(documents));

                size += siteDocumentMap.size();
                System.out.println("LOADING SPLICE SITES FROM SAM DATA - " + size + " RECORDS WRITTEN.");
                siteDocumentMap.clear();
            }

            System.out.println("DEBUG - CREATING LOCATION INDEX ...");
            collection.createIndex(Indexes.geo2dsphere(SpliceSiteField.location));
            System.out.println("DEBUG - FINISH CREATING LOCATION INDEX");

        } catch (Exception e) {
            e.printStackTrace();
        }

        ConfigTrack configTrack = new ConfigTrack();
        configTrack.setName(trackName);
        configTrack.setDisplayName(trackDisplayName);
        configTrack.setDescription(trackDescription);
        ConfigTrackView generalTrackView = new ConfigTrackView(ViewType.RNASeqSpliceSiteView, null);
        configTrack.addView(generalTrackView);

        ConfigTrackGroup trackGroup = currentConfigGenome.getTrackGroupMap().get(trackGroupName);
        trackGroup.addTrack(configTrack);


        return currentConfigGenome;

    }


    public ConfigGenome loadSamTrack(String genome, int userId,
                                            String trackName,
                                            String trackGroupName,
                                            String trackDisplayName,
                                            String trackDescription,
                                            String dataPath,
                                            HttpSession session){

        Map<String, ConfigGenome> configGenomeMap = null;
        try {
            configGenomeMap = (Map<String, ConfigGenome>) session.getAttribute(SessionKeys.CONFIG_GENOME_MAP);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (null == configGenomeMap) {

            ConfigGenomeMapRetriever configGenomeMapRetriever = new ConfigGenomeMapRetriever();
            configGenomeMap = configGenomeMapRetriever.retrieve(userId, configGenomeDao, userConfigGenomeDao);
            session.setAttribute(SessionKeys.CONFIG_GENOME_MAP, configGenomeMap);
        }
        ConfigGenome currentConfigGenome = configGenomeMap.get(genome);
        ConfigGenome globalConfigGenome = configGenomeMap.get(global);
        if (null != globalConfigGenome) {
            currentConfigGenome.addViews(globalConfigGenome.getViewMap());
        }

        Map<String, Integer> chrCodeMap = new HashMap<>();
        for (ConfigChromosome chr : currentConfigGenome.getChromosomeMap().values()) {
            chrCodeMap.put(chr.getName(), chr.getCode());
        }

        Map<String, Integer> chrLenMap = new HashMap<>();
        for (ConfigChromosome chr : currentConfigGenome.getChromosomeMap().values()) {
            chrLenMap.put(chr.getName(), chr.getLength());
        }

        int insertMultiDocsNum = 5000;
        int docsCounter = 1;
        int writtenCounter = 0;

        MongoClientURI mongoClientURI = new MongoClientURI(mongodbURI);
        MongoClient mongoClient = new MongoClient(mongoClientURI);

        String dbName = new StringBuilder(mongodbNamePrefix).append(genome).toString();
        MongoDatabase database = mongoClient.getDatabase(dbName);
        MongoCollection<Document> collection = database.getCollection(trackName);

        //drop the old before load data
        collection.drop();

        System.out.println("LOADING SAM DATA - BEGIN. Track NAME: " + trackName);
        try {
            Document pair = null;
            //Document leftRead = null;

            BufferedReader reader = new BufferedReader(new FileReader(dataPath));
            List<Document> pairDocumentList = new ArrayList<>(5000);
            String line = null;
            while ((line = reader.readLine()) != null) {

                if (line.startsWith("@")) {
                    continue;
                }

                String[] fields = line.trim().split("\\t");

                //比对的质量分数，在STAR2的输出中，255表示 uniquely mapping
                int MAPQ = Integer.valueOf(fields[4]);

                if (255 != MAPQ) {
                    // 忽略非 uniquely mapping reads
                    continue;
                }

                // Query template NAME
                String QNAME = fields[0];
                /*
                    sum of flags，每个flag用数字来表示，分别为：
                    1 read是pair中的一条（read表示本条read，mate表示pair中的另一条read）
                    2 pair一正一负完美的比对上
                    4 这条read没有比对上
                    8 mate没有比对上
                    16 这条read反向比对
                    32 mate反向比对
                    64 这条read是read1
                    128 这条read是read2
                    256 第二次比对
                    512 比对质量不合格
                    1024 read是PCR或光学副本产生
                    2048 辅助比对结果
                 */
                int FLAG = Integer.valueOf(fields[1]);

                /* 比对到参考序列上的染色体号 */
                String RNAME = fields[2];
                if (!RNAME.startsWith("chr")) {
                    RNAME = new StringBuilder("chr").append(RNAME).toString();
                }

                Integer chrCode = chrCodeMap.get(RNAME);
                if (null == chrCode) {
                    // 忽略不支持的染色体
                    continue;
                }

                int chrLen = chrLenMap.get(RNAME);

                /* 1-based leftmost mapping POSition */
                int POS = Integer.valueOf(fields[3]);

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

                /* RNEXT，下一个片段(mate)比对上的参考序列的编号，没有另外的片段，这里是’*‘，同一个片段，用’=‘；*/
                String RNEXT = fields[6];
                /* 忽略没有map到同一条染色体上的 read pair */
                if(! RNEXT.equals("=")) {
                    continue;
                }

                /* mate比对到参考序列上的第一个碱基位置，若无mate,则为0 */
                int PNEXT = Integer.valueOf(fields[7]);

                /*
                    signed observed Template LENgth. If all segments are mapped to the same reference, the
                    unsigned observed template length equals the number of bases from the leftmost mapped base to the
                    rightmost mapped base. The leftmost segment has a plus sign and the rightmost has a minus sign.
                    The sign of segments in the middle is undefined. It is set as 0 for single-segment template or when the
                    information is unavailable.
                 */
                int TLEN = Integer.valueOf(fields[8]);

                String SEQ = fields[9];
                String QUAL = fields[10];

                // do nothing, skip the optional fields
                String OptionalTAG = fields[11];

                /* 节省空间，暂不存储SEQ、QUAL及Optional TAG等信息 */
                Document read = new Document()
                        .append(SamField.QNAME, QNAME)
                        .append(SamField.FLAG, FLAG)
                        .append(SamField.RNAME, RNAME)
                        .append(SamField.POS, POS)
                        .append(SamField.MAPQ, MAPQ)
                        .append(SamField.CIGAR, CIGAR)
                        .append(SamField.RNEXT, RNEXT)
                        .append(SamField.PNEXT, PNEXT)
                        .append(SamField.TLEN, TLEN);

                // 在pair-end测序中，TLEN > 0 表明这个片段在左侧, TLEN < 0 表明片段在右侧
                if (TLEN > 0) {

                    int start = POS;
                    int end = start + TLEN;
                    LineString location = GeoSpatialUtils.getLineString(start, end, chrLen, chrCode);
                    pair = new Document();
                    pair.append(SamField.left, read);
                    pair.append(SamField.location, location);
                    pairDocumentList.add(pair);
                } else {
                    pair.append(SamField.right, read);
                    ++ writtenCounter;

                    if (docsCounter == insertMultiDocsNum) {
                        collection.insertMany(pairDocumentList);
                        System.out.println("LOADING SAM DATA - " + writtenCounter + " mapping pair written");
                        pairDocumentList.clear();
                        docsCounter = 1;
                    } else {
                        ++docsCounter;
                    }
                }

            }

            if (pairDocumentList.size() > 0) {
                collection.insertMany(pairDocumentList);
                System.out.println("LOADING SAM DATA - " + writtenCounter + " mapping pair written");
            }

            System.out.println("DEBUG - CREATING LOCATION INDEX ...");
            collection.createIndex(Indexes.geo2dsphere(SamField.location));
            System.out.println("DEBUG - FINISH CREATING LOCATION INDEX");

        } catch (Exception e) {
            e.printStackTrace();
        }

        ConfigTrack configTrack = new ConfigTrack();
        configTrack.setName(trackName);
        configTrack.setDisplayName(trackDisplayName);
        configTrack.setDescription(trackDescription);
        ConfigTrackView generalTrackView = new ConfigTrackView(ViewType.RNASeqRawDataView, null);
        configTrack.addView(generalTrackView);

        ConfigTrackGroup trackGroup = currentConfigGenome.getTrackGroupMap().get(trackGroupName);
        trackGroup.addTrack(configTrack);


        return currentConfigGenome;

    }


    public ConfigGenome loadGencodeGtfTrack(String genome, int userId,
                                     String trackName,
                                     String trackGroupName,
                                     String trackDisplayName,
                                     String trackDescription,
                                     String dataPath,
                                     String entryLink,
                                     HttpSession session){

        Map<String, ConfigGenome> configGenomeMap = null;
        try {
            configGenomeMap = (Map<String, ConfigGenome>) session.getAttribute(SessionKeys.CONFIG_GENOME_MAP);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (null == configGenomeMap) {

            ConfigGenomeMapRetriever configGenomeMapRetriever = new ConfigGenomeMapRetriever();
            configGenomeMap = configGenomeMapRetriever.retrieve(userId, configGenomeDao, userConfigGenomeDao);
            session.setAttribute(SessionKeys.CONFIG_GENOME_MAP, configGenomeMap);
        }
        ConfigGenome currentConfigGenome = configGenomeMap.get(genome);
        ConfigGenome globalConfigGenome = configGenomeMap.get(global);
        if (null != globalConfigGenome) {
            currentConfigGenome.addViews(globalConfigGenome.getViewMap());
        }

        Map<String, Integer> chrCodeMap = new HashMap<>();
        for (ConfigChromosome chr : currentConfigGenome.getChromosomeMap().values()) {
            chrCodeMap.put(chr.getName(), chr.getCode());
        }

        Map<String, Integer> chrLenMap = new HashMap<>();
        for (ConfigChromosome chr : currentConfigGenome.getChromosomeMap().values()) {
            chrLenMap.put(chr.getName(), chr.getLength());
        }

        int insertMultiDocsNum = 5000;
        int docsCounter = 1;
        int writtenCounter = 0;

        MongoClientURI mongoClientURI = new MongoClientURI(mongodbURI);
        MongoClient mongoClient = new MongoClient(mongoClientURI);

        String dbName = new StringBuilder(mongodbNamePrefix).append(genome).toString();
        MongoDatabase database = mongoClient.getDatabase(dbName);
        MongoCollection<Document> collection = database.getCollection(trackName);

        //drop the old before load data
        collection.drop();

        System.out.println("LOADING GTF DATA - BEGIN. Track NAME: " + trackName);
        try {
            List<Document> genomeList = new ArrayList();
            Document geneDoc = new Document();
            GtfReader gtfReader = new GtfReader(new FileReader(dataPath));
            int count = 0;

            while (gtfReader.ready()){//处理返回的基因


                geneDoc = gtfReader.readEntry();

                //System.out.println("以下是geneDocInfo");
                //System.out.println(geneDoc);

                //设置位置参数
                geneDoc = locationUtil.setLocationInfo(geneDoc,chrCodeMap,chrLenMap);

                if(geneDoc == null){
                    //忽略不支持的染色体
                    continue;
                }

                genomeList.add(geneDoc);


                count ++;


            }
            collection.insertMany(genomeList);


            System.out.println("LOADING GTF DATA - " + genomeList.size() + " genes written");
            genomeList.clear();


            collection.createIndex(Indexes.text(GencodeGtfTag.gene_id));
            collection.createIndex(Indexes.geo2dsphere(GtfField.location));

        } catch (Exception e) {
            e.printStackTrace();
        }

        ConfigTrack configTrack = new ConfigTrack();
        configTrack.setName(trackName);
        configTrack.setDisplayName(trackDisplayName);
        configTrack.setDescription(trackDescription);
        configTrack.setDefaultNormalView(ViewType.GeneModelLikeView);
        ConfigTrackView generalTrackView = new ConfigTrackView(ViewType.GeneModelLikeView, entryLink);

        // TODO : 在前端 Browser 中增加支持
        ConfigTrackView denseTrackView = new ConfigTrackView(ViewType.GeneModelDenseView, entryLink);
        configTrack.addView(generalTrackView);
        configTrack.addView(denseTrackView);

        ConfigTrackGroup trackGroup = currentConfigGenome.getTrackGroupMap().get(trackGroupName);
        trackGroup.addTrack(configTrack);

        return currentConfigGenome;
    }

}
