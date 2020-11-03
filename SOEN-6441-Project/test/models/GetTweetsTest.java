package models;

import org.junit.jupiter.api.Test;
import twitter4j.TwitterException;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

class GetTweetsTest {

    @org.junit.jupiter.api.Test
    void getTweets_keyword() throws TwitterException, ExecutionException, InterruptedException {

        //create a fake twitter and pass into gettweets

        /*

        after you create fake twitter, then you need to mock the twitter.search which return "acds"

        after that, you can call GetTweets_keyword(test), which will return a completable future a.

        a.CompletedFuture.get(), then you should get "acds"




         */

    }


}