package rpc.util;

/**
 * class $classname
 *
 * @author Yingpeng.Chen
 * @date 2018/7/27, 16:01
 */
public class MathUtil {
    /**
     * Get a random number from range
     * @param low lower bound of the range
     * @param high upper bound of the range
     * @return
     */
    public static int randomIntInRange(int low, int high) {
        if (low < 0 || high < 0 || low >= high) {
            throw new IllegalArgumentException("low must be smaller than high, and they should be both non-negative");
        }
        return low + (int)((high - low) * Math.random());
    }
}
