package rpc.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rpc.protocol.RpcRequest;
import rpc.protocol.RpcResponse;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;

/**
 * class $classname
 *
 * Handling of incoming rpc requests
 *
 * @author Yingpeng.Chen
 * @date 2018/7/25, 13:56
 */
public class RpcServerHandler extends SimpleChannelInboundHandler<RpcRequest> {
    private static final Logger LOG = LoggerFactory.getLogger(RpcServerHandler.class);
    private final Map<String, Object> beans;

    public RpcServerHandler(Map<String, Object> beans) {
        this.beans = beans;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest msg) {
        String serviceName = msg.getServiceName();
        if (serviceName == null || serviceName.isEmpty()) {
            throw new IllegalArgumentException("Invalid service name: " + serviceName);
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

        // get the result from the requested service beans, which is constructed when the server starts
        Object bean = beans.get(serviceName);
        if (Objects.isNull(bean)) {
            throw new IllegalArgumentException("Service `" + serviceName + "` does not exists");
        }

        Object result;
        try {
            Method m = bean.getClass().getMethod(methodName, paramTypes);
            result = m.invoke(bean, params);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            throw new IllegalArgumentException(
                "class `" + serviceName + "` does not have method which matches that signature");
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Method invocation error");
        }

        RpcResponse response = new RpcResponse();
        response.setStatus(1);
        response.setDescription("Success");
        response.setValue(result);
        response.setRequestId(msg.getRequestId());

//        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        ctx.writeAndFlush(response);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        RpcResponse response = new RpcResponse();
        response.setStatus(0);
        response.setDescription(cause.getMessage());

//        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        ctx.writeAndFlush(response);
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

    /**
     * Check whether the incoming rpc request is valid.
     * @param params the passing parameters for this request
     * @param paramTypes the type of the parameters
     * @return
     */
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
