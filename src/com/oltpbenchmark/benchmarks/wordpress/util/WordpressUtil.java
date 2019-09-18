package com.oltpbenchmark.benchmarks.wordpress.util;

import com.oltpbenchmark.benchmarks.wordpress.data.PostHistogram;
import com.oltpbenchmark.util.TextGenerator;
import com.oltpbenchmark.util.RandomDistribution.FlatHistogram;
import java.util.Arrays;
import java.util.List;
import java.util.Random;


public class WordpressUtil {

    static List<String> immutablePostTypeList = Arrays.asList("post", "page");

    public static String generatePostContent(Random rand, int uid) {
        FlatHistogram<Integer> w_contentLength = new FlatHistogram<>(rand, PostHistogram.CONTENT_LENGTH);
        int content_len = w_contentLength.nextValue().intValue();
        String content = TextGenerator.randomStr(rand, content_len) + "[ " + uid + " ]";

        String content_format = "<!-- wp:paragraph -->" +
                "<p>%s</p>" +
                "<!-- /wp:paragraph -->";

        return String.format(content_format, content);
    }

    public static String generatePostTitle(Random rand) {
        FlatHistogram<Integer> p_titleLength = new FlatHistogram<>(rand, PostHistogram.TITLE_LENGTH);
        int titleLength = p_titleLength.nextValue();
        return TextGenerator.randomStr(rand, titleLength);
    }

    public static String generateRandomURL (Random rand) {
        int num = rand.nextInt(10000);
        int len = rand.nextInt(32);
        String params = TextGenerator.randomStr(rand, len);
        return String.format("http://%s: %d/?p=%d", params, num, num);
    }

    public static String generateCommentContent(Random rand) {
        FlatHistogram<Integer> cLength = new FlatHistogram<>(rand, PostHistogram.COMMENT_LENGTH);
        return TextGenerator.randomStr(rand, cLength.nextValue());
    }

    public static String getRandomPostType(Random rand) {
        return immutablePostTypeList.get(rand.nextInt(immutablePostTypeList.size()));
    }

    public static String generateRandomIP (Random rand) {
        return String.format("%d.%d.%d.%d", rand.nextInt(256), rand.nextInt(256),
                rand.nextInt(256),rand.nextInt(256));
    }
}
