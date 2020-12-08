package actors;
import actors.HashtagActor;
import actors.SentimentActor;
import actors.TwitterStreamActor;
import actors.UserActor;
import akka.actor.testkit.typed.CapturedLogEvent;
import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.testkit.javadsl.TestKit;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import models.GetTweets;
import models.sessionData;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.AbstractActor;
import org.mockito.Mock;
import org.scalatestplus.junit.JUnitSuite;
import twitter4j.Status;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static commons.CommonHelper.buildStatusList;
import static commons.CommonHelper.createMockTweets;
import static org.mockito.Mockito.mock;


public class UserActorTest extends JUnitSuite {



    static ActorSystem system;

    public static ActorRef twitterStreamActor;
    public static ActorRef testingActor;
    public static TestKit probe;
    public static models.sessionData testUser;

    public static GetTweets getTweets = mock(GetTweets.class);


    @BeforeClass
    public static void setup() {

        system = ActorSystem.create();
        probe = new TestKit(system);
        testUser = new sessionData();
        twitterStreamActor = probe.childActorOf(TwitterStreamActor.prop());
        testingActor = system.actorOf(UserActor.props( probe.getTestActor(),testUser.getSessionID(),getTweets,twitterStreamActor));
    }

    @AfterClass
    public static void teardown() {
        TestKit.shutdownActorSystem(system);
        system = null;
    }

    /* Tests the instance methods of the supervisor actor */

    @Test
    public void userActor_test_Anymsg(){
        System.out.println("testing :: "+ "userActor_test_Anymsg");
        new TestKit(system) {
            {

                final TestKit probe = new TestKit(system);
                final Props props0 = Props.create(TwitterStreamActor.class);
                final ActorRef supervisor = system.actorOf(props0);
                GetTweets gt=new GetTweets();
                final Props props = Props.create(UserActor.class,probe.getRef(),"SYSTEM",gt,supervisor);
                final ActorRef subject = system.actorOf(props);
                subject.tell("{queryTerm:anymsg}",probe.getRef());
                TwitterStreamActor.updateStatus test1=new TwitterStreamActor.updateStatus("htmlcode","aSearch");

            }
        };
    }

    @Test
    public void userActor_test_Update() throws InterruptedException {
        System.out.println("testing :: "+ "userActor_test_Update");
        new TestKit(system) {
            {

                final TestKit probe = new TestKit(system);
                final Props props0 = Props.create(TwitterStreamActor.class);
                final ActorRef supervisor = system.actorOf(props0);
                GetTweets gt=new GetTweets();
                final Props props = Props.create(UserActor.class,probe.getRef(),"SYSTEM",gt,supervisor);
                final ActorRef subject = system.actorOf(props);
                TwitterStreamActor.updateStatus aStatus=new TwitterStreamActor.updateStatus("htmlCode","queryTerm");
                subject.tell(aStatus,probe.getRef());
                for(int i = 0; i < 32; i++) {
                    TwitterStreamActor.updateStatus status = new TwitterStreamActor.updateStatus("this is a test code", "test" + i);
                    TimeUnit.MILLISECONDS.sleep(10);
                    subject.tell(status,ActorRef.noSender());
                }
            }
        };
    }


}

