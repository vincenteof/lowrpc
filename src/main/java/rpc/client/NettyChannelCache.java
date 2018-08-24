package rpc.client;

import com.google.common.cache.*;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rpc.util.LowPair;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * class $classname
 *
 * @author Yingpeng.Chen
 * @date 2018/8/24, 10:30
 */
public class NettyChannelCache {
    private static NettyChannelCache instance;
    private static Logger LOG = LoggerFactory.getLogger(NettyChannelCache.class);

    private Cache<LowPair<String, Integer>, Channel> channelCache = CacheBuilder.newBuilder()
        .maximumSize(50)
        .expireAfterAccess(90, TimeUnit.SECONDS)
        .removalListener(
            (RemovalNotification<LowPair<String, Integer>, Channel> removal) -> {
                Channel c = removal.getValue();
                try {
                    c.closeFuture().sync();
                } catch (InterruptedException e) {
                    LOG.info("Close channel failed: {}", e);
                }
            }
        ).build();


    private NettyChannelCache() {}

    public static NettyChannelCache getInstance() {
        synchronized (NettyChannelCache.class) {
            if (instance == null) {
                instance = new NettyChannelCache();
            }
        }

        return instance;
    }

    public Channel getChannel(String address, int port, Bootstrap bootstrap) throws ExecutionException {
        return channelCache.get(new LowPair<>(address, port),
            () -> bootstrap.connect(address, port).sync().channel());
    }
}
