package actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import com.google.common.collect.HashBasedTable;
import twitter4j.*;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.function.Function;
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
public class TwitterStreamActor extends AbstractActor {


    private Set<String> trackKeywords = new HashSet<>();

    TwitterStream twitterStream ;

    HashMap<String, ActorRef> ChildActors = new HashMap<>();
    HashMap<String, ActorRef> KeyChildActors = new HashMap<>();
    List<Status> sentimentTweets=new ArrayList<>();
    HashBasedTable<String, Long, String> sentimentTable = HashBasedTable.create(); //suhel

    public static Props prop (){
        return Props.create(TwitterStreamActor.class);
    }


    public Props propNewChild(ActorRef wsout){
        return  Props.create(HashtagActor.class,wsout,getSelf());

    }

    private TwitterStreamActor(){
        System.out.println("Hashtag Parent Actor Created: " + getSelf().path());
       twitterStream = new TwitterStreamFactory().getInstance();

        initTwitterStream();
    }


    static class registerNewHashtag{
        private final String hashtagString;
        public registerNewHashtag(String hashtagString) {
            this.hashtagString = hashtagString;
        }
    }

    static class registerNewSearchQuery{
        private final String searchQuery;
        public registerNewSearchQuery(String searchQuery) {
            this.searchQuery = searchQuery;
        }
    }
    static class registerNewKeyword{
        private final String keyword;
        public registerNewKeyword(String keyword) {
            this.keyword = keyword;
        }
    }

   //suhel
    static class storeSentiments{
        private final String keyword;
        private final long msgID;
        private final String mode;
        public storeSentiments(String keyword,long msgID, String mode) {
            this.keyword = keyword;
            this.msgID=msgID;
            this.mode=mode;
        }
    }
    //suhel



    @Override
    public Receive createReceive() {
        return receiveBuilder()

                .match(registerNewHashtag.class, msg ->{
                    addNewHashtag(msg.hashtagString);
                        })
                .match(registerNewSearchQuery.class, msg ->{
                    addNewQuery(msg.searchQuery);
                })
                .match(registerNewKeyword.class, msg ->{
                    addNewKeyword(msg.keyword);
                })
                .match(removeChild.class, msg -> removeChild(msg.actorRef))
                .build();
    }

    private void removeChild(ActorRef actor){
        ChildActors.values().remove(actor);
    }
    private void addNewQuery(String msg){
        ChildActors.put(msg,sender());
        trackKeywords.add(msg);
        updateTwitterStream();
        System.out.println("I got your query from Twitter " + msg);
    }
    private void addNewHashtag(String msg){
        ChildActors.put(msg,sender());
        trackKeywords.add("#"+msg);
        updateTwitterStream();
        System.out.println("I got your hashtag " + msg);
    }
    private void addNewKeyword(String msg){
        KeyChildActors.put(msg,sender());
        trackKeywords.add(msg);
        updateTwitterStream();
        System.out.println("I got your keyword " + msg);
    }

    static class removeChild{
        private final ActorRef actorRef;
        removeChild(ActorRef actorRef) {
            this.actorRef = actorRef;
        }
    }

    private void updateTwitterStream(){

        FilterQuery filter = new FilterQuery();
        int n = trackKeywords.size();
        String arr[] = new String[n];
        arr = trackKeywords.toArray(arr);
        System.out.println("Twitter stream have " + arr);
        filter.track( arr);
        twitterStream.filter(filter);

    }


    public void initTwitterStream(){
        StatusListener listener = new StatusListener() {

            @Override
            public void onException(Exception e) {
                e.printStackTrace();
            }

            @Override
            public void onStatus(Status status) {

                ChildActors.entrySet().forEach(child -> {

                    analyseSentiments(child.getKey(),status);
                    outputAnalysedSentiment(child.getKey());// good to know after each tweet stream
                    System.out.println(" value: :" + child.getValue());
                    String result = formatResult.apply(status);

                    System.out.println(" checkpoint 1");


                    if(result.contains(child.getKey())) {
                    	HashtagActor.updateStatus reply = new HashtagActor.updateStatus(formatResult.apply(status));
                        child.getValue().tell(reply, self());
                    }
                });
                KeyChildActors.entrySet().forEach(child -> {
                	//System.out.println("in status " );
                	//List<String> result = GetKeywordStats(status);
                    System.out.println(" checkpoint 2");

                	KeywordActor.updateStatus reply = new KeywordActor.updateStatus(status);
                    child.getValue().tell(reply, self());


                ChildActors.entrySet().forEach(child -> {
                    String result = formatResult.apply(status);
                    if(result.contains(child.getKey())) {

                        var reply = new HashtagActor.updateStatus(formatResult.apply(status), child.getKey());
                        child.getValue().tell(reply, self());
                    }
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

    private Function<Status,String> formatResult = (s) -> {

        String userLocation = s.getUser().getLocation() != null ? s.getUser().getLocation() :  " ";
        String userName = s.getUser().getScreenName() != null ? s.getUser().getScreenName() :  " ";
        return
                "<tr class=\"status\" >\n" +
                        "		<td id="+userName+" ><a href=/user?s=" + userName.replaceAll(" ", "+") + "> " +userName + "</a></td>\n" +
                        "		<td><a href=/location?s=" +userLocation.replaceAll(" ", "+") + ">" + userLocation + "</a></td>\n" +
                        "		<td>" + s.getText().replaceAll("#(\\w+)+", "<a href=/hashtag?hashTag=$1>#$1</a>") + "</td>\n" +
                        "</tr>\n";

    };

    private void analyseSentiments(String searchWord, Status status){
        System.out.println("Call sentiment actor now 1234  ********************************** "+ searchWord);
        ActorRef sentiMentActor = getContext().actorOf(SentimentActor.props(self(),getSelf()));
        sentiMentActor.tell(new SentimentActor.tweetStatus(status,searchWord),getSelf());
    }
    private void storeAnalysedSentiment(String searchQuery,long msgID, String mode){
        System.out.println("I will store here each sentiment ");
        sentimentTable.put(searchQuery,msgID,mode);
    }

    private void outputAnalysedSentiment(String searchQuery){
        System.out.println("I will output  here each sentiments after each streaming");
        int happy=0;
        int sad=0;
        String dynamicAnalytic="";

        for (Entry<Long, String> row : sentimentTable.row(searchQuery).entrySet()) {
            if (row.getValue().equals("HAPPY"))
                happy=happy+1;
            if (row.getValue().equals("SAD"))
                sad=sad+1;
        }
        dynamicAnalytic=dynamicAnalytic+"  Total Tweets analysed : "  +sentimentTable.row(searchQuery).size();
        dynamicAnalytic=dynamicAnalytic+"  Total Happy Tweets : "  +happy;
        dynamicAnalytic=dynamicAnalytic+"  Total Sad Tweets : "    +sad;
        dynamicAnalytic=dynamicAnalytic+"  Total Neutral Tweets : "  +( sentimentTable.row(searchQuery).size()-happy+sad);
        System.out.println(dynamicAnalytic);



    }

}
