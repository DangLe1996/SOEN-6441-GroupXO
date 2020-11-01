package models;


import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.checkerframework.checker.units.qual.s;

import akka.actor.Status;
import twitter4j.*;

public class GetTweets {

	
	public GetTweets() {

	}
	
	public CompletionStage<Map<String, Integer>> GetKeywordStats(String keyword) throws TwitterException{

		
	    Twitter twitter = new TwitterFactory().getInstance();
	    Query query = new Query(keyword + " -filter:retweets");
	    query.count(250);
	    query.lang("en");
	    List<String> removewords = 
	    		//Arrays.asList("a","an","the","is","i","this","you","we","and","aboard", "about", "above", "across", "after", "against", "along", "amid", "among", "anti", "around", "as", "at", "before", "behind", "below", "beneath", "beside", "besides", "between", "beyond", "but", "by", "concerning", "considering", "despite", "down", "during", "except", "excepting", "excluding", "following", "for", "from", "in", "inside", "into", "like", "minus", "near", "of", "off", "on", "onto", "opposite", "outside", "over", "past", "per", "plus", "regarding", "round", "save", "since", "than", "through", "to", "toward", "towards", "under", "underneath", "unlike", "until", "up", "upon", "versus", "via", "with", "within", "without");
	    Arrays.asList("a", "b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z","as", "able", "about", "above", "according", "accordingly", "across", "actually", "after", "afterwards", "again", "against", "aint", "all", "allow", "allows", "almost", "alone", "along", "already", "also", "although", "always", "am", "among", "amongst", "an", "and", "another", "any", "anybody", "anyhow", "anyone", "anything", "anyway", "anyways", "anywhere", "apart", "appear", "appreciate", "appropriate", "are", "arent", "around", "as", "aside", "ask", "asking", "associated", "at", "available", "away", "awfully", "be", "became", "because", "become", "becomes", "becoming", "been", "before", "beforehand", "behind", "being", "believe", "below", "beside", "besides", "best", "better", "between", "beyond", "both", "brief", "but", "by", "cmon", "cs", "came", "can", "cant", "cannot", "cant", "cause", "causes", "certain", "certainly", "changes", "clearly", "co", "com", "come", "comes", "concerning", "consequently", "consider", "considering", "contain", "containing", "contains", "corresponding", "could", "couldnt", "course", "currently", "definitely", "described", "despite", "did", "didnt", "different", "do", "does", "doesnt", "doing", "dont", "done", "down", "downwards", "during", "each", "edu", "eg", "eight", "either", "else", "elsewhere", "enough", "entirely", "especially", "et", "etc", "even", "ever", "every", "everybody", "everyone", "everything", "everywhere", "ex", "exactly", "example", "except", "far", "few", "ff", "fifth", "first", "five", "followed", "following", "follows", "for", "former", "formerly", "forth", "four", "from", "further", "furthermore", "get", "gets", "getting", "given", "gives", "go", "goes", "going", "gone", "got", "gotten", "greetings", "had", "hadnt", "happens", "hardly", "has", "hasnt", "have", "havent", "having", "he", "hes", "hello", "help", "hence", "her", "here", "heres", "hereafter", "hereby", "herein", "hereupon", "hers", "herself", "hi", "him", "himself", "his", "hither", "hopefully", "how", "howbeit", "however", "i", "id", "ill", "im", "ive", "ie", "if", "ignored", "immediate", "in", "inasmuch", "inc", "indeed", "indicate", "indicated", "indicates", "inner", "insofar", "instead", "into", "inward", "is", "isnt", "it", "itd", "itll", "its", "its", "itself", "just", "keep", "keeps", "kept", "know", "knows", "known", "last", "lately", "later", "latter", "latterly", "least", "less", "lest", "let", "lets", "like", "liked", "likely", "little", "look", "looking", "looks", "ltd", "mainly", "many", "may", "maybe", "me", "mean", "meanwhile", "merely", "might", "more", "moreover", "most", "mostly", "much", "must", "my", "myself", "name", "namely", "nd", "near", "nearly", "necessary", "need", "needs", "neither", "never", "nevertheless", "new", "next", "nine", "no", "nobody", "non", "none", "noone", "nor", "normally", "not", "nothing", "novel", "now", "nowhere", "obviously", "of", "off", "often", "oh", "ok", "okay", "old", "on", "once", "one", "ones", "only", "onto", "or", "other", "others", "otherwise", "ought", "our", "ours", "ourselves", "out", "outside", "over", "overall", "own", "particular", "particularly", "per", "perhaps", "placed", "please", "plus", "possible", "presumably", "probably", "provides", "que", "quite", "qv", "rather", "rd", "re", "really", "reasonably", "regarding", "regardless", "regards", "relatively", "respectively", "right", "said", "same", "saw", "say", "saying", "says", "second", "secondly", "see", "seeing", "seem", "seemed", "seeming", "seems", "seen", "self", "selves", "sensible", "sent", "serious", "seriously", "seven", "several", "shall", "she", "should", "shouldnt", "since", "six", "so", "some", "somebody", "somehow", "someone", "something", "sometime", "sometimes", "somewhat", "somewhere", "soon", "sorry", "specified", "specify", "specifying", "still", "sub", "such", "sup", "sure", "ts", "take", "taken", "tell", "tends", "th", "than", "thank", "thanks", "thanx", "that", "thats", "thats", "the", "their", "theirs", "them", "themselves", "then", "thence", "there", "theres", "thereafter", "thereby", "therefore", "therein", "theres", "thereupon", "these", "they", "theyd", "theyll", "theyre", "theyve", "think", "third", "this", "thorough", "thoroughly", "those", "though", "three", "through", "throughout", "thru", "thus", "to", "together", "too", "took", "toward", "towards", "tried", "tries", "truly", "try", "trying", "twice", "two", "un", "under", "unfortunately", "unless", "unlikely", "until", "unto", "up", "upon", "us", "use", "used", "useful", "uses", "using", "usually", "value", "various", "very", "via", "viz", "vs", "want", "wants", "was", "wasnt", "way", "we", "wed", "well", "were", "weve", "welcome", "well", "went", "were", "werent", "what", "whats", "whatever", "when", "whence", "whenever", "where", "wheres", "whereafter", "whereas", "whereby", "wherein", "whereupon", "wherever", "whether", "which", "while", "whither", "who", "whos", "whoever", "whole", "whom", "whose", "why", "will", "willing", "wish", "with", "within", "without", "wont", "wonder", "would", "would", "wouldnt", "yes", "yet", "you", "youd", "youll", "youre", "youve", "your", "yours", "yourself", "yourselves", "zero");

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
	 
	
	
	public CompletionStage<String> GetTweets_keyword(String keyword) throws TwitterException{	
        if (keyword.length() < 1) {
            System.exit(-1);
        }
        Twitter twitter = new TwitterFactory().getInstance();
        Query query = new Query(keyword + " -filter:retweets");
        query.count(10);
        query.lang("en");
        QueryResult result; 

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
        				String::concat));
        
        /* There are 2 ways of doing this as per me,
         * if we have to add keyword hyperlink at the begining, then i couldnt find any way other than making it sequntial 
         * and then reduce with initial value like below, but we will loose performance of parallel stream
         * 
         * another way to do is in controller, after tweets are fetched add in the begining but this will breach
         * no business logic in controller rule
         *         return CompletableFuture.completedFuture(twitter.search(query).getTweets()
				.parallelStream()
				.map(s -> {
					return "\n" +
						   "<tr>\n" + 
							"		<td><a href=/user?s=" + s.getUser().getScreenName().replaceAll(" ", "+") + "> " + s.getUser().getScreenName() + "</a></td>\n" + 
							"		<td><a href=/location?s=" + s.getUser().getLocation().replaceAll(" ", "+") + ">" + s.getUser().getLocation() + "</a></td>\n" + 
							"		<td>" + s.getText().replaceAll("#(\\w+)+", "<a href=/hashtag?s=$1>#$1</a>") + "</td>\n" +
							"</tr>\n"; 
						})
				.sequential()
        		.reduce("		<tr>\n" + 
        				"			<th>Search terms:</th>\n" + 
        				"			<th><a href=/keyword?s=" + keyword.replaceAll(" ", "+") + "'>" + keyword + "</a></th>\n" + 
        				"		</tr>\n" + 
        				"		<tr>\n" + 
        				"			<th>User</th>\n" + 
        				"			<th>Location</th>\n" + 
        				"			<th>Tweet Text</th>\n" + 
        				"		</tr>\n",
        				String::concat));
         */

    }

}
	