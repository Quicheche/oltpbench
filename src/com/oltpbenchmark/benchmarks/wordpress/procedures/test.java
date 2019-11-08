package com.oltpbenchmark.benchmarks.wordpress.procedures;

import com.oltpbenchmark.api.Procedure;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;


public class test extends Procedure {

    private static final Logger LOG = Logger.getLogger(test.class);


    public void run (Connection conn) throws SQLException {
        //Archives
        long t1 = System.currentTimeMillis();

        Statement stmt = conn.createStatement();
        //PreparedStatement st = conn.prepareStatement("SELECT YEAR(post_date) AS `year`, MONTH(post_date) AS `month`, " +
                //"count(ID) as posts " + "FROM wp_posts WHERE post_type = 'post' AND post_status = 'publish' " +
                //"GROUP BY YEAR(post_date), MONTH(post_date) ORDER BY post_date DESC LIMIT 0, 12");

        stmt.executeQuery("SELECT YEAR(post_date) AS `year`, MONTH(post_date) AS `month`, " +
                "count(ID) as posts " + "FROM wp_posts WHERE post_type = 'post' AND post_status = 'publish' " +
                "GROUP BY YEAR(post_date), MONTH(post_date) ORDER BY post_date DESC LIMIT 0, 12");
        stmt.close();
        //t1 = System.currentTimeMillis();
        //st.execute();


        long tt = System.currentTimeMillis();
        LOG.info("duration for exe statement: " + (tt - t1) + " million secs");

        //st.close();
    }
}
