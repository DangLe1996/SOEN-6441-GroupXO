package test.actors;
import actors.KeywordActor;
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

public class KeywordActorTest extends JUnitSuite {



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
	public void testKeyWordActor() {

		System.out.println("testing testKeyWordActor");

		new TestKit(system) {
			{
				//System.out.println("invoking testSentimentActor");

				final TestKit probe = new TestKit(system);
				final Props props = Props.create(KeywordActor.class,probe.getRef(),probe.getRef());
				final ActorRef subject = system.actorOf(props);
				subject.tell("test a keyword",probe.getRef());
				within(
						Duration.ofSeconds(10),
						() -> {
							awaitCond(probe::msgAvailable);
							final List<Object> two = probe.receiveN(1);
							//TwitterStreamActor.registerNewKeyword temp= (SentimentActor.storeSentiments) two.get(0);
							//System.out.println(temp.keyword);
							//System.out.println(temp.mode);
							//Assert.assertEquals("HAPPY",temp.mode);
							expectNoMessage();
							return null;

						});
			}
		};
	}

}


