package rpc.client;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalNotification;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rpc.util.LowPair;

import java.util.concurrent.TimeUnit;

/**
 * class $classname
 *
 * @author Yingpeng.Chen
 * @date 2018/8/28, 11:50
 */
public class NettyChannelManager {
    private static NettyChannelManager instance;
    private static final Logger LOG = LoggerFactory.getLogger(NettyChannelManager.class);
    private Bootstrap bootstrap;
    private Cache<LowPair<String, Integer>, Channel> channelCache = CacheBuilder.newBuilder()
        .maximumSize(50)
        .expireAfterAccess(30, TimeUnit.SECONDS)
        .removalListener(
            (RemovalNotification<LowPair<String, Integer>, Channel> removal) -> {
                LOG.info("Removal is happening");
                Channel c = removal.getValue();
                try {
                    LOG.info(
                        "Cached channel status: `isOpen: {}`, `isActive: {}`, `isRegistered: {}`, `isWritable: {}`",
                        c.isOpen(), c.isActive(), c.isRegistered(), c.isWritable()
                    );
                    if (c.isOpen()) {
                        c.close().sync();
                        LOG.info(
                            "Cached channel status: `isOpen: {}`, `isActive: {}`, `isRegistered: {}`, `isWritable: {}`",
                            c.isOpen(), c.isActive(), c.isRegistered(), c.isWritable()
                        );
                        LOG.info("Expired channel has been closed");
                    }
                } catch (InterruptedException e) {
                    LOG.info("Close channel failed: {}", e);
                }
            }
        ).build();

    private NettyChannelManager() {}

    public static NettyChannelManager getInstance() {
        synchronized (NettyChannelManager.class) {
            if (instance == null) {
                instance = new NettyChannelManager();
                EventLoopGroup group = new NioEventLoopGroup();
                Bootstrap b = new Bootstrap();
                b.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new RpcClientInitializer());
                instance.bootstrap = b;
            }
        }

        return instance;
    }

    public Channel getChannel(String address, int port) throws InterruptedException {
        LowPair<String, Integer> key = new LowPair<>(address, port);
        Channel cached = channelCache.getIfPresent(key);

        if (cached != null && cached.isOpen()) {
            return cached;
        } else {
            Channel newOne = bootstrap.connect(address, port).sync().channel();
            channelCache.put(key, newOne);
            return newOne;
        }
    }

    public void shutdown() {
        new Thread(() ->  bootstrap.config().group().shutdownGracefully()).start();
    }
}
