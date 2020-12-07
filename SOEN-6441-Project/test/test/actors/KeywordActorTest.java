package test.actors;

import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import akka.actor.testkit.typed.javadsl.TestProbe;

import actors.KeywordActor.updateStatus;
import akka.actor.AbstractActor;

import org.junit.ClassRule;
import org.junit.Test;


import java.util.Optional;
import actors.KeywordActor;
import actors.TwitterStreamActor;
import actors.HashtagActor;
import static org.junit.Assert.assertEquals;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

public class KeywordActorTest {
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
		                })
		                .build();

		    }
	}

  @ClassRule public static final TestKitJunitResource testKit = new TestKitJunitResource();
	
  @Test
  public static void testUpdate() {
    TestProbe<ActorRef> testProbe = testKit.createTestProbe();
    
    //final Props props = new Props(testActor.class);
     //= system.actorOf(props);
     final ActorRef subject = system.actorOf(Props.create(testActor.class),"testclass");

     final ActorRef keywordActor = system.actorOf(KeywordActor.props(subject,subject),"testclass");
     
   // ActorRef<KeywordActor> keywordActor = testKit.spawn(KeywordActor.props(subject,subject));
//    HashtagActor.updateStatus reply = new HashtagActor.updateStatus("test:1");
//    keywordActor.tell(reply,subject);
//
//    assertEquals("test:1", testActor.data);

    /*
    deviceActor.tell(new Device.ReadTemperature(2L, readProbe.getRef()));c
    Device.RespondTemperature response1 = readProbe.receiveMessage();
    assertEquals(2L, response1.requestId);
    assertEquals(Optional.of(24.0), response1.value);

    deviceActor.tell(new Device.RecordTemperature(3L, 55.0, recordProbe.getRef()));
    assertEquals(3L, recordProbe.receiveMessage().requestId);

    deviceActor.tell(new Device.ReadTemperature(4L, readProbe.getRef()));
    Device.RespondTemperature response2 = readProbe.receiveMessage();
    assertEquals(4L, response2.requestId);
    assertEquals(Optional.of(55.0), response2.value);
    */
    
  }
}