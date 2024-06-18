package edu.rice.comp322;

import edu.rice.comp322.boruvka.Loader;
import edu.rice.comp322.boruvka.parallel.ParBoruvka;
import edu.rice.comp322.boruvka.sequential.SeqBoruvka;
import junit.framework.TestCase;

import static edu.rice.hj.Module0.launchHabaneroApp;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.IOException;

/**
 * This is a test class for your homework and should not be modified.
 *
 * @author <a href="http://shams.web.rice.edu/">Shams Imam</a> (shams@rice.edu)
 * @author Vivek Sarkar (vsarkar@rice.edu)
 */
public class BoruvkaCorrectnessTest extends TestCase {

    public void testInputUSAroadNY() throws IOException {
        final int inputIndex = 0;
        runKernel(Loader.inputs[inputIndex]);
    }

    public void testInputUSAroadBAY() throws IOException {
        final int inputIndex = 1;
        runKernel(Loader.inputs[inputIndex]);
    }

    public void testInputUSAroadCOL() throws IOException {
        final int inputIndex = 2;
        runKernel(Loader.inputs[inputIndex]);
    }

    private void runSingleBenchmark(final AbstractBoruvka benchmark, final String[] args) throws IOException {
        benchmark.initialize(args);
        benchmark.preIteration(0);
        launchHabaneroApp(() -> {
            benchmark.runIteration(5);
        });
    }

    private void runKernel(final String input) throws IOException {
        System.out.println("Testing parallel minimum spanning tree construction on " + input);

        final AbstractBoruvka parBenchmark = new ParBoruvka();
        final AbstractBoruvka seqBenchmark = new SeqBoruvka();

        final String[] args = {"-iter", "1", "-f", input};
        runSingleBenchmark(seqBenchmark, args);
        runSingleBenchmark(parBenchmark, args);

        final String edgesMessage = " Parallel edges (" + parBenchmark.totalEdges() +
            "), Sequential edges (" + seqBenchmark.totalEdges() + ")";
        assertEquals(edgesMessage + " are not equal!", seqBenchmark.totalEdges(), parBenchmark.totalEdges());

        final String weightsMessage = " Parallel edge weight (" + parBenchmark.totalWeight() +
                "), Sequential edge weights (" + seqBenchmark.totalWeight() + ")";
        assertTrue(weightsMessage + " have a large difference!", compareDoubles(seqBenchmark.totalWeight(),
                    parBenchmark.totalWeight()));
    }

    private boolean compareDoubles(final double expected, final double actual) {
        final double ratio = Math.abs(expected / actual);
        // being very liberal with the correctness check in the unit tests, allow 100X error in computing edge weights
        return ratio >= 0.0100 && ratio <= 100.00;
    }


}
