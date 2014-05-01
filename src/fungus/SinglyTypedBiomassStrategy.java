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

import java.util.logging.Level;
import java.util.logging.Logger;

import peersim.config.Configuration;
import peersim.core.CommonState;

public class SinglyTypedBiomassStrategy extends DynamicsStrategy {

  private static final String PAR_MYCOCAST_PROTO =
      "network.node.mycocast_proto";
  private static final String PAR_JOIN_STRATEGY =
      "config.join_strategy";

  private static Logger log =
      Logger.getLogger(SinglyTypedBiomassStrategy.class.getName());

  protected JoinStrategy joinStrategy;

  public SinglyTypedBiomassStrategy() {
    joinStrategy = JoinStrategy.getStrategy(PAR_JOIN_STRATEGY);
  }


  public void doDynamics(MycoNode node, HyphaData data, HyphaLink link) {
    // If this is free-floating biomass, attach to a foraging hypha
    MycoCast mycoCast = node.getMycoCast();

    mycoCast.ensureBiomass(node);

    MycoNode hyphaNode = null;

    if (link.isDisconnected()) {
      log.log(Level.FINER, node + " IS DISCONNECTED", node);
      hyphaNode = mycoCast.getForagingHypha();
    }
    if (link.isDisconnected()
        || link.getSameNeighbors().getHyphae().isEmpty()) {
      if (hyphaNode == null) {
        // No appropriate foraging hyphae, so spore as extending hypha
        log.log(Level.FINER, node + " CAN'T FIND A FORAGER", node);
        data.becomeExtending(node);
      } else {
        HyphaLink hyphaNodeLink = hyphaNode.getHyphaLink();
        log.log(Level.FINER, node + " CONNECTING TO FORAGING"
                + hyphaNode, new Object[] { node, hyphaNode });
        hyphaNodeLink.addNeighbor(node);
        MessageObserver.topoQueryMessages(2);
        MessageObserver.topoActionMessages(2);
      }
    }

    // This breaks out the logic for HITAP's Bulwark state...in that
    // case, nodes will mirror the AlertHormone concentration and
    // Bulwark/non-Bulwark state of the just-connected hypha
    if (hyphaNode != null) {
      joinStrategy.doJoin(node, hyphaNode);
    }

    // Ensure only one parent
    MycoList sn = link.getSameNeighbors().getHyphae();
    while (sn.size() > 1) {
      MycoNode excess = sn.getRandom();
      log.log(Level.FINE, node + " SEVERING EXCESS PARENT " +
              excess, new Object [] { node, excess});
      link.removeNeighbor(sn.getRandom());
      sn = link.getSameNeighbors().getHyphae();
    }
  }
}
