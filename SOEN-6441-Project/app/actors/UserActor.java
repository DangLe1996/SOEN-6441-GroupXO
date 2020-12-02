package actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import com.fasterxml.jackson.databind.JsonNode;
import models.GetTweets;
import models.sessionData;
import play.libs.Json;
import twitter4j.JSONObject;


import java.util.concurrent.CompletionStage;

public final class UserActor extends AbstractActor {

    private final ActorRef wsout; //keep track of actor ref
    private final String userID;
    private final GetTweets globalGetTweet;
    private final ActorRef TwitterStreamActor;
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
                .matchAny(msg -> {

                    addQueryFromJson(msg);
                })
                .match(addQuery.class, msg -> {
                    System.out.println("I got your json " + msg.queryTerm);
                })
                .build();
    }


    private void addQueryFromJson(Object msg){
        JSONObject obj = new JSONObject(msg.toString());
        String queryTerm = obj.getString("queryTerm");

        System.out.println("I got your queryterm type " + queryTerm);

        CompletionStage<sessionData> f =  globalGetTweet.GetTweetsWithUser(queryTerm,userID);
        f.thenAccept(sess ->{
            String htmlCode = sess.getCache().get(queryTerm);


            JsonNode personJson = Json.toJson( new AddNewQuery(htmlCode,queryTerm));
            wsout.tell(personJson,ActorRef.noSender());

        } );

    }


    private void addNewQuery(String query){


        TwitterStreamActor.tell( new TwitterStreamActor.registerNewSearchQuery(query ),getSelf());
        wsout.tell("I got your message " + query, ActorRef.noSender());
//        ws.tell("I got your message " + query,ActorRef.noSender());
//        ParentActor.tell(new HashtagActorParent.registerNewSearchQuery(query),getSelf());
    }


    public static class addQuery{

        private final String queryTerm;

        public addQuery(String queryTerm) {
            this.queryTerm = queryTerm;
        }
    }

    public static class AddNewQuery {
        private final String htmlCode;
        private final String queryTerm;
        private final String type = "AddNewQuery";
        AddNewQuery(String htmlCode, String queryTerm) {
            this.htmlCode = htmlCode;
            this.queryTerm = queryTerm;
        }
    }

    @Override
    public void postStop() throws Exception, Exception {
        System.out.println("actor ref removed");
        TwitterStreamActor.tell( new TwitterStreamActor.removeChild(getSelf()),getSelf());
    }
}