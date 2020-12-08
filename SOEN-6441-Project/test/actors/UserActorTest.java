package actors;

import akka.testkit.javadsl.TestKit;
import models.GetTweets;
import models.sessionData;
import org.hamcrest.core.Is;


import akka.actor.ActorRef;
import akka.actor.ActorSystem;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;


import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UserActorTest  {

    static ActorSystem system;

    public static ActorRef twitterStreamActor;
    public static ActorRef testingActor;
    public static ActorRef testingActor2;
    public static TestKit probe;
    public static models.sessionData testUser;

    public static GetTweets getTweets = mock(GetTweets.class);


    @Before
    public void setup() {

        system = ActorSystem.create();
        probe = new TestKit(system);
        testUser = new sessionData();
        twitterStreamActor = probe.childActorOf(TwitterStreamActor.prop());

        testUser.insertCache("test","this is a test");

        when(getTweets.GetTweetsWithUser("test",testUser.getSessionID()))
                .thenReturn(CompletableFuture.completedFuture(testUser));

        testingActor = system.actorOf(UserActor.props( probe.getTestActor(),testUser.getSessionID(),getTweets,twitterStreamActor));
        testingActor2 = system.actorOf(UserActor.props( probe.getTestActor(),testUser.getSessionID(),getTweets,twitterStreamActor));
    }



    @Test
    public void testSendQueryRequest() throws InterruptedException {
        Object test = "{\n" +
                "  \"queryTerm\": \"test\"\n" +
                "}";
        testingActor.tell(test,ActorRef.noSender());
        TimeUnit.MILLISECONDS.sleep(10);
        testingActor2.tell(test,ActorRef.noSender());
        TimeUnit.MILLISECONDS.sleep(10);
        assertThat(probe.getLastSender(),Is.is(not(true)));
        System.out.println(probe.getLastSender());
    }

    @Test
    public void testUpdate() throws InterruptedException {
        for(int i = 0; i < 32; i++){
            TwitterStreamActor.updateStatus status = new TwitterStreamActor.updateStatus("this is a test code", "test" + i);
            testingActor.tell(status,ActorRef.noSender());
            TimeUnit.MILLISECONDS.sleep(10);
        }
        assertThat(testingActor.isTerminated(), Is.is(false));
        System.out.println(probe.getLastSender().path());

    }

}





