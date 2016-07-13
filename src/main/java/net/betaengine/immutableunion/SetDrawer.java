package net.betaengine.immutableunion;

import java.util.HashSet;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import edu.uci.ics.jung.algorithms.layout.TreeLayout;
import edu.uci.ics.jung.graph.Forest;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import net.betaengine.algoviewer.Drawer;
import net.betaengine.immutableunion.Sets.Set;

public abstract class SetDrawer extends Drawer {
    private final HashSet<Set> alreadyVisited = new HashSet<>();
    private int edge = 1; // `edge` could be any class - using int and boxing is about the simplest.

    private final Forest<Set, Integer> graph;
    private final VisualizationViewer<Set, Integer> viewer;

    public SetDrawer(Forest<Set, Integer> graph, VisualizationViewer<Set, Integer> viewer) {
        this.graph = graph;
        this.viewer = viewer;
    }

    @Override
    protected void doDraw(Object currentResult) {
        Preconditions.checkArgument(currentResult == null || currentResult instanceof Set);
        
        clear();
        
        populateGraph((Set)currentResult);
        
        // You can call setGraph(...) on the existing TreeLayout but if you do it still hangs onto
        // state from the old graph (see `alreadyDone` etc.). So we create a completely new one...
        viewer.setGraphLayout(new TreeLayout<Set, Integer>(graph));
    }
    
    protected abstract void populateGraph(Set currentResult);

    protected void addSet(Set s) {
        if (alreadyVisited.contains(s)) {
            // This is a shortcoming of the current setup...
            System.err.println("Info: cannot add " + s + " as it's a pure subtree of one of the existing trees");
        } else {
            addSubTree(s);
        }
    }
    
    private void addSubTree(Set parent) {
        if (!alreadyVisited.contains(parent)) {
            alreadyVisited.add(parent);
            
            if (parent.getChildren().isEmpty()) {
                graph.addVertex(parent);
            } else {
                for (Set child : parent.getChildren()) {
                    graph.addEdge(edge++, parent, child);
                    addSubTree(child);
                }
            }
        }
    }
    
    private void clear() {
        // Remove all existing vertices (this will also remove all their edges).
        ImmutableList.copyOf(graph.getVertices()).stream().forEach(graph::removeVertex);
        
        alreadyVisited.clear();
        edge = 1;
    }
}
