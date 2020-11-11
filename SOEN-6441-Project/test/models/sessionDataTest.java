package models;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;


import static org.hamcrest.CoreMatchers.*;

class sessionDataTest {

    @Inject sessionData sessionData;
    private static sessionData testUser;
    @BeforeEach
    void setUp() {
        for(int i = 0; i < 3; i++){
            new sessionData();
        }
        testUser =  new sessionData();
    }

    @AfterEach
    void tearDown() {
    	System.out.println("in teardown");
        sessionData.cleanUpSessions();

    }

    @Test
    void getTestUser() {
        assertThat(sessionData.userCache.size(),is(4));
        assertThat(sessionData.getUser(testUser.toString()),is(equalTo(testUser)));
    }

    @Test
    void getInvalidUser(){
        assertThat(sessionData.getUser(null).getSessionID(),is("play5"));
        assertThat(sessionData.getUser("badID").getSessionID(),is("play6"));
    }
    @Test
    void testToString() {
        assertThat(testUser.toString(),is("sessionData{" +
                "sessionID='" + "play4" + '\'' +
                '}'));
    }


    @Test
    void insertNewCache() {

        testUser.insertCache("test1","this is test 1");
        testUser.insertCache("test2","this is test 2");
        testUser.insertCache("test3","this is test 3");

        assertThat(testUser.getCache().size(),is(3));
        assertThat(testUser.getQuery().size(),is(3));

    }
    
    @Test
    void insertCacheWithExisting(){
        testUser.insertCache("test2","this is test 2");
        testUser.insertCache("test2","this is test 2.1");

        assertThat(testUser.getCache().size(),is(1));
        assertThat(testUser.getQuery().size(),is(1));
    }

    @Test
    void testCacheMaxSize(){
        for(int i = 0; i < 12; i++){
            String keyword = "test" + i;
            String result = "this is test " + i;
            testUser.insertCache(keyword,result);
        }
        assertThat(testUser.getCache().size(),is(10));
        assertThat(testUser.getQuery().size(),is(10));
        assertThat(testUser.getQuery().get(0),is("test11"));
        assertThat(testUser.getCache().get("test11"),is("this is test 11"));
    }




}