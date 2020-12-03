package models;
import play.data.validation.Constraints;

/**
 * Search object for user form.
 */
public class Search {
    @Constraints.Required
    private String searchString;

    /**
     * @return searchString from user input form.
     */
    public String getSearchString() {
        return searchString;
    }

    /**
     * set the object searchString
     * @param searchString
     */
    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }

}
