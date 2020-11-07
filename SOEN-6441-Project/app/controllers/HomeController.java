package controllers;

import models.GetTweets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import models.Search;
import models.sessionData;
import twitter4j.*;
import java.util.Map;
import play.data.Form;
import play.data.FormFactory;
import play.i18n.MessagesApi;
import play.mvc.*;

import javax.inject.Inject;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.BiFunction;

import static play.libs.Scala.asScala;

/**
 * This controller contains an action to handle HTTP requests
 * to the application.
 */
public class HomeController extends Controller {


	
    private Form<Search> form ;
    private MessagesApi messagesApi;




    @Inject
    public HomeController(FormFactory formFactory, MessagesApi messagesApi) {

        this.form = formFactory.form(Search.class);
        this.messagesApi = messagesApi;

    }

	/**
	 * Lambda function to render tweets_display view.
	 */
	private final BiFunction<sessionData, Http.Request,Result> displayHomePage =
			(currentUser,request) ->ok(views.html.tweets_display
					.render(currentUser.getQuery(), currentUser.getCache(), form, request, messagesApi.preferred(request)))
					.addingToSession(request, "Twitter", currentUser.toString());

	/**
	 * Handle user request to see the last 10 tweets for a given keyword.
	 * @param request : Http request contains search query and session information.
	 * @return : display tweets_display with code 200 if the request is handled sucessfully. If there is an error, return to HomePage with new session.
	 * @throws TwitterException: exception from twitter4j server.
	 * @see models.GetTweets#GetTweets_keyword(String)
	 */
    public CompletionStage<Result> gettweet(Http.Request request) throws TwitterException{
    	final Form<Search> boundForm = form.bindFromRequest(request);

        if (boundForm.hasErrors()) {
			System.out.println("Error with bound form in HomeController.gettweet method");
            return CompletableFuture.completedFuture(redirect(routes.HomeController.homePage()));
        } else {
        	try {
				Search searchquery = boundForm.get();
				String currentUserID = request.session().get("Twitter").get();
				return new GetTweets().GetTweets_keyword(searchquery.getSearchString(),currentUserID)
						.thenApply(currentUser -> displayHomePage.apply(currentUser,request));


			}catch (NullPointerException ex){
				System.out.println("Null pointer exception in gettweet method");
        		return CompletableFuture.completedFuture(redirect(routes.HomeController.homePage()));
			}

        }      
    }

	/**
	 * This method display the Home Page. If the request does not contain any user information, a new user session will be created and attached.
	 * Else, user information (terms that user already searched for) is retrieved from the userCache in sessionData class.
	 * @param request
	 * @return CompletionStage<Result> that display the homePage.
	 * @see models.sessionData#getUser(String)
	 */
    public CompletionStage<Result> homePage(Http.Request request) {

		sessionData currenUser = null;
		String currentUserID = null;
    	if(request.session().get("Twitter").isPresent()){
			 currentUserID = request.session().get("Twitter").get();
			 currenUser  = sessionData.getUser(currentUserID);
			if(currenUser == null) {
				currenUser = new sessionData();
			}
		}
    	else{
			currenUser = new sessionData();
			currentUserID = currenUser.toString();
		}
		System.out.println("Current user: " + currentUserID);

		return CompletableFuture.completedFuture(displayHomePage.apply(currenUser,request));



    }



    public Result user(Http.Request request,String g) {
    	return ok("in user " + g);
    	
    }
    public Result location(Http.Request request,String g) {
    	return ok("in location " + g);
    	
    }

	/**
	 * Handle user request to see the word-level statistics of the last 250 tweets with a search term.
	 * @author: Girish
	 * @param searchQuery: the term that user want to see word analysis
	 * @return: A new page that display word-level statistics.
	 * @throws TwitterException
	 * @see models.GetTweets#GetKeywordStats(String) 
	 */
    public CompletionStage<Result> keyword(String searchQuery) throws TwitterException {
		return new GetTweets().GetKeywordStats(searchQuery)
				.thenApply(wc -> {
							LinkedHashMap<String, Integer> sortedwc = new LinkedHashMap<>();
							wc.entrySet()
									.parallelStream()
									.sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
									.forEachOrdered(swc -> sortedwc.put(swc.getKey(), swc.getValue()));

							String result = "";
							for (String s : sortedwc.keySet()) {
								result = result + "\n" + s + " \t\t: \t\t" + sortedwc.get(s);
							}
							return ok(views.html.wordstats.render(searchQuery, result));
						}
				);
	}
//

	/**
	 * Handle user request to see latest 10 tweets with a given hashtag
	 * @author: Dang Le
	 * @param searchQuery : the hashtag that user want to search for
	 * @return: A new page that display the last 10 tweets that use that hashtag
	 * @throws TwitterException: exception from twitter4j if the tweets are not retrieved successfully.
	 */
    public CompletionStage<Result> hashtag(String searchQuery) throws TwitterException {


		return  new GetTweets().GetTweets_keyword(searchQuery)
				.thenApply(tweet -> {
					return ok(views.html.tweets_hashtag_display.render(searchQuery, tweet));

				});
    }
    
}
