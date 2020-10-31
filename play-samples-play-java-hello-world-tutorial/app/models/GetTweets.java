package models;



import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;


import twitter4j.*;

public class GetTweets {

	
	public GetTweets() {

	}
	
	public static CompletionStage<String> GetTweets_keyword(String keyword) throws TwitterException{	
        if (keyword.length() < 1) {
            System.exit(-1);
        }
        Twitter twitter = new TwitterFactory().getInstance();
        Query query = new Query(keyword);
        query.count(10);
        query.lang("en");
        QueryResult result; 

        return CompletableFuture.completedFuture(twitter.search(query).getTweets()
				.parallelStream()
				.map(s -> {
					return "\n" +
						   "<tr>\n" + 
							"		<td><a href=/user?s=" + s.getUser().getScreenName().replaceAll(" ", "+") + "> " + s.getUser().getScreenName() + "</a></td>\n" + 
							"		<td><a href=/location?s=" + s.getUser().getLocation().replaceAll(" ", "+") + ">" + s.getUser().getLocation() + "</a></td>\n" + 
							"		<td>" + s.getText().replaceAll("#(\\w+)+", "<a href=/hashtag?s=$1>#$1</a>") + "</td>\n" +
							"</tr>\n"; 
						})
        		.reduce("",
        				String::concat));
        
        /* There are 2 ways of doing this as per me,
         * if we have to add keyword hyperlink at the begining, then i couldnt find any way other than making it sequntial 
         * and then reduce with initial value like below, but we will loose performance of parallel stream
         * 
         * another way to do is in controller, after tweets are fetched add in the begining but this will breach
         * no business logic in controller rule
         *         return CompletableFuture.completedFuture(twitter.search(query).getTweets()
				.parallelStream()
				.map(s -> {
					return "\n" +
						   "<tr>\n" + 
							"		<td><a href=/user?s=" + s.getUser().getScreenName().replaceAll(" ", "+") + "> " + s.getUser().getScreenName() + "</a></td>\n" + 
							"		<td><a href=/location?s=" + s.getUser().getLocation().replaceAll(" ", "+") + ">" + s.getUser().getLocation() + "</a></td>\n" + 
							"		<td>" + s.getText().replaceAll("#(\\w+)+", "<a href=/hashtag?s=$1>#$1</a>") + "</td>\n" +
							"</tr>\n"; 
						})
				.sequential()
        		.reduce("		<tr>\n" + 
        				"			<th>Search terms:</th>\n" + 
        				"			<th><a href=/keyword?s=" + keyword.replaceAll(" ", "+") + "'>" + keyword + "</a></th>\n" + 
        				"		</tr>\n" + 
        				"		<tr>\n" + 
        				"			<th>User</th>\n" + 
        				"			<th>Location</th>\n" + 
        				"			<th>Tweet Text</th>\n" + 
        				"		</tr>\n",
        				String::concat));
         */

    }

}
	