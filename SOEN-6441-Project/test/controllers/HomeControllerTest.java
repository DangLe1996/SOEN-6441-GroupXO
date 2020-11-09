package controllers;



import models.GetTweets;
import org.junit.Test;

import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import play.Application;
import play.Environment;
import play.api.inject.guice.GuiceApplicationBuilder;
import play.api.test.CSRFTokenHelper;
import play.test.Helpers;
import play.core.j.JavaResultExtractor;

import play.core.server.AkkaHttpServer;
import play.data.Form;
import play.data.FormFactory;
import play.i18n.MessagesApi;
import play.inject.guice.GuiceApplicationLoader;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;
import play.test.WithApplication;
import twitter4j.*;


import javax.inject.Inject;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static play.mvc.Http.RequestBuilder;
import static org.mockito.Mockito.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.GET;
import static play.test.Helpers.route;

//import org.apache.http.util.EntityUtils;
import play.http.HttpEntity;

@RunWith(MockitoJUnitRunner.class)
public class HomeControllerTest extends WithApplication {
    @Mock
    private static Twitter mockTwitter;
    @Mock
    private static QueryResult queryResult = mock(QueryResult.class);
    @InjectMocks
    GetTweets getTweets=new GetTweets(mockTwitter);

    @Test
    public void testHomeController() throws TwitterException, ExecutionException, InterruptedException {
        String testKeyWord="canada";

        FormFactory formFactory=null;

        RequestBuilder request=requestBuilder(testKeyWord);
        setDummyQueriesAndFurtherMocks(testKeyWord);

        FormFactory mockFormFactory = mock(FormFactory.class);
        mockFormFactory = new GuiceApplicationBuilder().injector().instanceOf(FormFactory.class);
        MessagesApi messageAPI=mock(MessagesApi.class);
        messageAPI=new GuiceApplicationBuilder().injector().instanceOf(MessagesApi.class);

        HomeController homeController=new HomeController(mockFormFactory,messageAPI);

        homeController.setGlobalGetTweet(getTweets);

        CompletionStage<Result> result =homeController.gettweet(request.build());

        String resultString =result.toCompletableFuture().get().toString();
        System.out.println(resultString);
        System.out.println("Check 1 " + result.toCompletableFuture().get().contentType());
        System.out.println("Check 1 " + result.toCompletableFuture().get().body());


        ///Result htmlResult = ok("<h1>Hello World!</h1>").as(Http.MimeTypes.HTML);

        //String content = EntityUtils.toString(result.toCompletableFuture().get().body());

        //assertThat(resultString,contains("montreal"));
        //assertThat("A","a");


    }

    @Test
    public void testBoundFormHasError() throws TwitterException, ExecutionException, InterruptedException {

        String testKeyWord=null;
        FormFactory formFactory=null;
        RequestBuilder request=Helpers.fakeRequest()
                //.bodyForm(hm)
                .session("Twitter","Twitter")
                .method(Helpers.POST);

        setDummyQueriesAndFurtherMocks(testKeyWord);
        FormFactory mockFormFactory = mock(FormFactory.class);
        mockFormFactory = new GuiceApplicationBuilder().injector().instanceOf(FormFactory.class);
        MessagesApi messageAPI=mock(MessagesApi.class);
        messageAPI=new GuiceApplicationBuilder().injector().instanceOf(MessagesApi.class);

        HomeController homeController=new HomeController(mockFormFactory,messageAPI);
        homeController.setGlobalGetTweet(getTweets);
        CompletionStage<Result> result =homeController.gettweet(request.build());
        //String resultString =result.toCompletableFuture().get().java
        //JavaResultExtractor




        //String header = JavaResultExtractor.getHeaders(result.toCompletableFuture().get().header()).get("Content-Type");


        //assertThat(resultString,contains("montreal"));
        //assertThat("A","a");


    }

    @Test
    public void testErrorInModel() throws TwitterException, ExecutionException, InterruptedException {

        String testKeyWord="canada";
        FormFactory formFactory=null;
        RequestBuilder request=requestBuilder(testKeyWord);
        //setDummyQueriesAndFurtherMocks(testKeyWord);
        FormFactory mockFormFactory = mock(FormFactory.class);
        mockFormFactory = new GuiceApplicationBuilder().injector().instanceOf(FormFactory.class);
        MessagesApi messageAPI=mock(MessagesApi.class);
        messageAPI=new GuiceApplicationBuilder().injector().instanceOf(MessagesApi.class);

        HomeController homeController=new HomeController(mockFormFactory,messageAPI);
        homeController.setGlobalGetTweet(getTweets);

        CompletionStage<Result> result =homeController.gettweet(request.build());

        String resultString =result.toCompletableFuture().get().toString();

        System.out.println(resultString);

    }

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

    @Test
    public void testException() throws TwitterException, ExecutionException, InterruptedException {

        String testKeyWord="canada";
        FormFactory formFactory=null;
        //RequestBuilder request=requestBuilder(testKeyWord);
        //setDummyQueriesAndFurtherMocks(testKeyWord);

        Map<String, String> hm
                = new HashMap<String, String>();
        hm.put("searchString", testKeyWord);

        RequestBuilder request=Helpers.fakeRequest()
                .bodyForm(hm)
                //.session("Twitter","facebook")
                .method(Helpers.POST);

        CSRFTokenHelper.addCSRFToken(request);
        FormFactory mockFormFactory = mock(FormFactory.class);
        mockFormFactory = new GuiceApplicationBuilder().injector().instanceOf(FormFactory.class);
        MessagesApi messageAPI=mock(MessagesApi.class);
        messageAPI=new GuiceApplicationBuilder().injector().instanceOf(MessagesApi.class);

        HomeController homeController=new HomeController(mockFormFactory,messageAPI);
        homeController.setGlobalGetTweet(getTweets);

        CompletionStage<Result> result =homeController.gettweet(request.build());

//        assertThat(result,is(null));
        //String resultString =result.toCompletableFuture().get().toString();
        //System.out.println(resultString);

    }


    //Helper Methods
    public void setDummyQueriesAndFurtherMocks( String testKeyWord ) throws TwitterException {

        Query inputQuery = new Query(testKeyWord+ " -filter:retweets");
        inputQuery.count(250);
        inputQuery.lang("en");
        List<Status> fakeTweets=buildStatusList(1,"HAPPY");
        when(mockTwitter.search(inputQuery)).thenReturn(queryResult);
        when(queryResult.getTweets()).thenReturn(fakeTweets);
        when(queryResult.getQuery()).thenReturn(testKeyWord);

    }

    public RequestBuilder requestBuilder(String testKeyWord ){
        Map<String, String> hm
                = new HashMap<String, String>();
        hm.put("searchString", testKeyWord);

        RequestBuilder request=Helpers.fakeRequest()
                .bodyForm(hm)
                .session("Twitter","Twitter")
                .method(Helpers.POST);

        CSRFTokenHelper.addCSRFToken(request);
        return request;
    }

    private List<Status> buildStatusList(int number, String tweetMode) throws TwitterException {
        List<Status> statuses = new ArrayList<>();
        Status aTestStatus = null;

        for (int position = 0; position < number; position++) {
            String rawJson = "{\"contributors\": null, \"truncated\": false, \"text\": \"\\\"Montreal Indians "+tweetMode+"\", \"in_reply_to_status_id\": null, \"random_number\": 0.29391851181222817, \"id\": 373208832580648960, \"favorite_count\": 0, \"source\": \"<a href=\\\"http://twitter.com/tweetbutton\\\" rel=\\\"nofollow\\\">Tweet Button</a>\", \"retweeted\": false, \"coordinates\": null, \"entities\": {\"symbols\": [], \"user_mentions\": [], \"hashtags\": [{\"indices\": [29, 35], \"text\": \"Syria\"}, {\"indices\": [47, 52], \"text\": \"Iraq\"}, {\"indices\": [109, 120], \"text\": \"propaganda\"}, {\"indices\": [121, 132], \"text\": \"MiddleEast\"}, {\"indices\": [133, 137], \"text\": \"war\"}], \"urls\": [{\"url\": \"http://t.co/FQU4QMIxPF\", \"indices\": [86, 108], \"expanded_url\": \"http://huff.to/1dinit0\", \"display_url\": \"huff.to/1dinit0\"}]}, \"in_reply_to_screen_name\": null, \"id_str\": \"373208832580648960\", \"retweet_count\": 0, \"in_reply_to_user_id\": null, \"favorited\": false, \"user\": {\"follow_request_sent\": null, \"profile_use_background_image\": true, \"geo_enabled\": false, \"verified\": false, \"profile_image_url_https\": \"https://si0.twimg.com/profile_images/3537112264/5ebce8651eb68383030dc01836215da1_normal.jpeg\", \"profile_sidebar_fill_color\": \"FFF7CC\", \"id\": 1360644582, \"profile_text_color\": \"0C3E53\", \"followers_count\": 27, \"profile_sidebar_border_color\": \"F2E195\", \"location\": \"Detroit \\u2663 Toronto\", \"default_profile_image\": false, \"id_str\": \"1360644582\", \"utc_offset\": -14400, \"statuses_count\": 1094, \"description\": \"Exorcising the sins of personal ignorance and accepted lies through reductionist analysis. Politics, economics, and science posts can be found here.\", \"friends_count\": 81, \"profile_link_color\": \"FF0000\", \"profile_image_url\": \"http://a0.twimg.com/profile_images/3537112264/5ebce8651eb68383030dc01836215da1_normal.jpeg\", \"notifications\": null, \"profile_background_image_url_https\": \"https://si0.twimg.com/images/themes/theme12/bg.gif\", \"profile_background_color\": \"BADFCD\", \"profile_banner_url\": \"https://pbs.twimg.com/profile_banners/1360644582/1366247104\", \"profile_background_image_url\": \"http://a0.twimg.com/images/themes/theme12/bg.gif\", \"name\": \"Neil Cheddie\", \"lang\": \"en\", \"following\": null, \"profile_background_tile\": false, \"favourites_count\": 4, \"screen_name\": \"Centurion480\", \"url\": null, \"created_at\": \"Thu Apr 18 00:34:18 +0000 2013\", \"contributors_enabled\": false, \"time_zone\": \"Eastern Time (US & Canada)\", \"protected\": false, \"default_profile\": false, \"is_translator\": false, \"listed_count\": 2}, \"geo\": null, \"in_reply_to_user_id_str\": null, \"possibly_sensitive\": false, \"lang\": \"en\", \"created_at\": \"Thu Aug 29 22:21:34 +0000 2013\", \"filter_level\": \"medium\", \"in_reply_to_status_id_str\": null, \"place\": null, \"_id\": {\"$oid\": \"521fc96edbef20c5d84b2dd8\"}}";
            //System.out.println(rawJson);
            aTestStatus = TwitterObjectFactory.createStatus(rawJson);
            statuses.add(aTestStatus);
        }
        return statuses;
    }
}