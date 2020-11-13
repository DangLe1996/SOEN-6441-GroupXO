package models;


import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.groupingBy;

import com.google.inject.AbstractModule;
import com.typesafe.config.ConfigException;
import twitter4j.*;

import javax.security.sasl.AuthenticationException;

/**
 * Handles the request of getting TwitterAPI
 */
public class GetTweets extends AbstractModule {




    private Twitter twitter ;
    public GetTweets(Twitter inputTwitter){
        twitter = inputTwitter;
    };
    public GetTweets(){
        twitter = new TwitterFactory().getInstance();
    }


    /**
     * Store results of all searched queries in a formatted HTML string, using searched keyword as key.
     * This GlobalCache reduce the needs to connect to Twitter4J if the
     * term was already searched.
     */
    public static HashMap<String, String> GlobalCache = new HashMap<>();
    public static HashMap<String, String> GlobalSentiments = new HashMap<>(); //suhel
    public static HashMap<String, List<String>> GlobalWordStatsCache = new HashMap<>();


    /**
     * User Defined array of words which when present marks a tweet as happy
     */
    private final static String[] happy = {"HAPPY", ":)", ":D", "<3", "PARTY", "😭","💜", "😀","\uD83D\uDC97\uD83D\uDC93","APPRECIATE","\uD83D\uDC9A","\uD83D\uDC4F","\uD83E\uDDE1","\uD83D\uDC9B","\uD83D\uDC9A","\uD83D\uDC9C","\uD83E\uDD70","\uD83D\uDDA4"};
    /**
     * User Defined array of words which when present marks a tweet as sad
     */
    private final static String[] sad = {"SAD", "ANGRY", ":(", "MAD", "DISAPPOINTMENT", "BAD DAY","\uD83D\uDCCA","\uD83D\uDE1E","\uD83D\uDE14"};

    /**
     * enum of mode of a tweet HAPPY, SAD , NEUTRAL
     */
    public enum Mode {
        HAPPY, SAD, NEUTRAL
    }

    /**
     * This method return a word-level statistics for the last 250 tweeets that contains a given SearchQuery, counting words in descending order
     * by frequency of the words.
     * @author: Girish
     * @param SearchQuery
     * @return list of string containing words and its count
     * @throws TwitterException
     */
    public CompletableFuture<List<String>> GetKeywordStats(String SearchQuery) {

        if ( GlobalWordStatsCache.containsKey(SearchQuery)) {
            return CompletableFuture.completedFuture(GlobalWordStatsCache.get(SearchQuery));
        }
        else {
            Query query = new Query(SearchQuery + " -filter:retweets");
            query.count(250);
            query.lang("en");
            List<String> removewords = Arrays.asList("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "as", "able", "about", "above", "according", "accordingly", "across", "actually", "after", "afterwards", "again", "against", "aint", "all", "allow", "allows", "almost", "alone", "along", "already", "also", "although", "always", "am", "among", "amongst", "an", "and", "another", "any", "anybody", "anyhow", "anyone", "anything", "anyway", "anyways", "anywhere", "apart", "appear", "appreciate", "appropriate", "are", "arent", "around", "as", "aside", "ask", "asking", "associated", "at", "available", "away", "awfully", "be", "became", "because", "become", "becomes", "becoming", "been", "before", "beforehand", "behind", "being", "believe", "below", "beside", "besides", "best", "better", "between", "beyond", "both", "brief", "but", "by", "cmon", "cs", "came", "can", "cant", "cannot", "cant", "cause", "causes", "certain", "certainly", "changes", "clearly", "co", "com", "come", "comes", "concerning", "consequently", "consider", "considering", "contain", "containing", "contains", "corresponding", "could", "couldnt", "course", "currently", "definitely", "described", "despite", "did", "didnt", "different", "do", "does", "doesnt", "doing", "dont", "done", "down", "downwards", "during", "each", "edu", "eg", "eight", "either", "else", "elsewhere", "enough", "entirely", "especially", "et", "etc", "even", "ever", "every", "everybody", "everyone", "everything", "everywhere", "ex", "exactly", "example", "except", "far", "few", "ff", "fifth", "first", "five", "followed", "following", "follows", "for", "former", "formerly", "forth", "four", "from", "further", "furthermore", "get", "gets", "getting", "given", "gives", "go", "goes", "going", "gone", "got", "gotten", "greetings", "had", "hadnt", "happens", "hardly", "has", "hasnt", "have", "havent", "having", "he", "hes", "hello", "help", "hence", "her", "here", "heres", "hereafter", "hereby", "herein", "hereupon", "hers", "herself", "hi", "him", "himself", "his", "hither", "hopefully", "how", "howbeit", "however", "i", "id", "ill", "im", "ive", "ie", "if", "ignored", "immediate", "in", "inasmuch", "inc", "indeed", "indicate", "indicated", "indicates", "inner", "insofar", "instead", "into", "inward", "is", "isnt", "it", "itd", "itll", "its", "its", "itself", "just", "keep", "keeps", "kept", "know", "knows", "known", "last", "lately", "later", "latter", "latterly", "least", "less", "lest", "let", "lets", "like", "liked", "likely", "little", "look", "looking", "looks", "ltd", "mainly", "many", "may", "maybe", "me", "mean", "meanwhile", "merely", "might", "more", "moreover", "most", "mostly", "much", "must", "my", "myself", "name", "namely", "nd", "near", "nearly", "necessary", "need", "needs", "neither", "never", "nevertheless", "new", "next", "nine", "no", "nobody", "non", "none", "noone", "nor", "normally", "not", "nothing", "novel", "now", "nowhere", "obviously", "of", "off", "often", "oh", "ok", "okay", "old", "on", "once", "one", "ones", "only", "onto", "or", "other", "others", "otherwise", "ought", "our", "ours", "ourselves", "out", "outside", "over", "overall", "own", "particular", "particularly", "per", "perhaps", "placed", "please", "plus", "possible", "presumably", "probably", "provides", "que", "quite", "qv", "rather", "rd", "re", "really", "reasonably", "regarding", "regardless", "regards", "relatively", "respectively", "right", "said", "same", "saw", "say", "saying", "says", "second", "secondly", "see", "seeing", "seem", "seemed", "seeming", "seems", "seen", "self", "selves", "sensible", "sent", "serious", "seriously", "seven", "several", "shall", "she", "should", "shouldnt", "since", "six", "so", "some", "somebody", "somehow", "someone", "something", "sometime", "sometimes", "somewhat", "somewhere", "soon", "sorry", "specified", "specify", "specifying", "still", "sub", "such", "sup", "sure", "ts", "take", "taken", "tell", "tends", "th", "than", "thank", "thanks", "thanx", "that", "thats", "thats", "the", "their", "theirs", "them", "themselves", "then", "thence", "there", "theres", "thereafter", "thereby", "therefore", "therein", "theres", "thereupon", "these", "they", "theyd", "theyll", "theyre", "theyve", "think", "third", "this", "thorough", "thoroughly", "those", "though", "three", "through", "throughout", "thru", "thus", "to", "together", "too", "took", "toward", "towards", "tried", "tries", "truly", "try", "trying", "twice", "two", "un", "under", "unfortunately", "unless", "unlikely", "until", "unto", "up", "upon", "us", "use", "used", "useful", "uses", "using", "usually", "value", "various", "very", "via", "viz", "vs", "want", "wants", "was", "wasnt", "way", "we", "wed", "well", "were", "weve", "welcome", "well", "went", "were", "werent", "what", "whats", "whatever", "when", "whence", "whenever", "where", "wheres", "whereafter", "whereas", "whereby", "wherein", "whereupon", "wherever", "whether", "which", "while", "whither", "who", "whos", "whoever", "whole", "whom", "whose", "why", "will", "willing", "wish", "with", "within", "without", "wont", "wonder", "would", "would", "wouldnt", "yes", "yet", "you", "youd", "youll", "youre", "youve", "your", "yours", "yourself", "yourselves", "zero");
            //try {
                CompletableFuture<List<String>> wordstat_sorted =
                        invokeTwitterServer(query)
                                .thenApply(result -> result.getTweets()
                                        .parallelStream()
                                        .map(s -> s.getText().split(" "))
                                        .flatMap(Arrays::stream)
                                        .filter(t -> t.matches("[a-zA-Z]+"))
                                        .filter(t -> !removewords.contains(t))
                                        .collect(Collectors.toMap(s -> s, s -> 1, Integer::sum))
                                )
                                .thenApply(wordstat_unsorted -> wordstat_unsorted.entrySet()
                                        .parallelStream()
                                        .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                                        .map(swc -> {
                                            return swc.getKey() + ":" + swc.getValue() + "\n";
                                        })
                                        .collect(Collectors.toList())
                                )
                                .thenApply(word_sorted -> {
                                            GlobalWordStatsCache.put((SearchQuery), word_sorted);
                                            return word_sorted;
                                        }
                                );
                return wordstat_sorted;
            }


        }
    }


    /**
     * Lambda BiFunction that takes a search query and its result, then return a formatted HTML string.
     */
    private static final BiFunction<String, String, String> tweetDisplayPageFormat = (searchquery, result) -> "		<tr>\n" +
            "			<th>Search terms:</th>\n" +
            "			<th><a href=/keyword?s=" + searchquery.replaceAll(" ", "+") + ">" + searchquery  + "</a></th>\n" +
            "	<th>"+ getTweetSentiments(searchquery) +"</th>" +
            "		</tr>\n" +
            "		<tr>\n" +
            "			<th>User</th>\n" +
            "			<th>Location</th>\n" +
            "			<th>Tweet Text</th>\n" +
            "		</tr>\n" + result;


    /**
     * Returns sessionData contains searchQuery with given userID
     * If GlobalCache contains a given keyword, then add it into LocalCache.
     * If the words have not been looked up before, call the overloaded method of GetTweets_keyword(String)
     * to look up the tweets, then add the result into GlobalCache and LocalCache of currentUser.
     * If the given keyword length is less than 1, return the currentUser session data.
     *
     * @param searchQuery : keyword that user want to looks up tweets
     * @param UserID      : user that request the search
     * @return sessionData of current user with given params
     * @throws TwitterException
     * @see models.GetTweets#GetTweetsWithKeyword(String)
     */
    public CompletionStage<sessionData> GetTweetsWithUser(String searchQuery, String UserID)  {

       sessionData currentUser = sessionData.getUser(UserID);
       
        if (searchQuery.length() < 2) {
            return CompletableFuture.completedFuture(currentUser);
        }

        if (GlobalCache.containsKey(searchQuery)) {
            List<String> userQuery = currentUser.getQuery();
            if (userQuery.contains(searchQuery)) {
                userQuery.remove(searchQuery);
                userQuery.add(0, searchQuery);
            } else {
                currentUser.insertCache(searchQuery, GlobalCache.get(searchQuery));
            }
            return CompletableFuture.completedFuture(currentUser);
        } else {
            return GetTweetsWithKeyword(searchQuery).thenApply(result -> {
                
                GlobalCache.put(searchQuery, result);
                currentUser.insertCache(searchQuery, result);
                return currentUser;
            });
        }

    }


    /**
     * Takes a string keyword and return a HTML formatted string showing the last 10 tweets contains the given keyword.
     * Only returns tweets with language of english.
     *
     * @param keyword : The keyword to looks up tweets
     * @return : HTML formatter string
     */
    public  CompletionStage<String> GetTweetsWithKeyword(String keyword)  {
        if (keyword.length() < 2) {
            return CompletableFuture.completedFuture("Cannot process empty string/Single Letter");
        }

        if (GlobalCache.containsKey(keyword)) {

            return CompletableFuture.completedFuture(
                    GlobalCache.get(keyword)
            );
        }

        Query query = new Query(keyword + " -filter:retweets");
        query.count(250);
        query.lang("en");
            return invokeTwitterServer(query)
                    .thenApply(result -> formatSentimental.apply(result, keyword))
                    .thenApply(result -> formatResult.apply(result))
                    .thenApply(tweet -> {
                        GlobalCache.put(keyword, tweetDisplayPageFormat.apply(keyword, tweet));
                        return GlobalCache.get(keyword);
                    }).exceptionally( ex -> ex.getMessage());


    }

    /**
     * Returns a boolean after matching a tweet text with provided list of happy/sad strings
     * @param inputStr ,items
     * @return boolean
     */

    public static boolean stringContainsItemFromList(String inputStr, String[] items) {
        return Arrays.stream(items).anyMatch(inputStr.toUpperCase() ::contains);
    }
    /**
     * Returns and stores mode analysis of tweets from a global cache
     * @author: Suhel
     * @param searchQuery
     * @return String
     */

    public static String getTweetSentiments(String searchQuery){


        if ( GlobalSentiments.containsKey(searchQuery)) {
            return GlobalSentiments.get(searchQuery);
        }
        return "Could not analyse Sentiments Due to Error/less no of Tweets on this Topic";

    }

    /**
     * Rendered the HTML for mode analysis and adds emoticons
     * @author: Suhel
     * @param mood,percentage
     * @return String
     */
    public static String renderSentimentsHTML(String mood,double percentage){
        String emoticon="";
        if (mood.equals(Mode.HAPPY.toString())) emoticon="\uD83D\uDE0C";
        else if (mood.equals(Mode.SAD.toString())) emoticon="\uD83D\uDE1E";
        else emoticon="\uD83D\uDE11";
        emoticon= "\uD83D\uDCCA"  +emoticon+" "+" Tweets are " +mood ;
        return emoticon;

    }


    /**
     * Get 10 tweets from Query result and adding HTML tags to display on screen.
     */
    private  Function<QueryResult,String> formatResult = (result) -> {

        return result.getTweets().parallelStream()
                .map(s -> {
                    return "\n" +
                            "<tr>\n" +
                            "		<td><a href=/user?s=" + s.getUser().getScreenName().replaceAll(" ", "+") + "> " + s.getUser().getScreenName() + "</a></td>\n" +
                            "		<td><a href=/location?s=" + s.getUser().getLocation().replaceAll(" ", "+") + ">" + s.getUser().getLocation() + "</a></td>\n" +
                            "		<td>" + s.getText().replaceAll("#(\\w+)+", "<a href=/hashtag?s=$1>#$1</a>") + "</td>\n" +
                            "</tr>\n";
                })
                .limit(10)
                .reduce("",
                        String::concat);

    };


    /**
     * Calculate the percentage of Happy or Sad tweet and store the result in GlobalSentiments Hashmap.
     */
    private  BiFunction<QueryResult, String, QueryResult> formatSentimental = (result, keyword) ->{

        String modeString = "";
        int queryResultSize = result.getTweets().size();
        Map<Mode, List<Status>> analyse;

        //Stream Grouping based on tweet Text
        analyse = result.getTweets().parallelStream()
                .collect(groupingBy(s -> {
                    if (stringContainsItemFromList(s.getText(), happy)) return Mode.HAPPY;
                    else if (stringContainsItemFromList(s.getText(), sad)) return Mode.SAD;
                    else return Mode.NEUTRAL;
                }));

        if (analyse.containsKey(Mode.HAPPY)) {
            double indicator = (analyse.get(Mode.HAPPY).size() * 100 / queryResultSize);
            if (indicator >= 70) {
                modeString=renderSentimentsHTML(Mode.HAPPY.toString(),indicator);

            }
        }
        if (analyse.containsKey(Mode.SAD)) {
            double indicator = (analyse.get(Mode.SAD).size() * 100 / queryResultSize);
            if (indicator >= 70) {
                modeString=renderSentimentsHTML(Mode.SAD.toString(),indicator);

            }
        }
        if ("".equals(modeString)) {
            modeString=renderSentimentsHTML(Mode.NEUTRAL.toString(),0);

        }
        GlobalSentiments.put(keyword, modeString); //cache with a key
        return result;
    };


    /**
     * Invoke Live Twitter server
     * @param query : search query to send to Twitter
     * @return: Query Result that include Tweets containing search query parameter.
     */
    private CompletableFuture<QueryResult> invokeTwitterServer(Query query)   {
        return CompletableFuture.supplyAsync( () -> {
            try {
                return twitter.search(query);
            } catch (Exception e) {
                System.out.println("Twitter Error: " + e.getMessage());
                return null;
            }
        });

    }


}
