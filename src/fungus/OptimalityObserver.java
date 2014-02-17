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

public class OptimalityObserver implements Control {
  private static Graph<MycoNode,MycoEdge> graph;
  //private Forest<MycoNode,MycoEdge> forest;
  //private DistanceStatistics ds = new DistanceStatistics();

  private static final String PAR_PERIOD = "period";
  private static int period;

  private static long optimalSuperpeerCount;
  private static MycoList optimalSuperpeers;

  private static Logger log =
      Logger.getLogger(OptimalityObserver.class.getName());

  public OptimalityObserver(String prefix) {
    graph = JungGraphObserver.getGraph();
    optimalSuperpeerCount = 0;
    optimalSuperpeers = new MycoList();
    period = Configuration.getInt(prefix + "." + PAR_PERIOD);

    ExperimentWriter.addMetric(new Metric<Long>("optimalSuperpeers") {public Long fetch() { return OptimalityObserver.getOptimalSuperpeerCount(); }});

  }


  public static long countOptimalSuperpeers() {
    MycoList allNodes = new MycoList(graph.getVertices());
    optimalSuperpeers = new MycoList();
    Collections.sort(allNodes, new MycoNodeCapacityComparator());
    long capacityCount = 0;
    long peerCount = allNodes.size();

    while (capacityCount < peerCount) {
      MycoNode n = allNodes.remove(allNodes.size() - 1);
      optimalSuperpeers.add(n);
      capacityCount += n.getHyphaData().getMaxCapacity();
    }

    optimalSuperpeerCount = optimalSuperpeers.size();
    return optimalSuperpeerCount;
  }

  public static long getOptimalSuperpeerCount() {
    return optimalSuperpeerCount;
  }

  public boolean execute() {
    if (CDState.getCycle() % period != 0)
        return false;

    countOptimalSuperpeers();
    log.info("OPTIMUM SUPERPEER COUNT: " + optimalSuperpeerCount);
    return false;
  }

}
