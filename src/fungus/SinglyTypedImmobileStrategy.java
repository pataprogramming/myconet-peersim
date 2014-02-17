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

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import peersim.config.Configuration;
import peersim.core.CommonState;

public class SinglyTypedImmobileStrategy extends SinglyTypedHyphaStrategy {

  private static final String PAR_SPAWN_PROB = "config.immobile.spawn_prob";

  private static double spawnProb;

  private static Logger log =
      Logger.getLogger(SinglyTypedImmobileStrategy.class.getName());

  public SinglyTypedImmobileStrategy() {
    spawnProb = Configuration.getDouble(PAR_SPAWN_PROB);
  }

  public void doDynamics(MycoNode node, HyphaData data, HyphaLink link) {
    // Execute logic common to all hyphal states
    doCommonDynamics(node, data, link);

        MycoList hyphae = link.getHyphae();

    // Randomly permute order so selections will be random
    java.util.Collections.shuffle(hyphae);
    MycoList sameHyphae = hyphae.getType(data.getType());
    MycoList differentHyphae = hyphae.getTypeNot(data.getType());
    MycoList sameExtending = sameHyphae.getExtending();
    MycoList sameBranching = sameHyphae.getBranching();
    MycoList sameImmobile = sameHyphae.getImmobile();

    int branchingCount = sameBranching.size();
    int immobileCount = sameImmobile.size();
    int differentCount = differentHyphae.size();

    MycoNode extendingNode = null; // A random extending node (same type)
    MycoNode secondExtendingNode = null; // Another random extending node
    // (same type)
    MycoNode branchingNode = null; // A random branching node (same type)
    MycoNode secondBranchingNode = null; // Another random branching node
    // (same type)
    MycoNode immobileNode = null; // A random immobile node (same type)

    // Get random nodes (all of same type
    if (sameExtending.size() > 0) {
      extendingNode = sameExtending.get(0);
    }
    if (sameExtending.size() > 1) {
      secondExtendingNode = sameExtending.get(1);
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

    // If two connected extending nodes, give one to the other so
    // they will collapse
    if (extendingNode != null && secondExtendingNode != null) {
      link.transferNeighbor(secondExtendingNode, extendingNode);
    }

    // // If two connected branching nodes, give one to the other so
    // // they will collapse
    // //if (branchingNode != null && secondBranchingNode != null) {
    // // link.transferNeighbor(secondBranchingNode, branchingNode);
    // //}

    // If under hyphal same capacity:
    // Connect to a random stable hyphae in 2-neighborhood
    if (link.isUnderSameHyphae()) {
      MycoNode candidate = link.get2Neighbors().getType(data.getType())
          .getStable().getRandom();
      if (candidate != null) {
        log.log(Level.FINE,
                node
                + " is under target for same-type hyphae, connecting to "
                + candidate + " in 2-neighborhood",
                new Object[] { node, candidate });
        link.addNeighbor(candidate);
      } else {
        log.log(Level.FINE,
                node
                + " is under target for same-type hyphae, but no candidates in 2-neighborhood",
                node);
      }
    }

    // If under ideal number of different-type hyphae, connect to a random
    // hypha
    if (link.isUnderDifferentHyphae()) {
      MycoCast myco = node.getMycoCast();
      MycoNode o = myco.getStableHypha();
      if (o != null) {
        link.addNeighbor(o);
      }
    }

    // Shake things up by sometimes randomly connecting to a new hypha
    if (CommonState.r.nextDouble() < spawnProb) {
      MycoCast myco = node.getMycoCast();
      MycoNode stable = myco.getStableHypha();
      if (stable != null) {
        log.log(Level.FINER, "FULLY CONNECTED " + node
                + " SPAWNS A HYPHA TO " + stable, new Object[] { node,
                                                                 stable });
        link.addNeighbor(stable);
      } else {
        log.log(Level.FINER, node
                + " COULDN'T FIND A STABLE NODE FOR SPAWNING", node);
      }
    }

    // If under biomass capacity:
    // First, absorb neighbors if our capacity allows
    if (link.isUnderBiomass()) {
      for (MycoNode neighbor : link.getSameNeighbors().getHyphae()) {
        if ((data.getMaxCapacity() > neighbor.getHyphaData()
             .getMaxCapacity())
            && (neighbor.getHyphaLink().sameBiomassDegree() <= link
                .amountUnderBiomass())) {
          log.log(Level.FINE, node + " ABSORBS NEIGHBOR " + neighbor,
                  new Object[] { node, neighbor });
          link.absorbHypha(neighbor);
        }
      }
    }
    // If under biomass capacity absorb biomass from neighbor extending
    // or branching nodes
    if (link.isUnderBiomass()) {
      MycoList extendingNeighbors = link.getSameNeighbors()
          .getExtending();
      for (MycoNode neighbor : extendingNeighbors) {
        if (!link.isUnderBiomass()) {
          break;
        }
        log.log(Level.FINE,
                node + " TRYING TO ABSORB " + link.amountUnderBiomass()
                + " FROM " + neighbor, new Object[] { node,
                                                      neighbor });

        neighbor.getHyphaLink().transferBiomass(node,
                                                link.amountUnderBiomass());
      }
      MycoList branchingNeighbors = link.getSameNeighbors().getBranching();
      for (MycoNode neighbor : branchingNeighbors) {
        if (!link.isUnderBiomass()) {
          break;
        }
        neighbor.getHyphaLink().transferBiomass(node,
                                                link.amountUnderBiomass());
      }
    }

    // If we can't scare up enough biomass, drop down to lower status
    if ((((float) link.sameBiomassDegree()) / (float) data
         .getIdealBiomass()) < 0.80) {
      // System.out.println("WHOOPS: Couldn't cut it!");
      data.becomeBranching(node);

    }

    // If over biomass capacity:
    // If possible, push biomass to a connected node
    if (link.isOverBiomass()) {
      log.log(Level.FINER, node + " IS " + link.amountOverBiomass()
              + " OVER BIOMASS CAPACITY!", node);
      if (extendingNode != null) {
        link.transferBiomass(extendingNode, link.amountOverBiomass());
      } else if (branchingNode != null) {
        link.transferBiomass(branchingNode, link.amountOverBiomass());
      } else if (immobileNode != null) {
        link.transferBiomass(immobileNode, link.amountOverBiomass());
      }
    }

    // If over same-type hyphal capacity, drop a random hypha of same type
    // //if (link.isOverHyphae()) {
    if (link.isOverSameHyphae()
        && (link.sameHyphaDegree() > data.getIdealHyphae())) {
      log.log(Level.FINER, node + " IS " + link.amountOverBiomass()
              + " OVER SAME-TYPE HYPHAL CAPACITY", node);
      MycoNode candidate = link.getSameNeighbors().getStable()
          .getRandom();

      if (candidate != null) {
        log.log(Level.FINER, node + " SEVERING HYPHA TO " + candidate,
                new Object[] { node, candidate });
        link.removeNeighbor(candidate);
      } else {
        log.log(Level.FINE,
                "COULD NOT FIND SUITABLE SAME-TYPE HYPHA FOR SEVERING",
                node);
      }
    }

    if (link.isOverDifferentHyphae()
        && (link.differentHyphaDegree() > data.getIdealOtherHyphae())) {
      log.log(Level.FINER, node + " IS " + link.amountOverBiomass()
              + " OVER DIFFERENT-TYPE HYPHAL CAPACITY", node);
      MycoNode candidate = link.getDifferentNeighbors().getStable()
          .getRandom();
      if (candidate != null) {
        log.log(Level.FINER, node + " SEVERING HYPHA TO " + candidate,
                new Object[] { node, candidate });
        link.removeNeighbor(candidate);
      } else {
        log.log(Level.FINE,
                "COULD NOT FIND SUITABLE DIFFERENT-TYPE HYPHA FOR SEVERING",
                node);
      }
    }
  }
}
