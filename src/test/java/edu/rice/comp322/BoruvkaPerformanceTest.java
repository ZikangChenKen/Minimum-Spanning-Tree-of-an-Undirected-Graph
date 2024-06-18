package edu.rice.comp322;

import edu.rice.comp322.boruvka.Loader;
import edu.rice.comp322.boruvka.parallel.ParBoruvka;
import edu.rice.comp322.boruvka.sequential.SeqBoruvka;
import junit.framework.TestCase;

import edu.rice.hj.api.SuspendableException;
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
public class BoruvkaPerformanceTest extends TestCase {

    public void testInputUSAroadNE() throws IOException {
        runKernel(Loader.inputs[5], PerfTestUtils.getTestLabel());
    }

    private void launchHabaneroAppWrapper(boolean actuallyLaunch, PerfTestUtils.CheckedFunction r) {
        if (!actuallyLaunch) {
            System.setProperty("hj.numWorkers", "1");
        }

        launchHabaneroApp(() -> {
            r.apply();
        });
        System.clearProperty("hj.numWorkers");
    }

    private long runMultiThreadedTest(final int nthreads, final String input, final String[] args,
            final String testLabel, final long referenceTime, final long referenceEdges,
            final double referenceWeights) {
        final PerfTestUtils.PerfTestResults[] perfResults = new PerfTestUtils.PerfTestResults[1];
        final AbstractBoruvka[] parBenchmark = new AbstractBoruvka[1];

        System.setProperty("hj.numWorkers", Integer.toString(nthreads));
        launchHabaneroAppWrapper(new ParBoruvka().usesHjLib(), () -> {
            perfResults[0] = PerfTestUtils.runPerfTest(testLabel,
                () -> {
                    // pre parallel
                    parBenchmark[0] = new ParBoruvka();
                    try {
                        parBenchmark[0].initialize(args);
                    } catch (IOException io) {
                        throw new RuntimeException(io);
                    }
                    parBenchmark[0].preIteration(0);
                },
                () -> {
                    // parallel main
                    parBenchmark[0].runIteration(nthreads);
                },
                () -> {
                    // post parallel
                },
                () -> {
                    // final checks
                    final String edgesMessage = " Parallel edges (" + parBenchmark[0].totalEdges() +
                        "), Sequential edges (" + referenceEdges + ")";
                    assertEquals(edgesMessage + " are not equal!", referenceEdges,
                            parBenchmark[0].totalEdges());

                    final String weightsMessage = " Parallel edge weight (" + parBenchmark[0].totalWeight() +
                        "), Sequential edge weights (" + referenceWeights + ")";
                    assertTrue(weightsMessage + " have a large difference!",
                            compareDoubles(referenceWeights, parBenchmark[0].totalWeight()));

                }, 7 /* # parallel runs */, referenceTime, nthreads, true);
        });

        return perfResults[0].parTime;
    }

    private void runKernel(final String input, final String testLabel) throws IOException {

        final String[] args = {"-iter", "1", "-f", input};
        final AbstractBoruvka[] parBenchmark = new AbstractBoruvka[1];
        final AbstractBoruvka[] seqBenchmark = new AbstractBoruvka[1];

        // Get single-threaded baseline and sequential performance results
        final PerfTestUtils.PerfTestResults[] seqResults = new PerfTestUtils.PerfTestResults[1];
        System.setProperty("hj.numWorkers", "1");
        launchHabaneroAppWrapper(new ParBoruvka().usesHjLib(), () -> {
            seqResults[0] = PerfTestUtils.runPerfTest(testLabel,
                () -> {
                    // pre parallel
                    parBenchmark[0] = new ParBoruvka();
                    try {
                        parBenchmark[0].initialize(args);
                    } catch (IOException io) {
                        throw new RuntimeException(io);
                    }
                    parBenchmark[0].preIteration(0);
                },
                () -> {
                    // parallel main
                    parBenchmark[0].runIteration(1);
                },
                () -> {
                    // post parallel
                },
                () -> {
                    // pre sequential
                    seqBenchmark[0] = new SeqBoruvka();
                    try {
                        seqBenchmark[0].initialize(args);
                    } catch (IOException io) {
                        throw new RuntimeException(io);
                    }
                    seqBenchmark[0].preIteration(0);
                },
                () -> {
                    // sequential main
                    seqBenchmark[0].runIteration(1);
                },
                () -> {
                    // post sequential
                },
                () -> {
                    // final checks
                    final String edgesMessage = " Parallel edges (" + parBenchmark[0].totalEdges() +
                        "), Sequential edges (" + seqBenchmark[0].totalEdges() + ")";
                    assertEquals(edgesMessage + " are not equal!", seqBenchmark[0].totalEdges(),
                            parBenchmark[0].totalEdges());

                    final String weightsMessage = " Parallel edge weight (" + parBenchmark[0].totalWeight() +
                        "), Sequential edge weights (" + seqBenchmark[0].totalWeight() + ")";
                    assertTrue(weightsMessage + " have a large difference!",
                            compareDoubles(seqBenchmark[0].totalWeight(), parBenchmark[0].totalWeight()));

                }, 7 /* # parallel runs */, 7 /* # seq runs */, 1 /* nWorkerThreads */, false /* doPrint */ );
        });

        final StringBuilder sb = new StringBuilder();
        sb.append("=== For dataset " + input + " ===\n");
        sb.append("Single-threaded test ran in " + seqResults[0].parTime + " ms, " +
                ((double)seqResults[0].seqTime / (double)seqResults[0].parTime) + "x faster than sequential (" +
                seqResults[0].seqTime + " ms)\n");

        int nthreads = PerfTestUtils.getAutograderNcores();
        if (nthreads == -1) {
            // Running locally
            final int maxNthreads = Runtime.getRuntime().availableProcessors();
            for (nthreads = 2; nthreads <= maxNthreads; nthreads *= 2) {
                final long perf = runMultiThreadedTest(nthreads, input, args, testLabel, seqResults[0].parTime,
                        seqBenchmark[0].totalEdges(), seqBenchmark[0].totalWeight());
                sb.append(nthreads + " threaded test ran in " + perf + " ms, " +
                        ((double)seqResults[0].parTime / (double)perf) + "x faster than single-threaded (" +
                        seqResults[0].parTime + " ms)\n");
            }
            System.out.println(sb.toString());
        } else {
            // Running on the autograder
            final long perf = runMultiThreadedTest(nthreads, input, args, testLabel, seqResults[0].parTime,
                    seqBenchmark[0].totalEdges(), seqBenchmark[0].totalWeight());
            sb.append(nthreads + " threaded test ran in " + perf + " ms, " +
                    ((double)seqResults[0].parTime / (double)perf) + "x faster than single-threaded (" +
                    seqResults[0].parTime + " ms)\n");
            System.out.println(sb.toString());
        }
    }

    private boolean compareDoubles(final double expected, final double actual) {
        final double ratio = Math.abs(expected / actual);
        // being very liberal with the correctness check in the unit tests, allow 100X error in computing edge weights
        return ratio >= 0.0100 && ratio <= 100.00;
    }


}
