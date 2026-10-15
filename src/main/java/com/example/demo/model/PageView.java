package com.example.demo.model;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.function.Function;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.http.HttpServletRequest;

/**
 * One page of a larger list, plus what the shared pagination fragment needs to
 * render it. Page numbers are 1-based.
 */
public final class PageView<T> {

    public static final int DEFAULT_SIZE = 10;

    /** Marks an ellipsis in {@link #getPageNumbers()}; real pages start at 1. */
    public static final int GAP = 0;

    private static final int NEIGHBOURS = 2;

    private final List<T> content;
    private final int page;
    private final int size;
    private final long totalItems;
    private final String requestUri;
    private final String queryString;
    private final String pageParam;

    private PageView(List<T> content, int page, int size, long totalItems,
            String requestUri, String queryString, String pageParam) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalItems = totalItems;
        this.requestUri = requestUri;
        this.queryString = queryString;
        this.pageParam = pageParam;
    }

    /** Slices an already loaded list. Out-of-range pages are clamped. */
    public static <T> PageView<T> of(List<T> all, int requestedPage, int size) {
        int page = clamp(requestedPage, totalPagesFor(all.size(), size));
        int from = (page - 1) * size;
        int to = Math.min(from + size, all.size());
        return new PageView<>(List.copyOf(all.subList(from, to)), page, size, all.size(), "", null, "page");
    }

    /**
     * Loads one page from the database; a page past the end falls back to the last
     * page.
     */
    public static <T> PageView<T> load(int requestedPage, int size, Sort sort,
            Function<Pageable, Page<T>> loader) {
        Page<T> result = loader.apply(PageRequest.of(clamp(requestedPage, Integer.MAX_VALUE) - 1, size, sort));
        if (result.getTotalPages() > 0 && result.getNumber() >= result.getTotalPages()) {
            result = loader.apply(PageRequest.of(result.getTotalPages() - 1, size, sort));
        }
        return new PageView<>(List.copyOf(result.getContent()), result.getNumber() + 1, size,
                result.getTotalElements(), "", null, "page");
    }

    /** Makes page links keep the current request's other query parameters. */
    public PageView<T> withLinks(HttpServletRequest request, String pageParam) {
        return new PageView<>(content, page, size, totalItems,
                request.getRequestURI(), request.getQueryString(), pageParam);
    }

    public <R> PageView<R> map(Function<? super T, ? extends R> mapper) {
        List<R> mapped = content.stream().<R>map(mapper).toList();
        return new PageView<>(mapped, page, size, totalItems, requestUri, queryString, pageParam);
    }

    public List<T> getContent() {
        return content;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public long getTotalItems() {
        return totalItems;
    }

    public int getTotalPages() {
        return totalPagesFor(totalItems, size);
    }

    public boolean isFirst() {
        return page <= 1;
    }

    public boolean isLast() {
        return page >= getTotalPages();
    }

    public boolean isMultiplePages() {
        return getTotalPages() > 1;
    }

    /** 1-based position of the first item on this page, or 0 when empty. */
    public long getFirstItem() {
        return content.isEmpty() ? 0 : (long) (page - 1) * size + 1;
    }

    public long getLastItem() {
        return content.isEmpty() ? 0 : getFirstItem() + content.size() - 1;
    }

    /**
     * Pages to show as buttons: first, last and the current page's neighbours,
     * with {@link #GAP} where pages are skipped.
     */
    public List<Integer> getPageNumbers() {
        int totalPages = getTotalPages();
        TreeSet<Integer> shown = new TreeSet<>();
        shown.add(1);
        shown.add(totalPages);
        for (int n = page - NEIGHBOURS; n <= page + NEIGHBOURS; n++) {
            if (n >= 1 && n <= totalPages) {
                shown.add(n);
            }
        }

        List<Integer> numbers = new ArrayList<>();
        int previous = 0;
        for (int n : shown) {
            if (previous != 0 && n - previous == 2) {
                numbers.add(previous + 1);
            } else if (previous != 0 && n - previous > 2) {
                numbers.add(GAP);
            }
            numbers.add(n);
            previous = n;
        }
        return numbers;
    }

    public String getPreviousLink() {
        return linkTo(page - 1);
    }

    public String getNextLink() {
        return linkTo(page + 1);
    }

    public String linkTo(int targetPage) {
        return UriComponentsBuilder.newInstance()
                .path(requestUri)
                .query(queryString)
                .replaceQueryParam(pageParam, targetPage)
                .build()
                .toUriString();
    }

    private static int clamp(int requestedPage, int totalPages) {
        return Math.min(Math.max(requestedPage, 1), Math.max(totalPages, 1));
    }

    private static int totalPagesFor(long totalItems, int size) {
        return (int) Math.max(1, (totalItems + size - 1) / size);
    }
}
