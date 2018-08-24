package rpc.util;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;


/**
 * class $classname
 *
 * @author Yingpeng.Chen
 * @date 2018/7/27, 16:36
 */
public class ConfigurationUtil {
    private static Logger LOG = LoggerFactory.getLogger(ConfigurationUtil.class);

    private static final Configurations configs = new Configurations();
    private static final ConcurrentHashMap<String, Object> configMap = new ConcurrentHashMap<>();
    private static final String CLZ_ROOT_PATH = Configuration.class.getResource("/").getPath();

    public static Configuration getPropConfig(String configFile) {
        if (configMap.containsKey(configFile)) {
            return (Configuration) configMap.get(configFile);
        }

        try {
            String filePath =  CLZ_ROOT_PATH + configFile;
            Configuration config = configs.properties(filePath);
            configMap.putIfAbsent(configFile, config);
            return config;
        } catch (ConfigurationException e) {
            LOG.error("Error in `ConfigurationUtil`: {}", e);
            return null;
        }
    }

    public static XMLConfiguration getXmlConfig(String configFile) {
        if (configMap.containsKey(configFile)) {
            return (XMLConfiguration) configMap.get(configFile);
        }

        try {
            XMLConfiguration config = configs.xml(configFile);
            configMap.putIfAbsent(configFile, config);
            return config;
        } catch (ConfigurationException e) {
            LOG.error("Error in `ConfigurationUtil`: {}", e);
            return null;
        }
    }

    public static void main(String[] args) {
        Configuration config = getPropConfig("rpc-server-config.properties");
        System.out.println(config.getString("rpc.server.address"));
    }
}
