package com.jn.sqlhelper.common.utils;

import com.jn.langx.util.Numbers;

public class FloatConverter implements Converter<Object, Float> {
    private static final DoubleConverter doubleConverter = DoubleConverter.INSTANCE;

    @Override
    public Float apply(Object input) {
        Double doubleValue = doubleConverter.apply(input);
        return Numbers.convertNumberToTargetClass(doubleValue, Float.class);
    }
}
