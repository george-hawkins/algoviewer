package net.betaengine.immutableunion;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Paint;
import java.util.HashSet;
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
    
    private class Drawer {
        private final Object drawLock = new Object();
        private boolean enabled = false;
        private ImmutableList<?> currentStack;
        private Set currentResult = null;
        private Timer timer = new Timer(200, e -> advance());
        private boolean entry;
        
        public Drawer() {
            timer.setRepeats(false);
        }
        
        public Paint getColor(Set s) {
            synchronized (drawLock) {
                if (currentStack.contains(s)) {
                    Object last = currentStack.get(0);
                    
                    if (s == last) {
                        return entry ? Color.GREEN : Color.RED;
                    } else {
                        return Color.BLUE;
                    }
                } else {
                    return Color.YELLOW;
                }
            }
        }
        
        public void drawEnter(String methodName) {
            synchronized (drawLock) {
                if (!enabled) {
                    return;
                }
                
                entry = true;
                currentStack = stack.copyStack();
            
                SwingUtilities.invokeLater(this::doDraw);
                wait(drawLock);
            }
        }
        
        public <T> void drawExit(T result, String methodName) {
            if (!(result instanceof Set)) {
                return;
            }
            
            synchronized (drawLock) {
                if (!enabled) {
                    return;
                }
                
                entry = false;
                currentStack = stack.copyStack();
                
                currentResult = (Set)result;
            
                SwingUtilities.invokeLater(this::doDraw);
                wait(drawLock);
            }
        }
        
        private void doDraw() {
            synchronized (drawLock) {
                System.err.print(".");
                
                // Remove all existing vertices (this will also remove all their edges).
                ImmutableList.copyOf(graph.getVertices()).stream().forEach(graph::removeVertex);
                
                populateGraph(graph);
                
                // If you call setGraph(...) on TreeLayout it still hangs onto state from the old graph (see `alreadyDone` etc.).
                // So we have to discard the old layout and create a new one.
                Layout<Set, Integer> newLayout = new TreeLayout<Set, Integer>(graph);
                
                viewer.setGraphLayout(newLayout);
    
                timer.start();
            }
        }
        
        private void populateGraph(Forest<Set, Integer> graph) {
            alreadyVisited.clear();
            
            addSubTree(graph, s1);
            addSubTree(graph, s2);
            
            if (currentResult != null) {
                if (alreadyVisited.contains(currentResult)) {
                    // This is a shortcoming of the current setup...
                    System.err.println("Info: cannot draw current result " + currentResult + " as it's a pure subtree of one of the existing trees");
                    // addSubTree(...) will simply ignore currentResult.
                }
                
                addSubTree(graph, currentResult);
            }
        }
        
        private final HashSet<Set> alreadyVisited = new HashSet<>();
        private Integer edge = 1; // `edge` could be any class - using Integer is about the simplest.

        private void addSubTree(Forest<Set, Integer> graph, Set parent) {
            if (!alreadyVisited.contains(parent)) {
                alreadyVisited.add(parent);
                
                if (parent.getChildren().isEmpty()) {
                    graph.addVertex(parent);
                } else {
                    for (Set child : parent.getChildren()) {
                        graph.addEdge(edge++, parent, child);
                        addSubTree(graph, child);
                    }
                }
            }
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
        
        public void clearResult() {
            currentResult = null;
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
        
        String getName();
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
            return ".";
        }
        
        @Override
        public String getName() {
            return toString();
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
            return "{" + left + elem + right + "}";
        }
        
        @Override
        public String getName() {
            return Integer.toString(elem);
        }
    }
    
    private final static int VIEWER_WIDTH = 1600;
    private final static int VIEWER_HEIGHT = 400;
    
    private final Forest<Set, Integer> graph = new OrderedForest<>();
    
    private final VisualizationViewer<Set, Integer> viewer = createViewer(graph);
    
    private VisualizationViewer<Set, Integer> createViewer(Forest<Set, Integer> graph) {
        TreeLayout<Set, Integer> layout = new TreeLayout<Set, Integer>(graph);
        VisualizationViewer<Set, Integer> viewer = new VisualizationViewer<Set, Integer>(layout, new Dimension(VIEWER_WIDTH, VIEWER_HEIGHT));
        
        viewer.setBackground(Color.WHITE);
        viewer.getRenderContext().setEdgeShapeTransformer(EdgeShape.line(graph));
        viewer.getRenderContext().setVertexFillPaintTransformer(drawer::getColor);
        viewer.getRenderContext().setVertexLabelTransformer(s -> s.getName());
        viewer.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.CNTR);
        viewer.setVertexToolTipTransformer(new ToStringLabeller());

        return viewer;
    }
    
    private Set s1;
    private Set s2;

    private final JButton nextButton = new JButton("Start >");
    
    private void start() {
        nextButton.setEnabled(false);
        
        // These calls are only relevant if we're redoing things, i.e. user pressed restart.
        drawer.clearResult();;
        drawer.setEnabled(false);
    
        s1 = new EmptySet().incl(7).incl(3).incl(11).incl(1).incl(5).incl(9).incl(13);
        s2 = new EmptySet().incl(8).incl(4).incl(12).incl(2).incl(6).incl(10).incl(14);
        
        stack.clear();
        
        drawer.setEnabled(true);
        
        new Thread(() -> {
            s1.union(s2);
            SwingUtilities.invokeLater(() -> finished());
        }).start();
    }
    
    private void finished() {
        System.err.println("Info: completed - " + stack);

        nextButton.setText("Restart >");
        nextButton.setEnabled(true);
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel();
        nextButton.addActionListener(e -> start());
        
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
