package actors;

import akka.actor.AbstractActor;
import akka.actor.AbstractActorWithTimers;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.pattern.Patterns;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Tables;
import scala.concurrent.Await;
import scala.concurrent.duration.Duration;
import twitter4j.*;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;
import scala.concurrent.Await;
import scala.concurrent.Promise;
import akka.util.Timeout;
import java.util.concurrent.TimeUnit;

import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.groupingBy;
import java.util.stream.Stream;
import java.util.Map.Entry;
import twitter4j.*;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

@Singleton
public class TwitterStreamActor extends AbstractActorWithTimers {


    private Set<String> KeywordsQueue = new HashSet<>();

    private Set<String> trackedKeywords = new HashSet<>();

    TwitterStream twitterStream;

    HashMap<String, ActorRef> ChildActors = new HashMap<>();
    HashMap<String, ActorRef> KeyChildActors = new HashMap<>();
    List<Status> sentimentTweets = new ArrayList<>();
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

    static class registerNewHashtag {
        private final String hashtagString;

        public registerNewHashtag(String hashtagString) {
            this.hashtagString = hashtagString;
        }
    }

    static class registerNewSearchQuery {
        private final String searchQuery;

        public registerNewSearchQuery(String searchQuery) {
            this.searchQuery = searchQuery;
        }
    }

    static class registerNewKeyword {
        private final String keyword;

        public registerNewKeyword(String keyword) {
            this.keyword = keyword;
        }
    }



    @Override
    public void preStart() {
        getTimers().startPeriodicTimer("Timer", new Tick(), Duration.create(5, TimeUnit.SECONDS));
    }

    private static final class Tick {
    }


    @Override
    public Receive createReceive() {
        return receiveBuilder()

                .match(registerNewHashtag.class, msg -> {
                    addNewHashtag(msg.hashtagString);
                })
                .match(Tick.class, msg -> updateTwitterStream())
                .match(registerNewSearchQuery.class, msg -> {
                    addNewQuery(msg.searchQuery);
                })
                .match(registerNewKeyword.class, msg -> {
                    addNewKeyword(msg.keyword);
                })
                .match(SentimentActor.storeSentiments.class, msg -> {
                    //System.out.println("Sentiment actor said : "+msg.mode);
                    storeAnalysedSentiment(msg.keyword,msg.msgID,msg.mode);
                }) //suhel
                .match(removeChild.class, msg -> removeChild(msg.actorRef))
                .build();
    }
    private void removeChild(ActorRef actor) {
        ChildActors.values().remove(actor);
    }

    private void addNewQuery(String msg) {
        ChildActors.put(msg, sender());
        if(trackedKeywords.contains(msg) == false) KeywordsQueue.add(msg);
        System.out.println("I got your query from Twitter " + msg);
    }
    private void addNewHashtag(String msg) {
        ChildActors.put(msg, sender());
        if(trackedKeywords.contains(msg) == false) KeywordsQueue.add(msg);
        System.out.println("I got your hashtag " + msg);
    }
    private void addNewKeyword(String msg) {
        KeyChildActors.put(msg, sender());
        if(trackedKeywords.contains(msg) == false) KeywordsQueue.add(msg);
        System.out.println("I got your keyword " + msg);
    }

    static class removeChild {
        private final ActorRef actorRef;
        removeChild(ActorRef actorRef) {
            this.actorRef = actorRef;
        }
    }
    private void updateTwitterStream() {
        System.out.println("updateing stream");
        if(KeywordsQueue.size() > 0) {
            trackedKeywords.addAll(KeywordsQueue);
            FilterQuery filter = new FilterQuery();
            int n = KeywordsQueue.size();
            String arr[] = new String[n];
            arr = KeywordsQueue.toArray(arr);
            System.out.println("Twitter stream have " + Arrays.stream(arr).reduce(String::join));
            filter.track(arr);
            KeywordsQueue.clear();
            twitterStream.filter(filter);
        }
    }


    public void initTwitterStream() {
        StatusListener listener = new StatusListener() {

            @Override
            public void onException(Exception e) {
                e.printStackTrace();
            }

            @Override
            public void onStatus(Status status) {
                ChildActors.entrySet().forEach(child -> {

                    analyseSentiments(child.getKey(), status);
                    // good to know after each tweet stream
                    System.out.println(" value: :" + child.getValue());
                    String result = formatResult.apply(status);
                    String appendResult="";

                    try{
                        appendResult=outputAnalysedSentiment(child.getKey());
                    }catch(Exception e){
                        appendResult="<CUSTOMSENTIMENT> \uD83D\uDEA7 Sentimate Actor is trying to Analyse Sentiments...</CUSTOMSENTIMENT>";
                    }

                    result=result+appendResult;//suhel



                    if (result.contains(child.getKey())) {

                        child.getValue().tell(new updateStatus(result, child.getKey()), self());
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

    private void analyseSentiments(String searchWord, Status status) {
        sentiMentActor.tell(new SentimentActor.tweetStatus(status, searchWord), getSelf());
    }

    private void storeAnalysedSentiment(String searchQuery, long msgID, String mode) {
        sentimentTable.put(searchQuery, msgID, mode);
    }


    private String outputAnalysedSentiment(String searchQuery) throws TimeoutException, InterruptedException {

        HashBasedTable<String, Long, String>  copyOfSentiMentActor=sentimentTable;
        Future f = Patterns.ask(sentiMentActor, new SentimentActor.replyAnalysis(searchQuery,copyOfSentiMentActor), 1000L);
        String result = (String) Await.result(f, Duration.create(10, "second"));
        return result;
    }
}
