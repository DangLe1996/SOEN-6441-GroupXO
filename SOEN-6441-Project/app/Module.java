import actors.HashtagActor;
import actors.HashtagActorParent;
import actors.UserActor;
import akka.actor.*;
import com.google.inject.AbstractModule;
import play.libs.akka.AkkaGuiceSupport;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;

import javax.inject.Provider;

public class Module extends AbstractModule  {
    @Override
    protected void configure() {


//       bind(TwitterStream.class).toInstance(new TwitterStreamFactory().getInstance());

    }


//    public static class UserActorFactoryProvider implements Provider<UserActor.Factory> {
//
//        @Override
//        public UserActor.Factory get() {
//            return id -> UserActor.create(id, stocksActor);
//        }
//    }
//

}
