package controllers;
import models.TwitterSearch;
import models.TwitterDataFetcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.data.Form;
import play.data.FormFactory;
import play.i18n.MessagesApi;
import play.mvc.*;
import twitter4j.Status;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

import static play.libs.Scala.asScala;
@Singleton
public class TwitterDataController extends Controller {

    private final Form<TwitterData> form;
    private MessagesApi messagesApi;
    private final List<TwitterSearch> tweets;

    private final Logger logger = LoggerFactory.getLogger(getClass()) ;

    @Inject
    public TwitterDataController(FormFactory formFactory, MessagesApi messagesApi) {
        this.form = formFactory.form(TwitterData.class);
        this.messagesApi = messagesApi;

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

    public Result postTweets(Http.Request request) {
        final Form<TwitterData> boundForm = form.bindFromRequest(request);

        if (boundForm.hasErrors()) {
            logger.error("errors = {}", boundForm.errors());
            return badRequest(views.html.listTweets.render(asScala(tweets), boundForm, request, messagesApi.preferred(request)));
        } else {
             TwitterData data = boundForm.get();
             TwitterDataFetcher a= new TwitterDataFetcher();
             List<Status> tweet2=a.fetchTwitterSearch(data.getSearchString());

            tweets.clear();
            for (Status tweet1 : tweet2) {

                tweets.add(new TwitterSearch(tweet1.getUser().getScreenName().toString(), tweet1.getText().toString()));


            }

            //tweets.add(new TwitterSearch(data.getSearchString(), "dummy"));
            return redirect(routes.TwitterDataController.searchTweets())
                    .flashing("info", "Searched!");
        }
    }


    public Result searchTweetsByLocation(Http.Request request){
        return ok(views.html.Location.render());
    }

}