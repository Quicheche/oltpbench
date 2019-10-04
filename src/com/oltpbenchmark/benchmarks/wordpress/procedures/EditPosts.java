package com.oltpbenchmark.benchmarks.wordpress.procedures;

import com.oltpbenchmark.api.Procedure;
import com.oltpbenchmark.api.SQLStmt;
import com.oltpbenchmark.benchmarks.wordpress.WordpressConstants;
import com.oltpbenchmark.util.TextGenerator;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

public class EditPosts extends Procedure {

    private static final Logger LOG = Logger.getLogger(EditPosts.class);


    public SQLStmt insertUpdatePost = new SQLStmt ("UPDATE wp_posts set post_content=?," +
            " post_modified=?, post_modified_gmt=? where ID=? ");

    public SQLStmt getOldPostContent = new SQLStmt(
       "SELECT  `post_content` " +
            "   FROM " + WordpressConstants.TABLENAME_WP_POSTS  +" WHERE ID=?"
    );

    public boolean run(Connection conn, int post_id, Random rand, String date) throws SQLException {

        String oldText = "";
        LOG.info("edit postID: " + post_id);
        ResultSet rs = this.getPreparedStatement(conn, getOldPostContent, post_id).executeQuery();
        PreparedStatement stmt = this.getPreparedStatement(conn, insertUpdatePost);
        try {
            if (rs.next()) {
                oldText = rs.getString(1);
                char[] newPostContent = TextGenerator.permuteText(rand, oldText.toCharArray());
                String newText = new String(newPostContent);
                int parameterIndex = 1;
                stmt.setString(parameterIndex++, "EDIT: "+ newText);
                stmt.setString(parameterIndex++, date);
                stmt.setString(parameterIndex++, date);
                stmt.setInt(parameterIndex++, post_id);
                rs.close();
            }
        } catch (SQLException e){
            throw e;
        }
        return stmt.execute();
    }
}
