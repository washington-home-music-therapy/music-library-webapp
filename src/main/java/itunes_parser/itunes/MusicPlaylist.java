package itunes_parser.itunes;

import lombok.Data;
import org.apache.commons.collections.Factory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: karlpauls
 * Date: 5/25/13
 * Time: 3:46 PM
 * To change this template use File | Settings | File Templates.
 */
@Data
public class MusicPlaylist {
    private String name;
    private boolean master;
    private int playlistId;
    private String playlistPersistentId;
    private int distinguishedKind;
    private boolean music;
    private boolean movies;
    private boolean tVShows;
    private boolean visible;
    private boolean allItems;
    private byte[] smartInfo;
    private byte[] smartCriteria;
    private boolean partyShuffle;
    private boolean folder;
    private String parentPersistentId;
    private List<MusicTrackReference> playlistItems = new ArrayList<MusicTrackReference>();
    private final Factory playlistItemsFactory = new Factory() {
        @Override
        public Object create() {
            return new MusicTrackReference();
        }
    };
}
