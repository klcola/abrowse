package com.colorseq.abrowse;


//import com.colorseq.cscore.dao.user.*;
import com.colorseq.abrowse.fileutil.GtfReader;
import com.colorseq.abrowse.fileutil.LocationUtil;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.geojson.LineString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.bson.Document;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.LocaleResolver;

import javax.print.Doc;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Lei Kong
 */
@Controller
public class DataUtilController {

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
    private UserConfigGenomeDao userConfigGenomeDao;
    private DataUtilService dataUtilService;

    @Autowired
    LocationUtil locationUtil;

    @Autowired
    private MessageSource messageSource;

    public DataUtilController() {
    }

    @Autowired
    public void setConfigGenomeDao(ConfigGenomeDao configGenomeDao) {
        this.configGenomeDao = configGenomeDao;
    }

    @Autowired
    public void setUserConfigGenomeDao(UserConfigGenomeDao userConfigGenomeDao) {
        this.userConfigGenomeDao = userConfigGenomeDao;
    }

    @Autowired
    public void setDataUtilService(DataUtilService dataUtilService) {
        this.dataUtilService = dataUtilService;
    }

    @Autowired
    LocaleResolver localeResolver;



    @RequestMapping(value = "/test", method = RequestMethod.GET)
    public String test() {
        return "redirect:/";
    }

    @RequestMapping(value = "/admin/", method = RequestMethod.GET)
    public String admin_index(Map<String, Object> modelMap,
                              Model model,
                              HttpSession session,
                              Authentication authentication, HttpServletRequest request, HttpServletResponse response) {
        int userId = 0;
        if (null != authentication) {
            UserEntity userEntity = (UserEntity) authentication.getPrincipal();
            userId = userEntity.getId();
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
        Locale locale = LocaleContextHolder.getLocale();

        //localeResolver.setLocale(request,response,Locale.CHINESE);

        //model.addAttribute("create",messageSource.getMessage("create",null,locale));
        Map<String, ConfigGenome> configGenomeMap4Html = new HashMap<>();
        configGenomeMap4Html.putAll(configGenomeMap);
        configGenomeMap4Html.remove(global);
        modelMap.put("configGenomeMap", configGenomeMap4Html);

        return "admin__index";
    }

    @RequestMapping(value = "/admin/create_track_group", method = RequestMethod.GET)
    public String admin_create_track_group_GET(String genome,
                                               String trackGroupName,
                                               String trackGroupDisplayName,
                                               Map<String, Object> modelMap,
                                               HttpSession session,
                                               Authentication authentication) {

        int userId = 0;
        if (null != authentication) {
            UserEntity userEntity = (UserEntity) authentication.getPrincipal();
            userId = userEntity.getId();

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

        modelMap.put("configGenomeMap", configGenomeMap);
        return "admin__create_track_group";
    }


    @RequestMapping(value = "/admin/create_track_group", method = RequestMethod.POST)
    public String admin_create_track_group(String genome,
                                           String trackGroupName,
                                           String trackGroupDisplayName,
                                           Map<String, Object> modelMap,
                                           HttpSession session,
                                           Authentication authentication) {
        int userId = 0;
        if (null != authentication) {
            UserEntity userEntity = (UserEntity) authentication.getPrincipal();
            userId = userEntity.getId();
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
        ConfigTrackGroup configTrackGroup = new ConfigTrackGroup();
        configTrackGroup.setName(trackGroupName);
        configTrackGroup.setDisplayName(trackGroupDisplayName);
        currentConfigGenome.addTrackGroup(configTrackGroup);

        this.configGenomeDao.save(currentConfigGenome);

        session.removeAttribute(SessionKeys.CONFIG_GENOME_MAP);
        modelMap.put("configGenome", currentConfigGenome);
        return "redirect:/";
    }

    @RequestMapping(value = "/admin/delete_track", method = RequestMethod.GET )
    public String adminDeleteTrackGet(String genome,
                                      Map<String, Object> modelMap,
                                      HttpSession session,
                                      Authentication authentication) {

        int userId = 0;
        if (null != authentication) {
            UserEntity userEntity = (UserEntity) authentication.getPrincipal();
            userId = userEntity.getId();
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
        if (null == genome) {
            genome = this.defaultGenome;
        }
        ConfigGenome currentConfigGenome = configGenomeMap.get(genome);
        modelMap.put("configGenome", currentConfigGenome);
        return "admin__delete_track";

    }

    @RequestMapping(value = "/admin/delete_track", method = RequestMethod.POST)
    public String adminDeleteTrackPost(String genome, String trackName,
                                       Map<String, Object> modelMap,
                                       HttpSession session,
                                       Authentication authentication) {

        int userId = 0;
        if (null != authentication) {
            UserEntity userEntity = (UserEntity) authentication.getPrincipal();
            userId = userEntity.getId();
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

        if (null == genome) {
            genome = this.defaultGenome;
        }

        ConfigGenome currentConfigGenome = configGenomeMap.get(genome);
        Collection<ConfigTrackGroup> configTrackGroups = currentConfigGenome.getTrackGroupMap().values();
        for (ConfigTrackGroup configTrackGroup : configTrackGroups ) {
            ConfigTrack configTrack = configTrackGroup.getTrackMap().get(trackName);
            if (null != configTrack) {
                configTrackGroup.getTrackMap().remove(trackName);
                break;
            }
        }

        MongoClientURI mongoClientURI = new MongoClientURI(mongodbURI);
        MongoClient mongoClient = new MongoClient(mongoClientURI);

        String dbName = new StringBuilder(mongodbNamePrefix).append(genome).toString();
        MongoDatabase database = mongoClient.getDatabase(dbName);
        MongoCollection<Document> collection = database.getCollection(trackName);

        //drop the old before load data
        collection.drop();

        this.configGenomeDao.save(currentConfigGenome);
        session.removeAttribute(SessionKeys.CONFIG_GENOME_MAP);
        //modelMap.put("configGenome", currentConfigGenome);
        return "redirect:/";
    }

    @RequestMapping(value = "/admin/delete_track_group", method = RequestMethod.GET )
    public String adminDeleteTrackGroupGet(String genome,
                                          Map<String, Object> modelMap,
                                          HttpSession session,
                                          Authentication authentication) {

        int userId = 0;
        if (null != authentication) {
            UserEntity userEntity = (UserEntity) authentication.getPrincipal();
            userId = userEntity.getId();

        }
        if (null == genome) {
            genome = this.defaultGenome;
        }

        ConfigGenome currentConfigGenome = this.dataUtilService.loadAdminPage(genome,userId,session);

        System.out.println("currentConfigGenome:======="+currentConfigGenome.getTrackGroupMap().get("value"));

        modelMap.put("configGenome", currentConfigGenome);
        return "admin__delete_track_group";

    }

    @ResponseBody
    @RequestMapping(value = "/admin/check_track_group", method = RequestMethod.POST)
    public Map<String,Object> adminCheckTrackGroup(String genome, String trackGroupName,
                                                  Map<String, Object> modelMap,
                                                  HttpSession session,
                                                  Authentication authentication){
        Map<String,Object> resultMap = new HashMap<>();

        int userId = 0;
        boolean isEmpty = false;
        if (null != authentication) {
            UserEntity userEntity = (UserEntity) authentication.getPrincipal();
            userId = userEntity.getId();

        }
        if (null == genome) {
            genome = this.defaultGenome;
        }
        ConfigGenome currentConfigGenome = this.dataUtilService.loadAdminPage(genome,userId,session);

        ConfigTrackGroup configTrackGroup = currentConfigGenome.getTrackGroupMap().get(trackGroupName);
        Map<String, ConfigTrack> trackMap = configTrackGroup.getTrackMap();
        System.out.println("trackMap========="+trackMap);
        if(trackMap == null || trackMap.size()<1){
            isEmpty = true;
        }
        resultMap.put("isEmpty",isEmpty);

        return resultMap;
    }

    /**
     * 管理员删除trackGroup
     * @param genome
     * @param trackGroupName
     * @param modelMap
     * @param session
     * @param authentication
     * @return
     */
    @RequestMapping(value = "/admin/delete_track_group", method = RequestMethod.POST)
    public String adminDeleteTrackGroupPost(String genome, String trackGroupName,
                                           Map<String, Object> modelMap,
                                           HttpSession session,
                                           Authentication authentication) {

        int userId = 0;
        if (null != authentication) {
            UserEntity userEntity = (UserEntity) authentication.getPrincipal();
            userId = userEntity.getId();
        }
        if (null == genome) {
            genome = this.defaultGenome;
        }
        ConfigGenome currentConfigGenome = this.dataUtilService.loadAdminPage(genome,userId,session);

        Map<String,ConfigTrackGroup> configTrackGroupMap = currentConfigGenome.getTrackGroupMap();
        ConfigTrackGroup configTrackGroup = configTrackGroupMap.get(trackGroupName);


        //如果group下面没有track信息 则可以删除
        if(configTrackGroup.getTrackMap() == null || configTrackGroup.getTrackMap().size() < 1){
            System.out.println("configTrackGroupMap======="+configTrackGroupMap);

            configTrackGroupMap.remove(trackGroupName);

            this.configGenomeDao.save(currentConfigGenome);
        }else{
            System.out.println("group下面有track信息，无法删除trackGroup!");
        }
        session.removeAttribute(SessionKeys.CONFIG_GENOME_MAP);
        return "redirect:/";
    }



    @RequestMapping(value = "/admin/load_hic_data", method = RequestMethod.GET)
    public String adminLoadHiCGit(String genome, Map<String, Object> modelMap,
                                  HttpSession session,
                                  Authentication authentication){
        int userId = 0;
        if (null != authentication) {
            UserEntity userEntity = (UserEntity) authentication.getPrincipal();
            userId = userEntity.getId();
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
        if (null == genome) {
            genome = this.defaultGenome;
        }
        ConfigGenome currentConfigGenome = configGenomeMap.get(genome);

        List<String> genomeList = new ArrayList<String>();
        for (String geno : configGenomeMap.keySet()){
            if("global".equals(geno)){
                continue;
            }
            genomeList.add(geno);
        }
        modelMap.put("defaultGenome",genome);
        modelMap.put("genomeList",genomeList);
        modelMap.put("configGenome", currentConfigGenome);
        return "admin__load_hic_data";
    }

    @RequestMapping(value = "/admin/load_hic_data", method = RequestMethod.POST)
    public String adminLoadHiCPost(String genome,
                                   String hicPath,
                                   String trackGroupName,
                                   String trackName,
                                   String trackDisplayName,
                                   String trackDescription,
                                   Map<String, Object> modelMap,
                                   HttpSession session,
                                   Authentication authentication) throws IOException {
        int userId = 0;
        if (null != authentication) {
            UserEntity userEntity = (UserEntity) authentication.getPrincipal();
            userId = userEntity.getId();
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

        try{
            Document pair = null;
            BufferedReader reader = new BufferedReader(new FileReader(hicPath));
            String line = null;
            int preBinIndex = -1;
            List<Document> binList = new ArrayList<>();
            Document document = new Document();
            int insertMultiDocsNum = 5000;
            ArrayList<Document> documentList = new ArrayList<>(insertMultiDocsNum);
            int size = 0;
            while ((line = reader.readLine()) != null){
                if (line.contains("chr_x") || line.trim().length() == 0){
                    continue;
                }
                String[] fields = line.trim().split("\\t");
                String chrName = fields[3];
                int start = Integer.parseInt(fields[4]);
                int end = Integer.parseInt(fields[5]);
                int binIndex = Integer.parseInt(fields[0]);
                String targetChr = fields[7];
                if (!targetChr.startsWith("chr")){
                    targetChr = "chr" + targetChr;
                }
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
                if (binIndex != preBinIndex){
                    if (preBinIndex != -1){
                        document.append(HiCField.binList,binList);
                        documentList.add(document);
                        if (documentList.size() >= insertMultiDocsNum) {
                            collection.insertMany(documentList);
                            documentList.clear();
                            documentList.ensureCapacity(insertMultiDocsNum);
                            size += insertMultiDocsNum;
                            System.out.println("LOADED RECORDS:" + size);
                        }
                    }
                    preBinIndex = binIndex;
                    document = new Document()
                            .append(HiCField.chr,chrName)
                            .append(HiCField.start,start)
                            .append(HiCField.end,end)
                            .append(HiCField.location,location)
                            .append(HiCField.binIndex,binIndex);
                    binList = new ArrayList<>();
                    Document bin = new Document().append(HiCField.targetBinIndex,fields[1])
                            .append(HiCField.value,fields[2])
                            .append(HiCField.targetChr, targetChr)
                            .append(HiCField.targetStart,Integer.parseInt(fields[8]))
                            .append(HiCField.targetend,Integer.parseInt(fields[9]));
                    binList.add(bin);
                }else {
                    Document bin = new Document().append(HiCField.targetBinIndex,fields[1])
                            .append(HiCField.value,fields[2])
                            .append(HiCField.targetChr, targetChr)
                            .append(HiCField.targetStart,Integer.parseInt(fields[8]))
                            .append(HiCField.targetend,Integer.parseInt(fields[9]));
                    binList.add(bin);
                }
            }
            if (documentList.size() > 0) {
                System.out.println("最后一次=====" + documentList.size());
                System.out.println("最后一个=====" + documentList.get(documentList.size()-1));
                collection.insertMany(documentList);
                documentList.clear();
            }
            collection.createIndex(Indexes.geo2dsphere(BedGraphField.location));
        }catch (Exception e){
            e.printStackTrace();
            throw e;
        }

        ConfigTrack configTrack = new ConfigTrack();
        configTrack.setName(trackName);
        configTrack.setDisplayName(trackDisplayName);
        configTrack.setDescription(trackDescription);
        ConfigTrackView generalTrackView = new ConfigTrackView(ViewType.HICView, null);
        configTrack.addView(generalTrackView);

        ConfigTrackGroup trackGroup = currentConfigGenome.getTrackGroupMap().get(trackGroupName);
        trackGroup.addTrack(configTrack);

        this.configGenomeDao.save(currentConfigGenome);
        session.removeAttribute(SessionKeys.CONFIG_GENOME_MAP);
        System.out.println("LOADING SPLICE SITES FROM SAM DATA - FINISHED. Track NAME: " + trackName);
        //modelMap.put("chrLen", chrLenMap);
        //modelMap.put("configGenome", currentConfigGenome);

        return "redirect:/";
    }


    @RequestMapping(value = "/admin/load_bedgraph_data", method = RequestMethod.GET)
    public String adminLoadBedGraphGet(String genome, Map<String, Object> modelMap,
                                       HttpSession session,
                                       Authentication authentication) {

        int userId = 0;
        if (null != authentication) {
            UserEntity userEntity = (UserEntity) authentication.getPrincipal();
            userId = userEntity.getId();
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
        if (null == genome) {
            genome = this.defaultGenome;
        }
        ConfigGenome currentConfigGenome = configGenomeMap.get(genome);

        List<String> genomeList = new ArrayList<String>();
        for (String geno : configGenomeMap.keySet()){
            if("global".equals(geno)){
                continue;
            }
            genomeList.add(geno);
        }
        modelMap.put("defaultGenome",genome);
        modelMap.put("genomeList",genomeList);
        modelMap.put("configGenome", currentConfigGenome);
        return "admin__load_bedgraph_data";
    }

    /**
     *
     * 导入 bedgraph 格式文件
     *
     * @return
     */
    @RequestMapping(value = "/admin/load_bedgraph_data", method = RequestMethod.POST)
    public String adminLoadBedGraphPost(String genome,
                                        String fileType,
                                        String dataPath,
                                        String plusPath,
                                        String minusPath,
                                        String trackGroupName,
                                        String trackName,
                                        String trackDisplayName,
                                        String trackDescription,
                                        boolean defaultOn,
                                        HttpSession session,
                                        Authentication authentication)
    {
        int userId = 0;
        if (null != authentication) {
            UserEntity userEntity = (UserEntity) authentication.getPrincipal();
            userId = userEntity.getId();
        }

        String trackFinalName = new StringBuilder("U").append(userId).append("_")
                .append(trackName).toString();

        ConfigGenome currentConfigGenome;
        if("1".equals(fileType)){
            currentConfigGenome = this.dataUtilService.loadBedGraphTrack(genome,userId,trackFinalName,
                    trackGroupName,trackDisplayName,trackDescription,dataPath,defaultOn,session);
        }else {
            currentConfigGenome = this.dataUtilService.loadBedGraphTrackPlusAndMinus(genome,userId,trackFinalName,
                    trackGroupName,trackDisplayName,trackDescription,plusPath,minusPath,defaultOn,session);
        }
        /*Map<String, ConfigGenome> configGenomeMap = null;
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

            BufferedReader reader = new BufferedReader(new FileReader(dataPath));

            String line = null;
            int size = 0;

            int insertMultiDocsNum = 5000;
            ArrayList<Document> documentList = new ArrayList<>(insertMultiDocsNum);

            while ((line = reader.readLine()) != null) {

                if (documentList.size() >= insertMultiDocsNum) {
                    collection.insertMany(documentList);
                    documentList.clear();
                    documentList.ensureCapacity(insertMultiDocsNum);
                    size += insertMultiDocsNum;
                    System.out.println("LOADED RECORDS:" + size);
                }

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

                Document recordDoc = new Document()
                        .append(BedGraphField.chr, chrName)
                        .append(BedGraphField.start, start)
                        .append(BedGraphField.end, end)
                        .append(BedGraphField.value, value)
                        .append(BedGraphField.location, location);

                documentList.add(recordDoc);

            }

            if (documentList.size() > 0) {
                collection.insertMany(documentList);
                documentList.clear();
            }

            collection.createIndex(Indexes.geo2dsphere(BedGraphField.location));

        } catch (IOException e) {
            e.printStackTrace();
        }*/

        ConfigTrack configTrack = new ConfigTrack();
        configTrack.setName(trackFinalName);
        configTrack.setDisplayName(trackDisplayName);
        configTrack.setDescription(trackDescription);

        String viewType = null;
        if("1".equals(fileType)) {
            viewType = ViewType.BedGraphView;
        }else {
            viewType = ViewType.StrandBedGraphView;
        }

        if (defaultOn) {
            configTrack.setDefaultNormalView(viewType);
        }
        ConfigTrackView generalTrackView = new ConfigTrackView(viewType, null);
        configTrack.addView(generalTrackView);

        ConfigTrackGroup trackGroup = currentConfigGenome.getTrackGroupMap().get(trackGroupName);
        trackGroup.addTrack(configTrack);

        this.configGenomeDao.save(currentConfigGenome);
        session.removeAttribute(SessionKeys.CONFIG_GENOME_MAP);
        System.out.println("LOADING BEDGRAPH DATA - FINISHED. Track NAME: " + trackFinalName);

        return "redirect:/";
    }


    @RequestMapping(value = "/admin/load_abrowse_splicesite_data", method = RequestMethod.GET)
    public String admin_load_abrowse_splicesite_data_GET(String genome, Map<String, Object> modelMap,
                                                         HttpSession session,
                                                         Authentication authentication) {

        int userId = 0;
        if (null != authentication) {
            UserEntity userEntity = (UserEntity) authentication.getPrincipal();
            userId = userEntity.getId();
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
        if (null == genome) {
            genome = this.defaultGenome;
        }
        ConfigGenome currentConfigGenome = configGenomeMap.get(genome);

        List<String> genomeList = new ArrayList<String>();
        for (String geno : configGenomeMap.keySet()){
            if("global".equals(geno)){
                continue;
            }
            genomeList.add(geno);
        }
        modelMap.put("defaultGenome",genome);
        modelMap.put("genomeList",genomeList);

        modelMap.put("configGenome", currentConfigGenome);
        return "admin__load_abrowse_splicesite_data";
    }

    @RequestMapping(value = "/admin/load_abrowse_splicesite_data", method = RequestMethod.POST)
    public String admin_load_abrowse_splicesite_data(String genome,
                                                     String ssPath,
                                                     String trackGroupName,
                                                     String trackName,
                                                     String trackDisplayName,
                                                     String trackDescription,
                                                     Map<String, Object> modelMap,
                                                     HttpSession session,
                                                     Authentication authentication)
    {
        int userId = 0;
        if (null != authentication) {
            UserEntity userEntity = (UserEntity) authentication.getPrincipal();
            userId = userEntity.getId();
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

            BufferedReader reader = new BufferedReader(new FileReader(ssPath));
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

        this.configGenomeDao.save(currentConfigGenome);
        session.removeAttribute(SessionKeys.CONFIG_GENOME_MAP);
        System.out.println("LOADING SPLICE SITES FROM ABROWSE SS DATA - FINISHED. Track NAME: " + trackName);
        //modelMap.put("chrLen", chrLenMap);
        //modelMap.put("configGenome", currentConfigGenome);

        return "redirect:/";
    }



    @RequestMapping(value = "/admin/load_splicesite_data", method = RequestMethod.GET)
    public String admin_load_splicesite_data_GET(String genome, Map<String, Object> modelMap,
                                          HttpSession session,
                                          Authentication authentication) {

        int userId = 0;
        if (null != authentication) {
            UserEntity userEntity = (UserEntity) authentication.getPrincipal();
            userId = userEntity.getId();
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
        if (null == genome) {
            genome = this.defaultGenome;
        }
        ConfigGenome currentConfigGenome = configGenomeMap.get(genome);
        List<String> genomeList = new ArrayList<String>();
        for (String geno : configGenomeMap.keySet()){
            if("global".equals(geno)){
                continue;
            }
            genomeList.add(geno);
        }
        modelMap.put("defaultGenome",genome);
        modelMap.put("genomeList",genomeList);
        modelMap.put("configGenome", currentConfigGenome);
        return "admin__load_splicesite_data";
    }

    /*
        注意：本方法目前仅适用于 STAR2 的默认 sam 输出格式，而且需要使用samtools对sam文件按照染色体位置排序，
        该格式的以下特点对本方法能够正确工作至关重要：
        1. uniquely mapping reads 的 MAPQ 为255
        2. 比对结果按照染色体位置，从小到大排序
         */
    @RequestMapping(value = "/admin/load_splicesite_data", method = RequestMethod.POST)
    public String admin_load_splicesite_data(String genome,
                                      String samPath,
                                      String trackGroupName,
                                      String trackName,
                                      String trackDisplayName,
                                      String trackDescription,
                                      Map<String, Object> modelMap,
                                      HttpSession session,
                                      Authentication authentication)
    {
        int userId = 0;
        if (null != authentication) {
            UserEntity userEntity = (UserEntity) authentication.getPrincipal();
            userId = userEntity.getId();
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

            BufferedReader reader = new BufferedReader(new FileReader(samPath));
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

        this.configGenomeDao.save(currentConfigGenome);
        session.removeAttribute(SessionKeys.CONFIG_GENOME_MAP);
        System.out.println("LOADING SPLICE SITES FROM SAM DATA - FINISHED. Track NAME: " + trackName);
        //modelMap.put("chrLen", chrLenMap);
        //modelMap.put("configGenome", currentConfigGenome);

        return "redirect:/";
    }



    @RequestMapping(value = "/admin/load_sam_data", method = RequestMethod.GET)
    public String admin_load_sam_data_GET(String genome, Map<String, Object> modelMap,
                                          HttpSession session,
                                          Authentication authentication) {

        int userId = 0;
        if (null != authentication) {
            UserEntity userEntity = (UserEntity) authentication.getPrincipal();
            userId = userEntity.getId();
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
        if (null == genome) {
            genome = this.defaultGenome;
        }
        ConfigGenome currentConfigGenome = configGenomeMap.get(genome);

        modelMap.put("configGenome", currentConfigGenome);

        List<String> genomeList = new ArrayList<String>();
        for (String geno : configGenomeMap.keySet()){
            if("global".equals(geno)){
                continue;
            }
            genomeList.add(geno);
        }
        modelMap.put("defaultGenome",genome);
        modelMap.put("genomeList",genomeList);

        return "admin__load_sam_data";
    }

    /*
    注意：本方法目前仅适用于 STAR2 的默认 sam 输出格式，而且不可以（请注意是不可以）使用samtools排序后的sam文件。
    该格式的以下特点对本方法能够正确工作至关重要：
    1. uniquely mapping reads 的 MAPQ 为255
    2. pair-end reads的mapping结果上下相邻，且左侧在上，右侧在下
    例如：
    ST-E00493:99:HFGF3ALXX:5:1101:9881:46525        99      2       157415591       255     150M    =       157415743       302     CTTCTGCACTTTCCCACCAAGCTGGGATCTGGGTGAGTCTAGAGATATTTGTGAAATAAGCTAATTTGAAAGGACACCACAATCCGTCAAAAGCGACTTTCTTCCTCTTCCACAGCACGATGAAGGCCAGGGATAAATTTCAAGAGTTGC  <AFFFJJFJFAAAJJFJJFAJFAFJ<--FJJJFJJAJJFJJJFFAFFJ<FJJJFAA-FFF-7-<7FJJJJJJFAFAFAAFJFFFFFAAJJJJJJJJJ<J<FFAJFFFFFFAFJJF<JJJJJFAFJFA7AAA<JJ-7-<-7JJA7<<-7FF  NH:i:1  HI:i:1  AS:i:298        nM:i:0
    ST-E00493:99:HFGF3ALXX:5:1101:9881:46525        147     2       157415743       255     150M    =       157415591       -302    TCGGACACTTCCCTTTCTGCTCTTCCGGGGCAGGGTCCCAAACATGCTTGATAAGTTGCCCTCCCACAAGGGAGACATGGATCCGCTGCTGGTGTTACTGATGCTCCGGTTCCTCCTTGAAGATGACCTCCTCAGAAAATCATCCCCCTC  -A)<-A<JJ<<A7<A<F<F7-<7)<F-A--AA)JA<FFJJAJFJFAJ<FJF<<AF--<7AA<AFJJJFJJFF7AAA77FAFA<7JA<FJAFJFJJ<JJJA7FFFJAFFF-FAAAAJJFJJJJ<JA<JJAAFJJJJJJAF<77-AJFAAAA  NH:i:1  HI:i:1  AS:i:298        nM:i:0
     */
    @RequestMapping(value = "/admin/load_sam_data", method = RequestMethod.POST)
    public String admin_load_sam_data(String genome,
                                      String samPath,
                                      String trackGroupName,
                                      String trackName,
                                      String trackDisplayName,
                                      String trackDescription,
                                      Map<String, Object> modelMap,
                                      HttpSession session,
                                      Authentication authentication)
    {
        int userId = 0;
        if (null != authentication) {
            UserEntity userEntity = (UserEntity) authentication.getPrincipal();
            userId = userEntity.getId();
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

            BufferedReader reader = new BufferedReader(new FileReader(samPath));
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

        this.configGenomeDao.save(currentConfigGenome);
        session.removeAttribute(SessionKeys.CONFIG_GENOME_MAP);
        System.out.println("LOADING GTF DATA - FINISHED. Track NAME: " + trackName);
        //modelMap.put("chrLen", chrLenMap);
        modelMap.put("configGenome", currentConfigGenome);
        return "redirect:/";
    }

    @RequestMapping(path = "/admin/getCurrentConfigGenome", method = RequestMethod.POST)
    @ResponseBody
    public Map<String,Object> getCurrentConfigGenome(String genome,Authentication authentication,HttpSession session){
        Map<String,Object> resultMap = new HashMap<>();
        int userId = 0;
        if (null != authentication) {
            UserEntity userEntity = (UserEntity) authentication.getPrincipal();
            userId = userEntity.getId();
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
        if (null == genome) {
            genome = this.defaultGenome;
        }
        ConfigGenome currentConfigGenome = configGenomeMap.get(genome);
        resultMap.put("currenConfgGenome",currentConfigGenome);
        return resultMap;
    }

    @RequestMapping(value = "/admin/load_gencode_gtf_data", method = RequestMethod.GET)
    public String admin_load_gencode_gtf_data_GET(String genome, Map<String, Object> modelMap,
                                                  HttpSession session,
                                                  Authentication authentication) {

        int userId = 0;
        if (null != authentication) {
            UserEntity userEntity = (UserEntity) authentication.getPrincipal();
            userId = userEntity.getId();
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
        if (null == genome) {
            genome = this.defaultGenome;
        }
        ConfigGenome currentConfigGenome = configGenomeMap.get(genome);
        List<String> genomeList = new ArrayList<String>();
        for (String geno : configGenomeMap.keySet()){
            if("global".equals(geno)){
                continue;
            }
            genomeList.add(geno);
        }
        modelMap.put("defaultGenome",genome);
        modelMap.put("genomeList",genomeList);

        modelMap.put("configGenome", currentConfigGenome);
        return "admin__load_gencode_gtf_data";
    }

    @RequestMapping(value = "/admin/load_gencode_gtf_data", method = RequestMethod.POST)
    public String admin_load_gencode_gtf_data(String genome,
                                              String gtfPath,
                                              String trackGroupName,
                                              String trackName,
                                              String trackDisplayName,
                                              String trackDescription,
                                              String entryLink,
                                              Map<String, Object> modelMap,
                                              HttpSession session,
                                              Authentication authentication) {
        int userId = 0;
        if (null != authentication) {
            UserEntity userEntity = (UserEntity) authentication.getPrincipal();
            userId = userEntity.getId();
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
            GtfReader gtfReader = new GtfReader(new FileReader(gtfPath));
            int count = 0;

            while (gtfReader.ready()){//处理返回的基因


                geneDoc = gtfReader.readEntry();

                //System.out.println("以下是geneDocInfo");
                //System.out.println(geneDoc);

                //设置位置参数
                geneDoc = locationUtil.setLocationInfo(geneDoc,chrCodeMap,chrLenMap);

                if(geneDoc == null){
                    System.out.println("CCCCCCCCCCCCCCCC");
                    //忽略不支持的染色体
                    continue;
                }

                genomeList.add(geneDoc);


                count ++;


            }
	    System.out.println("BBBBBBBBBBBBBBBBB - genomeList.size():" + genomeList.size());
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

        this.configGenomeDao.save(currentConfigGenome);
        session.removeAttribute(SessionKeys.CONFIG_GENOME_MAP);
        System.out.println("LOADING GTF DATA - FINISHED. Track NAME: " + trackName);
        //modelMap.put("chrLen", chrLenMap);
        //modelMap.put("configGenome", currentConfigGenome);
        return "redirect:/";
    }
}
