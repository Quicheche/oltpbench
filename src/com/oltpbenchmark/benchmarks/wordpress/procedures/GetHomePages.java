package com.oltpbenchmark.benchmarks.wordpress.procedures;

import com.oltpbenchmark.api.Procedure;
import com.oltpbenchmark.api.SQLStmt;
import com.oltpbenchmark.benchmarks.wordpress.WordpressConstants;
import com.oltpbenchmark.benchmarks.wordpress.util.HomePage;
import  com.oltpbenchmark.benchmarks.wordpress.util.HomePage.Block;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GetHomePages extends Procedure {


    public SQLStmt getRecentPostInfo = new SQLStmt(
      "select distinct p.post_title, p.ID, p.post_content, u.ID, u.display_name, p.post_date from "
              +  WordpressConstants.TABLENAME_WP_POSTS + " p, "
              +  WordpressConstants.TABLENAME_WP_USERS + " u"
              + " where p.post_status = ? and p.post_type !=?"
              + " and p.post_author = u.ID ORDER BY p.post_date DESC LIMIT 100;"
    );


    public HomePage run (Connection conn) throws SQLException {
        List<Block> blocks = new ArrayList<>();
        PreparedStatement st = this.getPreparedStatement(conn, getRecentPostInfo);
        st.setString(1, WordpressConstants.PUBLISHED_STATUS);
        st.setString(2, WordpressConstants.REVISION_STATUS);
        ResultSet rs = st.executeQuery();
        if (!rs.next()) {
            rs.close();
            throw new IllegalArgumentException("No posts or pages in current database");
        } else {
            while(rs.next()) {
                String title = rs.getString(1);
                int id = rs.getInt(2);
                String text = rs.getString(3);
                int uid = rs.getInt(4);
                String displayName = rs.getString(5);
                Block block = new Block(title, id, text, uid, displayName);
                blocks.add(block);
            }
            rs.close();
        }
        HomePage fp = new HomePage(blocks);
        return fp;

    }
}
