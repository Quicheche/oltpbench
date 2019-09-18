package com.oltpbenchmark.benchmarks.wordpress;

public class WordpressConstants {

    /**
     * Table name
     */

    public static final String TABLENAME_WP_POSTS          = "wp_posts";
    public static final String TABLENAME_WP_USERS          = "wp_users";
    public static final String TABLENAME_WP_COMMENTS       = "wp_comments";


    //base line
    public static final int NUM_USERS = 10;

    public static final int NUM_COMMENT = 5;

    public static final int NUM_POSTS = 20;

    public static final int PASS_LENGTH = 32;

    public static final int EDIT_POSTS = 5;


    //DISTRIBUTION CONSTANTS
    public static final double USER_ID_SIGMA = 1.0001d;

    //Status constants
    public static final int USER_STATUS = 0;
    public static final String PUBLISHED_STATUS = "publish";
    public static final String INHERIT_STATUS = "inherit";
    public static final String REVISION_STATUS = "revision";
    public static final String OPEN = "open";
    public static final String CLOSE = "closed";
}
