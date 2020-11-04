package models;


import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import org.mockito.stubbing.OngoingStubbing;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.test.WithApplication;
import twitter4j.*;
import org.junit.Test;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.test.WithApplication;
import twitter4j.conf.ConfigurationBuilder;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.containsString;

@RunWith(MockitoJUnitRunner.class)
public class GetTweetsTest extends WithApplication {

    private static final String SIMPLE_QUERY_STRING = "code for concordia 3";
    private static final int SIMPLE_RETURN_ZERO = 0;
    private static final int SIMPLE_RETURN_ONE = 1;

    @Mock
    private Twitter twitter;

    @InjectMocks
    private GetTweets aGetTwitter = null;

    @Before
    public void setUp() throws Exception {

        aGetTwitter = new GetTweets(twitter);
    }

    @After
    public void tearDown() throws Exception {

        aGetTwitter = null;
    }


    @Override
    protected Application provideApplication() {
        return new GuiceApplicationBuilder().build();
    }

    private List<Status> buildStatusList(int number) throws TwitterException {
        List<Status> statuses = new ArrayList<>();
        for (int position = 0; position < number; position++) {
            //statuses.add(mock(Status.class));
            String rawJson = "{\"contributors\": null, \"truncated\": false, \"text\": \"\\\"Montreal Indians\", \"in_reply_to_status_id\": null, \"random_number\": 0.29391851181222817, \"id\": 373208832580648960, \"favorite_count\": 0, \"source\": \"<a href=\\\"http://twitter.com/tweetbutton\\\" rel=\\\"nofollow\\\">Tweet Button</a>\", \"retweeted\": false, \"coordinates\": null, \"entities\": {\"symbols\": [], \"user_mentions\": [], \"hashtags\": [{\"indices\": [29, 35], \"text\": \"Syria\"}, {\"indices\": [47, 52], \"text\": \"Iraq\"}, {\"indices\": [109, 120], \"text\": \"propaganda\"}, {\"indices\": [121, 132], \"text\": \"MiddleEast\"}, {\"indices\": [133, 137], \"text\": \"war\"}], \"urls\": [{\"url\": \"http://t.co/FQU4QMIxPF\", \"indices\": [86, 108], \"expanded_url\": \"http://huff.to/1dinit0\", \"display_url\": \"huff.to/1dinit0\"}]}, \"in_reply_to_screen_name\": null, \"id_str\": \"373208832580648960\", \"retweet_count\": 0, \"in_reply_to_user_id\": null, \"favorited\": false, \"user\": {\"follow_request_sent\": null, \"profile_use_background_image\": true, \"geo_enabled\": false, \"verified\": false, \"profile_image_url_https\": \"https://si0.twimg.com/profile_images/3537112264/5ebce8651eb68383030dc01836215da1_normal.jpeg\", \"profile_sidebar_fill_color\": \"FFF7CC\", \"id\": 1360644582, \"profile_text_color\": \"0C3E53\", \"followers_count\": 27, \"profile_sidebar_border_color\": \"F2E195\", \"location\": \"Detroit \\u2663 Toronto\", \"default_profile_image\": false, \"id_str\": \"1360644582\", \"utc_offset\": -14400, \"statuses_count\": 1094, \"description\": \"Exorcising the sins of personal ignorance and accepted lies through reductionist analysis. Politics, economics, and science posts can be found here.\", \"friends_count\": 81, \"profile_link_color\": \"FF0000\", \"profile_image_url\": \"http://a0.twimg.com/profile_images/3537112264/5ebce8651eb68383030dc01836215da1_normal.jpeg\", \"notifications\": null, \"profile_background_image_url_https\": \"https://si0.twimg.com/images/themes/theme12/bg.gif\", \"profile_background_color\": \"BADFCD\", \"profile_banner_url\": \"https://pbs.twimg.com/profile_banners/1360644582/1366247104\", \"profile_background_image_url\": \"http://a0.twimg.com/images/themes/theme12/bg.gif\", \"name\": \"Neil Cheddie\", \"lang\": \"en\", \"following\": null, \"profile_background_tile\": false, \"favourites_count\": 4, \"screen_name\": \"Centurion480\", \"url\": null, \"created_at\": \"Thu Apr 18 00:34:18 +0000 2013\", \"contributors_enabled\": false, \"time_zone\": \"Eastern Time (US & Canada)\", \"protected\": false, \"default_profile\": false, \"is_translator\": false, \"listed_count\": 2}, \"geo\": null, \"in_reply_to_user_id_str\": null, \"possibly_sensitive\": false, \"lang\": \"en\", \"created_at\": \"Thu Aug 29 22:21:34 +0000 2013\", \"filter_level\": \"medium\", \"in_reply_to_status_id_str\": null, \"place\": null, \"_id\": {\"$oid\": \"521fc96edbef20c5d84b2dd8\"}}";
            Status aTestStatus = TwitterObjectFactory.createStatus(rawJson);

            statuses.add(aTestStatus);
        }
        return statuses;
    }

   @Test
    public void test_invokeTwitterServer() throws TwitterException, ExecutionException, InterruptedException {
        QueryResult queryResult = mock(QueryResult.class);
        //QueryResult queryResult = new QueryResult() ;
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        when(twitter.search(new Query(SIMPLE_QUERY_STRING))).thenReturn(queryResult);
        when(queryResult.getTweets()).thenReturn(buildStatusList(SIMPLE_RETURN_ONE));

        CompletableFuture<QueryResult> statuses = aGetTwitter.invokeTwitterServer(new Query(SIMPLE_QUERY_STRING));
        System.out.println(statuses.get().getTweets());
        assertThat(statuses.toCompletableFuture().get().toString(),containsString("Mock for QueryResult"));
        assertThat(statuses.get().getTweets().toString(),containsString("Montreal Indians"));
    }

    @Test
    public void test_invokeTwitterServer_exception() throws TwitterException, ExecutionException, InterruptedException {

   ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey("a")
                .setOAuthConsumerSecret("b")
                .setOAuthAccessToken("v")
                .setOAuthAccessTokenSecret("d");
        TwitterFactory tf = new TwitterFactory(cb.build());
        Twitter twitter1 = tf.getInstance();


        GetTweets gt=new GetTweets(twitter1);
        CompletableFuture<QueryResult> statuses = gt.invokeTwitterServer(new Query("a thing"));
        //System.out.println("a thing?"+statuses.get().getTweets());
        //System.out.println(statuses.get().getTweets());




    }


    @Test
    public void test_object_creation() throws TwitterException, ExecutionException, InterruptedException {
        GetTweets gt=new GetTweets();
        assertTrue(gt instanceof GetTweets);
        assertThat(gt.GetTweets_keyword("").toCompletableFuture().get(),is("Cannot process empty string"));

    }
    @Test
    public void test_GetTweets_keyword_keyword() throws TwitterException, ExecutionException, InterruptedException {

        Timestamp timestamp = new Timestamp(System.currentTimeMillis()); //Dont fethc from cache

        QueryResult queryResult = mock(QueryResult.class);

        when(twitter.search(new Query(SIMPLE_QUERY_STRING))).thenReturn(queryResult);
        when(queryResult.getTweets()).thenReturn(buildStatusList(SIMPLE_RETURN_ONE));


        CompletionStage<String> statuses = aGetTwitter.GetTweets_keyword(SIMPLE_QUERY_STRING);

        System.out.println("Qury result Dummy" + statuses.toCompletableFuture().get());
        //SOME COVERAGE BY MOCKITO
        assertThat(statuses.toCompletableFuture().get().toString(),containsString(SIMPLE_QUERY_STRING));
    }

}
