package com.facebook.presto.test;

import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Created by liubangchen on 2018/1/18.
 */
public class PrestoDrvierTest {

    @Test
    public void testPrestoDriver()throws Exception{
        String url = "jdbc:presto://127.0.0.1:3000/hive/tpcds_text_2";
        Class.forName("com.facebook.presto.jdbc.PrestoDriver");
        Connection connection = DriverManager.getConnection(url, "root", "1234560");
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery("select * from web_sales_par order by ws_ext_list_price desc , ws_ext_ship_cost asc limit 10");
        while (rs.next()) {
            System.out.println("count size is:\t"+rs.getLong("ws_sold_date_sk"));
        }
        rs.close();
        connection.close();
    }
}
