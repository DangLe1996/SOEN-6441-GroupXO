package controllers;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.Guice;
import models.GetTweets;
import models.GetTweetsTest;
import models.sessionData;
import org.eclipse.jetty.util.Callback;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import play.Application;
import play.ApplicationLoader;
import play.Environment;
import play.api.inject.guice.GuiceableModule;
import play.inject.guice.GuiceApplicationBuilder;
import play.inject.guice.GuiceApplicationLoader;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;
import play.test.WithApplication;
import twitter4j.*;

import static org.hamcrest.CoreMatchers.is;
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

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;


@ExtendWith(MockitoExtension.class)
public class HomeControllerTest extends WithApplication {


    @Mock
    final sessionData sdata=mock(sessionData.class);
    @Inject
    Application application;
    final GetTweets getTweetsMock = mock(GetTweets.class);
    @Before
    public void setup() {
        Module testModule = new AbstractModule() {
            @Override
            public void configure() {
                bind(GetTweets.class).toInstance(getTweetsMock);
            }
        };
        GuiceApplicationBuilder builder = new GuiceApplicationLoader()
                .builder(new ApplicationLoader.Context(Environment.simple()))
                .overrides(testModule);
        Guice.createInjector(builder.applicationModule()).injectMembers(this);
        Helpers.start(application);
    }

//
//    @Inject
//    Application application;
//
//    final GetTweets getTweetsMock = mock(GetTweets.class);
////
////    @Mock
////    Twitter twitter = mock(Twitter.class);
//
//
//    @Before
//    public void setup() {
//        Module testModule = new AbstractModule() {
//            @Override
//            public void configure() {
//                bind(GetTweets.class).toInstance(getTweetsMock);
//            }
//        };
//        GuiceApplicationBuilder builder = new GuiceApplicationLoader()
//                .builder(new ApplicationLoader.Context(Environment.simple()))
//                .overrides(testModule);
//        Guice.createInjector(builder.applicationModule()).injectMembers(this);
//        Helpers.start(application);
//    }
//
////    @Before
////    public void setup() {
////        Module testModule = new AbstractModule() {
////            @Override
////            public void configure() {
////                bind(GetTweets.class).toInstance(getTweetsMock);
////                bind(Twitter.class).toInstance(twitter);
////            }
////        };
////
////        GuiceApplicationBuilder builder = new GuiceApplicationLoader()
////                .builder(new ApplicationLoader.Context(Environment.simple()))
////                .overrides(GuiceableModule.guiceable(testModule));
////        Guice.createInjector(builder.applicationModule()).injectMembers(this);
////
////        Helpers.start(application);
////    }
//
//

    @Test
    public void testHomeController() throws Exception {

        sessionData sdata = mock(sessionData.class);

        when(sdata.getCache()).thenReturn((Hashtable<String, String>) new Hashtable<>().put("test", "123"));

        ArrayList aList = new ArrayList();
        aList.add("test");

        when(sdata.getQuery()).thenReturn(aList);

        CompletionStage<sessionData> test = new CompletableFuture<>();

        test.toCompletableFuture().complete(sdata);

        running(application, () -> {

            Http.RequestBuilder request = new Http.RequestBuilder()
                    .method(POST)
                    .session("Twitter", "sessionData{sessionID='play1'}")
                .bodyForm(ImmutableMap.of("searchString", "abcd"))
                .uri("/");


            try {

                when(getTweetsMock.GetTweets_keyword("abcd", "sessionData{sessionID='play1'}")).thenReturn(test);
            } catch (TwitterException e) {
                e.printStackTrace();
            }
            Result result = route(app, request);
            assertEquals(OK, result.status());
        });
    }

    @Test
    public void testIndex() {
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(GET)
                .uri("/");

        Result result = route(app, request);
        assertEquals(OK, result.status());
    }


    //    private static String testHashtag = "zonauang";
//
//    @Mock
//    Twitter twitter = mock(Twitter.class);
//
//    @Mock
//    QueryResult queryResult = mock(QueryResult.class);
//
//    @Mock
//    Status status = mock(Status.class);
//
//    @Spy
//     List<Status> results ;
//
//    @InjectMocks
//     GetTweets theMock = new GetTweets(twitter);
//
//    @Override
//    protected Application provideApplication() {
//        return new GuiceApplicationBuilder().build();
//    }
//
//
//    private void setUp() throws ExecutionException, InterruptedException, TwitterException {
//
//        List<CompletableFuture> tasks = new ArrayList<>();
//
//        results = new ArrayList<>();
//       when(status.getText()).thenReturn("this is a test");
//        results.add(status);
//        results.add(status);
//        results.add(status);
//
//         when(queryResult.getTweets()).thenReturn(results);
//        Query inputQuery = new Query(testHashtag + " -filter:retweets");
//        inputQuery.count(10);
//        inputQuery.lang("en");
//
//
//        when(twitter.search(inputQuery)).thenReturn(queryResult);
//
//
//    }
//
//    /**
//     * Test session management. Multiple user enter sites and the session management
//     * keep track correctly.
//     */
    @Test
    public void testHomePage() {
        for (int i = 1; i < 10; i++) {
            Http.RequestBuilder request = new Http.RequestBuilder()
                    .method(GET)
                    .uri("/");

            Result result = route(app, request);
            assertEquals(OK, result.status());
            String resultString = result.session().get("Twitter").orElse("no user created");

            String testuser = String.format("sessionData{sessionID='play%d'}", i);
            assertThat(resultString, is(testuser));

        }
    }
}

//
//    }
//
//
//    /**
//     * Test that after the user enter the app,
//     * it would not create a new session
//     */
//    @Test
//    public void testReturnUser(){
//
//        Http.RequestBuilder request = new Http.RequestBuilder()
//                .method(GET)
//                .uri("/");
//
//        route(app, request);
//
//         request = new Http.RequestBuilder()
//                .method(GET)
//                .uri("/")
//                .session("Twitter","sessionData{sessionID='play1'}");
//        Result result = route(app, request);
//        assertEquals(OK, result.status());
//        String resultString = result.session().get("Twitter").orElse("no user created");
//        String testuser = String.format("sessionData{sessionID='play1'}" );
//        assertThat(resultString, is(testuser));
//
//    }
//
//    @Test
//    public void testHashTag() throws ExecutionException, InterruptedException, TwitterException {
//
//        setUp();
//        Http.RequestBuilder request = new Http.RequestBuilder()
//                .method(GET)
//                .uri("/hashtag?s="+ testHashtag);
//        Result result = route(app, request);
//
//        assertEquals(OK, result.status());
//
//    }
//
//
//    @Test
//    public void testGetLocation(){
//
//        String location = "Montreal";
//
//        Http.RequestBuilder request = new Http.RequestBuilder()
//                .method(GET)
//                .uri("/location?s="+ location);
//        Result result = route(app, request);
//
//        assertEquals(OK, result.status());
//        assertThat(contentAsString(result).contains(location),is(true));
//    }
//
//
//    @Test
//    public void testGetTweets() {
////        Http.RequestBuilder request = Helpers.fakeRequest()
////                .method(POST)
////                .session("Twitter","Twitter")
////                .bodyForm(ImmutableMap.of("searchString", "A SEARCH STRING"))
////                .uri("/");
////
////        Result result = route(app, request);
////        assertEquals(SEE_OTHER, result.status());
//
//
//    }


