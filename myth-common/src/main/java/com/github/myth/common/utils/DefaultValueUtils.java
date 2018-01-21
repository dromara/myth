package com.github.myth.common.utils;

/**
 *
 * @author xiaoyu@kuparts.com
 * @version 1.0
 * @date 2018/1/21 下午6:21
 * @since JDK 1.8
 */
public class DefaultValueUtils {


    private static final int ZERO = 0;

    public static  Object getDefaultValue(Class type) {
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
