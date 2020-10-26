package controllers;

import models.TwitterDataFetcher;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.*;
import twitter4j.TwitterException;

import javax.inject.Inject;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static java.lang.String.format;
import static play.libs.Scala.asScala;

public class MyController  extends Controller {


    private HttpExecutionContext httpExecutionContext;

    @Inject
    public MyController(HttpExecutionContext ec) {
        this.httpExecutionContext = ec;
    }

    public CompletionStage<Result> index() throws TwitterException {
        // Use a different task with explicit EC
        return new TwitterDataFetcher().fetchTwitterSearchCompleted("vietnam").thenApplyAsync(
                answer -> {
                    return ok("answer was " + answer).flashing("info", "Response updated!");
                },
                httpExecutionContext.current());


    }

}


