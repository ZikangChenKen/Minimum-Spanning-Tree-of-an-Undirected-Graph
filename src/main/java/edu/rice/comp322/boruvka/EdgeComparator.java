package edu.rice.comp322.boruvka;

import java.util.Comparator;

/**
 * This class should not be modified.
 *
 * @author <a href="http://shams.web.rice.edu/">Shams Imam</a> (shams@rice.edu)
 */
final class EdgeComparator implements Comparator<Edge> {
    public int compare(final Edge e1, final Edge e2) {
        if (e1.weight() == e2.weight()) {
            return 0;
        } else if (e1.weight() < e2.weight()) {
            return -1;
        } else {
            return 1;
        }
    }
}
