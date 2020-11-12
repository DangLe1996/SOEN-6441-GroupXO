package models;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class SearchTest {


    /**
     * Test that search object store and retrieve search string correctly.
     */
    @Test
    public void testCorrectString() {
        String searchPhrase = "this is test search";
        Search testSearch = new Search();
        testSearch.setSearchString(searchPhrase);
        assertThat(testSearch.getSearchString(),is(searchPhrase));

    }

    /**
     * Test that if search string is empty, no exception thrown
     */
    @Test
    public void testEmptyString() {
        String searchPhrase = " ";
        Search testSearch = new Search();
        testSearch.setSearchString(searchPhrase);
        assertThat(testSearch.getSearchString(),is(searchPhrase));

    }

    /**
     * Test that if search string is null, no exception thrown
     */
    @Test
    public void testNullString() {
        String searchPhrase = null;
        Search testSearch = new Search();
        testSearch.setSearchString(searchPhrase);
        assertThat(testSearch.getSearchString(),is(searchPhrase));

    }
}
