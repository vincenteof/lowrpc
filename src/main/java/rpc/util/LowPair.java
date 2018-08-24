package rpc.util;

import java.util.Objects;

/**
 * class $classname
 *
 * @author Yingpeng.Chen
 * @date 2018/8/24, 10:21
 */
public class LowPair<T1, T2> {
    private T1 first;
    private T2 second;

    public LowPair(T1 first, T2 second) {
        this.first = first;
        this.second = second;
    }

    public T1 getFirst() {
        return first;
    }

    public T2 getSecond() {
        return second;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LowPair<?, ?> lowPair = (LowPair<?, ?>) o;
        return Objects.equals(first, lowPair.first) &&
            Objects.equals(second, lowPair.second);
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }

    @Override
    public String toString() {
        return "LowPair{" +
            "first=" + first +
            ", second=" + second +
            '}';
    }
}
