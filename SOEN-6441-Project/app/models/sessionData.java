package models;

import scala.math.Equiv;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class sessionData {


    /**
     * Incrementor that increase every time a new user enter the application. Used to create unique identification.
     */
    public static int currentID = 0;
    /**
     * Store userSession of all user entered the system, using user's sessionID as key.
     */
    public static LinkedHashMap<String,sessionData> userCache = new LinkedHashMap<>();


    /**
     * Store the list of latest 10 keywords that user looks up
     */
    private List<String> queries;
    /**
     * Store the result of the latest 10 keywords user looks up in a LinkedHashMap, using the search keyword as key
     */
    private LinkedHashMap<String,String> localCache;
    /**
     * Unique identification string for user.
     */
    private String sessionID ;

    /**
     * Constructor for sesstionData. It automatically increment currentID to create a unique key, then
     * create a new session ID. The user is added to userCache, which userID as key and store user SessionData.
     */
    public sessionData(){
        queries = new ArrayList<>();
        localCache = new LinkedHashMap<>();
        currentID ++;
        this.sessionID =  "play" + currentID;
        userCache.put(this.toString(),this);
    }

    /**
     * Return user information with given userID
     * If user exist in userCache, returns that sessionData.
     * If user does not exist, creates and returns a new sessionData.
     * If the userID exist in userCache but the object is corrupted,
     * removes user key from userCache and return a new user sessionData.
     * @param userID : key to retrieve user information from userCache
     * @return: sessionData object of the user with given userID
     */
    public static sessionData getUser(String userID){
        if(userCache.containsValue(userID)) return new sessionData();
        if(userCache.get(userID) == null){
            userCache.remove(userID);
            return new sessionData();
        }else {
            return userCache.get(userID);
        }
    }

    /**
     * @return "sessionData{" + "sessionID='" + sessionID + '\'' + '}'
     */
    @Override
    public String toString() {
        return "sessionData{" +
                "sessionID='" + sessionID + '\'' +
                '}';
    }

    /**
     * @return Current User's queries.
     */
    public List<String> getQuery(){
        return this.queries;
    }

    /**
     * @return User's search history, which stored in localCache.
     * @see models.sessionData#localCache
     */
    public LinkedHashMap<String,String> getCache(){
        return this.localCache;
    }

    /**
     * This method insert search term and its result into current user's local cache and queries.
     * If the local cache size is more than 10, it removes the last entries before adding the new entry
     * into the beginning of the list.
     * @param searchTerm: they keyword that user wants to looks up
     * @param result: 10 latest tweets contain the searchTerm, retrieved from twitter4j.
     * @see models.sessionData#localCache
     * @see models.sessionData#queries
     */
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
