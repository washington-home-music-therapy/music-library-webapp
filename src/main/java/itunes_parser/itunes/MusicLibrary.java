package itunes_parser.itunes;

import lombok.Data;
import org.apache.commons.collections.Factory;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: karlpauls
 * Date: 5/25/13
 * Time: 3:33 PM
 * To change this template use File | Settings | File Templates.
 */
@Data
public class MusicLibrary {
    private int majorVersion;
    private int minorVersion;
    private Date date;
    private String applicationVersion;
    private int features;
    private boolean showContentRatings;
    private String musicFolder;
    private String libraryPersistentId;
    private Map<Integer,MusicTrack> tracks = new HashMap<Integer, MusicTrack>();
    private final Factory tracksFactory = new Factory() {
        @Override
        public Object create() {
            return new MusicTrack();
        }
    };
    private List<MusicPlaylist> playlists = new ArrayList<MusicPlaylist>();
    private final Factory playlistsFactory = new Factory() {
        @Override
        public Object create() {
            return new MusicPlaylist();
        }
    };
}
