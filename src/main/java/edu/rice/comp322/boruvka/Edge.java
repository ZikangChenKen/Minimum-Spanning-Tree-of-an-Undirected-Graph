package edu.rice.comp322.boruvka;

/**
 * This class should not be modified.
 *
 * @author <a href="http://shams.web.rice.edu/">Shams Imam</a> (shams@rice.edu)
 */
public abstract class Edge<C extends Component> {
    public abstract double weight();

    public abstract Edge<C> replaceComponent(final C other, final C seqComponent);

    public abstract C fromComponent();

    public abstract C toComponent();

    public abstract C getOther(final C loopNode);
}
