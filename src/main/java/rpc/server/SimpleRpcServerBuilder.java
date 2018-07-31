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
import rpc.srsd.ServiceRegistrationInfo;
import rpc.srsd.ServiceRegistry;
import rpc.util.ConfigurationUtil;
import rpc.util.Constant;
import rpc.util.ReflectionUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


/**
 * class $classname
 *
 * @author Yingpeng.Chen
 * @date 2018/7/26, 13:40
 */
// seems has some problems, it has no default value for port and beansPackName
public class SimpleRpcServerBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(SimpleRpcServerBuilder.class);

    private int port;
    private String beansPackName;
    private ServiceRegistry registry;

    private SimpleRpcServerBuilder() {}

    public static SimpleRpcServerBuilder builder(ServiceRegistry registry) {
        SimpleRpcServerBuilder result = new SimpleRpcServerBuilder();
        result.registry = registry;
        return result;
    }

    public SimpleRpcServerBuilder port(int port) {
        this.port = port;
        return this;
    }

    public SimpleRpcServerBuilder beansPackName(String beansPackName) {
        this.beansPackName = beansPackName;
        return this;
    }

    public RpcServer build() {
        return new SimpleRpcServer();
    }

    private class SimpleRpcServer extends RpcServer {
        SimpleRpcServer() {
            super(SimpleRpcServerBuilder.this.port);
        }

        @Override
        public void start() throws Exception {
            EventLoopGroup bossGroup = new NioEventLoopGroup(1);
            EventLoopGroup workerGroup = new NioEventLoopGroup();

            Map<String, Object> beans = new HashMap<>();
            ReflectionUtil.getClzFromPack(beansPackName).stream()
                .filter(clz -> clz.getDeclaredAnnotation(LowRpcService.class) != null)
                .forEach(clz -> {
                    Object bean;
                    LowRpcService service = clz.getDeclaredAnnotation(LowRpcService.class);
                    try {
                        Constructor c = clz.getDeclaredConstructor();
                        bean = c.newInstance();
                        beans.put(service.name(), bean);
                    } catch (NoSuchMethodException |
                            IllegalAccessException |
                            InvocationTargetException |
                            InstantiationException e) {
                        e.printStackTrace();
                        return;
                    }

                    Configuration config = ConfigurationUtil.getPropConfig(Constant.RPC_SERVER_CONFIG);
                    String address = config.getString(Constant.RPC_SERVER_ADDRESS);
                    String port = config.getString(Constant.RPC_SERVER_PORT);
                    Objects.requireNonNull(address);
                    Objects.requireNonNull(port);
                    // id is something like `testService-192-168-115-22`
                    String id = service.name() + "-" + address.replaceAll("\\.", "-");

                    ServiceRegistrationInfo regInfo = new ServiceRegistrationInfo();
                    regInfo.setName(service.name());
                    regInfo.setAddress(address);
                    regInfo.setPort(Integer.parseInt(port));
                    regInfo.setId(id);

                    LOG.info("Registration: {}", regInfo);

                    registry.register(regInfo);
                });

            try {
                ServerBootstrap b = new ServerBootstrap();
                b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new RpcServerInitializer(beans));

                b.bind(port).sync().channel().closeFuture().sync();
            } finally {
                bossGroup.shutdownGracefully();
                workerGroup.shutdownGracefully();
            }
        }
    }
}
