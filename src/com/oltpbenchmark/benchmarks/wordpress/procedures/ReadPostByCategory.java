package com.oltpbenchmark.benchmarks.wordpress.procedures;

import com.oltpbenchmark.api.Procedure;
import org.apache.log4j.Logger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ReadPostByCategory extends Procedure {

    /**
     * given a category/tag (term_taxonomy_id), get latest related posts
     */
    private static final Logger LOG = Logger.getLogger(ReadPostByCategory.class);

    public void run(Connection conn, int term_id) throws SQLException {

        PreparedStatement st = conn.prepareStatement("SELECT SQL_CALC_FOUND_ROWS wp_posts.ID FROM wp_posts " +
                "LEFT JOIN wp_term_relationships ON (wp_posts.ID = wp_term_relationships.object_id) " +
                "WHERE 1=1 AND ( wp_term_relationships.term_taxonomy_id IN (" + term_id + ")) " +
                "AND wp_posts.post_type = 'post' " +
                "AND (wp_posts.post_status = 'publish') " +
                "GROUP BY wp_posts.ID ORDER BY wp_posts.post_date DESC LIMIT 0, 10");

        ResultSet rs = st.executeQuery();

        StringBuilder postIDs = new StringBuilder();
        if (!rs.next()) {
            LOG.info("no results with termid: " + term_id);
            return;
        }
        while (rs.next()) {
            int id = rs.getInt(1);
            postIDs.append(id + ",");

        }

        postIDs.deleteCharAt(postIDs.length() - 1);
        rs.close();

        //Retrieve post content
        st = conn.prepareStatement("SELECT wp_posts.* FROM wp_posts WHERE ID IN (" + postIDs.toString() + ")");
        st.execute();

        //Get all categories for each posts
        st = conn.prepareStatement("SELECT t.*, tt.*, tr.object_id FROM wp_terms AS t " +
                "INNER JOIN wp_term_taxonomy AS tt ON t.term_id = tt.term_id " +
                "INNER JOIN wp_term_relationships AS tr ON tr.term_taxonomy_id = tt.term_taxonomy_id " +
                "WHERE tt.taxonomy IN ('category', 'post_tag', 'post_format') " +
                "AND tr.object_id IN (" + postIDs.toString() + ") ORDER BY t.name ASC;");
        st.execute();
        st.close();
    }

}
