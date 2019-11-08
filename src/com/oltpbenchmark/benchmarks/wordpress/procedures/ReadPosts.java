package com.oltpbenchmark.benchmarks.wordpress.procedures;

import com.oltpbenchmark.api.Procedure;
import com.oltpbenchmark.api.SQLStmt;
import com.oltpbenchmark.benchmarks.wordpress.WordpressConstants;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ReadPosts extends Procedure {



    public SQLStmt readPostContent = new SQLStmt(
            "select * from  " + WordpressConstants.TABLENAME_WP_POSTS
                    + " where ID=?"
    );

    public SQLStmt readComments = new SQLStmt(
            "SELECT SQL_CALC_FOUND_ROWS wp_comments.comment_ID FROM wp_comments " +
                    "WHERE ( comment_approved = '1' ) AND comment_post_ID =? " +
                    "AND comment_parent = 0 ORDER BY wp_comments.comment_date_gmt ASC, wp_comments.comment_ID ASC"
    );


    public SQLStmt readPostTaxonomy = new SQLStmt(
            "SELECT t.*, tt.*, tr.object_id FROM wp_terms AS t INNER JOIN wp_term_taxonomy AS tt " +
                    "ON t.term_id = tt.term_id INNER JOIN wp_term_relationships AS tr " +
                    "ON tr.term_taxonomy_id = tt.term_taxonomy_id WHERE  " +
                    " tr.object_id IN (?) ORDER BY t.name ASC "
    );

    public SQLStmt readPostMeta = new SQLStmt(
            "SELECT post_id, meta_key, meta_value FROM wp_postmeta WHERE post_id IN (?) ORDER BY meta_id ASC"
    );



    /**
     * Read using join
     * @param conn
     * @param post_ID
     * @return
     * @throws SQLException
     */
    public boolean run(Connection conn, int post_ID) throws SQLException {

        //Post contents
        PreparedStatement st = this.getPreparedStatement(conn, readPostContent);
        st.setInt(1, post_ID);
        st.execute();

        //read post meta
        st = this.getPreparedStatement(conn, readPostMeta);
        st.setInt(1, post_ID);
        st.execute();


        //comments
        st = this.getPreparedStatement(conn, readComments);
        st.setInt(1, post_ID);
        st.execute();

        //terms
        st = this.getPreparedStatement(conn, readPostTaxonomy);
        st.setInt(1, post_ID);
        st.execute();

        st.close();
        return true;
    }
}
