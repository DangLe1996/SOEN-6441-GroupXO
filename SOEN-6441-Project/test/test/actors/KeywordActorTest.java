package test.actors;
import actors.KeywordActor;
import actors.SentimentActor;
import actors.TwitterStreamActor;
import static commons.CommonHelper.buildStatusList;
import akka.actor.testkit.typed.CapturedLogEvent;
import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.testkit.javadsl.TestKit;
import test.actors.KeywordActorTest.testActor;

import org.hamcrest.core.StringContains;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.ClassRule;
import static org.mockito.Mockito.mock;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.AbstractActor;
import org.scalatestplus.junit.JUnitSuite;
import twitter4j.Status;
import java.util.concurrent.TimeUnit;
import java.time.Duration;
import java.util.List;
import static commons.CommonHelper.buildStatusList;
import static org.junit.Assert.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Contains JUnit test cases for testing the KeywordActor Class
 * of the application.
 */

public class KeywordActorTest extends JUnitSuite {

	static String data = "";

	static ActorSystem system;
	static class testActor extends AbstractActor{
		public static String data;
		  public static Props props(){
		        return Props.create(testActor.class);
		    }
		  @Override
		    public Receive createReceive() {
		        return receiveBuilder()
		                .match(String.class,msg -> {		                
		                    this.data = msg;
		                    KeywordActorTest.data = msg;
		                })
		                .build();

		    }
	}

	@BeforeClass
	public static void setup() {
		system = ActorSystem.create();
	}

	@AfterClass
	public static void teardown() {
		TestKit.shutdownActorSystem(system);
		system = null;
	}
	@ClassRule public static final TestKitJunitResource testKit = new TestKitJunitResource();
	
	/***
     * Tests the KeyWordActor by mocking and supplying fake tweets
     *  @author Girish
     */

	
	@Test
	public void testKeyWordActor() {

	    TestProbe<ActorRef> testProbe = testKit.createTestProbe();

	    List<Status> tempstatus = buildStatusList(50,"HAPPY");

	     final ActorRef subject = system.actorOf(Props.create(testActor.class),"testclass1");

	     final ActorRef keywordActor = system.actorOf(KeywordActor.props(subject,subject),"testclass");

	     KeywordActor.updateStatus reply = null;
	     for(Status s: tempstatus) {
	     reply = new KeywordActor.updateStatus(s);
		    keywordActor.tell(reply,subject);
	     }
	     try {
        	 TimeUnit.SECONDS.sleep(5);
         } catch (InterruptedException ie)
         {
            
         }
	     assertThat( KeywordActorTest.data,StringContains.containsString("Indians:50"));
		
	}

}


