package models;

import com.google.common.collect.ImmutableMap;
import org.eclipse.jetty.util.Callback;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;
import play.test.Helpers;
import play.test.WithApplication;
import twitter4j.*;
import twitter4j.api.SearchResource;
import twitter4j.conf.ConfigurationBuilder;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Stream;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.GET;
import static play.test.Helpers.POST;
import static play.test.Helpers.route;
import static play.test.Helpers.*;

public class GetTweetsTest extends WithApplication {

    private static String testKeyWord = "america";

    static int maxSize = 250;
    private static CompletableFuture<QueryResult> aCachedQueryResult;

    @Mock
    private static Twitter twitter = mock(Twitter.class);
    @Mock
    private static Status status = mock(Status.class);

    @Mock

    private static QueryResult queryResult = mock(QueryResult.class);

    @InjectMocks
    private GetTweets AGetTweet;


    private static List<Status> results = new ArrayList<>();

    @Override
    protected Application provideApplication() {
        return new GuiceApplicationBuilder().build();

    }

    @Test
    public void testGetTweets_keyword() throws ExecutionException, InterruptedException, TwitterException {

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        testKeyWord=testKeyWord+timestamp;
        GetTweets gt = new GetTweets(twitter);
        Query inputQuery = new Query(testKeyWord+ " -filter:retweets");
        inputQuery.count(maxSize);
        inputQuery.lang("en");

        //Creats some fake tweets
        List<Status> fakeTweetsHappy=buildStatusList(7,"HAPPY");
        List<Status> fakeTweetsSad=buildStatusList(7,"SAD");
        List<Status> fakeTweets =new ArrayList<>();
        fakeTweets.addAll(fakeTweetsHappy);
        fakeTweets.addAll(fakeTweetsSad);
        //Creats some fake Ends

        //Mocking
        when(twitter.search(inputQuery)).thenReturn(queryResult);
        when(queryResult.getTweets()).thenReturn(fakeTweets);
        when(queryResult.getQuery()).thenReturn(testKeyWord);
        //Mocking Ends

        CompletionStage<String> a= gt.GetTweets_keyword(testKeyWord);
        System.out.println(a.toCompletableFuture().get());
        assertThat(a.toCompletableFuture().get().toString(),containsString("Montreal"));

    }




    @Test
    public void testSadSentiments() throws ExecutionException, InterruptedException, TwitterException {

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        testKeyWord=testKeyWord+timestamp;
        GetTweets gt = new GetTweets(twitter);
        Query inputQuery = new Query(testKeyWord+ " -filter:retweets");
        inputQuery.count(maxSize);
        inputQuery.lang("en");

        //Creats some fake tweets
        List<Status> fakeTweetsHappy=buildStatusList(1,"HAPPY");
        List<Status> fakeTweetsSad=buildStatusList(10,"SAD");
        List<Status> fakeTweets =new ArrayList<>();
        fakeTweets.addAll(fakeTweetsHappy);
        fakeTweets.addAll(fakeTweetsSad);
        //Creats some fake Ends

        //Mocking
        when(twitter.search(inputQuery)).thenReturn(queryResult);
        when(queryResult.getTweets()).thenReturn(fakeTweets);
        when(queryResult.getQuery()).thenReturn(testKeyWord);
        //Mocking Ends

        CompletionStage<String> a= gt.GetTweets_keyword(testKeyWord);
        System.out.println(a.toCompletableFuture().get());
        assertThat(a.toCompletableFuture().get().toString(),containsString("Tweets are SAD"));

    }

    @Test
    public void testHappyentiments() throws ExecutionException, InterruptedException, TwitterException {

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        testKeyWord=testKeyWord+timestamp;
        GetTweets gt = new GetTweets(twitter);
        Query inputQuery = new Query(testKeyWord+ " -filter:retweets");
        inputQuery.count(maxSize);
        inputQuery.lang("en");

        //Creats some fake tweets
        List<Status> fakeTweetsHappy=buildStatusList(20,"HAPPY");
        List<Status> fakeTweetsSad=buildStatusList(5,"SAD");
        List<Status> fakeTweets =new ArrayList<>();
        fakeTweets.addAll(fakeTweetsHappy);
        fakeTweets.addAll(fakeTweetsSad);
        //Creats some fake Ends

        //Mocking
        when(twitter.search(inputQuery)).thenReturn(queryResult);
        when(queryResult.getTweets()).thenReturn(fakeTweets);
        when(queryResult.getQuery()).thenReturn(testKeyWord);
        //Mocking Ends

        CompletionStage<String> a= gt.GetTweets_keyword(testKeyWord);
        System.out.println(a.toCompletableFuture().get());
        assertThat(a.toCompletableFuture().get(),containsString("Tweets are HAPPY"));

    }


    @Test
    public void testGetTweets_keyword_testNull() throws ExecutionException, InterruptedException, TwitterException {

        GetTweets gt = new GetTweets(twitter);
        CompletionStage<String> a= gt.GetTweets_keyword("a");
        assertThat(a.toCompletableFuture().get(),containsString("Cannot process empty string"));

    }

  /*  @Test
    public void call_actual_for_sentiments() throws TwitterException {

        Twitter twitter = new TwitterFactory().getInstance();
        GetTweets gt= new GetTweets(twitter);
        CompletionStage<String> a=gt.GetTweets_keyword("donald trump");

		GetTweets gt1 = new GetTweets(twitter1);
		CompletableFuture<QueryResult> statuses = gt.invokeTwitterServer(new Query("a thing"));
		assertNull(statuses.toCompletableFuture().get());
*/
    private List<Status> buildStatusList(int number,String tweetMode) throws TwitterException {
        List<Status> statuses = new ArrayList<>();
        Status aTestStatus = null;

        for (int position = 0; position < number; position++) {
            String rawJson = "{\"contributors\": null, \"truncated\": false, \"text\": \"\\\"Montreal Indians "+tweetMode+"\", \"in_reply_to_status_id\": null, \"random_number\": 0.29391851181222817, \"id\": 373208832580648960, \"favorite_count\": 0, \"source\": \"<a href=\\\"http://twitter.com/tweetbutton\\\" rel=\\\"nofollow\\\">Tweet Button</a>\", \"retweeted\": false, \"coordinates\": null, \"entities\": {\"symbols\": [], \"user_mentions\": [], \"hashtags\": [{\"indices\": [29, 35], \"text\": \"Syria\"}, {\"indices\": [47, 52], \"text\": \"Iraq\"}, {\"indices\": [109, 120], \"text\": \"propaganda\"}, {\"indices\": [121, 132], \"text\": \"MiddleEast\"}, {\"indices\": [133, 137], \"text\": \"war\"}], \"urls\": [{\"url\": \"http://t.co/FQU4QMIxPF\", \"indices\": [86, 108], \"expanded_url\": \"http://huff.to/1dinit0\", \"display_url\": \"huff.to/1dinit0\"}]}, \"in_reply_to_screen_name\": null, \"id_str\": \"373208832580648960\", \"retweet_count\": 0, \"in_reply_to_user_id\": null, \"favorited\": false, \"user\": {\"follow_request_sent\": null, \"profile_use_background_image\": true, \"geo_enabled\": false, \"verified\": false, \"profile_image_url_https\": \"https://si0.twimg.com/profile_images/3537112264/5ebce8651eb68383030dc01836215da1_normal.jpeg\", \"profile_sidebar_fill_color\": \"FFF7CC\", \"id\": 1360644582, \"profile_text_color\": \"0C3E53\", \"followers_count\": 27, \"profile_sidebar_border_color\": \"F2E195\", \"location\": \"Detroit \\u2663 Toronto\", \"default_profile_image\": false, \"id_str\": \"1360644582\", \"utc_offset\": -14400, \"statuses_count\": 1094, \"description\": \"Exorcising the sins of personal ignorance and accepted lies through reductionist analysis. Politics, economics, and science posts can be found here.\", \"friends_count\": 81, \"profile_link_color\": \"FF0000\", \"profile_image_url\": \"http://a0.twimg.com/profile_images/3537112264/5ebce8651eb68383030dc01836215da1_normal.jpeg\", \"notifications\": null, \"profile_background_image_url_https\": \"https://si0.twimg.com/images/themes/theme12/bg.gif\", \"profile_background_color\": \"BADFCD\", \"profile_banner_url\": \"https://pbs.twimg.com/profile_banners/1360644582/1366247104\", \"profile_background_image_url\": \"http://a0.twimg.com/images/themes/theme12/bg.gif\", \"name\": \"Neil Cheddie\", \"lang\": \"en\", \"following\": null, \"profile_background_tile\": false, \"favourites_count\": 4, \"screen_name\": \"Centurion480\", \"url\": null, \"created_at\": \"Thu Apr 18 00:34:18 +0000 2013\", \"contributors_enabled\": false, \"time_zone\": \"Eastern Time (US & Canada)\", \"protected\": false, \"default_profile\": false, \"is_translator\": false, \"listed_count\": 2}, \"geo\": null, \"in_reply_to_user_id_str\": null, \"possibly_sensitive\": false, \"lang\": \"en\", \"created_at\": \"Thu Aug 29 22:21:34 +0000 2013\", \"filter_level\": \"medium\", \"in_reply_to_status_id_str\": null, \"place\": null, \"_id\": {\"$oid\": \"521fc96edbef20c5d84b2dd8\"}}";
            System.out.println(rawJson);
            aTestStatus = TwitterObjectFactory.createStatus(rawJson);
            statuses.add(aTestStatus);
        }
        return statuses;
    }
}
