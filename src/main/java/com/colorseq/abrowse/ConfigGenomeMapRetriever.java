package com.colorseq.abrowse;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * @author Lei Kong
 */
public class ConfigGenomeMapRetriever {

    public ConfigGenomeMapRetriever() {
    }

    public Map<String, ConfigGenome> retrieve(int userId, ConfigGenomeDao configGenomeDao,
                                              UserConfigGenomeDao userConfigGenomeDao) {

        List<ConfigGenome> configGenomeList = configGenomeDao.findAll();
        System.out.println("读取 genome 配置 " + configGenomeList.size() + " 个。");
        // Hashtable 是线程安全的
        Map<String, ConfigGenome> configGenomeMap = new Hashtable<>();

        for (ConfigGenome configGenome : configGenomeList) {
            String name = configGenome.getName();
            configGenomeMap.put(name, configGenome);
        }

        List<UserConfigGenome> userConfigGenomeList = userConfigGenomeDao.findAllByUserId(userId);
        if (null != userConfigGenomeList && userConfigGenomeList.size() > 0) {
            for (UserConfigGenome userConfigGenome : userConfigGenomeList) {
                // 目前仅支持用户自定义 Tracks 和 TrackGroups
                // 重要规定：用户自定义 TrackGroups 不会和系统已有 TrackGroups 重名
                Map<String, ConfigTrackGroup> userTrackGroupMap = userConfigGenome.getConfigGenome().getTrackGroupMap();
                String genomeName = userConfigGenome.getName();
                ConfigGenome configGenome = configGenomeMap.get(genomeName);
                configGenome.getTrackGroupMap().putAll(userTrackGroupMap);
            }
        }

        return configGenomeMap;
    }
}
