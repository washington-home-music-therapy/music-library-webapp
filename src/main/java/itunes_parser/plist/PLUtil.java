package itunes_parser.plist;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.commons.beanutils.converters.DateConverter;
import org.apache.commons.collections.Factory;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import java.io.IOException;
import java.text.*;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: karlpauls
 * Date: 5/26/13
 * Time: 2:49 PM
 * To change this template use File | Settings | File Templates.
 */
public final class PLUtil {
    static {
        ConvertUtils.register(new DateConverter(null), Date.class);
    }

    public static final Format IDENTITY_FORMAT = new Format() {
        @Override
        public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
            toAppendTo.append(obj.toString().trim());
            return toAppendTo;
        }

        @Override
        public Object parseObject(String source, ParsePosition pos) {
            pos.setIndex(source.length());
            return source.trim();
        }
    };
    public static final Format BOOLEAN_FORMAT = new Format() {
        @Override
        public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
            return toAppendTo.append(obj);
        }

        @Override
        public Object parseObject(String source, ParsePosition pos) {
            pos.setIndex(source.length());
            return Boolean.parseBoolean(source);
        }
    };
    public static final Format BASE64_FORMAT = new Format() {
        @Override
        public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
            BASE64Encoder encoder = new BASE64Encoder();
            return  toAppendTo.append(encoder.encode((byte[])obj));
        }

        @Override
        public Object parseObject(String source, ParsePosition pos) {
            BASE64Decoder decoder = new BASE64Decoder();
            pos.setIndex(source.length());
            try {
                return decoder.decodeBuffer(source);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
    };
    private static DateFormat DATE_FORMAT = new SimpleDateFormat("YYYY-MM-DD'T'HH:mm:ss'Z'");
    private static NumberFormat INT_FORMAT = DecimalFormat.getIntegerInstance();
    public static String toPropertyName(String plKey) {
        if(Character.isLowerCase(plKey.charAt(1))) {
            plKey = Character.toLowerCase(plKey.charAt(0)) + plKey.substring(1);
        }

        return plKey.replaceAll(" ","").replaceAll("ID","Id");
    }

    private static Map<String,String> TYPES = ImmutableMap.of("date", "Date", "integer", "int", "string", "String", "data", "byte[]");
    private static Map<String,Format> TYPE_CLASSES = ImmutableMap.of("date", DATE_FORMAT, "integer", INT_FORMAT, "string", IDENTITY_FORMAT, "boolean", BOOLEAN_FORMAT, "data", BASE64_FORMAT);

    public static String getType(String value) {
        return TYPES.get(value);
    }

    public static Object parse(String type, String value) throws ParseException {
        return TYPE_CLASSES.get(type).parseObject(value);
    }

    transient static PropertyUtilsBean utils = BeanUtilsBean.getInstance().getPropertyUtils();
    public static <T> T readFrom(T bean, Iterator<PLEvent> pl) {
        return readFrom(bean,pl,"");
    }

    private static <T> T readFrom(T bean, Iterator<PLEvent> pl, String address) {
        Map<String,Object> map = null;
        try {
            map = utils.describe(bean);
        } catch (Exception e) { // IllegalAccessException || InvocationTargetException || NoSuchMethodException
            throw new IllegalStateException("Could not describe " + bean.getClass() + " at " + address,e);
        }
        while(pl.hasNext()) {
            PLEvent event = pl.next();

            if (readMapFrom(map, event, pl, address)) {
                try {
                    BeanUtils.populate(bean, map);
                } catch (Exception e) { // IllegalAccessException || InvocationTargetException
                    throw new IllegalStateException("Could not populate " + bean.getClass() + " at " + address,e);
                }
                return bean;
            }
        }

        throw new IllegalArgumentException("Expected END_DICT, unexpected end of stream at " + address);
    }

    /**
     *
     * @param dst
     * @param event
     * @param itr
     * @return true if end of parent
     */
    private static boolean readMapFrom(Map<String, Object> dst, PLEvent event, Iterator<PLEvent> itr, String address) {
        String key;
        switch(event.getState()) {
            case KEY:
                key = event.asKey();
                break;
            case END_DICT:
                return true; // parent populates
            default:
                throw new IllegalArgumentException("Expected KEY or END_DICT, got " + event + " at " + address);
        }

        Object value;
        event = itr.next();
        switch (event.getState()) {
            case INTEGER:
            case DATE:
            case STRING:
            case DATA:
            case BOOLEAN:
                value = event.asValue();
                break;
            case START_ARRAY:
                // assert that described property is Collection
                if(!dst.containsKey(key)) {
                    throw new IllegalArgumentException("No such child list: " + key + " at " + address);
                }
                Object arrayObj = dst.get(key);
                if(arrayObj instanceof Collection) {
                    Factory childFactory = (Factory)dst.get(key + "Factory");
                    event = itr.next();
                    Collection childArray = (Collection)arrayObj;
                    do {
                        Object childObj = readFrom(childFactory.create(),itr,address + "." + key);
                        childArray.add(childObj);
                        event = itr.next();
                    } while(event.getState() != PLEventReader.State.END_ARRAY);
                } else {
                    throw new IllegalArgumentException("START_ARRAY does not describe " + arrayObj.getClass() + " at " + address);
                }
                value = arrayObj;
                break;
            case START_DICT:
                // assert that described property is Map
                // TODO singleton dict support for typed data?
                if(!dst.containsKey(key)) {
                    throw new IllegalArgumentException("No such child map: " + key + " at " + address);
                }
                Object mapObj = dst.get(key);
                if(mapObj instanceof Map) {
                    Factory childFactory = (Factory)dst.get(key + "Factory");
                    event = itr.next();
                    Map childMap = (Map)mapObj;
                    do {
                        readEntriesFrom(childMap, event.asKey(), childFactory, itr, address + "." + key);
                        event = itr.next();
                    } while(event.getState() == PLEventReader.State.KEY);
                } else {
                    throw new IllegalArgumentException("START_DICT does not describe " + mapObj.getClass() + " at " + address);
                }

                switch(event.getState()) {
                    default:
                        throw new IllegalArgumentException("Expected END_DICT, got " + event + " at " + address);
                    case END_DICT:
                        break;
                }
                value = mapObj;
                break;
            default:
                throw new IllegalArgumentException("Expected value, got " + event.getState() + " at " + address);
        }

        if(!dst.containsKey(key))
            throw new IllegalArgumentException("Undescribed property " + key + " at " + address + " state " + event.getState()); // TODO + " in " + bean.getClass());

        dst.put(key, value);
//            try {
//                BeanUtils.setProperty(bean,key,value);
//            } catch (Exception e) { // IllegalAccessException || InvocationTargetException e
//                throw new IllegalArgumentException("Could not set " + key + "=" + value + " on " + bean.getClass(),e);
//            }
        return false;
    }

    private static void readEntriesFrom(Map parent, String childKey, Factory childFactory, Iterator<PLEvent> itr, String address) {
        Object propertyObj;
        PLEvent event = itr.next(); // value (array or dict)
        switch(event.getState()) {
            case START_DICT:
                propertyObj = readFrom(childFactory.create(), itr, address);
                break;
            default:
                throw new IllegalArgumentException("Expected START_DICT, got " + event + " at " + address);
        }
        parent.put(childKey, propertyObj);
    }

}