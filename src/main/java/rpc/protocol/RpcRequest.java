package rpc.protocol;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * class $classname
 *
 * @author Yingpeng.Chen
 * @date 2018/7/25, 11:05
 */
// Is it able to remove the setter of `requestId`
// Find a better way to create requestId
public class RpcRequest {
    private static AtomicInteger counter = new AtomicInteger(0);

    private String serviceName;
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

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
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
            "serviceName='" + serviceName + '\'' +
            ", methodName='" + methodName + '\'' +
            ", paramTypes=" + Arrays.toString(paramTypes) +
            ", params=" + Arrays.toString(params) +
            ", requestId=" + requestId +
            '}';
    }
}
