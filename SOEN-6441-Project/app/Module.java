import actors.HashtagActor;
import actors.HashtagActorParent;
import actors.UserActor;
import akka.actor.*;
import com.google.inject.AbstractModule;
import play.libs.akka.AkkaGuiceSupport;

import javax.inject.Provider;

public class Module extends AbstractModule implements AkkaGuiceSupport {
    @Override
    protected void configure() {


//        bindActor(AbstractActor.class,"HashtagParent");

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
