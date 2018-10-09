# Build-in Flux operators

## Operator properties
There are four possibilities how to add properties to the functions.

### Use build-in constructor
```java
Flux flux = Flux
    .from("telegraf")
    .window(15L, ChronoUnit.MINUTES, 20L, ChronoUnit.SECONDS)
    .sum();
```
### Use build-in properties
```java
Flux.from("telegraf")
    .window()
        .withEvery(15L, ChronoUnit.MINUTES)
        .withPeriod(20L, ChronoUnit.SECONDS)
    .sum();
```

### Specify the property name and value
```java
Flux.from("telegraf")
    .window()
        .withPropertyValue("every", 15L, ChronoUnit.MINUTES)
        .withPropertyValue("period", 20L, ChronoUnit.SECONDS)
    .sum();
```
### Specify the named properties
```java
Map<String, Object> properties = new HashMap<>();
properties.put("every", new TimeInterval(15L, ChronoUnit.MINUTES));
properties.put("period", new TimeInterval(20L, ChronoUnit.SECONDS));

Flux flux = Flux
    .from("telegraf")
    .window()
        .withPropertyNamed("every")
        .withPropertyNamed("period")
    .sum();
```

## Supported operators

### from

Starting point for all queries. Get data from the specified database [[doc](https://github.com/influxdata/platform/tree/master/query#from)].
- `bucket` - The name of the bucket to query. [string]
- `hosts` - [array of strings]

```java
Flux flux = Flux.from("telegraf");
```
```java
Flux flux = Flux
    .from("telegraf", new String[]{"192.168.1.200", "192.168.1.100"})
    .last();
```

### custom expressions
```java
Flux flux = Flux.from("telegraf")
    .expression("map(fn: (r) => r._value * r._value)")
    .expression("sum()");
```

### count
Counts the number of results [[doc](https://github.com/influxdata/platform/tree/master/query#count)].
- `useStartTime` - Use the start time as the timestamp of the resulting aggregate. [boolean]
```java
Flux flux = Flux
    .from("telegraf")
    .count();
```

### covariance
Covariance is an aggregate operation. Covariance computes the covariance between two columns [[doc](https://github.com/influxdata/platform/blob/master/query/docs/SPEC.md#covariance)].
- `columns` - List of columns on which to compute the covariance. Exactly two columns must be provided. [array of strings]
- `pearsonr` -  Indicates whether the result should be normalized to be the [Pearson R coefficient](https://en.wikipedia.org/wiki/Pearson_correlation_coefficient). [boolean]
- `valueDst` - The column into which the result will be placed. Defaults to `_value`. [string]
```java
Flux flux = Flux
    .from("telegraf")
    .covariance(new String[]{"_value", "_valueSquare"});
```
```java
Flux flux = Flux
    .from("telegraf")
    .covariance()
        .withColumns(new String[]{"columnA", "columnB"})
        .withPearsonr(true)
        .withValueDst("_newColumn");
```

### derivative
Computes the time based difference between subsequent non null records [[doc](https://github.com/influxdata/platform/blob/master/query/docs/SPEC.md#derivative)].
- `unit` - The time duration to use for the result. [duration]
- `nonNegative` - Indicates if the derivative is allowed to be negative. [boolean]
- `columns` - List of columns on which to compute the derivative. [array of strings]
- `timeSrc` - The source column for the time values. Defaults to `_time`. [string]

```java
Flux flux = Flux
    .from("telegraf")
    .derivative(1L, ChronoUnit.MINUTES);
```

```java
Flux flux = Flux
    .from("telegraf")
    .derivative()
        .withUnit(10L, ChronoUnit.DAYS)
        .withNonNegative(true)
        .withColumns(new String[]{"columnCompare_1", "columnCompare_2"})
        .withTimeSrc("_timeColumn");
```

### difference
Difference computes the difference between subsequent non null records [[doc](https://github.com/influxdata/platform/blob/master/query/docs/SPEC.md#difference)].
- `nonNegative` - Indicates if the derivative is allowed to be negative. If a value is encountered which is less than the previous value then it is assumed the previous value should have been a zero. [boolean]
- `columns` -  The list of columns on which to compute the difference. Defaults `["_value"]`. [array of strings]
```java
Flux flux = Flux
    .from("telegraf")
    .groupBy("_measurement")
    .difference();
```
```java
Flux flux = Flux
    .from("telegraf")
    .range(-5L, ChronoUnit.MINUTES)
    .difference(new String[]{"_value", "_time"}, false);
```

### distinct
Distinct produces the unique values for a given column [[doc](https://github.com/influxdata/platform/blob/master/query/docs/SPEC.md#distinct)].
- `column` - The column on which to track unique values. [string]
```java
Flux flux = Flux
    .from("telegraf")
    .groupBy("_measurement")
    .distinct("_measurement");
```

### drop
Drop will exclude specified columns from a table. Columns to exclude can be specified either through a list, or a predicate function. 
When a dropped column is part of the group key it will also be dropped from the key [[doc](https://github.com/influxdata/platform/blob/master/query/docs/SPEC.md#drop)].
- `columns` -  The list of columns which should be excluded from the resulting table. Cannot be used with `fn`. [array of strings]
- `fn` - The function which takes a column name as a parameter and returns a boolean indicating whether or not the column should be excluded from the resulting table. Cannot be used with `columns`. [function(column)]

```java
Flux flux = Flux
    .from("telegraf")
    .drop(new String[]{"host", "_measurement"});
```

```java
Flux flux = Flux
    .from("telegraf")
    .drop()
        .withFunction("col =~ /free*/");
```
### filter

Filters the results using an expression [[doc](https://github.com/influxdata/platform/tree/master/query#filter)].
- `fn` - Function to when filtering the records. The function must accept a single parameter which will be 
the records and return a boolean value. Records which evaluate to true, will be included in the results. [function(record) bool]

Supported Record columns:
- `_measurement`
- `_field`
- `_start`
- `_stop`
- `_time`
- `_value`
- `custom` - the custom column value by `Restrictions.column("_id").notEqual(5)`

Supported Record restrictions:
- `equal`
- `notEqual`
- `less`
- `greater`
- `greater`
- `lessOrEqual`
- `greaterOrEqual`
- `custom` - the custom restriction by `Restrictions.value().custom(15L, "=~")`

```java
Restrictions restrictions = Restrictions.and(
    Restrictions.measurement().equal("mem"),
    Restrictions.field().equal("usage_system"),
    Restrictions.tag("service").equal("app-server")
);

Flux flux = Flux
    .from("telegraf")
    .filter(restrictions)
    .range(-4L, ChronoUnit.HOURS)
    .count();
```
```java
Restrictions restriction = Restrictions.and(
    Restrictions.tag("instance_type").equal(Pattern.compile("/prod/")),
    Restrictions.field().greater(10.5D),
    Restrictions.time().lessOrEqual(new TimeInterval(-15L, ChronoUnit.HOURS))
);

Flux flux = Flux
    .from("telegraf")
    .filter(restriction)
    .range(-4L, 2L, ChronoUnit.HOURS)
    .count();
```

### first
Returns the first result of the query [[doc](https://github.com/influxdata/platform/tree/master/query#first)].
- `useStartTime` - Use the start time as the timestamp of the resulting aggregate. [boolean]

```java
Flux flux = Flux
    .from("telegraf")
    .first();
```

### group
Groups results by a user-specified set of tags [[doc](https://github.com/influxdata/platform/tree/master/query#group)].
- `by` - Group by these specific tag names. Cannot be used with `except` option. [array of strings]
- `keep` - Keep specific tag keys that were not in `by` in the results. [array of strings]
- `except` - Group by all but these tag keys. Cannot be used with `by` option. [array of strings]

```java
Flux.from("telegraf")
    .range(-30L, ChronoUnit.MINUTES)
    .groupBy(new String[]{"tag_a", "tag_b"});
```
```java
// by + keep
Flux.from("telegraf")
    .range(-30L, ChronoUnit.MINUTES)
    .groupBy(new String[]{"tag_a", "tag_b"}, new String[]{"tag_c"});
```
```java
// except + keep
Flux.from("telegraf")
    .range(-30L, ChronoUnit.MINUTES)
    .groupExcept(new String[]{"tag_a"}, new String[]{"tag_b", "tag_c"});
```

### integral
For each aggregate column, it outputs the area under the curve of non null records. 
The curve is defined as function where the domain is the record times and the range is the record values. [[doc](https://github.com/influxdata/platform/blob/master/query/docs/SPEC.md#integral)].
- `unit` - Time duration to use when computing the integral. [duration]
```java
Flux flux = Flux
    .from("telegraf")
    .integral(1L, ChronoUnit.MINUTES);
```

### join
Join two time series together on time and the list of `on` keys [[doc](https://github.com/influxdata/platform/tree/master/query#join)].
- `tables` - Map of tables to join. Currently only two tables are allowed. [map of tables]
- `on` - List of tag keys that when equal produces a result set. [array of strings]
- `method` - An optional parameter that specifies the type of join to be performed. When not specified, an inner join is performed. [string] 

The method parameter may take on any one of the following values:
- `inner` - inner join
- `cross` - cross product
- `left` - left outer join
- `right` - right outer join
- `outer` - full outer join

The `on` parameter and the `cross` method are mutually exclusive.

```java
Flux cpu = Flux.from("telegraf")
    .filter(Restrictions.and(Restrictions.measurement().equal("cpu"), Restrictions.field().equal("usage_user")))
    .range(-30L, ChronoUnit.MINUTES);

Flux mem = Flux.from("telegraf")
    .filter(Restrictions.and(Restrictions.measurement().equal("mem"), Restrictions.field().equal("used_percent")))
    .range(-30L, ChronoUnit.MINUTES);

Flux flux = Flux.join()
    .withTable("cpu", cpu)
    .withTable("mem", mem)
    .withOn("host");
```

### keep
Keep is the inverse of drop. It will return a table containing only columns that are specified, ignoring all others. 
Only columns in the group key that are also specified in `keep` will be kept in the resulting group key [[doc](https://github.com/influxdata/platform/blob/master/query/docs/SPEC.md#keep)].
- `columns` -  The list of columns that should be included in the resulting table. Cannot be used with `fn`. [array of strings]
- `fn` - The function which takes a column name as a parameter and returns a boolean indicating whether or not the column should be included in the resulting table. Cannot be used with `columns`. [function(column)]

```java
Flux flux = Flux
    .from("telegraf")
    .keep(new String[]{"_time", "_value"});
```

```java
Flux flux = Flux
    .from("telegraf")
    .keep()
        .withFunction("col =~ /inodes*/");
```

### last
Returns the last result of the query [[doc](https://github.com/influxdata/platform/tree/master/query#last)].
- `useStartTime` - Use the start time as the timestamp of the resulting aggregate. [boolean]

```java
Flux flux = Flux
    .from("telegraf")
    .last();
```

### limit
Restricts the number of rows returned in the results [[doc](https://github.com/influxdata/platform/tree/master/query#limit)].
- `n` - The maximum number of records to output. [int] 
```java
Flux flux = Flux
    .from("telegraf")
    .limit(5);
```
### map
Applies a function to each row of the table [[doc](https://github.com/influxdata/platform/tree/master/query#map)].
- `fn` - The function to apply to each row. The return value of the function may be a single value or an object.

```java
// Square the value

Restrictions restriction = Restrictions.and(
    Restrictions.measurement().equal("cpu"),
    Restrictions.field().equal("usage_system"),
    Restrictions.tag("service").equal("app-server")
);

Flux flux = Flux
    .from("telegraf")
    .filter(restriction)
    .range(-12L, ChronoUnit.HOURS)
    .map("r._value * r._value");
```

```java
// Square the value and keep the original value

Restrictions restriction = Restrictions.and(
    Restrictions.measurement().equal("cpu"),
    Restrictions.field().equal("usage_system"),
    Restrictions.tag("service").equal("app-server")
);

Flux flux = Flux
    .from("telegraf")
    .filter(restriction)
    .range(-12L, ChronoUnit.HOURS)
    .map("{value: r._value, value2:r._value * r._value}");
```

### max
Returns the max value within the results [[doc](https://github.com/influxdata/platform/tree/master/query#max)].
- `useStartTime` - Use the start time as the timestamp of the resulting aggregate. [boolean]

```java
Flux flux = Flux
    .from("telegraf")
    .range(-12L, ChronoUnit.HOURS)
    .window(10L, ChronoUnit.MINUTES)
    .max();
```

### mean
Returns the mean of the values within the results [[doc](https://github.com/influxdata/platform/tree/master/query#mean)].
- `useStartTime` - Use the start time as the timestamp of the resulting aggregate. [boolean]

```java
Flux flux = Flux
    .from("telegraf")
    .range(-12L, ChronoUnit.HOURS)
    .window(10L, ChronoUnit.MINUTES)
    .mean();
```

### min
Returns the min value within the results [[doc](https://github.com/influxdata/platform/tree/master/query#min)].
- `useStartTime` - Use the start time as the timestamp of the resulting aggregate. [boolean]

```java
Flux flux = Flux
    .from("telegraf")
    .range(-12L, ChronoUnit.HOURS)
    .window(10L, ChronoUnit.MINUTES)
    .min();
```

### range
Filters the results by time boundaries [[doc](https://github.com/influxdata/platform/tree/master/query#range)].
- `start` - Specifies the oldest time to be included in the results. [duration or timestamp]
- `stop` - Specifies the exclusive newest time to be included in the results. Defaults to `"now"`. [duration or timestamp]

```java
// by interval
Flux flux = Flux
    .from("telegraf")
    .range(-12L, -1L, ChronoUnit.HOURS)
```
```java
// by Instant
Flux flux = Flux
    .from("telegraf")
    .range(Instant.now().minus(4, ChronoUnit.HOURS),
           Instant.now().minus(15, ChronoUnit.MINUTES)
    );
```

### rename
Rename will rename specified columns in a table. If a column is renamed and is part of the group key, the column name in the group key will be updated [[doc](https://github.com/influxdata/platform/blob/master/query/docs/SPEC.md#range)].
- `columns` -  The map of columns to rename and their corresponding new names. Cannot be used with `fn`. [map of columns]
- `fn` - The function which takes a single string parameter (the old column name) and returns a string representing the new column name. Cannot be used with `columns`. [function(column)]

```java
Map<String, String> map = new HashMap<>();
map.put("host", "server");
map.put("_value", "val");

Flux flux = Flux
    .from("telegraf")
    .rename(map);
```

```java
Flux flux = Flux
    .from("telegraf")
    .rename("{col}_new");
```

### sample
Sample values from a table [[doc](https://github.com/influxdata/platform/tree/master/query#sample)].
- `n` - Sample every Nth element. [int]
- `pos` - Position offset from start of results to begin sampling. `pos` must be less than `n`. If `pos` less than 0, a random offset is used. Default is -1 (random offset). [int]
```java
Flux flux = Flux.from("telegraf")
    .filter(and(measurement().equal("cpu"), field().equal("usage_system")))
    .range(-1L, ChronoUnit.DAYS)
    .sample(10);
```
```java
Flux flux = Flux.from("telegraf")
    .filter(and(measurement().equal("cpu"), field().equal("usage_system")))
    .range(-1L, ChronoUnit.DAYS)
    .sample(5, 1);
```

### set
Assigns a static value to each record [[doc](https://github.com/influxdata/platform/tree/master/query#set)].
- `key` - Label for the column to set. [string]
- `value` - Value for the column to set. [string]

```java
Flux flux = Flux
    .from("telegraf")
    .set("location", "Carolina");
```

### shift
Shift add a fixed duration to time columns [[doc](https://github.com/influxdata/platform/blob/master/query/docs/SPEC.md#shift)].
- `shift` - The amount to add to each time value. [duration]
- `columns` - The list of all columns that should be shifted. Defaults `["_start", "_stop", "_time"]`. [array of strings]
```java
Flux flux = Flux
    .from("telegraf")
    .shift(10L, ChronoUnit.HOURS);
```
```java
Flux flux = Flux
    .from("telegraf")
    .shift(10L, ChronoUnit.HOURS, new String[]{"_time", "custom"});
```

### skew
Skew of the results [[doc](https://github.com/influxdata/platform/tree/master/query#skew)].
- `useStartTime` - Use the start time as the timestamp of the resulting aggregate. [boolean]

```java
Flux flux = Flux
    .from("telegraf")
    .range(-30L, -15L, ChronoUnit.MINUTES)
    .skew();
```

### sort
Sorts the results by the specified columns. Default sort is ascending [[doc](https://github.com/influxdata/platform/tree/master/query#skew)].
- `cols` - List of columns used to sort. Precedence from left to right. Default is `"value"`. [array of strings]
- `desc` - Sort results descending. Default false. [boolean]

```java
Flux flux = Flux
    .from("telegraf")
    .sort(new String[]{"region", "value"});
```
```java
 Flux flux = Flux
    .from("telegraf")
    .sort(true);
```

### spread
Difference between min and max values [[doc](https://github.com/influxdata/platform/tree/master/query#spread)].
- `useStartTime` - Use the start time as the timestamp of the resulting aggregate. [boolean]

```java
Flux flux = Flux
    .from("telegraf")
    .spread();
```

### stddev
Standard Deviation of the results [[doc](https://github.com/influxdata/platform/tree/master/query#stddev)].
- `useStartTime` - Use the start time as the timestamp of the resulting aggregate. [boolean]

```java
Flux flux = Flux
    .from("telegraf")
    .stddev();
```

### sum
Sum of the results [[doc](https://github.com/influxdata/platform/tree/master/query#sum)].
```java
Flux flux = Flux
    .from("telegraf")
    .sum();
```

### toBool
Convert a value to a bool [[doc](https://github.com/influxdata/platform/tree/master/query#tobool)].
```java
Flux flux = Flux
    .from("telegraf")
    .filter(and(measurement().equal("mem"), field().equal("used")))
    .toBool();
```

### toInt
Convert a value to a int [[doc](https://github.com/influxdata/platform/tree/master/query#toint)].
```java
Flux flux = Flux
    .from("telegraf")
    .filter(and(measurement().equal("mem"), field().equal("used")))
    .toInt();
```

### toFloat
Convert a value to a float [[doc](https://github.com/influxdata/platform/tree/master/query#tofloat)].
```java
Flux flux = Flux
    .from("telegraf")
    .filter(and(measurement().equal("mem"), field().equal("used")))
    .toFloat();
```

### toDuration
Convert a value to a duration [[doc](https://github.com/influxdata/platform/tree/master/query#toduration)].
```java
Flux flux = Flux
    .from("telegraf")
    .filter(and(measurement().equal("mem"), field().equal("used")))
    .toDuration();
```

### toString
Convert a value to a string [[doc](https://github.com/influxdata/platform/tree/master/query#tostring)].
```java
Flux flux = Flux
    .from("telegraf")
    .filter(and(measurement().equal("mem"), field().equal("used")))
    .toString();
```

### toTime
Convert a value to a time [[doc](https://github.com/influxdata/platform/tree/master/query#totime)].
```java
Flux flux = Flux
    .from("telegraf")
    .filter(and(measurement().equal("mem"), field().equal("used")))
    .toTime();
```

### toUInt
Convert a value to a uint [[doc](https://github.com/influxdata/platform/tree/master/query#touint)].
```java
Flux flux = Flux
    .from("telegraf")
    .filter(and(measurement().equal("mem"), field().equal("used")))
    .toUInt();
```

### window
Groups the results by a given time range [[doc](https://github.com/influxdata/platform/tree/master/query#window)].
- `every` - Duration of time between windows. Defaults to `period's` value. [duration] 
- `period` - Duration of the windowed partition. Defaults to `period's` value. [duration] 
- `offset` - The offset duration relative to the location offset. It can be negative, indicating that the offset goes backwards in time. The default aligns the window boundaries to line up with the `now` option time. [time]
- `column` - Name of the time column to use. Defaults to `_time`. [string]
- `startCol` - Name of the column containing the window start time. Defaults to `_start`. [string]
- `stopCol` - Name of the column containing the window stop time. Defaults to `_stop`. [string]

```java
Flux flux = Flux
    .from("telegraf")
    .window(15L, ChronoUnit.MINUTES)
    .max();
```
```java
Flux flux = Flux
    .from("telegraf")
    .window(15L, ChronoUnit.MINUTES,
            20L, ChronoUnit.SECONDS,
            -50L, ChronoUnit.WEEKS,
            1L, ChronoUnit.SECONDS)
    .max();
```

### yield
Yield a query results to yielded results [[doc](https://github.com/influxdata/platform/blob/master/query/docs/SPEC.md#yield)].
- `name` - The unique name to give to yielded results. [string]
```java
Flux flux = Flux
    .from("telegraf")
    .yield("0");
```

## Custom operator
We assume that exist custom function measurement that filter measurement by their name. The `Flux` implementation looks like this: 

```flux
// Define measurement function which accepts table as the piped argument.
measurement = (m, table=<-) => table |> filter(fn: (r) => r._measurement == m)
```

The Java implementation:
```java
public class FilterMeasurement extends AbstractParametrizedFlux {

    public FilterMeasurement(@Nonnull final Flux source) {
        super(source);
    }

    @Nonnull
    @Override
    protected String operatorName() {
        // name of the Flux function
        return "measurement";
    }

    /**
     * @param measurement the measurement name. Has to be defined.
     * @return this
     */
    @Nonnull
    public FilterMeasurement withName(@Nonnull final String measurement) {

        Arguments.checkNonEmpty(measurement, "Measurement name");

        // name of parameter from the Flux function
        withPropertyValueEscaped("m", measurement);

        return this;
    }
}
```

Using the measurement function:
```java
Flux flux = Flux
    .from("telegraf")
    .operator(FilterMeasurement.class)
        .withName("cpu")
    .sum();
```

The Flux script:
```
from(bucket:"telegraf")
    |> measurement(m: "cpu")
    |> sum()
```

