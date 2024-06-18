package edu.rice.comp322.boruvka;

import edu.rice.comp322.util.IntPair;

import java.io.*;
import java.util.*;
import java.util.zip.GZIPInputStream;

/**
 * This class should not be modified.
 *
 * @author <a href="http://shams.web.rice.edu/">Shams Imam</a> (shams@rice.edu)
 */
public final class Loader {

    // http://www.dis.uniroma1.it/challenge9/download.shtml
    public static final String[] inputs = {
        "src/main/resources/boruvka/USA-road-d.NY.gr.gz", // 0
        "src/main/resources/boruvka/USA-road-d.BAY.gr.gz", // 1
        "src/main/resources/boruvka/USA-road-d.COL.gr.gz", // 2
        "src/main/resources/boruvka/USA-road-d.FLA.gr.gz", // 3
        "src/main/resources/boruvka/USA-road-d.NW.gr.gz", // 4
        "src/main/resources/boruvka/USA-road-d.NE.gr.gz" // 5
    };
    public static String fileName = inputs[0];

    /**
     * Utility for parsing the configuration of the current run from a string array.
     */
    public static void parseArgs(final String[] args) {
        int i = 0;

        while (i < args.length) {
            final String loopOptionKey = args[i];

            switch (loopOptionKey) {
                case "-f":
                    i += 1;
                    final String value = args[i];
                    if (value.matches("[0-9]+")) {
                        final int valueInt = Integer.parseInt(value);
                        final int inputIndex = Math.max(0, Math.min(valueInt, inputs.length));
                        fileName = inputs[inputIndex];
                    } else {
                        fileName = value;
                    }
                    break;
                default:
                    break;
            }

            i += 1;
        }
    }

    /**
     * Read edges from the provided input file.
     */
    public static <C extends Component, E extends Edge> void read(final BoruvkaFactory<C, E> boruvkaFactory,
            final Queue<C> nodesLoaded) {

        final Map<Integer, C> nodesMap = new HashMap<>();
        final Map<IntPair, E> edgesMap = new HashMap<>();

        double totalWeight = 0;
        int edges = 0;
        try {
            // Open the compressed file
            final GZIPInputStream in = new GZIPInputStream(new FileInputStream(fileName));
            final Reader r = new BufferedReader(new InputStreamReader(in));
            final StreamTokenizer st = new StreamTokenizer(r);
            final String cstring = "c";
            final String pstring = "p";
            st.commentChar(cstring.charAt(0));
            st.commentChar(pstring.charAt(0));
            // read graph
            while (st.nextToken() != StreamTokenizer.TT_EOF) {
                assert (st.sval.equals("a"));
                st.nextToken();
                final int from = (int) st.nval;
                st.nextToken();
                final int to = (int) st.nval;
                final C nodeFrom = getComponent(boruvkaFactory, nodesMap, from);
                final C nodeTo = getComponent(boruvkaFactory, nodesMap, to);
                assert (nodeTo != nodeFrom); // Assume no self-loops in the input graph
                st.nextToken();
                final int weight = (int) st.nval;
                addEdge(boruvkaFactory, edgesMap, from, to, nodeFrom, nodeTo, weight);
                totalWeight += weight;
                edges++;
            }
            // Close the file and stream
            in.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }

        final List<C> nodesList = new ArrayList<>();
        nodesList.addAll(nodesMap.values());
        Collections.shuffle(nodesList);
        nodesLoaded.addAll(nodesList);
    }

    private static <C extends Component, E extends Edge> C getComponent(final BoruvkaFactory<C, E> factory,
            final Map<Integer, C> nodesMap, final int node) {
        if (!nodesMap.containsKey(node)) {
            nodesMap.put(node, factory.newComponent(node));
        }
        return nodesMap.get(node);
    }

    private static <C extends Component, E extends Edge> void addEdge(
            final BoruvkaFactory<C, E> factory, final Map<IntPair, E> edgesMap,
            final int from, final int to, final C fromC, final C toC, final double w) {

        final IntPair p;
        if (from < to) {
            p = new IntPair(from, to);
        } else {
            p = new IntPair(to, from);
        }
        if (!edgesMap.containsKey(p)) {
            final E e = factory.newEdge(fromC, toC, w);
            edgesMap.put(p, e);
            fromC.addEdge(e);
            toC.addEdge(e);
        } else {
            assert (edgesMap.get(p).weight() == w);
        }
    }
}
