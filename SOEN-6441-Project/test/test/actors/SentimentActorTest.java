package test.actors;
import actors.SentimentActor;
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
public class SentimentActorTest extends JUnitSuite {



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

    @Test
    public void testSentimentActor() {

        System.out.println("testing testSentimentActor");

        new TestKit(system) {
            {

                final TestKit probe = new TestKit(system);
                final Props props = Props.create(SentimentActor.class,probe.getRef(),probe.getRef());
                final ActorRef subject = system.actorOf(props);

                List<Status> fakeTweets=createMockTweets(1,1,1);
                Status aStatus=fakeTweets.get(0);
                SentimentActor.tweetStatus aNewStatus=new SentimentActor.tweetStatus(aStatus,"dummy");
                SentimentActor.tweetStatus bNewStatus=new SentimentActor.tweetStatus(fakeTweets.get(1),"dummy");
                SentimentActor.tweetStatus cNewStatus=new SentimentActor.tweetStatus(fakeTweets.get(2),"dummy");
                subject.tell(aNewStatus,probe.getRef());
                subject.tell(bNewStatus,probe.getRef());
                subject.tell(cNewStatus,probe.getRef());
                subject.tell("AnyMessage",probe.getRef());
                subject.tell("KillSwitch",probe.getRef());
                within(
                        Duration.ofSeconds(10),
                        () -> {
                            awaitCond(probe::msgAvailable);
                            final List<Object> two = probe.receiveN(3);
                            SentimentActor.storeSentiments temp= (SentimentActor.storeSentiments) two.get(0);
                            Assert.assertEquals("HAPPY",temp.mode);
                            SentimentActor.storeSentiments temp2= (SentimentActor.storeSentiments) two.get(1);
                            Assert.assertEquals("SAD",temp2.mode);
                            SentimentActor.storeSentiments temp3= (SentimentActor.storeSentiments) two.get(2);
                            Assert.assertEquals("NEUTRAL",temp3.mode);
                            expectNoMessage();
                            return null;

                        });
            }
        };
    }

}


