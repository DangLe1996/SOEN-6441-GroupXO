package test.actors;
import actors.SentimentActor;
import actors.TwitterStreamActor;
import akka.actor.testkit.typed.CapturedLogEvent;
import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.testkit.javadsl.TestKit;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.AbstractActor;
import org.scalatestplus.junit.JUnitSuite;
import twitter4j.Status;

import java.time.Duration;
import java.util.List;
import static commons.CommonHelper.buildStatusList;
import static commons.CommonHelper.createMockTweets;
public class TwitterStreamActorTest extends JUnitSuite {



    static ActorSystem system;

    @BeforeClass
    public static void setup() {
        system = ActorSystem.create();
    }

    @AfterClass
    public static void teardown() {
        TestKit.shutdownActorSystem(system);
        system = null;
    }

    /* Tests the instance methods of the supervisor actor */

    @Test
    public void JunitTestSuperVisor_Sentiments(){
        new TestKit(system) {
            {

                final TestKit probe = new TestKit(system);
                final Props props = Props.create(TwitterStreamActor.class);
                final ActorRef subject = system.actorOf(props);

                SentimentActor.storeSentiments aSentiment;
                aSentiment = new SentimentActor.storeSentiments("TestKeyWord",1000L,"HAPPY");
                subject.tell(aSentiment,probe.getRef());
                //subject.tell("KillSwitch",probe.getRef());


             /*   within(
                        Duration.ofSeconds(10),
                        () -> {
                            awaitCond(probe::msgAvailable);
                            expectNoMessage();
                            return null;

                        });*/
            }
        };
    }

 }





