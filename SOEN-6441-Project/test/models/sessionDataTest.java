package models;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
import org.junit.Test;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.test.WithApplication;

import javax.inject.Inject;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;


import static org.hamcrest.CoreMatchers.*;

public class sessionDataTest  extends WithApplication {

    @Override
    protected Application provideApplication() {
        return new GuiceApplicationBuilder().build();
    }


    @Inject sessionData sessionData;
    private static sessionData testUser;
    //@BeforeEach
    public void setUp() {
        for(int i = 0; i < 3; i++){
            new sessionData();
        }
        testUser =  new sessionData();
    }

    //@AfterEach
    public  void tearDown() {

        sessionData.cleanUpSessions();

    }

    @Test
    public  void getTestUser() {
        setUp();

        assertThat(sessionData.userCache.size(),is(4));
        assertThat(sessionData.getUser(testUser.toString()),is(equalTo(testUser)));
        tearDown();
    }

    @Test
    public  void getInvalidUser(){
        setUp();
        assertThat(sessionData.getUser(null).getSessionID(),is("play5"));
        assertThat(sessionData.getUser("badID").getSessionID(),is("play6"));
        tearDown();
    }
    @Test
    public  void testToString() {
        setUp();
        assertThat(testUser.toString(),is("sessionData{" +
                "sessionID='" + "play4" + '\'' +
                '}'));
        tearDown();
    }


    @Test
    public  void insertNewCache() {
        setUp();

        testUser.insertCache("test1","this is test 1");
        testUser.insertCache("test2","this is test 2");
        testUser.insertCache("test3","this is test 3");

        assertThat(testUser.getCache().size(),is(3));
        assertThat(testUser.getQuery().size(),is(3));
        tearDown();
    }
    
    @Test
    public  void insertCacheWithExisting(){
        setUp();
        testUser.insertCache("test2","this is test 2");
        testUser.insertCache("test2","this is test 2.1");

        assertThat(testUser.getCache().size(),is(1));
        assertThat(testUser.getQuery().size(),is(1));
        tearDown();
    }

    @Test
    public void testCacheMaxSize(){
        setUp();

        //sessionData testUser=new sessionData() ;
        for(int i = 0; i < 12; i++){
            String keyword = "test" + i;
            String result = "this is test " + i;
            testUser.insertCache(keyword,result);
        }
        assertThat(testUser.getCache().size(),is(10));
        assertThat(testUser.getQuery().size(),is(10));
        assertThat(testUser.getQuery().get(0),is("test11"));
        assertThat(testUser.getCache().get("test11"),is("this is test 11"));
        tearDown();
    }




}
