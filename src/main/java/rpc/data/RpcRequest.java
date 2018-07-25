package rpc.data;

import rpc.util.JsonSerializationUtil;

import java.util.Arrays;

/**
 * class $classname
 *
 * @author Yingpeng.Chen
 * @date 2018/7/25, 11:05
 */

public class RpcRequest {
    private String clzName;
    private String methodName;
    private Class<?>[] paramTypes;
    private Object[] params;

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

    @Override
    public String toString() {
        return "RpcRequest{" +
            "clzName='" + clzName + '\'' +
            ", methodName='" + methodName + '\'' +
            ", paramTypes=" + Arrays.toString(paramTypes) +
            ", params=" + Arrays.toString(params) +
            '}';
    }

    public static void main(String[] args) {
        RpcRequest req = new RpcRequest();
        req.setClzName("Test");
        req.setMethodName("test");
        req.setParams(new Object[] {1, 2});
        req.setParamTypes(new Class<?>[] {Integer.class, Integer.class});

        RpcRequest afterReq = JsonSerializationUtil.fromBytes(JsonSerializationUtil.toBytes(req), RpcRequest.class);
        System.out.println(afterReq);
    }
}
