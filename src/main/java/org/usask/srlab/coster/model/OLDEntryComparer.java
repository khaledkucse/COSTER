package org.usask.srlab.coster.model;

import java.util.Comparator;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

public class OLDEntryComparer implements Comparator<OLDEntry> {
    @Override
    public int compare(OLDEntry x, OLDEntry y) {
        // TODO: Handle null x or y values
        int startComparison = compare(x.score, y.score);
        return startComparison;
    }
    private static int compare(double a, double b) {
        return a < b ? -1
                : a > b ? 1
                : 0;
    }

    @Override
    public Comparator<OLDEntry> reversed() {
        return null;
    }

    @Override
    public Comparator<OLDEntry> thenComparing(Comparator<? super OLDEntry> other) {
        return null;
    }

    @Override
    public <U> Comparator<OLDEntry> thenComparing(Function<? super OLDEntry, ? extends U> keyExtractor, Comparator<? super U> keyComparator) {
        return null;
    }

    @Override
    public <U extends Comparable<? super U>> Comparator<OLDEntry> thenComparing(Function<? super OLDEntry, ? extends U> keyExtractor) {
        return null;
    }

    @Override
    public Comparator<OLDEntry> thenComparingInt(ToIntFunction<? super OLDEntry> keyExtractor) {
        return null;
    }

    @Override
    public Comparator<OLDEntry> thenComparingLong(ToLongFunction<? super OLDEntry> keyExtractor) {
        return null;
    }

    @Override
    public Comparator<OLDEntry> thenComparingDouble(ToDoubleFunction<? super OLDEntry> keyExtractor) {
        return null;
    }
}
