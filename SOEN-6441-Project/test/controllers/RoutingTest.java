package controllers;

import akka.actor.ActorSystem;
import models.GetTweets;
import models.sessionData;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.Application;
import play.api.inject.guice.GuiceApplicationBuilder;
import play.api.test.CSRFTokenHelper;
import play.data.FormFactory;
import play.filters.csrf.CSRF;
import play.i18n.MessagesApi;
import play.mvc.Http;
import play.mvc.Result;
import play.test.WithApplication;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;
import static play.inject.Bindings.bind;
import static play.test.Helpers.*;

public class RoutingTest  extends WithApplication {

    private static GetTweets getTweetsTest = mock(GetTweets.class);
    private static Application testApp;

    private static  sessionData testUser;


    @Before
    public void initialize(){



        FormFactory mockFormFactory = new GuiceApplicationBuilder().injector().instanceOf(FormFactory.class);

        MessagesApi messageAPIMock = new GuiceApplicationBuilder().injector().instanceOf(MessagesApi.class);
        ActorSystem actorSystem = new GuiceApplicationBuilder().injector().instanceOf(ActorSystem.class);

        HomeController homeControllerMock = new HomeController(mockFormFactory,messageAPIMock);

        homeControllerMock.setGlobalGetTweet(getTweetsTest);

        testUser = new sessionData();
        testApp = new play.inject.guice.GuiceApplicationBuilder()
                .overrides(bind(HomeController.class).toInstance(homeControllerMock))
                .build();

        testUser.insertCache("test1","test1Result");
    }

    @After
    public void CleanUP(){
        sessionData.cleanUpSessions();

    }
    /**
     * Test routing to hashtag url and getting result back
     */
    @Test
    public void tesHashTagRoute()  {

        when(getTweetsTest.GetTweetsWithKeyword("test")).thenReturn(CompletableFuture.completedFuture("this is a test"));
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(GET)
                .uri("/hashtag?s=test");
        Result result = route(testApp, request);
        assertThat(contentAsString(result).contains("this is a test"),is(true));
        assertThat(result.status(),is(OK));
    }

    @Test
    public void testKeywordRoute(){
        List<String> testResult = Arrays.asList("This is test1", "this is test 2", "value of x is 3");

        when(getTweetsTest.GetKeywordStats("test")).thenReturn(CompletableFuture.completedFuture(testResult));
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(GET)
                .uri("/keyword?s=test");
        Result result = route(testApp, request);

        for(String s : testResult){
            assertThat(contentAsString(result).contains(s),is(true));
        }
        assertThat(result.status(),is(OK));
    }

    @Test
    public void testLocationRoute(){
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(GET)
                .uri("/location?s=Montreal");
        Result result = route(testApp, request);
        assertThat(result.status(),is(OK));
        assertThat(contentAsString(result).contains("in location Montreal"),is(true));
    }

    @Test
    public void testUserRoute(){
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(GET)
                .uri("/user?s=Tom");
        Result result = route(testApp, request);
        assertThat(result.status(),is(OK));
        assertThat(contentAsString(result).contains("in user Tom"),is(true));
    }


    /**
     * Test home page when user first enter
     * Expected result: should not have any td tag,
     * and session data user cache should have only 2 user, with one
     * being added in the initialized method
     * which is in the table tabulated from the twitter result.
     */
    @Test
    public void testHomePageRouteWithNewUser(){
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(GET)
                .uri("/");
        Result result = route(testApp, request);

        assertThat(sessionData.userCache.size(),is(2));
        assertThat(result.status(),is(OK));
        assertThat(contentAsString(result).contains("<td>>"),is(false));
    }

    @Test
    public void testHomePageRouteWithExistingUser(){


        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(GET)
                .session("Twitter", testUser.toString())
                .uri("/");
        Result result = route(testApp, request);
        assertThat(contentAsString(result).contains("test1Result"),is(true));
        assertThat(result.status(),is(OK));

    }

    @Test
    public void testGetTweetWithValidRequest(){

        String searchString = "this is a test";

        testUser.insertCache(searchString,"this is test result");

        when(getTweetsTest.GetTweetsWithUser(searchString, testUser.toString()))
                .thenReturn(CompletableFuture.completedFuture(testUser));


        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(POST)
                .bodyForm(Map.of("searchString",searchString))
                .session("Twitter", testUser.toString())
                .uri("/");
        CSRFTokenHelper.addCSRFToken(request);

        Result result = route(testApp, request);
        assertThat(contentAsString(result).contains("this is test result"),is(true));
        assertThat(result.status(),is(OK));


    }

    /**
     * When trying to get tweet with missing search query, should return to home page (Code 303)
     */
    @Test
    public void testGetTweetWithMissingSearch(){


        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(POST)
                .session("Twitter", testUser.toString())
                .uri("/");
        CSRFTokenHelper.addCSRFToken(request);
        Result result = route(testApp, request);
        assertThat(contentAsString(result).contains("<td>>"),is(false));
        assertThat(result.status(),is(303));

    }




}
