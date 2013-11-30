package itunes_parser.itunes;

import lombok.Data;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: karlpauls
 * Date: 5/25/13
 * Time: 3:46 PM
 * To change this template use File | Settings | File Templates.
 */
@Data
public class MusicTrack extends MusicTrackReference {
    private String name;
    private String artist;
    private String sortArtist;
    private String composer;
    private String albumArtist;
    private String album;
    private String sortAlbum;
    private String genre;
    private String grouping;
    private String kind;
    private int size;
    private int totalTime;
    private int trackNumber;
    private int trackCount;
    private int year;
    private Date dateModified;
    private Date dateAdded;
    private int bitRate;
    private int sampleRate;
    private String comments;
    private int artworkCount;
    private String sortName;
    private String persistentId;
    private String trackType;
    private String location;
    private int fileFolderCount;
    private int libraryFolderCount;
    private int discNumber;
    private int discCount;
    private int playCount;
    private int normalization;
    private Date playDate;
    private Date playDateUTC;
    private boolean compilation;
    private int albumRating;
    private boolean albumRatingComputed;
    private int rating;
    private String sortAlbumArtist;
    private boolean disabled;
    private boolean ratingComputed;
    private int skipCount;
    private Date skipDate;
    private String sortComposer;
}
