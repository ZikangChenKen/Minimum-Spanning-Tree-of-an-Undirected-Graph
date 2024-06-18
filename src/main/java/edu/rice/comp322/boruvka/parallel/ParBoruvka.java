package edu.rice.comp322.boruvka.parallel;

import edu.rice.comp322.AbstractBoruvka;
import edu.rice.comp322.boruvka.BoruvkaFactory;
import edu.rice.comp322.boruvka.Edge;
import edu.rice.comp322.boruvka.Loader;
import edu.rice.hj.api.SuspendableException;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class must be modified.
 *
 * @author <a href="http://shams.web.rice.edu/">Shams Imam</a> (shams@rice.edu)
 */
public final class ParBoruvka extends AbstractBoruvka implements BoruvkaFactory<ParComponent, ParEdge> {

    // Queue to store the thread.
    private ConcurrentLinkedQueue<Thread> threadsQueue = new ConcurrentLinkedQueue<>();

    // Queue to store the nodes.
    private ConcurrentLinkedQueue<ParComponent> nodesLoaded = new ConcurrentLinkedQueue<>();

    public ParBoruvka() {
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

        // Adding each thread to the queue and start computing in each thread.
        for (int i = 0; i < nthreads; i++) {
            Thread newThread = new Thread(() -> computeBoruvka(nodesLoaded));
            threadsQueue.add(newThread);
            newThread.start();
        }

        // Join each thread.
        while (!threadsQueue.isEmpty()) {
            try {
                threadsQueue.poll().join();
            } catch (InterruptedException error) {
                error.printStackTrace();
            }
        }
    }

    private void computeBoruvka(final Queue<ParComponent> nodesLoaded) {

        ParComponent loopNode;

        // START OF EDGE CONTRACTION ALGORITHM
        while ((loopNode = nodesLoaded.poll()) != null) {

            // Try lock loopNode.
            if (!loopNode.lock.tryLock()) {
                continue;
            }

            // Unlock loopNode and continue if loopNode is dead.
            if (loopNode.isDead) {
                loopNode.lock.unlock();
                continue; // node loopNode has already been merged
            }

            final Edge<ParComponent> e = loopNode.getMinEdge(); // retrieve loopNode's edge with minimum cost

            // Unlock loopNode if we have done.
            if (e == null) {
                loopNode.lock.unlock();
                break; // done - we've contracted the graph to a single node
            }

            final ParComponent other = e.getOther(loopNode);

            // Try lock other and unlock loopNode and add it back to nodesLoaded if the lock fails.
            if (!other.lock.tryLock()) {
                loopNode.lock.unlock();
                nodesLoaded.add(loopNode);
                continue;
            }

            // Unlock the loopNode and other if other is dead.
            if (other.isDead) {
                loopNode.lock.unlock();
                other.lock.unlock();
                continue;
            }

            other.isDead = true;
            loopNode.merge(other, e.weight()); // merge node other into node loopNode

            // Unlock the loopNode and other for other thread to access them.
            loopNode.lock.unlock();
            other.lock.unlock();

            nodesLoaded.add(loopNode); // add newly merged loopNode back in the work-list
        }
        // END OF EDGE CONTRACTION ALGORITHM
        if (loopNode != null) {
            totalEdges = loopNode.totalEdges();
            totalWeight = loopNode.totalWeight();
        }
    }

    @Override
    public ParComponent newComponent(final int nodeId) {
        return new ParComponent(nodeId);
    }

    @Override
    public ParEdge newEdge(final ParComponent from, final ParComponent to, final double weight) {
        return new ParEdge(from, to, weight);
    }
}


