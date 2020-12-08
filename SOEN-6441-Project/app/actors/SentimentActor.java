package actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Tables;
import models.GetTweets;
import twitter4j.Status;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/*** Acts as an sentiment actor and calculates sentiments after each tweet status is received*/
public class SentimentActor extends AbstractActor {
    /** ActorRef sender reference */
    private final ActorRef ws;
    /** ActorRef reply to  reference */
    private final ActorRef replyTo;

    /** Constructor for SentimentActor
     * 
     * @param ws
     * @param replyTo
     * @author suhel
     */
    public SentimentActor( ActorRef ws, ActorRef replyTo) {
        this.ws = ws;
        this.replyTo = replyTo;
    }

   /** Acts as an inbox */
    public static class tweetStatus{
        private final Status status;
        private final String searchString;

       /** constructor for Sentiment.ActortweetStatus
        * 
        * @param status
        * @param searchString
        * @author suhel
        */
       
        public tweetStatus(Status status,String searchString) {
            this.status = status;
            this.searchString=searchString;
        }
    }
    /** replies as this class */
    public static class storeSentiments {
        public String keyword;
        public long msgID;
        public String mode;

        /** Constructor for Sentiment.storeSentiments
         * 
         * @param keyword
         * @param msgID
         * @param mode
         * @author suhel
         */

        public storeSentiments(String keyword, long msgID, String mode) {
            this.keyword = keyword;
            this.msgID = msgID;
            this.mode = mode;
        }
    }

    /* calculates async modes of tweets*/
    
     
    public static class replyAnalysis {
        public String keyword;
        public HashBasedTable<String, Long, String> sentimentTable;

        /** constructor for Sentiment.replyAnalysis
         * 
         * @param keyword
         * @param sentimentTable
         * @author suhel
         */
        public replyAnalysis(String keyword, HashBasedTable<String, Long, String> sentimentTable) {
            this.keyword = keyword;
            this.sentimentTable = sentimentTable;
        }
    }

    /** Properties of the actor
     * 
     * @param ws
     * @param replyTo
     * @return
     * @author Suhel
     */
    public static Props props(ActorRef ws, ActorRef replyTo) {
        return Props.create(SentimentActor.class, () -> new SentimentActor( ws, replyTo));
    }

    /** inbox of the actor Sentiment.replyAnalysis
     * it can accept message as tweetStatus.class,replyAnalysis.class and any object
     * @return
     */
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(tweetStatus.class, msg->{
                    String mode=getTweetSentimentsPerTweets(msg.status);
                    getSender().tell(new storeSentiments(msg.searchString,msg.status.getId(),mode),getSelf());
                })
                .match(replyAnalysis.class, fut -> {
                    String result = analysedSentiment(fut.keyword, fut.sentimentTable);
                    getSender().tell(result, getSelf());
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

        //System.out.println("Single Tweets Analysis");
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
     * When asked by other actors, calculates sentiments and retunrs the mode afte each status is refreshed
     *
     * @param searchQuery ,sentimentTable
     * @return String
     */
    public String analysedSentiment(String searchQuery, HashBasedTable<String, Long, String> sentimentTable) {
//        System.out.println("Trying to analyse");
        //System.out.println("What was earlier thing?   "+GetTweets.initSentimentMap.get(searchQuery));
        try {
            String dynamicAnalytic = "";

            Map<String, Map<Long, String>> makePopulatedMap = Tables.unmodifiableTable(sentimentTable).rowMap();

            // THIS STREAM IS PROBLEM
            Map<String, Long> counted = makePopulatedMap.get(searchQuery).values().stream()
                    .collect(Collectors.groupingBy(s -> s, Collectors.counting()));
            //concurrent exception is just fine, it will just pass and try to recalculate next
            double totalSentiments = (counted.get("NEUTRAL") != null ? counted.get("NEUTRAL") : 0) + (counted.get("HAPPY") != null ? counted.get("HAPPY") : 0) +
                    (counted.get("SAD") != null ? counted.get("SAD") : 0);

            double neutralPercent = ((counted.get("NEUTRAL") != null ? counted.get("NEUTRAL") : 0) / totalSentiments);

            Double truncatedneutralPercent = BigDecimal.valueOf(neutralPercent * 100)
                    .setScale(3, RoundingMode.HALF_UP)
                    .doubleValue();

            double happyPercent = ((counted.get("HAPPY") != null ? counted.get("HAPPY") : 0) / totalSentiments);

            Double truncatedhappyPercent = BigDecimal.valueOf(happyPercent * 100)
                    .setScale(3, RoundingMode.HALF_UP)
                    .doubleValue();

            double sadPercent = ((counted.get("SAD") != null ? counted.get("SAD") : 0) / totalSentiments);
            Double truncatedsadPercent = BigDecimal.valueOf(sadPercent * 100)
                    .setScale(3, RoundingMode.HALF_UP)
                    .doubleValue();

            double thresHold = 70.0;

            if (truncatedsadPercent >= thresHold)
                dynamicAnalytic = "Overall Mode : SAD  \uD83D\uDE1E";
            else if (truncatedhappyPercent > thresHold)
                dynamicAnalytic = "Overall Mode : HAPPY \uD83D\uDE0A";
            else
                dynamicAnalytic = "Overall Mode : NEUTRAL \uD83D\uDE10";


            //System.out.println("Overall Mode : "+dynamicAnalytic);
            dynamicAnalytic = dynamicAnalytic + "  Total Tweets= " + totalSentiments;
            dynamicAnalytic = dynamicAnalytic + "  Happy percent=   " + truncatedhappyPercent;
            dynamicAnalytic = dynamicAnalytic + "  Sad percent=   " + truncatedsadPercent;
            //dynamicAnalytic = dynamicAnalytic + "  Neutral percent: " + truncatedneutralPercent;
            dynamicAnalytic = "<CUSTOMSENTIMENT>" + dynamicAnalytic + "</CUSTOMSENTIMENT>";
            //System.out.println(dynamicAnalytic);
            return dynamicAnalytic;

        } catch (Exception e) {
//            System.out.println("Will try next time");
        }
        return "<CUSTOMSENTIMENT> \uD83D\uDEA7 Sentimate Actor is trying to Analyse Sentiments...</CUSTOMSENTIMENT>";
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
