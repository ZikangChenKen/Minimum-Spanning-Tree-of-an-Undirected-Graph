package edu.rice.comp322;

import junit.framework.*;
import java.util.Arrays;
import edu.rice.hj.api.SuspendableException;

public class PerfTestUtils {

    public static int getAutograderNcores() {
        final String ncoresStr = System.getProperty("autograder.ncores");
        if (ncoresStr == null) {
            return -1;
        } else {
            return Integer.parseInt(ncoresStr);
        }
    }

    public static class PerfTestResults {
        public final long seqTime;
        public final long parTime;

        public PerfTestResults(long seqTime, long parTime) {
            this.seqTime = seqTime;
            this.parTime = parTime;
        }
    }

    private static long runPerfTestHelper(Runnable preRunnable, CheckedFunction runnable, Runnable postRunnable,
            final int nruns) throws SuspendableException {
        final long[] run_times = new long[nruns];
        for (int r = 0; r < nruns; r++) {
            if (preRunnable != null) {
                preRunnable.run();
            }
            final long start = System.currentTimeMillis();
            runnable.apply();
            final long elapsed = System.currentTimeMillis() - start;

            if (postRunnable != null) {
                postRunnable.run();
            }

            run_times[r] = elapsed;
        }

        Arrays.sort(run_times);
        return run_times[0];
    }

    private static long runPerfTestHelperNoSuspend(Runnable preRunnable, Runnable runnable, Runnable postRunnable,
            final int nruns) {
        final long[] run_times = new long[nruns];
        for (int r = 0; r < nruns; r++) {
            if (preRunnable != null) {
                preRunnable.run();
            }
            final long start = System.currentTimeMillis();
            runnable.run();
            final long elapsed = System.currentTimeMillis() - start;

            if (postRunnable != null) {
                postRunnable.run();
            }

            run_times[r] = elapsed;
        }

        Arrays.sort(run_times);
        return run_times[0];
    }

    public static PerfTestResults runPerfTest(String lbl, Runnable preParallelRunnable,
            CheckedFunction parallelRunnable, Runnable postParallelRunnable, Runnable preSeqRunnable,
            CheckedFunction seqRunnable, Runnable postSeqRunnable, Runnable afterAll, final int nParRuns,
            final int nSeqRuns, final int nWorkerThreads, final boolean doPrint) throws SuspendableException {
        final long seqAvg = runPerfTestHelper(preSeqRunnable, seqRunnable, postSeqRunnable, nSeqRuns);
        final long parAvg = runPerfTestHelper(preParallelRunnable, parallelRunnable, postParallelRunnable, nParRuns);
        if (afterAll != null) afterAll.run();

        if (doPrint) {
            System.out.println("\nHABANERO-AUTOGRADER-PERF-TEST " + nWorkerThreads + "T " + lbl + " " + seqAvg +
                    " " + parAvg);
        }
        return new PerfTestResults(seqAvg, parAvg);
    }

    public static PerfTestResults runPerfTest(String lbl, Runnable preParallelRunnable,
            CheckedFunction parallelRunnable, Runnable postParallelRunnable, Runnable afterAll, final int nParRuns,
            final long providedSeqTime, final int nWorkerThreads, final boolean doPerfPrint) throws SuspendableException {
        final long parAvg = runPerfTestHelper(preParallelRunnable, parallelRunnable, postParallelRunnable, nParRuns);
        if (afterAll != null) afterAll.run();

        if (doPerfPrint) {
            System.out.println("\nHABANERO-AUTOGRADER-PERF-TEST " + nWorkerThreads + "T " + lbl + " " +
                    providedSeqTime + " " + parAvg);
        }
        return new PerfTestResults(providedSeqTime, parAvg);
    }

    public static PerfTestResults runPerfTestNoSuspend(String lbl, Runnable preParallelRunnable,
            Runnable parallelRunnable, Runnable postParallelRunnable, Runnable preSeqRunnable,
            Runnable seqRunnable, Runnable postSeqRunnable, Runnable afterAll, final int nParRuns,
            final int nSeqRuns, final int nWorkerThreads) {
        final long seqAvg = runPerfTestHelperNoSuspend(preSeqRunnable, seqRunnable, postSeqRunnable, nSeqRuns);
        final long parAvg = runPerfTestHelperNoSuspend(preParallelRunnable, parallelRunnable, postParallelRunnable,
                nParRuns);
        if (afterAll != null) afterAll.run();

        System.out.println("\nHABANERO-AUTOGRADER-PERF-TEST " + nWorkerThreads + "T " + lbl + " " + seqAvg + " " +
                parAvg);
        return new PerfTestResults(seqAvg, parAvg);
    }

    public static PerfTestResults runPerfTestNoSuspend(String lbl, Runnable preParallelRunnable,
            Runnable parallelRunnable, Runnable postParallelRunnable, Runnable afterAll, final int nParRuns,
            final long providedSeqTime, final int nWorkerThreads) {
        final long parAvg = runPerfTestHelperNoSuspend(preParallelRunnable, parallelRunnable, postParallelRunnable,
                nParRuns);
        if (afterAll != null) afterAll.run();

        System.out.println("\nHABANERO-AUTOGRADER-PERF-TEST " + nWorkerThreads + "T " + lbl + " " +
                providedSeqTime + " " + parAvg);
        return new PerfTestResults(providedSeqTime, parAvg);
    }


    // Must be called from the test entrypoint
    public static String getTestLabel() {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        StackTraceElement caller = stack[2];
        return caller.getClassName() + "." + caller.getMethodName();
    }


    @FunctionalInterface
    public interface CheckedFunction { void apply() throws SuspendableException; }
}

