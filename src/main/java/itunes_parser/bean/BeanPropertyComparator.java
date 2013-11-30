package itunes_parser.bean;

import lombok.Data;
import org.apache.commons.beanutils.BeanUtils;

import java.util.Comparator;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: karlpauls
 * Date: 5/31/13
 * Time: 8:17 AM
 * To change this template use File | Settings | File Templates.
 */
@Data
public class BeanPropertyComparator implements Comparator {
    private final List<String> properties;

    @Override
    public int compare(Object o1, Object o2) {
        try {
            for(String p : properties) {
                String p1 = BeanUtils.getProperty(o1,p);
                String p2 = BeanUtils.getProperty(o2,p);
                if(p1 != p2) {
                    if(p1 == null) return 1;
                    if(p2 == null) return -1;
                    int diff = p1.compareTo(p2);
                    if(diff != 0) return diff;
                }
            }
//        } catch (IllegalAccessException e) {
//        } catch (InvocationTargetException e) {
//        } catch (NoSuchMethodException e) {
        } catch(Exception e) {
            throw new RuntimeException(e);
        }

        return 0;
    }
}
