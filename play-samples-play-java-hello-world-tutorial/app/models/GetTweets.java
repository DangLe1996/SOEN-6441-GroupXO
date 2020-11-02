package models;


import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import twitter4j.*;

public class GetTweets {

	public static  HashMap<String,String> GlobalCache = new HashMap<>();
	
	public GetTweets() {

	}



	public CompletionStage<Map<String, Integer>> GetKeywordStats(String keyword) throws TwitterException{

		
	    Twitter twitter = new TwitterFactory().getInstance();
	    Query query = new Query(keyword + " -filter:retweets");
	    query.count(250);
	    query.lang("en");
	    List<String> removewords = Arrays.asList("a", "b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z","as", "able", "about", "above", "according", "accordingly", "across", "actually", "after", "afterwards", "again", "against", "aint", "all", "allow", "allows", "almost", "alone", "along", "already", "also", "although", "always", "am", "among", "amongst", "an", "and", "another", "any", "anybody", "anyhow", "anyone", "anything", "anyway", "anyways", "anywhere", "apart", "appear", "appreciate", "appropriate", "are", "arent", "around", "as", "aside", "ask", "asking", "associated", "at", "available", "away", "awfully", "be", "became", "because", "become", "becomes", "becoming", "been", "before", "beforehand", "behind", "being", "believe", "below", "beside", "besides", "best", "better", "between", "beyond", "both", "brief", "but", "by", "cmon", "cs", "came", "can", "cant", "cannot", "cant", "cause", "causes", "certain", "certainly", "changes", "clearly", "co", "com", "come", "comes", "concerning", "consequently", "consider", "considering", "contain", "containing", "contains", "corresponding", "could", "couldnt", "course", "currently", "definitely", "described", "despite", "did", "didnt", "different", "do", "does", "doesnt", "doing", "dont", "done", "down", "downwards", "during", "each", "edu", "eg", "eight", "either", "else", "elsewhere", "enough", "entirely", "especially", "et", "etc", "even", "ever", "every", "everybody", "everyone", "everything", "everywhere", "ex", "exactly", "example", "except", "far", "few", "ff", "fifth", "first", "five", "followed", "following", "follows", "for", "former", "formerly", "forth", "four", "from", "further", "furthermore", "get", "gets", "getting", "given", "gives", "go", "goes", "going", "gone", "got", "gotten", "greetings", "had", "hadnt", "happens", "hardly", "has", "hasnt", "have", "havent", "having", "he", "hes", "hello", "help", "hence", "her", "here", "heres", "hereafter", "hereby", "herein", "hereupon", "hers", "herself", "hi", "him", "himself", "his", "hither", "hopefully", "how", "howbeit", "however", "i", "id", "ill", "im", "ive", "ie", "if", "ignored", "immediate", "in", "inasmuch", "inc", "indeed", "indicate", "indicated", "indicates", "inner", "insofar", "instead", "into", "inward", "is", "isnt", "it", "itd", "itll", "its", "its", "itself", "just", "keep", "keeps", "kept", "know", "knows", "known", "last", "lately", "later", "latter", "latterly", "least", "less", "lest", "let", "lets", "like", "liked", "likely", "little", "look", "looking", "looks", "ltd", "mainly", "many", "may", "maybe", "me", "mean", "meanwhile", "merely", "might", "more", "moreover", "most", "mostly", "much", "must", "my", "myself", "name", "namely", "nd", "near", "nearly", "necessary", "need", "needs", "neither", "never", "nevertheless", "new", "next", "nine", "no", "nobody", "non", "none", "noone", "nor", "normally", "not", "nothing", "novel", "now", "nowhere", "obviously", "of", "off", "often", "oh", "ok", "okay", "old", "on", "once", "one", "ones", "only", "onto", "or", "other", "others", "otherwise", "ought", "our", "ours", "ourselves", "out", "outside", "over", "overall", "own", "particular", "particularly", "per", "perhaps", "placed", "please", "plus", "possible", "presumably", "probably", "provides", "que", "quite", "qv", "rather", "rd", "re", "really", "reasonably", "regarding", "regardless", "regards", "relatively", "respectively", "right", "said", "same", "saw", "say", "saying", "says", "second", "secondly", "see", "seeing", "seem", "seemed", "seeming", "seems", "seen", "self", "selves", "sensible", "sent", "serious", "seriously", "seven", "several", "shall", "she", "should", "shouldnt", "since", "six", "so", "some", "somebody", "somehow", "someone", "something", "sometime", "sometimes", "somewhat", "somewhere", "soon", "sorry", "specified", "specify", "specifying", "still", "sub", "such", "sup", "sure", "ts", "take", "taken", "tell", "tends", "th", "than", "thank", "thanks", "thanx", "that", "thats", "thats", "the", "their", "theirs", "them", "themselves", "then", "thence", "there", "theres", "thereafter", "thereby", "therefore", "therein", "theres", "thereupon", "these", "they", "theyd", "theyll", "theyre", "theyve", "think", "third", "this", "thorough", "thoroughly", "those", "though", "three", "through", "throughout", "thru", "thus", "to", "together", "too", "took", "toward", "towards", "tried", "tries", "truly", "try", "trying", "twice", "two", "un", "under", "unfortunately", "unless", "unlikely", "until", "unto", "up", "upon", "us", "use", "used", "useful", "uses", "using", "usually", "value", "various", "very", "via", "viz", "vs", "want", "wants", "was", "wasnt", "way", "we", "wed", "well", "were", "weve", "welcome", "well", "went", "were", "werent", "what", "whats", "whatever", "when", "whence", "whenever", "where", "wheres", "whereafter", "whereas", "whereby", "wherein", "whereupon", "wherever", "whether", "which", "while", "whither", "who", "whos", "whoever", "whole", "whom", "whose", "why", "will", "willing", "wish", "with", "within", "without", "wont", "wonder", "would", "would", "wouldnt", "yes", "yet", "you", "youd", "youll", "youre", "youve", "your", "yours", "yourself", "yourselves", "zero");

	    return CompletableFuture.completedFuture(twitter.search(query).getTweets()
					.parallelStream()
					.map(s -> s.getText().split(" "))
					//.map(s -> s.split(" "))
					.flatMap(Arrays::stream)
					.map(t -> t.toLowerCase())
					.filter(t -> t.matches("[a-zA-Z]+"))
					.filter(t -> !removewords.contains(t))
					.collect(Collectors.toMap(s -> s, s -> 1, Integer::sum))); 
	}

	private static final BiFunction<String,String,String> tweetDisplayPageFormat = (searchquery, tweet) -> "		<tr>\n" +
			"			<th>Search terms:</th>\n" +
			"			<th><a href=/keyword?s=" + searchquery.replaceAll(" ", "+") + ">" + searchquery + "</a></th>\n" +
			"		</tr>\n" +
			"		<tr>\n" +
			"			<th>User</th>\n" +
			"			<th>Location</th>\n" +
			"			<th>Tweet Text</th>\n" +
			"		</tr>\n" + tweet;


	public static CompletionStage<sessionData> GetTweets_keyword(String searchQuery, String UserID) throws TwitterException {
		
		if (searchQuery.length() < 1) {
			System.exit(-1);
		}
		sessionData currentUser = sessionData.getUser(UserID);

		if(GlobalCache.containsKey(searchQuery)){
			List<String> userQuery = currentUser.getQuery();
			if(userQuery.contains(searchQuery)){
				userQuery.remove(searchQuery);
				userQuery.add(0, searchQuery);
			}
			else{
				currentUser.insertCache(searchQuery,GlobalCache.get(searchQuery));
			}
			return CompletableFuture.completedFuture(currentUser);

		}
		else{

			System.out.println("Current User is " + currentUser);

			return GetTweets_keyword(searchQuery).thenApply(result -> {
				GlobalCache.put(searchQuery,result);
				currentUser.insertCache(searchQuery,result);
				return currentUser;
			});
		}

	}
	
	
	public static CompletionStage<String> GetTweets_keyword(String keyword) throws TwitterException{
        if (keyword.length() < 1) {
            System.exit(-1);
        }

		if(GlobalCache.containsKey(keyword)){

			return CompletableFuture.completedFuture(
				GlobalCache.get(keyword)
			);
		}

        Twitter twitter = new TwitterFactory().getInstance();
        Query query = new Query(keyword + " -filter:retweets");
        query.count(10);
        query.lang("en");


        return CompletableFuture.completedFuture(twitter.search(query).getTweets()
				.parallelStream()
				.map(s -> {
					return "\n" +
						   "<tr>\n" + 
							"		<td><a href=/user?s=" + s.getUser().getScreenName().replaceAll(" ", "+") + "> " + s.getUser().getScreenName() + "</a></td>\n" + 
							"		<td><a href=/location?s=" + s.getUser().getLocation().replaceAll(" ", "+") + ">" + s.getUser().getLocation() + "</a></td>\n" + 
							"		<td>" + s.getText().replaceAll("#(\\w+)+", "<a href=/hashtag?s=$1>#$1</a>") + "</td>\n" +
							"</tr>\n"; 
						})
        		.reduce("",
        				String::concat)).thenApply(tweet -> {
			GlobalCache.put(keyword,tweetDisplayPageFormat.apply(keyword, tweet));
			return GlobalCache.get(keyword);

		});
        

    }

}
	
