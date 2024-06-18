package edu.rice.comp322.boruvka.parallel;

import edu.rice.comp322.boruvka.Component;
import edu.rice.comp322.boruvka.Edge;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class may be modified.
 *
 * @author <a href="http://shams.web.rice.edu/">Shams Imam</a> (shams@rice.edu)
 */
public final class ParComponent extends Component<ParComponent> {

    public final int nodeId;
    public List<Edge<ParComponent>> pq = new ArrayList<>();
    public double totalWeight = 0;
    public long totalEdges = 0;
    public boolean isDead = false;

    public final ReentrantLock lock = new ReentrantLock();

    protected ParComponent(final int nodeId) {
        super();
        this.nodeId = nodeId;
    }

    @Override
    public int nodeId() {
        return nodeId;
    }

    @Override
    public double totalWeight() {
        return totalWeight;
    }

    @Override
    public long totalEdges() {
        return totalEdges;
    }

    /**
     * insert edge in weight order.
     */
    public void addEdge(final Edge<ParComponent> e) {
        // insert edge in weight order
        int i = 0;
        while (i < pq.size()) {
            if (e.weight() < pq.get(i++).weight()) {
                i--;
                break;
            }
        }
        pq.add(i, e);
    }

    /**
     * Get the edge with minimum weight from the sorted list pq.
     */
    public Edge<ParComponent> getMinEdge() {
        if (pq.size() == 0) {
            return null;
        }
        return pq.get(0);
    }

    /**
     * Merge two components together, connected by an edge with weight edgeWeight.
     */
    public void merge(final ParComponent other, final double edgeWeight) {
        totalWeight += other.totalWeight + edgeWeight;
        totalEdges += other.totalEdges + 1;
        final List<Edge<ParComponent>> npq = new ArrayList<>();
        int i = 0;
        int j = 0;
        while (i + j < pq.size() + other.pq.size()) {
            // get rid of inter-component edges
            while (i < pq.size()) {
                final Edge<ParComponent> e = pq.get(i);
                if ((e.fromComponent() != this && e.fromComponent() != other) || (e.toComponent() != this && e.toComponent() != other)) {
                    break;
                }
                i++;
            }
            while (j < other.pq.size()) {
                final Edge<ParComponent> e = other.pq.get(j);
                if ((e.fromComponent() != this && e.fromComponent() != other) || (e.toComponent() != this && e.toComponent() != other)) {
                    break;
                }
                j++;
            }
            if (j < other.pq.size() && (i >= pq.size() || pq.get(i).weight() > other.pq.get(j).weight())) {
                npq.add(other.pq.get(j++).replaceComponent(other, this));
            } else if (i < pq.size()) {
                npq.add(pq.get(i++).replaceComponent(other, this));
            }
        }
        other.pq.clear();
        pq.clear();
        pq = npq;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Component)) {
            return false;
        }

        final Component component = (Component) o;

        if (nodeId != component.nodeId()) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return nodeId;
    }

    @Override
    public String toString() {
        return "ParComponent{" +
                "nodeId=" + nodeId +
                ", pq.size=" + pq.size() +
                ", isDead=" + isDead +
                '}';
    }
}
