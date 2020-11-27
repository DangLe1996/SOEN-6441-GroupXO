package actors;

import akka.actor.AbstractActor;
import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.Props;
import twitter4j.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Keeps track of actors that subscribed to a search hashtag query and also which hashtag this actor is responsible for.
 */
public class HashtagActor extends AbstractActor {


    private final ActorRef ws; //keep track of actor ref

    public HashtagActor( ActorRef ws) {
        this.ws = ws;
        System.out.println("Hashtag Actor Created");

    }
    public static Props props(ActorRef wsout){

        return Props.create(HashtagActor.class,wsout);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(String.class,msg -> {
                    getTweeterStream(msg);
                    System.out.println("I got your message " + msg);
                    ws.tell("I received your message: " + msg, self());
                })

                .build();
    }

    private void getTweeterStream(String searchString){

        StatusListener listener = new StatusListener() {

            @Override
            public void onException(Exception e) {
                e.printStackTrace();
            }

            @Override
            public void onStatus(Status status) {
                ws.tell(formatResult.apply(status),self());
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

        TwitterStream twitterStream = new TwitterStreamFactory().getInstance();

        twitterStream.addListener(listener);


        twitterStream.filter(searchString);
        twitterStream.sample();


    }

    private Function<Status,String> formatResult = (s) -> {

                    return "\n" +
                            "<tr class=\"status\" >\n" +
                            "		<td><a href=/user?s=" + s.getUser().getScreenName().replaceAll(" ", "+") + "> " + s.getUser().getScreenName() + "</a></td>\n" +
                            "		<td><a href=/location?s=" + s.getUser().getLocation().replaceAll(" ", "+") + ">" + s.getUser().getLocation() + "</a></td>\n" +
                            "		<td>" + s.getText().replaceAll("#(\\w+)+", "<a href=/hashtag?hashTag=$1>#$1</a>") + "</td>\n" +
                            "</tr>\n";

    };
//    private static class getTweet{
//        private final ActorRef replyTo;
//        public  getTweet(String searchString, ActorRef replyTo){
//            this.replyTo = replyTo;
//            StatusListener listener = new StatusListener() {
//
//                @Override
//                public void onException(Exception e) {
//                    e.printStackTrace();
//                }
//
//                @Override
//                public void onStatus(Status status) {
//
//                }
//                @Override
//                public void onDeletionNotice(StatusDeletionNotice arg) {
//                }
//                @Override
//                public void onScrubGeo(long userId, long upToStatusId) {
//                }
//                @Override
//                public void onStallWarning(StallWarning warning) {
//                }
//
//                @Override
//                public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
//                }
//            };
//
//            TwitterStream twitterStream = new TwitterStreamFactory().getInstance();
//
//            twitterStream.addListener(listener);
//
//
//            twitterStream.filter(searchString);
//            twitterStream.sample();
//
//        }
//
//    }


}
