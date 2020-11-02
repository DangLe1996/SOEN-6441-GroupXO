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
import views.html.tweets_display;

import javax.inject.Inject;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import static play.libs.Scala.asScala;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
public class HomeController extends Controller {

    /**
     * An action that renders an HTML page with a welcome message.
     * The configuration in the <code>routes</code> file means that
     * this method will be called when the application receives a
     * <code>GET</code> request with a path of <code>/</code>.
     */
	
	private class session_data{
		private List<String> queries;
	    private LinkedHashMap<String,String> cache;
		
		session_data(List<String> query,LinkedHashMap<String,String> cache){
			this.queries = query;
			this.cache = cache;
		}
		private List<String> getQuery(){
			return this.queries;
		}
		private LinkedHashMap<String,String> getCache(){
			return this.cache;
		}
		
	}
	
    private Form<Search> form ;
    private MessagesApi messagesApi;
    public static HashMap<String,session_data> sessions;
    public static Integer session_count;


    private HashMap<String,String> GlobalHashtagCache;

    @Inject
    public HomeController(FormFactory formFactory, MessagesApi messagesApi) {
    	this.GlobalHashtagCache = new HashMap<>();
        this.form = formFactory.form(Search.class);
        this.messagesApi = messagesApi;
        this.session_count = 0;
        this.sessions =  new HashMap<String,session_data>();
    }

	private final BiFunction<sessionData, Http.Request,Result> displayTweetPage =
			(currentUser,request) ->ok(tweets_display
					.render(currentUser.getQuery(), currentUser.getCache(), form, request, messagesApi.preferred(request)))
					.addingToSession(request, "Twitter", currentUser.toString());

    public CompletionStage<Result> gettweet(Http.Request request) throws TwitterException{
    	final Form<Search> boundForm = form.bindFromRequest(request);

        if (boundForm.hasErrors()) {
            return CompletableFuture.completedFuture(badRequest(views.html.tweets_display.render(new ArrayList<String>(),new LinkedHashMap<String,String>(), boundForm, request, messagesApi.preferred(request))));
        } else {
        	try {
				Search searchquery = boundForm.get();
				String currentUserID = request.session().get("Twitter").get();
				return GetTweets.GetTweets_keyword(searchquery.getSearchString(),currentUserID)
						.thenApply(currentUser -> displayTweetPage.apply(currentUser,request));


			}catch (NullPointerException ex){
        		return CompletableFuture.completedFuture(redirect(routes.HomeController.searchPage()).withNewSession());
			}

        }      
    }
    
    public CompletionStage<Result> searchPage(Http.Request request) {

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

		return CompletableFuture.completedFuture(displayTweetPage.apply(currenUser,request));



    }


    private session_data getNewSession(String userID){
		LinkedHashMap<String,String> newcache =  new LinkedHashMap<String, String>();
		List<String> newquery = new ArrayList<String>();
		session_data new_session = new session_data(newquery,newcache);
		sessions.put(userID,new_session);
		return new_session;
	}


    public Result user(Http.Request request,String g) {
    	return ok("in user " + g);
    	
    }
    public Result location(Http.Request request,String g) {
    	return ok("in location " + g);
    	
    }
    public CompletionStage<Result> keyword(Http.Request request,String g) throws TwitterException {
		return new GetTweets().GetKeywordStats(g)
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
							return ok(views.html.wordstats.render(g, result));
						}
				);
	}
//

    public CompletionStage<Result> hashtag(String searchquery) throws TwitterException {


		return new GetTweets().GetTweets_keyword(searchquery)
				.thenApply(tweet -> {
					return ok(views.html.tweets_hashtag_display.render(searchquery, tweet));

				});
    }
    
}
