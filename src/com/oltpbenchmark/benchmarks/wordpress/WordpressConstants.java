package com.oltpbenchmark.benchmarks.wordpress;

import java.util.Arrays;
import java.util.List;

public class WordpressConstants {

    /**
     * Table name
     */

    public static final String TABLENAME_WP_POSTS          = "wp_posts";
    public static final String TABLENAME_WP_USERS          = "wp_users";
    public static final String TABLENAME_WP_COMMENTS       = "wp_comments";
    public static final String TABLENAME_WP_USERMETA      = "wp_usermeta";


    //base line
    public static final int NUM_USERS = 50;

    public static final int NUM_COMMENT = 100;

    public static final int NUM_POSTS = 15000;

    public static final int PASS_LENGTH = 32;

    public static final int NUM_TERMS = 100;


    //user meta_key
    public static final List<String> USER_META_KEY = Arrays.asList("nickname", "first_name", "last_name",
            "description","rich_editing" , "syntax_highlighting", "comment_shortcuts", "admin_color",
            "use_ssl", "show_admin_bar_front", "locale", "wp_capabilities", "wp_user_level", "session_tokens");

    public static final List<String> POST_META_LIST= Arrays.asList("_wp_page_template",
            "_wp_attached_file", "_wp_attachment_metadata", "_wp_attachment_context", "_wp_attachment_image_alt",
            "_wp_attachment_backup_sizes", "_wp_old_date", "_wp_old_slug");


    //DISTRIBUTION CONSTANTS
    public static final double USER_ID_SIGMA = 1.0001d;

    //Status constants
    public static final int USER_STATUS = 0;
    public static final String PUBLISHED_STATUS = "publish";
    public static final String OPEN = "open";

}
