package rpc.client;

import com.google.common.cache.*;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rpc.util.LowPair;

import java.util.concurrent.*;

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
        .expireAfterAccess(30, TimeUnit.SECONDS)
        .removalListener(
            (RemovalNotification<LowPair<String, Integer>, Channel> removal) -> {
                LOG.info("Removal is happening");
                Channel c = removal.getValue();
                try {
//                    c.closeFuture().sync();   this operation blocks until the server close the connection
                    LOG.info(
                        "Cached channel status: `isOpen: {}`, `isActive: {}`, `isRegistered: {}`, `isWritable: {}`",
                        c.isOpen(), c.isActive(), c.isRegistered(), c.isWritable()
                    );
                    c.close().sync();
                    LOG.info(
                        "Cached channel status: `isOpen: {}`, `isActive: {}`, `isRegistered: {}`, `isWritable: {}`",
                        c.isOpen(), c.isActive(), c.isRegistered(), c.isWritable()
                    );
                } catch (InterruptedException e) {
                    LOG.info("Close channel failed: {}", e);
                    return;
                }
                LOG.info("Expired channel has been closed");
                this.cleanHandle.cancel(true);
            }
        ).build();

    private final ScheduledExecutorService scheduler =
        Executors.newScheduledThreadPool(1, r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });

    private ScheduledFuture<?> cleanHandle;

    private NettyChannelCache() {}

    public static NettyChannelCache getInstance() {
        synchronized (NettyChannelCache.class) {
            if (instance == null) {
                instance = new NettyChannelCache();
                final Runnable cleaner = () -> {
                    LOG.info("In cleaner");
                    instance.channelCache.cleanUp();
                };
                instance.cleanHandle = instance.scheduler.scheduleAtFixedRate(
                    cleaner, 20, 20, TimeUnit.SECONDS
                );
            }
        }

        return instance;
    }

    // prob: need some reconnect strategy if the connection is closed ???
    public Channel getChannel(String address, int port, Bootstrap bootstrap) throws ExecutionException {
        return channelCache.get(
            new LowPair<>(address, port),
            () -> bootstrap.connect(address, port).sync().channel()
        );
    }
}
