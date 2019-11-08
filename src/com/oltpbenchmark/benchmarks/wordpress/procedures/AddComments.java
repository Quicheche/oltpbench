package com.oltpbenchmark.benchmarks.wordpress.procedures;

import com.oltpbenchmark.api.Procedure;
import com.oltpbenchmark.api.SQLStmt;
import com.oltpbenchmark.benchmarks.wordpress.WordpressConstants;
import com.oltpbenchmark.benchmarks.wordpress.data.NameHistogram;
import com.oltpbenchmark.benchmarks.wordpress.util.WordpressUtil;
import com.oltpbenchmark.util.RandomDistribution;
import com.oltpbenchmark.util.TextGenerator;
import com.oltpbenchmark.util.TimeUtil;
import org.apache.log4j.Logger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

public class AddComments extends Procedure {

    private static final Logger LOG = Logger.getLogger(AddComments.class);


    public SQLStmt addComments = new SQLStmt(
        "INSERT INTO " + WordpressConstants.TABLENAME_WP_COMMENTS  +
                "(`comment_post_ID`, `comment_author`, `comment_author_email`, `comment_author_url`, " +
                " `comment_author_IP`, `comment_date`, `comment_date_gmt`, `comment_content`, `comment_karma`," +
                " `comment_approved`, `comment_agent`, `comment_type`, `comment_parent`, `user_id`) VALUES" +
                " (?,?,?,?,?,?,?,?,?,?,?,?,?,?)"
    );

    public SQLStmt getCommentsCount = new SQLStmt(
      "SELECT COUNT(*) FROM wp_comments WHERE comment_post_ID =? AND comment_approved = '1'"
    );
    
    public SQLStmt updateCommentsCount = new SQLStmt(
      "UPDATE " + WordpressConstants.TABLENAME_WP_POSTS + " SET comment_count= ? WHERE ID=?"
    );

    public void run(Connection conn,  Random rand, int post_id) throws SQLException {
        PreparedStatement stmt = this.getPreparedStatement(conn, addComments);

        NameHistogram name_h = new NameHistogram();
        RandomDistribution.FlatHistogram<Integer> name_len_rng = new RandomDistribution.FlatHistogram<Integer>(rand, name_h);

        int author_len = name_len_rng.nextValue().intValue();
        String comment_author_name = TextGenerator.randomStr(rand, author_len);
        char eChars[] = TextGenerator.randomChars(rand, rand.nextInt(32) + 5);
        eChars[4 + rand.nextInt(eChars.length - 4)] = '@';
        String comment_author_email = new String(eChars);
        String agent = TextGenerator.randomStr(rand, rand.nextInt(180));

        int parameterIndex = 1;
        stmt.setInt(parameterIndex++, post_id);                                       //comment_post_id
        stmt.setString(parameterIndex++, comment_author_name);                        //comment_author
        stmt.setString(parameterIndex++, comment_author_email);                       //comment_author_email
        stmt.setString(parameterIndex++, "");                                      //comment_author_url
        stmt.setString(parameterIndex++, WordpressUtil.generateRandomIP(rand));       //comment_author_IP
        stmt.setString(parameterIndex++, TimeUtil.getCurrentTimeString());            //comment_date
        stmt.setString(parameterIndex++, TimeUtil.getCurrentTimeString());            //comment_date_gmt
        stmt.setString(parameterIndex++, WordpressUtil.generateCommentContent(rand)); //comment_content
        stmt.setInt(parameterIndex++, 0);                                          //comment_karma
        stmt.setInt(parameterIndex++, 1);                                          //comment_approved
        stmt.setString(parameterIndex++, agent);                                      //comment_agent
        stmt.setString(parameterIndex++, "");                                      //comment_type
        stmt.setInt(parameterIndex++, 0);                                          //comment_parent
        stmt.setInt(parameterIndex++, 0);                                          //user_id

        ResultSet rs = this.getPreparedStatement(conn, getCommentsCount, post_id).executeQuery();
        int prev_comment_count = 0;
        if (rs.next()) {
            prev_comment_count = rs.getInt(1);
        }

        rs.close();
        stmt.execute();
        // update #num of comment for this post
        int updated = this.getPreparedStatement(conn, updateCommentsCount, ++prev_comment_count,
                post_id ).executeUpdate();

        if (updated != 1) {
            String msg = String.format("Failed to update comment count for post #%d ", post_id, updated);

            throw new SQLException(msg);
        }
    }

}
