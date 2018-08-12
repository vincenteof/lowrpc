package rpc.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.apache.commons.configuration2.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rpc.inject.BeanHouse;
import rpc.srsd.ServiceRegistrationInfo;
import rpc.srsd.ServiceRegistry;
import rpc.util.ConfigurationUtil;
import rpc.util.NetworkUtil;

import java.util.Map;
import java.util.Objects;

import static rpc.util.Constant.*;

/**
 * A simple implementation of the abstract `RpcServer`, which is very inefficient.
 */
public class SimpleRpcServer extends RpcServer {
    private static final Logger LOG = LoggerFactory.getLogger(SimpleRpcServer.class);

    private final String beansPackName;
    private final ServiceRegistry registry;

    public static class Builder {
        private final String beansPackName;
        private final ServiceRegistry registry;

        private int port = 8322;

        public Builder(String beansPackName, ServiceRegistry registry) {
            this.beansPackName = beansPackName;
            this.registry = registry;
        }

        public Builder port(int port) {
            this.port = port;
            return this;
        }

        public SimpleRpcServer build() {
            return new SimpleRpcServer(this);
        }
    }


    private SimpleRpcServer(Builder builder) {
        super(builder.port);
        this.beansPackName = builder.beansPackName;
        this.registry = builder.registry;
    }

    public static Builder builder(String beansPackName, ServiceRegistry registry) {
        return new Builder(beansPackName, registry);
    }

    @Override
    public void start() throws Exception {
        Configuration config = ConfigurationUtil.getPropConfig(RPC_SERVER_CONFIG);
        BeanHouse beanHouse = BeanHouse.create(beansPackName);
        Map<String, Object> beans = beanHouse.getServiceBeans();

        beans.keySet().forEach(serviceName -> {
            // do not use the configuration for address
            String address = NetworkUtil.getLocalIp();
            String port = config.getString(RPC_SERVER_PORT);
            Objects.requireNonNull(address);
            Objects.requireNonNull(port);
            // id is something like `testService-192-168-115-22`
            String id = serviceName + "-" + address.replaceAll("\\.", "-");

            ServiceRegistrationInfo regInfo = new ServiceRegistrationInfo();
            regInfo.setName(serviceName);
            regInfo.setAddress(address);
            regInfo.setPort(Integer.parseInt(port));
            regInfo.setId(id);

            LOG.info("Registration: {}", regInfo);

            registry.register(regInfo);
        });

        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new RpcServerInitializer(beans));
            b.bind(this.getPort()).sync().channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
