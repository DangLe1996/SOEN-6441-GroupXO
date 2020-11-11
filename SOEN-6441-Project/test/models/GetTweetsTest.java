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

import static commons.CommonHelper.buildStatusList;
import static commons.CommonHelper.queryBuilder;
import static commons.CommonHelper.createMockTweets;
import static commons.CommonHelper.getBadTwitterInstance;

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

    @Test
    public void testGetTweets_keyword() throws ExecutionException, InterruptedException, TwitterException {

        String testKeyWord="concordia";
        Query inputQuery=queryBuilder(testKeyWord);
        List<Status> fakeTweets =createMockTweets(7,7,0);

        //Mocking
        when(twitter.search(inputQuery)).thenReturn(queryResult);
        when(queryResult.getTweets()).thenReturn(fakeTweets);
        when(queryResult.getQuery()).thenReturn(testKeyWord);
        //Mocking Ends
        GetTweets gt = new GetTweets(twitter);
        CompletionStage<String> a= gt.GetTweets_keyword(testKeyWord);
        assertThat(a.toCompletableFuture().get().toString(),containsString("Montreal"));
        
  
    }
    
    @Test
    public void testGetTweets_keyword_cache() throws ExecutionException, InterruptedException, TwitterException {

        String testKeyWord="concordia";
        Query inputQuery=queryBuilder(testKeyWord);
        List<Status> fakeTweets =createMockTweets(7,7,0);
        GetTweets gt = new GetTweets(twitter);
        //Mocking
        when(twitter.search(inputQuery)).thenReturn(queryResult);
        when(queryResult.getTweets()).thenReturn(fakeTweets);
        when(queryResult.getQuery()).thenReturn(testKeyWord);
        //Mocking Ends
        gt.GlobalCache.put(testKeyWord, "Montreal");
        CompletionStage<String> a= gt.GetTweets_keyword(testKeyWord);
        assertThat(a.toCompletableFuture().get().toString(),containsString("Montreal"));
        

    }

    @Test
    public void testGetKeywordStats() throws ExecutionException, InterruptedException, TwitterException {
        String testKeyWord="concordia";
        Query inputQuery=queryBuilder(testKeyWord);
        List<Status> fakeTweets =createMockTweets(7,7,0);
        GetTweets gt = new GetTweets(twitter);


        //Mocking
        when(twitter.search(inputQuery)).thenReturn(queryResult);
        when(queryResult.getTweets()).thenReturn(fakeTweets);
        when(queryResult.getQuery()).thenReturn(testKeyWord);
        //Mocking Ends

        CompletionStage<List<String>> a= gt.GetKeywordStats(testKeyWord);
        
        List<String> wordstat = new ArrayList<>();
        wordstat.add("Indians:14\n");
        wordstat.add("HAPPY:7\n");
        wordstat.add("SAD:7\n");
        assertEquals(a.toCompletableFuture().get().toString(),wordstat.toString());

    }
    @Test
    public void testGetKeywordStats_cache() throws ExecutionException, InterruptedException, TwitterException {

        String testKeyWord="concordia";
        Query inputQuery=queryBuilder(testKeyWord);
        List<Status> fakeTweets =createMockTweets(7,7,0);
        GetTweets gt = new GetTweets(twitter);
        //Mocking
        when(twitter.search(inputQuery)).thenReturn(queryResult);
        when(queryResult.getTweets()).thenReturn(fakeTweets);
        when(queryResult.getQuery()).thenReturn(testKeyWord);
        //Mocking Ends
        List<String> wordstat = new ArrayList<>();
        wordstat.add("Indians:14\n");
        wordstat.add("HAPPY:7\n");
        wordstat.add("SAD:7\n");
        gt.GlobalWordStatsCache.put(testKeyWord,wordstat);
        CompletionStage<List<String>> a= gt.GetKeywordStats(testKeyWord);
        assertEquals(a.toCompletableFuture().get().toString(),wordstat.toString());

    }


    @Test
    public void testSadSentiments() throws ExecutionException, InterruptedException, TwitterException {

        String testKeyWord="concordia sad";
        Query inputQuery=queryBuilder(testKeyWord);
        List<Status> fakeTweets =createMockTweets(1,10,0);
        GetTweets gt = new GetTweets(twitter);

        //Mocking
        when(twitter.search(inputQuery)).thenReturn(queryResult);
        when(queryResult.getTweets()).thenReturn(fakeTweets);
        when(queryResult.getQuery()).thenReturn(testKeyWord);
        //Mocking Ends

        CompletionStage<String> a= gt.GetTweets_keyword(testKeyWord);

        assertThat(a.toCompletableFuture().get().toString(),containsString("Tweets are SAD"));

    }

    @Test
    public void testHappySentiments() throws ExecutionException, InterruptedException, TwitterException {

        String testKeyWord="concordia happy again";
        Query inputQuery=queryBuilder(testKeyWord);
        List<Status> fakeTweets =createMockTweets(20,5,0);
        GetTweets gt = new GetTweets(twitter);
        //Mocking
        when(twitter.search(inputQuery)).thenReturn(queryResult);
        when(queryResult.getTweets()).thenReturn(fakeTweets);
        when(queryResult.getQuery()).thenReturn(testKeyWord);
        //Mocking Ends

        CompletionStage<String> a= gt.GetTweets_keyword(testKeyWord);

        assertThat(a.toCompletableFuture().get(),containsString("Tweets are HAPPY"));

    }

    @Test
    public void testHappySentimentsZERO() throws ExecutionException, InterruptedException, TwitterException {

        String testKeyWord="concordia happy";
        Query inputQuery=queryBuilder(testKeyWord);
        List<Status> fakeTweets =createMockTweets(0,0,1);
        GetTweets gt = new GetTweets(twitter);
        //Mocking
        when(twitter.search(inputQuery)).thenReturn(queryResult);
        when(queryResult.getTweets()).thenReturn(fakeTweets);
        when(queryResult.getQuery()).thenReturn(testKeyWord);
        //Mocking Ends

        CompletionStage<String> a= gt.GetTweets_keyword(testKeyWord);

        assertThat(a.toCompletableFuture().get(),containsString("Tweets are NEUTRAL"));

    }


    @Test
    public void testGetTweets_keyword_testNull() throws ExecutionException, InterruptedException, TwitterException {

        GetTweets gt = new GetTweets(twitter);
        CompletionStage<String> a= gt.GetTweets_keyword("a");
        assertThat(a.toCompletableFuture().get(),containsString("Cannot process empty string"));

    }

    
    @Test(expected=Exception.class)
    public void check_Twitter_invoke_exception() throws TwitterException, ExecutionException, InterruptedException {


         Twitter twitter=getBadTwitterInstance();
         GetTweets gt=new GetTweets(twitter);
         CompletionStage<String> a= gt.GetTweets_keyword("SHOULD BE EXCEPTION and return null");
         assertEquals(null, a.toCompletableFuture().get());
         //assertThat(a.toCompletableFuture().get(),containsString("Cannot process empty string"));

    }
    
    @Test
    public void testGetTweetSentimentsExp(){
        assertThat(GetTweets.getTweetSentiments("a sentiment"),is("Could not analyse Sentiments Due to Error/less no of Tweets on this Topic"));
    }

    @Test
    public void testGetTweetsParameters() throws TwitterException, ExecutionException, InterruptedException {
        GetTweets gt=new GetTweets();
        assertThat(gt.GetTweets_keyword("a","play1").toCompletableFuture().get(),not("simply a session data"));
    }

    @Test
    public void testUserSessions() throws TwitterException, ExecutionException, InterruptedException {

        testKeyWord="User Session test";
        Query inputQuery=queryBuilder(testKeyWord);

        List<Status> fakeTweets=createMockTweets(1,0,100);
        when(twitter.search(inputQuery)).thenReturn(queryResult);
        when(queryResult.getTweets()).thenReturn(fakeTweets);
        when(queryResult.getQuery()).thenReturn(testKeyWord);
        GetTweets gt=new GetTweets(twitter);
        sessionData s=new sessionData();
        s.cleanUpSessions();
        CompletionStage<sessionData> a= gt.GetTweets_keyword(testKeyWord,null);

        CompletionStage<sessionData> b= gt.GetTweets_keyword(testKeyWord,"sessionData{sessionID='play1'}");
        assertThat(b.toCompletableFuture().get().getCache().toString(),containsString("Montreal"));

    }


    @Test
    public void testNeutralSentiments() throws ExecutionException, InterruptedException, TwitterException {

        testKeyWord="Concordia University";
        Query inputQuery=queryBuilder(testKeyWord);
        List<Status> fakeTweets=createMockTweets(1,0,100);
        //Mocking
        when(twitter.search(inputQuery)).thenReturn(queryResult);
        when(queryResult.getTweets()).thenReturn(fakeTweets);
        when(queryResult.getQuery()).thenReturn(testKeyWord);
        //Mocking Ends
        GetTweets gt = new GetTweets(twitter);
        CompletionStage<String> a= gt.GetTweets_keyword(testKeyWord);
        assertThat(a.toCompletableFuture().get().toString(),containsString("Tweets are NEUTRAL"));

    }



}
