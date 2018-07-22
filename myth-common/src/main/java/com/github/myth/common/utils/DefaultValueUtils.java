package com.github.myth.common.utils;

/**
 * DefaultValueUtils.
 * @author xiaoyu
 */
public class DefaultValueUtils {

    private static final int ZERO = 0;

    /**
     * return default object.
     * @param type class
     * @return Object
     */
    public static Object getDefaultValue(final Class type) {
        if (boolean.class.equals(type)) {
            return Boolean.FALSE;
        } else if (byte.class.equals(type)) {
            return ZERO;
        } else if (short.class.equals(type)) {
            return ZERO;
        } else if (int.class.equals(type)) {
            return ZERO;
        } else if (long.class.equals(type)) {
            return ZERO;
        } else if (float.class.equals(type)) {
            return ZERO;
        } else if (double.class.equals(type)) {
            return ZERO;
        }
        return null;
    }

}
