package org.mmah.web;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterators;
import itunes_parser.itunes.MusicLibrary;
import org.mmah.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import itunes_parser.ITunes;

import javax.servlet.http.HttpServletRequest;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Map;
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
    private final ConcurrentHashMap<String,MusicLibrary> libraryStore = new ConcurrentHashMap<String, MusicLibrary>();

    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    @ResponseBody
    public String quickUpload(HttpServletRequest request,
                              @RequestParam String user,
                              @RequestParam String library,
                              @RequestParam MultipartFile data) throws IOException {
        // TODO exception resolver

        File tempFile = File.createTempFile(data.getOriginalFilename(),"mmah.temp");
        data.transferTo(tempFile);

        ITunes itReader = new ITunes("genre", "year", "grouping", "artist", "album", "trackNumber", "name");
        MusicLibrary parsedLibrary = null;
        try {
            parsedLibrary = itReader.read(tempFile);
        } catch (XMLStreamException e) {
            // TODO move to errors directory, email karl
            // TODO error checksum
            return "There was an error parsing your library, the development team has been notified. Please wait for an email before resubmitting.";
        }

        libraryStore.put(libraryKey(user,library),parsedLibrary);

        // look up or generate account and library ids, validate file and present link
        return MessageFormat.format("Library for {0} named {1} stored, {2} tracks, {3} playlists",
                user,
                library,
                parsedLibrary.getTracks().size(),
                parsedLibrary.getPlaylists().size());
    }

    private String libraryKey(String user, String library) {
        return user + "%%" + library;
    }

    @RequestMapping("/{libraryId:\\d+}/")
    @ResponseBody
    public String viewLibrary(@PathVariable long libraryId) {
        return "library view, library: " + libraryId;
    }

    @RequestMapping("/{libraryId:\\d+}/upload")
    @ResponseBody
    public String updateLibrary(@PathVariable long libraryId) {
        return "library updated, library: " + libraryId;
    }

    @RequestMapping(value = "", method = RequestMethod.GET, params = "q")
    @ResponseBody
    public String find(HttpServletRequest request,
                       @RequestParam String q) {
        String[] words = q.split("\\s+");
        ArrayList<Map.Entry<String,MusicLibrary>> resultSet = new ArrayList<Map.Entry<String, MusicLibrary>>();
        ArrayList<String> names = new ArrayList<String>();

        nextLibrary:
        for(Map.Entry<String,MusicLibrary> e : libraryStore.entrySet()) {
            for(String word : words ) {
                if(e.getKey().contains(word)) {
                    resultSet.add(e);
                    names.add(e.getKey());
                    continue nextLibrary;
                }
            }
        }

        return names.isEmpty()?"No results found":"Found libraries " + names;
    }
}
