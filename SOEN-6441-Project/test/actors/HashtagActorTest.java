
package actors;

import akka.actor.*;

import akka.testkit.javadsl.TestKit;
import com.fasterxml.jackson.databind.JsonNode;
import org.hamcrest.core.*;
import org.hamcrest.core.StringContains;
import org.junit.Before;
import org.junit.Before;
import org.junit.Test;
import org.scalatestplus.junit.JUnitSuite;


import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;



public class HashtagActorTest  extends JUnitSuite {

    ActorSystem system;

    public static ActorRef twitterStreamActor;
     public static ActorRef testingActor;
    public static TestKit probe;

    @Before
    public void init(){
        system = ActorSystem.create();
         probe = new TestKit(system);
        twitterStreamActor = probe.childActorOf(TwitterStreamActor.prop());
        testingActor = system.actorOf(HashtagActor.props( probe.getTestActor(),twitterStreamActor));
    }



    @Test
    public void testSendingQuery() throws InterruptedException {

        Object test = "{\n" +
                "  \"queryTerm\": \"test\"\n" +
                "}";

        testingActor.tell(test,ActorRef.noSender());

        assertThat(testingActor.isTerminated(), Is.is(false));
        assertThat(twitterStreamActor.isTerminated(),Is.is(false));

    }

    @Test
    public void stopActor() throws InterruptedException {

        system.stop(testingActor);
        TimeUnit.SECONDS.sleep(1);
        assertThat(testingActor.isTerminated(), Is.is(true));


    }


    @Test
    public void testUpdate() throws InterruptedException {
        System.out.println(" Hashtag actor in action");
        for(int i = 0; i < 32; i++){
            TwitterStreamActor.updateStatus status = new TwitterStreamActor.updateStatus("this is a test code", "test" + i);
            TimeUnit.MILLISECONDS.sleep(10);
            testingActor.tell(status,ActorRef.noSender());
        }
        assertThat(testingActor.isTerminated(), Is.is(false));
        System.out.println(probe.getLastSender().path());

    }

    @Test
    public void test_Hash_tag_update() throws InterruptedException {

        System.out.println("testing test_Hash_tag_update");

        new TestKit(system) {
            {
                final TestKit probe = new TestKit(system);
                final Props props0 = Props.create(TwitterStreamActor.class);
                final ActorRef supervisor = system.actorOf(props0);

                final Props props = Props.create(HashtagActor.class,probe.getRef(),supervisor);
                final ActorRef subject = system.actorOf(props);
                for(int i = 0; i < 32; i++) {
                    TwitterStreamActor.updateStatus status = new TwitterStreamActor.updateStatus("this is a test code", "test" + i);
                    TimeUnit.MILLISECONDS.sleep(10);
                    Object msg="Any msg";
                    subject.tell(msg,ActorRef.noSender());
                    subject.tell(status,ActorRef.noSender());


                }
                within(
                        Duration.ofSeconds(10),
                        () -> {
                            awaitCond(probe::msgAvailable);
                            expectNoMessage();
                            return null;

                        });
            }
        };
    }


    @Test
    public void test_Hash_tag_Any() throws InterruptedException {

        System.out.println("testing test_Hash_tag_Any");

        new TestKit(system) {
            {
                final TestKit probe = new TestKit(system);
                final Props props0 = Props.create(TwitterStreamActor.class);
                final ActorRef supervisor = system.actorOf(props0);

                final Props props = Props.create(HashtagActor.class,probe.getRef(),supervisor);
                final ActorRef subject = system.actorOf(props);

                Object msg="Any msg";
                subject.tell(msg,ActorRef.noSender());



            }


        };
    }


}

