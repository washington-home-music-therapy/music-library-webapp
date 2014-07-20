package itunes_parser;

import com.google.common.collect.*;
import itunes_parser.bean.BeanPropertyComparator;
import itunes_parser.itunes.MusicLibrary;
import itunes_parser.itunes.MusicTrack;
import itunes_parser.itunes.MusicTrackReference;
import itunes_parser.plist.PLEventReader;
import itunes_parser.plist.PLUtil;
import org.apache.commons.beanutils.BeanUtils;

import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * ITunes reader
 */
public final class ITunes {
    private static final SimpleDateFormat DATE = new SimpleDateFormat("YYYYMMdd");

    private final ImmutableList<String> display;
    private final BeanPropertyComparator sort;


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
            ITunes itReader = new ITunes("*genre", "year", "*grouping", "name", "artist", "album", "*trackNumber");
            MusicLibrary library = itReader.read(file);
            ImmutableListMultimap<String,MusicTrack> genres = Multimaps.index(
                    library.getTracks().values(), track -> track.getGenre());

            File completeFile = getNumberedFile("complete-library-" + DATE.format(new Date()), ".csv");
            PrintWriter out = new PrintWriter(new FileWriter(completeFile));
            itReader.printTo(library,out);
            out.close();

            for(String genre : genres.keySet()) {
                String grouping = genres.get(genre).get(0).getGrouping();
                out = new PrintWriter(new FileWriter(getChildFile(completeFile, grouping, "/", genre, "-", DATE.format(new Date()), ".csv")));
                itReader.printHeader(out);
                itReader.printTo(genres.get(genre),library,out);
                out.close();
            }
        } catch(Exception e) {
            e.printStackTrace(error);
        } catch(Throwable t) {
            t.printStackTrace();
            t.printStackTrace(error);
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


    public ITunes(String ... displayKeys) {
        List<String> columns = new ArrayList<String>();
        List<String> order = new ArrayList<String>();

        for(String s : displayKeys) {
            if(s.startsWith("*")) {
                order.add(s.substring(1));
            } else if(s.startsWith("#")) {
                columns.add(s.substring(1));
            }else {
                order.add(s);
                columns.add(s);
            }
        }

        display = ImmutableList.copyOf(columns);
        sort = new BeanPropertyComparator(ImmutableList.copyOf(order));
    }

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

    public void printTo(MusicLibrary library, PrintWriter out) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        printHeader(out);
        printTo(library.getTracks().values(), library, out);
    }

    public void printHeader(PrintWriter out) {
        for(String s : display) {
            out.print(s);
            out.print(",");
        }
        out.println();
    }

    public void printTo(Collection<? extends MusicTrackReference> tracks, MusicLibrary library, PrintWriter out) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {

        TreeSet<MusicTrackReference> sortedTracks = new TreeSet<MusicTrackReference>(sort);
        sortedTracks.addAll(tracks);

        for(MusicTrackReference trackReference : sortedTracks) {
            MusicTrack to = trackReference.getTrack(library);
            Map track = BeanUtils.describe(to);
            for(String s : display) {
                Object o = track.get(s);
                if(o != null) {
                    String ov = o.toString();
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
}
