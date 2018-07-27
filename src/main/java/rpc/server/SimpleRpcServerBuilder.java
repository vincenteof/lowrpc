package rpc.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.apache.commons.configuration2.Configuration;
import rpc.srsd.ServiceRegistrationInfo;
import rpc.srsd.ServiceRegistry;
import rpc.util.ConfigurationUtil;
import rpc.util.ReflectionUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


/**
 * class $classname
 *
 * @author Yingpeng.Chen
 * @date 2018/7/26, 13:40
 */
public class SimpleRpcServerBuilder {
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
                    }
                    // id在注册时如何生成???
                    Configuration config = ConfigurationUtil.getPropConfig("common");
                    String address = Optional.ofNullable(config.getString("rpc.server.address"))
                        .orElseThrow(() -> new IllegalStateException("`rpc.server.address` is not configured"));
                    int port = config.getInt("rpc.server.port");

                    ServiceRegistrationInfo regInfo = new ServiceRegistrationInfo();
                    regInfo.setName(service.name());
                    regInfo.setAddress(address);
                    regInfo.setPort(port);
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
