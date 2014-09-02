package itunes_parser;

import com.google.common.collect.*;
import itunes_parser.bean.BeanPropertyComparator;
import itunes_parser.itunes.MusicLibrary;
import itunes_parser.itunes.MusicTrack;
import itunes_parser.itunes.MusicTrackReference;
import itunes_parser.plist.PLEventReader;
import itunes_parser.plist.PLUtil;
import lombok.Data;
import org.apache.commons.beanutils.BeanUtils;

import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * ITunes reader
 */
public final class ITunes {
    private static final boolean DEBUG = false;
    private static final SimpleDateFormat DATE = new SimpleDateFormat("YYYYMMdd");

//    private final ImmutableList<String> display;
//    private final BeanPropertyComparator sort;

    public static Map<Genre,Config> configuration(Multimap<Genre, Genre> groups) {
        Map<Genre,Config> configs = new HashMap<>();
        configs.put(Genre.NIL,new Config());

        Config c = new Config();
        c.display.add("genre");
        c.sort = new BeanPropertyComparator(ImmutableList.of("artist","name"));

        for(Genre grouping : groups.keySet()) {
            configs.put(grouping, c);
        }

        c = new Config();
        c.display = ImmutableList.of("composer", "name");
        c.sort = new BeanPropertyComparator(ImmutableList.of("composer", "name"));
        configs.put(new Genre("Classical", "Classical"), c);

//        c = new Config();
//        c.display = ImmutableList.of("composer", "name");
//        configs.put(new Genre("Orchestra", "Popular"), c);
//        configs.put(new Genre("Patriotic", "Popular"), c);

        c = new Config();
        c.sort = new BeanPropertyComparator(ImmutableList.of("album", "name"));
        configs.put(new Genre("Soundtrack", "Popular"), c);

        Genre spiritualGroup = new Genre("","Spiritual");
        c = new Config();
        c.display = ImmutableList.of("name");
        c.sort = new BeanPropertyComparator(ImmutableList.of("genre","name"));
        for(Genre g : groups.get(spiritualGroup)) {
            configs.put(g, c);
        }
        c = new Config();
        c.display = ImmutableList.of("genre","name");
        c.sort = new BeanPropertyComparator(ImmutableList.of("genre","name"));
        configs.put(spiritualGroup, c);

        return configs;
    }

    public static void main(String[] argv) throws Throwable {
        File errorF = new File(ITunes.class.getName() + "_errors_" + DATE.format(new Date()) + ".txt");
        FileOutputStream errorFile = new FileOutputStream(errorF);
        PrintWriter error = new PrintWriter(errorFile);

        File file = null;
        for(String dir : Arrays.asList(".","src/test/resources")) {
            file = new File(dir,"iTunes Music Library.xml").getCanonicalFile();
            if(file.exists() && file.canRead()) break;
        }
        try {
            ITunes itReader = new ITunes();
            MusicLibrary library = itReader.read(file);
            final ImmutableListMultimap<Genre,MusicTrack> genres = Multimaps.index(
                    library.getTracks().values(), it -> new Genre(it.getGenre().trim(), it.getGrouping().trim()));
            final ImmutableListMultimap<Genre,MusicTrack> groups = Multimaps.index(
                    library.getTracks().values(), it->new Genre("",it.getGrouping().trim()));
            final ImmutableListMultimap<Genre,Genre> groupsToGenres = Multimaps.index(
                    genres.keySet(), it->new Genre("",it.getGrouping().trim()));

            Map<Genre,Config> configuration = configuration(groupsToGenres);

            File completeFile = getNumberedFile("complete-library-" + DATE.format(new Date()), ".csv");
            File indexFile = getChildFile(completeFile, "index.html");
            PrintWriter index = new PrintWriter(new FileWriter(indexFile));
            System.out.println(completeFile + " - " + library.getTracks().size());
            PrintWriter out = new PrintWriter(new FileWriter(completeFile));
            Config conf = new Config();
            conf.display.add("grouping");
            conf.display.add("genre");
            itReader.printTo(library,out, conf);
            out.close();

            for(Genre genre : genres.keySet()) {
                conf = configuration.get(genre);
                if(conf == null) {
                    conf = configuration.get(Genre.NIL);
                }

                File childFile = getChildFile(completeFile, genre.getGrouping(), "/", genre.getName(), "-", DATE.format(new Date()), ".csv");
                System.out.println(childFile + " - " + genres.get(genre).size());
                out = new PrintWriter(new FileWriter(childFile));
                itReader.printHeader(out, conf.display);
                itReader.printTo(genres.get(genre), library, out, conf);
                out.close();
            }

            for(Genre grouping : groups.keySet()) {
                conf = configuration.get(grouping);
                if(conf == null) {
                    conf = configuration.get(Genre.NIL);
                }

                File childFile = getChildFile(completeFile, grouping.getGrouping(), "-", DATE.format(new Date()), ".csv");
                out = new PrintWriter(new FileWriter(childFile));
                itReader.printHeader(out, conf.display);
                itReader.printTo(groups.get(grouping), library, out, conf);
                out.close();

                index.append(format(
                        "<div class=%grouping% id=%group-{0}%><a href=%{2}% class=%title%>{0}</a>",
                        grouping.getGrouping(), "", "LINK"));
                for(Genre genre : groupsToGenres.get(grouping)) {
                    index.append(format(
                            "<div class=%genre% id=%genre-{0}%><a href=%{2}% class=%title%>{0}</a><span class=%count%>{1}</span>",
                            genre.getName(), genres.get(genre).size(), "LINK"));
                    index.append("</div>");
                }
                index.append("</div>");
            }
            index.close();
        } catch(Exception e) {
            e.printStackTrace();
            e.printStackTrace(error);
        } catch(Throwable t) {
            t.printStackTrace();
            t.printStackTrace(error);
            throw t;
        }
        error.close();
        errorFile.close();
        RandomAccessFile raf = new RandomAccessFile(errorF,"r");
        boolean empty = raf.length() < 1;
        raf.close();
        if(empty) {
            errorF.delete();
        }
    }

    private static String format(String format, Object... args) {
        return MessageFormat.format(format,args).replaceAll("%", "\"");
    }

    private static File getChildFile(File parent, String ... parts) {
        StringBuilder sb = new StringBuilder(parent.getName());
        sb.setLength(sb.lastIndexOf("."));
        sb.append("/");
        for(String part : parts) {
            sb.append(part);
        }
        File child = new File(parent.getParentFile(),sb.toString());
        parent = child.getParentFile();
        if(parent != null) {
            parent.mkdirs();
        }
        return child;
    }

    private static File getNumberedFile(String prefix, String suffix) {
        File file = new File(prefix + suffix);
        int i = 0;
        while(file.exists()) {
            i++;
            file = new File(prefix + "-" + i + suffix);
        }
        File parent = file.getParentFile();
        if(parent != null) {
            parent.mkdirs();
        }
        return file;
    }

    public ITunes() {
//        display = null;
//        sort = null;
    }

//    public ITunes(List<String> displayKeys, List<String> sortKeys) {
//        display = ImmutableList.copyOf(displayKeys);
//        sort = new BeanPropertyComparator(ImmutableList.copyOf(sortKeys));
//    }
//
//    public ITunes(String ... displayKeys) {
//        List<String> columns = new ArrayList<String>();
//        List<String> order = new ArrayList<String>();
//
//        for(String s : displayKeys) {
//            if(s.startsWith("*")) {
//                order.add(s.substring(1));
//            } else if(s.startsWith("#")) {
//                columns.add(s.substring(1));
//            }else {
//                order.add(s);
//                columns.add(s);
//            }
//        }
//
//        display = ImmutableList.copyOf(columns);
//        sort = new BeanPropertyComparator(ImmutableList.copyOf(order));
//    }

    public MusicLibrary read(File file) throws FileNotFoundException, XMLStreamException {
        PLEventReader itr = new PLEventReader(new FileReader(file));

        itr.seek(PLEventReader.State.START_PLIST,null, PLEventReader.State.START_DICT);

        MusicLibrary library = PLUtil.readFrom(new MusicLibrary(), itr);

        return library;
    }

    public MusicLibrary read(InputStream is) throws XMLStreamException {
        PLEventReader itr = new PLEventReader(is);

        itr.seek(PLEventReader.State.START_PLIST, null, PLEventReader.State.START_DICT);

        MusicLibrary library = PLUtil.readFrom(new MusicLibrary(), itr);

        return library;
    }

    public void printTo(MusicLibrary library, PrintWriter out, Config conf) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        printHeader(out, conf.display);
        printTo(library.getTracks().values(), library, out, conf);
    }

//    public void printHeader(PrintWriter out) {
//        printHeader(out,display);
//    }

    public void printHeader(PrintWriter out, List<String> displayKeys) {
        for(String s : displayKeys) {
            out.print(s);
            out.print(",");
        }
        out.println();
    }

//    public void printTo(Collection<? extends MusicTrackReference> tracks, MusicLibrary library, PrintWriter out) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
//        printTo(tracks, library, out, display, sort, (m,k) -> m.get(k) );
//    }

    public void printTo(Collection<? extends MusicTrackReference> tracks, MusicLibrary library, PrintWriter out, Config conf) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {

        TreeSet<MusicTrackReference> sortedTracks = new TreeSet<MusicTrackReference>(conf.sort);
        sortedTracks.addAll(tracks);

        for(MusicTrackReference trackReference : sortedTracks) {
            MusicTrack to = trackReference.getTrack(library);
            Map track = BeanUtils.describe(to);
            for(String s : conf.display) {
                Object o = conf.fallbackFunc.apply(track, s);
                if(o != null) {
                    String ov = o.toString();
                    ov = ov.replaceAll("\"","''");
                    if(ov.indexOf(',') > -1) {
                        out.print('"');
                        out.print(ov);
                        out.print('"');
                    } else {
                        out.print(ov);
                    }
                }
                out.print(",");
            }
            out.println();
        }
    }

    static class Config {

        public Map<String,String> fallback;
        public BiFunction<Map<String, String>, String, String> fallbackFunc = (m,k1) -> {
            String k = k1;
            String v = null;
            while(k != null) {
                v = m.get(k);
                if(v != null && v.length() > 0) break;
                k = fallback.get(k);
            }
            if(DEBUG && k != k1) v = v + "(" + k + ")";
            return v;
        };
        public List<String> display;
        public BeanPropertyComparator sort;
        public Map<String,Function<String,String>> format;



        public Config() {
            fallback = new HashMap<>(ImmutableMap.of(
//                    "albumArtist", "composer",
//                    "composer", "artist",
                    "", ""
            ));
            display = new ArrayList<>(Arrays.asList(
                    "artist",
                    "name"
//                    "composer",
//                    "album",
//                    "year"
            ));
            /*display = ImmutableList.of(
                    "artist",
                    "album",
                    "name"
            );/**/
            sort = new BeanPropertyComparator(ImmutableList.of(
//                    "genre",
//                    "grouping",
//                    "composer",
                    "artist",
                    "name"
            ));
            /*sort = new BeanPropertyComparator(ImmutableList.of(
                    "genre",
//                    "year",
                    "grouping",
//                    "albumArtist",
                    "album",
                    "trackNumber"
            ));/**/
            final double minutesPerMilli = 1.0 / 60 / 1000;
            format = ImmutableMap.of(
                    "totalTime", it->String.valueOf(Integer.parseInt(it) * minutesPerMilli),
                    "", Function.identity()
            );
        }
    }

    @Data
    static class Genre {
        public static final Genre NIL = new Genre("","");

        private final String name;
        private final String grouping;
    }
}
