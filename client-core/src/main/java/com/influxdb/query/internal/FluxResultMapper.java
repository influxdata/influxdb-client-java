/*
 * The MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.influxdb.query.internal;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.influxdb.annotations.Column;
import com.influxdb.exceptions.InfluxException;
import com.influxdb.query.FluxRecord;

public class FluxResultMapper {

    // we don't cover some corner cases(e.g. someURI someHRName) under which explicit mapping is required
    private static final Pattern CAMEL_CASE_TO_SNAKE_CASE_REPLACE_PATTERN = Pattern.compile("[A-Z]");

    /**
     * Maps FluxRecord into custom POJO class.
     *
     * @param record flux record
     * @param clazz  target class
     * @param <T>    pojo type
     * @return
     */

    @Nonnull
    public <T> T toPOJO(@Nonnull final FluxRecord record, @Nonnull final Class<T> clazz) {

        Objects.requireNonNull(record, "Record is required");
        Objects.requireNonNull(clazz, "Class type is required");

        try {
            T pojo = clazz.newInstance();

            Class<?> currentClazz = clazz;
            while (currentClazz != null) {

                Field[] fields = currentClazz.getDeclaredFields();
                for (Field field : fields) {
                    Column anno = field.getAnnotation(Column.class);
                    String columnName = field.getName();
                    if (anno != null && !anno.name().isEmpty()) {
                        columnName = anno.name();
                    }

                    Map<String, Object> recordValues = record.getValues();

                    String col = null;

                    if (recordValues.containsKey(columnName)) {
                        col = columnName;
                    } else if (recordValues.containsKey("_" + columnName)) {
                        col = "_" + columnName;
                    } else if (anno != null && anno.measurement()) {
                        col = "_measurement";
                    } else {
                        String columnNameInSnakeCase = camelCaseToSnakeCase(columnName);
                        if (recordValues.containsKey(columnNameInSnakeCase)) {
                            col = columnNameInSnakeCase;
                        }
                    }

                    if (col != null) {
                        Object value = record.getValueByKey(col);

                        setFieldValue(pojo, field, value);
                    }
                }

                currentClazz = currentClazz.getSuperclass();
            }
            return pojo;
        } catch (Exception e) {
            throw new InfluxException(e);
        }
    }

    private String camelCaseToSnakeCase(final String str) {
        return CAMEL_CASE_TO_SNAKE_CASE_REPLACE_PATTERN.matcher(str)
            .replaceAll("_$0")
            .toLowerCase();
    }

    private void setFieldValue(@Nonnull final Object object,
                               @Nullable final Field field,
                               @Nullable final Object value) {

        if (field == null || value == null) {
            return;
        }
        String msg =
            "Class '%s' field '%s' was defined with a different field type and caused a ClassCastException. "
                + "The correct type is '%s' (current field value: '%s').";

        try {
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            Class<?> fieldType = field.getType();

            //the same type
            if (fieldType.equals(value.getClass())) {
                field.set(object, value);
                return;
            }

            //convert primitives
            if (double.class.isAssignableFrom(fieldType)) {
                field.setDouble(object, toDoubleValue(value));
                return;
            }
            if (long.class.isAssignableFrom(fieldType)) {
                field.setLong(object, toLongValue(value));
                return;
            }
            if (int.class.isAssignableFrom(fieldType)) {
                field.setInt(object, toIntValue(value));
                return;
            }
            if (boolean.class.isAssignableFrom(fieldType)) {
                field.setBoolean(object, Boolean.valueOf(String.valueOf(value)));
                return;
            }
            if (BigDecimal.class.isAssignableFrom(fieldType)) {
                field.set(object, toBigDecimalValue(value));
                return;
            }

            //enum
            if (fieldType.isEnum()) {
                //noinspection unchecked, rawtypes
                field.set(object, Enum.valueOf((Class<Enum>) fieldType, String.valueOf(value)));
                return;
            }

            field.set(object, value);

        } catch (ClassCastException | IllegalAccessException e) {

            throw new InfluxException(String.format(msg, object.getClass().getName(), field.getName(),
                value.getClass().getName(), value));
        }
    }

    private double toDoubleValue(final Object value) {

        if (double.class.isAssignableFrom(value.getClass()) || Double.class.isAssignableFrom(value.getClass())) {
            return (double) value;
        }

        return (Double) value;
    }

    private long toLongValue(final Object value) {

        if (long.class.isAssignableFrom(value.getClass()) || Long.class.isAssignableFrom(value.getClass())) {
            return (long) value;
        }

        return ((Double) value).longValue();
    }

    private int toIntValue(final Object value) {

        if (int.class.isAssignableFrom(value.getClass()) || Integer.class.isAssignableFrom(value.getClass())) {
            return (int) value;
        }

        return ((Double) value).intValue();
    }

    private BigDecimal toBigDecimalValue(final Object value) {
        if (String.class.isAssignableFrom(value.getClass())) {
            return new BigDecimal((String) value);
        }

        if (double.class.isAssignableFrom(value.getClass()) || Double.class.isAssignableFrom(value.getClass())) {
            return BigDecimal.valueOf((double) value);
        }

        if (int.class.isAssignableFrom(value.getClass()) || Integer.class.isAssignableFrom(value.getClass())) {
            return BigDecimal.valueOf((int) value);
        }

        if (long.class.isAssignableFrom(value.getClass()) || Long.class.isAssignableFrom(value.getClass())) {
            return BigDecimal.valueOf((long) value);
        }

        String message = String.format("Cannot cast %s [%s] to %s.",
                value.getClass().getName(), value, BigDecimal.class);

        throw new ClassCastException(message);
    }
}
