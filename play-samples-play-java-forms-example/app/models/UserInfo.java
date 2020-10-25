package models;

import twitter4j.Status;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

public class UserInfo {

    private static int lastUserID;

    private int userID;
    private Dictionary<String,List<Status>> cacheSearch;
    public void makeNewUser(){
        lastUserID ++;
        this.userID = lastUserID;
        cacheSearch = new Hashtable<>();
    }

    public void updateSearchCache(String keyword, List<Status> results){




    }



}
