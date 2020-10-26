package models;

import twitter4j.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.BinaryOperator;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class TwitterDataFetcher {

    private final Logger logger = LoggerFactory.getLogger(getClass()) ;




    public CompletionStage<String> fetchTwitterSearchCompleted(String requestString ) throws TwitterException {

        Twitter twitter = new TwitterFactory().getInstance();
        Query query = new Query(requestString);
        BinaryOperator<String> adder = (a, b) -> {
            return a + b;
        };
        query.count(10);
        QueryResult result;
        return CompletableFuture.completedFuture( String.join("\n \n",twitter.search(query).getTweets()
                .parallelStream().map(r -> r.getText()).collect(Collectors.toList())));



    }



    public List<String> fetchTwitterSearch(Query query) throws TwitterException {

        QueryResult result;
        //do {
        List<Status> tweets=new ArrayList<>();
        Twitter twitter = new TwitterFactory().getInstance();
        result = twitter.search(query);

        return result.getTweets().parallelStream().map(r -> r.getText()).collect(Collectors.toList());


    }


    public List<Status>  fetchTwitterSearch( String searchWord){

        List<Status> tweets=new ArrayList<>();

        //logger.info("","location:  " + System.getProperty("user.dir"));

        if (searchWord.length() < 1) {
            //logger.info("a","hello");
            //logger.info("b","java twitter4j.examples.search.SearchTweets [query]");
            System.exit(-1);
        }
        Twitter twitter = new TwitterFactory().getInstance();
        try {

            Query query = new Query(searchWord);
            query.count(10);
            QueryResult result;
            //do {

            result = twitter.search(query);

            tweets = result.getTweets();
            return tweets;


            //} while ((query = result.nextQuery()) != null);
            //
            // System.exit(0);
        } catch (TwitterException te) {
            te.printStackTrace();
            //logger.error("","Failed to search tweets->"+te.getMessage());
            System.exit(-1);
        }

          return tweets;
    }


    public List<Status> fetchTwitterByLocation(String stringQuery, GeoLocation searchLocationInput){





        List<Status> tweets=new ArrayList<>();

        //logger.info("","location:  " + System.getProperty("user.dir"));

//        if (searchWord.length() < 1) {
//            //logger.info("a","hello");
//            //logger.info("b","java twitter4j.examples.search.SearchTweets [query]");
//            System.exit(-1);
//        }
        Twitter twitter = new TwitterFactory().getInstance();
        try {

            Query query = new Query(stringQuery);
            query.count(10);
            query.geoCode(searchLocationInput,10, Query.Unit.km);
            QueryResult result;
            //do {

            result = twitter.search(query);

            tweets = result.getTweets();
            return tweets;


            //} while ((query = result.nextQuery()) != null);
            //
            // System.exit(0);
        } catch (TwitterException te) {
            te.printStackTrace();
            //logger.error("","Failed to search tweets->"+te.getMessage());
            System.exit(-1);
        }

        return tweets;
    }

}
