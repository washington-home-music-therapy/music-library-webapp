package itunes_parser.plist;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.ImmutableList;
import lombok.Getter;
import lombok.Setter;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.io.InputStream;
import java.io.Reader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static javax.xml.stream.XMLStreamConstants.*;

/**
 * Created with IntelliJ IDEA.
 * User: karlpauls
 * Date: 5/25/13
 * Time: 8:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class PLEventReader extends AbstractIterator<PLEvent> {
    public static enum State {
        START_PLIST(START_ELEMENT,"plist"),
        END_PLIST(END_ELEMENT,"plist"),
        START_DICT(START_ELEMENT,"dict"),
        END_DICT(END_ELEMENT,"dict"),
        KEY(START_ELEMENT,"key",CHARACTERS,END_ELEMENT),
        INTEGER(START_ELEMENT,"integer",CHARACTERS,END_ELEMENT),
        DATE(START_ELEMENT,"date",CHARACTERS,END_ELEMENT),
        STRING(START_ELEMENT,"string",CHARACTERS,END_ELEMENT),
        DATA(START_ELEMENT,"data",CHARACTERS,END_ELEMENT),
        BOOLEAN(START_ELEMENT,"(true|false)",END_ELEMENT),
        START_ARRAY(START_ELEMENT,"array"),
        END_ARRAY(END_ELEMENT,"array"),
        ;

        public int trigger;
        public Pattern name;
        public int[] tail;

        private State(int keyEvent, String nameMatches, int... consumeEvents) {
            trigger = keyEvent;
            name = Pattern.compile(nameMatches);
            tail = consumeEvents;
        }

    }

    private static final Map<Pattern,State> start = new HashMap<Pattern, State>();
    private static final Map<Pattern,State> end = new HashMap<Pattern, State>();

    static {
        for( State s : State.values() ) {
            switch(s.trigger) {
                case START_ELEMENT:
                    start.put(s.name, s);
                    break;
                case END_ELEMENT:
                    end.put(s.name,s);
                    break;
                default:
            }
        }
    }

    private State state = null;

    @Setter
    @Getter
    private XMLEventReader xml;

    public PLEventReader(Reader rdr) throws XMLStreamException {
        this(XMLInputFactory.newInstance().createXMLEventReader(rdr));
    }

    public PLEventReader(InputStream is) throws XMLStreamException {
        this(XMLInputFactory.newInstance().createXMLEventReader(is));
    }

    public PLEventReader(XMLEventReader xml) {
        this.xml = xml;
    }

    @Override
    protected PLEvent computeNext() {
        try {
            XMLEvent event = null;
            State state = null;
            while(state == null) {
                if(! xml.hasNext() )
                    return endOfData();

                event = xml.nextEvent();
                String tag;
                eventType:
                switch (event.getEventType()) {
                    case START_ELEMENT:
                        tag = event.asStartElement().getName().getLocalPart();
                        for(Map.Entry<Pattern,State> e : start.entrySet()) {
                            Matcher m = e.getKey().matcher(tag);

                            if(m.matches()) {
                                // TODO boolean true/false?
                                state = e.getValue();
                                break eventType;
                            }
                        }
//                        state = start.get(event.asStartElement().getName().getLocalPart());
                        break;

                    case END_ELEMENT:
                        tag = event.asEndElement().getName().getLocalPart();
                        for(Map.Entry<Pattern,State> e : end.entrySet()) {
                            Matcher m = e.getKey().matcher(tag);

                            if(m.matches()) {
                                // TODO boolean true/false?
                                state = e.getValue();
                                break eventType;
                            }
                        }
//                        state = end.get(event.asEndElement().getName().getLocalPart());
                        break;

                    default:
                }
            }

            if(state.tail.length == 0) {
                return new PLEvent(state, ImmutableList.of(event));
            }

            List<XMLEvent> events = new ArrayList<XMLEvent>();
            events.add(event);

            for(int i = 0; i < state.tail.length; i++) {
                while(event.getEventType() != state.tail[i]) {
                    event = xml.nextEvent();
                }

                events.add(event);
            }

            return new PLEvent(state, ImmutableList.copyOf(events));
        } catch (XMLStreamException e) {
            e.printStackTrace();
            return endOfData();
        }
    }

    public PLEvent seek(State key, String name, State ... tail) {
        while(hasNext()) {
            PLEvent event = next();
            while(event.getState() != key) {
                if(!hasNext()) return null;
                event = next();
            }

            if(name != null && !name.equals(event.getXml().get(1).toString()))
                continue;

            for(int i = 0; i < tail.length; i++) {
                while(event.getState() != tail[i]) {
                    if(!hasNext()) return null;
                    event = next();
                }
            }

            return event;
        }

        return null;
    }

}