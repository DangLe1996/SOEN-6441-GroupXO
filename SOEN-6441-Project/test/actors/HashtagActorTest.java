package actors;

import akka.actor.*;

import akka.testkit.javadsl.TestKit;
import com.fasterxml.jackson.databind.JsonNode;
import org.hamcrest.core.*;
import org.hamcrest.core.StringContains;
import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;

class HashtagActorTest {

    ActorSystem system;

    public static ActorRef twitterStreamActor;
     public static ActorRef testingActor;
    public static TestKit probe;

    @BeforeEach
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
        TimeUnit.SECONDS.sleep(5);
        assertThat(testingActor.isTerminated(), Is.is(true));


    }


    @Test
    public void testUpdate() throws InterruptedException {
        for(int i = 0; i < 32; i++){
            TwitterStreamActor.updateStatus status = new TwitterStreamActor.updateStatus("this is a test code", "test" + i);
            TimeUnit.MILLISECONDS.sleep(10);
            testingActor.tell(status,ActorRef.noSender());
        }
        assertThat(testingActor.isTerminated(), Is.is(false));
        System.out.println(probe.getLastSender().path());

    }


}