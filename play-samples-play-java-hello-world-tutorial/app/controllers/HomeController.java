package controllers;

import models.GetTweets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import models.Search;
import twitter4j.*;
import java.util.Map;
import play.data.Form;
import play.data.FormFactory;
import play.i18n.MessagesApi;
import play.mvc.*;
import views.html.tweets_display;

import javax.inject.Inject;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

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
	
    private final Form<Search> form;
    private MessagesApi messagesApi;
    private HashMap<String,session_data> sessions;
    private Integer session_count;

    @Inject
    public HomeController(FormFactory formFactory, MessagesApi messagesApi) {
        this.form = formFactory.form(Search.class);
        this.messagesApi = messagesApi;
        this.session_count = 0;
        this.sessions =  new HashMap<String,session_data>();
    }
    
    public CompletionStage<Result> gettweet(Http.Request request) throws TwitterException{
    	final Form<Search> boundForm = form.bindFromRequest(request);

        if (boundForm.hasErrors()) {
            return CompletableFuture.completedFuture(badRequest(views.html.tweets_display.render(new ArrayList<String>(),new LinkedHashMap<String,String>(), boundForm, request, messagesApi.preferred(request))));
        } else {
            Search searchquery = boundForm.get();
             
	        String current_user = request.session().get("Twitter").get(); 
	        session_data current_session = getSession(current_user,false);
	        LinkedHashMap<String,String> cache = current_session.getCache();	
	        List<String> query = current_session.getQuery();
		    boolean alreadySearched = cache.containsKey(searchquery.getSearchString());  
		    if (alreadySearched) {  
		    		query.remove(searchquery.getSearchString());
		    		query.add(0,searchquery.getSearchString());
		        	return CompletableFuture.completedFuture(redirect(routes.HomeController.searchPage())
		        			.addingToSession(request, "Twitter", current_user)
		        			);
		    }
		    else {
		        query.add(0,searchquery.getSearchString()); 
			    return new GetTweets().GetTweets_keyword(searchquery.getSearchString())
			        	   .thenApply(tweet -> {tweet = "		<tr>\n" + 
								        				"			<th>Search terms:</th>\n" + 
								        				"			<th><a href=/keyword?s=" + searchquery.getSearchString().replaceAll(" ", "+") + ">" + searchquery.getSearchString() + "</a></th>\n" + 
								        				"		</tr>\n" + 
								        				"		<tr>\n" + 
								        				"			<th>User</th>\n" + 
								        				"			<th>Location</th>\n" + 
								        				"			<th>Tweet Text</th>\n" + 
								        				"		</tr>\n" + tweet;
			        		   					cache.put(searchquery.getSearchString(),tweet);
			        	   						return redirect(routes.HomeController.searchPage())
			        	   								.addingToSession(request, "Twitter", current_user)
			        	   								;});
			    /* if we have to add keyword hyperlink at the begining, then i couldnt find any way other than making it sequntial 
		         * and then reduce with initial value like below, but we will loose performance of parallel stream
		         * 
		         * another way to do is in controller, after tweets are fetched add in the begining but this will breach
		         * no business logic in controller rule
		         */     
		    }

        }      
    }
    
    public CompletionStage<Result> searchPage(Http.Request request) {
    	
    	return request
    		      .session()
    		      .get("Twitter")
    		      .map(user -> { 
    		    	  			session_data current_session = getSession(user,false);
				        		return CompletableFuture.completedFuture(ok(views.html.tweets_display.render(current_session.getQuery(),current_session.getCache(), form, request, messagesApi.preferred(request)))
				        					.addingToSession(request, "Twitter", user)
				        					);       			
        						})
    		      .orElseGet(() ->{
		    					 this.session_count += 1;
		    					 String newuser = "play" + Integer.toString(this.session_count);
    		    	  			 session_data new_session = getSession(newuser,true);
					    		 return CompletableFuture.completedFuture(ok(views.html.tweets_display.render(new_session.getQuery(),new_session.getCache(), form, request, messagesApi.preferred(request)))
					    				.addingToSession(request, "Twitter", newuser));
    		      				});
    }
    
    private session_data getSession(String user,boolean newsession) {
    	if (newsession) {
	    	LinkedHashMap<String,String> newcache =  new LinkedHashMap<String, String>();
	   	 	List<String> newquery = new ArrayList<String>();
			session_data new_session = new session_data(newquery,newcache);
			this.sessions.put(user,new_session);
			return new_session;
    	}
    	else {
    		session_data current_session = this.sessions.get(user);
    		if (current_session != null) {
    			return current_session;
    		}
    		else {
    			List<String> query = new ArrayList<String>();;
    			LinkedHashMap<String,String> cache = new LinkedHashMap<String, String>();
    			session_data s = new session_data(query,cache);
    			this.sessions.put(user,s);
    			return s;
    		}
    	}
    }

    public Result user(Http.Request request,String g) {
    	return ok("in user " + g);
    	
    }
    public Result location(Http.Request request,String g) {
    	return ok("in location " + g);
    	
    }
    public CompletionStage<Result> keyword(Http.Request request,String g) throws TwitterException{
    	return new GetTweets().GetKeywordStats(g)
	        	   .thenApply(wc -> { 
	        		   				 LinkedHashMap<String, Integer> sortedwc = new LinkedHashMap<>();
				        	         wc.entrySet()
				        	         .parallelStream()
				        	         .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
				        	         .forEachOrdered(swc -> sortedwc.put(swc.getKey(), swc.getValue()));
				        	         
				        	        String result = "";
					   				for(String s:sortedwc.keySet()) {
					   					result = result + "\n" + s + " \t\t: \t\t" + sortedwc.get(s);
				   					}
				        		   	return ok(views.html.wordstats.render(g,result));
				        		   	}
	        			   	);

    public Result keyword(Http.Request request,String g) {
    	return ok("in keyword " + g);
	}

    public Result hashtag(Http.Request request,String g) {
    	return ok("in hashtag " + g);
    	
    }
    

}
