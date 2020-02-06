/*
package com.colorseq.abrowse;

import com.colorseq.cscore.dao.Project;
import com.colorseq.cscore.dao.ProjectDao;
import com.colorseq.cscore.dao.user.*;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.geojson.LineString;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Con*/
/**//*
troller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpSession;
import java.io.*;
import java.security.Principal;
import java.util.*;


*/
/**
 * @author Lei Kong
 *//*

@Controller
public class ProjectDataUtilController {

    private String global = "global";

    @Value("${abrowse.default-genome}")
    private String defaultGenome;

    @Value("${abrowse.mongodb-name-prefix}")
    private String mongodbNamePrefix;

    @Value("${abrowse.mongodb-uri}")
    private String mongodbURI;

    @Value("${colorseq.project-base}")
    private String projectBase;

    @Value("${colorseq.pipeline.rnaseq.hisat2.splicesite-extension}")
    private String spliceSiteExt;

    private ProjectDao projectDao;
    private ConfigGenomeDao configGenomeDao;
    private UserConfigGenomeDao userConfigGenomeDao;

    @Autowired
    public void setProjectDao(ProjectDao projectDao) {
        this.projectDao = projectDao;
    }

    @Autowired
    public void setConfigGenomeDao(ConfigGenomeDao configGenomeDao) {
        this.configGenomeDao = configGenomeDao;
    }

    @Autowired
    public void setUserConfigGenomeDao(UserConfigGenomeDao userConfigGenomeDao) {
        this.userConfigGenomeDao = userConfigGenomeDao;
    }

    @RequestMapping(value = "/user/{p}/load_data", method = RequestMethod.GET)
    public String loadProjectDataGet(@PathVariable int p, String genome, Map<String, Object> modelMap,
                                     HttpSession session, Authentication authentication) {

        UserEntity userEntity = (UserEntity) authentication.getPrincipal();
        String username = userEntity.getUsername();
        Project project = this.projectDao.findByIdAndUsername(p, username);
        if (null == project) {
            Map<String, String> infoMap = new Hashtable<>();
            infoMap.put("info", "项目不存在");
            modelMap.put("infoMap", infoMap);
            return "process_info";
        }

        int userId = userEntity.getId();
        System.out.println("DEBUG userId:" + userId);

        if (null == genome) {
            genome = this.defaultGenome;
        }

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

        UserConfigGenome userConfigGenome = this.createUserProjectTrackGroup(genome, project, userId);
        this.loadStringTieGtfFile(genome, project, userId, currentConfigGenome, userConfigGenome);
        this.loadSpliceSiteFiles(genome, project.getId(), userId, currentConfigGenome, userConfigGenome);

        // 强制后续网页刷新 ConfigGenome 配置
        session.removeAttribute(SessionKeys.CONFIG_GENOME_MAP);

        return "redirect:/";
    }

    private String getUserProjectTrackGroupName(int projectId) {
        return new StringBuilder("project_").append(projectId).toString();
    }

    private UserConfigGenome createUserProjectTrackGroup(String genome, Project project, int userId) {


        UserConfigGenome userConfigGenome = this.userConfigGenomeDao.findByUserIdAndName(userId, genome);
        if (null == userConfigGenome) {
            userConfigGenome = new UserConfigGenome(userId, genome);
        }

        String trackGroupName = this.getUserProjectTrackGroupName(project.getId());
        String trackGroupDisplayName = project.getName();

        ConfigTrackGroup configTrackGroup = new ConfigTrackGroup();
        configTrackGroup.setName(trackGroupName);
        configTrackGroup.setDisplayName(trackGroupDisplayName);
        userConfigGenome.getConfigGenome().addTrackGroup(configTrackGroup);

        this.userConfigGenomeDao.save(userConfigGenome);

        return userConfigGenome;
    }

    private void loadStringTieGtfFile(String genome, Project project, int userId, ConfigGenome currentConfigGenome,
                                      UserConfigGenome userConfigGenome) {

        int p = project.getId();
        String trackGroupName = this.getUserProjectTrackGroupName(p);
        String trackDisplayName = new StringBuilder("StringTie GTF of ").append(project.getName()).toString();
        String gtfPath = new StringBuilder(this.projectBase).append("/").append(p)
                .append("/results/020.assembl_by_stringtie/transcriptome.final.gtf").toString();
        String trackName = new StringBuilder("u").append(userId).append("_p").append(p)
                .append("_stringtie_gtf").toString();

        Map<String, Integer> chrCodeMap = new HashMap<>();
        for (ConfigChromosome chr : currentConfigGenome.getChromosomeMap().values()) {
            //System.out.println("DEBUG CHR_NAME: " + chr.getName() + ", " + chr.getCode());
            chrCodeMap.put(chr.getName(), chr.getCode());
        }

        Map<String, Integer> chrLenMap = new HashMap<>();
        for (ConfigChromosome chr : currentConfigGenome.getChromosomeMap().values()) {
            //System.out.println("DEBUG CHR_LEN: " + chr.getName() + ", " + chr.getLength());
            chrLenMap.put(chr.getName(), chr.getLength());
        }

        MongoClientURI mongoClientURI = new MongoClientURI(mongodbURI);
        MongoClient mongoClient = new MongoClient(mongoClientURI);

        String dbName = new StringBuilder(mongodbNamePrefix).append(genome).toString();
        MongoDatabase database = mongoClient.getDatabase(dbName);
        MongoCollection<Document> collection = database.getCollection(trackName);

        //drop the old before load data
        collection.drop();



        System.out.println("LOADING GTF DATA - BEGIN. Track NAME: " + trackName);
        try {

            int insertMultiDocsNum = 5000;
            int docsCounter = 1;
            int writtenCounter = 0;
            String prevGeneId = null;

            Document geneDoc = null;
            Document transcriptDoc = null;
            List<Document> transcriptDocList = null;
            List<Document> blockDocList = null;

            BufferedReader reader = new BufferedReader(new FileReader(gtfPath));
            ArrayList<Document> geneDocumentList = new ArrayList<>(insertMultiDocsNum + 2);
            String line = null;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#")) {
                    continue;
                }

                String[] fields = line.trim().split("\\t");

                String chrName = fields[0];
                if (!chrName.startsWith("chr")) {
                    // for Ensembl format gtf
                    chrName = new StringBuilder("chr").append(chrName).toString();
                }
                Integer chrCode = chrCodeMap.get(chrName);
                if (null == chrCode) {
                    continue;
                }

                int chrLen = chrLenMap.get(chrName);

                int start = Integer.valueOf(fields[3]);

                int end = Integer.valueOf(fields[4]);

                String scoreStr = fields[5];
                float score = 0;
                if (!scoreStr.equals(".")) {
                    score = Float.valueOf(scoreStr);
                }

                Map<String, String> attributes = new HashMap<>();


                if (fields[2].equals(GtfFeatureType.transcript)) {

                    String attributeStr = fields[8].trim().replaceAll("\"", "");
                    if (attributeStr.charAt(attributeStr.length() - 1) == ';') {
                        attributeStr = attributeStr.substring(0, attributeStr.length() - 1);
                    }
                    String[] attrPairStrArray = attributeStr.split(";");
                    for (String attrPairStr : attrPairStrArray) {
                        attrPairStr = attrPairStr.trim();
                        String[] attrPair = attrPairStr.split(" ");
                        attributes.put(attrPair[0], attrPair[1]);
                    }

                    String geneId = attributes.get(StringTieGtfTag.gene_id);
                    if (! geneId.equals(prevGeneId)) {

                        prevGeneId = geneId;
                        ++writtenCounter;

                        if (docsCounter == insertMultiDocsNum) {
                            collection.insertMany(geneDocumentList);
                            System.out.println("LOADING GTF DATA - " + writtenCounter + " genes written");
                            geneDocumentList.clear();
                            geneDocumentList.ensureCapacity(insertMultiDocsNum + 2);
                            docsCounter = 1;
                        } else {
                            ++docsCounter;
                        }

                        LineString location = GeoSpatialUtils.getLineString(start, end, chrLen, chrCode);

                        transcriptDocList = new ArrayList<>();

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
                                .append(GtfField.location, location)
                                .append(GtfField.transcripts, transcriptDocList);

                        geneDocumentList.add(geneDoc);
                    }

                    blockDocList = new ArrayList<>();
                    transcriptDoc = new Document()
                            .append(GencodeGtfTag.transcript_id, attributes.get(GencodeGtfTag.transcript_id))
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
                    //.append(GtfField.attributes, attributes);
                    blockDocList.add(blockDocument);
                }
            }

            if (geneDocumentList.size() > 0) {
                collection.insertMany(geneDocumentList);
                System.out.println("LOADING GTF DATA - " + writtenCounter + " genes written");
                geneDocumentList.clear(); //optional ?
            }

            collection.createIndex(Indexes.text(GencodeGtfTag.gene_id));
            collection.createIndex(Indexes.geo2dsphere(GtfField.location));

        } catch (Exception e) {
            e.printStackTrace();
        }

        ConfigTrack configTrack = new ConfigTrack();
        configTrack.setName(trackName);
        configTrack.setDisplayName(trackDisplayName);
        configTrack.setDescription("");
        configTrack.setDefaultNormalView("GeneModelLikeView");
        ConfigTrackView generalTrackView = new ConfigTrackView("GeneModelLikeView", null);
        ConfigTrackView denseTrackView = new ConfigTrackView("GeneModelDenseView", null);
        configTrack.addView(generalTrackView);
        configTrack.addView(denseTrackView);

        ConfigTrackGroup trackGroup = userConfigGenome.getConfigGenome().getTrackGroupMap().get(trackGroupName);
        trackGroup.addTrack(configTrack);

        this.userConfigGenomeDao.save(userConfigGenome);
        System.out.println("LOADING GTF DATA - FINISHED. Track NAME: " + trackName);
    }

    private void loadSpliceSiteFiles(String genome, int p, int userId, ConfigGenome currentConfigGenome,
                                       UserConfigGenome userConfigGenome) {

        String trackGroupName = this.getUserProjectTrackGroupName(p);

        Map<String, Integer> chrCodeMap = new HashMap<>();
        for (ConfigChromosome chr : currentConfigGenome.getChromosomeMap().values()) {
            //System.out.println("DEBUG CHR_NAME: " + chr.getName() + ", " + chr.getCode());
            chrCodeMap.put(chr.getName(), chr.getCode());
        }

        Map<String, Integer> chrLenMap = new HashMap<>();
        for (ConfigChromosome chr : currentConfigGenome.getChromosomeMap().values()) {
            //System.out.println("DEBUG CHR_LEN: " + chr.getName() + ", " + chr.getLength());
            chrLenMap.put(chr.getName(), chr.getLength());
        }

        MongoClientURI mongoClientURI = new MongoClientURI(mongodbURI);
        MongoClient mongoClient = new MongoClient(mongoClientURI);

        String dbName = new StringBuilder(mongodbNamePrefix).append(genome).toString();
        MongoDatabase database = mongoClient.getDatabase(dbName);

        String hisatDirPath = new StringBuilder(this.projectBase).append("/").append(p)
                .append("/results/010.mapping_by_hisat2/hisat2/sample/").toString();

        System.out.println("DEBUG hisatDirPath:" + hisatDirPath);

        File hisatDir = new File(hisatDirPath);

        try {

            // create new filename filter
            FilenameFilter fileNameFilter = new FilenameFilter() {

                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(spliceSiteExt);
                }
            };

            File[] spliceSiteFileArray = hisatDir.listFiles(fileNameFilter);

            System.out.println("DEBUG: splicesite file num:" + spliceSiteFileArray.length);

            for (File spliceSiteFile : spliceSiteFileArray) {

                String fileName = spliceSiteFile.getName();
                String sampleName = fileName.replace(spliceSiteExt, "");
                String trackName = new StringBuilder("u").append(userId).append("_p").append(p)
                        .append("_").append(sampleName).toString();
                MongoCollection<Document> collection = database.getCollection(trackName);

                //drop the old before load data
                collection.drop();

                System.out.println("DEBUG loading splicesite data from " + fileName);
                BufferedReader reader = new BufferedReader(new FileReader(spliceSiteFile));
                String line = null;
                int size = 0;
                int cacheSize = 9999;
                ArrayList<Document> documentList = new ArrayList<>(cacheSize + 2);
                while ((line = reader.readLine()) != null) {

                    if (size > cacheSize) {
                        collection.insertMany(documentList);
                        size = 0;
                        documentList.clear();
                        documentList.ensureCapacity(cacheSize + 2);
                    }

                    String[] fields = line.split("\\t");

                    String chrName = fields[0];
                    if (!chrName.startsWith("chr")) {
                        chrName = new StringBuilder("chr").append(chrName).toString();
                    }

                    Integer chrCode = chrCodeMap.get(chrName);
                    if (null == chrCode) {
                        // 忽略不支持的染色体
                        continue;
                    }

                    int left = Integer.valueOf(fields[1]);
                    int right = Integer.valueOf(fields[2]);
                    int depth = Integer.valueOf(fields[3]);
                    int chrLen = chrLenMap.get(chrName);

                    Document spliceSite = new Document();
                    spliceSite.append(SpliceSiteField.left, left);
                    spliceSite.append(SpliceSiteField.right, right);
                    spliceSite.append(SpliceSiteField.depth, depth);

                    LineString location = GeoSpatialUtils.getLineString(left, right, chrLen, chrCode);
                    spliceSite.append(SpliceSiteField.location, location);
                    documentList.add(spliceSite);
                    ++ size;
                }

                if (! documentList.isEmpty()) {
                    collection.insertMany(documentList);
                }

                System.out.println("DEBUG - CREATING LOCATION INDEX ...");
                collection.createIndex(Indexes.geo2dsphere(SamField.location));
                System.out.println("DEBUG - FINISH CREATING LOCATION INDEX");

                ConfigTrack configTrack = new ConfigTrack();
                configTrack.setName(trackName);
                configTrack.setDisplayName(sampleName + " SpliceSites");
                configTrack.setDescription("");
                ConfigTrackView generalTrackView = new ConfigTrackView("RNASeqSpliceSiteView", null);
                configTrack.addView(generalTrackView);

                ConfigTrackGroup trackGroup = userConfigGenome.getConfigGenome().getTrackGroupMap().get(trackGroupName);
                trackGroup.addTrack(configTrack);

                System.out.println("DEBUG loading splicesite data from " + fileName + " finished.");
            }

        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.userConfigGenomeDao.save(userConfigGenome);

    }

}
*/
