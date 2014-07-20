package itunes_parser.itunes;

import lombok.Data;

/**
 * Created with IntelliJ IDEA.
 * User: karlpauls
 * Date: 5/28/13
 * Time: 5:51 PM
 * To change this template use File | Settings | File Templates.
 */
@Data
public class MusicTrackReference {
    private int trackId;

    public MusicTrack getTrack(MusicLibrary library) {
        return library.getTracks().get(String.valueOf(trackId));
    }
}
