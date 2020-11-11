package commons;

import models.GetTweets;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class CommonHelper {

    public static List<Status> buildStatusList(int number, String tweetMode) throws TwitterException {
        List<Status> statuses = new ArrayList<>();
        Status aTestStatus = null;

        for (int position = 0; position < number; position++) {
            String rawJson = "{\"contributors\": null, \"truncated\": false, \"text\": \"\\\"Montreal Indians actually " + tweetMode + "\", \"in_reply_to_status_id\": null, \"random_number\": 0.29391851181222817, \"id\": 373208832580648960, \"favorite_count\": 0, \"source\": \"<a href=\\\"http://twitter.com/tweetbutton\\\" rel=\\\"nofollow\\\">Tweet Button</a>\", \"retweeted\": false, \"coordinates\": null, \"entities\": {\"symbols\": [], \"user_mentions\": [], \"hashtags\": [{\"indices\": [29, 35], \"text\": \"Syria\"}, {\"indices\": [47, 52], \"text\": \"Iraq\"}, {\"indices\": [109, 120], \"text\": \"propaganda\"}, {\"indices\": [121, 132], \"text\": \"MiddleEast\"}, {\"indices\": [133, 137], \"text\": \"war\"}], \"urls\": [{\"url\": \"http://t.co/FQU4QMIxPF\", \"indices\": [86, 108], \"expanded_url\": \"http://huff.to/1dinit0\", \"display_url\": \"huff.to/1dinit0\"}]}, \"in_reply_to_screen_name\": null, \"id_str\": \"373208832580648960\", \"retweet_count\": 0, \"in_reply_to_user_id\": null, \"favorited\": false, \"user\": {\"follow_request_sent\": null, \"profile_use_background_image\": true, \"geo_enabled\": false, \"verified\": false, \"profile_image_url_https\": \"https://si0.twimg.com/profile_images/3537112264/5ebce8651eb68383030dc01836215da1_normal.jpeg\", \"profile_sidebar_fill_color\": \"FFF7CC\", \"id\": 1360644582, \"profile_text_color\": \"0C3E53\", \"followers_count\": 27, \"profile_sidebar_border_color\": \"F2E195\", \"location\": \"Detroit \\u2663 Toronto\", \"default_profile_image\": false, \"id_str\": \"1360644582\", \"utc_offset\": -14400, \"statuses_count\": 1094, \"description\": \"Exorcising the sins of personal ignorance and accepted lies through reductionist analysis. Politics, economics, and science posts can be found here.\", \"friends_count\": 81, \"profile_link_color\": \"FF0000\", \"profile_image_url\": \"http://a0.twimg.com/profile_images/3537112264/5ebce8651eb68383030dc01836215da1_normal.jpeg\", \"notifications\": null, \"profile_background_image_url_https\": \"https://si0.twimg.com/images/themes/theme12/bg.gif\", \"profile_background_color\": \"BADFCD\", \"profile_banner_url\": \"https://pbs.twimg.com/profile_banners/1360644582/1366247104\", \"profile_background_image_url\": \"http://a0.twimg.com/images/themes/theme12/bg.gif\", \"name\": \"Neil Cheddie\", \"lang\": \"en\", \"following\": null, \"profile_background_tile\": false, \"favourites_count\": 4, \"screen_name\": \"Centurion480\", \"url\": null, \"created_at\": \"Thu Apr 18 00:34:18 +0000 2013\", \"contributors_enabled\": false, \"time_zone\": \"Eastern Time (US & Canada)\", \"protected\": false, \"default_profile\": false, \"is_translator\": false, \"listed_count\": 2}, \"geo\": null, \"in_reply_to_user_id_str\": null, \"possibly_sensitive\": false, \"lang\": \"en\", \"created_at\": \"Thu Aug 29 22:21:34 +0000 2013\", \"filter_level\": \"medium\", \"in_reply_to_status_id_str\": null, \"place\": null, \"_id\": {\"$oid\": \"521fc96edbef20c5d84b2dd8\"}}";
            // System.out.println(rawJson);
            aTestStatus = TwitterObjectFactory.createStatus(rawJson);
            statuses.add(aTestStatus);
        }
        return statuses;
    }

    public static Query queryBuilder(String searchString) {

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        //searchString = searchString + timestamp;
        Query inputQuery = new Query(searchString + " -filter:retweets");
        inputQuery.count(10);
        inputQuery.lang("en");
        return inputQuery;

    }

    public static List<Status> createMockTweets(int happy, int sad,int neutral) throws TwitterException {
        List<Status> fakeTweetsHappy;
        List<Status> fakeTweetsSad;
        List<Status> fakeTweetsNeutral;
        List<Status> fakeTweets = new ArrayList<>();


        if (happy>0) {
            fakeTweetsHappy = buildStatusList(happy, "HAPPY");
            fakeTweets.addAll(fakeTweetsHappy);
        }
        if (sad>0) {
            fakeTweetsSad = buildStatusList(sad, "SAD");
            fakeTweets.addAll(fakeTweetsSad);
        }
        if (neutral>0) {
            fakeTweetsNeutral = buildStatusList(neutral, "NEUTRAL");
            fakeTweets.addAll(fakeTweetsNeutral);
        }
        //System.out.println(fakeTweets);
        return fakeTweets;
    }

    public static Twitter  getBadTwitterInstance(){


        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey("NOT A VALID AUTH TOKEN")
                .setOAuthConsumerSecret("a secret")
                .setOAuthAccessToken("a token")
                .setOAuthAccessTokenSecret("a secret");

        TwitterFactory tf = new TwitterFactory(cb.build());
        Twitter twitter = tf.getSingleton();
        return twitter;

    }

}

