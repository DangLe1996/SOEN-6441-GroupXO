package models;
import twitter4j.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class TwitterDataFetcher {

    private final Logger logger = LoggerFactory.getLogger(getClass()) ;


    public void fetchTwitterSearch1(String searchWord){

    }


    public List<Status>  fetchTwitterSearch(String searchWord){


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

}
