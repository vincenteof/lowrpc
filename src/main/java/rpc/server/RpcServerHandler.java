package rpc.server;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import rpc.protocol.RpcRequest;
import rpc.protocol.RpcResponse;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;

/**
 * class $classname
 *
 * @author Yingpeng.Chen
 * @date 2018/7/25, 13:56
 */
public class RpcServerHandler extends SimpleChannelInboundHandler<RpcRequest> {
    private Map<String, Object> beans;

    public RpcServerHandler(Map<String, Object> beans) {
        this.beans = beans;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest msg) {

        String clzName = msg.getClzName();
        if (clzName == null || clzName.isEmpty()) {
            throw new IllegalArgumentException("Invalid class name: " + clzName);
        }

        String methodName = msg.getMethodName();
        if (methodName == null || methodName.isEmpty()) {
            throw new IllegalArgumentException("Invalid method name: " + methodName);
        }

        Object[] params = msg.getParams();
        Class<?>[] paramTypes = msg.getParamTypes();
        if (!hasSameLength(params, paramTypes)) {
            throw new IllegalArgumentException("Parameters and parameter types have different length");
        }

        if (!isParamsCompatible(params, paramTypes)) {
            throw new IllegalArgumentException("Parameters is not compatible with parameter types");
        }

        Object bean = beans.get(clzName);
        if (Objects.isNull(bean)) {
            throw new IllegalArgumentException("class `" + clzName + "` does not exists");
        }

        Object result;
        try {
            Method m = bean.getClass().getMethod(methodName, paramTypes);
            result = m.invoke(bean, params);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            throw new IllegalArgumentException(
                "class `" + clzName + "` does not have method which matches that signature");
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Method invocation error");
        }

        RpcResponse response = new RpcResponse();
        response.setStatus(1);
        response.setDescription("Success");
        response.setValue(result);
        response.setRequestId(msg.getRequestId());

        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        RpcResponse response = new RpcResponse();
        response.setStatus(0);
        response.setDescription(cause.getMessage());
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private boolean hasSameLength(Object[] params, Class<?>[] paramTypes) {
        if (params == null && paramTypes == null) {
            return true;
        }

        if (params == null || paramTypes == null) {
            return false;
        }

        return params.length == paramTypes.length;
    }

    private boolean isParamsCompatible(Object[] params, Class<?>[] paramTypes) {
        if (params == null && paramTypes == null) {
            return true;
        }

        if (params == null || paramTypes == null) {
            return false;
        }

        for (int i = 0; i < params.length; i++) {
            if (!(paramTypes[i].isInstance(params[i]))) {
                return false;
            }
        }

        return true;
    }
}
