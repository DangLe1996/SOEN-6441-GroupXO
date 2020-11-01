package controllers;
import models.TwitterSearch;
import models.TwitterDataFetcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.api.mvc.Cookie;
import play.data.Form;
import play.data.FormFactory;
import play.i18n.MessagesApi;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.*;
import twitter4j.Status;
import twitter4j.TwitterException;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import static play.libs.Scala.asScala;
@Singleton
public class TwitterDataController extends Controller {

    private final Form<TwitterData> form;
    private MessagesApi messagesApi;
    private List<TwitterSearch> tweets;

    private final Logger logger = LoggerFactory.getLogger(getClass()) ;
    private HttpExecutionContext httpExecutionContext;

    @Inject
    public TwitterDataController(FormFactory formFactory, MessagesApi messagesApi,HttpExecutionContext ec) {
        this.form = formFactory.form(TwitterData.class);
        this.messagesApi = messagesApi;
        this.httpExecutionContext = ec;
        this.tweets = com.google.common.collect.Lists.newArrayList(
            new TwitterSearch(null, null)


        );
    }

    public Result index() {
        return ok(views.html.index.render());
    }

    public Result searchTweets(Http.Request request) {
        return ok(views.html.listTweets.render(asScala(tweets), form, request, messagesApi.preferred(request)));
    }


    public CompletionStage<Result> postTweets(Http.Request request) throws TwitterException {



        request.session().adding("test","123");

        // Use a different task with explicit EC
        final Form<TwitterData> boundForm = form.bindFromRequest(request);
        TwitterData data = boundForm.get();
        request.session().adding("test","abcd");
        request.flash().adding("test","abcd");
        return new TwitterDataFetcher().fetchTwitterSearchCompleted(data.getSearchString()).thenApplyAsync(
                answer -> {
                    return ok(views.html.listTweets.render(asScala(answer), form, request, messagesApi.preferred(request)))
                            .withCookies((Http.Cookie.builder("test","123").withMaxAge(Duration.ofSeconds(900)).build()))
                            ;

                },
                httpExecutionContext.current());
    }






}