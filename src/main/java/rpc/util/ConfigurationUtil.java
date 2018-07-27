package rpc.util;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;

import java.util.concurrent.ConcurrentHashMap;


/**
 * class $classname
 *
 * @author Yingpeng.Chen
 * @date 2018/7/27, 16:36
 */
public class ConfigurationUtil {
    private static final Configurations configs = new Configurations();
    private static final ConcurrentHashMap<String, Object> configMap = new ConcurrentHashMap<>();

    public static Configuration getPropConfig(String config) {
        return null;
    }

}
