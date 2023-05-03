# flux-dsl

## Features

- [Function properties](#function-properties)
- [Supported functions](#supported-functions)
- [Using Imports](#using-imports)
- [Add missing functions](#add-missing-functions)
- [Custom functions](#custom-functions)
- [Time zones](#time-zones)

## Function properties
There are four possibilities how to add properties to the functions.

### Use built-in constructor
```java
Flux flux = Flux
    .from("telegraf")
    .window(15L, ChronoUnit.MINUTES, 20L, ChronoUnit.SECONDS)
    .sum();
```
### Use built-in properties
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

String query = flux.toString(properties);
```

## Supported functions

### from

Starting point for all queries. Get data from the specified database [[doc](http://bit.ly/flux-spec#from)].
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

### arrayFrom

Constructs a table from an array of records.
- `rows` - Array of records to construct a table with. [array of records]

```java
Map<String, Object> record1 = new HashMap<>();
record1.put("foo",  "bar");
record1.put("baz",  21.2);

Flux flux = Flux.arrayFrom(record1, record1);
```

### custom expressions
```java
Flux flux = Flux.from("telegraf")
    .expression("map(fn: (r) => r._value * r._value)")
    .expression("sum()");
```   

### aggregateWindow
Applies an aggregate or selector function (any function with a column parameter) to fixed windows of time [[doc](http://bit.ly/flux-spec#aggregateWindow)].
- `every` - The duration of windows. [duration]
- `fn` - The aggregate function used in the operation. [function] 
- `column` - The column on which to operate. Defaults to `_value`. [string]
- `timeSrc` - The time column from which time is copied for the aggregate record. Defaults to `_stop`. [string]
- `timeDst` - The “time destination” column to which time is copied for the aggregate record. Defaults to `_time`. [string]
- `createEmpty` - For windows without data, this will create an empty window and fill it with a `null` aggregate value. Defaults to `true`. [boolean]

```java
Flux flux = Flux
    .from("telegraf")
    .aggregateWindow(10L, ChronoUnit.SECONDS, "mean");
```                                                   

```java  
Flux flux = Flux
    .from("telegraf")
        .aggregateWindow()
            .withEvery("10s")
            .withAggregateFunction("sum")
            .withColumn("_value")
            .withTimeSrc("_stop")
            .withTimeDst("_time")
            .withCreateEmpty(true);
```                                

```java 
Flux flux = Flux
    .from("telegraf")
        .aggregateWindow()
            .withEvery(5L, ChronoUnit.MINUTES)
            .withFunction("tables |> quantile(q: 0.99, column:column)");
```

### columns
The columns() function lists the column labels of input tables [[doc](http://bit.ly/flux-spec#columns)].
- `column` - The name of the output column in which to store the column labels. Defaults to `_value`. [string]

```java
Flux flux = Flux
    .from("telegraf")
    .range(-12L, ChronoUnit.HOURS)
    .window(10L, ChronoUnit.MINUTES)
    .columns();
```

### count
Counts the number of results [[doc](http://bit.ly/flux-spec#count)].
- `column` - The column to aggregate. Defaults to `_value`. [string]
```java
Flux flux = Flux
    .from("telegraf")
    .count();
```

### covariance
Covariance is an aggregate operation. Covariance computes the covariance between two columns [[doc](http://bit.ly/flux-spec#covariance)].
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

### cumulativeSum
Cumulative sum computes a running sum for non null records in the table. 
The output table schema will be the same as the input table [[doc](http://bit.ly/flux-spec#cumulative-sum)].
- `columns` - a list of columns on which to operate [array of strings]
```java
Flux flux = Flux
    .from("telegraf")
    .cumulativeSum(new String[]{"_value"});
```

### derivative
Computes the time based difference between subsequent non null records [[doc](http://bit.ly/flux-spec#derivative)].
- `unit` - The time duration to use for the result. [duration]
- `nonNegative` - Indicates if the derivative is allowed to be negative. [boolean]
- `columns` - List of columns on which to compute the derivative. [array of strings]
- `timeColumn` - The source column for the time values. Defaults to `_time`. [string]

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
        .withTimeColumn("_timeColumn");
```

### difference
Difference computes the difference between subsequent non null records [[doc](http://bit.ly/flux-spec#difference)].
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
Distinct produces the unique values for a given column [[doc](http://bit.ly/flux-spec#distinct)].
- `column` - The column on which to track unique values. [string]
```java
Flux flux = Flux
    .from("telegraf")
    .groupBy("_measurement")
    .distinct("_measurement");
```

### drop
Drop will exclude specified columns from a table. Columns to exclude can be specified either through a list, or a predicate function. 
When a dropped column is part of the group key it will also be dropped from the key [[doc](http://bit.ly/flux-spec#drop)].
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
        .withFunction("column =~ /free*/");
```

### duplicate
Duplicate will duplicate a specified column in a table [[doc](http://bit.ly/flux-spec#duplicate)].
- `column` - The column to duplicate. [string]
- `as` The name that should be assigned to the duplicate column. [string]

```java
Flux flux = Flux
    .from("telegraf")
    .duplicate("host", "server");
```

### fill

Replaces all null values in input tables with a non-null value [[doc](http://bit.ly/flux-spec#fill)].
- `column` - The column to fill. Defaults to `_value`. [string]
- `value` The constant value to use in place of nulls. The type must match the type of the valueColumn. [object]
- `usePrevious` If set, then assign the value set in the previous non-null row. Cannot be used with value. [boolean]

```java
Flux flux = Flux
    .from("telegraf")
    .fill(0.0);
```

### filter

Filters the results using an expression [[doc](http://bit.ly/flux-spec#filter)].
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
- `not`
- `notEqual`
- `less`
- `greater`
- `greater`
- `exists`
- `lessOrEqual`
- `greaterOrEqual`
- `contains`
- `custom` - the custom restriction by `Restrictions.value().custom(15L, "=~")`

```java
Restrictions restrictions = Restrictions.and(
    Restrictions.measurement().equal("mem"),
    Restrictions.field().equal("usage_system"),
    Restrictions.value().exists(),
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
Returns the first result of the query [[doc](http://bit.ly/flux-spec#first)].

```java
Flux flux = Flux
    .from("telegraf")
    .first();
```

### group
Groups results by a user-specified set of tags [[doc](http://bit.ly/flux-spec#group)].
- `columns` - List of columns used to calculate the new group key. Default `[]`. [array of strings]
- `mode` - The grouping mode, can be one of `by` or `except`. The default is `by`.
    - `by` - the specified columns are the new group key [string]
    - `except` - the new group key is the difference between the columns of the table under exam and `columns`. [string]

```java
Flux.from("telegraf")
    .range(-30L, ChronoUnit.MINUTES)
    .groupBy(new String[]{"tag_a", "tag_b"});

Flux.from("telegraf")
    .range(-30L, ChronoUnit.MINUTES)
    .groupBy("tag_a"});
```
```java
// except mode
Flux.from("telegraf")
    .range(-30L, ChronoUnit.MINUTES)
    .groupExcept(new String[]{"tag_c"});
```

### integral
For each aggregate column, it outputs the area under the curve of non null records. 
The curve is defined as function where the domain is the record times and the range is the record values. [[doc](http://bit.ly/flux-spec#integral)].
- `unit` - Time duration to use when computing the integral. [duration]
```java
Flux flux = Flux
    .from("telegraf")
    .integral(1L, ChronoUnit.MINUTES);
```

### interpolateLinear
the `interpolate.linear` function inserts rows at regular intervals using linear interpolation to determine values for inserted rows.
- `duration` - Time duration to use when computing the interpolation. [duration]
```java
Flux flux = Flux
    .from("telegraf")
    .interpolateLinear(1L, ChronoUnit.MINUTES);
```

### join
Join two time series together on time and the list of `on` keys [[doc](http://bit.ly/flux-spec#join)].
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
Only columns in the group key that are also specified in `keep` will be kept in the resulting group key [[doc](http://bit.ly/flux-spec#keep)].
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
        .withFunction("column =~ /inodes*/");
```

### last
Returns the last result of the query [[doc](http://bit.ly/flux-spec#last)].
- `column` - The column used to verify the existence of a value. Defaults to `_value`. [string]

```java
Flux flux = Flux
    .from("telegraf")
    .last();
```

### limit
Restricts the number of rows returned in the results [[doc](http://bit.ly/flux-spec#limit)].
- `n` - The maximum number of records to output. [int] 
- `offset` - The number of records to skip per table. Default to `0`. [int]
```java
Flux flux = Flux
    .from("telegraf")
    .limit(5);
```    
```java
Flux flux = Flux
    .from("telegraf")
    .limit(100, 10);
```
### map
Applies a function to each row of the table [[doc](http://bit.ly/flux-spec#map)].
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
Returns the max value within the results [[doc](http://bit.ly/flux-spec#max)].
- `column` - The column to use to calculate the maximum value. Defaults to `_value`. [string]

```java
Flux flux = Flux
    .from("telegraf")
    .range(-12L, ChronoUnit.HOURS)
    .window(10L, ChronoUnit.MINUTES)
    .max();
```

### mean
Returns the mean of the values within the results [[doc](http://bit.ly/flux-spec#mean)].
- `column` - The column to use to compute the mean. Defaults to `_value`. [string]

```java
Flux flux = Flux
    .from("telegraf")
    .range(-12L, ChronoUnit.HOURS)
    .window(10L, ChronoUnit.MINUTES)
    .mean();
```

### min
Returns the min value within the results [[doc](http://bit.ly/flux-spec#min)].
- `column` - The column to use to calculate the minimum value. Defaults to `_value`. [string]

```java
Flux flux = Flux
    .from("telegraf")
    .range(-12L, ChronoUnit.HOURS)
    .window(10L, ChronoUnit.MINUTES)
    .min();
```

### quantile
quantile is both an aggregate operation and a selector operation depending on selected options. 
In the aggregate methods, it outputs the value that represents the specified quantile of the non null record as a float [[doc](http://bit.ly/flux-spec#quantile-aggregate)].
- `column` - specifies a column to aggregate. Defaults to `_value`. [array of strings]
- `quantile` - value between 0 and 1 indicating the desired quantile. [float]
- `method` - quantile provides 3 methods for computation:
    - `estimate_tdigest` - an aggregate result that uses a tdigest data structure to compute an accurate quantile estimate on large data sources.
    - `exact_mean` - an aggregate result that takes the average of the two points closest to the quantile value.
    - `exact_selector` - Quantile
- `compression` - Compression indicates how many centroids to use when compressing the dataset. A larger number produces a more accurate result at the cost of increased memory requirements. Defaults to `1000`. [float]
```java
Flux flux = Flux
    .from("telegraf")
    .quantile(0.80F);

Flux flux = Flux
    .from("telegraf")
    .quantile()
        .withColumn("value2")
        .withQuantile(0.75F)
        .withMethod(MethodType.EXACT_MEAN)
        .withCompression(2_000F);
```

### pivot
Pivot collects values stored vertically (column-wise) in a table 
and aligns them horizontally (row-wise) into logical sets [[doc](http://bit.ly/flux-spec#pivot)].
- `rowKey` - List of columns used to uniquely identify a row for the output. [array of strings]
- `columnKey` - List of columns used to pivot values onto each row identified by the rowKey. [array of strings]
- `valueColumn` - Identifies the single column that contains the value to be moved around the pivot [string]

```java
Flux flux = Flux.from("telegraf")
    .pivot()
        .withRowKey(new String[]{"_time"})
        .withColumnKey(new String[]{"_field"})
        .withValueColumn("_value");
```

### range
Filters the results by time boundaries [[doc](http://bit.ly/flux-spec#range)].
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

### reduce
Reduce aggregates records in each table according to the reducer `fn`. 
The output for each table will be the group key of the table, plus columns corresponding to each field in the reducer object [[doc](http://bit.ly/flux-spec#reduce)].

If the reducer record contains a column with the same name as a group key column, 
then the group key column's value is overwritten, and the outgoing group key is changed. 
However, if two reduced tables write to the same destination group key, then the function will error.

- `fn` - Function to apply to each record with a reducer object of type 'a. [(r: record, accumulator: 'a) -> 'a]
- `identity` - an initial value to use when creating a reducer ['a]

```java
Flux flux = Flux
    .from("telegraf")
    .range(-12L, ChronoUnit.HOURS)
    .reduce("{ sum: r._value + accumulator.sum }", "{sum: 0.0}");    
``` 

```java
Flux flux = Flux
    .from("telegraf")
    .range(-12L, ChronoUnit.HOURS)
    .reduce()
        .withFunction("{sum: r._value + accumulator.sum,\ncount: accumulator.count + 1.0}")
        .withIdentity("{sum: 0.0, count: 0.0}");
```

### rename
Rename will rename specified columns in a table. If a column is renamed and is part of the group key, the column name in the group key will be updated [[doc](http://bit.ly/flux-spec#range)].
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
    .rename("\"{col}_new\"");
```

### sample
Sample values from a table [[doc](http://bit.ly/flux-spec#sample)].
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
Assigns a static value to each record [[doc](http://bit.ly/flux-spec#set)].
- `key` - Label for the column to set. [string]
- `value` - Value for the column to set. [string]

```java
Flux flux = Flux
    .from("telegraf")
    .set("location", "Carolina");
```

### timeShift
Shift add a fixed duration to time columns [[doc](http://bit.ly/flux-spec#shift)].
- `duration` - The amount to add to each time value. [duration]
- `columns` - The list of all columns that should be shifted. Defaults `["_start", "_stop", "_time"]`. [array of strings]
```java
Flux flux = Flux
    .from("telegraf")
    .timeShift(10L, ChronoUnit.HOURS);
```
```java
Flux flux = Flux
    .from("telegraf")
    .timeShift(10L, ChronoUnit.HOURS, new String[]{"_time", "custom"});
```

### truncateTimeColumn
Truncates all input time values in the _time to a specified unit. [[doc](http://bit.ly/flux-spec#truncateTimeColumn)].
- `unit` - Unit of time to truncate to. Has to be defined. [duration]
```java
Flux flux = Flux
    .from("telegraf")
    .truncateTimeColumn(ChronoUnit.SECONDS);
```


### skew
Skew of the results [[doc](http://bit.ly/flux-spec#skew)].
- `column` - The column on which to operate. Defaults to `_value`. [string]

```java
Flux flux = Flux
    .from("telegraf")
    .range(-30L, -15L, ChronoUnit.MINUTES)
    .skew();
```

### sort
Sorts the results by the specified columns. Default sort is ascending [[doc](http://bit.ly/flux-spec#skew)].
- `columns` - List of columns used to sort. Precedence from left to right. Default is `"value"`. [array of strings]
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
Difference between min and max values [[doc](http://bit.ly/flux-spec#spread)].
- `column` - The column on which to operate. Defaults to `_value`. [string]

```java
Flux flux = Flux
    .from("telegraf")
    .spread();
```

### stddev
Standard Deviation of the results [[doc](http://bit.ly/flux-spec#stddev)].
- `column` - The column on which to operate. Defaults to `_value`. [string]

```java
Flux flux = Flux
    .from("telegraf")
    .stddev();
```

### sum
Sum of the results [[doc](http://bit.ly/flux-spec#sum)].
- `column` - The column on which to operate. Defaults to `_value`. [string]

```java
Flux flux = Flux
    .from("telegraf")
    .sum();
```

### tail
Tail caps the number of records in output tables to a fixed size [[doc](http://bit.ly/flux-spec#tail)].
- `n` - The maximum number of records to output. [int]
- `offset` - The number of records to skip per table. Default to `0`. [int]
```java
Flux flux = Flux
    .from("telegraf")
    .tail(5);
```    
```java
Flux flux = Flux
    .from("telegraf")
    .tail(100, 10);
```

### to
The To operation takes data from a stream and writes it to a bucket [[doc](http://bit.ly/flux-spec#to)].
- `bucket` - The bucket to which data will be written. [string]
- `bucketID` - The ID of the bucket to which data will be written. [string]
- `org` - The organization name of the above bucket. [string]
- `orgID` - The organization ID of the above bucket. [string]
- `host` - The remote host to write to. [string]
- `token` - The authorization token to use when writing to a remote host. [string]
- `timeColumn` - The time column of the output. [string]
- `tagColumns` - The tag columns of the output. **Default:** All columns of type string, excluding all value columns and the `_field` column if present. [array of strings]
- `fieldFn` - Function that takes a record from the input table and returns an object. For each record from the input table fieldFn returns on object that maps output field key to output value. **Default:** `(r) => ({ [r._field]: r._value })` [function(record) object]
```java
Flux flux = Flux
    .from("telegraf")
    .to("my-bucket", "my-org");
```

### toBool
Convert a value to a bool [[doc](http://bit.ly/flux-spec#tobool)].
```java
Flux flux = Flux
    .from("telegraf")
    .filter(and(measurement().equal("mem"), field().equal("used")))
    .toBool();
```

### toInt
Convert a value to a int [[doc](http://bit.ly/flux-spec#toint)].
```java
Flux flux = Flux
    .from("telegraf")
    .filter(and(measurement().equal("mem"), field().equal("used")))
    .toInt();
```

### toFloat
Convert a value to a float [[doc](http://bit.ly/flux-spec#tofloat)].
```java
Flux flux = Flux
    .from("telegraf")
    .filter(and(measurement().equal("mem"), field().equal("used")))
    .toFloat();
```

### toDuration
Convert a value to a duration [[doc](http://bit.ly/flux-spec#toduration)].
```java
Flux flux = Flux
    .from("telegraf")
    .filter(and(measurement().equal("mem"), field().equal("used")))
    .toDuration();
```

### toString
Convert a value to a string [[doc](http://bit.ly/flux-spec#tostring)].
```java
Flux flux = Flux
    .from("telegraf")
    .filter(and(measurement().equal("mem"), field().equal("used")))
    .toString();
```

### toTime
Convert a value to a time [[doc](http://bit.ly/flux-spec#totime)].
```java
Flux flux = Flux
    .from("telegraf")
    .filter(and(measurement().equal("mem"), field().equal("used")))
    .toTime();
```

### toUInt
Convert a value to a uint [[doc](http://bit.ly/flux-spec#touint)].
```java
Flux flux = Flux
    .from("telegraf")
    .filter(and(measurement().equal("mem"), field().equal("used")))
    .toUInt();
```

### union

Merges two or more input streams into a single output stream [[doc](http://bit.ly/flux-spec#union)]. 
- `tables` - the tables to union. [array of flux]

```java
Flux flux = Flux.union(
    Flux.from("telegraf1"),
    Flux.from("telegraf2")
);
```

```java
Flux v1 = Flux.from("telegraf1").asVariable("v1");
Flux v2 = Flux.from("telegraf1").asVariable("v2");
Flux flux = Flux.union(v1, v2);
```

### window
Groups the results by a given time range [[doc](http://bit.ly/flux-spec#window)].
- `every` - Duration of time between windows. Defaults to `period's` value. [duration] 
- `period` - Duration of the windowed partition. Defaults to `period's` value. [duration] 
- `offset` - The offset duration relative to the location offset. It can be negative, indicating that the offset goes backwards in time. The default aligns the window boundaries to line up with the `now` option time. [time]
- `timeColumn` - Name of the time column to use. Defaults to `_time`. [string]
- `startColumn` - Name of the column containing the window start time. Defaults to `_start`. [string]
- `stopColumn` - Name of the column containing the window stop time. Defaults to `_stop`. [string]

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
Yield a query results to yielded results [[doc](http://bit.ly/flux-spec#yield)].
- `name` - The unique name to give to yielded results. [string]
```java
Flux flux = Flux
    .from("telegraf")
    .yield("0");
```

## Using Imports

Each Flux-Class can add required imports via the `addImport` method.
For an example take a look at the [custom functions section](#custom-functions) below.

## Add missing functions

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

## Custom functions

### Custom function with a flux implementation

```java
public static class MyCustomFunction extends AbstractFunctionFlux<MyCustomFunction.MyCustomFunctionCall> {

    public MyCustomFunction() {
        super("from2",
                new FromFlux()
                        .withPropertyValue("bucket", "n"),
                MyCustomFunctionCall::new,
                new Parameter("n"));
        addImport("foo");
    }

    public static class MyCustomFunctionCall extends AbstractFunctionCallFlux {

        public MyCustomFunctionCall(@Nonnull String name) {
            super(name);
        }

        public MyCustomFunctionCall withN(final String n) {
            this.withPropertyValueEscaped("n", n);
            return this;
        }
    }
}
```

usage:

```java
MyCustomFunction fun = new MyCustomFunction();

Expressions flux = new Expressions(
    fun,
    fun.invoke().withN(0)
);
flux.toString()
```

result:

```javascript
import "foo"
from2 = (n) => from(bucket: n)
from2(n:"telegraf")
```

### Defining a custom function with a freestyle expression

```java
public static class MultByXFunction extends AbstractFunctionFlux<MultByXFunction.MultByXFunctionCall> {

  public MultByXFunction() {
    super("multByX",
            new FreestyleExpression("tables")
                    .map("(r) => ({r with _value: r._value * x})"),
            MultByXFunctionCall::new,
            new Parameter("tables").withPipeForward(true),
            new Parameter("x"));
  }

  public static class MultByXFunctionCall extends AbstractFunctionCallFlux {

    public MultByXFunctionCall(@Nonnull String name) {
      super(name);
    }

    @Nonnull
    public MultByXFunctionCall withX(final Number x) {
      this.withPropertyValue("x", x);
      return this;
    }
  }
}
```

usage:

```java
MultByXFunction multByX = new MultByXFunction();
Expressions flux = new Expressions(
    multByX,
    Flux.from("telegraph")
        .withPipedFunction(multByX)
        .withX(42.)
        .count()
);
flux.toString()
```

result:

```javascript
multByX = (tables=<-, x) => ()
	|> map(fn: (r) => (r) => ({r with _value: r._value * x}))
from(bucket:"telegraph")
	|> multByX(x:42.0)
	|> count()
```

## Time zones

The Flux option sets the default time zone of all times in the script. The default value is `timezone.utc`.

You can construct a timezone with a fixed offset:

```java
Flux flux = Flux
    .from("telegraf")
    .withLocationFixed("-8h")
    .count();
```

The Flux script:
```
import "timezone"

option location = timezone.fixed(offset: -8h)

from(bucket:"telegraf")
    |> count()
```

or you can construct a timezone based on a location name:

```java
Flux flux = Flux
    .from("telegraf")
    .withLocationNamed("America/Los_Angeles")
    .count();
```

The Flux script:
```
import "timezone"

option location = timezone.location(name: "America/Los_Angeles")

from(bucket:"telegraf")
    |> count()
```

## Version

The latest version for Maven dependency:
```xml
<dependency>
  <groupId>com.influxdb</groupId>
  <artifactId>flux-dsl</artifactId>
  <version>6.8.0</version>
</dependency>
```

Or when using with Gradle:
```groovy
dependencies {
    implementation "com.influxdb:flux-dsl:6.8.0"
}
```

### Snapshot Repository
The snapshots are deployed into [OSS Snapshot repository](https://oss.sonatype.org/content/repositories/snapshots/).

#### Maven
```xml
<repository>
    <id>ossrh</id>
    <name>OSS Snapshot repository</name>
    <url>https://oss.sonatype.org/content/repositories/snapshots/</url>
    <releases>
        <enabled>false</enabled>
    </releases>
    <snapshots>
        <enabled>true</enabled>
    </snapshots>
</repository>
```
#### Gradle
```
repositories {
    maven { url "https://oss.sonatype.org/content/repositories/snapshots" }
}
```
