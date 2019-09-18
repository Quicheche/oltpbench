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


    public SQLStmt insertUpdatePost = new SQLStmt(
            "INSERT INTO " + WordpressConstants.TABLENAME_WP_POSTS
                    + "(`post_author`, `post_date`, `post_date_gmt`, " +
                    "     `post_content`, `post_title`, `post_excerpt`, " +
                    "     `post_status`, `comment_status`, `ping_status`, `post_password`," +
                    "     `post_name`, `to_ping`, `pinged`, `post_modified`, `post_modified_gmt`," +
                    "     `post_content_filtered`," +
                    "     `post_parent`,`guid`, `menu_order`,`post_type`, `post_mime_type`, `comment_count` ) VALUES" +
                    "     (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"
    );



    public SQLStmt getOldPostContent = new SQLStmt(
       "SELECT  `post_author`,  " +
            "     `post_content`, `post_title`, `post_excerpt`, " +
                    "     `comment_status`, `ping_status`, `post_password`," +
                    "     `post_name`, `to_ping`, `pinged`," +
                    "     `post_content_filtered`," +
                    "     `post_parent`,`guid`, `menu_order`,`post_type`, `post_mime_type`, `comment_count`  FROM "
               + WordpressConstants.TABLENAME_WP_POSTS  +" WHERE ID=?"
    );

    public boolean run(Connection conn, int post_id, Random rand, String date) throws SQLException {

        LOG.info("selected post_id: " + post_id );
        String oldText = "";

        ResultSet rs = this.getPreparedStatement(conn, getOldPostContent, post_id).executeQuery();
        PreparedStatement stmt = this.getPreparedStatement(conn, insertUpdatePost);
        try {
            if (rs.next()) {


                oldText = rs.getString(2);
                char[] newPostContent = TextGenerator.permuteText(rand, oldText.toCharArray());

                String newText = new String(newPostContent);
                int parameterIndex = 1, rsColumn = 1;

                stmt.setInt(parameterIndex++, rs.getInt(rsColumn++));                      //post author
                stmt.setString(parameterIndex++, date);                                    //post_date
                stmt.setString(parameterIndex++, date);                                    //post_date_gmt
                stmt.setString(parameterIndex++, "EDIT: "+ newText);                    //post_content
                rsColumn++;
                stmt.setString(parameterIndex++, rs.getString(rsColumn++));                 //post_title
                stmt.setString(parameterIndex++, rs.getString(rsColumn++) );                //post_excerpt
                stmt.setString(parameterIndex++, WordpressConstants.INHERIT_STATUS);        //post_status
                stmt.setString(parameterIndex++, rs.getString(rsColumn++));                 //comment_status
                stmt.setString(parameterIndex++, rs.getString(rsColumn++));                 //ping_status'
                stmt.setString(parameterIndex++, rs.getString(rsColumn++));                 //post_password
                stmt.setString(parameterIndex++, rs.getString(rsColumn++));                 //post_name
                stmt.setString(parameterIndex++, rs.getString(rsColumn++));                 //to_ping
                stmt.setString(parameterIndex++, rs.getString(rsColumn++));                 //pinged
                stmt.setString(parameterIndex++, date);                                     //post_modified
                stmt.setString(parameterIndex++, date);                                     //post_moditied_gmt
                stmt.setString(parameterIndex++, rs.getString(rsColumn++));                 //post_content_filtered
                stmt.setInt(parameterIndex++, rs.getInt(rsColumn++));                       //post_parent
                stmt.setString(parameterIndex++,rs.getString(rsColumn++));                  //guid
                stmt.setInt(parameterIndex++, rs.getInt(rsColumn++));                       //menu_order
                stmt.setString(parameterIndex++, rs.getString(rsColumn++));                 //post_type
                stmt.setString(parameterIndex++, rs.getString(rsColumn++));                 //post_mime_type
                stmt.setInt(parameterIndex++, rs.getInt(rsColumn++));                       //comment_count
                rs.close();
            }
        } catch (SQLException e){
            throw e;
        }
        return stmt.execute();
    }
}
