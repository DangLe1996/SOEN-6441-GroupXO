package models;

import twitter4j.TwitterException;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class GetTweetsTest {

    @org.junit.jupiter.api.Test
    void getTweets_keyword() throws TwitterException {

//        var test = GetTweets.GetTweets_keyword("vietnam");


        String testString = " #Vietnam is #also good";

//        Arrays.stream(testString.split(" ")).filter();
        System.out.println(testString.replaceAll("#(\\w+)+", "<a href=@routes.HomeController.hashtag(\"$1\")>$1</a>"));

        String test2 = "5 * x^3 - 6 * x^1 + 1";
        test2.replaceAll("\\^([0-9]+)", "<sup>$1</sup>");
    }
}