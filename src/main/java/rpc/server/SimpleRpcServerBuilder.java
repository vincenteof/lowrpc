package rpc.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import rpc.util.ReflectionUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;


/**
 * class $classname
 *
 * @author Yingpeng.Chen
 * @date 2018/7/26, 13:40
 */
public class SimpleRpcServerBuilder {
    private int port;
    private String beansPackName;

    private SimpleRpcServerBuilder() {}

    public static SimpleRpcServerBuilder builder() {
        return new SimpleRpcServerBuilder();
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
            ReflectionUtil.getClzFromPack(beansPackName).forEach(clz -> {
                Object bean;
                try {
                    Constructor c = clz.getDeclaredConstructor();
                    bean = c.newInstance();
                    beans.put(clz.getName(), bean);
                } catch (NoSuchMethodException |
                        IllegalAccessException |
                        InvocationTargetException |
                        InstantiationException e) {
                    e.printStackTrace();
                }
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

    public static void main(String[] args) {
        Class<Integer> c = Integer.class;
        System.out.println(c.getName());
    }
}
