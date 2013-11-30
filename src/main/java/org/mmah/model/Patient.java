package org.mmah.model;

import lombok.Data;
import org.joda.time.DateTime;

import java.util.List;

/**
 *
 */
@Data
public class Patient {
    private long id;
    private String name;

    private List<Account> caretakers;
    private DateTime birthday;
    private List<String> keywords;
    private List<Playlist> playlists;
    private List<Library> libraries;
}
