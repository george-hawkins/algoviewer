package net.betaengine.immutableunion;

import java.awt.Color;
import java.awt.Paint;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;

import edu.uci.ics.jung.graph.Forest;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import net.betaengine.algoviewer.BasicGui;
import net.betaengine.algoviewer.Drawer;
import net.betaengine.algoviewer.DrawerCallstack;
import net.betaengine.immutableunion.Sets.Set;

public class UnionAlgoViewer {
    private final DrawerCallstack stack = new DrawerCallstack();
    
    private Drawer drawer;

    private static class UnionDrawer extends SetDrawer {
        private Set s1;
        private Set s2;
        
        public UnionDrawer(Forest<Set, Integer> graph, VisualizationViewer<Set, Integer> viewer) {
            super(graph, viewer);
        }

        @Override
        protected void setupAlgo() {
            s1 = Sets.createEmptySet().incl(7).incl(3).incl(11).incl(1).incl(5).incl(9).incl(13);
            s2 = Sets.createEmptySet().incl(8).incl(4).incl(12).incl(2).incl(6).incl(10).incl(14);
        }

        @Override
        protected void runAlgo() {
            s1.union(s2);
        }
        
        @Override
        protected void populateGraph(Set currentResult) {
            addSet(s1);
            addSet(s2);
            
            if (currentResult != null) {
                addSet(currentResult);
            }
        }
    }

    private void createAndShowGui() {
        Sets.setPusher(stack);
        
        BasicGui<Set, Integer> basicGui = new BasicGui<Set, Integer>("Demo") {
            @Override
            protected Paint getColor(Set s) {
                return UnionAlgoViewer.this.getColor(s);
            }
            
            @Override
            protected void doStart() {
                drawer.runAlgo(stack::clear, () -> {
                    System.err.println("Info: completed - " + stack);
                    finished();
                });
            }

            @Override
            protected String getVertexName(Set s) {
                return s.getName();
            }
        };
        
        drawer = new UnionDrawer(basicGui.getGraph(), basicGui.getViewer());
        
        stack.setDrawer(drawer);
        
        basicGui.show();
    }
    
    private Paint getColor(Set s) {
        return getColor(stack.getCurrentStack(), stack.isEntry(), s);
    }
    
    private static Paint getColor(ImmutableList<?> stack, boolean entry, Set s) {
        if (!stack.contains(s)) {
            return Color.YELLOW;
        }
        
        return s != stack.get(0) ? Color.BLUE : (entry ? Color.GREEN : Color.RED);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new UnionAlgoViewer().createAndShowGui());
    }

    static {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }
}
