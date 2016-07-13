package net.betaengine.algoviewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Paint;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import edu.uci.ics.jung.algorithms.layout.TreeLayout;
import edu.uci.ics.jung.graph.Forest;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.Renderer;

public abstract class BasicGui<V, E> {
    private final static int VIEWER_WIDTH = 1600;
    private final static int VIEWER_HEIGHT = 400;
    
    private final JFrame frame;
    private final JButton startButton = new JButton("Start >");
    
    private final Forest<V, E> graph = new OrderedForest<>();
    private final VisualizationViewer<V, E> viewer = createViewer(graph);
    
    public Forest<V, E> getGraph() { return graph; }
    
    public VisualizationViewer<V, E> getViewer() { return viewer; }
    
    private VisualizationViewer<V, E> createViewer(Forest<V, E> graph) {
        TreeLayout<V, E> layout = new TreeLayout<V, E>(graph);
        VisualizationViewer<V, E> viewer = new VisualizationViewer<V, E>(layout, new Dimension(VIEWER_WIDTH, VIEWER_HEIGHT));
        
        viewer.setBackground(Color.WHITE);
        viewer.getRenderContext().setEdgeShapeTransformer(EdgeShape.line(graph));
        viewer.getRenderContext().setVertexFillPaintTransformer(this::getColor);
        viewer.getRenderContext().setVertexLabelTransformer(this::getVertexName);
        viewer.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.CNTR);
        viewer.setVertexToolTipTransformer(new ToStringLabeller());

        return viewer;
    }
    
    protected abstract Paint getColor(V s);
    
    protected abstract String getVertexName(V v);
    
    protected abstract void doStart();
    
    protected void finished() {
        startButton.setText("Restart >");
        startButton.setEnabled(true);
    }
    
    private JPanel createButtonPanel() {
        startButton.addActionListener(e -> {
            startButton.setEnabled(false);
            
            doStart();
        });
        
        JPanel panel = new JPanel();
        
        panel.add(startButton);
        
        return panel;
    }

    public BasicGui(String title) {
        frame = new JFrame("Demo");

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        Container content = frame.getContentPane();
        content.add(viewer, BorderLayout.CENTER);
        content.add(createButtonPanel(), BorderLayout.SOUTH);
    }
    
    public void show() {
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
