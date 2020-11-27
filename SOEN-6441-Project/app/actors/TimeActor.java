package actors;

import actors.UserActor.TimeMessage;
import akka.actor.AbstractActorWithTimers;
import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.Props;
import scala.concurrent.duration.Duration;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Time Actor keeps track of all userActors.
 */
public class TimeActor extends AbstractActorWithTimers {


    private static List<ActorRef> userActors = new ArrayList<>();

    public static Props getProps() {
        return Props.create(TimeActor.class);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(RegisterMsg.class, msg -> {
                    registerNewClient(sender());})
                .match(Tick.class, msg -> notifyClients())
                .build();
    }

    private void notifyClients() {
        TimeMessage tMsg = new TimeMessage(LocalDateTime.now().toString());
        userActors.forEach(ar -> ar.tell(tMsg, self()));
    }

    /**
     * Prestart start a time actor which periodically run a timer every 5 seconds
     * Prestart only executed once, and every time the timer run it call on the Tick Class.
     */
    @Override
    public void preStart() {
        System.out.println("Time Actor Prestart ");
        getTimers().startPeriodicTimer("Timer", new Tick(), Duration.create(2, TimeUnit.SECONDS));
    }

    private void registerNewClient(ActorRef sender){

        System.out.println("New User entered"); userActors.add(sender());
    }


    private static final class Tick {
    }

    static public class RegisterMsg {
    }
}
