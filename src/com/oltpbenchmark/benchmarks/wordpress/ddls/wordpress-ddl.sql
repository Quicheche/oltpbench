SET DATABASE SQL SYNTAX MYS TRUE;
DROP TABLE IF EXISTS wp_users;
CREATE TABLE wp_users (
  ID bigint(20)  NOT NULL AUTO_INCREMENT,
  user_login varchar(60) ,
  user_pass varchar(255) ,
  user_nickname varchar(50),
  user_email varchar(100),
  user_url varchar(100),
  user_registered datetime ,
  user_activation_key varchar(255),
  user_status int(11) NOT NULL DEFAULT '0',
  display_name varchar(250)  NOT NULL DEFAULT '',
  PRIMARY KEY (`ID`),
);
CREATE INDEX IDX_WP_LOGINKEY ON wp_users (user_login);
CREATE INDEX IDEX_WP_NICKNAME ON wp_users (user_nickname);
CREATE INDEX IDX_WP_EMAIL ON wp_users (user_email);

DROP TABLE IF EXISTS wp_posts;
CREATE TABLE wp_posts (
  ID bigint(20)  NOT NULL AUTO_INCREMENT,
  post_author bigint(20)  NOT NULL DEFAULT '0',
  post_date datetime NOT NULL ,
  post_date_gmt datetime NOT NULL ,
  post_content longtext NOT NULL,
  post_title text NOT NULL,
  post_excerpt text  NOT NULL DEFAULT '',
  post_status varchar(20) NOT NULL DEFAULT 'publish',
  comment_status varchar(20)  NOT NULL DEFAULT 'open',
  ping_status varchar(20) NOT NULL DEFAULT 'open',
  post_password varchar(255)  NOT NULL DEFAULT '',
  post_name varchar(200)  NOT NULL DEFAULT '',
  to_ping text  NOT NULL,
  pinged text  NOT NULL,
  post_modified datetime NOT NULL ,
  post_modified_gmt datetime NOT NULL ,
  post_content_filtered longtext  NOT NULL,
  post_parent bigint(20)  NOT NULL DEFAULT '0',
  guid varchar(255)  NOT NULL DEFAULT '',
  menu_order int(11) NOT NULL DEFAULT '0',
  post_type varchar(20)  NOT NULL DEFAULT 'post',
  post_mime_type varchar(100)  NOT NULL DEFAULT '',
  comment_count bigint(20) NOT NULL DEFAULT '0',
  PRIMARY KEY (ID)
);
CREATE INDEX IDX_WP_POSTNAME ON wp_posts (post_name);
CREATE INDEX IDX_WP_TYPE_STATUS_DATE ON wp_posts (post_type, post_status, post_date, ID);
CREATE INDEX IDX_WP_POSTAUTHOR ON wp_posts (post_author);

DROP TABLE IF EXISTS wp_comments;
CREATE TABLE wp_comments (
  comment_ID bigint(20)  NOT NULL AUTO_INCREMENT,
  comment_post_ID bigint(20)  NOT NULL DEFAULT '0',
  comment_author text   NOT NULL,
  comment_author_email varchar(100)  NOT NULL DEFAULT '',
  comment_author_url varchar(200)  NOT NULL DEFAULT '',
  comment_author_IP varchar(100)  NOT NULL DEFAULT '',
  comment_date datetime NOT NULL,
  comment_date_gmt datetime NOT NULL,
  comment_content text  NOT NULL,
  comment_karma int(11) NOT NULL DEFAULT '0',
  comment_approved varchar(20)  NOT NULL DEFAULT '1',
  comment_agent varchar(255)  NOT NULL DEFAULT '',
  comment_type varchar(20)  NOT NULL DEFAULT '',
  comment_parent bigint(20)  NOT NULL DEFAULT '0',
  user_id bigint(20)  NOT NULL DEFAULT '0',
  PRIMARY KEY (comment_ID)
);
CREATE INDEX IDX_COMMENT_POSTID ON wp_comments (comment_post_ID);
CREATE INDEX IDX_COMMENT_APPROVED_DATEDMT ON wp_comments (comment_approved, comment_date_gmt);
CREATE INDEX IDX_COMMENT_DATA_GMT ON wp_comments (comment_parent);
CREATE INDEX IDX_COMMENT_PARENT ON wp_comments (comment_date_gmt);
CREATE INDEX IDX_COMMENT_AUTHOR_EMAIL ON wp_comments (comment_author_email);