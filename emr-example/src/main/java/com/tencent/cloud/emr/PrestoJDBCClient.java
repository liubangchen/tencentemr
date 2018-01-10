package com.tencent.cloud.emr;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Created by liubangchen on 2018/1/10.
 */
public class PrestoJDBCClient {

    public static void main(String[] args) throws Exception {
        String url = "jdbc:presto://172.21.32.12:9000/hive/liubangtest";
        Class.forName("com.facebook.presto.jdbc.PrestoDriver");
        Connection connection = DriverManager.getConnection(url, "test", "123456");
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery("show TABLES ");
        while (rs.next()) {
            System.out.println(rs.getRow());
        }
        connection.close();
    }
}
