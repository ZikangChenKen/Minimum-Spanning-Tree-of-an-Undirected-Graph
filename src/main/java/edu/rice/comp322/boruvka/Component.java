package edu.rice.comp322.boruvka;

/**
 * This class should not be modified.
 *
 * @author <a href="http://shams.web.rice.edu/">Shams Imam</a> (shams@rice.edu)
 */
public abstract class Component<C extends Component> {
    public abstract int nodeId();

    public abstract void addEdge(final Edge<C> e);

    public abstract double totalWeight();

    public abstract long totalEdges();
}
