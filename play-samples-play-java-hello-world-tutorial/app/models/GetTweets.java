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
	
	public static CompletionStage<List<Status>> GetTweets_keyword(String keyword) throws TwitterException{	
        if (keyword.length() < 1) {
            System.exit(-1);
        }
        Twitter twitter = new TwitterFactory().getInstance();
        Query query = new Query(keyword + " -filter:retweets");
        query.count(10);
        query.lang("en");

        return CompletableFuture.completedFuture(twitter.search(query).getTweets()
				.parallelStream()
				.collect(Collectors.toList()));

    }

}
