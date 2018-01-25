CREATE external TABLE requestinfo(
city STRING,
country STRING,
ip STRING,
appid STRING,
mobile STRING,
imsi STRING,
pvuid STRING
)
PARTITIONED BY (ds STRING )
STORED AS ORC
LOCATION 'cosn://hadoopshanghai/hive/requestinfo'
tblproperties ("orc.compress"="SNAPPY","orc.stripe.size"="268435456");


CREATE external TABLE mytable(
city STRING,
country STRING,
ip STRING,
appid STRING,
mobile STRING,
imsi STRING,
pvuid STRING
)
PARTITIONED BY (ds STRING )
STORED AS PARQUET
tblproperties ('PARQUET.COMPRESS'='SNAPPY',"parquet.stripe.size"="268435456");
