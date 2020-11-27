package actors;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class HelloWorld extends AbstractBehavior<HelloWorld.Command> {

    interface Command{}


    public static Behavior<Command>create(){
        return Behaviors.setup(context -> new HelloWorld(context) );
    }


    public enum SayHello implements Command{
        INSTANCE
    }

    public HelloWorld( ActorContext<Command> context) {
        super(context);
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessageEquals(SayHello.INSTANCE,this::onSayHello)
                .onMessage(ChangeMessage.class,this::onChangeMessage)
                .build();
    }

    private Behavior<Command> onChangeMessage(ChangeMessage command) {
        message = command.Message;
        return this;
    }

    private Behavior<Command> onSayHello() {
        System.out.println(message);
        return this;
    }


    public static class ChangeMessage implements Command{
        public final String Message;

       public ChangeMessage(String message) {
           Message = message;
       }
   }

   private String message = "hello world";


}
