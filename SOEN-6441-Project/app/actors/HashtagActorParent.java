package actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import twitter4j.*;

import javax.inject.Singleton;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

@Singleton
public class HashtagActorParent extends AbstractActor {


    private Set<String> trackKeywords = new HashSet<>();
    private TwitterStream twitterStream ;

    HashMap<String, ActorRef> ChildActors = new HashMap<>();

    public static Props prop (){
        return Props.create(HashtagActorParent.class);
    }


    public Props propNewChild(ActorRef wsout){
        return  Props.create(HashtagActor.class,wsout,getSelf());

    }

    private HashtagActorParent(){
        System.out.println("Hashtag Parent Actor Created: " + getSelf().path());
        twitterStream = new TwitterStreamFactory().getInstance();
    }


    static class registerNewHashtag{
        private final String hashtagString;
        public registerNewHashtag(String hashtagString) {
            this.hashtagString = hashtagString;
        }
    }

    public static class registerNewChild{
        private final ActorRef wsout;

        public registerNewChild(ActorRef wsout) {
            this.wsout = wsout;
        }
    }


    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(registerNewChild.class, msg -> {
                    sender().tell(propNewChild(msg.wsout),getSelf());
                })
                .match(registerNewHashtag.class, msg ->{
                    ChildActors.put(msg.hashtagString,sender());
                    trackKeywords.add("#"+msg.hashtagString);
                    updateTwitterStream();
                    System.out.println("I got your message " + msg.hashtagString);
                        })

                .build();
    }

    private void updateTwitterStream(){


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
        twitterStream.cleanUp();
        twitterStream.addListener(listener);
        FilterQuery filter = new FilterQuery();
        int n = trackKeywords.size();
        String arr[] = new String[n];
        arr = trackKeywords.toArray(arr);

        System.out.println("Twitter stream have " + arr);
        filter.track( arr);
        twitterStream.filter(filter);

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
