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
LOCATION 'cosn://hadoopshanghai/hive'
tblproperties ("orc.compress"="SNAPPY");
