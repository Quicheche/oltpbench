package com.oltpbenchmark.benchmarks.wordpress.procedures;

import com.oltpbenchmark.api.Procedure;
import java.sql.*;


public class GetHomePages extends Procedure {

    /**
     * Assume Blog's home page display latest post with post_title, post_content, and author
     */

    public void run (Connection conn) throws SQLException {
        PreparedStatement st = conn.prepareStatement("select p.ID, p.post_title,p.post_content, p.post_author, " +
                "p.post_date, u.display_name from wp_posts p " +
                 "left join wp_users u on u.ID = p.post_author " +
                 "where p.post_status='publish' and p.post_type !='revision' \n" +
                 "order by p.ID desc limit 20");

        st.executeQuery();
    }
}
