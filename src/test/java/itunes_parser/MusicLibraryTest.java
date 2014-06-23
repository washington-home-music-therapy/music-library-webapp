package itunes_parser;

import com.google.common.base.Functions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import itunes_parser.bean.BeanPropertyComparator;
import itunes_parser.itunes.MusicLibrary;
import itunes_parser.itunes.MusicTrack;
import itunes_parser.plist.PLEventReader;
import itunes_parser.plist.PLUtil;
import org.apache.commons.beanutils.BeanUtils;
import org.junit.Ignore;
import org.junit.Test;

import javax.xml.stream.XMLStreamException;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: karlpauls
 * Date: 5/25/13
 * Time: 8:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class MusicLibraryTest {
    @Test
    @Ignore
    public void testParseLibrary() throws FileNotFoundException, XMLStreamException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        PLEventReader itr = new PLEventReader(new FileReader("Larry's iTunes Library.xml")); //));

        itr.seek(PLEventReader.State.START_PLIST,null, PLEventReader.State.START_DICT);

        MusicLibrary library = PLUtil.readFrom(new MusicLibrary(), itr);

        Map<String,Map<String,List<MusicTrack>>> indexes = new HashMap<String, Map<String, List<MusicTrack>>>();
//        indexes.put("genre",new HashMap<String, MusicTrack>());
//        indexes.put("grouping", new HashMap<String, MusicTrack>());
//        indexes.put("year", new HashMap<String, MusicTrack>());

        indexes.put("artist,genre,grouping", new TreeMap<String, List<MusicTrack>>());

        for(MusicTrack track : library.getTracks().values()) {
            for(Map.Entry<String,Map<String,List<MusicTrack>>> index : indexes.entrySet()) {
                String[] keys = index.getKey().split(",");
                StringBuilder sb = new StringBuilder();
                sb.append(BeanUtils.getProperty(track,keys[0]));
                for(int i = 1; i < keys.length; i++) {
                    sb.append(",");
                    String val = BeanUtils.getProperty(track,keys[i]);
                    if(val != null)
                        sb.append(val);
                }
                String indexKey = sb.toString();
                List<MusicTrack> tracks = index.getValue().get(indexKey);
                if(tracks == null) {
                    tracks = new ArrayList<MusicTrack>();
                    index.getValue().put(indexKey,tracks);
                }
                tracks.add(track);
            }
        }

        System.out.print(library.getTracks().size());
        System.out.println(" total tracks");

        System.out.println("artist,genre,grouping");
        for(Map.Entry<String,List<MusicTrack>> e : indexes.get("artist,genre,grouping").entrySet()) {
            System.out.print(e.getKey());
            System.out.print(" has ");
            System.out.print(e.getValue().size());
            System.out.println(" tracks");
        }

        List<String> order = ImmutableList.of("genre","year","grouping","artist","album","trackNumber","name");

        List<String> columns = ImmutableList.of("genre","year","grouping","artist","album","trackNumber","name");

        TreeSet<MusicTrack> tracks = new TreeSet<MusicTrack>(new BeanPropertyComparator(order));
        tracks.addAll(library.getTracks().values());

        PrintStream libraryOut = new PrintStream(new FileOutputStream("library.tsv"));
        printTo(Maps.asMap(new HashSet(columns), Functions.identity()), columns, libraryOut);
        for(MusicTrack to : tracks) {
            Map track = BeanUtils.describe(to);
            printTo(track, columns, libraryOut);
        }
        libraryOut.close();
    }

    private void printTo(Map track, List<String> columns, PrintStream libraryOut) {
        for(String s : columns) {
            Object o = track.get(s);
            if(o != null) {
                String ov = o.toString();
//                if(ov.indexOf(',') > -1) {
//                    libraryOut.print('"');
                    libraryOut.print(ov.replaceAll("\t","  ")); //("[\\\\\"]","\\\\$0")); // replace " with \" or \ with \\
//                    libraryOut.print('"');
//                } else {
//                    libraryOut.print(ov);
//                }
            }
            libraryOut.print("\t");
        }
        libraryOut.println();
    }
}
