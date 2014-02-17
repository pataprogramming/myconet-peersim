/* Copyright (c) 2014, Paul L. Snyder <paul@pataprogramming.com>,
 * Daniel Dubois, Nicolo Calcavecchia.
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * Any later version. It may also be redistributed and/or modified under the
 * terms of the BSD 3-Clause License.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */


package fungus;

import peersim.config.*;
import peersim.cdsim.CDState;
import peersim.core.*;
import peersim.util.*;
import java.util.*;
import java.util.logging.*;
import java.text.DecimalFormat;

import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.*;
import edu.uci.ics.jung.algorithms.importance.*;
import edu.uci.ics.jung.algorithms.layout.*;
import edu.uci.ics.jung.algorithms.layout.util.*;
import edu.uci.ics.jung.algorithms.util.*;
import edu.uci.ics.jung.graph.*;
import edu.uci.ics.jung.graph.util.*;
import edu.uci.ics.jung.visualization.*;
import edu.uci.ics.jung.visualization.control.*;
import edu.uci.ics.jung.visualization.decorators.*;
import edu.uci.ics.jung.visualization.picking.*;
import edu.uci.ics.jung.visualization.renderers.*;
import org.apache.commons.collections15.*;
import org.apache.commons.collections15.functors.*;

public class JungVisualizer implements Control {
  private static final String PAR_JUNGGRAPH_PROTO = "junggraph_proto";
  private static final String PAR_SHOW_EDGES = "show_edges";
  private static final String PAR_SCALE_SHAPES = "scale_shapes";
  private static final String PAR_LABEL_NODES = "label_nodes";

  private static final String PAR_IMAGE_DIR = "image_dir";
  private static final String PAR_TESTNAME = "config.basename";

  private static final String PAR_TRANSFORMERS = "transformers";

  private static Logger log = Logger.getLogger(JungVisualizer.class.getName());

  private final String name;
  //    private final int jungGraphPid;

  private static boolean showEdges;
  private static boolean scaleShapes;
  private static boolean labelNodes;

  private static String imageDir;
  private static String nameFragment;

  private MycoGraph graph;

  private JFrame frame;
  //private BasicVisualizationServer<Node,String> visualizer;
  private VisualizationViewer<MycoNode,MycoEdge> visualizer;
  private VisualizationModel<MycoNode,MycoEdge> visualizationModel;

  private JTextField roundField;

  private edu.uci.ics.jung.algorithms.layout.Layout<MycoNode,MycoEdge> layout;
  private Relaxer relaxer;

  private VisualizerTransformers vt;


  public void saveAsPNG() {

    String filePath = String.format("%s/%s-%03d.png", imageDir, nameFragment,
                                    CDState.getCycle());

    File dir = new File(imageDir);
    dir.mkdir();

    Dimension size = visualizer.getSize();
    BufferedImage image =
        new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_RGB);
    Graphics2D g2 = image.createGraphics();
    visualizer.paint(g2);

    try {
      File file = new File(filePath);
      ImageIO.write(image, "png", file);
    } catch (Exception e) {
      System.out.println(e);
    }
  }


  public JungVisualizer(String name) {
    this.name = name;

    showEdges = Configuration.getBoolean(name + "." + PAR_SHOW_EDGES);
    scaleShapes = Configuration.getBoolean(name + "." + PAR_SCALE_SHAPES);
    labelNodes = Configuration.getBoolean(name + "." + PAR_LABEL_NODES);
    imageDir = Configuration.getString(name + "." + PAR_IMAGE_DIR);
    nameFragment = Configuration.getString(PAR_TESTNAME);

    if (vt == null) {
      try {
        Class vtClass = Configuration.getClass(name + "." + PAR_TRANSFORMERS);
        if (VisualizerTransformers.class.isAssignableFrom(vtClass)) {
          vt = (VisualizerTransformers) vtClass.newInstance();
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    graph = JungGraphObserver.getGraph();

    vt.setGraph(graph);  // Have to call this manually!

    JungGraphObserver.stepBlocked = true;
    JungGraphObserver.noBlock = false;

    layout = vt.makeLayout(graph);

    Dimension preferredSize = new Dimension(660,660);
    visualizationModel = new
        DefaultVisualizationModel<MycoNode,MycoEdge>(layout, preferredSize);

    relaxer = visualizationModel.getRelaxer();

    visualizer =
        new VisualizationViewer<MycoNode,MycoEdge>(visualizationModel,
                                                   preferredSize);
    visualizer.addGraphMouseListener(new InfoFrameVertexListener());

    visualizer.setDoubleBuffered(true);


    // final Color under50VertexColor = Color.BLACK;
    // final Stroke under50VertexStroke = new BasicStroke(1.0f,
    //                                                    BasicStroke.CAP_BUTT,
    //                                                    BasicStroke.JOIN_MITER);
    // final Color over50VertexColor = Color.MAGENTA;
    // final Stroke over50VertexStroke = new BasicStroke(2.0f,
    //                                                   BasicStroke.CAP_BUTT,
    //                                                   BasicStroke.JOIN_MITER);
    // final Color over80VertexColor = Color.BLUE;
    // final Stroke over80VertexStroke = new BasicStroke(2.0f,
    //                                                   BasicStroke.CAP_BUTT,
    //                                                   BasicStroke.JOIN_MITER);
    // final Color over95VertexColor = Color.GREEN;
    // final Stroke over95VertexStroke = new BasicStroke(2.0f,
    //                                                   BasicStroke.CAP_BUTT,
    //                                                   BasicStroke.JOIN_MITER);
    // final Color over100VertexColor = Color.RED;
    // final Stroke over100VertexStroke = new BasicStroke(5.0f,
    //                                                    BasicStroke.CAP_BUTT,
    //                                                    BasicStroke.JOIN_MITER);
    // Transformer<MycoNode,Stroke> nodeStrokeRenderer =
    //     new Transformer<MycoNode, Stroke>() {
    //   public Stroke transform(MycoNode n){
    //     int capacity = n.getHyphaData().getMax();
    //     int attached = n.getHyphaLink().degree();
    //     double ratio = ((double) attached) / ((double) capacity);

    //     if (ratio > 1.0) {
    //       return over100VertexStroke;
    //     } else if (ratio > 0.95) {
    //       return over95VertexStroke;
    //     } else if (ratio > 0.80) {
    //       return over80VertexStroke;
    //     } else if (ratio > 0.50) {
    //       return over50VertexStroke;
    //     } else {
    //       return under50VertexStroke;
    //     }
    //   }
    // };
    // Transformer<MycoNode,Paint> nodeOutlineRenderer =
    //     new Transformer<MycoNode, Paint>() {
    //   public Paint transform(MycoNode n) {
    //     int capacity = n.getHyphaData().getMax();
    //     int attached = n.getHyphaLink().degree();
    //     double ratio = ((double) attached) / ((double) capacity);

    //     if (ratio > 1.0) {
    //       return over100VertexColor;
    //     } else if (ratio > 0.95) {
    //       return over95VertexColor;
    //     } else if (ratio > 0.80) {
    //       return over80VertexColor;
    //     } else if (ratio > 0.50) {
    //       return over50VertexColor;
    //     } else {
    //       return under50VertexColor;
    //     }
    //   }
    // };

    /*Transformer<MycoNode,Paint> nodeFillRenderer = new Transformer<MycoNode,Paint>() {
      public Paint transform(MycoNode n) {
      HyphaData data = n.getHyphaData();
      if (!n.isUp()) { return Color.BLACK; }
      if (data.isBiomass()) { return Color.BLUE; }
      else if (data.isExtending()) { return Color.RED; }
      else if (data.isBranching()) { return Color.YELLOW; }
      else { return Color.GREEN; }
      }
      };*/

    /*Transformer<MycoNode,Paint> nodeFillRenderer = new Transformer<MycoNode,Paint>() {
      public Paint transform(MycoNode n) {
      HyphaData data = n.getHyphaData();
      if (data.isBiomass()) { return Color.BLUE; }
      else if (data.isExtending()) { return Color.RED; }
      else if (data.isBranching()) { return Color.YELLOW; }
      else { return Color.GREEN; }
      }
      };*/


    final Color transparent = new Color(0,0,0,0);

    Transformer<MycoEdge,Paint> transparentEdges =
        new Transformer<MycoEdge, Paint>() {
      public Paint transform(MycoEdge e) {
        return transparent;
      }
    };

    visualizer.setBackground(Color.WHITE);

    visualizer.getRenderContext()
        .setVertexFillPaintTransformer(vt.getNodeFillRenderer());
    visualizer.getRenderContext()
        .setVertexShapeTransformer(vt.getShapeTransformer(scaleShapes));
    if (labelNodes) {
      visualizer.getRenderContext()
          .setVertexLabelTransformer(vt.getNodeLabeller());
    }
    visualizer.getRenderContext().setVertexStrokeTransformer(vt.getVertexStrokeTransformer());
    visualizer.getRenderContext().setVertexDrawPaintTransformer(vt.getVertexDrawPaintTransformer());
    //visualizer.setVertexToolTipTransformer(new ToStringLabeller());

    if (showEdges) {
      visualizer.getRenderContext()
          .setEdgeStrokeTransformer(vt.getEdgeStrokeTransformer());
      visualizer.getRenderContext()
          .setEdgeDrawPaintTransformer(vt.getEdgeDrawPaintTransformer());
      visualizer.getRenderContext()
          .setArrowDrawPaintTransformer(vt.getEdgeDrawPaintTransformer());
      visualizer.getRenderContext()
          .setArrowFillPaintTransformer(vt.getEdgeDrawPaintTransformer());
    } else {
      visualizer.getRenderContext()
          .setEdgeDrawPaintTransformer(transparentEdges);
      visualizer.getRenderContext()
          .setArrowDrawPaintTransformer(transparentEdges);
      visualizer.getRenderContext()
          .setArrowFillPaintTransformer(transparentEdges);
    }

    frame = new JFrame();
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    Container c = frame.getContentPane();
    c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));

    //JScrollPane scrollPane = new JScrollPane(visualizer);
    //c.add(scrollPane);
    c.add(visualizer);

    JPanel buttonPane = new JPanel();
    buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.X_AXIS));

    final JButton captureButton = new JButton("capture");
    ActionListener capturer = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          saveAsPNG();
        }
      };
    captureButton.addActionListener(capturer);

    final JButton freezeButton = new JButton("freeze");
    ActionListener freezer = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (freezeButton.getText().equals("freeze")) {
            relaxer.pause();
            freezeButton.setText("unfreeze");
          } else {
            relaxer.resume();
            freezeButton.setText("freeze");
          }
        }
      };
    freezeButton.addActionListener(freezer);

    JButton pauseButton = new JButton("pause");
    ActionListener pauser = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          //e.consume();
          JungGraphObserver.pauseAction();
        }
      };
    pauseButton.addActionListener(pauser);

    JButton stepButton = new JButton("step");
    ActionListener stepper = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          System.out.println("Clicked!\n");
          //e.consume();
          JungGraphObserver.stepAction();
        }
      };
    stepButton.addActionListener(stepper);

    JButton walkButton = new JButton("walk");
    ActionListener walker = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          System.out.println("Clicked!\n");
          //e.consume();
          JungGraphObserver.walkAction();
        }
      };
    walkButton.addActionListener(walker);

    JButton runButton = new JButton("run");
    ActionListener runner = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          //e.consume();
          JungGraphObserver.runAction();
        }
      };
    runButton.addActionListener(runner);


    roundField = new JTextField("0");

    buttonPane.add(freezeButton);
    buttonPane.add(captureButton);
    buttonPane.add(Box.createHorizontalGlue());
    buttonPane.add(pauseButton);
    buttonPane.add(stepButton);
    buttonPane.add(walkButton);
    buttonPane.add(runButton);
    buttonPane.add(Box.createHorizontalGlue());
    buttonPane.add(roundField);
    c.add(buttonPane);

    frame.pack();
    frame.setVisible(true);

    JungGraphObserver.setVisualizer(visualizer);
  }

  public boolean execute() {
    // Update virtualization transformers (if implemented by extending
    // class) Specifically, this handles updating colors in case of
    // changing numbers of types for clustering scenarios
    vt.execute();

    roundField.setText(new Integer(CDState.getCycle()).toString());
    return false;
  }
}
