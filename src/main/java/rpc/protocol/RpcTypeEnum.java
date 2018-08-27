package rpc.protocol;

/**
 * class $classname
 *
 * @author Yingpeng.Chen
 * @date 2018/8/27, 16:08
 */
public enum RpcTypeEnum {
    REQUEST("request", 0), RESPONSE("response", 1), CLOSE("close", -1);

    private final String name;
    private final int value;

    RpcTypeEnum(String name, int value) {
        this.name = name;
        this.value = value;
    }


    public String getName() {
        return name;
    }

    public int getValue() {
        return value;
    }
}
