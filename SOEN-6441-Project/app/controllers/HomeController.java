package controllers;
import actors.HashtagActor;
import actors.KeywordActor;
import actors.UserActor;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.ActorRef;
import akka.stream.Materializer;
import akka.stream.javadsl.Flow;
import models.GetTweets;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import models.Search;
import models.sessionData;
import play.libs.Json;
import play.libs.streams.ActorFlow;
import play.data.Form;
import play.data.FormFactory;
import play.i18n.MessagesApi;
import play.mvc.*;
import views.html.tweets_hashtag_display;

import javax.inject.Inject;


import java.util.function.BiFunction;

import static play.libs.Scala.asScala;

/**
 * This controller contains an action to handle HTTP requests
 * to the application.
 */
public class HomeController extends Controller {


	@Inject
	private ActorSystem actorSystem;
	@Inject
	 Materializer materializer;

	private ActorRef TwitterStreamActor;

    private Form<Search> form ;
    private MessagesApi messagesApi;

	@Inject
	GetTweets globalGetTweet;

	public void setGlobalGetTweet(GetTweets globalGetTweet) {
		this.globalGetTweet = globalGetTweet;
	}

	@Inject
	public HomeController(FormFactory formFactory, MessagesApi messagesApi, ActorSystem as, Materializer mat) {

		this.form = formFactory.form(Search.class);
		this.messagesApi = messagesApi;
		this.actorSystem = as;
		this.materializer = mat;
		this.TwitterStreamActor = this.actorSystem.actorOf(Props.create(actors.TwitterStreamActor.class),"HashtagParent");

	}

	/**
	 * Lambda function to render tweets_display view.
	 */
	private final BiFunction<sessionData, Http.Request,Result> displayHomePage =
			(currentUser,request) ->ok(views.html.tweets_display
					.render(currentUser.getQuery(), currentUser.getCache(),  request, messagesApi.preferred(request)))
					.addingToSession(request, "Twitter", currentUser.toString());

	/**
	 * Handle user request to see the last 10 tweets for a given keyword.
	 * @param request : Http request contains search query and session information.
	 * @return : display tweets_display with code 200 if the request is handled sucessfully. If there is an error, return to HomePage with new session
	 * @see models.GetTweets#GetTweetsWithKeyword(String)
	 */
    public CompletionStage<Result> gettweet(Http.Request request) {
    	final Form<Search> boundForm = form.bindFromRequest(request,"searchString");

        if (boundForm.hasErrors()) {
            return CompletableFuture.completedFuture(redirect(routes.HomeController.homePage()));
        } else {
				String searchquery = boundForm.get().getSearchString();
				String currentUserID = request.session().get("Twitter").get();
				return globalGetTweet.GetTweetsWithUser(searchquery, currentUserID)
						.thenApply(currentUser -> displayHomePage.apply(currentUser, request));

        }
    }

	/**
	 * This method display the Home Page. If the request does not contain any user information, a new user session will be created and attached.
	 * Else, user information (terms that user already searched for) is retrieved from the userCache in sessionData class.
	 * @param request Holder for session Data
	 * @return Html display the homePage.
	 * @see models.sessionData#getUser(String)
	 */
    public CompletionStage<Result> homePage(Http.Request request) {
		sessionData currentUser = null;
		String currentUserID = null;
    	if(request.session().get("Twitter").isPresent()){
			 currentUserID = request.session().get("Twitter").get();
			 currentUser  = sessionData.getUser(currentUserID);
		}
    	else{
			currentUser = new sessionData();

		}
		return CompletableFuture.completedFuture(displayHomePage.apply(currentUser,request));

    }

	public WebSocket indexWs(){
		return WebSocket.Json.accept( request -> {
			return ActorFlow.actorRef(wsout ->{
				String currentUserID = request.session().get("Twitter").get();
				System.out.println("wsout value is " + wsout.toString());
				return UserActor.props(wsout, currentUserID, globalGetTweet, TwitterStreamActor );
			}, actorSystem, materializer);

		});

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
	 * @see models.GetTweets#GetKeywordStats(String) 
	 */
    public CompletionStage<Result> keyword(Http.Request request,String searchQuery)  {
		return globalGetTweet.GetKeywordStats(searchQuery)
				.thenApply(result -> {
			String res = "";
			for(String s: result) {
				res =  res + " \n" + s;
			}
				return ok(views.html.wordstats.render(request,searchQuery, res));
				});
	}
//
	/**
	 * Handle user request to see latest 10 tweets with a given hashtag
	 * @author: Dang Le
	 * @param searchQuery : the hashtag that user want to search for
	 * @return: A new page that display the last 10 tweets that use that hashtag
	 */
    public CompletionStage<Result> hashtag(Http.Request request,String searchQuery)  {


		return  globalGetTweet.GetTweetsWithKeyword("#"+searchQuery)
				.thenApply(tweet -> {
					final Result ok = ok(tweets_hashtag_display.render(request, searchQuery, tweet)).addingToSession(request,"Hashtag",searchQuery);
					return ok;
				});
    }


    HashMap<String,Flow<Json,Json,?>> hashtagFlowsMap = new HashMap<>();

	/**
	 * Create an actorRef that is responsible for handling message websocket for hashtag
	 * @return Websocket Connection with ActorRef
	 */
	public WebSocket HashTagWs(){

	return WebSocket.Json.accept(request -> {
			return ActorFlow.actorRef(wsout -> {
				return HashtagActor.props(wsout, TwitterStreamActor);
			}, actorSystem, materializer);

	});

	}
	HashMap<String,Flow<String,String,?>> keywordFlowsMap = new HashMap<>();

	public WebSocket keywordWs(){
		System.out.println("in keyword ws");
		return WebSocket.Text.accept(request -> {
			Flow<String,String,?> temp = ActorFlow.actorRef(wsout -> {
				return KeywordActor.props(wsout, TwitterStreamActor);
			}, actorSystem, materializer);
			return temp;
		});


	}

}
