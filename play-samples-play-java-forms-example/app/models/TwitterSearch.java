package models;
import twitter4j.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class TwitterSearch {

    //private final Logger logger = LoggerFactory.getLogger(getClass()) ;

    public String getScreenName() {
        return screenName;
    }

    public TwitterSearch(String screenName, String tweetText) {
        this.screenName = screenName;
        this.tweetText = tweetText;
    }

    public void setScreenName(String screenName) {
        this.screenName = screenName;
    }

    public String screenName;

    public String getTweetText() {
        return tweetText;
    }

    public void setTweetText(String tweetText) {
        this.tweetText = tweetText;
    }

    public String tweetText;


}
