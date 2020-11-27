package tweets;

import java.util.List;

public class TweetHistory {

    public String getSearchQuery() {
        return SearchQuery;
    }

    public List<String> getHistoryTweets() {
        return historyTweets;
    }

    private final String SearchQuery;
    private final List<String> historyTweets;

    public TweetHistory(String searchQuery, List<String> historyTweets) {
        SearchQuery = searchQuery;
        this.historyTweets = historyTweets;
    }
    public String getType() {
        return "tweethistory";
    }

}
