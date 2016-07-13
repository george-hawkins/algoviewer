package net.betaengine.immutableunion;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Paint;
import java.util.List;
import java.util.function.Supplier;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.TreeLayout;
import edu.uci.ics.jung.graph.Forest;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.Renderer;

public class Sets {
    private final CallStack stack = new CallStack();
    private final Drawer drawer = new Drawer();
    
    private <T> T push(Object source, Supplier<T> s, String methodName) {
        try {
            stack.push(source, methodName);
            
            drawer.drawEnter(methodName);
            T result = s.get();
            drawer.drawExit(result, methodName);
            
            return result;
        } finally {
            stack.pop();
        }
    }
    
//  draw() {
//      synchronized (lock) {
//          SwingUtilities.invokeLater(doRun);
//          lock.wait(); // next button calls notify()
//      }
//  }
//  

    private class Drawer {
        private final Object drawLock = new Object();
        private boolean enabled = false;
        private Set currentResult = null;
        private Timer timer = new Timer(200, e -> advance());
        
        public Drawer() {
            timer.setRepeats(false);
        }
        
        public void drawEnter(String methodName) {
            synchronized (drawLock) {
                if (!enabled) {
                    return;
                }
            
                SwingUtilities.invokeLater(this::doDraw);
                wait(drawLock);
            }
        }
        
        public <T> void drawExit(T result, String methodName) {
            synchronized (drawLock) {
                if (!enabled) {
                    return;
                }
                
                if (!(result instanceof Set)) {
                    return;
                }
                
                currentResult = (Set)result;
            
                SwingUtilities.invokeLater(this::doDraw);
                wait(drawLock);
            }
        }
        
        private void doDraw() {
            System.out.print(".");
            
            // Remove all existing vertices (this will also remove all their edges).
            ImmutableList.copyOf(graph.getVertices()).stream().forEach(graph::removeVertex);
            
            populateGraph(graph);
            
            viewer.setGraphLayout(createLayout(graph));
            viewer.repaint();

            timer.start();
        }
        
        private Layout<Set, Integer> createLayout(Forest<Set, Integer> graph) {
            try {
                // If you call setGraph(...) on TreeLayout it still hangs onto state from the old graph (see `alreadyDone` etc.).
                // So we have to discard the old layout and create a new one.
                return new TreeLayout<Set, Integer>(graph);
            } catch (IllegalArgumentException e) {
                System.err.println(e.getMessage());
                
                return viewer.getGraphLayout();
            }
        }
        
        private void populateGraph(Forest<Set, Integer> graph) {
            addSet(graph, s1);
            addSet(graph, s2);
            
            if (currentResult != null) {
                addSet(graph, currentResult);
            }
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        private void advance() {
            synchronized (drawLock) {
                drawLock.notify();
            }
        }
        
        private void wait(Object o) {
            try {
                o.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
    }

    private interface Set {
        Set union(Set that);

        Set incl(int elem);
        
        List<Set> getChildren();
    }
    
    private abstract class AbstractSet implements Set {
        protected final static String UNION_METHOD = "union";
        protected final static String INCL_METHOD = "incl";
    }

    private class EmptySet extends AbstractSet {
        @Override
        public Set union(final Set that) {
            return push(this, () -> that, UNION_METHOD);
        }

        @Override
        public Set incl(final int elem) {
            return push(this, () -> new NonEmptySet(elem, new EmptySet(), new EmptySet()), INCL_METHOD);
        }
        
        @Override
        public List<Set> getChildren() { return ImmutableList.of(); }
        
        @Override
        public String toString() {
            return "-";
        }
    }

    private class NonEmptySet extends AbstractSet {
        private final int elem;
        private final Set left;
        private final Set right;

        // TODO: should be package private so only NonEmptySet and EmptySet can access it.
        public NonEmptySet(final int elem, final Set left, final Set right) {
            this.elem = elem;
            this.left = left;
            this.right = right;
        }

        @Override
        public Set union(final Set that) {
            return push(this, () -> {
                final Set leftUnion = left.union(that);
                final Set childrenUnion = right.union(leftUnion);
                final Set result = childrenUnion.incl(elem);
    
                return result;
            }, UNION_METHOD);
        }

        @Override
        public Set incl(final int x) {
            return push(this, () -> {
                final Set result;
    
                if (x < elem) {
                    result = new NonEmptySet(elem, left.incl(x), right);
                } else if (x > elem) {
                    result = new NonEmptySet(elem, left, right.incl(x));
                } else {
                    result = this;
                }
                
                return result;
            }, INCL_METHOD);
        }
        @Override
        public List<Set> getChildren() { return ImmutableList.of(left, right); }
        
        @Override
        public String toString() {
            return Integer.toString(elem);
        }
    }
    
    private final static int VIEWER_WIDTH = 1600;
    private final static int VIEWER_HEIGHT = 400;
    
    private /*final*/ Forest<Set, Integer> graph = new OrderedForest<>();
    
    private /*final*/ VisualizationViewer<Set, Integer> viewer = createViewer(graph);
    
    private boolean colorToggle = true;
    
    private Paint getColor(Set s) {
        return colorToggle ? Color.RED : Color.GREEN;
    }
    
    private VisualizationViewer<Set, Integer> createViewer(Forest<Set, Integer> graph) {
        TreeLayout<Set, Integer> layout = new TreeLayout<Set, Integer>(graph);
        VisualizationViewer<Set, Integer> viewer = new VisualizationViewer<Set, Integer>(layout, new Dimension(VIEWER_WIDTH, VIEWER_HEIGHT));
        
        viewer.setBackground(Color.WHITE);
        viewer.getRenderContext().setEdgeShapeTransformer(EdgeShape.line(graph));
        viewer.getRenderContext().setVertexFillPaintTransformer(this::getColor);
        viewer.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());
        viewer.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.CNTR);
        viewer.setVertexToolTipTransformer(new ToStringLabeller());

        return viewer;
    }
    
    private Set s1;
    private Set s2;

    private void addSet(Forest<Set, Integer> graph, Set set) {
        graph.addVertex(set);
        addChildren(graph, set);
    }

    private Integer edge = 1; // `edge` could be any class - using Integer is about the simplest.

    private void addChildren(Forest<Set, Integer> graph, Set parent) {
        for (Set child : parent.getChildren()) {
            graph.addEdge(edge++, parent, child);
            addChildren(graph, child);
        }
    }
    
    private boolean started = false;
    
    private void nextStep() {
        if (started) {
            System.err.println("Info: already started");
            return;
        }
        
        started = true;
    
        s1 = new EmptySet().incl(7).incl(3).incl(11).incl(1).incl(5).incl(9).incl(13);
        s2 = new EmptySet().incl(8).incl(4).incl(12).incl(2).incl(6).incl(10).incl(14);
        
        stack.clear();
        
        drawer.setEnabled(true);
        
        new Thread(() -> {
            s1.union(s2);
            System.err.println("Info: completed");
        }).start();
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel();
        JButton nextButton = new JButton("Next >");
        
        nextButton.addActionListener(e -> nextStep());
        
        panel.add(nextButton);
        
        return panel;
    }

    private void createAndShowGui() {
        JFrame frame = new JFrame("Demo");

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        Container content = frame.getContentPane();
        content.add(viewer, BorderLayout.CENTER);
        content.add(createButtonPanel(), BorderLayout.SOUTH);
        frame.pack();
        
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Sets().createAndShowGui());
    }

    static {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }
}
