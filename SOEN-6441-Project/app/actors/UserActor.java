package actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import models.sessionData;

public final class UserActor extends AbstractActor {

    private final ActorRef ws; //keep track of actor ref
    private final sessionData userData;

    public UserActor(ActorRef ws, sessionData userData) {
        this.ws = ws;
        this.userData = userData;
    }

    public static Props props( ActorRef ws, sessionData sessionData ){
        return Props.create(UserActor.class,ws, sessionData);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(String.class, this::addNewQuery )
                .build();
    }

    private void addNewQuery(String query){

    }
}