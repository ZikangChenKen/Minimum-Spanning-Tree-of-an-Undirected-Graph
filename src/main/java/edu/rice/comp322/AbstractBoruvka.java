package edu.rice.comp322;

import java.io.IOException;

import edu.rice.hj.api.SuspendableException;

/**
 * @author <a href="http://shams.web.rice.edu/">Shams Imam</a> (shams@rice.edu)
 */
public abstract class AbstractBoruvka {

    protected long totalEdges = 0;
    protected double totalWeight = 0.0;

    public final long totalEdges() {
        return totalEdges;
    }

    public final double totalWeight() {
        return totalWeight;
    }

    public abstract void initialize(String[] args) throws IOException;

    public abstract void preIteration(int iterationIndex);

    public abstract void runIteration(int nthreads) throws SuspendableException;

    public abstract boolean usesHjLib();
}
