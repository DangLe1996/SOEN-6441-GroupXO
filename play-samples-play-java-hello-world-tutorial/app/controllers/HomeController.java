package controllers;

import models.GetTweets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import models.Search;
import twitter4j.*;

import play.data.Form;
import play.data.FormFactory;
import play.i18n.MessagesApi;
import play.mvc.Http.Cookie;
import play.mvc.*;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

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
	    private LinkedHashMap<String,List<Status>> cache;
		
		session_data(List<String> query,LinkedHashMap<String,List<Status>> cache){
			this.queries = query;
			this.cache = cache;
		}
		private List<String> getQuery(){
			return this.queries;
		}
		private LinkedHashMap<String,List<Status>> getCache(){
			return this.cache;
		}
		
	}
	
    private final Form<Search> form;
    private MessagesApi messagesApi;
    private HashMap<String,session_data> sessions;
    private Integer session_count;

    @Inject
    public HomeController(FormFactory formFactory, MessagesApi messagesApi) {
        this.form = formFactory.form(Search.class);
        this.messagesApi = messagesApi;
        //this.cache = new LinkedHashMap<String, List<Status>>();
        this.session_count = 0;
        this.sessions =  new HashMap<String,session_data>();
    }
    
    public CompletionStage<Result> gettweet(Http.Request request) throws TwitterException{
    	final Form<Search> boundForm = form.bindFromRequest(request);

        if (boundForm.hasErrors()) {
            return CompletableFuture.completedFuture(badRequest(views.html.tweets_display.render(new ArrayList<String>(),new LinkedHashMap<String,List<Status>>(), boundForm, request, messagesApi.preferred(request))));
        } else {
            Search searchquery = boundForm.get();
             
	        String current_user = request.session().get("Twitter").get(); 
	        session_data current_session = this.sessions.get(current_user);
	
	        if (current_session != null) {
	
		        LinkedHashMap<String,List<Status>> cache = current_session.getCache();	
		        boolean alreadySearched = cache.containsKey(searchquery.getSearchString());  
		        if (alreadySearched) {  
		        	return CompletableFuture.completedFuture(redirect(routes.HomeController.searchPage())
		        			.addingToSession(request, "Twitter", current_user)
		        			);
		        }
		        else {
		        	List<String> query = current_session.getQuery();
		        	query.add(searchquery.getSearchString()); 
			        return (GetTweets.GetTweets_keyword(searchquery.getSearchString())
			        	   .thenApply(status -> {cache.put(searchquery.getSearchString(),status);
			        	   						return redirect(routes.HomeController.searchPage())
			        	   								.addingToSession(request, "Twitter", current_user)
			        	   								;}));
			        
		        }
	        }
	        else {
	        	return CompletableFuture.completedFuture(redirect(routes.HomeController.searchPage())
	        			.addingToSession(request, "Twitter", current_user)
	        			);
	         	
	        }
        }
    
    }
    
    public CompletionStage<Result> searchPage(Http.Request request) {
    	
    	return request
    		      .session()
    		      .get("Twitter")
    		      .map(user -> { 
    		    	  			String current_user = request.session().get("Twitter").get();
    		    	  			session_data current_session = this.sessions.get(current_user);
			        			if (current_session != null) {
				        			List<String> query = current_session.getQuery();
				        			LinkedHashMap<String,List<Status>> cache = current_session.getCache();
				        			return CompletableFuture.completedFuture(ok(views.html.tweets_display.render(query,cache , form, request, messagesApi.preferred(request)))
				        					.addingToSession(request, "Twitter", current_user)
				        					)
				       				     ;
				        			}
			        			else {
				        				List<String> query = new ArrayList<String>();;
				            			LinkedHashMap<String,List<Status>> cache = new LinkedHashMap<String, List<Status>>();
				            			return CompletableFuture.completedFuture(ok(views.html.tweets_display.render(query,cache , form, request, messagesApi.preferred(request)))
				            					.addingToSession(request, "Twitter", current_user)
				           				     );
				        			}
        			
        						})
    		      .orElseGet(() ->{
					        	 LinkedHashMap<String,List<Status>> newcache =  new LinkedHashMap<String, List<Status>>();
					        	 List<String> newquery = new ArrayList<String>();
					    		 List<Object> newsession = new ArrayList<Object>();
					    		 newsession.add(newquery);
					    		 newsession.add(newcache);
					    		 this.session_count += 1;
					    		 session_data s = new session_data(newquery,newcache);
					    		 String newuser = "play" + Integer.toString(this.session_count);
					    		 this.sessions.put(newuser,s);
					    		 return CompletableFuture.completedFuture(ok(views.html.tweets_display.render(newquery,newcache , form, request, messagesApi.preferred(request)))
					    				.addingToSession(request, "Twitter", newuser));
    		      				});
    }

    public Result user(String g) {
    	return ok("in user " + g);
    	
    }
    public Result location(String g) {
    	return ok("in location " + g);
    	
    }
    public Result word(String g) {
    	return ok("in word " + g);
    	
    }

}
