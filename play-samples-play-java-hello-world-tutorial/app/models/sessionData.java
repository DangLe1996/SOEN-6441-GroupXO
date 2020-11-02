package models;

import scala.math.Equiv;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class sessionData {

    public static int currentID = 0;
    public static LinkedHashMap<String,sessionData> userCache = new LinkedHashMap<>();

    private List<String> queries = new ArrayList<>();
    private LinkedHashMap<String,String> localCache = new LinkedHashMap<>();
    private String sessionID ;
    public sessionData(){
        currentID ++;
        this.sessionID =  "play" + Integer.toString(currentID);
        userCache.put(this.toString(),this);
    }

    public static sessionData getUser(String userID){
        if(userCache.containsValue(userID)) return new sessionData();
        if(userCache.get(userID) == null){
            userCache.remove(userID);
            return new sessionData();
        }else {
            return userCache.get(userID);
        }
    }

    @Override
    public String toString() {
        return "sessionData{" +
                "sessionID='" + sessionID + '\'' +
                '}';
    }

    public List<String> getQuery(){
        return this.queries;
    }
    public LinkedHashMap<String,String> getCache(){
        return this.localCache;
    }

    public void insertCache(String searchTerm, String result){
        if(localCache.size()>=10){
            String key = this.queries.get(10);
            this.localCache.remove(key);
            this.queries.remove(key);
        }
        this.localCache.put(searchTerm,result);
        this.queries.add(0,searchTerm);
    }



}
