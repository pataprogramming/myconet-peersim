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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import peersim.cdsim.CDState;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import edu.uci.ics.jung.graph.Forest;
import edu.uci.ics.jung.graph.Graph;

public class TypeObserver implements Control {

  private static MycoGraph graph;
  // private Forest<MycoNode,MycoEdge> forest;
  // private DistanceStatistics ds = new DistanceStatistics();

  private static List<Graph<MycoNode, MycoEdge>> typeGraphs;
  private static List<MycoList> nodeLists = new ArrayList<MycoList>();

  private static Set<ChangeListener> changeListeners = new HashSet<ChangeListener>();

  private static final String PAR_PERIOD = "period";
  private static int period;

  public static int numTypes = 0;
  public static List<TypeStats> stats = new ArrayList<TypeStats>();

  protected static Random generator;

  private static Logger log = Logger.getLogger(TypeObserver.class.getName());

  private static int totalCapacity = 0;

  public static int getNumTypes() {
    return numTypes;
  }

  public static Graph<MycoNode, MycoEdge> getTypeGraph(int t) {
    if (t >= numTypes) {
      return new MycoGraph();
    }
    return typeGraphs.get(t);
  }

  public static int getTypeCapacity(int t) {
    if (t >= stats.size()) {
      return 0;
    }
    return stats.get(t).capacity;
  }

  public static int getTotalCapacity() {
    if (totalCapacity == 0)
        for (int i = 0; i < numTypes; i++) {
          totalCapacity += getTypeCapacity(i);
        }

    return totalCapacity;
  }

  public static int getActiveJobsOfType(int t) {
    if (t >= nodeLists.size()) {
      return 0;
    }
    int jobCount = 0;
    for (MycoNode n : nodeLists.get(t)) {
      HyphaData d = n.getHyphaData();
      jobCount += d.getQueueLength();
    }
    return jobCount;
  }


  public static double getIdealQueueLength(MycoNode n) {
    double totalJobs = getActiveJobsOfType(n.getHyphaData().getType());
    double thisCapacity = n.getHyphaData().getCapacity();
    double totalCapacity = getTypeCapacity(n.getHyphaData().getType());
    return totalJobs * (thisCapacity / totalCapacity);
  }


  public static double getMetric(MycoNode n) {
    double actualQueueLength = n.getHyphaData().getQueueLength();
    double idealQueueLength = getIdealQueueLength(n);
    double thisCapacity = n.getHyphaData().getCapacity();

    double metric;

    if (actualQueueLength > idealQueueLength) {
      metric = 1.0 - (idealQueueLength / actualQueueLength);
    } else if (actualQueueLength < idealQueueLength) {
      metric = -1.0 + (actualQueueLength / idealQueueLength);
    } else {
      metric = 0.0;
    }

    //double metric = java.lang.Math.abs(actualQueueLength - idealQueueLength) / thisCapacity;
    return metric;
    //return idealQueueLength;
  }


  public TypeObserver(String prefix) {
    period = Configuration.getInt(prefix + "." + PAR_PERIOD);
    graph = JungGraphObserver.getGraph();
    resetStats();

    if (generator == null) {
      TypeObserver.generator = CommonState.r;
    }
  }

  private static void resetStats() {
    // Wipe stats from last round
    for (TypeStats s : stats) {
      s.reset();
    }

    // Add new stat objects if more types have been added
    numTypes = HyphaData.numTypes; // Stash locally for log writer
    while (numTypes > stats.size()) {
      stats.add(new TypeStats(stats.size()));
    }
  }

  public static void addChangeListener(ChangeListener cl) {
    changeListeners.add(cl);
  }

  public static void removeChangeListener(ChangeListener cl) {
    if (changeListeners.contains(cl)) {
      changeListeners.remove(cl);
    }
  }

  public static void notifyChangeListeners() {
    for (ChangeListener cl : changeListeners) {
      cl.stateChanged(new ChangeEvent(TypeObserver.class));
    }
  }

  public static void updateStats() {
    log.fine("Num Types: " + numTypes + " Type Graphs: "
             + typeGraphs.size());

    for (int t = 0; t < numTypes; t++) {
      Graph<MycoNode, MycoEdge> tg = typeGraphs.get(t);
      TypeStats s = stats.get(t);

      s.count = tg.getVertexCount();
      for (MycoNode n : tg.getVertices()) {
        if (n.getHyphaData().isDead()) {
          s.count--;
          continue;
        }
        s.capacity += n.getHyphaData().getMaxCapacity();
      }
      // log.finer(s.toString());

      /* Don't calc - blows heap for 5,000 nodes
         Forest<MycoNode, MycoEdge> f = MycoGraph
         .getMinimumSpanningForest(tg);
         s.components = f.getTrees().size();
         log.finer(s.toString());
      */
      s.components = -1;

    }

    notifyChangeListeners();
  }

  public static MycoNode getRandomNodeOfType(int type) {
    if (type > nodeLists.size() || (nodeLists.get(type).size() == 0)) {
      return null;
    }
    MycoList nl = nodeLists.get(type);
    return nl.get(generator.nextInt(nl.size()));
  }


  public void updateNodeLists() {
    log.fine("Updating lists of nodes by type");

    for (MycoList l : nodeLists) {
      l.clear();
    }

    while (nodeLists.size() < numTypes) {
      nodeLists.add(new MycoList());
    }
    for (int t = 0; t < nodeLists.size(); t++) {
      nodeLists.get(t).addAll(typeGraphs.get(t).getVertices());
    }
  }



  public boolean execute() {
    if (CDState.getCycle() % period != 0)
        return false;

    resetStats();

    typeGraphs = graph.getTypeGraphs();
    updateNodeLists();

    updateStats();

    StringBuilder sb = new StringBuilder();
    java.util.Formatter f = new java.util.Formatter(sb, Locale.US);

    f.format("Type Counts: ");
    for (TypeStats ts : stats) {
      f.format("%s ", ts.toString());
    }

    log.finer(sb.toString());
    return false;
  }

}
