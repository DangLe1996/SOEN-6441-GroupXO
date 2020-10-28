package models;
import play.data.validation.Constraints;

public class Search {
    @Constraints.Required
    private String searchString;

    public String getSearchString() {
        return searchString;
    }

    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }
}
