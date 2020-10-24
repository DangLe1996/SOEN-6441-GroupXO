package controllers;
import play.data.validation.Constraints;
public class TwitterData {

    @Constraints.Required
    private String searchString;

    public String getSearchString() {
        return searchString;
    }

    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }
}
