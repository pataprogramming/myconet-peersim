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
import java.util.*;
import java.util.logging.*;
import org.apache.commons.collections15.*;
import edu.uci.ics.jung.graph.*;
import edu.uci.ics.jung.graph.util.*;

public class FungalGrowth implements CDProtocol {
  private static final String PAR_MYCOCAST_PROTO =
      "network.node.mycocast_proto";
  private static final String PAR_HYPHADATA_PROTO =
      "network.node.hyphadata_proto";
  private static final String PAR_HYPHALINK_PROTO =
      "network.node.hyphalink_proto";
  private static final String PAR_FUNGALGROWTH_PROTO =
      "network.node.fungalgrowth_proto";
  private static final String PAR_START_CYCLE = "start_cycle";
  private static final String PAR_PERIOD = "period";
  private static final String PAR_STRATEGY_MAP = "strategy_map";

  private static int mycoCastPid;
  private static int hyphaDataPid;
  private static int hyphaLinkPid;
  private static int fungalGrowthPid;
  private static int startCycle;
  private static int period;

  private static DynamicsStrategyMap strategyMap;

  private MycoNode myNode = null;

  private static Logger log = Logger.getLogger(FungalGrowth.class.getName());

  public FungalGrowth(String prefix) {
    mycoCastPid = Configuration.getPid(PAR_MYCOCAST_PROTO);
    hyphaDataPid = Configuration.getPid(PAR_HYPHADATA_PROTO);
    hyphaLinkPid = Configuration.getPid(PAR_HYPHALINK_PROTO);
    fungalGrowthPid = Configuration.getPid(PAR_FUNGALGROWTH_PROTO);
    startCycle = Configuration.getInt(prefix + "." + PAR_START_CYCLE);
    period = Configuration.getInt(prefix + "." + PAR_PERIOD);

    if (strategyMap == null) {
      try {
        Class strategyMapClass = Configuration.getClass(prefix + "." +
                                                        PAR_STRATEGY_MAP);
        if (DynamicsStrategyMap.class.isAssignableFrom(strategyMapClass)) {
          strategyMap = (DynamicsStrategyMap) strategyMapClass.newInstance();
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  public Object clone() {
    FungalGrowth ret = null;
    try {
      ret = (FungalGrowth) super.clone();
    } catch (CloneNotSupportedException e) {
      // Never happens
    }
    return ret;
  }

  public void nextCycle(Node node, int pid) {
    if (CDState.getCycle() < startCycle || CDState.getCycle() % period != 0)
        return;

    HyphaData data = (HyphaData) node.getProtocol(hyphaDataPid);
    HyphaLink link = (HyphaLink) node.getProtocol(hyphaLinkPid);

    myNode = (MycoNode) node;

    if (!myNode.isUp()) {
      return;
    }

    // System.out.println(node);
    // FIXME: Make all
    MessageObserver.topoQueryMessages(link.degree());
    MessageObserver.gossipMessages(2);

    // Dead nodes should not execute the protocol
    if (!myNode.isUp()) {
      return;
    }

    // Clean up dead neighbors
    MycoList dead = link.deadNeighbors();
    for (MycoNode neighbor : dead) {
      link.pruneNeighbor(neighbor);
    }

    // Now, perform actions appropriate for current protocol state
    DynamicsStrategy strategy = strategyMap.get(data.getState());

    if (strategy == null) {
      throw new RuntimeException("Strategy not defined in map for " +
                                 data.getState());
    }

    strategy.doDynamics(myNode, data, link);

    myNode.getMycoCast().verify(myNode, data.getState());
  }
}
