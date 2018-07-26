package rpc.protocol;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * class $classname
 *
 * @author Yingpeng.Chen
 * @date 2018/7/25, 11:05
 */

public class RpcRequest {
    private static AtomicInteger counter = new AtomicInteger(0);

    private String clzName;
    private String methodName;
    private Class<?>[] paramTypes;
    private Object[] params;
    private Integer requestId;

    private RpcRequest() {}

    public static RpcRequest create() {
        RpcRequest request = new RpcRequest();
        request.requestId = counter.getAndIncrement();
        return request;
    }

    public static AtomicInteger getCounter() {
        return counter;
    }

    public static void setCounter(AtomicInteger counter) {
        RpcRequest.counter = counter;
    }

    public String getClzName() {
        return clzName;
    }

    public void setClzName(String clzName) {
        this.clzName = clzName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Class<?>[] getParamTypes() {
        return paramTypes;
    }

    public void setParamTypes(Class<?>[] paramTypes) {
        this.paramTypes = paramTypes;
    }

    public Object[] getParams() {
        return params;
    }

    public void setParams(Object[] params) {
        this.params = params;
    }

    public Integer getRequestId() {
        return requestId;
    }

    public void setRequestId(Integer requestId) {
        this.requestId = requestId;
    }

    @Override
    public String toString() {
        return "RpcRequest{" +
            "clzName='" + clzName + '\'' +
            ", methodName='" + methodName + '\'' +
            ", paramTypes=" + Arrays.toString(paramTypes) +
            ", params=" + Arrays.toString(params) +
            ", requestId=" + requestId +
            '}';
    }
}
