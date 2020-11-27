package actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;

public final class UserActor extends AbstractActor {


    private final ActorRef ws; //keep track of actor ref

    public UserActor(ActorRef ws) {
        System.out.println("New User Actor Created " + ws.hashCode());
        this.ws = ws;
    }

    /**
     * To Generate new User Actor.
     * @param wsout
     * @return
     */
    public static Props props (ActorRef wsout){

        return Props.create(UserActor.class, wsout); //props call UserActor Constructor, with the parameter of wsout. Then the wsout initialize ws.
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(TimeMessage.class, this::sendTime)
                .match(HashtagStatus.class,this::sendHashtag)
                .build();
    }

    /**
     * Every time a new user enter, tell the timeActor Register Msg class and pass in self as parameter.
     */
    @Override
    public void preStart() {
        System.out.println("New Time Actor Created " + self().hashCode());
        context().actorSelection("/user/timeActor/")
                .tell(new TimeActor.RegisterMsg(), self());
    }

    private void sendHashtag(HashtagStatus mesg){
        final ObjectNode response = Json.newObject();
        response.put("hastagStatus",mesg.status);
        ws.tell(response,self());
    }

    private void sendTime(TimeMessage msg){
        final ObjectNode response = Json.newObject();
        response.put("time",msg.time);
        ws.tell(response,self());
    }

    public static class TimeMessage{
        public final String time;

        public TimeMessage(String time) {
            this.time = time;
        }
    }

    public static class HashtagStatus{
        public final String status;

        public HashtagStatus(String status) {
            this.status = status;
        }
    }



}