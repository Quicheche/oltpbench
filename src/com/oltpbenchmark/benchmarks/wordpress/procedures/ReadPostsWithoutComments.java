package com.oltpbenchmark.benchmarks.wordpress.procedures;

import com.oltpbenchmark.api.Procedure;
import com.oltpbenchmark.api.SQLStmt;
import com.oltpbenchmark.benchmarks.wordpress.WordpressConstants;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ReadPostsWithoutComments extends Procedure {


    public SQLStmt readPostsWithoutComments = new SQLStmt(
            "select * from " + WordpressConstants.TABLENAME_WP_POSTS
                    + "  where ID =?"
    );

    public SQLStmt readPostTaxonomy = new SQLStmt(
            "select * from wp_term_taxonomy w where w.term_taxonomy_id =" +
                    "(select term_taxonomy_id from wp_term_relationships where object_id =?) "
    );

    public SQLStmt readPostMeta = new SQLStmt(
            "select * from wp_postmeta where post_id=?"
    );

    // join wp_posts with category table
    public SQLStmt readPostAndTaxonomy = new SQLStmt("select p.*, w.taxonomy from wp_posts p \n" +
            "left join wp_term_relationships u on u.object_id = p.ID \n" +
            "left join wp_term_taxonomy w on w.term_taxonomy_id = u.term_taxonomy_id \n" +
            "where p.ID =?");

    public void run(Connection conn, int post_ID) throws SQLException {
        PreparedStatement st = this.getPreparedStatement(conn, readPostAndTaxonomy);
        st.setInt(1, post_ID);
        st.executeQuery();

        // get category
        st = this.getPreparedStatement(conn, readPostTaxonomy);
        st.setInt(1, post_ID);
        st.executeQuery();

        //post meta
        st = this.getPreparedStatement(conn, readPostMeta);
        st.setInt(1, post_ID);
        st.executeQuery();

    }

}
