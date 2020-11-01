package models;

import com.google.common.base.Stopwatch;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import twitter4j.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import org.hamcrest.core.*;
import static org.hamcrest.MatcherAssert.*;
class TwitterDataFetcherTest {

    @Test
    void fetchTwitterSearch() {
        String testKey = "america";
        TwitterDataFetcher test = new TwitterDataFetcher();
        List<Status> results =  test.fetchTwitterSearch(testKey);

        results.parallelStream().filter(r -> !r.getText().toLowerCase().contains(testKey)).map(r ->  r.isTruncated()).forEach(System.out::println);





    }

    @Test
    void fetchTwitterLocation() {

        GeoLocation Montreal = new GeoLocation(45.508888,-73.561668);
        TwitterDataFetcher test = new TwitterDataFetcher();
        List<Status> results =  test.fetchTwitterByLocation("test",Montreal);
        results.parallelStream().map(r -> r.getUser().getLocation()).forEach(System.out::println);
        var x = results.parallelStream().map(r -> r.getText()).collect(Collectors.toList());
        String result = StringUtils.join(x, ", ");
    }





    @Test
    void fetchDataByQuery() throws TwitterException, ExecutionException, InterruptedException {

        TwitterDataFetcher test = new TwitterDataFetcher();
        List<String> keywords = new ArrayList<>();
        keywords.add("Montreal");
        keywords.add("Quebec");
        keywords.add("Canada");
        keywords.add("Sea");
        keywords.add("Wind");
        keywords.add("solar");
        keywords.add("clock");
        keywords.add("shoe");
        keywords.add("protein");


        long start = System.nanoTime();

        CompletableFuture[] futures = keywords.stream()
                .map(keyword-> CompletableFuture.supplyAsync(() -> {
                    try {
                        return test.fetchTwitterSearch(new Query().geoCode(new GeoLocation(100,100),10, Query.Unit.km));
                    } catch (TwitterException e) {
                        e.printStackTrace();
                    }

                    return null;
                })).map(f -> f.thenAccept(System.out::println)).toArray(size -> new CompletableFuture[size]);

        CompletableFuture.allOf(futures).join();

        System.out.println("All shops have now responded in " + ((System.nanoTime() - start) / 1_000_000) + " msecs");



    }
}