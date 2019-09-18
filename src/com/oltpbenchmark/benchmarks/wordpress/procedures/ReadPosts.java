package com.oltpbenchmark.benchmarks.wordpress.procedures;

import com.oltpbenchmark.api.Procedure;
import com.oltpbenchmark.api.SQLStmt;
import com.oltpbenchmark.benchmarks.wordpress.WordpressConstants;
import org.apache.commons.lang.StringUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ReadPosts extends Procedure {


    public SQLStmt readPostContent = new SQLStmt(
            "select * from  " + WordpressConstants.TABLENAME_WP_POSTS
                    + " where ID=?"
    );

    public SQLStmt readComments = new SQLStmt(
            "select * from  "
                    + WordpressConstants.TABLENAME_WP_COMMENTS
                    + " where comment_post_ID=? limit 10"
    );

    public SQLStmt readPostAndComments = new SQLStmt(
            "select * from " + WordpressConstants.TABLENAME_WP_POSTS + " LEFT JOIN "
                    + WordpressConstants.TABLENAME_WP_COMMENTS
                    + " ON ID = comment_post_ID where ID = ?"
    );

    /**
     * Read using left join
     * @param conn
     * @param post_ID
     * @return
     * @throws SQLException
     */
    public boolean run(Connection conn, int post_ID) throws SQLException {
        PreparedStatement st = this.getPreparedStatement(conn, readPostAndComments);
        st.setInt(1, post_ID);
        ResultSet rs = st.executeQuery();
        if (rs.next()) {
            while (rs.next()) {
                String content = rs.getString(5);
                int comment_count = rs.getInt(23);
                assert (StringUtils.isNotEmpty(content));
                if (comment_count > 0) {
                    String comment_content = rs.getString(32);
                    assert (StringUtils.isNotEmpty(comment_content));
                }
            }
            rs.close();
        } else {
            rs.close();
            throw new IllegalArgumentException(
                    String.format("Invalid postId: %d to get post and comments ", post_ID));
        }

        return true;
    }

    /**
     * Reading post and comments seperatelly
     * @param conn
     * @param post_ID
     * @return
     * @throws SQLException
     */
    public boolean run2(Connection conn, int post_ID) throws SQLException {
        PreparedStatement st = this.getPreparedStatement(conn, readPostContent);
        st.setInt(1, post_ID);
        ResultSet rs_post = st.executeQuery();
        int comment_count = 0;
        if (rs_post.next()) {
            String content = rs_post.getString(5);
            comment_count = rs_post.getInt(23);
            assert (content != null);
        } else {
            rs_post.close();
            throw new IllegalArgumentException("Invalid postId");
        }
        // retrive comments if the number of comments for this post > 0
        if (comment_count > 0) {
            st = this.getPreparedStatement(conn, readComments);
            st.setInt(1, post_ID);
            ResultSet rs_comment = st.executeQuery();
            if (!rs_comment.next()) {
                throw new IllegalArgumentException("Invalid postId to get comments for postID: " + post_ID);
            } else {
                while (rs_comment.next()) {
                    String ip = rs_comment.getString(6);
                    String comment_content = rs_comment.getString(4);
                    assert (StringUtils.isNotEmpty(ip) && StringUtils.isNotEmpty(comment_content));
                }
                rs_comment.close();
            }
        }
        return true;
    }
}
