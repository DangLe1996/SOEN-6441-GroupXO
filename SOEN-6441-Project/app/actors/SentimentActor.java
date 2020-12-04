package actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import models.GetTweets;
import twitter4j.Status;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;

public class SentimentActor extends AbstractActor {
    private final ActorRef ws;
    private final ActorRef replyTo;

    public SentimentActor( ActorRef ws, ActorRef replyTo) {
        this.ws = ws;
        this.replyTo = replyTo;
    }

    public static class tweetsSet{
        private final String queryTerm;
        private final List<Status> allTweets;

        public tweetsSet(String queryTerm, List<Status> allTweets ) {
            this.queryTerm = queryTerm;
            this.allTweets=allTweets;
        }
    }
    public static class tweetStatus{
        private final Status status;
        private final String searchString;

        public tweetStatus(Status status,String searchString) {
            this.status = status;
            this.searchString=searchString;
        }
    }

    public static Props props(ActorRef ws, ActorRef replyTo) {
        return Props.create(SentimentActor.class, () -> new SentimentActor( ws, replyTo));
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(tweetStatus.class, msg->{
                    System.out.println("I got a Single  tweets for ");
                    String mode=getTweetSentimentsPerTweets(msg.status);
                    System.out.println("Mode of the Tweet is "+ mode);
                    ws.tell(new TwitterStreamActor.storeSentiments(msg.searchString,msg.status.getId(),mode),getSelf());
                })
                .matchAny(msg -> {
                    System.out.println("got the Bunch of Tweets");
                })
                .build();
    }

    /**
     * Returns a String to parent Actor after matching a tweet text with provided list of happy/sad strings
     * @param singleTweets
     * @return String
     */

    private String getTweetSentimentsPerTweets(Status singleTweets){

        System.out.println("Single Tweets Analysis");
        if (stringContainsItemFromList(singleTweets.getText(), happy))
            return GetTweets.Mode.HAPPY.toString();
        else if (stringContainsItemFromList(singleTweets.getText(), sad))
            return GetTweets.Mode.SAD.toString();
        else return GetTweets.Mode.NEUTRAL.toString();
    }


    /**
     * Returns a boolean after matching a tweet text with provided list of happy/sad strings
     * @param inputStr ,items
     * @return boolean
     */
    public static boolean stringContainsItemFromList(String inputStr, String[] items) {
        return Arrays.stream(items).anyMatch(inputStr.toUpperCase() ::contains);
    }

    /**
     * User Defined array of words which when present marks a tweet as happy
     */
    private final static String[] happy = {"HAPPY", ":)", ":D", "<3", "PARTY", "😭","💜", "😀","\uD83D\uDC97\uD83D\uDC93","APPRECIATE","\uD83D\uDC9A","\uD83D\uDC4F","\uD83E\uDDE1","\uD83D\uDC9B","\uD83D\uDC9A","\uD83D\uDC9C","\uD83E\uDD70","\uD83D\uDDA4"};
    /**
     * User Defined array of words which when present marks a tweet as sad
     */
    private final static String[] sad = {"SAD", "ANGRY", ":(", "MAD", "DISAPPOINTMENT", "BAD DAY","\uD83D\uDCCA","\uD83D\uDE1E","\uD83D\uDE14"};
  /**
     * enum of mode of a tweet HAPPY, SAD , NEUTRAL
     */
    public enum Mode {
        HAPPY, SAD, NEUTRAL
    }
}