package com.oltpbenchmark.benchmarks.wordpress.util;

import java.util.List;


public class HomePage {

    public List<Block> frontPageContents;

    public HomePage(List<Block> lists) {
        this.frontPageContents = lists;
    }


    public static class Block  {
        public String post_title;
        public int post_id;
        public String post_content;
        public int uid;
        public String post_author;

        public Block(String t, int id, String c, int uid, String author) {
            this.post_title = t;
            this.post_id = id;
            this.post_content = c;
            this.uid = uid;
            this.post_author = author;
        }
    }
}
