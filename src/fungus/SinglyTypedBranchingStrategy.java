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

public class SinglyTypedBranchingStrategy extends SinglyTypedHyphaStrategy {

  private static Logger log =
      Logger.getLogger(SinglyTypedBranchingStrategy.class.getName());

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

    int extendingCount = sameExtending.size();
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

    // If no extending nodes, grow an extending node
    if (extendingCount == 0) {
      extendingNode = link.growHypha();
    }

    // If under biomass capacity:
    // First, absorb neighbors if we're larger and our capacity allows
    if (link.isUnderBiomass()) {
      log.log(Level.FINER, "UNDER BIOMASS CAPACITY; absorbing neighbors",
              node);
      for (MycoNode neighbor : sameHyphae) {
        if ((data.getMaxCapacity() > neighbor.getHyphaData()
             .getMaxCapacity())
            && (neighbor.getHyphaLink().sameDegree() <= data
                .getMaxCapacity() - link.sameDegree())) {
          link.absorbHypha(neighbor);
        }
      }
    }
    // If under biomass capacity absorb biomass from neighbor extending
    // nodes
    MycoList extendingNeighbors = link.getSameNeighbors().getExtending();
    for (MycoNode neighbor : extendingNeighbors) {
      if (!link.isUnderBiomass()) {
        break;
      }
      log.log(Level.FINER, "UNDER BIOMASS CAPACITY", node);
      neighbor.getHyphaLink().transferBiomass(node,
                                              link.amountUnderBiomass());
    }

    // If over biomass capacity:
    // Push biomass to an extending node
    if (link.isOverBiomass() && extendingNode != null) {
      link.transferBiomass(extendingNode, link.amountOverBiomass());
    }

    // If two connected extending nodes, give one to the other so
    // they will collapse
    if (extendingNode != null && secondExtendingNode != null) {
      link.transferNeighbor(secondExtendingNode, extendingNode);
      secondExtendingNode = null;
    }

    // If under hyphal same capacity:
    // Connect to a random hyphae in 2-neighborhood
    if (link.isUnderSameHyphae()) {
      MycoNode candidate = link.get2Neighbors().getType(data.getType())
          .getHyphae().getRandom();
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

    // If under hyphal different capacity:
    // Connect to hypha of different type in 2-neighborhood
    if (link.isUnderDifferentHyphae()) {
      MycoCast myco = node.getMycoCast();

      MycoNode o = myco.getStableHypha();
      if (o != null) {
        log.log(Level.FINE,
                node
                + " is under target for different hyphae, connecting to random node "
                + o, new Object[] { node, o });
        link.addNeighbor(o);
      }
    }

    // If over hyphal same and different hyphal capacity:
    // Become an immobile node
    // Pass excess hyphae to an extending node
    if (link.isAtOrOverSameHyphae() && link.isAtOrOverDifferentHyphae()) {
      data.becomeImmobile(node);
      if (secondExtendingNode != null) {
        if (extendingNode != null) {
          link.transferNeighbor(extendingNode, secondExtendingNode);
        } else if (immobileNode != null) {
          link.transferNeighbor(immobileNode, extendingNode);
        }
      }
    }

    // If over different-type hypha target, drop random links
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

    if (link.isUnderBiomass()) {
      log.log(Level.FINE, node
              + " couldn't maintain biomass, dropping to EXTENDING state");
      data.becomeExtending(node);
    }
  }
}
