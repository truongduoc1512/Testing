package com.example.demo.pagination;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;

import org.hibernate.ScrollableResults;
import org.hibernate.query.Query;
import org.junit.jupiter.api.Test;

class PaginationResultTest {

    @Test
    void emptyResult_normalizesPageAndReturnsImmutableCollections() {
        ScrollableQueryFixture fixture = scrollableQueryFixture(false);
        when(fixture.scroll.getRowNumber()).thenReturn(-1);

        PaginationResult<String> result = new PaginationResult<>(fixture.query, 0, 10, 5);

        assertEquals(0, result.getTotalRecords());
        assertEquals(0, result.getTotalPages());
        assertEquals(1, result.getCurrentPage());
        assertEquals(10, result.getMaxResult());
        assertEquals(Collections.emptyList(), result.getList());
        assertEquals(Arrays.asList(1, 0), result.getNavigationPages());
        assertThrows(UnsupportedOperationException.class, () -> result.getList().add("x"));
        assertThrows(UnsupportedOperationException.class, () -> result.getNavigationPages().add(2));
    }

    @Test
    void populatedResult_collectsRecordsAndCalculatesNonDivisiblePages() {
        ScrollableQueryFixture fixture = scrollableQueryFixture(true);
        when(fixture.scroll.scroll(0)).thenReturn(true);
        when(fixture.scroll.get(0)).thenReturn("first", "second");
        when(fixture.scroll.next()).thenReturn(true, false);
        when(fixture.scroll.getRowNumber()).thenReturn(1, 1, 12);

        PaginationResult<String> result = new PaginationResult<>(fixture.query, 1, 5, 10);

        assertEquals(Arrays.asList("first", "second"), result.getList());
        assertEquals(13, result.getTotalRecords());
        assertEquals(3, result.getTotalPages());
        assertEquals(1, result.getCurrentPage());
        assertEquals(Arrays.asList(1, 2, 3), result.getNavigationPages());
    }

    @Test
    void largeResult_capsNavigationAndAddsLeadingEllipsis() {
        ScrollableQueryFixture fixture = scrollableQueryFixture(true);
        when(fixture.scroll.scroll(70)).thenReturn(true);
        when(fixture.scroll.get(0)).thenReturn("record");
        when(fixture.scroll.next()).thenReturn(false);
        when(fixture.scroll.getRowNumber()).thenReturn(99);

        PaginationResult<String> result = new PaginationResult<>(fixture.query, 8, 10, 5);

        assertEquals(100, result.getTotalRecords());
        assertEquals(10, result.getTotalPages());
        assertEquals(8, result.getCurrentPage());
        assertEquals(Arrays.asList(1, -1, 6, 7, 8, 9, 10), result.getNavigationPages());
    }

    @Test
    void largeResultAtStart_addsTrailingEllipsis() {
        ScrollableQueryFixture fixture = scrollableQueryFixture(true);
        when(fixture.scroll.scroll(0)).thenReturn(true);
        when(fixture.scroll.get(0)).thenReturn("record");
        when(fixture.scroll.next()).thenReturn(false);
        when(fixture.scroll.getRowNumber()).thenReturn(99);

        PaginationResult<String> result = new PaginationResult<>(fixture.query, 1, 10, 5);

        assertEquals(Arrays.asList(1, 2, -1, 10), result.getNavigationPages());
    }

    @Test
    void pageBeyondLast_isClampedOnlyForNavigationAndMissingPageHasNoRows() {
        ScrollableQueryFixture fixture = scrollableQueryFixture(true);
        when(fixture.scroll.scroll(190)).thenReturn(false);
        when(fixture.scroll.getRowNumber()).thenReturn(19);

        PaginationResult<String> result = new PaginationResult<>(fixture.query, 20, 10, 5);

        assertEquals(20, result.getTotalRecords());
        assertEquals(2, result.getTotalPages());
        assertEquals(20, result.getCurrentPage());
        assertEquals(Collections.emptyList(), result.getList());
        assertEquals(Arrays.asList(1, 2), result.getNavigationPages());
    }

    @Test
    void iterationStopsWhenCursorFallsBeforeRequestedPage() {
        ScrollableQueryFixture fixture = scrollableQueryFixture(true);
        when(fixture.scroll.scroll(10)).thenReturn(true);
        when(fixture.scroll.get(0)).thenReturn("record");
        when(fixture.scroll.next()).thenReturn(true);
        when(fixture.scroll.getRowNumber()).thenReturn(9, 19);

        PaginationResult<String> result = new PaginationResult<>(fixture.query, 2, 10, 5);

        assertEquals(Collections.singletonList("record"), result.getList());
    }

    @Test
    void iterationStopsAtExclusivePageEnd() {
        ScrollableQueryFixture fixture = scrollableQueryFixture(true);
        when(fixture.scroll.scroll(0)).thenReturn(true);
        when(fixture.scroll.get(0)).thenReturn("record");
        when(fixture.scroll.next()).thenReturn(true);
        when(fixture.scroll.getRowNumber()).thenReturn(10, 19);

        PaginationResult<String> result = new PaginationResult<>(fixture.query, 1, 10, 5);

        assertEquals(Collections.singletonList("record"), result.getList());
    }

    @SuppressWarnings("unchecked")
    private ScrollableQueryFixture scrollableQueryFixture(boolean hasRows) {
        Query<String> query = mock(Query.class);
        ScrollableResults scroll = mock(ScrollableResults.class);
        when(query.scroll(any())).thenReturn(scroll);
        when(scroll.first()).thenReturn(hasRows);
        return new ScrollableQueryFixture(query, scroll);
    }

    private static final class ScrollableQueryFixture {
        private final Query<String> query;
        private final ScrollableResults scroll;

        private ScrollableQueryFixture(Query<String> query, ScrollableResults scroll) {
            this.query = query;
            this.scroll = scroll;
        }
    }
}
