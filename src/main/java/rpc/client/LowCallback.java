package rpc.client;

/**
 * class $classname
 *
 * Just an interface for function type `V => Void`.
 *
 * @author Yingpeng.Chen
 * @date 2018/8/21, 14:59
 */
public interface LowCallback<V> {
    void callback(V result);
}
