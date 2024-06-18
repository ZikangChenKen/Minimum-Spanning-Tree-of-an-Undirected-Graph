package edu.rice.comp322.boruvka.sequential;

import edu.rice.comp322.AbstractBoruvka;
import edu.rice.comp322.boruvka.BoruvkaFactory;
import edu.rice.comp322.boruvka.Edge;
import edu.rice.comp322.boruvka.Loader;
import edu.rice.hj.api.SuspendableException;

import java.util.LinkedList;
import java.util.Queue;

/**
 * This class should not be modified.
 *
 * @author <a href="http://shams.web.rice.edu/">Shams Imam</a> (shams@rice.edu)
 */
public final class SeqBoruvka extends AbstractBoruvka implements BoruvkaFactory<SeqComponent, SeqEdge> {

    protected final Queue<SeqComponent> nodesLoaded = new LinkedList<>();

    public SeqBoruvka() {
        super();
    }

    @Override
    public boolean usesHjLib() {
        return true;
    }

    @Override
    public void initialize(final String[] args) {
        Loader.parseArgs(args);
    }

    @Override
    public void preIteration(final int iterationIndex) {
        // Exclude reading file input from timing measurement
        nodesLoaded.clear();
        Loader.read(this, nodesLoaded);

        totalEdges = 0;
        totalWeight = 0;
    }

    @Override
    public void runIteration(int nthreads) throws SuspendableException {
        computeBoruvka(nodesLoaded);
    }

    private void computeBoruvka(final Queue<SeqComponent> nodesLoaded) {

        SeqComponent loopNode = null;

        // START OF EDGE CONTRACTION ALGORITHM
        while (!nodesLoaded.isEmpty()) {

            // poll() removes first element (node loopNode) from the nodesLoaded work-list
            loopNode = nodesLoaded.poll();

            if (loopNode.isDead) {
                continue; // node loopNode has already been merged
            }

            final Edge<SeqComponent> e = loopNode.getMinEdge(); // retrieve loopNode's edge with minimum cost
            if (e == null) {
                break; // done - we've contracted the graph to a single node
            }

            final SeqComponent other = e.getOther(loopNode);
            other.isDead = true;
            loopNode.merge(other, e.weight()); // merge node other into node loopNode
            nodesLoaded.add(loopNode); // add newly merged loopNode back in the work-list

        }
        // END OF EDGE CONTRACTION ALGORITHM
        if (loopNode != null) {
            totalEdges = loopNode.totalEdges();
            totalWeight = loopNode.totalWeight();
        }
    }

    @Override
    public SeqComponent newComponent(final int nodeId) {
        return new SeqComponent(nodeId);
    }

    @Override
    public SeqEdge newEdge(final SeqComponent from, final SeqComponent to, final double weight) {
        return new SeqEdge(from, to, weight);
    }
}


