package com.oltpbenchmark.benchmarks.wordpress.procedures;

import com.oltpbenchmark.api.Procedure;
import com.oltpbenchmark.api.SQLStmt;
import com.oltpbenchmark.benchmarks.wordpress.WordpressConstants;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ReadPosts extends Procedure {



    public SQLStmt readComments = new SQLStmt(
            "select * from  "
                    + WordpressConstants.TABLENAME_WP_COMMENTS
                    + " where comment_post_ID=? limit 10"
    );


    public SQLStmt readPostMeta = new SQLStmt(
            "select * from wp_postmeta where post_id=?"
    );

    // join wp_posts with category table
    public SQLStmt readPostAndTaxonomy = new SQLStmt("select p.*, w.taxonomy from wp_posts p \n" +
            "left join wp_term_relationships u on u.object_id = p.ID \n" +
            "left join wp_term_taxonomy w on w.term_taxonomy_id = u.term_taxonomy_id \n" +
            "where p.ID =?");

    /**
     * Read using join
     * @param conn
     * @param post_ID
     * @return
     * @throws SQLException
     */
    public boolean run(Connection conn, int post_ID) throws SQLException {
        PreparedStatement st = this.getPreparedStatement(conn, readPostAndTaxonomy);
        st.setInt(1, post_ID);
        ResultSet rs = st.executeQuery();
        // read post comment count
        int comment_count = rs.getInt(23);
        rs.close();

        //read comments
        if (comment_count > 0) {
            st = this.getPreparedStatement(conn, readComments);
            st.setInt(1, post_ID);
            st.executeQuery();
        }

        //read post meta
        st = this.getPreparedStatement(conn, readPostMeta);
        st.setInt(1, post_ID);
        st.executeQuery();
        st.close();
        return true;
    }
}
