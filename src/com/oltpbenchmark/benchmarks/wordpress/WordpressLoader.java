package com.oltpbenchmark.benchmarks.wordpress;

import com.oltpbenchmark.api.Loader;
import com.oltpbenchmark.benchmarks.wordpress.data.NameHistogram;
import com.oltpbenchmark.benchmarks.wordpress.util.WordpressUtil;
import com.oltpbenchmark.catalog.Table;
import com.oltpbenchmark.distributions.ScrambledZipfianGenerator;
import com.oltpbenchmark.util.RandomDistribution.FlatHistogram;
import com.oltpbenchmark.util.RandomDistribution;
import com.oltpbenchmark.util.SQLUtil;
import com.oltpbenchmark.util.TextGenerator;
import com.oltpbenchmark.util.TimeUtil;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CountDownLatch;

public class WordpressLoader extends Loader<WordpressBenchmark> {

    private static final Logger LOG = Logger.getLogger(WordpressLoader.class);
    public final static int configCommitCount = 1000;

    private final int num_users;
    private final int num_posts;
    private final int num_comments;

    Map<Integer, Integer> commentsPerPosts;

    public WordpressLoader(WordpressBenchmark benchmark) {
        super(benchmark);
        this.num_users = (int) Math.round(WordpressConstants.NUM_USERS * this.scaleFactor);
        this.num_posts = (int) Math.round(WordpressConstants.NUM_POSTS * this.scaleFactor);
        this.num_comments = (int) Math.round(WordpressConstants.NUM_COMMENT * this.scaleFactor);
        this.commentsPerPosts = new HashMap<>();
        LOG.info("num# of users: " + this.num_users);
        LOG.info("num# of posts: " + this.num_posts);
    }

    @Override
    public List<LoaderThread> createLoaderThreads() throws SQLException {

        List<LoaderThread> threads = new ArrayList<LoaderThread>();
        final int numLoaders = this.benchmark.getWorkloadConfiguration().getLoaderThreads();

        // first we load USERS
        final int numItems = this.num_users;
        final int itemsPerThread = Math.max(numItems / numLoaders, 1);
        final int numUserThreads = (int) Math.ceil((double) this.num_users / itemsPerThread);
        final int postsPerThread = Math.max(this.num_posts / numLoaders, 1);
        final int numPostThreads = (int) Math.ceil((double) this.num_posts / postsPerThread);
        final int commentsPerThread = Math.max(this.num_comments / numLoaders, 1);
        final int numCommentsThreads = (int) Math.ceil((double) this.num_comments / commentsPerThread);

        final CountDownLatch userLatch = new CountDownLatch(numUserThreads);

        // USERS

        for (int i = 0; i < numUserThreads; i++) {
            // load USERS[lo, hi]
            final int lo = i * itemsPerThread + 1;
            final int hi = Math.min(this.num_users, (i + 1) * itemsPerThread);
            threads.add(new LoaderThread() {
                @Override
                public void load(Connection conn) throws SQLException {
                    WordpressLoader.this.loadUsers(conn, lo, hi);
                    userLatch.countDown();
                }
            });
        }

        //load comments
        for (int i = 0; i < numCommentsThreads; i++) {
            final int lo = i * commentsPerThread + 1;
            final int hi = Math.min(this.num_comments, (i + 1) * commentsPerThread);
            threads.add(new LoaderThread() {
                @Override
                public void load(Connection conn) throws SQLException {
                    try {
                        userLatch.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                    WordpressLoader.this.loadComments(conn, lo, hi);
                }
            });
        }

        // load posts
        for (int i = 0; i < numPostThreads; i++) {

            final int lo = i * postsPerThread + 1;
            final int hi = Math.min(this.num_posts, (i + 1) * postsPerThread);
            threads.add(new LoaderThread() {
                @Override
                public void load(Connection conn) throws SQLException {
                    try {
                        userLatch.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                    WordpressLoader.this.loadPosts(conn, lo, hi);
                }
            });
        }
        return threads;
    }

    private void loadUsers(Connection conn, int lo, int hi) throws SQLException {

        Table catalog_tbl = this.benchmark.getTableCatalog(WordpressConstants.TABLENAME_WP_USERS);
        assert (catalog_tbl != null);

        String sql = SQLUtil.getInsertSQL(catalog_tbl, this.getDatabaseType());
        PreparedStatement userInsert = conn.prepareStatement(sql);

        NameHistogram name_h = new NameHistogram();
        FlatHistogram<Integer> name_len_rng = new FlatHistogram<Integer>(this.rng(), name_h);

        Random rand = new Random();
        int total = 0;
        int batchSize = 0;


        for (int i = lo; i <= hi; i++) {
            // Generate a random username for this user
            int name_length = name_len_rng.nextValue().intValue();
            String login_name = TextGenerator.randomStr(this.rng(), name_length);
            String user_pass = TextGenerator.randomStr(rand, WordpressConstants.PASS_LENGTH);
            char eChars[] = TextGenerator.randomChars(rand, rand.nextInt(32) + 5);
            eChars[4 + rand.nextInt(eChars.length - 4)] = '@';
            String email = new String(eChars);
            String registrationTime = TimeUtil.getCurrentTimeString();

            int parameterIndex = 1;
            userInsert.setInt(parameterIndex++, i);                                        // ID
            userInsert.setString(parameterIndex++, login_name);                            // LOGIN
            userInsert.setString(parameterIndex++, user_pass);                             //user_pass
            userInsert.setString(parameterIndex++, login_name);                           //nick name
            userInsert.setString(parameterIndex++, email);                                 // user_email
            userInsert.setString(parameterIndex++, WordpressUtil.generateRandomURL(rand)); //user_url
            userInsert.setString(parameterIndex++, registrationTime);                      //user_registered
            userInsert.setString(parameterIndex++, "");                                 //user_activation_key
            userInsert.setInt(parameterIndex++, WordpressConstants.USER_STATUS);           //user_status
            userInsert.setString(parameterIndex++, login_name);                            //display_name
            userInsert.addBatch();

            batchSize++;
            total++;
            if ((batchSize % configCommitCount) == 0) {
                int result[] = userInsert.executeBatch();
                assert (result != null);
                conn.commit();
                userInsert.clearBatch();
                batchSize = 0;
                if (LOG.isDebugEnabled()) {
                    LOG.debug(String.format("Users %d / %d", total, this.num_users));
                }
            }
        } // FOR
        if (batchSize > 0) {
            userInsert.executeBatch();
            conn.commit();
            userInsert.clearBatch();
        }
        userInsert.close();
        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Users Loaded [%d]", total));
        }
    }

    /**
     * loading posts or pages
     *
     * @param conn
     * @param lo
     * @param hi
     * @throws SQLException
     */
    protected void loadPosts(Connection conn, int lo, int hi) throws SQLException {
        Table catalog_tbl = this.benchmark.getTableCatalog(WordpressConstants.TABLENAME_WP_POSTS);
        assert (catalog_tbl != null);
        String sql = SQLUtil.getInsertSQL(catalog_tbl, this.getDatabaseType());
        PreparedStatement postInsert = conn.prepareStatement(sql);

        int total = 0;
        int batchSize = 0;
        ScrambledZipfianGenerator zy = new ScrambledZipfianGenerator(1, this.num_users);
        for (int i = lo; i <= hi; i++) {
            int uid = zy.nextInt();
            Random rand = new Random();

            String date = TimeUtil.getCurrentTimeString();
            String page_content = WordpressUtil.generatePostContent(rand, uid);
            String page_title = WordpressUtil.generatePostTitle(rand);

            int commentCount = commentsPerPosts.getOrDefault(i, 0);
            int parameterIndex = 1;

            postInsert.setInt(parameterIndex++, i);                                          //set ID
            postInsert.setInt(parameterIndex++, uid);                                        //post author
            postInsert.setString(parameterIndex++, date);                                    //post_date
            postInsert.setString(parameterIndex++, date);                                    //post_date_gmt
            postInsert.setString(parameterIndex++, page_content);                            //post_content
            postInsert.setString(parameterIndex++, page_title);                              //post_title
            postInsert.setString(parameterIndex++, "");                                  //post_excerpt
            postInsert.setString(parameterIndex++, WordpressConstants.PUBLISHED_STATUS);     //post_status
            postInsert.setString(parameterIndex++, WordpressConstants.OPEN);                 //comment_status
            postInsert.setString(parameterIndex++, WordpressConstants.OPEN);                 //ping_status'
            postInsert.setString(parameterIndex++, "");                                   //post_password
            postInsert.setString(parameterIndex++, "");                                   //post_name
            postInsert.setString(parameterIndex++, "");                                   //to_ping
            postInsert.setString(parameterIndex++, "");                                   //pinged
            postInsert.setString(parameterIndex++, date);                                    //post_modified
            postInsert.setString(parameterIndex++, date);                                    //post_moditied_gmt
            postInsert.setString(parameterIndex++, "");                                   //post_content_filtered
            postInsert.setInt(parameterIndex++, 0);                                       //post_parent
            postInsert.setString(parameterIndex++, WordpressUtil.generateRandomURL(rand));   //guid
            postInsert.setInt(parameterIndex++, 0);                                       //menu_order
            postInsert.setString(parameterIndex++, WordpressUtil.getRandomPostType(rand));   //post_type
            postInsert.setString(parameterIndex++, "");                                   //post_mime_type
            postInsert.setInt(parameterIndex++, commentCount);                                //comment_count
            postInsert.addBatch();
            batchSize++;
            total++;

            if ((batchSize % configCommitCount) == 0) {
                int result[] = postInsert.executeBatch();
                assert (result != null);
                conn.commit();
                postInsert.clearBatch();
                batchSize = 0;
                if (LOG.isDebugEnabled()) {
                    LOG.debug(String.format("posts %d / %d", total, this.num_posts));
                }
            }
        }
        if (batchSize > 0) {
            postInsert.executeBatch();
            conn.commit();
        }
        postInsert.close();
        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("posts Loaded [%d]", total));
        }

    }

    protected void loadComments(Connection conn, int lo, int hi) throws SQLException {
        Table catalog_tbl = this.benchmark.getTableCatalog(WordpressConstants.TABLENAME_WP_COMMENTS);
        assert (catalog_tbl != null);
        String sql = SQLUtil.getInsertSQL(catalog_tbl, this.getDatabaseType());

        PreparedStatement commentInsert = conn.prepareStatement(sql);

        NameHistogram name_h = new NameHistogram();
        RandomDistribution.FlatHistogram<Integer> name_len_rng = new RandomDistribution.FlatHistogram<Integer>(this.rng(), name_h);
        int batchSize = 0;
        Random rand = new Random();
        int total = 0;

        for (int i = lo; i <= hi; i++) {
            int author_len = name_len_rng.nextValue().intValue();
            String comment_author_name = TextGenerator.randomStr(this.rng(), author_len);
            char eChars[] = TextGenerator.randomChars(rand, rand.nextInt(32) + 5);
            eChars[4 + rand.nextInt(eChars.length - 4)] = '@';
            String comment_author_email = new String(eChars);
            String agent = TextGenerator.randomStr(rand, rand.nextInt(180));
            int comment_post_id = rand.nextInt(this.num_posts);

            //calculate # comments per post_id
            commentsPerPosts.put(comment_post_id,
                    commentsPerPosts.getOrDefault(comment_post_id, 0) + 1);


            int parameterIndex = 1;
            commentInsert.setInt(parameterIndex++, i);                                             //comment_id
            commentInsert.setInt(parameterIndex++, comment_post_id);                               //comment_post_id
            commentInsert.setString(parameterIndex++, comment_author_name);                        //comment_author
            commentInsert.setString(parameterIndex++, comment_author_email);                      //comment_author_email
            commentInsert.setString(parameterIndex++, "");                                     //comment_author_url
            commentInsert.setString(parameterIndex++, WordpressUtil.generateRandomIP(rand));       //comment_author_IP
            commentInsert.setString(parameterIndex++, TimeUtil.getCurrentTimeString());            //comment_date
            commentInsert.setString(parameterIndex++, TimeUtil.getCurrentTimeString());            //comment_date_gmt
            commentInsert.setString(parameterIndex++, WordpressUtil.generateCommentContent(rand)); //comment_content
            commentInsert.setInt(parameterIndex++, 0);                                          //comment_karma
            commentInsert.setInt(parameterIndex++, 1);                                          //comment_approved
            commentInsert.setString(parameterIndex++, agent);                                      //comment_agent
            commentInsert.setString(parameterIndex++, "");                                      //comment_type
            commentInsert.setInt(parameterIndex++, 0);                                          //comment_parent
            commentInsert.setInt(parameterIndex++, 0);                                          //user_id
            commentInsert.addBatch();

            batchSize++;
        }
        if (batchSize > 0) {
            commentInsert.executeBatch();
            conn.commit();

        }
        commentInsert.close();
        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("comments Loaded [%d]", total));
        }

    }
}
