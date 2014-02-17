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
import java.awt.geom.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
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

public class UtilizationObserver implements Control {
  private Graph<MycoNode,MycoEdge> graph;
  //private Forest<MycoNode,MycoEdge> forest;
  //private DistanceStatistics ds = new DistanceStatistics();

  private static final String PAR_PERIOD = "period";
  private static int period;

  private static Set<ChangeListener> changeListeners =
      new HashSet<ChangeListener>();

  public static double nodeCount;
  public static double biomassCount;
  public static double hyphaCount;
  public static double extendingCount;
  public static double branchingCount;
  public static double immobileCount;
  public static double bulwarkCount;
  public static double averageDegree;
  public static double averageExtendingDegree;
  public static double averageBranchingDegree;
  public static double averageImmobileDegree;
  public static double averageBulwarkDegree;
  public static double totalDegree;
  public static double totalExtendingDegree;
  public static double totalBranchingDegree;
  public static double totalImmobileDegree;
  public static double totalBulwarkDegree;
  public static double hyphaUtilization;
  public static double extendingUtilization;
  public static double branchingUtilization;
  public static double immobileUtilization;
  public static double bulwarkUtilization;
  public static double totalHyphaCapacity;
  public static double totalBiomassCapacity;
  public static double totalExtendingCapacity;
  public static double totalBranchingCapacity;
  public static double totalImmobileCapacity;
  public static double totalBulwarkCapacity;
  public static double utilLow;
  public static double util50;
  public static double util80;
  public static double util95;
  public static double util100;
  public static double utilOver;
  public static double extendingUtilLow;
  public static double extendingUtil50;
  public static double extendingUtil80;
  public static double extendingUtil95;
  public static double extendingUtil100;
  public static double extendingUtilOver;
  public static double branchingUtilLow;
  public static double branchingUtil50;
  public static double branchingUtil80;
  public static double branchingUtil95;
  public static double branchingUtil100;
  public static double branchingUtilOver;
  public static double immobileUtilLow;
  public static double immobileUtil50;
  public static double immobileUtil80;
  public static double immobileUtil95;
  public static double immobileUtil100;
  public static double immobileUtilOver;
  public static double utilLowCount;
  public static double util50Count;
  public static double util80Count;
  public static double util95Count;
  public static double util100Count;
  public static double utilOverCount;
  public static double extendingUtilLowCount;
  public static double extendingUtil50Count;
  public static double extendingUtil80Count;
  public static double extendingUtil95Count;
  public static double extendingUtil100Count;
  public static double extendingUtilOverCount;
  public static double branchingUtilLowCount;
  public static double branchingUtil50Count;
  public static double branchingUtil80Count;
  public static double branchingUtil95Count;
  public static double branchingUtil100Count;
  public static double branchingUtilOverCount;
  public static double immobileUtilLowCount;
  public static double immobileUtil50Count;
  public static double immobileUtil80Count;
  public static double immobileUtil95Count;
  public static double immobileUtil100Count;
  public static double immobileUtilOverCount;

  public static double hyphaRatio;
  public static double extendingHyphaRatio;
  public static double branchingHyphaRatio;
  public static double immobileHyphaRatio;
  public static double bulwarkRatio;
  public static double totalBranchingUtil;
  public static double totalExtendingUtil;
  public static double totalImmobileUtil;
  public static double totalBulwarkUtil;
  public static double averageUtil;
  public static double averageExtendingUtil;
  public static double averageBranchingUtil;
  public static double averageImmobileUtil;
  public static double averageBulwarkUtil;

  public static double stableUtilization;
  public static double stableCount;
  public static double totalStableCapacity;
  public static double stableHyphaRatio;

  public UtilizationObserver(String prefix) {
    period = Configuration.getInt(prefix + "." + PAR_PERIOD);
    graph = JungGraphObserver.getGraph();
    clearStats();

    ExperimentWriter.addMetric(new Metric<Double>("extendingDegreeMean") {public Double fetch() { return UtilizationObserver.averageExtendingDegree; }});
    ExperimentWriter.addMetric(new Metric<Double>("branchingDegreeMean") {public Double fetch() { return UtilizationObserver.averageBranchingDegree; }});
    ExperimentWriter.addMetric(new Metric<Double>("immobileDegreeMean") {public Double fetch() { return UtilizationObserver.averageImmobileDegree; }});
    ExperimentWriter.addMetric(new Metric<Double>("bulwarkDegreeMean") {public Double fetch() { return UtilizationObserver.averageBulwarkDegree; }});
    ExperimentWriter.addMetric(new Metric<Integer>("totalDegree") {public Integer fetch() { return (new Double(UtilizationObserver.totalDegree)).intValue(); }});
    ExperimentWriter.addMetric(new Metric<Integer>("extendingDegree") {public Integer fetch() { return (new Double(UtilizationObserver.totalExtendingDegree)).intValue(); }});
    ExperimentWriter.addMetric(new Metric<Integer>("branchingDegree") {public Integer fetch() { return (new Double(UtilizationObserver.totalBranchingDegree)).intValue(); }});
    ExperimentWriter.addMetric(new Metric<Integer>("immobileDegree") {public Integer fetch() { return (new Double(UtilizationObserver.totalImmobileDegree)).intValue(); }});
    ExperimentWriter.addMetric(new Metric<Integer>("bulwarkDegree") {public Integer fetch() { return (new Double(UtilizationObserver.totalBulwarkDegree)).intValue(); }});
    ExperimentWriter.addMetric(new Metric<Integer>("hyphaCapacity") {public Integer fetch() { return (new Double(UtilizationObserver.totalHyphaCapacity)).intValue(); }});
    ExperimentWriter.addMetric(new Metric<Integer>("biomassCapacity") {public Integer fetch() { return (new Double(UtilizationObserver.totalBiomassCapacity)).intValue(); }});
    ExperimentWriter.addMetric(new Metric<Integer>("extendingCapacity") {public Integer fetch() { return (new Double(UtilizationObserver.totalExtendingCapacity)).intValue(); }});
    ExperimentWriter.addMetric(new Metric<Integer>("branchingCapacity") {public Integer fetch() { return (new Double(UtilizationObserver.totalBranchingCapacity)).intValue(); }});
    ExperimentWriter.addMetric(new Metric<Integer>("immobileCapacity") {public Integer fetch() { return (new Double(UtilizationObserver.totalImmobileCapacity)).intValue(); }});
  }

  private static void clearStats() {
    nodeCount = 0.0;
    biomassCount = 0.0;
    hyphaCount = 0.0;
    extendingCount = 0.0;
    branchingCount = 0.0;
    immobileCount = 0.0;
    bulwarkCount = 0.0;
    averageDegree = 0.0;
    averageExtendingDegree = 0.0;
    averageBranchingDegree = 0.0;
    averageImmobileDegree = 0.0;
    averageBulwarkDegree = 0.0;
    totalDegree = 0.0;
    totalExtendingDegree = 0.0;
    totalBranchingDegree = 0.0;
    totalImmobileDegree = 0.0;
    totalBulwarkDegree = 0.0;
    hyphaUtilization = 0.0;
    extendingUtilization = 0.0;
    branchingUtilization = 0.0;
    immobileUtilization = 0.0;
    bulwarkUtilization = 0.0;
    totalHyphaCapacity = 0.0;
    totalBiomassCapacity = 0.0;
    totalExtendingCapacity = 0.0;
    totalBranchingCapacity = 0.0;
    totalImmobileCapacity = 0.0;
    totalBulwarkCapacity = 0.0;
    utilLow = 0.0;
    util50 = 0.0;
    util80 = 0.0;
    util95 = 0.0;
    util100 = 0.0;
    utilOver = 0.0;
    extendingUtilLow = 0.0;
    extendingUtil50 = 0.0;
    extendingUtil80 = 0.0;
    extendingUtil95 = 0.0;
    extendingUtil100 = 0.0;
    extendingUtilOver = 0.0;
    branchingUtilLow = 0.0;
    branchingUtil50 = 0.0;
    branchingUtil80 = 0.0;
    branchingUtil95 = 0.0;
    branchingUtil100 = 0.0;
    branchingUtilOver = 0.0;
    immobileUtilLow = 0.0;
    immobileUtil50 = 0.0;
    immobileUtil80 = 0.0;
    immobileUtil95 = 0.0;
    immobileUtil100 = 0.0;
    immobileUtilOver = 0.0;
    utilLowCount = 0.0;
    util50Count = 0.0;
    util80Count = 0.0;
    util95Count = 0.0;
    util100Count = 0.0;
    utilOverCount = 0.0;
    extendingUtilLowCount = 0.0;
    extendingUtil50Count = 0.0;
    extendingUtil80Count = 0.0;
    extendingUtil95Count = 0.0;
    extendingUtil100Count = 0.0;
    extendingUtilOverCount = 0.0;
    branchingUtilLowCount = 0.0;
    branchingUtil50Count = 0.0;
    branchingUtil80Count = 0.0;
    branchingUtil95Count = 0.0;
    branchingUtil100Count = 0.0;
    branchingUtilOverCount = 0.0;
    immobileUtilLowCount = 0.0;
    immobileUtil50Count = 0.0;
    immobileUtil80Count = 0.0;
    immobileUtil95Count = 0.0;
    immobileUtil100Count = 0.0;
    immobileUtilOverCount = 0.0;

    hyphaRatio = 0.0;
    extendingHyphaRatio = 0.0;
    branchingHyphaRatio = 0.0;
    immobileHyphaRatio = 0.0;
    bulwarkRatio = 0.0;
    totalBranchingUtil = 0.0;
    totalExtendingUtil = 0.0;
    totalImmobileUtil = 0.0;
    totalBulwarkUtil = 0.0;
    averageUtil = 0.0;
    averageExtendingUtil = 0.0;
    averageBranchingUtil = 0.0;
    averageImmobileUtil = 0.0;
    averageBulwarkUtil = 0.0;

    stableUtilization = 0.0;
    stableCount = 0.0;
    totalStableCapacity = 0.0;
    stableHyphaRatio = 0.0;
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
      cl.stateChanged(new ChangeEvent(UtilizationObserver.class));
    }
  }

  public static void updateStats() {
    clearStats();

    MycoList bulwark = MycoCast.getBulwarkNodes();
    MycoList extending = MycoCast.getExtendingHyphae();
    MycoList branching = MycoCast.getBranchingHyphae();
    MycoList immobile = MycoCast.getImmobileHyphae();
    MycoList biomass = MycoCast.getAllBiomass();
    MycoList hyphae = MycoCast.getAllHyphae();
    MycoList all = MycoCast.getAllNodes();

    bulwarkCount = bulwark.size();
    hyphaCount = hyphae.size();
    biomassCount = biomass.size();
    extendingCount = extending.size();
    branchingCount = branching.size();
    immobileCount = immobile.size();

    nodeCount = all.size();

    double nodeUtil;
    //totalDegree = 0.0;
    //totalStableDegree = 0.0;
    double totalUtil = 0.0;
    double totalExtendingUtil = 0.0;
    double totalBranchingUtil = 0.0;
    double totalImmobileUtil = 0.0;
    double totalBulwarkUtil = 0.0;
    totalHyphaCapacity = 0.0;
    totalStableCapacity = 0.0;
    totalBulwarkCapacity = 0.0;

    for (MycoNode n : biomass) {
      totalBiomassCapacity += n.getHyphaData().getMaxCapacity();
    }

    for (MycoNode n : bulwark) {
      totalBulwarkCapacity += n.getHyphaData().getMaxCapacity();
      nodeUtil = (new Integer(n.getHyphaLink().degree())).doubleValue()
          / n.getHyphaData().getIdealBiomass();
      totalBulwarkUtil += nodeUtil;
      totalBulwarkDegree += n.getHyphaLink().degree();
    }

    for (MycoNode n : hyphae) {
      //boolean isStable = n.getHyphaData().isBranching() ||
      //    n.getHyphaData().isImmobile();
      boolean isExtending = n.getHyphaData().isExtending();
      boolean isBranching = n.getHyphaData().isBranching();
      boolean isImmobile = n.getHyphaData().isImmobile();

      totalDegree += n.getHyphaLink().degree();
      totalHyphaCapacity += n.getHyphaData().getMaxCapacity();

      nodeUtil = (new Integer(n.getHyphaLink().sameBiomassDegree())).doubleValue()
          / n.getHyphaData().getIdealBiomass();

      totalUtil += nodeUtil;

      if (isExtending) {
        totalExtendingUtil += nodeUtil;
        totalExtendingDegree += n.getHyphaLink().degree();
        totalExtendingCapacity += n.getHyphaData().getMaxCapacity();
      }
      if (isBranching) {
        totalBranchingUtil += nodeUtil;
        totalBranchingDegree += n.getHyphaLink().degree();
        totalBranchingCapacity += n.getHyphaData().getMaxCapacity();
      }
      if (isImmobile) {
        totalImmobileUtil += nodeUtil;
        totalImmobileDegree += n.getHyphaLink().degree();
        totalImmobileCapacity += n.getHyphaData().getMaxCapacity();
      }

      if (nodeUtil < 0.5) {
        utilLowCount += 1.0;
        if (isExtending) {
          extendingUtilLowCount += 1.0;
        } else if (isBranching) {
          branchingUtilLowCount += 1.0;
        } else if (isImmobile) {
          immobileUtilLowCount += 1.0;
        }
      }
      if (nodeUtil >= 0.5) {
        util50Count += 1.0;
        if (isExtending) {
          extendingUtil50Count += 1.0;
        } else if (isBranching) {
          branchingUtil50Count += 1.0;
        } else if (isImmobile) {
          immobileUtil50Count += 1.0;
        }
      }
      if (nodeUtil >= 0.8) {
        util80Count += 1.0;
        if (isExtending) {
          extendingUtil80Count += 1.0;
        } else if (isBranching) {
          branchingUtil80Count += 1.0;
        } else if (isImmobile) {
          immobileUtil80Count += 1.0;
        }
      }
      if (nodeUtil >= 0.95) {
        util95Count += 1.0;
        if (isExtending) {
          extendingUtil95Count += 1.0;
        } else if (isBranching) {
          branchingUtil95Count += 1.0;
        } else if (isImmobile) {
          immobileUtil95Count += 1.0;
        }
      }
      if (nodeUtil == 1.0) {
        util100Count += 1.0;
        if (isExtending) {
          extendingUtil100Count += 1.0;
        } else if (isBranching) {
          branchingUtil100Count += 1.0;
        } else if (isImmobile) {
          immobileUtil100Count += 1.0;
        }
      }
      if (nodeUtil > 1.0) {
        utilOverCount += 1.0;
        if (isExtending) {
          extendingUtilOverCount += 1.0;
        } else if (isBranching) {
          branchingUtilOverCount += 1.0;
        } else if (isImmobile) {
          immobileUtilOverCount += 1.0;
        }
      }
    }

    if (hyphaCount != 0.0) {
      utilLow = utilLowCount / hyphaCount;
      util50 = util50Count / hyphaCount;
      util80 = util80Count / hyphaCount;
      util95 = util95Count / hyphaCount;
      util100 = util100Count / hyphaCount;
      utilOver = utilOverCount / hyphaCount;
      averageDegree = totalDegree / hyphaCount;
      averageUtil = totalUtil / hyphaCount;
    }
    if (extendingCount != 0.0) {
      extendingUtilLow = extendingUtilLowCount / extendingCount;
      extendingUtil50 = extendingUtil50Count / extendingCount;
      extendingUtil80 = extendingUtil80Count / extendingCount;
      extendingUtil95 = extendingUtil95Count / extendingCount;
      extendingUtil100 = extendingUtil100Count / extendingCount;
      extendingUtilOver = extendingUtilOverCount / extendingCount;
      averageExtendingDegree = totalExtendingDegree / extendingCount;
      averageExtendingUtil = totalExtendingUtil / extendingCount;
    }
    if (branchingCount != 0.0) {
      branchingUtilLow = branchingUtilLowCount / branchingCount;
      branchingUtil50 = branchingUtil50Count / branchingCount;
      branchingUtil80 = branchingUtil80Count / branchingCount;
      branchingUtil95 = branchingUtil95Count / branchingCount;
      branchingUtil100 = branchingUtil100Count / branchingCount;
      branchingUtilOver = branchingUtilOverCount / branchingCount;
      averageBranchingDegree = totalBranchingDegree / branchingCount;
      averageBranchingUtil = totalBranchingUtil / branchingCount;
    }
    if (immobileCount != 0.0) {
      immobileUtilLow = immobileUtilLowCount / immobileCount;
      immobileUtil50 = immobileUtil50Count / immobileCount;
      immobileUtil80 = immobileUtil80Count / immobileCount;
      immobileUtil95 = immobileUtil95Count / immobileCount;
      immobileUtil100 = immobileUtil100Count / immobileCount;
      immobileUtilOver = immobileUtilOverCount / immobileCount;
      averageImmobileDegree = totalImmobileDegree / immobileCount;
      averageImmobileUtil = totalImmobileUtil / immobileCount;
    }

    if (totalHyphaCapacity != 0.0) {
      hyphaUtilization = totalDegree / totalHyphaCapacity;
    }
    if (totalExtendingCapacity != 0.0) {
      extendingUtilization = totalExtendingDegree/totalExtendingCapacity;
    }
    if (totalBranchingCapacity != 0.0) {
      branchingUtilization = totalBranchingDegree/totalBranchingCapacity;
    }
    if (totalImmobileCapacity != 0.0) {
      immobileUtilization = totalImmobileDegree / totalImmobileCapacity;
    }
    if (totalBulwarkCapacity != 0.0) {
      bulwarkUtilization = totalBulwarkDegree / totalBulwarkCapacity;
    }

    if (nodeCount != 0.0) {
      hyphaRatio = hyphaCount / nodeCount;
      extendingHyphaRatio = extendingCount / nodeCount;
      branchingHyphaRatio = branchingCount / nodeCount;
      immobileHyphaRatio = immobileCount / nodeCount;
      bulwarkRatio = bulwarkCount / nodeCount;
    }

    stableCount = immobileCount + branchingCount;
    if (stableCount != 0.0) {
      stableUtilization = ((totalImmobileUtil +
                            totalBranchingUtil)
                           / stableCount);
      stableHyphaRatio = stableCount / nodeCount;
    }

    notifyChangeListeners();
  }

  private static Logger log =
      Logger.getLogger(UtilizationObserver.class.getName());

  public boolean execute() {
    if (CDState.getCycle() % period != 0)
        return false;

    updateStats();

    StringBuilder sb = new StringBuilder();
    java.util.Formatter f = new java.util.Formatter(sb, Locale.US);
    f.format("<50%%: %1.3f 50%%: %1.3f 80%%: %1.3f \n95%%: %1.3f 100%%: %1.3f Over%%: %1.3f\n", utilLow, util50, util80, util95, util100, utilOver);
    f.format("Avg Degree: %.2f Avg Util: %1.3f Hypha Ratio: %1.3f\n",
             averageDegree, averageUtil, hyphaRatio);
    f.format("Hyphal Utilization: %1.3f Stable Hypha Utilization %1.3f\n",
             hyphaUtilization, stableUtilization);
    f.format("Total Hyphal Capacity: %1.0f Total Stable Hyphal Capacity: %1.0f\n",
             totalHyphaCapacity, totalStableCapacity);
    f.format("Network Size: %d\n", Network.size());

    log.info(sb.toString());

    return false;
  }

}
