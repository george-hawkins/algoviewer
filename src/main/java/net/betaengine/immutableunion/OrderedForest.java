package net.betaengine.immutableunion;

import java.util.Collection;
import java.util.LinkedHashSet;

import edu.uci.ics.jung.graph.DelegateForest;
import edu.uci.ics.jung.graph.DelegateTree;
import edu.uci.ics.jung.graph.DirectedOrderedSparseMultigraph;
import edu.uci.ics.jung.graph.Tree;
import edu.uci.ics.jung.graph.util.TreeUtils;

// This class exists purely to enable us to replace the HashSet instances in getRoots() and getTrees() with
// LinkedHashSet instances in order to preserve the ordering of the trees (and not just of their vertexes).
@SuppressWarnings("serial")
public class OrderedForest<V, E> extends DelegateForest<V, E> {
    public OrderedForest() {
        // Using DirectedOrderedSparseMultigraph (rather than the default DirectedSparseGraph
        // means children are ordered according to their insertion order.
        super(new DirectedOrderedSparseMultigraph<>());
    }
    @Override
    public Collection<V> getRoots() {
        Collection<V> roots = new LinkedHashSet<V>();
        for(V v : delegate.getVertices()) {
            if(delegate.getPredecessorCount(v) == 0) {
                roots.add(v);
            }
        }
        return roots;
    }

    @Override
    public Collection<Tree<V, E>> getTrees() {
        Collection<Tree<V,E>> trees = new LinkedHashSet<Tree<V,E>>();
        for(V v : getRoots()) {
            Tree<V,E> tree = new DelegateTree<V,E>();
            tree.addVertex(v);
            TreeUtils.growSubTree(this, tree, v);
            trees.add(tree);
        }
        return trees;
    }
}