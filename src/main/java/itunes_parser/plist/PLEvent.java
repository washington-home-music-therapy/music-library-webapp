package itunes_parser.plist;

import lombok.Data;

import javax.xml.stream.events.XMLEvent;
import java.text.ParseException;
import java.util.List;

/**
* Created with IntelliJ IDEA.
* User: karlpauls
* Date: 5/26/13
* Time: 2:58 PM
* To change this template use File | Settings | File Templates.
*/
@Data
public class PLEvent {
    private final PLEventReader.State state;
    private final List<XMLEvent> xml;

    public String asKey() {
        return PLUtil.toPropertyName(getXml().get(1).toString());
    }

    public String asType() {
        return getXml().get(0).asStartElement().getName().getLocalPart();
    }

    public Object asValue() {
        try {
            switch(state) {
                case BOOLEAN:
                    return PLUtil.parse("boolean",getXml().get(0).asStartElement().getName().getLocalPart());
                default:
                    return PLUtil.parse(asType(),getXml().get(1).toString());
            }
        } catch (ParseException e) {
            throw new IllegalArgumentException("Could not parse " + state + ": " + xml,e);
        }
    }
}
