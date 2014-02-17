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

import peersim.cdsim.*;
import peersim.config.*;
import peersim.core.*;
import peersim.util.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.*;
import java.io.*;
//import java.awt.*;
//import java.awt.geom.*;
//import java.awt.event.*;
//import javax.swing.*;
//import javax.swing.event.*;
import edu.uci.ics.jung.algorithms.importance.*;
import edu.uci.ics.jung.algorithms.layout.*;
import edu.uci.ics.jung.algorithms.metrics.*;
import edu.uci.ics.jung.algorithms.shortestpath.*;
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

public class ConnectivityObserver implements Control {
  private MycoGraph graph;
  //private Forest<MycoNode,MycoEdge> forest;
  private DistanceStatistics ds = new DistanceStatistics();


  private static long componentCount;

  private static Logger log =
      Logger.getLogger(ConnectivityObserver.class.getName());

  private static final String PAR_PERIOD = "period";
  private static int period;

  public ConnectivityObserver(String prefix) {
    graph = JungGraphObserver.getGraph();
    period = Configuration.getInt(prefix + "." + PAR_PERIOD);

    ExperimentWriter.addMetric(new Metric<Long>("componentCount") {public Long fetch() { return ConnectivityObserver.getComponentCount(); }});
  }

  public static long getComponentCount() {
    return componentCount;
  }

  public boolean execute() {
    if (CDState.getCycle() % period != 0)
        return false;

    //Set<Set<MycoNode>> forest = graph.findConnectedComponents();
    //log.info(forest.size() + " GRAPH COMPONENTS");
    //componentCount = forest.size();
    Forest<MycoNode,MycoEdge> forest = graph.getMinimumSpanningForest();

    componentCount = forest.getTrees().size();
    log.info(componentCount + " GRAPH COMPONENTS");

    /*
      List<Integer> sizes = new ArrayList<Integer>();
      StringBuffer sb  = new StringBuffer();
      java.util.Formatter f = new java.util.Formatter(sb, Locale.US);

      for (Set<MycoNode> tree : forest) {
      sizes.add(tree.size());
      if (tree.size() < 10) {
      f.format("[COMPONENT: ");
      for (MycoNode n : tree) {
      f.format(n.toString());
      f.format(" ");
      }
      f.format("]");
      }
      }
      log.info(sb.toString());
    */
    //Collections.sort(sizes);
    //sb  = new StringBuffer();
    //f = new java.util.Formatter(sb, Locale.US);
    //f.format("Sizes:");
    //for (int i : sizes) {
    //    f.format(" %d", i);
    //}
    //log.info(sb.toString());

    //Transformer<MycoNode,Double> avgDist =
    //ds.averageDistances(graph, new UnweightedShortestPath<MycoNode,MycoEdge>(graph));

    Map<MycoNode,Double> ccMap = Metrics.clusteringCoefficients(graph);

    double ccGlobal = 0.0;
    for (double l : ccMap.values()) {
      ccGlobal += l;
    }

    ccGlobal /= ccMap.size();

    // double acc = 0.0;
    // System.out.println("CALCULATING DISTANCE");
    // for (MycoNode n : graph.getVertices()) {
    //     System.out.println("Checking..." +n);
    //     acc += avgDist.transform(n);
    // }

    // acc /= graph.getVertexCount();

    // log.info("AVERAGE DISTANCE " + acc);
    log.info("DIAMETER " + ds.diameter(graph));
    log.info("CLUSTERING COEFFICIENT " + ccGlobal);
    return false;
  }

}
