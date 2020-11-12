package models;

import org.junit.After;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.test.WithApplication;
import twitter4j.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static play.test.Helpers.route;

import static commons.CommonHelper.queryBuilder;
import static commons.CommonHelper.createMockTweets;
import static commons.CommonHelper.getBadTwitterInstance;

public class GetTweetsTest extends WithApplication {

    private static String testKeyWord = "america";

    static int maxSize = 250;
    private static CompletableFuture<QueryResult> aCachedQueryResult;
    @Mock
    private static Twitter twitter = mock(Twitter.class);

    @Mock
    private static QueryResult queryResult = mock(QueryResult.class);



    @After
    public void CleanUp(){
        sessionData.cleanUpSessions();
        GetTweets.GlobalCache.clear();

    }

    @Override
    protected Application provideApplication() {
        return new GuiceApplicationBuilder().build();

    }

    /**
     *
     * Test the GetTweetsWithKeyword from GetTweets with a result of 7 happy and 7 sad tweets.
     * Expected result contains neutral tweet sentimental
     * The fake tweets should contain a word montreal.
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws TwitterException
     */
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
        CompletionStage<String> a= gt.GetTweetsWithKeyword(testKeyWord);


        assertThat(a.toCompletableFuture().get(),containsString("Montreal"));



    }

    /**
     * Test caching system. Once a testkeyword already exist in Cache, the twitter API should not be called on again.
     * In this test, the GetTweet object is not injected with mock Twitter, so it will raise an error if
     * the method call Tiwtter API.
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws TwitterException
     */
    @Test
    public void testGetTweets_keyword_cache() throws TwitterException, ExecutionException, InterruptedException {

        String testKeyWord="concordia";
        GetTweets gt = new GetTweets(twitter);
        gt.GlobalCache.put(testKeyWord, "Montreal");
        CompletionStage<String> a= gt.GetTweetsWithKeyword(testKeyWord);
        assertThat(a.toCompletableFuture().get(),containsString("Montreal"));


    }

    @Test
    public void testGetKeywordStats() throws ExecutionException, InterruptedException, TwitterException {
        String testKeyWord="concordia";
        Query inputQuery=queryBuilder(testKeyWord);
        List<Status> fakeTweets =createMockTweets(7,7,0);

        //Mocking

        when(queryResult.getTweets()).thenReturn(fakeTweets);
        when(queryResult.getQuery()).thenReturn(testKeyWord);
        when(twitter.search(inputQuery)).thenReturn(queryResult);
        //Mocking Ends
        GetTweets gt = new GetTweets(twitter);
        CompletionStage<List<String>> a= gt.GetKeywordStats(testKeyWord);

        List<String> wordstat = new ArrayList<>();
        wordstat.add("Indians:14\n");
        wordstat.add("HAPPY:7\n");
        wordstat.add("SAD:7\n");
        assertEquals(a.toCompletableFuture().get().toString(),wordstat.toString());

    }

    /**
     * Test Word Statistic caching system. If a keyword have already been statistically analyzed,
     * it should not be invoked from twitter and analyzed again but rather retrieve from cache.
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws TwitterException
     */
    @Test
    public void testGetKeywordStats_cache() throws ExecutionException, InterruptedException, TwitterException {

        String testKeyWord="concordia";
        GetTweets gt = new GetTweets(twitter);
        List<String> wordstat = new ArrayList<>();
        wordstat.add("Indians:14\n");
        wordstat.add("HAPPY:7\n");
        wordstat.add("SAD:7\n");
        gt.GlobalWordStatsCache.put(testKeyWord,wordstat);
        CompletionStage<List<String>> a= gt.GetKeywordStats(testKeyWord);
        assertEquals(a.toCompletableFuture().get().toString(),wordstat.toString());

    }


    /**
     * Test the sentimental analysis, using 1 happy and 10 sad tweets,
     * expected result is "Tweets are SAD"
     */
    @Test
    public void testSadSentiments()   {

        String testKeyWord="concordia sad";
        Query inputQuery=queryBuilder(testKeyWord);
        List<Status> fakeTweets =createMockTweets(1,10,0);
        GetTweets gt = new GetTweets(twitter);

        //Mocking
        try {
            when(twitter.search(inputQuery)).thenReturn(queryResult);
        }
        catch (TwitterException ex){
            System.out.println("Twitter couldn't be mocked with error : " + ex.getErrorMessage());
        }
        when(queryResult.getTweets()).thenReturn(fakeTweets);
        when(queryResult.getQuery()).thenReturn(testKeyWord);
        //Mocking Ends

        CompletionStage<String> a= gt.GetTweetsWithKeyword(testKeyWord);
        try {
            assertThat(a.toCompletableFuture().get(), containsString("Tweets are SAD"));
        }
        catch (InterruptedException ex){
            System.out.println("System was interrupted: " + ex.getMessage());
        }
        catch (ExecutionException ex){
            System.out.println("Execution exception : "+ ex.getLocalizedMessage());
        }

    }


    /**
     * Test the sentimental analysis, using 20 happy and 5 sad tweets,
     * expected result is "Tweets are HAPPY"
     */
    @Test
    public void testHappySentiments()  {

        String testKeyWord="concordia happy again";
        Query inputQuery=queryBuilder(testKeyWord);
        List<Status> fakeTweets =createMockTweets(20,5,0);
        GetTweets gt = new GetTweets(twitter);
        //Mocking
        try {
            when(twitter.search(inputQuery)).thenReturn(queryResult);
        }
        catch (TwitterException ex){
            System.out.println("Twitter couldn't be mocked with error : " + ex.getErrorMessage());
        }
        when(queryResult.getTweets()).thenReturn(fakeTweets);
        when(queryResult.getQuery()).thenReturn(testKeyWord);
        //Mocking Ends

        CompletionStage<String> a= gt.GetTweetsWithKeyword(testKeyWord);

        try {
            assertThat(a.toCompletableFuture().get(), containsString("Tweets are HAPPY"));
        }
        catch (InterruptedException ex){
            System.out.println("System was interrupted: " + ex.getMessage());
        }
        catch (ExecutionException ex){
            System.out.println("Execution exception : "+ ex.getLocalizedMessage());
        }

    }

    /**
     * Test neutral sentimental.
     */
    @Test
    public void testHappySentimentsZERO()  {

        String testKeyWord="concordia happy";
        Query inputQuery=queryBuilder(testKeyWord);
        List<Status> fakeTweets =createMockTweets(0,0,1);
        GetTweets gt = new GetTweets(twitter);
        //Mocking
        try {
            when(twitter.search(inputQuery)).thenReturn(queryResult);
        }
        catch (TwitterException ex){
            System.out.println("Twitter couldn't be mocked with error : " + ex.getErrorMessage());
        }
        when(queryResult.getTweets()).thenReturn(fakeTweets);
        when(queryResult.getQuery()).thenReturn(testKeyWord);
        //Mocking Ends

        CompletionStage<String> a= gt.GetTweetsWithKeyword(testKeyWord);

        try {
            assertThat(a.toCompletableFuture().get(), containsString("Tweets are NEUTRAL"));
        }
        catch (InterruptedException ex){
            System.out.println("System was interrupted: " + ex.getMessage());
        }
        catch (ExecutionException ex){
            System.out.println("Execution exception : "+ ex.getLocalizedMessage());
        }

    }


    /**
     * If user try to find a single character, return error message saying cannot process empty string.
     */
    @Test
    public void testGetTweets_keyword_testNull()  {

        GetTweets gt = new GetTweets(twitter);
        try {
            CompletionStage<String> a = gt.GetTweetsWithKeyword("a");
            assertThat(a.toCompletableFuture().get(),containsString("Cannot process empty string"));
        }
        catch (InterruptedException ex){
            System.out.println("System was interrupted: " + ex.getMessage());
        }
        catch (ExecutionException ex){
            System.out.println("Execution exception : "+ ex.getLocalizedMessage());
        }


    }


    /**
     * Test bad twitter authentication data.
     */
    @Test()
    public void check_Twitter_invoke_exception()  {


         Twitter twitter=getBadTwitterInstance();
         GetTweets gt=new GetTweets(twitter);
         CompletionStage<String> a= gt.GetTweetsWithKeyword("SHOULD BE EXCEPTION and return null");
         try {
             System.out.println(a.toCompletableFuture().get());
             assertThat(a.toCompletableFuture().get(),is("java.lang.NullPointerException"));
         }
         catch (InterruptedException ex){
             System.out.println("System was interrupted: " + ex.getMessage());
         }
         catch (ExecutionException ex){
             System.out.println("Execution exception in test : "+ ex.getLocalizedMessage());
         }


    }

    /**
     * Test empty tweets sentimental
     */
    @Test
    public void testGetTweetSentimentsExp(){
        assertThat(GetTweets.getTweetSentiments("a sentiment"),is("Could not analyse Sentiments Due to Error/less no of Tweets on this Topic"));
    }


    /**
     *
     * Test getting tweet with a new user. Expected result is a valid search result with
     * new user being created.
     */
    @Test
    public void testUserSessionsWithGetString()  {


        testKeyWord="User Session test";
        Query inputQuery=queryBuilder(testKeyWord);

        List<Status> fakeTweets=createMockTweets(1,0,100);
        try {
            when(twitter.search(inputQuery)).thenReturn(queryResult);
        }
        catch (TwitterException ex){
            System.out.println("Twitter couldn't be mocked with error : " + ex.getErrorMessage());

        }
        when(queryResult.getTweets()).thenReturn(fakeTweets);
        when(queryResult.getQuery()).thenReturn(testKeyWord);
        GetTweets gt=new GetTweets(twitter);
        sessionData s=new sessionData();

        CompletionStage<sessionData> b= gt.GetTweetsWithUser(testKeyWord,s.toString());
        try {
            assertThat(b.toCompletableFuture().get().getCache().toString(), containsString("Montreal"));
        }
        catch (InterruptedException ex){
            System.out.println("System was interrupted: " + ex.getMessage());
        }
        catch (ExecutionException ex){
            System.out.println("Execution exception in test : "+ ex.getLocalizedMessage());
        }

    }

    /**
     * Test get result from String from Global Cache
     */
    @Test
    public void testUserSessionsWithGetStringFromGlobalCache()  {

        testKeyWord="User Session test";
        String testResult = "This is a test";
        GetTweets gt=new GetTweets(twitter);
        sessionData s=new sessionData();
        s.cleanUpSessions();

        gt.GlobalCache.put(testKeyWord,"This is a test");

        CompletionStage<sessionData> b= gt.GetTweetsWithUser(testKeyWord,"sessionData{sessionID='play1'}");
        try {
            assertThat(b.toCompletableFuture().get().getCache().get(testKeyWord), is(testResult));
        }
        catch (InterruptedException ex){
            System.out.println("System was interrupted: " + ex.getMessage());
        }
        catch (ExecutionException ex){
            System.out.println("Execution exception in test : "+ ex.getLocalizedMessage());
        }

    }

    /**
     * Test get result from String from local Cache
     */
    @Test
    public void testUserSessionsWithGetStringFromLocalCache()  {

        testKeyWord="User Session test";
        String testResult = "This is a test";
        GetTweets gt=new GetTweets(twitter);
        sessionData s=new sessionData();

        gt.GlobalCache.put(testKeyWord,testResult);

        s.insertCache("test1", "this is test 1");
        s.insertCache("test2", "this is test 2");
        s.insertCache(testKeyWord, testResult);

        CompletionStage<sessionData> b= gt.GetTweetsWithUser(testKeyWord,s.toString());
        try {
            assertThat(b.toCompletableFuture().get().getCache().get(testKeyWord), is(testResult));
            assertThat(s.getQuery().get(0),is(testKeyWord));
        }
        catch (InterruptedException ex){
            System.out.println("System was interrupted: " + ex.getMessage());
        }
        catch (ExecutionException ex){
            System.out.println("Execution exception in test : "+ ex.getLocalizedMessage());
        }

    }

    /**
     * Test that if entered an empty string, return the same user with no new query added.
     */
    @Test
    public void testUserSessionsWithEmptyString()  {

        testKeyWord="User Session test";
        String testResult = "This is a test";
        GetTweets gt=new GetTweets(twitter);
        sessionData s=new sessionData();

        gt.GlobalCache.put(testKeyWord,testResult);

        s.insertCache("test1", "this is test 1");
        s.insertCache("test2", "this is test 2");
        s.insertCache(testKeyWord, testResult);

        CompletionStage<sessionData> b= gt.GetTweetsWithUser(" ",s.toString());
        try {
            assertThat(b.toCompletableFuture().get().getQuery().size(),is(3));
        }
        catch (InterruptedException ex){
            System.out.println("System was interrupted: " + ex.getMessage());
        }
        catch (ExecutionException ex){
            System.out.println("Execution exception in test : "+ ex.getLocalizedMessage());
        }

    }





}
