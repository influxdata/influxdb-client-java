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
package com.influxdb.client.internal;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;

import com.influxdb.Arguments;
import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.exceptions.InfluxException;

/**
 * @author Jakub Bednar (bednar@github) (15/10/2018 13:04)
 */
class MeasurementMapper {

    private static final Logger LOG = Logger.getLogger(MeasurementMapper.class.getName());

    private static final ConcurrentMap<String, ConcurrentMap<String, Field>> CLASS_FIELD_CACHE
            = new ConcurrentHashMap<>();

    @Nonnull
    <M> Point toPoint(@Nonnull final M measurement, @Nonnull final WritePrecision precision) throws InfluxException {

        Arguments.checkNotNull(measurement, "measurement");

        Class<?> measurementType = measurement.getClass();
        cacheMeasurementClass(measurementType);

        if (measurementType.getAnnotation(Measurement.class) == null) {
            String message = String
                    .format("Measurement type '%s' does not have a @Measurement annotation.", measurementType);

            throw new InfluxException(message);
        }

        Point point = Point.measurement(getMeasurementName(measurementType));

        CLASS_FIELD_CACHE.get(measurementType.getName()).forEach((name, field) -> {

            Column column = field.getAnnotation(Column.class);

            Object value;
            try {
                field.setAccessible(true);
                value = field.get(measurement);
            } catch (IllegalAccessException e) {

                throw new InfluxException(e);
            }

            if (value == null) {
                Object[] params = {field.getName(), measurement};
                LOG.log(Level.FINEST, "Field {0} of {1} has null value", params);
                return;
            }

            Class<?> fieldType = field.getType();
            if (column.tag()) {
                point.addTag(name, value.toString());
            } else if (column.timestamp()) {
                Instant instant = (Instant) value;
                point.time(instant, precision);
            } else if (isNumber(fieldType)) {
                point.addField(name, (Number) value);
            } else if (Boolean.class.isAssignableFrom(fieldType) || boolean.class.isAssignableFrom(fieldType)) {
                point.addField(name, (Boolean) value);
            } else if (String.class.isAssignableFrom(fieldType)) {
                point.addField(name, (String) value);
            } else {
                point.addField(name, value.toString());
            }
        });

        LOG.log(Level.FINEST, "Mapped measurement: {0} to Point: {1}", new Object[]{measurement, point});

        return point;
    }

    @Nonnull
    private String getMeasurementName(@Nonnull final Class<?> measurementType) {
        return measurementType.getAnnotation(Measurement.class).name();
    }

    private boolean isNumber(@Nonnull final Class<?> fieldType) {
        return Number.class.isAssignableFrom(fieldType)
                || double.class.isAssignableFrom(fieldType)
                || long.class.isAssignableFrom(fieldType)
                || int.class.isAssignableFrom(fieldType);
    }

    private void cacheMeasurementClass(@Nonnull final Class<?>... measurementTypes) {

        for (Class<?> measurementType : measurementTypes) {
            if (CLASS_FIELD_CACHE.containsKey(measurementType.getName())) {
                continue;
            }
            ConcurrentMap<String, Field> initialMap = new ConcurrentHashMap<>();
            ConcurrentMap<String, Field> influxColumnAndFieldMap = CLASS_FIELD_CACHE
                    .putIfAbsent(measurementType.getName(), initialMap);
            if (influxColumnAndFieldMap == null) {
                influxColumnAndFieldMap = initialMap;
            }

            for (Field field : measurementType.getDeclaredFields()) {
                Column colAnnotation = field.getAnnotation(Column.class);
                if (colAnnotation != null) {
                    String name = colAnnotation.name();
                    if (name.isEmpty()) {
                        name = field.getName();
                    }
                    influxColumnAndFieldMap.put(name, field);
                }
            }
        }
    }
}