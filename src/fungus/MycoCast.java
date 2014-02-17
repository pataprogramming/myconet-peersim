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

import java.util.*;
import java.util.logging.*;
import peersim.cdsim.*;
import peersim.core.*;
import peersim.config.*;

// Simulated source of random (via gossip) nodes for self-healing

public class MycoCast implements CDProtocol, Cleanable {
  private static final String PAR_HYPHADATA_PROTO =
      "network.node.hyphadata_proto";
  private static final String PAR_HYPHALINK_PROTO =
      "network.node.hyphalink_proto";

  private static int hyphadataPid;
  private static int hyphalinkPid;

  private static Logger log = Logger.getLogger(MycoCast.class.getName());

  protected static Set<MycoNode> biomassNodes;
  protected static Set<MycoNode> extendingNodes;
  protected static Set<MycoNode> branchingNodes;
  protected static Set<MycoNode> immobileNodes;
  protected static Set<MycoNode> bulwarkNodes;
  protected static Random generator;

  protected static HashMap<HyphaType,Set<MycoNode>> typeMap;

  protected MycoNode myNode;


  public MycoCast(String prefix) {
    hyphadataPid = Configuration.getPid(PAR_HYPHADATA_PROTO);
    hyphalinkPid = Configuration.getPid(PAR_HYPHALINK_PROTO);
    if (generator == null) {
      MycoCast.generator = CommonState.r;
    }
    if (biomassNodes == null) {
      MycoCast.biomassNodes = new HashSet<MycoNode>();
    }
    if (bulwarkNodes == null) {
      MycoCast.bulwarkNodes = new HashSet<MycoNode>();
    }
    if (extendingNodes == null) {
      MycoCast.extendingNodes = new HashSet<MycoNode>();
    }
    if (branchingNodes == null) {
      MycoCast.branchingNodes = new HashSet<MycoNode>();
    }
    if (immobileNodes == null) {
      MycoCast.immobileNodes = new HashSet<MycoNode>();
    }
    if (typeMap == null) {
      MycoCast.typeMap = new HashMap<HyphaType,Set<MycoNode>>();
      MycoCast.typeMap.put(HyphaType.BIOMASS, biomassNodes);
      MycoCast.typeMap.put(HyphaType.BULWARK, bulwarkNodes);
      MycoCast.typeMap.put(HyphaType.EXTENDING, extendingNodes);
      MycoCast.typeMap.put(HyphaType.BRANCHING, branchingNodes);
      MycoCast.typeMap.put(HyphaType.IMMOBILE, immobileNodes);
    }
  }

  public void nextCycle(Node node, int pid) {
    myNode = (MycoNode) node;
    verify(myNode, myNode.getHyphaData().getState());
    /*Set<MycoNode> s = new HashSet<MycoNode>(getAllNodes());
      for (int i = 0; i < Network.size(); i++) {
      s.remove(Network.get(i));
      log.warning(s.size() + " UNDEAD NODES IN MYCOCAST");
      }
      for (MycoNode n : s) {
      kill(n);
      }*/
    //        return false;
  }

  public static void ensureBiomass(MycoNode node) {
    verify(node, HyphaType.BIOMASS);
  }

  public static MycoList getAllNodes() {
    MycoList ret = new MycoList(MycoCast.biomassNodes);
    ret.addAll(MycoCast.bulwarkNodes);
    ret.addAll(MycoCast.immobileNodes);
    ret.addAll(MycoCast.branchingNodes);
    ret.addAll(MycoCast.extendingNodes);
    return ret;
  }

  public static MycoList getAllHyphae() {
    MycoList ret = new MycoList();
    ret.addAll(MycoCast.immobileNodes);
    ret.addAll(MycoCast.branchingNodes);
    ret.addAll(MycoCast.extendingNodes);
    return ret;
  }

  public static MycoList getAllNonBiomass() {
    MycoList ret = getAllHyphae();
    ret.addAll(MycoCast.bulwarkNodes);
    return ret;
  }

  public static MycoList getBulwarkNodes() {
    MycoList ret = new MycoList(MycoCast.bulwarkNodes);
    return ret;
  }

  public static MycoList getImmobileHyphae() {
    MycoList ret = new MycoList(MycoCast.immobileNodes);
    return ret;
  }

  public static MycoList getBranchingHyphae() {
    MycoList ret = new MycoList(MycoCast.branchingNodes);
    return ret;
  }

  public static MycoList getExtendingHyphae() {
    MycoList ret = new MycoList(MycoCast.extendingNodes);
    return ret;
  }

  public static MycoList getAllBiomass() {
    MycoList ret = new MycoList(biomassNodes);
    return ret;
  }

  public static int countBiomass() {
    return MycoCast.biomassNodes.size();
  }

  public static int countBulwark() {
    return MycoCast.bulwarkNodes.size();
  }

  public static int countExtending() {
    return MycoCast.extendingNodes.size();
  }

  public static int countBranching() {
    return MycoCast.branchingNodes.size();
  }

  public static int countImmobile() {
    return MycoCast.immobileNodes.size();
  }

  public static void kill(MycoNode n) {
    log.log(Level.FINER, "KILLING " + n, n);
    MycoCast.biomassNodes.remove(n);
    MycoCast.bulwarkNodes.remove(n);
    MycoCast.extendingNodes.remove(n);
    MycoCast.branchingNodes.remove(n);
    MycoCast.immobileNodes.remove(n);
  }

  public static MycoNode pickFrom(Collection<MycoNode> c) {
    int n = generator.nextInt(c.size());
    MycoNode ret = null;
    Iterator<MycoNode> it = c.iterator();
    for (int i = 0; i < n; i++) {
      ret = it.next();
    }
    return ret;
  }

  public static MycoNode pickRandomNode() {
    return pickFrom(getAllNodes());
  }

  public static MycoNode pickImmobileNode() {
    return pickFrom(MycoCast.immobileNodes);
  }

  public static MycoNode pickBranchingNode() {
    return pickFrom(MycoCast.branchingNodes);
  }

  public static MycoNode pickExtendingNode() {
    return pickFrom(MycoCast.extendingNodes);
  }

  public static MycoNode getStableHypha() {
    int n;
    MycoNode ret = null;
    Collection<MycoNode> all = getAllNonBiomass();
    if (!all.isEmpty()) {
      log.finest("SEARCHING FOR A HYPHA");
      ret = pickFrom(all);
    }
    if (ret != null) {
      log.finest("FOUND STABLE NODE " + ret.getID());
    } else {
      log.finest("COULDN'T FIND A STABLE NODE");
    }
    return ret;
  }


  /*public static MycoNode getStableHypha() {
    int n;
    MycoNode ret = null;
    if (!MycoCast.immobileNodes.isEmpty()) {
    log.finest("SEARCHING FOR AN IMMOBILE NODE");
    ret = pickImmobileNode();
    }
    if (ret == null && !MycoCast.branchingNodes.isEmpty()) {
    log.finest("SEARCHING FOR A BRANCHING NODE");
    ret = pickBranchingNode();
    }
    if (ret == null && !MycoCast.extendingNodes.isEmpty()) {
    log.finest("SEARCHING FOR AN EXTENDING NODE");
    ret = pickExtendingNode();
    }

    if (ret != null) {
    log.finest("FOUND STABLE NODE " + ret.getID());
    } else {
    log.finest("COULDN'T FIND A STABLE NODE");
    }
    return ret;
    }*/

  public static void verify(MycoNode node, HyphaType type) {
    Set<MycoNode> properSet = typeMap.get(type);

    if (! properSet.contains(node)) {
      log.log(Level.FINE, node.getID() + " not properly registered as " +
              type);
      become(node,type);
    }
  }

  public static void become(MycoNode node, HyphaType type) {
    log.log(Level.FINER, node.getID() + " MOVETO " + type,
            node);

    for (HyphaType bucket : typeMap.keySet()) {
      if (type == bucket) {
        typeMap.get(bucket).add(node);
      } else {
        typeMap.get(bucket).remove(node);
      }
    }

    // if (type == HyphaType.BIOMASS) {
    //     MycoCast.biomassNodes.add(node);
    //     MycoCast.bulwarkNodes.remove(node);
    //     MycoCast.extendingNodes.remove(node);
    //     MycoCast.branchingNodes.remove(node);
    //     MycoCast.immobileNodes.remove(node);
    // } else if (type == HyphaType.BULWARK) {
    //     MycoCast.biomassNodes.remove(node);
    //     MycoCast.bulwarkNodes.add(node);
    //     MycoCast.extendingNodes.remove(node);
    //     MycoCast.branchingNodes.remove(node);
    //     MycoCast.immobileNodes.remove(node);
    // } else if (type == HyphaType.EXTENDING) {
    //     MycoCast.biomassNodes.remove(node);
    //     MycoCast.bulwarkNodes.remove(node);
    //     MycoCast.extendingNodes.add(node);
    //     MycoCast.branchingNodes.remove(node);
    //     MycoCast.immobileNodes.remove(node);
    // } else if (type == HyphaType.BRANCHING) {
    //     MycoCast.biomassNodes.remove(node);
    //     MycoCast.bulwarkNodes.remove(node);
    //     MycoCast.extendingNodes.remove(node);
    //     MycoCast.branchingNodes.add(node);
    //     MycoCast.immobileNodes.remove(node);
    //     log.log(Level.FINER, "NOW " + MycoCast.branchingNodes.size() +
    //             " BRANCHING HYPHA", node);
    // } else if (type == HyphaType.IMMOBILE) {
    //     MycoCast.biomassNodes.remove(node);
    //     MycoCast.bulwarkNodes.remove(node);
    //     MycoCast.extendingNodes.remove(node);
    //     MycoCast.branchingNodes.remove(node);
    //     MycoCast.immobileNodes.add(node);
    // } else if (type == HyphaType.DEAD) {
    //     MycoCast.biomassNodes.remove(node);
    //     MycoCast.bulwarkNodes.remove(node);
    //     MycoCast.extendingNodes.remove(node);
    //     MycoCast.branchingNodes.remove(node);
    //     MycoCast.immobileNodes.remove(node);
    // }
  }

  public static MycoNode getForagingHypha() {
    // int n;
    // MycoNode node;
    // MycoNode ret = null;
    // HyphaLink l;

    // if (!MycoCast.extendingNodes.isEmpty()) {
    //   n = generator.nextInt(MycoCast.extendingNodes.size());
    //   Iterator<MycoNode> it = MycoCast.extendingNodes.iterator();
    //   for (int i = 0; i < n; i++) {
    //     node = it.next();
    //     l = (HyphaLink) node.getProtocol(hyphalinkPid);
    //     if (l.hasCapacity(node)) {
    //       ret = node;
    //       break;
    //     }
    //   }
    // }
    // return ret;
    return getAttachableFrom(new MycoList(MycoCast.extendingNodes), 1.00);
  }

  public static MycoNode getAttachableHypha() {
    MycoList hyphae = getAllHyphae();

    return getAttachableFrom(hyphae, 1.05);
  }

  public static MycoNode getAttachableFrom(ArrayList<MycoNode> c,
                                           double multiplier) {
    //int n;
    MycoNode node;
    MycoNode ret = null;
    HyphaLink l;

    Collections.shuffle(c, CommonState.r);

    for (int i = 0; i < c.size(); i++) {
      node = c.get(i);
      l = (HyphaLink) node.getProtocol(hyphalinkPid);
      if (l.withinCapacity(node, multiplier)) {
        ret = node;
        break;
      }
    }
    return ret;
  }

  @Override
  public void onKill() {
    kill(myNode);
  }

  public Object clone() {
    MycoCast ret = null;
    try {
      ret = (MycoCast) super.clone();
    } catch (CloneNotSupportedException e) {
      // Never happens
    }
    return ret;
  }

}
