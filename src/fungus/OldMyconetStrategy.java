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

public class OldMyconetStrategy extends DynamicsStrategy {

  private static final String PAR_MYCOCAST_PROTO =
      "network.node.mycocast_proto";
  private static final String PAR_SPAWN_PROB =
      "config.immobile.spawn_prob";

  private static Logger log =
      Logger.getLogger(OldMyconetStrategy.class.getName());

  private static double spawnProb;

  //protected JoinStrategy joinStrategy;

  public OldMyconetStrategy() {
    //joinStrategy = JoinStrategy.getStrategy(PAR_JOIN_STRATEGY);
    spawnProb = Configuration.getDouble(PAR_SPAWN_PROB);
  }

  public void doBiomass(MycoNode node, HyphaData data, HyphaLink link) {
    // If this is free-floating biomass, attach to a foraging hypha
    MycoCast mycoCast = node.getMycoCast();

    mycoCast.ensureBiomass(node);

    if (link.isDisconnected() || link.getHyphae().isEmpty()) {
      log.finer(node + " IS DISCONNECTED");
      MycoNode hyphaNode = mycoCast.getForagingHypha();
      if (hyphaNode == null) {
        // No foraging hyphae, so spore an extending hypha
        //System.out.println(node + " becomes extending");
        log.finer(node + " CAN'T FIND A FORAGER");
        data.becomeExtending(node);
      } else {
        HyphaLink hyphaNodeLink = hyphaNode.getHyphaLink();
        log.finer(node +
                  " CONNECTING TO FORAGING"
                  + hyphaNode);
        hyphaNodeLink.addNeighbor(node);
      }
    }
  }

  public void doHypha(MycoNode node, HyphaData data, HyphaLink link) {
    // Clean up dead neighbors
    MycoCast mycoCast = node.getMycoCast();
    HyphaData d;

    /*
      List<MycoNode> dead = link.deadNeighbors();
      for (MycoNode n : dead) {
      d = (HyphaData) n.getProtocol(hyphaDataPid);
      if (d.isBiomass()) {
      // Simply clean dead biomass
      mycoCast.kill(n);
      link.pruneNeighbor(n);
      } else if (d.isExtending()) {
      // If node has lost an extending node, grow a new one
      link.growHypha();
      } else {
      // If node has lost a stable neighbor, pick a new one
      MycoNode stable = mycoCast.getStableHypha();
      if (stable != null) {
      link.addNeighbor(stable);
      }
      }
      }*/

    //System.out.println("Foom");

    MycoList biomassList = link.getBiomass();

    int myCapacity = data.getMaxCapacity();

    MycoNode minHypha = null;
    int minHyphaCapacity = Integer.MAX_VALUE;
    MycoNode maxBiomass = null;
    int maxBiomassCapacity = 0;


    // If disconnected from other hypha, pick a random stable hypha
    //   (if possible) and connect to it
    MycoList hyphaeList = link.getHyphae();
    MycoList stableList = link.getStable();
    if (hyphaeList.isEmpty() || (stableList.isEmpty() && !data.isExtending())) {
      log.finer(node + " IS DISCONNECTED FROM OTHER HYPHAE");
      //System.out.println("hyphaeList is empty");

      MycoNode stable = mycoCast.getStableHypha();
      if (stable != null) {
        log.finer("DISCONNECTED " + node +
                  " CONNECTS TO " + stable);
        link.addNeighbor(stable);
      } else {
        log.finer(node + " COULDN'T FIND A STABLE NODE TO CONNECT");
      }
    }

    for (MycoNode n : biomassList) {
      d = n.getHyphaData();
      if (d.getMaxCapacity() >  maxBiomassCapacity) {
        maxBiomassCapacity = d.getMaxCapacity();
        maxBiomass = n;
      }
    }

    // If a neighbor has a biomass node larger than our capacity
    // and larger than our own largest biomass node, steal it!
    MycoList exclude = new MycoList();
    exclude.add(node);
    MycoList maxNeighborBiomass = link.getMaxNeighborBiomass(exclude);

    MycoNode myMaxBiomass = link.getMaxBiomass();
    if ((maxNeighborBiomass.size() != 0)
        &&
        (myCapacity
         < maxNeighborBiomass.get(0).getHyphaData().getMaxCapacity())
        &&
        ((myMaxBiomass != null)
         &&
         (myMaxBiomass.getHyphaData().getMaxCapacity()
          < maxNeighborBiomass.get(0).getHyphaData().getMaxCapacity())
        ))
    {
      log.fine(node + " IS STEALING BIOMASS NODE " +
               maxNeighborBiomass.get(0) + " FROM NODE " +
               maxNeighborBiomass.get(1));
      maxNeighborBiomass.get(1).getHyphaLink().transferNeighbor(maxNeighborBiomass.get(0), node);
      myMaxBiomass = maxNeighborBiomass.get(0);
      //link.swapHyphae(maxBiomass);
    }
    if ((myMaxBiomass != null)
        &&
        (myMaxBiomass.getMaxCapacity()
         > node.getHyphaData().getMaxCapacity())
        )
    {
      log.finer(node + " IS SWAPPING WITH " + myMaxBiomass);
      link.swapHyphae(myMaxBiomass);
    }

    /*{
    // If largest capacity biomass node is larger than smallest-capacity
    //      neighboring hypha then pass biomass node to neighbor
    hyphaeList = link.getHyphae();

    for (MycoNode n : hyphaeList) {
    d = n.getHyphaData();
    if (d.getMaxCapacity() <  minHyphaCapacity) {                    minHyphaCapacity = d.getMaxCapacity();
    minHypha = n;
    }
    }
    if (minHyphaCapacity < maxBiomassCapacity) {
    if (maxBiomass == null) { log.warning("BAD maxBiomass"); }
    if (minHypha == null) { log.warning("BAD minHypha"); }
    link.transferNeighbor(maxBiomass, minHypha);
    }
    }*/
    if (data.isExtending()) {
      doExtending(node,data,link);
    } else if (data.isBranching()) {
      doBranching(node, data, link);
    } else if (data.isImmobile()) {
      doImmobile(node, data, link);
    }
  }

  public void doExtending(MycoNode node, HyphaData data, HyphaLink link) {
    //System.out.println("Do Extending");
    MycoList hyphae = link.getHyphae();
    java.util.Collections.shuffle(hyphae);

    int branchingCount = 0;
    int immobileCount = 0;
    MycoNode branchingNode = null;  // A random branching node
    MycoNode secondBranchingNode = null;  // Another random branching node
    MycoNode immobileNode = null;   // A random immobile node
    HyphaData d;

    // If two extending nodes are neighbors, absorb the neighbor
    if (!hyphae.isEmpty()) {
      for (MycoNode n : hyphae) {
        d = n.getHyphaData();
        if (d.isExtending()) {
          //data.becomeBranching();
          link.absorbHypha(n);
        } else if (d.isBranching()) {
          branchingCount++;
          secondBranchingNode = branchingNode;
          branchingNode = n;
        } else {
          immobileCount++;
          immobileNode = n;
        }
      }
    }

    if (branchingNode != null && secondBranchingNode != null) {
      log.finer(node + " IS CONNECTED TO " +
                branchingCount + " BRANCHING NODES");
      // Drop connection to extra branching node
      ////link.removeNeighbor(secondBranchingNode);
    }

    if (immobileNode != null && branchingNode != null) {
      log.finer(node + " IS CONNECTED TO " +
                branchingCount + " BRANCHING NODES and " +
                immobileCount + " IMMOBILE NODES");
      // Drop connection to extra branching node
      link.removeNeighbor(immobileNode);
    }

    for (MycoNode n : hyphae) {
      d = (HyphaData) n.getHyphaData();
      if (d.isExtending()) {
      }
    }


    // If over biomass capacity, become a branching node, choose a
    //   neighbor as a new extending node, and pass excess biomass
    //   to it
    if (link.isOverBiomass()) {
      data.becomeBranching(node);
      MycoNode newHypha = link.growHypha();
      if (newHypha != null) {
        int myCapacity = data.getMaxCapacity();
        int biomassDegree = link.biomassDegree();

        link.transferBiomass(newHypha, biomassDegree - myCapacity);
      }
    }
  }

  public void doBranching(MycoNode node, HyphaData data, HyphaLink link) {
    MycoList hyphae = link.getHyphae();
    java.util.Collections.shuffle(hyphae);
    int extendingCount = 0;
    int immobileCount = 0;

    HyphaData d;
    MycoNode extendingNode = null;  // A random extending node
    MycoNode secondExtendingNode = null;  // Another random extending node
    MycoNode immobileNode = null;   // A random immobile node
    for (MycoNode n : hyphae) {
      d = n.getHyphaData();
      if (d.isExtending()) {
        extendingCount++;
        secondExtendingNode = extendingNode;
        extendingNode = n;
      } else if (d.isBranching()) {
        //    Merge adjacent branching nodes
        //link.absorbHypha(n);
      } else {
        immobileCount++;
        immobileNode = n;
      }
    }

    //    If no extending nodes, grow an extending node
    if (extendingCount == 0) {
      extendingNode = link.growHypha();
    }

    // If under biomass capacity:
    // First, absorb neighbors if we're larger and our capacity allows
    if (link.isUnderBiomass()) {
      for (MycoNode neighbor : link.getHyphae()) {
        if ((data.getMaxCapacity() > neighbor.getHyphaData().getMaxCapacity())
            &&
            (neighbor.getHyphaLink().degree() <=
             data.getMaxCapacity() - link.degree())
            ) {
          link.absorbHypha(neighbor);
        }
      }
    }
    // If under biomass capacity absorb biomass from neighbor extending
    // nodes
    MycoList extendingNeighbors = link.getExtending();
    for (MycoNode neighbor : extendingNeighbors) {
      if (! link.isUnderBiomass()) { break; }
      neighbor.getHyphaLink().transferBiomass(node, link.amountUnderBiomass());
    }

    // If over biomass capacity:
    //    Push biomass to an extending node
    if (link.isOverBiomass() && extendingNode != null) {
      link.transferBiomass(extendingNode, link.amountOverBiomass());
    }

    // If two connected extending nodes, give one to the other so
    //   they will collapse
    if (extendingNode != null && secondExtendingNode != null) {
      link.transferNeighbor(secondExtendingNode, extendingNode);
      secondExtendingNode = null;
    }

    // If under hyphal capacity:
    //  Connect to a random stable node
    if (link.isUnderHyphae()) {
      MycoCast myco = node.getMycoCast();

      MycoNode o = myco.getStableHypha();
      if (o != null) {
        link.addNeighbor(o);
      }
    }

    // If over hyphal capacity:
    //    Become an immobile node
    //    Pass excess hyphae to an extending node
    if (link.isOverHyphae()) {
      data.becomeImmobile(node);
      if (secondExtendingNode != null) {
        if (extendingNode != null) {
          //System.out.println("Ping.");
          link.transferNeighbor(extendingNode, secondExtendingNode);
        } else if (immobileNode != null) {
          //System.out.println("Pang.");
          link.transferNeighbor(immobileNode, extendingNode);
        }
      }
    }

    if (link.isUnderBiomass()) {
      //System.out.println("WHOOPS: Couldn't cut it!");
      data.becomeExtending(node);
    }
  }

  public void doImmobile(MycoNode node, HyphaData data, HyphaLink link) {
    // log.fine("IMMOBILE NODE CAPACITY " + myNode);
    MycoList hyphae = link.getHyphae();
    java.util.Collections.shuffle(hyphae);
    int extendingCount = 0;
    int branchingCount = 0;
    int immobileCount = 0;

    HyphaData d;
    MycoNode extendingNode = null;  // A random extending node
    MycoNode secondExtendingNode = null; // A random second extending node
    MycoNode branchingNode = null;  // A random branching node
    MycoNode secondBranchingNode = null; // A random second branching node
    MycoNode immobileNode = null;   // A random immobile node
    for (MycoNode n : hyphae) {
      d = n.getHyphaData();
      if (d.isExtending()) {
        extendingCount++;
        secondExtendingNode = extendingNode;
        extendingNode = n;
      } else if (d.isBranching()) {
        branchingCount++;
        branchingNode = n;
        secondBranchingNode = branchingNode;
      } else {
        immobileCount++;
        immobileNode = n;
      }
    }

    // If two connected extending nodes, give one to the other so
    //   they will collapse
    if (extendingNode != null && secondExtendingNode != null) {
      link.transferNeighbor(secondExtendingNode, extendingNode);
    }

    //// If two connected branching nodes, give one to the other so
    ////   they will collapse
    ////if (branchingNode != null && secondBranchingNode != null) {
    ////    link.transferNeighbor(secondBranchingNode, branchingNode);
    ////}

    // If under ideal number of hyphae, connect to a random hypha
    if (link.isUnderHyphae()) {
      MycoCast myco = node.getMycoCast();
      MycoNode o = myco.getStableHypha();
      if (o != null) {
        link.addNeighbor(o);
      }
    }

    // If under hyphal capacity, try to connect to another stable hypha
    if (link.isUnderHyphae()) {
      log.finer(node + " IS " + link.amountOverBiomass() +
                " UNDER HYPHAL CAPACITY");

      MycoNode o = node.getMycoCast().getStableHypha();
      if (o != null) {
        log.finer(node + " GROWING HYPHA INTO " + o);
        link.addNeighbor(o);
      }
    }

    // Shake things up by sometimes randomly connecting to a new hypha
    if (CommonState.r.nextDouble() < spawnProb) {
      MycoCast myco = node.getMycoCast();
      MycoNode stable = myco.getStableHypha();
      if (stable != null) {
        log.finer("FULLY CONNECTED " + node +
                  " SPAWNS A HYPHA TO " + stable);
        link.addNeighbor(stable);
      } else {
        log.finer(node + " COULDN'T FIND A STABLE NODE FOR SPAWNING");
      }
    }

    // If under biomass capacity:
    // First, absorb neighbors if our capacity allows
    if (link.isUnderBiomass()) {
      for (MycoNode neighbor : link.getHyphae()) {
        if ((data.getMaxCapacity()
             > neighbor.getHyphaData().getMaxCapacity())
            &&
            (neighbor.getHyphaLink().biomassDegree() <=
             link.amountUnderBiomass())
            ) {
          log.fine(node + " ABSORBS NEIGHBOR " +
                   neighbor);
          link.absorbHypha(neighbor);
        }
      }
    }
    // If under biomass capacity absorb biomass from neighbor extending
    // or branching nodes
    if (link.isUnderBiomass()) {
      MycoList extendingNeighbors = link.getExtending();
      for (MycoNode neighbor : extendingNeighbors) {
        if (! link.isUnderBiomass()) { break; }
        log.fine(node + " TRYING TO ABSORB " +
                 link.amountUnderBiomass() + " FROM " +
                 neighbor);

        neighbor.getHyphaLink().transferBiomass(node, link.amountUnderBiomass());
      }
      MycoList branchingNeighbors = link.getBranching();
      for (MycoNode neighbor : branchingNeighbors) {
        if (! link.isUnderBiomass()) { break; }
        neighbor.getHyphaLink().transferBiomass(node, link.amountUnderBiomass());
      }
    }

    // If we can't scare up enough biomass, drop down to lower status
    if ((((float) link.biomassDegree()) / (float)data.getIdealBiomass()) < 0.80) {
      //System.out.println("WHOOPS: Couldn't cut it!");
      data.becomeBranching(node);

      /*HyphaLink l;
        if (extendingNode != null) {
        l = extendingNode.getHyphaLink();
        l.transferBiomass(node, link.amountUnderBiomass());
        } else if (branchingNode != null) {
        l = branchingNode.getHyphaLink();
        //System.out.println("Pung");
        l.transferBiomass(node, link.amountUnderBiomass());
        } else {
        data.becomeExtending(node);
        }*/
    }


    // If over biomass capacity:
    //   If possible, push biomass to a connected node
    if (link.isOverBiomass()) {
      log.finer(node + " IS " + link.amountOverBiomass() +
                " OVER BIOMASS CAPACITY!");
      if (extendingNode != null) {
        link.transferBiomass(extendingNode, link.amountOverBiomass());
      } else if (branchingNode != null) {
        link.transferBiomass(branchingNode, link.amountOverBiomass());
      } else if (immobileNode != null) {
        link.transferBiomass(immobileNode, link.amountOverBiomass());
      }
    }


    // If over hyphal capacity, drop a random hypha
    ////if (link.isOverHyphae()) {
    while (link.isOverHyphae() &&
           (link.hyphaDegree() > link.idealHyphae())) {
      log.finer(node + " IS " + link.amountOverBiomass() +
                " OVER HYPHAL CAPACITY");
      MycoList candidates = link.getStable();
      if (candidates.size() == 0) {
        candidates = link.getHyphae();
      }

      int nnum = CommonState.r.nextInt(candidates.size());
      MycoNode o = candidates.get(nnum);
      log.finer(node + " SEVERING HYPHA TO " + o);
      link.removeNeighbor(o);
    }

    /*
    // If under hyphal capacity, become a branching hypha
    if (link.isUnderHyphae()) {
    data.becomeBranching(node);
    } else if (link.isOverHyphae()) {
    // If over hyphal capacity // FIXME! Careless logic
    // Transfer a hypha to another hypha
    if (extendingNode != null) {
    if (branchingNode != null) {
    //System.out.println("Pung.");
    link.transferNeighbor(extendingNode, branchingNode);
    } else if (immobileNode != null) {
    //System.out.println("Pong.");
    link.transferNeighbor(immobileNode, extendingNode);
    } else {
    // FIXME: Drop excess immobile?
    }
    } else {
    if (branchingNode != null) {
    if (immobileNode != null) {
    //System.out.println("Poong.");
    link.transferNeighbor(immobileNode, branchingNode);
    }
    } else {
    // FIXME: Drop excess immobile?
    }
    }
    }*/
    //log.fine("IMMOBILE NODE CAPACITY " + myNode);
  }

  public void doDynamics(MycoNode node, HyphaData data, HyphaLink link) {

    if (!node.isUp()) {
      return;
    }

    //System.out.println(node);

    MycoCast mycoCast = node.getMycoCast();

    // Dead nodes should not execute the protocol
    if (!node.isUp()) {
      return;
    }

    // Clean up dead neighbors
    MycoList dead = link.deadNeighbors();
    for (MycoNode neighbor : dead) {
      link.pruneNeighbor(neighbor);
    }

    // Now, perform actions appropriate for different protocol states
    if (data.isBiomass()) {
      doBiomass(node,data,link);
    } else {
      doHypha(node,data,link);
    }
  }
}
