package rpc.util;

import com.alibaba.fastjson.JSON;

import java.nio.charset.Charset;

/**
 * class $classname
 *
 * @author Yingpeng.Chen
 * @date 2018/7/25, 11:37
 */
public class JsonSerializationUtil {
    public static byte[] toBytes(Object obj) {
        return JSON.toJSONString(obj).getBytes(Charset.forName("UTF-8"));
    }

    public static <T> T fromBytes(byte[] bytes, Class<T> clz) {
        return JSON.parseObject(bytes, clz);
    }
}
