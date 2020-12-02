package actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import twitter4j.*;

import javax.inject.Inject;
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



    @Override
    public Receive createReceive() {
        return receiveBuilder()

                .match(registerNewHashtag.class, msg ->{
                    addNewHashtag(msg.hashtagString);
                        })
                .match(registerNewSearchQuery.class, msg ->{
                    addNewQuery(msg.searchQuery);
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
                    String result = formatResult.apply(status);
                    if(result.contains(child.getKey())) {
                        var reply = new HashtagActor.updateStatus(formatResult.apply(status));
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
}
