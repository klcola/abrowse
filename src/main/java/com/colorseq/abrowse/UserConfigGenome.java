package com.colorseq.abrowse;

import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Id;
import java.util.List;
import java.util.Map;

@Document(collection="user_config_genome")
@CompoundIndexes({
        @CompoundIndex(name = "userId_name_idx", def = "{'userId': 1, 'name': 1}")
})
public class UserConfigGenome {

    @Id
    private String id;

    @Indexed
    private int userId;

    private String name;

    private ConfigGenome configGenome;

    private long trackCount;

    public UserConfigGenome() {
    }

    public UserConfigGenome(int userId, String name) {

        this.userId = userId;
        this.name = name;
        this.configGenome = new ConfigGenome();
        this.configGenome.setName(name);
        this.configGenome.setId(new StringBuilder("u").append(userId).append("_").append(name).toString());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ConfigGenome getConfigGenome() {
        return configGenome;
    }

    public void setConfigGenome(ConfigGenome configGenome) {
        this.configGenome = configGenome;
    }

    public long getTrackCount() {
        return trackCount;
    }

    public void setTrackCount(long trackCount) {
        this.trackCount = trackCount;
    }


    public void addTrackGroup(ConfigTrackGroup configTrackGroup) {
        Map<String, ConfigTrackGroup> trackGroupMap = this.configGenome.getTrackGroupMap();
        trackGroupMap.put(configTrackGroup.getName(), configTrackGroup);
    }

    public void addTrack(String trackGroupName, ConfigTrack configTrack){

        Map<String, ConfigTrackGroup> trackGroupMap = this.configGenome.getTrackGroupMap();
        ConfigTrackGroup configTrackGroup = trackGroupMap.get(trackGroupName);
        if(configTrackGroup != null) {
            configTrackGroup.addTrack(configTrack);
        }
    }

    public void deleteTrackMap(String trackGroupName,String trackName){

        Map<String, ConfigTrackGroup> trackGroupMap = this.configGenome.getTrackGroupMap();
        ConfigTrackGroup configTrackGroup = trackGroupMap.get(trackGroupName);
        if(configTrackGroup != null){

        }
    }

}
