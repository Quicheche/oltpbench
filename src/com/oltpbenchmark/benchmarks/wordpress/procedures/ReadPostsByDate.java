package com.oltpbenchmark.benchmarks.wordpress.procedures;

import com.oltpbenchmark.DBWorkload;
import com.oltpbenchmark.api.Procedure;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ReadPostsByDate extends Procedure {

    /**
     * Read Post through Archives
     * @param conn
     * @throws SQLException
     */

    private static final Logger LOG = Logger.getLogger(ReadPostsByDate.class);

    public void run(Connection conn) throws SQLException {
        // Get this year and random month
        long t1 = System.currentTimeMillis();
        conn.setAutoCommit(false);
        PreparedStatement st = conn.prepareStatement("SELECT YEAR(post_date) AS `year`, MONTH(post_date) " +
                "AS `month`, count(ID) as 'posts' FROM wp_posts WHERE post_type = 'post' AND post_status = 'publish' " +
                "GROUP BY YEAR(post_date), MONTH(post_date) " +
                "ORDER BY post_date DESC");
        ResultSet rs = st.executeQuery();
        long t2 = System.currentTimeMillis();

        LOG.info("duration for : " + (t2 - t1)/1000 + " sec");
        int year = 0;
        List<Integer> months = new ArrayList<>();
        while(rs.next()) {
            if (year == 0) {
                year = rs.getInt(1);
            }
            months.add(rs.getInt(2));
        }
        rs.close();
        Random rand = new Random();
        int random_month = months.get(rand.nextInt(months.size()));


        //Retrive posts by date
        st = conn.prepareStatement("SELECT SQL_CALC_FOUND_ROWS wp_posts.ID FROM wp_posts WHERE 1=1 " +
                "AND ( ( YEAR( wp_posts.post_date ) =" +  year + " AND MONTH( wp_posts.post_date ) ="
                + random_month + " )) " +
                "AND wp_posts.post_type = 'post' AND (wp_posts.post_status = 'publish') " +
                "ORDER BY wp_posts.post_date DESC LIMIT 0, 10");
        StringBuilder post_IDs = new StringBuilder();
        rs = st.executeQuery();
        while(rs.next()) {
            post_IDs.append(rs.getInt(1));
            post_IDs.append(",");
        }

        post_IDs.deleteCharAt(post_IDs.length()-1);
        rs.close();

        st = conn.prepareStatement("SELECT wp_posts.* FROM wp_posts WHERE ID IN (" + post_IDs.toString() + ")");
        st.execute();

        //post categories
        st = conn.prepareStatement("SELECT t.*, tt.*, tr.object_id FROM wp_terms AS t " +
                "INNER JOIN wp_term_taxonomy AS tt ON t.term_id = tt.term_id " +
                "INNER JOIN wp_term_relationships AS tr ON tr.term_taxonomy_id = tt.term_taxonomy_id " +
                "WHERE tt.taxonomy IN ('category', 'post_tag', 'post_format') " +
                "AND tr.object_id IN (" + post_IDs.toString() + ") ORDER BY t.name ASC;");
        st.execute();
        st.close();

    }
}
