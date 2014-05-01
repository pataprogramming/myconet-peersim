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

import java.lang.Math;
import java.util.logging.Level;
import java.util.logging.Logger;

import peersim.config.Configuration;
import peersim.core.CommonState;

public class BasicExtendingStrategy extends BasicHyphaStrategy {

  private static Logger log =
      Logger.getLogger(BasicExtendingStrategy.class.getName());

  public void doDynamics(MycoNode node, HyphaData data, HyphaLink link) {
    // Execute logic common to all hyphal states`
    doCommonDynamics(node, data, link);

    // System.out.println("Do Extending");
    MycoList hyphae = link.getHyphae();

    // Randomly permute order so selections will be random
    java.util.Collections.shuffle(hyphae);
    MycoList extending = hyphae.getExtending();
    MycoList branching = hyphae.getBranching();
    MycoList immobile = hyphae.getImmobile();

    int branchingCount = branching.size();
    int immobileCount = immobile.size();

    MycoNode extendingNode = null; // A random extending node
    MycoNode branchingNode = null; // A random branching node
    MycoNode secondBranchingNode = null; // Another random branching node
    MycoNode immobileNode = null; // A random immobile node

    // Get random nodes
    if (extending.size() > 0) {
      extendingNode = extending.get(0);
    }
    if (branching.size() > 0) {
      branchingNode = branching.get(0);
    }
    if (branching.size() > 1) {
      secondBranchingNode = branching.get(1);
    }
    if (immobile.size() > 0) {
      immobileNode = immobile.get(0);
    }

    // If two extending nodes are neighbors, absorb the neighbor
    if (extendingNode != null) {
      //link.absorbHypha(extendingNode);
      for (MycoNode n : extending) {
        if (n.getHyphaData().getCapacity() <= data.getCapacity()) {
          link.absorbHypha(n);
        }
      }
    }

    if (branchingNode != null && secondBranchingNode != null) {
      log.log(Level.FINER, node + " IS CONNECTED TO " + branchingCount
              + " BRANCHING NODES", node);
      // Drop connection to extra branching node
      // //link.removeNeighbor(secondBranchingNode);
    }

    if (immobileNode != null && branchingNode != null) {
      log.log(Level.FINER, node + " IS CONNECTED TO " + branchingCount
              + " BRANCHING NODES and " + immobileCount
              + " IMMOBILE NODES", node);
      // Drop connection to extra branching node
      link.removeNeighbor(immobileNode);
    }

    // If not attached to at least parent_target stable hypha, grow a link
    if (link.getStable().size() < Math.floor(data.getParentTarget())) {
      MycoCast myco = node.getMycoCast();
      MycoNode o = myco.getStableHypha();
      if (o != null) {
        log.log(Level.FINE,
                node
                + " not yet connected to a stable hypha, growing a link to "
                + o, new Object[] { node, o });
        link.addNeighbor(o);
      }
    }


    // If over biomass capacity, become a branching node, choose a
    // neighbor as a new extending node, and pass excess
    // biomass to it
    if (link.isOverBiomass()) {
      data.becomeBranching(node);
      MycoNode newHypha = link.growHypha();
      if (newHypha != null) {
        int myCapacity = data.getMaxCapacity();
        int biomassDegree = link.sameBiomassDegree();

        link.transferBiomass(newHypha, biomassDegree - myCapacity);
      }
    } else {
      // If over hyphal connections target, drop the excess
      MycoList hyp = link.getHyphae();
      while (hyp.size() > Math.floor(data.getParentTarget())) {
        MycoNode candidate = hyp.getRandom();
        log.log(Level.FINE, node + " has more than one hypha, randomly " +
                " dropping connection to " + candidate,
                new Object[] { node, candidate });
        link.removeNeighbor(candidate);
        hyp = link.getHyphae();
      }
    }
  }
}
