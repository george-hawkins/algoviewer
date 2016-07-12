package net.betaengine.immutableunion;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import edu.uci.ics.jung.algorithms.layout.TreeLayout;
import edu.uci.ics.jung.graph.DelegateForest;
import edu.uci.ics.jung.graph.Forest;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;

public class TreeViewer {
    private JPanel createPanel() {
        Forest<String, Integer> graph = createTree();
        TreeLayout<String, Integer> layout = new TreeLayout<String, Integer>(graph);
        VisualizationViewer<String, Integer> viewer = new VisualizationViewer<String, Integer>(layout, new Dimension(600, 600));
        
        viewer.setBackground(Color.WHITE);
        viewer.getRenderContext().setEdgeShapeTransformer(EdgeShape.line(graph));
        viewer.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());
        viewer.setVertexToolTipTransformer(new ToStringLabeller());

        return viewer;
    }

    private Forest<String, Integer> createTree() {
        Forest<String, Integer> graph = new DelegateForest<String, Integer>();
        
        Integer vertex = 1; // Vertex could be any class - using Integer is about the simplest.

        graph.addVertex("V0");
        graph.addEdge(vertex++, "V0", "V1");
        graph.addEdge(vertex++, "V0", "V2");
        graph.addEdge(vertex++, "V1", "V4");
        graph.addEdge(vertex++, "V2", "V3");
        graph.addEdge(vertex++, "V2", "V5");
        graph.addEdge(vertex++, "V4", "V6");
        graph.addEdge(vertex++, "V4", "V7");
        graph.addEdge(vertex++, "V3", "V8");
        graph.addEdge(vertex++, "V6", "V9");
        graph.addEdge(vertex++, "V4", "V10");

        graph.addVertex("A0");
        graph.addEdge(vertex++, "A0", "A1");
        graph.addEdge(vertex++, "A0", "A2");
        graph.addEdge(vertex++, "A0", "A3");

        graph.addVertex("B0");
        graph.addEdge(vertex++, "B0", "B1");
        graph.addEdge(vertex++, "B0", "B2");
        graph.addEdge(vertex++, "B1", "B4");
        graph.addEdge(vertex++, "B2", "B3");
        graph.addEdge(vertex++, "B2", "B5");
        graph.addEdge(vertex++, "B4", "B6");
        graph.addEdge(vertex++, "B4", "B7");
        graph.addEdge(vertex++, "B3", "B8");
        graph.addEdge(vertex++, "B6", "B9");
        
        System.err.println("tree count: " + graph.getTrees().size());

        return graph;
    }

    private void createAndShowGui() {
        JFrame frame = new JFrame("Demo");

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(createPanel());
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TreeViewer().createAndShowGui());
    }
}