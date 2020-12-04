package actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import com.fasterxml.jackson.databind.JsonNode;
import models.sessionData;
import play.libs.Json;
import twitter4j.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

/**
 * Keeps track of actors that subscribed to a search hashtag query and also which hashtag this actor is responsible for.
 */
public class HashtagActor extends AbstractActor {

    private final ActorRef ws; //keep track of actor ref
    private final ActorRef replyTo;


    List<String> lasTweets = new ArrayList<>();
    private String QueryString;

    public static Props props(ActorRef ws, ActorRef replyTo){

        return Props.create(HashtagActor.class,ws, replyTo);
    }

    public HashtagActor( ActorRef ws, ActorRef replyTo) {

        this.ws = ws;
        this.replyTo = replyTo;
        System.out.println("Hashtag Actor Created");

    }
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(String.class,msg -> {
                    QueryString = msg;

                    replyTo.tell(new TwitterStreamActor.registerNewHashtag(msg),getSelf());
                })
                .match(updateStatus.class,msg -> {
                    updateResult(msg);
                })
                .matchAny( msg -> {
                    addQueryFromJson(msg);
                })
                .matchEquals("KillSwitch", msg -> {
                    System.out.println("Actor terminated");
                    context().stop(self());})
                .build();

    }

    private void addQueryFromJson(Object msg){
        JSONObject obj = new JSONObject(msg.toString());
        String queryTerm = "#"+obj.getString("queryTerm");
        System.out.println("I got your queryterm type " + queryTerm);

        replyTo.tell(new TwitterStreamActor.registerNewHashtag(queryTerm),getSelf());

    }


    private void updateResult(updateStatus status){

        if(lasTweets.size()>=30){
            List<String> temp = lasTweets.stream().limit(10).collect(Collectors.toList());
            lasTweets = temp;

        }
        if(! lasTweets.contains(status)) {
            lasTweets.add(status.htmlCode);
            ws.tell(Json.toJson(status), ActorRef.noSender());
        }

    }

    public static class updateStatus{
        private final String htmlCode;
        private final String queryTerm;

        updateStatus(String htmlCode, String queryTerm) {
            this.htmlCode = htmlCode;
            this.queryTerm = queryTerm;
        }
    }




}
