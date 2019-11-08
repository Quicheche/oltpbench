package com.oltpbenchmark.benchmarks.wordpress.procedures;

import com.oltpbenchmark.api.Procedure;
import com.oltpbenchmark.api.SQLStmt;
import com.oltpbenchmark.benchmarks.wordpress.WordpressConstants;
import com.oltpbenchmark.benchmarks.wordpress.util.WordpressUtil;
import com.oltpbenchmark.util.SQLUtil;
import com.oltpbenchmark.util.TextGenerator;
import com.oltpbenchmark.util.TimeUtil;
import org.apache.log4j.Logger;

import java.sql.*;
import java.util.Random;

public class AddNewPost extends Procedure {

    private static final Logger LOG = Logger.getLogger(AddNewPost.class);


    public SQLStmt insertNewPostMeta = new SQLStmt(
            "INSERT INTO wp_postmeta " + "(post_id, meta_key, meta_value) VALUES (?,?,?) "
    );

    public SQLStmt postTerm = new SQLStmt("INSERT INTO wp_term_relationships " +
            "(object_id, term_taxonomy_id, term_order) values (?, ?, ?)");

    public SQLStmt termTaxonomy = new SQLStmt("UPDATE wp_term_taxonomy " +
            "SET count =? where term_taxonomy_id =?");

    public void run(Connection conn, int uid, Random rand, String date, int num_terms) throws SQLException {

        String insertPost = "INSERT INTO " + WordpressConstants.TABLENAME_WP_POSTS + "(`post_author`, `post_date`, `post_date_gmt`, " +
                "     `post_content`, `post_title`, `post_excerpt`, " +
                "     `post_status`, `comment_status`, `ping_status`, `post_password`," +
                "     `post_name`, `to_ping`, `pinged`, `post_modified`, `post_modified_gmt`," +
                "     `post_content_filtered`," +
                "     `post_parent`,`guid`, `menu_order`,`post_type`, `post_mime_type`, `comment_count` ) VALUES" +
                "     (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement stmt = conn.prepareStatement(insertPost, Statement.RETURN_GENERATED_KEYS);
        PreparedStatement postMetaStmt = this.getPreparedStatement(conn, insertNewPostMeta);
        PreparedStatement postTermStmt = this.getPreparedStatement(conn, postTerm);
        PreparedStatement termTaxonomyStmt = this.getPreparedStatement(conn, termTaxonomy);

        int term_id = rand.nextInt(num_terms) + 1;

        // count
        PreparedStatement getCount = conn.prepareStatement("SELECT count FROM " +
                "wp_term_taxonomy WHERE term_taxonomy_id=?");
        getCount.setInt(1, term_id);
        ResultSet rs = getCount.executeQuery();

        int prev_count = 0;
        if (rs.next()) {
            prev_count = rs.getInt(1);
        }
        rs.close();
        getCount.close();

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
        stmt.setString(parameterIndex++, "post");   //post_type
        stmt.setString(parameterIndex++, "");                                   //post_mime_type
        stmt.setInt(parameterIndex++, 0);                                       //comment_count


        stmt.executeUpdate();

        rs = stmt.getGeneratedKeys();
        int latest_postID = rs.getInt(1);

        //insert post_meta
        int index = 0;
        parameterIndex = 1;
        int size = WordpressConstants.POST_META_LIST.size();
        while(index < size) {
            postMetaStmt.setInt(parameterIndex++, latest_postID);
            postMetaStmt.setString(parameterIndex++, WordpressConstants.POST_META_LIST.get(index));
            postMetaStmt.setString(parameterIndex++, TextGenerator.randomStr(rand, 20));
            postMetaStmt.addBatch();
            index++;
            parameterIndex = 1;
        }

        // update term relationship
        parameterIndex = 1;
        postTermStmt.setInt(parameterIndex++, latest_postID);
        postTermStmt.setInt(parameterIndex++, term_id);
        postTermStmt.setInt(parameterIndex++, 0);

        //update term count
        parameterIndex = 1;
        termTaxonomyStmt.setInt(parameterIndex++, ++prev_count);
        termTaxonomyStmt.setInt(parameterIndex++, term_id);


        execute(conn, postTermStmt);
        executeBatch(conn, postMetaStmt);
        execute(conn, termTaxonomyStmt);

    }


    public void executeBatch(Connection conn, PreparedStatement p) throws SQLException{
        boolean successful = false;
        while (!successful) {
            try {
                p.executeBatch();
                successful = true;
                p.clearBatch();
            } catch (SQLException esql) {
                throw esql;
            }
        }
    }

    public void execute(Connection conn, PreparedStatement p) throws SQLException{
        boolean successful = false;
        while (!successful) {
            try {
                p.execute();
                conn.commit();
                successful = true;
            } catch (SQLException esql) {
                throw esql;
            }
        }
    }
}
