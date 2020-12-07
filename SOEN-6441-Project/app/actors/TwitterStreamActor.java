package actors;

import akka.actor.AbstractActorWithTimers;
import akka.actor.ActorRef;
import akka.actor.Props;
import static akka.pattern.PatternsCS.ask;
import com.google.common.collect.HashBasedTable;
import scala.concurrent.duration.Duration;
import twitter4j.*;

import javax.inject.Singleton;
import java.util.*;


import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

import java.util.concurrent.TimeoutException;

import static java.util.stream.Collectors.groupingBy;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

/**
 * Responsible for getting Twitter Streams. This actor will push new tweets to all subscribers.
 */
@Singleton
public class TwitterStreamActor extends AbstractActorWithTimers {


    /**
     * Keeps track of queue keywords. This queue will be emptied every 5 seconds
     */
    private Set<String> KeywordsQueue = new HashSet<>();


    TwitterStream twitterStream;

    /**
     * Key: keywords
     * Value: Actors that are subscribing to the keywords.
     */
    HashMap<String, List<ActorRef>> ChildActors = new HashMap<>();

    HashMap<String, ActorRef> KeyChildActors = new HashMap<>();

    HashBasedTable<String, Long, String> sentimentTable = HashBasedTable.create(); //suhel
    ActorRef sentiMentActor = getContext().actorOf(SentimentActor.props(self(), getSelf()));

    public static Props prop() {
        return Props.create(TwitterStreamActor.class);
    }

    private TwitterStreamActor() {
        System.out.println("Hashtag Parent Actor Created: " + getSelf().path());
        twitterStream = new TwitterStreamFactory().getInstance();

        initTwitterStream();
    }

    static class registerNewSearchQuery {
        private final String searchQuery;

        public registerNewSearchQuery(String searchQuery) {
            this.searchQuery = searchQuery;
        }
    }

    /**
     * Every 5 seconds, raise a Tick message to clear out the keyword queue.
     */
    @Override
    public void preStart() {
        getTimers().startPeriodicTimer("Timer", new Tick(), Duration.create(5, TimeUnit.SECONDS));
    }

    /**
     * trigger class
     */
    private static final class Tick {
    }

    /**
     * If match Tick class, update twitter stream.
     * If match RegisterNewSearch class, add the message into query queue
     * If match storeSentiments class, add the message into sentimentTable.
     * Remove child from list of subscriber if match removeChild class.
     * @return
     */
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Tick.class, msg -> updateTwitterStream())
                .match(registerNewSearchQuery.class, msg -> {
                    addNewQuery(msg.searchQuery);
                })
                .match(SentimentActor.storeSentiments.class, msg -> {
                    storeAnalysedSentiment(msg.keyword,msg.msgID,msg.mode);
                })
                .match(removeChild.class, msg -> removeChild(msg.actorRef))
                .build();
    }

    /**
     * Unsubscribe actor when they are terminated
     * @param actor
     */
    private void removeChild(ActorRef actor) {
        ChildActors.values().remove(actor);
    }

    /**
     * Add new keyword into queue when an actor request.
     * @param msg keyword to keep track
     */
    private void addNewQuery(String msg) {
        if(ChildActors.containsKey(msg)){
            List<ActorRef> temp = new ArrayList<>();
            temp.add(sender());
            ChildActors.get(msg).stream().forEach(r -> temp.add(r));
            ChildActors.remove(msg);
            ChildActors.put(msg, temp);
        }
        else {
            ChildActors.put(msg, List.of(sender()));
            KeywordsQueue.add(msg);
        }

        System.out.println("I got your query from Twitter " + msg);
    }

    /**
     * Message class to remove child from childActorRef List.
     */
    static class removeChild {
        private final ActorRef actorRef;
        removeChild(ActorRef actorRef) {
            this.actorRef = actorRef;
        }
    }

    /**
     * Update twitter stream by clearing out the KeywordsQueue and add the
     * child elements into twitterStream.filter. This method
     * is called every Tick(), which is 5 seconds.
     */
    private void updateTwitterStream() {
        if(KeywordsQueue.size() > 0) {
            FilterQuery filter = new FilterQuery();
            int n = KeywordsQueue.size();
            String arr[] = new String[n];
            arr = ChildActors.keySet().toArray(arr);
            filter.track(arr);
            KeywordsQueue.clear();
            twitterStream.filter(filter);
        }
    }

    /**
     * Initialize twitter stream by adding a Status listener into twitterStream.
     * This listener contain OnStatus method that is executed automatically every time a new status is received from TwitterStream.
     * When a new status is received, it checks the list of subscribers and the keyword that the subscriber is asking for.
     * If the status contain that keyword, it push the status to the subscriber.
     */
    public void initTwitterStream() {
        StatusListener listener = new StatusListener() {
            @Override
            public void onException(Exception e) {
                e.printStackTrace();
            }

            @Override
            public void onStatus(Status status) {

                String result = formatResult.apply(status);
                 ChildActors.entrySet().stream()
                        .filter(child -> result.contains(child.getKey()))
                        .findFirst().ifPresent( res -> {
                     try{
                         String finalResult = result;
                         outputAnalysedSentiment(res.getKey()).thenAccept(r ->
                                 res.getValue().forEach(ch ->
                                         ch.tell(new updateStatus(finalResult + r.toString(), res.getKey()), self())));
                     }catch(Exception e){
                         String appendResult="<CUSTOMSENTIMENT> \uD83D\uDEA7 Sentimate Actor is trying to Analyse Sentiments...</CUSTOMSENTIMENT>";
                         res.getValue().forEach(ch ->
                                 ch.tell(new updateStatus(result + appendResult, res.getKey()), self()));
                     }
                        });

                KeyChildActors.entrySet().forEach(child -> {
                    KeywordActor.updateStatus reply = new KeywordActor.updateStatus(status);
                    child.getValue().tell(reply, self());
                });
            }

            @Override
            public void onDeletionNotice(StatusDeletionNotice arg) {
            }

            @Override
            public void onScrubGeo(long userId, long upToStatusId) {
            }

            @Override
            public void onStallWarning(StallWarning warning) {
            }

            @Override
            public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
            }
        };

        twitterStream.addListener(listener);
    }

    /**
     * Response class to subscribers. Contain the html code and query term that is needed to update.
     */
    public static class updateStatus{
        public final String htmlCode;
        public final String queryTerm;
        updateStatus(String htmlCode, String queryTerm) {
            this.htmlCode = htmlCode;
            this.queryTerm = queryTerm;
        }
    }
    private Function<Status, String> formatResult = (s) -> {

        String userLocation = s.getUser().getLocation() != null ? s.getUser().getLocation() : " ";
        String userName = s.getUser().getScreenName() != null ? s.getUser().getScreenName() : " ";
        return
                "<tr class=\"status\" >\n" +
                        "		<td id=" + userName + " ><a href=/user?s=" + userName.replaceAll(" ", "+") + "> " + userName + "</a></td>\n" +
                        "		<td><a href=/location?s=" + userLocation.replaceAll(" ", "+") + ">" + userLocation + "</a></td>\n" +
                        "		<td>" + s.getText().replaceAll("#(\\w+)+", "<a href=/hashtag?hashTag=$1>#$1</a>") + "</td>\n" +
                        "</tr>\n";

    };

    /**
     * Tell the sentiment actor to analyze a particular status
     * @param searchWord
     * @param status
     */
    private void analyseSentiments(String searchWord, Status status) {
        sentiMentActor.tell(new SentimentActor.tweetStatus(status, searchWord), getSelf());
    }

    /**
     * Store the sentiment into Table
     * @param searchQuery
     * @param msgID
     * @param mode
     */
    private void storeAnalysedSentiment(String searchQuery, long msgID, String mode) {
        sentimentTable.put(searchQuery, msgID, mode);
    }


    /**
     * Ask the sentiment actor for a result from analyzing tweet request.
     * @param searchQuery key to retrieve sentimental for.
     * @return Completeable future that contain the string of sentimental
     */
    private CompletableFuture<Object> outputAnalysedSentiment(String searchQuery)  {
        HashBasedTable<String, Long, String>  copyOfSentiMentActor=sentimentTable;
        CompletionStage<Object> f = ask(sentiMentActor, new SentimentActor.replyAnalysis(searchQuery,copyOfSentiMentActor), 1000L);
        return f.toCompletableFuture();


    }
}
