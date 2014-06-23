package org.mmah.web;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import itunes_parser.ITunes;
import itunes_parser.itunes.MusicLibrary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created with IntelliJ IDEA.
 * Date: 11/17/13
 * Time: 7:20 PM
 * To change this template use File | Settings | File Templates.
 */
@Controller
@RequestMapping("/library")
public class LibraryController {
    private final BiMap<String,Long> libraryIds = Maps.synchronizedBiMap(HashBiMap.<String,Long>create());
    private final ConcurrentHashMap<Long,MusicLibrary> libraryStore = new ConcurrentHashMap<Long, MusicLibrary>();

    @Autowired
    private File dataDir;

    private List<String> libraryColumns = ImmutableList.of("genre", "year", "grouping", "artist", "album", "trackNumber", "name");

    @Autowired
    private Random random;

    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    @ResponseBody
    public String quickUpload(HttpServletRequest request,
                              @RequestParam String user,
                              @RequestParam String library,
                              @RequestParam MultipartFile data) throws IOException {
        // TODO exception resolver

        ITunes itReader = new ITunes(); // not displaying columns
        MusicLibrary parsedLibrary = null;
        try {
            parsedLibrary = itReader.read(data.getInputStream());
        } catch (XMLStreamException e) {
            // TODO move to errors directory, email karl
            // TODO error checksum
            return "There was an error parsing your library, the development team has been notified. Please wait for an email before resubmitting.";
        }

        long uid = createId();

        File storageFile = getLibraryFile(uid);
        data.transferTo(storageFile);

        libraryIds.put(libraryKey(user,library),uid);
        libraryStore.put(uid,parsedLibrary);

        // look up or generate account and library ids, validate file and present link
        return MessageFormat.format("Library for {0} named {1} stored, {2} tracks, {3} playlists -- id {4}",
                user,
                library,
                parsedLibrary.getTracks().size(),
                parsedLibrary.getPlaylists().size(),
                uid);
    }

    private String libraryKey(String user, String library) {
        return user + "%%" + library;
    }

    private long createId() throws IOException {
        File libraryFile;
        long uid;
        do {
            uid = Math.abs(random.nextLong());
            libraryFile = getLibraryFile(uid);
        } while(libraryFile.exists());
        libraryFile.createNewFile();
        return uid;
    }

    private File getLibraryFile(long id) {
        return new File(dataDir,id + ".xml");
    }

    @RequestMapping(value = "/{libraryId:\\d+}/", produces = "application/json")
    @ResponseBody
    public MusicLibrary viewLibraryJson(@PathVariable long libraryId) throws FileNotFoundException, XMLStreamException {
        return loadLibrary(libraryId);
    }

    @RequestMapping(value = "/{libraryId:\\d+}/", produces = "text/plain")
    @ResponseBody
    public String viewLibrary(@PathVariable long libraryId) throws FileNotFoundException, XMLStreamException {
        MusicLibrary parsedLibrary = loadLibrary(libraryId);

        String key = libraryIds.inverse().get(libraryId);

        String user = "unknown";
        String library = "unknown";

        if(key != null) {
            String[] params = key.split("%%");
            user = params[0];
            library = params[1];
        }

        // look up or generate account and library ids, validate file and present link
        return MessageFormat.format("Library for {0} named {1} stored, {2} tracks, {3} playlists -- id {4}",
                user,
                library,
                parsedLibrary.getTracks().size(),
                parsedLibrary.getPlaylists().size(),
                libraryId);
    }

    protected MusicLibrary loadLibrary(long libraryId) throws FileNotFoundException, XMLStreamException {
        MusicLibrary parsedLibrary =  libraryStore.get(libraryId);
        if(parsedLibrary == null) {
            File storageFile = getLibraryFile(libraryId);
            ITunes itReader = new ITunes(); // not displaying columns
            parsedLibrary = itReader.read(storageFile);
        }
        return parsedLibrary;
    }

    @RequestMapping("/{libraryId:\\d+}/upload")
    @ResponseBody
    public String updateLibrary(@PathVariable long libraryId,
                                @RequestParam MultipartFile data) throws IOException {
        MusicLibrary parsedLibrary = libraryStore.get(libraryId);
        if(parsedLibrary == null) {
            return MessageFormat.format("Library {0} does not exist",libraryId);
        }

        File storageFile = getLibraryFile(libraryId);
        storageFile.delete();

        ITunes itReader = new ITunes(); // not displaying columns
        try {
            parsedLibrary = itReader.read(data.getInputStream());
        } catch (XMLStreamException e) {
            // TODO move to errors directory, email karl
            // TODO error checksum
            return "There was an error parsing your library, the development team has been notified. Please wait for an email before resubmitting.";
        }

        data.transferTo(storageFile);

        libraryStore.put(libraryId,parsedLibrary);

        // look up or generate account and library ids, validate file and present link
        return MessageFormat.format("Library updated, {2} tracks, {3} playlists -- id {4}",
                null,
                null,
                parsedLibrary.getTracks().size(),
                parsedLibrary.getPlaylists().size(),
                libraryId);
    }

    // provide search by artist column only
    @RequestMapping(value = "", method = RequestMethod.GET, params = {"q","field=artist"})
    @ResponseBody
    public String findByArtist(HttpServletRequest request,
                       @RequestParam String q) {
        return null;
    }

    // provide search by title column only
    @RequestMapping(value = "", method = RequestMethod.GET, params = {"q","field=title"})
    @ResponseBody
    public String findByTitle(HttpServletRequest request,
                               @RequestParam String q) {
        return null;
    }

    @RequestMapping(value = "", method = RequestMethod.GET, params = "q")
    @ResponseBody
    public String find(HttpServletRequest request,
                       @RequestParam String q) {
        String[] words = q.split("\\s+");
        ArrayList<String> names = new ArrayList<String>();

        nextLibrary:
        for(String name : libraryIds.keySet()) {
            for(String word : words ) {
                if(name.contains(word)) {
                    names.add(name);
                    continue nextLibrary;
                }
            }
        }

        return names.isEmpty()?"No results found":"Found libraries " + names;
    }

    /*
    roots
    classical
    traditional
    spiritual
    other
    popular
    r&b / rock
     */
    @RequestMapping("/{libraryId:\\d+}/group")
    @ResponseBody
    public String showGroupSummary() {
        return null;
    }

    /*
    subgroups:

    classical
        orchestra
        vocal
        instrumentalist
        opera

    other
        world
        nature
        instrumental
        spoken / audio

    popular
        vocals
        vocals male
        vocals female
        soundtrack
        orchestra
        pop
        sing-a-long
        romance

    patriotic

    r&b / rock
        r&b 1947-1974
        r&b 1974+
        rock 1954-1963
        rock 1963-1973
        rock 1974+
        r&b soul

    roots
        jazz
        jazz big-band
        blues

    spiritual
        contemporary spiritual
        holiday
        gospel
        hymns moravian
        hymns lutheran

    traditional
        country western
        country bluegrass
        country contemporary
        country classic
        country cowboy
        country & folk traditional
     */

    @RequestMapping(value = "/{libraryId:\\d+}/genre", params = "group")
    @ResponseBody
    public String showGenres() {
        return null;
    }

    @RequestMapping("/{libraryId:\\d+}/artist")
    @ResponseBody
    public String showArtists() {
        return null;
    }
}
