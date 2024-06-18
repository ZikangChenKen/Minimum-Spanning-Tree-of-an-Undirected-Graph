package edu.rice.comp322.boruvka.parallel;

import edu.rice.comp322.boruvka.Edge;

/**
 * This class may be modified.
 *
 * @author <a href="http://shams.web.rice.edu/">Shams Imam</a> (shams@rice.edu)
 */
public final class ParEdge extends Edge<ParComponent> implements Comparable<Edge> {

    protected ParComponent fromComponent;
    protected ParComponent toComponent;
    public double weight;

    protected ParEdge(final ParComponent from, final ParComponent to, final double w) {
        fromComponent = from;
        toComponent = to;
        weight = w;
    }

    @Override
    public ParComponent fromComponent() {
        return fromComponent;
    }

    @Override
    public ParComponent toComponent() {
        return toComponent;
    }

    @Override
    public double weight() {
        return weight;
    }

    /**
     * Given one member of this edge, return the other.
     */
    public ParComponent getOther(final ParComponent from) {
        if (fromComponent == from) {
            assert (toComponent != from);
            return toComponent;
        }

        if (toComponent == from) {
            assert (fromComponent != from);
            return fromComponent;
        }
        assert (false);
        return null;

    }

    @Override
    public int compareTo(final Edge e) {
        if (e.weight() == weight) {
            return 0;
        } else if (weight < e.weight()) {
            return -1;
        } else {
            return 1;
        }
    }

    /**
     * Given one member of this edge, swap it out for a different component.
     */
    public ParEdge replaceComponent(final ParComponent from, final ParComponent to) {
        if (fromComponent == from) {
            fromComponent = to;
        }
        if (toComponent == from) {
            toComponent = to;
        }
        return this;
    }
}
