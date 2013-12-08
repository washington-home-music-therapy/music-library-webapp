package itunes_parser;

import com.google.common.collect.ImmutableList;
import org.apache.commons.beanutils.BeanUtils;
import itunes_parser.bean.BeanPropertyComparator;
import itunes_parser.itunes.MusicLibrary;
import itunes_parser.itunes.MusicTrack;
import itunes_parser.plist.PLEventReader;
import itunes_parser.plist.PLUtil;

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



    public static void main2(String[] argv) throws Throwable {
        File errorF = new File(ITunes.class.getName() + "_errors_" + DATE.format(new Date()) + ".txt");
        FileOutputStream errorFile = new FileOutputStream(errorF);
        PrintWriter error = new PrintWriter(errorFile);
        try {
            ITunes itReader = new ITunes("genre", "year", "grouping", "artist", "album", "trackNumber", "name");
            MusicLibrary library = itReader.read(new File("iTunes Music Library.xml").getCanonicalFile());
            PrintWriter out = new PrintWriter(new FileWriter(getNumberedFile("demo-itunes-song-list-" + DATE.format(new Date()), ".csv")));
            itReader.printTo(library,out);
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

    private static File getNumberedFile(String prefix, String suffix) {
        File file = new File(prefix + suffix);
        int i = 0;
        while(file.exists()) {
            i++;
            file = new File(prefix + "-" + i + suffix);
        }
        return file;
    }


    public ITunes(String ... displayKeys) {
        List<String> columns = new ArrayList<String>();
        List<String> order = new ArrayList<String>();

        for(String s : displayKeys) {
            if(s.startsWith("*")) {
                order.add(s.substring(1));
            } else {
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

        itr.seek(PLEventReader.State.START_PLIST,null, PLEventReader.State.START_DICT);

        MusicLibrary library = PLUtil.readFrom(new MusicLibrary(), itr);

        return library;
    }

    public void printTo(MusicLibrary library, PrintWriter out) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        TreeSet<MusicTrack> tracks = new TreeSet<MusicTrack>(sort);
        tracks.addAll(library.getTracks().values());

        for(String s : display) {
            out.print(s);
            out.print(",");
        }
        out.println();

        for(MusicTrack to : tracks) {
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
