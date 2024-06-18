package edu.rice.comp322.boruvka;

/**
 * This class should not be modified.
 *
 * @author <a href="http://shams.web.rice.edu/">Shams Imam</a> (shams@rice.edu)
 */
public interface BoruvkaFactory<C extends Component, E extends Edge> {

    C newComponent(int nodeId);

    E newEdge(C from, C to, double weight);

}
