package org.mmah.model.json;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import itunes_parser.itunes.MusicLibrary;
import kap.jackson.config.JsonModel;

import java.util.Date;
import java.util.List;
import java.util.Map;

// TODO @JsonIgnoreProperties for values not declared in this interface
@JsonModel(MusicLibrary.class)
public interface JsonMusicLibrary {
    @JsonIgnore
    int getMajorVersion();
    @JsonIgnore
    int getMinorVersion();
    @JsonInclude(JsonInclude.Include.NON_NULL)
    Date getDate();
    @JsonIgnore
    String getApplicationVersion();
    @JsonIgnore
    int getFeatures();
    @JsonIgnore
    boolean isShowContentRatings();
    @JsonIgnore
    String getMusicFolder();
    @JsonIgnore
    String getLibraryPersistentId();
    Map<Integer,Object> getTracks();
    @JsonIgnore
    Object getTracksFactory();
    List<Object> getPlaylists();
    @JsonIgnore
    Object getPlayListsFactory();
}
