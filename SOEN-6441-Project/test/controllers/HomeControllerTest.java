package controllers;

import com.google.common.collect.ImmutableMap;
import models.GetTweets;
import models.GetTweetsTest;
import org.eclipse.jetty.util.Callback;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;
import play.test.WithApplication;
import twitter4j.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.GET;
import static play.test.Helpers.POST;
import static play.test.Helpers.route;
import static play.test.Helpers.*;

import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;


@ExtendWith(MockitoExtension.class)
public class HomeControllerTest extends WithApplication {

    private static String testHashtag = "zonauang";

    @Mock
    Twitter twitter = mock(Twitter.class);

    @Mock
    QueryResult queryResult = mock(QueryResult.class);

    @Mock
    Status status = mock(Status.class);

    @Spy
     List<Status> results ;

    @InjectMocks
     GetTweets theMock = new GetTweets(twitter);

    @Override
    protected Application provideApplication() {
        return new GuiceApplicationBuilder().build();
    }


    private void setUp() throws ExecutionException, InterruptedException, TwitterException {

        List<CompletableFuture> tasks = new ArrayList<>();

        results = new ArrayList<>();
       when(status.getText()).thenReturn("this is a test");
        results.add(status);
        results.add(status);
        results.add(status);

         when(queryResult.getTweets()).thenReturn(results);
        Query inputQuery = new Query(testHashtag + " -filter:retweets");
        inputQuery.count(10);
        inputQuery.lang("en");


        when(twitter.search(inputQuery)).thenReturn(queryResult);


    }

    /**
     * Test session management. Multiple user enter sites and the session management
     * keep track correctly.
     */
    @Test
    public void testHomePage() {
        for(int i = 1; i < 10; i++) {
            Http.RequestBuilder request = new Http.RequestBuilder()
                    .method(GET)
                    .uri("/");

            Result result = route(app, request);
            assertEquals(OK, result.status());
            String resultString = result.session().get("Twitter").orElse("no user created");

            String testuser = String.format("sessionData{sessionID='play%d'}", i );
            assertThat(resultString, is(testuser));

        }


    }


    /**
     * Test that after the user enter the app,
     * it would not create a new session
     */
    @Test
    public void testReturnUser(){

        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(GET)
                .uri("/");

        route(app, request);

         request = new Http.RequestBuilder()
                .method(GET)
                .uri("/")
                .session("Twitter","sessionData{sessionID='play1'}");
        Result result = route(app, request);
        assertEquals(OK, result.status());
        String resultString = result.session().get("Twitter").orElse("no user created");
        String testuser = String.format("sessionData{sessionID='play1'}" );
        assertThat(resultString, is(testuser));

    }

    @Test
    public void testHashTag() throws ExecutionException, InterruptedException, TwitterException {

        setUp();
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(GET)
                .uri("/hashtag?s="+ testHashtag);
        Result result = route(app, request);

        assertEquals(OK, result.status());

    }


    @Test
    public void testGetLocation(){

        String location = "Montreal";

        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(GET)
                .uri("/location?s="+ location);
        Result result = route(app, request);

        assertEquals(OK, result.status());
        assertThat(contentAsString(result).contains(location),is(true));
    }


    @Test
    public void testGetTweets() {
//        Http.RequestBuilder request = Helpers.fakeRequest()
//                .method(POST)
//                .session("Twitter","Twitter")
//                .bodyForm(ImmutableMap.of("searchString", "A SEARCH STRING"))
//                .uri("/");
//
//        Result result = route(app, request);
//        assertEquals(SEE_OTHER, result.status());


    }

}
