package actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import com.fasterxml.jackson.databind.JsonNode;
import models.GetTweets;
import models.sessionData;
import play.libs.Json;
import twitter4j.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;
import static actors.TwitterStreamActor.*;
public final class UserActor extends AbstractActor {

    private final ActorRef wsout; //keep track of actor ref
    private final String userID;
    private final GetTweets globalGetTweet;
    private final ActorRef TwitterStreamActor;
    List<updateStatus> lasTweets = new ArrayList<>();
    public static Props props(ActorRef wsout, String userID, GetTweets globalGetTweet, ActorRef streamParentActor){

        return Props.create(UserActor.class, wsout,userID, globalGetTweet,streamParentActor);
    }

    private UserActor(ActorRef wsout, String userID, GetTweets globalGetTweet, ActorRef streamParentActor) {
        this.wsout = wsout;
        this.userID = userID;
        this.globalGetTweet = globalGetTweet;
        TwitterStreamActor = streamParentActor;

        System.out.println("New user actor created" + getSelf().toString());

    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(updateStatus.class, msg -> {
                    updateResult(msg);
                })
                .matchAny(msg -> {
                    addQueryFromJson(msg);
                })

                .build();
    }

    private void updateResult(updateStatus status) {
        if(lasTweets.size()>=30){
            List<updateStatus> temp = lasTweets.stream().limit(10).collect(Collectors.toList());
            lasTweets = temp;
        }
        if(! lasTweets.contains(status)) {
            lasTweets.add(status);
            JsonNode newQueryJson = Json.toJson( new AddNewQuery(status.htmlCode,status.queryTerm,"UpdateQuery"));
            wsout.tell(newQueryJson,ActorRef.noSender());

        }
    }

    private void addQueryFromJson(Object msg){
        JSONObject obj = new JSONObject(msg.toString());
        String queryTerm = obj.getString("queryTerm");
        System.out.println("I got your queryterm type " + queryTerm);
        TwitterStreamActor.tell(new TwitterStreamActor.registerNewSearchQuery(queryTerm),getSelf());
        CompletionStage<sessionData> f =  globalGetTweet.GetTweetsWithUser(queryTerm,userID);
        f.thenAccept(sess ->{
            String htmlCode = sess.getCache().get(queryTerm);
            JsonNode newQueryJson = Json.toJson( new AddNewQuery(htmlCode,queryTerm,"AddNewQuery"));
            wsout.tell(newQueryJson,getSelf());
        } );

    }

    public static class AddNewQuery {
        public final String htmlCode;
        public final String queryTerm;
        public final String type ;
        AddNewQuery(String htmlCode, String queryTerm, String type) {
            this.htmlCode = htmlCode;
            this.queryTerm = queryTerm;
            this.type = type;

        }
    }
    @Override
    public void postStop() throws Exception, Exception {
        System.out.println("actor ref removed");
        TwitterStreamActor.tell( new TwitterStreamActor.removeChild(getSelf()),getSelf());
    }
}