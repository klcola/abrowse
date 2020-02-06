package com.colorseq.abrowse;

//import com.colorseq.cscore.dao.user.*;
import com.colorseq.abrowse.fileutil.GtfReader;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.geojson.LineString;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import sun.security.krb5.Config;


import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;

/**
 * @Author: Zeshan Cheng
 * @Date: 2018/10/18 下午4:39
 * @ClassDescription:
 */
@Controller
public class UserDataUtilController {

    private String global = "global";

    @Value("${abrowse.default-genome}")
    private String defaultGenome;

    @Value("${abrowse.mongodb-name-prefix}")
    private String mongodbNamePrefix;

    @Value("${abrowse.mongodb-uri}")
    private String mongodbURI;

    private ConfigGenomeDao configGenomeDao;
    private UserConfigGenomeDao userConfigGenomeDao;
    private UserEntityDao userEntityDao;
    private DataUtilService dataUtilService;

    @Autowired
    private MessageSource messageSource;

    public UserDataUtilController(){

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
    public void setUserEntityDao(UserEntityDao userEntityDao) {
        this.userEntityDao = userEntityDao;
    }

    @Autowired
    public void setDataUtilService(DataUtilService dataUtilService) {
        this.dataUtilService = dataUtilService;
    }

    @RequestMapping(value = "/user/", method = RequestMethod.GET)
    public String user_index(Map<String, Object> modelMap,
                              String genome,
                              Model model,
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
        UserConfigGenome userConfigGenome = this.userConfigGenomeDao.findByUserIdAndName(userId,genome);
        if (userConfigGenome == null) {
            userConfigGenome = new UserConfigGenome(userId,genome);
            userConfigGenome.setUserId(userId);
            userConfigGenome.setName(this.defaultGenome);
            userConfigGenome.setTrackCount(1);

            /*ConfigGenome configGenome = new ConfigGenome();
            configGenome.setName(this.defaultGenome);
            configGenome.setDisplayName("Homo sapiens");

            userConfigGenome.setConfigGenome(configGenome);*/
            this.userConfigGenomeDao.save(userConfigGenome);
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
        //Locale locale = LocaleContextHolder.getLocale();
        //model.addAttribute("create",messageSource.getMessage("create",null,locale));
        modelMap.put("configGenomeMap", configGenomeMap);

        return "user__index";
    }

    @RequestMapping(value = "/user/create_track_group", method = RequestMethod.GET)
    public String user_create_track_group_GET(String genome,
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
        return "user__create_track_group";
    }



    /**
     * 普通用户新建track group
     * @param genome
     * @param trackGroupName
     * @param trackGroupDisplayName
     * @param modelMap
     * @param session
     * @param authentication
     * @return
     */
    @RequestMapping(value = "/user/create_track_group", method = RequestMethod.POST)
    public String user_create_track_group(String genome,
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

        UserConfigGenome userConfigGenome = this.userConfigGenomeDao.findByUserIdAndName(userId,genome);

        if(userConfigGenome == null){
            System.out.println("新建crack_group空了");
            userConfigGenome = new UserConfigGenome(userId,genome);
            userConfigGenome.setTrackCount(1);
        }

        //TODO:不完善
        userConfigGenome.setUserId(userId);
        userConfigGenome.setName(genome);
        userConfigGenome.setTrackCount(userConfigGenome.getTrackCount()+1);

        String trackGroupFinalName = new StringBuilder("U").append(userId).append("_")
                .append(userConfigGenome.getTrackCount()).toString();


        ConfigGenome currentConfigGenome = configGenomeMap.get(genome);


        ConfigTrackGroup configTrackGroup = new ConfigTrackGroup();
        configTrackGroup.setName(trackGroupFinalName);
        configTrackGroup.setDisplayName(trackGroupDisplayName);

        currentConfigGenome.addTrackGroup(configTrackGroup);

        //this.configGenomeDao.save(currentConfigGenome);

        userConfigGenome.addTrackGroup(configTrackGroup);

        this.userConfigGenomeDao.save(userConfigGenome);

        session.removeAttribute(SessionKeys.CONFIG_GENOME_MAP);
        //modelMap.put("configGenome", currentConfigGenome);
        modelMap.put("userConfigGenome", userConfigGenome);
        return "redirect:/";

    }


    @RequestMapping(value = "/user/delete_track", method = RequestMethod.GET )
    public String userDeleteTrackGet(String genome,
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
        UserConfigGenome userConfigGenome = this.userConfigGenomeDao.findByUserIdAndName(userId,genome);
        ConfigGenome configGenome = userConfigGenome.getConfigGenome();

 /*       configGenome.setName(this.defaultGenome);
        configGenome.setDisplayName("Homo sapiens");

        this.userConfigGenomeDao.save(userConfigGenome);*/

        //ConfigGenome currentConfigGenome = this.dataUtilService.loadPage(genome,userId,session);

        System.out.println("currentConfigGenome:======="+configGenome.getTrackGroupMap().get("value"));

        modelMap.put("configGenome", configGenome);
        return "user__delete_track";

    }


    /**
     * 普通用户删除track
     * @param genome
     * @param trackName
     * @param modelMap
     * @param session
     * @param authentication
     * @return
     */
    @RequestMapping(value = "/user/delete_track", method = RequestMethod.POST)
    public String userDeleteTrackPost(String genome, String trackName,
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

        UserConfigGenome userConfigGenome = this.userConfigGenomeDao.findByUserIdAndName(userId,genome);

        ConfigGenome configGenome = userConfigGenome.getConfigGenome();
        Collection<ConfigTrackGroup> configTrackGroups = configGenome.getTrackGroupMap().values();
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

        this.userConfigGenomeDao.save(userConfigGenome);
        session.removeAttribute(SessionKeys.CONFIG_GENOME_MAP);
        //modelMap.put("configGenome", currentConfigGenome);
        return "redirect:/";
    }


    @RequestMapping(value = "/user/delete_track_group", method = RequestMethod.GET )
    public String userDeleteTrackGroupGet(String genome,
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
        UserConfigGenome userConfigGenome = this.userConfigGenomeDao.findByUserIdAndName(userId,genome);
        ConfigGenome configGenome = userConfigGenome.getConfigGenome();

        //ConfigGenome currentConfigGenome = this.dataUtilService.loadPage(genome,userId,session);

        System.out.println("currentConfigGenome:======="+configGenome.getTrackGroupMap().get("value"));

        modelMap.put("configGenome", configGenome);
        return "user__delete_track_group";

    }

    @ResponseBody
    @RequestMapping(value = "/user/check_track_group", method = RequestMethod.POST)
    public Map<String,Object> userCheckTrackGroup(String genome, String trackGroupName,
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
        UserConfigGenome userConfigGenome = this.userConfigGenomeDao.findByUserIdAndName(userId,genome);
        ConfigGenome configGenome = userConfigGenome.getConfigGenome();
        ConfigTrackGroup configTrackGroup = configGenome.getTrackGroupMap().get(trackGroupName);
        Map<String, ConfigTrack> trackMap = configTrackGroup.getTrackMap();
        System.out.println("trackMap========="+trackMap);
        if(trackMap == null || trackMap.size()<1){
            isEmpty = true;
        }
        resultMap.put("isEmpty",isEmpty);

        return resultMap;
    }



    /**
     * 普通用户删除trackGroup
     * @param genome
     * @param trackGroupName
     * @param modelMap
     * @param session
     * @param authentication
     * @return
     */
    @RequestMapping(value = "/user/delete_track_group", method = RequestMethod.POST)
    public String userDeleteTrackGroupPost(String genome, String trackGroupName,
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

        /*if (null == configGenomeMap) {

            ConfigGenomeMapRetriever configGenomeMapRetriever = new ConfigGenomeMapRetriever();
            configGenomeMap = configGenomeMapRetriever.retrieve(userId, configGenomeDao, userConfigGenomeDao);
            session.setAttribute(SessionKeys.CONFIG_GENOME_MAP, configGenomeMap);
        }*/

        if (null == genome) {
            genome = this.defaultGenome;
        }

        UserConfigGenome userConfigGenome = this.userConfigGenomeDao.findByUserIdAndName(userId,genome);
        ConfigGenome configGenome = userConfigGenome.getConfigGenome();


        //ConfigGenome currentConfigGenome = configGenomeMap.get(genome);
        Map<String,ConfigTrackGroup> configTrackGroupMap = configGenome.getTrackGroupMap();
        ConfigTrackGroup configTrackGroup = configTrackGroupMap.get(trackGroupName);


        //如果group下面没有track信息 则可以删除
        if(configTrackGroup.getTrackMap() == null || configTrackGroup.getTrackMap().size() < 1){
            System.out.println("configTrackGroupMap======="+configTrackGroupMap);

            configTrackGroupMap.remove(trackGroupName);

            this.userConfigGenomeDao.save(userConfigGenome);
        }else{
            System.out.println("group下面有track信息，无法删除trackGroup!");
        }


        session.removeAttribute(SessionKeys.CONFIG_GENOME_MAP);
        return "redirect:/";
    }


    @RequestMapping(value = "/user/load_bedgraph_data", method = RequestMethod.GET)
    public String userLoadBedGraphGet(String genome, Map<String, Object> modelMap,
                                       HttpSession session,
                                       Authentication authentication) {

        int userId = 0;
        if (null != authentication) {
            UserEntity userEntity = (UserEntity) authentication.getPrincipal();
            userId = userEntity.getId();
        }

       ConfigGenome currentConfigGenome = this.dataUtilService.loadCommonUserPage(genome,userId,session);

        modelMap.put("configGenome", currentConfigGenome);
        return "user__load_bedgraph_data";
    }

    /**
     *
     * 导入 bedgraph 格式文件
     *
     * @return
     */
    @RequestMapping(value = "/user/load_bedgraph_data", method = RequestMethod.POST)
    public String userLoadBedGraphPost(String genome,
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
        UserConfigGenome userConfigGenome = this.userConfigGenomeDao.findByUserIdAndName(userId,genome);

        String trackFinalName = new StringBuilder("U").append(userId).append("_")
                .append(userConfigGenome.getTrackCount()).toString();

        ConfigGenome currentConfigGenome;
        if("1".equals(fileType)){
            currentConfigGenome = this.dataUtilService.loadBedGraphTrack(genome,userId,trackFinalName,
                    trackGroupName,trackDisplayName,trackDescription,dataPath,defaultOn,session);
        }else {
            currentConfigGenome = this.dataUtilService.loadBedGraphTrackPlusAndMinus(genome,userId,trackFinalName,
                    trackGroupName,trackDisplayName,trackDescription,plusPath,minusPath,defaultOn,session);
        }
        /*ConfigGenome currentConfigGenome = this.dataUtilService.loadBedGraphTrack(genome,userId,trackFinalName,
                trackGroupName,trackDisplayName,trackDescription,dataPath,defaultOn,session);
*/
        userConfigGenome.setTrackCount(userConfigGenome.getTrackCount()+1);
        userConfigGenome.setUserId(userId);

        ConfigGenome configGenome = userConfigGenome.getConfigGenome();

        //向user config genome中添加track信息
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


        userConfigGenome.addTrack(trackGroupName,configTrack);
        userConfigGenome.setName(genome);

        this.userConfigGenomeDao.save(userConfigGenome);
        session.removeAttribute(SessionKeys.CONFIG_GENOME_MAP);
        System.out.println("LOADING BEDGRAPH DATA - FINISHED. Track NAME: " + trackFinalName);

        return "redirect:/";
    }


    /**
     * 加载splicesite文件页面
     * @param genome
     * @param modelMap
     * @param session
     * @param authentication
     * @return
     */
    @RequestMapping(value = "/user/load_splicesite_data", method = RequestMethod.GET)
    public String user_load_abrowse_splicesite_data_GET(String genome, Map<String, Object> modelMap,
                                                         HttpSession session,
                                                         Authentication authentication) {

        int userId = 0;
        if (null != authentication) {
            UserEntity userEntity = (UserEntity) authentication.getPrincipal();
            userId = userEntity.getId();
        }


        ConfigGenome currentConfigGenome = this.dataUtilService.loadCommonUserPage(genome,userId,session);

        modelMap.put("configGenome", currentConfigGenome);
        return "user__load_splicesite_data";
    }


    /**
     * 加载splicesite文件数据
     * @param genome
     * @param ssPath
     * @param trackGroupName
     * @param trackName
     * @param trackDisplayName
     * @param trackDescription
     * @param modelMap
     * @param session
     * @param authentication
     * @return
     */
    @RequestMapping(value = "/user/load_splicesite_data", method = RequestMethod.POST)
    public String user_load_abrowse_splicesite_data(String genome,
                                                     String ssPath,
                                                     String trackGroupName,
                                                     String trackName,
                                                     String trackDisplayName,
                                                     String trackDescription,
                                                     Map<String, Object> modelMap,
                                                     HttpSession session,
                                                     Authentication authentication) {
        int userId = 0;

        if (null != authentication) {
            UserEntity userEntity = (UserEntity) authentication.getPrincipal();
            userId = userEntity.getId();
        }

        UserConfigGenome userConfigGenome = this.userConfigGenomeDao.findByUserIdAndName(userId,genome);

        String trackFinalName = new StringBuilder("U").append(userId).append("_")
                .append(userConfigGenome.getTrackCount()).toString();

        ConfigGenome currentConfigGenome = this.dataUtilService.loadSplicesiteTrack(genome,userId,trackFinalName,trackGroupName,trackDisplayName,trackDescription,ssPath,session);


        userConfigGenome.setTrackCount(userConfigGenome.getTrackCount()+1);
        userConfigGenome.setUserId(userId);

        //向user config genome中添加track信息
        ConfigTrack configTrack = new ConfigTrack();
        configTrack.setName(trackFinalName);
        configTrack.setDisplayName(trackDisplayName);
        configTrack.setDescription(trackDescription);
        ConfigTrackView generalTrackView = new ConfigTrackView(ViewType.RNASeqSpliceSiteView, null);
        configTrack.addView(generalTrackView);

        userConfigGenome.addTrack(trackGroupName,configTrack);
        //userConfigGenome.setConfigGenome(currentConfigGenome);
        userConfigGenome.setName(genome);

        this.userConfigGenomeDao.save(userConfigGenome);
        //this.configGenomeDao.save(currentConfigGenome);
        session.removeAttribute(SessionKeys.CONFIG_GENOME_MAP);
        System.out.println("LOADING SPLICE SITES FROM ABROWSE SS DATA - FINISHED. Track NAME: " + trackFinalName);
        //modelMap.put("chrLen", chrLenMap);
        //modelMap.put("configGenome", currentConfigGenome);

        return "redirect:/";
    }

    @RequestMapping(value = "/user/load_splicesite_data_by_sam", method = RequestMethod.GET)
    public String user_load_splicesite_data_by_sam(String genome, Map<String, Object> modelMap,
                                                 HttpSession session,
                                                 Authentication authentication) {

        int userId = 0;
        if (null != authentication) {
            UserEntity userEntity = (UserEntity) authentication.getPrincipal();
            userId = userEntity.getId();
        }

        ConfigGenome currentConfigGenome = this.dataUtilService.loadCommonUserPage(genome,userId,session);

        modelMap.put("configGenome", currentConfigGenome);
        return "user__load_splicesite_data_by_sam";
    }



    /*
        注意：本方法目前仅适用于 STAR2 的默认 sam 输出格式，而且需要使用samtools对sam文件按照染色体位置排序，
        该格式的以下特点对本方法能够正确工作至关重要：
        1. uniquely mapping reads 的 MAPQ 为255
        2. 比对结果按照染色体位置，从小到大排序
         */
    @RequestMapping(value = "/user/load_splicesite_data_by_sam", method = RequestMethod.POST)
    public String user_load_splicesite_data_by_sam(String genome,
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

        UserConfigGenome userConfigGenome = this.userConfigGenomeDao.findByUserIdAndName(userId,genome);

        String trackFinalName = new StringBuilder("U").append(userId).append("_")
                .append(userConfigGenome.getTrackCount()).toString();

        ConfigGenome currentConfigGenome = this.dataUtilService.loadSplicesiteTrackBySam(genome,userId,trackFinalName
        ,trackGroupName,trackDisplayName,trackDescription,samPath,session);



        userConfigGenome.setTrackCount(userConfigGenome.getTrackCount()+1);
        userConfigGenome.setUserId(userId);

        //向user config genome中添加track信息
        ConfigTrack configTrack = new ConfigTrack();
        configTrack.setName(trackFinalName);
        configTrack.setDisplayName(trackDisplayName);
        configTrack.setDescription(trackDescription);
        ConfigTrackView generalTrackView = new ConfigTrackView(ViewType.RNASeqSpliceSiteView, null);
        configTrack.addView(generalTrackView);

        userConfigGenome.addTrack(trackGroupName,configTrack);
        userConfigGenome.setName(genome);

        this.userConfigGenomeDao.save(userConfigGenome);
        session.removeAttribute(SessionKeys.CONFIG_GENOME_MAP);
        System.out.println("LOADING SPLICE SITES FROM SAM DATA - FINISHED. Track NAME: " + trackFinalName);


        return "redirect:/";
    }

    @RequestMapping(value = "/user/load_sam_data", method = RequestMethod.GET)
    public String user_load_sam_data_GET(String genome, Map<String, Object> modelMap,
                                                    HttpSession session,
                                                    Authentication authentication) {

        int userId = 0;
        if (null != authentication) {
            UserEntity userEntity = (UserEntity) authentication.getPrincipal();
            userId = userEntity.getId();
        }

        ConfigGenome currentConfigGenome = this.dataUtilService.loadCommonUserPage(genome,userId,session);

        modelMap.put("configGenome", currentConfigGenome);
        return "user__load_sam_data";
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
    @RequestMapping(value = "/user/load_sam_data", method = RequestMethod.POST)
    public String user_load_sam_data(String genome,
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

        //TODO:name
        UserConfigGenome userConfigGenome = this.userConfigGenomeDao.findByUserIdAndName(userId,genome);

        String trackFinalName = new StringBuilder("U").append(userId).append("_")
                .append(userConfigGenome.getTrackCount()).toString();

        ConfigGenome currentConfigGenome = this.dataUtilService.loadSamTrack(genome,userId,trackFinalName,trackGroupName,trackDisplayName,trackDescription,samPath,session);
        userConfigGenome.setTrackCount(userConfigGenome.getTrackCount()+1);
        userConfigGenome.setUserId(userId);

        //向user config genome中添加track信息
        ConfigTrack configTrack = new ConfigTrack();
        configTrack.setName(trackFinalName);
        configTrack.setDisplayName(trackDisplayName);
        configTrack.setDescription(trackDescription);
        ConfigTrackView generalTrackView = new ConfigTrackView(ViewType.RNASeqRawDataView, null);
        configTrack.addView(generalTrackView);

        userConfigGenome.addTrack(trackGroupName,configTrack);
        userConfigGenome.setName(genome);

        this.userConfigGenomeDao.save(userConfigGenome);

        //this.configGenomeDao.save(currentConfigGenome);
        session.removeAttribute(SessionKeys.CONFIG_GENOME_MAP);
        System.out.println("LOADING GTF DATA - FINISHED. Track NAME: " + trackFinalName);
        //modelMap.put("chrLen", chrLenMap);
        modelMap.put("configGenome", currentConfigGenome);
        return "redirect:/";
    }


    @RequestMapping(value = "/user/load_gencode_gtf_data", method = RequestMethod.GET)
    public String user_load_gencode_gtf_data_GET(String genome, Map<String, Object> modelMap,
                                                  HttpSession session,
                                                  Authentication authentication) {

        int userId = 0;
        if (null != authentication) {
            UserEntity userEntity = (UserEntity) authentication.getPrincipal();
            userId = userEntity.getId();
        }

        ConfigGenome currentConfigGenome = this.dataUtilService.loadCommonUserPage(genome,userId,session);

        modelMap.put("configGenome", currentConfigGenome);
        return "user__load_gencode_gtf_data";
    }

    @RequestMapping(value = "/user/load_gencode_gtf_data", method = RequestMethod.POST)
    public String user_load_gencode_gtf_data(String genome,
                                              String gtfPath,
                                              String trackGroupName,
                                              //String trackName,
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

        UserConfigGenome userConfigGenome = this.userConfigGenomeDao.findByUserIdAndName(userId,genome);

        String trackFinalName = new StringBuilder("U").append(userId).append("_")
                .append(userConfigGenome.getTrackCount()).toString();

        ConfigGenome currentConfigGenome = this.dataUtilService.loadGencodeGtfTrack(genome,userId,trackFinalName,trackGroupName,trackDisplayName,trackDescription,gtfPath,entryLink,session);
        userConfigGenome.setTrackCount(userConfigGenome.getTrackCount()+1);
        userConfigGenome.setUserId(userId);

        //向user config genome中添加track信息
        ConfigTrack configTrack = new ConfigTrack();
        //TODO:config track name 不再从前端获取
        configTrack.setName(trackFinalName);
        configTrack.setDisplayName(trackDisplayName);
        configTrack.setDescription(trackDescription);
        configTrack.setDefaultNormalView(ViewType.GeneModelLikeView);
        ConfigTrackView generalTrackView = new ConfigTrackView(ViewType.GeneModelLikeView, entryLink);

        // TODO : 在前端 Browser 中增加支持
        ConfigTrackView denseTrackView = new ConfigTrackView(ViewType.GeneModelDenseView, entryLink);
        configTrack.addView(generalTrackView);
        configTrack.addView(denseTrackView);

        userConfigGenome.addTrack(trackGroupName,configTrack);
        userConfigGenome.setName(genome);

        this.userConfigGenomeDao.save(userConfigGenome);
        //this.configGenomeDao.save(currentConfigGenome);
        session.removeAttribute(SessionKeys.CONFIG_GENOME_MAP);
        System.out.println("LOADING GTF DATA - FINISHED. Track NAME: " + trackFinalName);
        //modelMap.put("chrLen", chrLenMap);
        //modelMap.put("configGenome", currentConfigGenome);
        return "redirect:/";
    }

}
