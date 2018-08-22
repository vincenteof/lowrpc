package rpc.client;

import io.netty.bootstrap.Bootstrap;

/**
 * class $classname
 *
 * @author Yingpeng.Chen
 * @date 2018/8/22, 11:57
 */
public class NettyBootstrap {
    private NettyBootstrap() {}

    private static Bootstrap nioInstance;
    private static Bootstrap oioInstance;

    public static Bootstrap nioBootstrap() {
        return  null;
    }

    public static Bootstrap oioBootstrap() {
        return  null;
    }
}
