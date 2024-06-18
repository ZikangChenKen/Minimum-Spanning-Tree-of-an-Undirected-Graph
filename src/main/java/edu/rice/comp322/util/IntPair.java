package edu.rice.comp322.util;

public final class IntPair {
    public final int left;
    public final int right;

    public IntPair(final int f, final int s) {
        left = f;
        right = s;
    }

    @SuppressWarnings("unchecked")
    @Override
    public final boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if ((o == null) || (o.getClass() != this.getClass())) {
            return false;
        }
        final IntPair p = (IntPair) o;
        return p.left == left && p.right == right;
    }

    @Override
    public final int hashCode() {
        int hash = 13;
        hash = (31 * hash) + left;
        hash = (31 * hash) + right;
        return hash;
    }

    @Override
    public final String toString() {
        return String.format("<%d,%d>", left, right);
    }
}
