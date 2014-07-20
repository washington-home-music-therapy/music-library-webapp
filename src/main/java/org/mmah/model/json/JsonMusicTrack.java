package org.mmah.model.json;

import com.fasterxml.jackson.annotation.JsonIgnore;
import itunes_parser.itunes.MusicTrack;
import kap.jackson.config.JsonModel;

import java.util.Date;

/**
 * Created by karl on 7/7/14.
 */
@JsonModel(MusicTrack.class)
public interface JsonMusicTrack {
    String getName();
    String getArtist();
    @JsonIgnore
    String getSortArtist();
    String getComposer();
    String getAlbumArtist();
    String getAlbum();
    @JsonIgnore
    String getSortAlbum();

    String getGenre();
    String getGrouping();
    String getKind();
    int getSize();
    int getTotalTime();
    int getTrackNumber();
    int getTrackCount();
    int getYear();
    @JsonIgnore
    Date getDateModified();
    @JsonIgnore
    Date getDateAdded();
    int getBitRate();
    int getSampleRate();
    String getComments();
    @JsonIgnore
    int getArtworkCount();
    @JsonIgnore
    String getSortName();
    @JsonIgnore
    String getPersistentId();
    @JsonIgnore
    String getTrackType();
    @JsonIgnore
    String getLocation();
    @JsonIgnore
    int getFileFolderCount();
    @JsonIgnore
    int getLibraryFolderCount();
    int getDiscNumber();
    int getDiscCount();
    @JsonIgnore
    int getPlayCount();
    @JsonIgnore
    int getNormalization();
    @JsonIgnore
    Date getPlayDate();
    @JsonIgnore
    Date getPlayDateUTC();
    boolean isCompilation();
    @JsonIgnore
    int getAlbumRating();
    @JsonIgnore
    boolean isAlbumRatingComputed();
    @JsonIgnore
    int getRating();
    @JsonIgnore
    String getSortAlbumArtist();
    @JsonIgnore
    boolean isDisabled();
    @JsonIgnore
    boolean isRatingComputed();
    @JsonIgnore
    int getSkipCount();
    @JsonIgnore
    Date getSkipDate();
    @JsonIgnore
    String getSortComposer();
}
