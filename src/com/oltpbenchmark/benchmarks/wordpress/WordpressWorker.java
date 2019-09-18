package com.oltpbenchmark.benchmarks.wordpress;

import com.oltpbenchmark.api.TransactionType;
import com.oltpbenchmark.api.Worker;
import com.oltpbenchmark.benchmarks.wordpress.procedures.*;
import com.oltpbenchmark.types.TransactionStatus;
import com.oltpbenchmark.util.TimeUtil;
import com.oltpbenchmark.api.Procedure.UserAbortException;
import java.sql.SQLException;
import com.oltpbenchmark.util.RandomDistribution.Flat;
import com.oltpbenchmark.util.RandomDistribution.Zipf;


public class WordpressWorker extends Worker<WordpressBenchmark> {

    private final int num_users;
    private final int num_posts;
    private final int num_edit_posts;

    public WordpressWorker(WordpressBenchmark benchmarkModule, int id) {
        super(benchmarkModule, id);
        this.num_users =  (int)Math.round(WordpressConstants.NUM_USERS *
                this.getWorkloadConfiguration().getScaleFactor());
        this.num_posts = (int) Math.round(WordpressConstants.NUM_POSTS *
                this.getWorkloadConfiguration().getScaleFactor());
        this.num_edit_posts = (int) Math.round(WordpressConstants.EDIT_POSTS *
                this.getWorkloadConfiguration().getScaleFactor());
    }

    @Override
    protected TransactionStatus executeWork(TransactionType txnType) throws UserAbortException, SQLException {

        Flat z_users = new Flat(this.rng(), 1, this.num_users);
        Zipf z_posts = new Zipf(this.rng(), 1, this.num_posts, WordpressConstants.USER_ID_SIGMA);
        Zipf edit_post = new Zipf(this.rng(), 1, this.num_edit_posts, WordpressConstants.USER_ID_SIGMA);

        int userId = z_users.nextInt();
        int postId = z_posts.nextInt();
        int editPostId = edit_post.nextInt();

        if (txnType.getProcedureClass().equals(GetHomePages.class)) {
            getHomePage();
        } else if (txnType.getProcedureClass().equals(ReadPosts.class)) {
            readPosts(postId);
        } else if (txnType.getProcedureClass().equals(AddNewPost.class)) {
            addNewPosts(userId);
        } else if (txnType.getProcedureClass().equals(AddComments.class)) {
            addComments( postId);
        } else if (txnType.getProcedureClass().equals(EditPosts.class)) {
            editPosts(editPostId);
        }

        conn.commit();
        return (TransactionStatus.SUCCESS);
    }

    public void getHomePage() throws SQLException {
        GetHomePages proc = this.getProcedure(GetHomePages.class);
        assert(proc != null);
        try {
            proc.run(conn);
        } catch (SQLException ex) {
            throw ex;
        }
    }

    public void readPosts(int post_id) throws SQLException {
        ReadPosts proc = this.getProcedure(ReadPosts.class);
        assert(proc != null);
        try {
            proc.run(conn, post_id);
        } catch (SQLException ex) {
            throw ex;
        }
    }

    public void addNewPosts(int uid) throws SQLException {
        AddNewPost proc = this.getProcedure(AddNewPost.class);
        assert (proc != null);
        String date = TimeUtil.getCurrentTimeString();
        try {
            proc.run(conn, uid, this.rng(), date);
        } catch (SQLException ex) {
            throw ex;
        }

    }

    public void addComments( int postId) throws SQLException {
       // post_id,  cur_count value
        AddComments proc = this.getProcedure(AddComments.class);
        assert(proc != null);
        try {
            proc.run(conn, this.rng(), postId);
        } catch (SQLException ex) {
            throw ex;
        }
    }

    public void editPosts(int post_id) throws SQLException {

        EditPosts proc = this.getProcedure(EditPosts.class);
        String date = TimeUtil.getCurrentTimeString();
        assert(proc != null);
        try {
            proc.run(conn,  post_id, this.rng(), date);
        } catch (SQLException ex) {
            throw ex;
        }
    }
}
