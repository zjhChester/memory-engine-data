package xyz.jiahao.memory.engine.utils;

import java.util.Collection;
import java.util.Map;

/**
 * @author jiahao.zhang
 */
public class CollectionUtils {

    public static boolean isEmpty(Collection<?> collection) {
        return (collection == null || collection.isEmpty());
    }

    public static boolean isEmpty(Map<?, ?> map) {
        return (map == null || map.isEmpty());
    }
}
