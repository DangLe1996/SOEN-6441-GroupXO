package tweets;

import static java.util.Objects.requireNonNull;

public class TweetUpdate {

    public String getUserName() {
        return UserName;
    }

    public String getLocation() {
        return Location;
    }

    public String getText() {
        return Text;
    }

    private final String UserName;
    private final String Location;
    private final String Text;
    private final String SearchQuery;
    public TweetUpdate(String userName, String location, String text, String searchQuery) {
        UserName = userName;
        Location = location;
        Text = text;
        SearchQuery = searchQuery;
    }


    public String getType() {
        return "TweetUpdate";
    }


}
