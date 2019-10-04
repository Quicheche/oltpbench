package com.oltpbenchmark.benchmarks.wordpress.procedures;

import com.oltpbenchmark.api.Procedure;
import com.oltpbenchmark.api.SQLStmt;
import org.apache.log4j.Logger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ReadPostByCategory extends Procedure {

    /**
     * given a category/tag (term_taxonomy_id), get latest related posts
     */
    private static final Logger LOG = Logger.getLogger(ReadPostByCategory.class);

    SQLStmt getPostMeta = new SQLStmt("select * from wp_postmeta where post_id=?");


    public void run(Connection conn, int term_id) throws SQLException {

        PreparedStatement st = conn.prepareStatement("select p.ID, p.post_title,p.post_content, " +
                "wu.display_name from wp_term_relationships w  " +
                "left join wp_posts p on p.ID = w.object_id " +
                "left join wp_users wu on p.post_author = wu.ID " +
                "where w.term_taxonomy_id =" + term_id +
                "    LIMIT 20");


       ResultSet rs = st.executeQuery();

        List<Integer> post = new ArrayList<>();
        if (rs.next()) {
            while (rs.next()) {
                int post_id = rs.getInt(1);
                post.add(post_id);
            }
        }
        rs.close();
        if (post.size() > 0) {
            for (int i = 0; i < post.size(); i++) {
                st = this.getPreparedStatement(conn, getPostMeta, post.get(i));
                rs = st.executeQuery();
                rs.close();
            }
        }
       rs.close();
    }

}
