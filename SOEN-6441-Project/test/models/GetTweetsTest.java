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
	// @Autowired
	// GetTweets gt;

	/*
	 * @Test public void test_object_creation() throws TwitterException,
	 * ExecutionException, InterruptedException { GetTweets gt=new GetTweets();
	 * assertTrue(gt instanceof GetTweets);
	 * assertThat(gt.GetTweets_keyword("").toCompletableFuture().get(),
	 * is("Cannot process empty string"));
	 * 
	 * }
	 * 
	 * @Test public void test_invokeTwitterServer_exception() throws
	 * TwitterException, ExecutionException, InterruptedException {
	 * 
	 * ConfigurationBuilder cb = new ConfigurationBuilder();
	 * cb.setDebugEnabled(true).setOAuthConsumerKey("a").setOAuthConsumerSecret("b")
	 * .setOAuthAccessToken("v").setOAuthAccessTokenSecret("d"); TwitterFactory tf =
	 * new TwitterFactory(cb.build()); Twitter twitter1 = tf.getInstance();
	 * 
	 * GetTweets gt=new GetTweets(twitter1);
	 * 
	 * CompletableFuture<QueryResult> statuses = gt.invokeTwitterServer(new
	 * Query("a thing")); assertNull(statuses.toCompletableFuture().get()); }
	 */
	@Test
	public void testGetTweets_keyword() throws ExecutionException, InterruptedException, TwitterException {

		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		testKeyWord = testKeyWord + timestamp;
		GetTweets gt = new GetTweets(twitter);
		Query inputQuery = new Query(testKeyWord + " -filter:retweets");
		inputQuery.count(10);
		inputQuery.lang("en");
		when(twitter.search(inputQuery)).thenReturn(queryResult);
		when(queryResult.getTweets()).thenReturn(buildStatusList(1));
		CompletionStage<String> a = gt.GetTweets_keyword(testKeyWord);
		System.out.println(a.toCompletableFuture().get());
		assertThat(a.toCompletableFuture().get().toString(), containsString("Montreal"));

	}

	@Test
	public void testGetTweets_keyword_exceptions() throws ExecutionException, InterruptedException, TwitterException {
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		GetTweets gt = new GetTweets(twitter);
		testKeyWord = testKeyWord + timestamp;
		Query inputQuery = new Query(testKeyWord + " -filter:retweets");
		inputQuery.count(10);
		inputQuery.lang("en");
		when(twitter.search(inputQuery)).thenReturn(queryResult);
		when(queryResult.getTweets()).thenReturn(buildStatusList(0));
		CompletionStage<String> a = gt.GetTweets_keyword(testKeyWord);
		System.out.println(a.toCompletableFuture().get());
		assertThat(a.toCompletableFuture().get().toString(), not(containsString("a test ResultSet")));

		when(twitter.search(inputQuery)).thenReturn(queryResult);
		when(queryResult.getTweets()).thenReturn(null);
		CompletionStage<String> nullA = gt.GetTweets_keyword(testKeyWord);
		System.out.println("****" + nullA.toCompletableFuture().get());
	}

	@Test
	public void testGetTweets_keyword_cache() throws ExecutionException, InterruptedException, TwitterException {

		// Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		testKeyWord = "create a cache";
		GetTweets gt = new GetTweets(twitter);
		Query inputQuery = new Query(testKeyWord + " -filter:retweets");
		inputQuery.count(10);
		inputQuery.lang("en");
		when(twitter.search(inputQuery)).thenReturn(queryResult);
		when(queryResult.getTweets()).thenReturn(buildStatusList(1));
		CompletionStage<String> a = gt.GetTweets_keyword(testKeyWord);
		System.out.println(a.toCompletableFuture().get());
		assertThat(a.toCompletableFuture().get().toString(), containsString("Montreal"));

		testKeyWord = "create a cache";
		when(queryResult.getTweets()).thenReturn(buildStatusList(0)); // dont fake any result
		CompletionStage<String> aCache = gt.GetTweets_keyword(testKeyWord); // from cache now
		assertThat(aCache.toCompletableFuture().get().toString(), containsString("Montreal"));

	}

	@Test
	public void testGetTweets_keyword_testNull() throws ExecutionException, InterruptedException, TwitterException {

		// testKeyWord="create a cache";
		GetTweets gt = new GetTweets(twitter);
		CompletionStage<String> a = gt.GetTweets_keyword("a");
		assertThat(a.toCompletableFuture().get().toString(), containsString("Cannot process empty string"));

	}

	@Test
	public void test_invokeTwitterServer() throws ExecutionException, InterruptedException, TwitterException {

		GetTweets gt = new GetTweets(twitter);
		Query inputQuery = new Query(testKeyWord + " -filter:retweets");
		QueryResult queryResult = mock(QueryResult.class);
		inputQuery.count(10);
		inputQuery.lang("en");

		when(twitter.search(inputQuery)).thenReturn(queryResult);
		when(queryResult.getTweets()).thenReturn(buildStatusList(1));

		CompletableFuture<QueryResult> a = gt.invokeTwitterServer(inputQuery);
		System.out.println(a.toCompletableFuture().get());
		assertThat(a.toCompletableFuture().get().toString(), containsString("Mock for QueryResult"));
		// System.out.println("TweetText" +
		// a.toCompletableFuture().get().getTweets().toString());
		assertThat(a.toCompletableFuture().get().getTweets().toString(), containsString("Montreal"));
		// exception

		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true).setOAuthConsumerKey("a").setOAuthConsumerSecret("b").setOAuthAccessToken("v")
				.setOAuthAccessTokenSecret("d");
		TwitterFactory tf = new TwitterFactory(cb.build());
		Twitter twitter1 = tf.getInstance();

		GetTweets gt1 = new GetTweets(twitter1);
		CompletableFuture<QueryResult> statuses = gt.invokeTwitterServer(new Query("a thing"));
		assertNull(statuses.toCompletableFuture().get());

	}

	/*
	 * @Test public void testSentiments() throws TwitterException,
	 * ExecutionException, InterruptedException {
	 * 
	 * String testKeyWord ="donald trump";
	 * 
	 * GetTweets gt = new GetTweets(); Query inputQuery = new Query(testKeyWord +
	 * " -filter:retweets"); QueryResult queryResult = mock(QueryResult.class);
	 * inputQuery.count(10); inputQuery.lang("en"); CompletionStage<String> a=
	 * gt.GetTweets_keyword(testKeyWord);
	 * //System.out.println(a.toCompletableFuture().get());
	 * 
	 * 
	 * }
	 * 
	 */
	private List<Status> buildStatusList(int number) throws TwitterException {
		List<Status> statuses = new ArrayList<>();
		Status aTestStatus = null;
		for (int position = 0; position < number; position++) {
			// statuses.add(mock(Status.class));
			String rawJson = "{\"contributors\": null, \"truncated\": false, \"text\": \"\\\"Montreal Indians\", \"in_reply_to_status_id\": null, \"random_number\": 0.29391851181222817, \"id\": 373208832580648960, \"favorite_count\": 0, \"source\": \"<a href=\\\"http://twitter.com/tweetbutton\\\" rel=\\\"nofollow\\\">Tweet Button</a>\", \"retweeted\": false, \"coordinates\": null, \"entities\": {\"symbols\": [], \"user_mentions\": [], \"hashtags\": [{\"indices\": [29, 35], \"text\": \"Syria\"}, {\"indices\": [47, 52], \"text\": \"Iraq\"}, {\"indices\": [109, 120], \"text\": \"propaganda\"}, {\"indices\": [121, 132], \"text\": \"MiddleEast\"}, {\"indices\": [133, 137], \"text\": \"war\"}], \"urls\": [{\"url\": \"http://t.co/FQU4QMIxPF\", \"indices\": [86, 108], \"expanded_url\": \"http://huff.to/1dinit0\", \"display_url\": \"huff.to/1dinit0\"}]}, \"in_reply_to_screen_name\": null, \"id_str\": \"373208832580648960\", \"retweet_count\": 0, \"in_reply_to_user_id\": null, \"favorited\": false, \"user\": {\"follow_request_sent\": null, \"profile_use_background_image\": true, \"geo_enabled\": false, \"verified\": false, \"profile_image_url_https\": \"https://si0.twimg.com/profile_images/3537112264/5ebce8651eb68383030dc01836215da1_normal.jpeg\", \"profile_sidebar_fill_color\": \"FFF7CC\", \"id\": 1360644582, \"profile_text_color\": \"0C3E53\", \"followers_count\": 27, \"profile_sidebar_border_color\": \"F2E195\", \"location\": \"Detroit \\u2663 Toronto\", \"default_profile_image\": false, \"id_str\": \"1360644582\", \"utc_offset\": -14400, \"statuses_count\": 1094, \"description\": \"Exorcising the sins of personal ignorance and accepted lies through reductionist analysis. Politics, economics, and science posts can be found here.\", \"friends_count\": 81, \"profile_link_color\": \"FF0000\", \"profile_image_url\": \"http://a0.twimg.com/profile_images/3537112264/5ebce8651eb68383030dc01836215da1_normal.jpeg\", \"notifications\": null, \"profile_background_image_url_https\": \"https://si0.twimg.com/images/themes/theme12/bg.gif\", \"profile_background_color\": \"BADFCD\", \"profile_banner_url\": \"https://pbs.twimg.com/profile_banners/1360644582/1366247104\", \"profile_background_image_url\": \"http://a0.twimg.com/images/themes/theme12/bg.gif\", \"name\": \"Neil Cheddie\", \"lang\": \"en\", \"following\": null, \"profile_background_tile\": false, \"favourites_count\": 4, \"screen_name\": \"Centurion480\", \"url\": null, \"created_at\": \"Thu Apr 18 00:34:18 +0000 2013\", \"contributors_enabled\": false, \"time_zone\": \"Eastern Time (US & Canada)\", \"protected\": false, \"default_profile\": false, \"is_translator\": false, \"listed_count\": 2}, \"geo\": null, \"in_reply_to_user_id_str\": null, \"possibly_sensitive\": false, \"lang\": \"en\", \"created_at\": \"Thu Aug 29 22:21:34 +0000 2013\", \"filter_level\": \"medium\", \"in_reply_to_status_id_str\": null, \"place\": null, \"_id\": {\"$oid\": \"521fc96edbef20c5d84b2dd8\"}}";
			aTestStatus = TwitterObjectFactory.createStatus(rawJson);

			statuses.add(aTestStatus);
		}
		return statuses;
	}
}
