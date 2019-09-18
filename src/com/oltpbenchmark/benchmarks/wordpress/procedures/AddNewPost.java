package com.oltpbenchmark.benchmarks.wordpress.procedures;

import com.oltpbenchmark.api.Procedure;
import com.oltpbenchmark.api.SQLStmt;
import com.oltpbenchmark.benchmarks.wordpress.WordpressConstants;
import com.oltpbenchmark.benchmarks.wordpress.util.WordpressUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Random;

public class AddNewPost extends Procedure {

    public SQLStmt insertNewPost = new SQLStmt(
            "INSERT INTO " + WordpressConstants.TABLENAME_WP_POSTS
                    + "(`post_author`, `post_date`, `post_date_gmt`, " +
                    "     `post_content`, `post_title`, `post_excerpt`, " +
                    "     `post_status`, `comment_status`, `ping_status`, `post_password`," +
                    "     `post_name`, `to_ping`, `pinged`, `post_modified`, `post_modified_gmt`," +
                    "     `post_content_filtered`," +
                    "     `post_parent`,`guid`, `menu_order`,`post_type`, `post_mime_type`, `comment_count` ) VALUES" +
                    "     (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"
    );


    public boolean run(Connection conn, int uid, Random rand, String date) throws SQLException {
        PreparedStatement stmt = this.getPreparedStatement(conn, insertNewPost);
        String page_content = "NEW[ "  + uid + "]" + WordpressUtil.generatePostContent(rand, uid);
        String page_title = WordpressUtil.generatePostTitle(rand);

        int parameterIndex = 1;

        stmt.setInt(parameterIndex++, uid);                                        //post author
        stmt.setString(parameterIndex++, date);                                    //post_date
        stmt.setString(parameterIndex++, date);                                    //post_date_gmt
        stmt.setString(parameterIndex++, page_content);                            //post_content
        stmt.setString(parameterIndex++, page_title);                              //post_title
        stmt.setString(parameterIndex++, "" );                                  //post_excerpt
        stmt.setString(parameterIndex++, WordpressConstants.PUBLISHED_STATUS);     //post_status
        stmt.setString(parameterIndex++, WordpressConstants.OPEN);                 //comment_status
        stmt.setString(parameterIndex++, WordpressConstants.OPEN);                 //ping_status'
        stmt.setString(parameterIndex++, "");                                   //post_password
        stmt.setString(parameterIndex++, "");                                   //post_name
        stmt.setString(parameterIndex++, "");                                   //to_ping
        stmt.setString(parameterIndex++, "");                                   //pinged
        stmt.setString(parameterIndex++, date);                                    //post_modified
        stmt.setString(parameterIndex++, date);                                    //post_moditied_gmt
        stmt.setString(parameterIndex++, "");                                   //post_content_filtered
        stmt.setInt(parameterIndex++, 0);                                       //post_parent
        stmt.setString(parameterIndex++, WordpressUtil.generateRandomURL(rand));   //guid
        stmt.setInt(parameterIndex++, 0);                                       //menu_order
        stmt.setString(parameterIndex++, WordpressUtil.getRandomPostType(rand));   //post_type
        stmt.setString(parameterIndex++, "");                                   //post_mime_type
        stmt.setInt(parameterIndex++, 0);                                       //comment_count

        return (stmt.execute());
    }
}
