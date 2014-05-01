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

public class SinglyTypedExtendingStrategy extends SinglyTypedHyphaStrategy {

  private static Logger log =
      Logger.getLogger(SinglyTypedExtendingStrategy.class.getName());

  public void doDynamics(MycoNode node, HyphaData data, HyphaLink link) {
    // Execute logic common to all hyphal states`
    doCommonDynamics(node, data, link);

    // System.out.println("Do Extending");
    MycoList hyphae = link.getHyphae();

    // Randomly permute order so selections will be random
    java.util.Collections.shuffle(hyphae);
    MycoList sameHyphae = hyphae.getType(data.getType());
    MycoList sameExtending = sameHyphae.getExtending();
    MycoList sameBranching = sameHyphae.getBranching();
    MycoList sameImmobile = sameHyphae.getImmobile();

    int branchingCount = sameBranching.size();
    int immobileCount = sameImmobile.size();

    MycoNode extendingNode = null; // A random extending node (same type)
    MycoNode branchingNode = null; // A random branching node (same type)
    MycoNode secondBranchingNode = null; // Another random branching node
    // (same type)
    MycoNode immobileNode = null; // A random immobile node (same type)

    // Get random nodes (all of same type
    if (sameExtending.size() > 0) {
      extendingNode = sameExtending.get(0);
    }
    if (sameBranching.size() > 0) {
      branchingNode = sameBranching.get(0);
    }
    if (sameBranching.size() > 1) {
      secondBranchingNode = sameBranching.get(1);
    }
    if (sameImmobile.size() > 0) {
      immobileNode = sameImmobile.get(0);
    }

    // If two same-type extending nodes are neighbors, absorb the neighbor
    if (extendingNode != null) {
      link.absorbHypha(extendingNode);
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

    // If not attached to at least one stable of same type, grow a link
    if (link.getSameNeighbors().getStable().size() < 1) {
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

    // If not at C_O, grow a link
    if (link.getDifferentNeighbors().getHyphae().size() < 1) {
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

    // If over different-type hypha target, drop a random link
    MycoList different = link.getDifferentNeighbors().getHyphae();
    if (different.size() > data.getIdealOtherHyphae()) {
      MycoNode candidate = different.getRandom();
      log.log(Level.FINE,
              node
              + " is over target for different hyphae, randomly dropping connection to "
              + candidate, new Object[] { node, candidate });
      link.removeNeighbor(candidate);
      different = link.getDifferentNeighbors().getHyphae();
    }

    // If over biomass capacity, become a branching node, choose a
    // same-type neighbor as a new extending node, and pass excess
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
      // If over same-type hyphal connections, drop the excess
      MycoList same = link.getHyphae().getType(data.getType());
      while (same.size() > 1) {
        MycoNode candidate = same.getRandom();
        log.log(Level.FINE,
                node
                + " has more than one same-type hypha, randomly dropping connection to "
                + candidate, new Object[] { node, candidate });
        link.removeNeighbor(candidate);
        same = link.getHyphae().getType(data.getType());
      }
    }
  }
}
