package com.oltpbenchmark.benchmarks.wordpress.procedures;

import com.oltpbenchmark.api.Procedure;
import org.apache.log4j.Logger;

import java.sql.*;


public class GetHomePages extends Procedure {

    private static final Logger LOG = Logger.getLogger(GetHomePages.class);

    public void run (Connection conn) throws SQLException {

        //Archives
        long t1 = System.currentTimeMillis();
        PreparedStatement st = conn.prepareStatement("SELECT YEAR(post_date) AS `year`, MONTH(post_date) AS `month`, " +
                "count(ID) as posts " + "FROM wp_posts WHERE post_type = 'post' AND post_status = 'publish' " +
                "GROUP BY YEAR(post_date), MONTH(post_date) ORDER BY post_date DESC ");
        long tt = System.currentTimeMillis();
        LOG.info("duration for prepareStatements: " + (tt - t1)/1000 + " sec");
        st.execute();

        long t2 = System.currentTimeMillis();
        LOG.info("duration for Archives execution: " + (t2 - tt)/1000 + " sec");

        // Recent Post_IDs
         t1 = System.currentTimeMillis();
         st = conn.prepareStatement("SELECT wp_posts.ID FROM wp_posts WHERE 1=1 " +
                "AND wp_posts.post_type = 'post' " +
                "AND ((wp_posts.post_status = 'publish')) " +
                "ORDER BY wp_posts.post_date DESC LIMIT 0, 5 ");

        StringBuilder post_IDs = new StringBuilder();
        ResultSet rs = st.executeQuery();
        while(rs.next()) {
            post_IDs.append(rs.getInt(1));
            post_IDs.append(",");
        }
        post_IDs.deleteCharAt(post_IDs.length()-1);
        rs.close();

        //Post contents
        st = conn.prepareStatement("SELECT wp_posts.* FROM wp_posts WHERE ID IN (" + post_IDs.toString() + ")");
        st.execute();
         t2 = System.currentTimeMillis();
        LOG.info("duration for recentPosts: " + (t2 - t1)/1000+ " sec");
        //Recent Comments, comment meta
        t1 = System.currentTimeMillis();
        st = conn.prepareStatement("SELECT wp_comments.comment_ID FROM wp_comments JOIN wp_posts " +
                "ON wp_posts.ID = wp_comments.comment_post_ID WHERE ( comment_approved = '1' ) " +
                "AND wp_posts.post_status IN ('publish') ORDER BY wp_comments.comment_date_gmt DESC LIMIT 0,5 ");
        rs = st.executeQuery();
        t2 = System.currentTimeMillis();
     //   LOG.info("duration for comments: " + (t2 - t1)/1000+ " sec");
        StringBuilder comment_IDs = new StringBuilder();
        while(rs.next()) {
            comment_IDs.append(rs.getInt(1));
            comment_IDs.append(",");
        }

        rs.close();
        comment_IDs.deleteCharAt(comment_IDs.length()-1);
        st = conn.prepareStatement("SELECT comment_id, meta_key, meta_value FROM wp_commentmeta " +
                "WHERE comment_id IN (" +comment_IDs.toString()+ ") ORDER BY meta_id ASC");
        st.execute();



        //Categories
        t1 = System.currentTimeMillis();
        st = conn.prepareStatement("SELECT  tt.*, t.* FROM wp_terms AS t INNER JOIN wp_term_taxonomy AS tt " +
                "ON t.term_id = tt.term_id WHERE tt.taxonomy IN ('category') AND tt.count > 0 " +
                "ORDER BY t.name ASC LIMIT 0,5");

        st.execute();

        t2 = System.currentTimeMillis();
      //  LOG.info("duration for categories: " + (t2 - t1)/1000+ " sec");
        st.close();
    }
}
