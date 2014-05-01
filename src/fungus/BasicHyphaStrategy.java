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


public abstract class BasicHyphaStrategy extends DynamicsStrategy {

  private static Logger log =
      Logger.getLogger(BasicHyphaStrategy.class.getName());

  /* Logic for dynamics common to all hyphal states */
  public void doCommonDynamics(MycoNode node, HyphaData data, HyphaLink link) {
    // Clean up dead neighbors
    MycoCast mycoCast = node.getMycoCast();
    HyphaData d;

    int myCapacity = data.getMaxCapacity();

    MycoNode minHypha = null;
    int minHyphaCapacity = Integer.MAX_VALUE;
    MycoNode maxBiomass = null;
    int maxBiomassCapacity = 0;

    // If disconnected from other hypha, pick a random stable hypha
    // (if possible) and connect to it
    MycoList hyphaeList = link.getHyphae();
    MycoList stableList = link.getStable();

    MycoList biomassList = link.getBiomass();

    if (hyphaeList.isEmpty()
        || (stableList.isEmpty() && !data.isExtending())) {
      log.log(Level.FINER, node + " IS DISCONNECTED FROM OTHER HYPHAE",
              node);

      MycoNode stable = mycoCast.getStableHypha();
      if (stable != null) {
        log.finer("DISCONNECTED " + node + " CONNECTS TO " + stable);
        link.addNeighbor(stable);
      } else {
        log.log(Level.FINER, node
                + " COULDN'T FIND A STABLE NODE TO CONNECT", node);
      }
    }

    // Find largest attached biomass node
    MycoNode myMaxBiomass = link.getMaxBiomass();

    if (myMaxBiomass != null) {
      maxBiomass = myMaxBiomass;
      maxBiomassCapacity = myMaxBiomass.getHyphaData().getMaxCapacity();
    } else {
      maxBiomassCapacity = 0;
    }

    // Find largest biomass attached to a neighbor
    for (MycoNode n : link.get2Neighbors().getBiomass()) {
      d = n.getHyphaData();
      if (d.getMaxCapacity() > maxBiomassCapacity) {
        maxBiomassCapacity = d.getMaxCapacity();
        maxBiomass = n;
      }
    }

    log.log(Level.FINER, "CHECKING FOR STEAL", node);
    // If a neighbor has a biomass node larger than our capacity
    // and larger than our own largest biomass node, steal it!
    if (myMaxBiomass != null
        && maxBiomassCapacity > myMaxBiomass.getHyphaData().getMaxCapacity()
        && maxBiomassCapacity > node.getHyphaData().getMaxCapacity()) {
      MycoNode parent = maxBiomass.getHyphaLink().getParent();
      log.log(Level.FINE, node + " IS STEALING BIOMASS NODE "
              + maxBiomass + " FROM NODE " + parent,
              new Object[] { node, maxBiomass, parent });
      parent.getHyphaLink().transferNeighbor(maxBiomass, node);
      myMaxBiomass = maxBiomass;
    }

    // If we have a biomass larger than us, swap with it
    log.log(Level.FINER, "CHECKING FOR SWAP", node);
    if ((myMaxBiomass != null)
        && (myMaxBiomass.getMaxCapacity() > node.getHyphaData()
            .getMaxCapacity())) {
      log.log(Level.FINER, node + " IS SWAPPING WITH " + myMaxBiomass,
              new Object[] { node, myMaxBiomass });
      link.swapHyphae(myMaxBiomass);
    } else {
      // If we're not attached to a hyphae, go hunting
      log.log(Level.FINER, "HUNTING FOR HYPHA", node);
      if (link.getSameNeighbors().getHyphae().size() == 0) {
        log.log(Level.FINER, node
                + " IS NOT ATTACHED TO A HYPHA", node);
        MycoList candidates = link.get2Neighborhood().getHyphae();
        log.log(Level.FINER, "hyphae in 2-neighborhood are: "
                + candidates, node);
        if (candidates.size() > 0) {
          MycoNode target = candidates.getRandom();
          log.log(Level.FINER, "FOUND " + candidates.size()
                  + "candidates, connecting to " + target);
        }
      }
    }
  }
}
