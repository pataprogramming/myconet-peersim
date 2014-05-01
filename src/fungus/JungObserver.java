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
import peersim.core.*;
import peersim.util.*;
import java.util.*;
import java.util.logging.*;

import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import edu.uci.ics.jung.algorithms.importance.*;
import edu.uci.ics.jung.algorithms.layout.*;
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

public class JungObserver implements Control {
  private static final String PAR_HYPHADATA_PROTO = "hyphadata_proto";
  private static final String PAR_HYPHALINK_PROTO = "hyphalink_proto";
  private static final String PAR_MYCOCAST_PROTO = "mycocast_proto";

  private static Logger log = Logger.getLogger(JungObserver.class.getName());

  private final String name;
  private final int hyphadataPid;
  private final int hyphalinkPid;
  private final int mycocastPid;

  private Graph<MycoNode,String> graph;
  private JFrame frame;
  //private BasicVisualizationServer<Node,String> visualizer;
  private VisualizationViewer<MycoNode,String> visualizer;

  private Thread mainThread;
  private boolean stepBlocked = true;
  private boolean noBlock = false;

  private Object self;

  public JungObserver(String name) {
    this.name = name;
    this.hyphadataPid = Configuration.getPid(name + "." + PAR_HYPHADATA_PROTO);
    this.hyphalinkPid = Configuration.getPid(name + "." + PAR_HYPHALINK_PROTO);
    this.mycocastPid = Configuration.getPid(name + "." + PAR_MYCOCAST_PROTO);
    self = this;
    mainThread = Thread.currentThread();

    graph = new DirectedSparseGraph<MycoNode,String>();
    edu.uci.ics.jung.algorithms.layout.SpringLayout<MycoNode,String> layout =
        new edu.uci.ics.jung.algorithms.layout.SpringLayout<MycoNode,String>(graph);
    layout.setSize(new Dimension(650,650));
    layout.setRepulsionRange(75);
    //layout.setForceMultiplier(0.75);
    layout.setStretch(0.85);

    Dimension preferredSize = new Dimension(650,650);
    VisualizationModel<MycoNode,String> visualizationModel =
        new DefaultVisualizationModel<MycoNode,String>(layout, preferredSize);
    visualizer = new VisualizationViewer<MycoNode,String>(visualizationModel,
                                                          preferredSize);
    visualizer.addGraphMouseListener(new InfoFrameVertexListener());

    //visualizer = new BasicVisualizationServer<Node,String>(layout);
    //visualizer.setPreferredSize(new Dimension(650, 650));

    Transformer<MycoNode,String> nodeLabeller = new Transformer<MycoNode,String>() {
        public String transform(MycoNode n) {
          return Long.toString(n.getID());
        }
      };

    final Shape biomassCircle = new Ellipse2D.Float(-2.5f,-2.5f,5.0f,5.0f);
    final Shape hyphaCircle = new Ellipse2D.Float(-5.0f,-5.0f,10.0f,10.0f);
    Transformer<MycoNode,Shape> shapeTransformer =
        new Transformer<MycoNode,Shape>() {
          public Shape transform(MycoNode n) {
            HyphaData data = n.getHyphaData();
            if (data.isBiomass()) {
              return biomassCircle;
            } else {
              return hyphaCircle;
            }
          }

        };

    Transformer<MycoNode,Paint> nodeFillRenderer = new Transformer<MycoNode,Paint>() {
        public Paint transform(MycoNode n) {
          HyphaData data = (HyphaData) n.getProtocol(hyphadataPid);
          if (!n.isUp()) { return Color.BLACK; }
          if (data.isBiomass()) { return Color.BLUE; }
          else if (data.isExtending()) { return Color.RED; }
          else if (data.isBranching()) { return Color.YELLOW; }
          else { return Color.GREEN; }
        }
      };
    Transformer<MycoNode,Paint> nodeShapeRenderer = new Transformer<MycoNode,Paint>() {
        public Paint transform(MycoNode n) {
          HyphaData data = (HyphaData) n.getProtocol(hyphadataPid);
          if (data.isBiomass()) { return Color.BLUE; }
          else if (data.isExtending()) { return Color.RED; }
          else if (data.isBranching()) { return Color.YELLOW; }
          else { return Color.GREEN; }
        }
      };

    final Stroke biomassStroke = new BasicStroke(0.25f, BasicStroke.CAP_BUTT,
                                                 BasicStroke.JOIN_MITER, 10.0f);
    final Stroke hyphalStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
                                                BasicStroke.JOIN_MITER, 10.0f);

    Transformer<String,Stroke> edgeStrokeTransformer =
        new Transformer<String, Stroke>() {
          public Stroke transform(String s) {
            Pair<MycoNode> vertices = graph.getEndpoints(s);
            HyphaData firstData = vertices.getFirst().getHyphaData();
            HyphaData secondData = vertices.getSecond().getHyphaData();
            if (firstData.isHypha() && secondData.isHypha()) {
              return hyphalStroke;
            } else {
              return biomassStroke;
            }
          }
        };

    visualizer.getRenderContext().setVertexFillPaintTransformer(nodeFillRenderer);
    visualizer.getRenderContext().setVertexShapeTransformer(shapeTransformer);
    visualizer.getRenderContext().setVertexLabelTransformer(nodeLabeller);
    visualizer.setVertexToolTipTransformer(new ToStringLabeller<MycoNode>());
    visualizer.getRenderContext().setEdgeStrokeTransformer(edgeStrokeTransformer);
    frame = new JFrame();
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    Container c = frame.getContentPane();
    c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));

    //JScrollPane scrollPane = new JScrollPane(visualizer);
    //c.add(scrollPane);
    c.add(visualizer);

    JPanel buttonPane = new JPanel();
    buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.X_AXIS));

    JButton pauseButton = new JButton("pause");
    ActionListener pauser = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          //e.consume();
          synchronized(self) {
            stepBlocked = true;
            noBlock = false;
            //self.notifyAll();
          }
        }
      };
    pauseButton.addActionListener(pauser);

    JButton stepButton = new JButton("step");
    ActionListener stepper = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          System.out.println("Clicked!\n");
          //e.consume();
          synchronized(self) {
            stepBlocked = false;
            self.notifyAll();
          }
        }
      };
    stepButton.addActionListener(stepper);

    JButton runButton = new JButton("run");
    ActionListener runner = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          //e.consume();
          synchronized(self) {
            stepBlocked = false;
            noBlock = true;
            self.notifyAll();
          }
        }
      };
    runButton.addActionListener(runner);

    buttonPane.add(pauseButton);
    buttonPane.add(stepButton);
    buttonPane.add(runButton);
    c.add(buttonPane);

    frame.pack();
    frame.setVisible(true);
  }

  public boolean execute() {
    MycoCast mycocast = (MycoCast) Network.get(0).getProtocol(mycocastPid);

    int bio = mycocast.countBiomass();
    int bul = mycocast.countBulwark();
    int ext = mycocast.countExtending();
    int bra = mycocast.countBranching();
    int imm = mycocast.countImmobile();

    // Need to wipe existing graph (reimplement with some intelligence)

    //for (String v : graph.getVertices()) {
    //    System.out.println("womble");
    //    graph.removeVertex(v);
    // }

    // Update vertices
    Set<MycoNode> activeNodes = new HashSet<MycoNode>();
    for (int i = 0; i < Network.size(); i++) {
      MycoNode n = (MycoNode) Network.get(i);
      activeNodes.add(n);
      HyphaData data = n.getHyphaData();
      //if (data.isBiomass()) { continue; }
      if (graph.containsVertex(n)) {
        graph.removeVertex(n);
      }
      if (!graph.containsVertex(n)) {
        graph.addVertex(n);
      }
    }
    Set<MycoNode> visualizerNodes =
        new HashSet<MycoNode>(graph.getVertices());
    visualizerNodes.removeAll(activeNodes);

    for (MycoNode n : visualizerNodes) {
      graph.removeVertex(n);
    }

    // Update edges
    for (int i = 0; i < Network.size(); i++) {
      MycoNode n = (MycoNode) Network.get(i);
      HyphaData data = n.getHyphaData();
      HyphaLink link = n.getHyphaLink();
      MycoList neighbors = link.getHyphae();
      Collection<MycoNode> jungNeighbors = graph.getNeighbors(n);

      for (MycoNode o : jungNeighbors) {
        if (!neighbors.contains(o)) {
          String edge = graph.findEdge(n,o);
          if (edge != null) {
            graph.removeEdge(edge);
          }
          /*String edgeName;
            if (n.getID() < o.getID()) {
            edgeName = Long.toString(n.getID()) + "-"  +
            Long.toString(o.getID());
            } else {
            edgeName = Long.toString(o.getID()) + "-"  +
            Long.toString(n.getID());
            }
            graph.removeEdge(edgeName);*/
        }
      }

      for (MycoNode o : neighbors) {
        String edgeName;
        edgeName = Long.toString(n.getID()) + "-"  +
            Long.toString(o.getID());
        if (!graph.containsEdge(edgeName)) {
          graph.addEdge(edgeName, n, o, EdgeType.DIRECTED);
        }
        /*                String edgeName;
                          if (n.getID() < o.getID()) {
                          edgeName = Long.toString(n.getID()) + "-"  +
                          Long.toString(o.getID());
                          } else {
                          edgeName = Long.toString(o.getID()) + "-"  +
                          Long.toString(n.getID());
                          }
                          if (!graph.containsEdge(edgeName)) {
                          graph.addEdge(edgeName, n, o, EdgeType.UNDIRECTED);
                          }*/
      }
    }
    visualizer.stateChanged(new ChangeEvent(graph));

    try {
      while (stepBlocked && !noBlock) {
        synchronized(this) {
          wait();
        }
      }
    } catch (InterruptedException e) {
      stepBlocked = true;
    }
    stepBlocked = true;

    //System.out.println(graph.toString());
    return false;
  }
  /*
    protected MycoCast getMycoCast(MycoNode n) {
    return n.getMycoCast();
    return (MycoCast) n.getProtocol(mycocastPid);
    }

    public HyphaData getHyphaData(MycoNode n) {
    return n.getHyphaData();
    }

    public HyphaLink getHyphaLink(MycoNode n) {
    return n.getHyphaLink();
    }
  */
  ///public FungalGrowth getFungalGrowth(Node n) {
  //    return (FungalGrowth) n.getProtocol(fungalgrowthPid);
  //}

}
