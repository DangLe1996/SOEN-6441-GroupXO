package actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import play.libs.Json;
import twitter4j.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Keeps track of actors that subscribed to a search hashtag query and also which hashtag this actor is responsible for.
 */
public class HashtagActor extends AbstractActor {

    private final ActorRef ws; //keep track of actor ref
    private final ActorRef TwitterStreamActor;


    List<TwitterStreamActor.updateStatus> lasTweets = new ArrayList<>();


    /**
     * Template on how to create Hashtag Actor
     * @param ws Websocket Actor created by Play
     * @param TwitterStreamActor Actor for twitterStream
     * @return
     */
    public static Props props(ActorRef ws, ActorRef TwitterStreamActor){
        return Props.create(HashtagActor.class,ws, TwitterStreamActor);
    }

    /**
     * Factory method to create Hashtag Actor.
     * @param ws
     * @param TwitterStreamActor
     */
    private HashtagActor( ActorRef ws, ActorRef TwitterStreamActor) {
        this.ws = ws;
        this.TwitterStreamActor = TwitterStreamActor;
        System.out.println("Hashtag Actor Created");
    }

    /**
     * If match with the UpdateStatus class from Twitter Stream Actor, push the new status onto websocket
     * If does not match with any classes from above, means the message come from Websocket Actor and thus
     * call the addQuery from Json to parse the message
     * @return
     */
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(TwitterStreamActor.updateStatus.class, msg -> {
                    updateResult(msg);
                })
                .matchAny( msg -> {
                    addQueryFromJson(msg);
                })
                .build();

    }

    /**
     * Parse the given json object and take the query term from the given object.
     * @param msg Json object from Front End
     */
    private void addQueryFromJson(Object msg){
        JSONObject obj = new JSONObject(msg.toString());
        String queryTerm = "#"+obj.getString("queryTerm");
        System.out.println("I got your queryterm type " + queryTerm);
        TwitterStreamActor.tell(new TwitterStreamActor.registerNewHashtag(queryTerm),getSelf());

    }

    /**
     * Keeps tracks of 30 latest status to prevent duplication when twitter return multiple same tweets.
     * If the status was not in the last 30, then push the string onto Front End.
     * Once the size of cache is more than 30, reduce it back to 10 to save space.
     * @param status New status to push to front end
     */
    private void updateResult(TwitterStreamActor.updateStatus status){

        if(lasTweets.size()>=30){
            List<TwitterStreamActor.updateStatus > temp = lasTweets.stream().limit(10).collect(Collectors.toList());
            lasTweets = temp;

        }
        if(! lasTweets.contains(status)) {
            lasTweets.add(status);
            ws.tell(Json.toJson(status), ActorRef.noSender());
        }

    }

    /**
     * Before stopping, tell TwitterStreamActor to remote itself to save memory space.
     */
    @Override
    public void postStop()    {
        System.out.println("actor ref removed");
        TwitterStreamActor.tell( new TwitterStreamActor.removeChild(getSelf()),getSelf());
    }






}
