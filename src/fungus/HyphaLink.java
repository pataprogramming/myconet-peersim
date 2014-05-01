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
import edu.uci.ics.jung.graph.*;
import edu.uci.ics.jung.graph.util.*;

import org.apache.commons.collections15.*;

public class HyphaLink implements Linkable, CDProtocol, Cleanable {

  private static final String PAR_HYPHADATA_PROTO =
      "network.node.hyphadata_proto";
  private static final String PAR_HYPHALINK_PROTO =
      "network.node.hyphalink_proto";
  private static final String PAR_MYCOCAST_PROTO  =
      "network.node.mycocast_proto";

  private static int hyphaDataPid;
  private static int hyphaLinkPid;
  private static int mycoCastPid;

  private static Logger log = Logger.getLogger(HyphaLink.class.getName());

  //    private HyphaData data;
  //    private HyphaLink link;
  private MycoList neighbors;
  private MycoNode myNode;

  private static List<HyphaLinkListener> listeners =
      new ArrayList<HyphaLinkListener>();
  private static List<FailureAnnouncementListener> failureListeners =
      new ArrayList<FailureAnnouncementListener>();

  public HyphaLink(String prefix) {
    mycoCastPid = Configuration.getPid(PAR_MYCOCAST_PROTO);
    hyphaDataPid = Configuration.getPid(PAR_HYPHADATA_PROTO);
    hyphaLinkPid = Configuration.getPid(PAR_HYPHALINK_PROTO);

    myNode = null;
    // FIXME: May not work correctly with default clone()
    neighbors = new MycoList();
  }

  public void nextCycle(Node node, int pid) {
    myNode = (MycoNode) node;
    for (MycoNode neighbor : neighbors.duplicate()) {
      if (!neighbor.isUp()) {
        pruneNeighbor(neighbor);
      }
    }
    // data = (HyphaData) node.getProtocol(hyphaDataPid);
    // link = (HyphaLink) node.getProtocol(hyphaLinkPid);
  }

  public String toString() {
    String ret = "<HyphaLink: " + " ";
    HyphaData d;

    for (MycoNode n : neighbors) {
      d = (HyphaData) n.getProtocol(hyphaDataPid);
      ret += n + " " + d.toString() + " ";
    }
    ret += ">";
    return ret;
  }

  public boolean hasCapacity(MycoNode n) {
    HyphaData d;
    d = (HyphaData) n.getProtocol(hyphaDataPid);
    return neighbors.size() < d.getMaxCapacity();
  }

  public boolean withinCapacity(MycoNode n, double multiplier) {
    return neighbors.size() < (n.getHyphaData().getMaxCapacity() * multiplier);
  }

  public boolean addNeighbor(Node neighbor) {
    return addNeighbor((MycoNode) neighbor);
  }

  public boolean addNeighbor(MycoNode neighbor) {
    if (myNode == neighbor || neighbors.contains(neighbor)) {
      // do nothing; no double-entries, no self-links
    } else {
      neighbors.add(neighbor);
      fireLinkAdded(neighbor);
    }
    HyphaLink neighborLink =
        ((HyphaLink) neighbor.getProtocol(hyphaLinkPid));
    neighborLink.linkBack(myNode);
    MessageObserver.topoActionMessages(2);
    return true;
  }

  public int neighborCount() {
    return neighbors.size();
  }


  public boolean linkBack(MycoNode neighbor) {
    if (myNode == neighbor || neighbors.contains(neighbor)) { return true; }
    boolean ret = neighbors.add(neighbor);
    if (ret) {
      fireLinkAdded(neighbor);
    }
    return ret;
  }

  public MycoList deadNeighbors() {
    return neighbors.getDead();
  }

  public boolean removeNeighbor(MycoNode neighbor) {
    log.finer("REMOVING CONNECTION WITH " + neighbor);
    neighbors.remove(neighbor);
    fireLinkRemoved(neighbor);
    HyphaLink neighborLink =
        ((HyphaLink) neighbor.getProtocol(hyphaLinkPid));
    neighborLink.pruneNeighbor(myNode);
    MessageObserver.topoActionMessages(2);
    return true;
  }

  public boolean pruneNeighbor(MycoNode neighbor) {
    boolean ret = neighbors.remove(neighbor);
    if (ret) {
      fireLinkRemoved(neighbor);
    }
    return ret;
  }

  public MycoList getNeighbors() {
    return neighbors.duplicate();
  }

  public MycoList getSameNeighbors() {
    HyphaData d =
        ((HyphaData) (myNode).getProtocol(hyphaDataPid));
    return neighbors.getType(d.getType());
  }

  public MycoList getDifferentNeighbors() {
    HyphaData d =
        ((HyphaData) (myNode).getProtocol(hyphaDataPid));
    return neighbors.getTypeNot(d.getType());
  }

  public MycoList getBiomass() {
    return neighbors.getBiomass();
  }

  public MycoList getHyphae() {
    return neighbors.getHyphae();
  }

  public MycoList getImmobile() {
    return neighbors.getImmobile();
  }

  public MycoList getBranching() {
    return neighbors.getBranching();
  }

  public MycoList getExtending() {
    return neighbors.getExtending();
  }

  public MycoList getStable() {
    return neighbors.getStable();
  }

  public boolean contains(Node neighbor) {
    return neighbors.contains(neighbor);
  }

  public boolean isDisconnected() {
    return neighbors.isEmpty();
  }

  public int idealHyphae() {
    HyphaData d =
        ((HyphaData) (myNode).getProtocol(hyphaDataPid));
    return d.getIdealHyphae();
  }
  public int idealOtherHyphae() {
    HyphaData d =
        ((HyphaData) (myNode).getProtocol(hyphaDataPid));
    return d.getIdealOtherHyphae();
  }

  public boolean isIdealHyphae() {
    return (hyphaDegree() == idealHyphae());
  }

  public boolean isOverHyphae() {
    return (hyphaDegree() > idealHyphae());
  }

  public boolean isUnderHyphae() {
    return (hyphaDegree() < idealHyphae());
  }

  public boolean isAtOrOverHyphae() {
    return (hyphaDegree() >= idealHyphae());
  }

  public int idealBiomass() {
    return myNode.getHyphaData().getIdealBiomass();
  }

  public boolean isIdealSameHyphae() {
    return (sameHyphaDegree() == idealHyphae());
  }

  public boolean isOverSameHyphae() {
    return (sameHyphaDegree() > idealHyphae());
  }

  public boolean isUnderSameHyphae() {
    return (sameHyphaDegree() < idealHyphae());
  }

  public boolean isAtOrOverSameHyphae() {
    return (sameHyphaDegree() >= idealHyphae());
  }

  public boolean isIdealDifferentHyphae() {
    return (differentHyphaDegree() == idealOtherHyphae());
  }

  public boolean isOverDifferentHyphae() {
    return (differentHyphaDegree() > idealOtherHyphae());
  }

  public boolean isAtOrOverDifferentHyphae() {
    return (differentHyphaDegree() >= idealOtherHyphae());
  }

  public boolean isUnderDifferentHyphae() {
    return (differentHyphaDegree() < idealOtherHyphae());
  }

  public boolean isOverBiomass() {
    return (sameBiomassDegree() > idealBiomass());
  }

  public boolean isUnderBiomass() {
    return (sameBiomassDegree() < idealBiomass());
  }

  public int amountOverBiomass() {
    return (sameBiomassDegree() - idealBiomass());
  }

  public int amountUnderBiomass() {
    return (idealBiomass() - sameBiomassDegree());
  }

  public int amountOverSameHyphae() {
    return (sameHyphaDegree() - idealHyphae());
  }

  public int amountUnderSameHyphae() {
    return (idealHyphae() - sameHyphaDegree());
  }

  public int degree() {
    return neighbors.size();
  }

  public int sameDegree() {
    return getSameNeighbors().size();
  }

  public int differentDegree() {
    return getDifferentNeighbors().size();
  }

  public int hyphaDegree() {
    return neighbors.getHyphae().size();
  }

  public int sameHyphaDegree() {
    return getSameNeighbors().getHyphae().size();
  }

  public int differentHyphaDegree() {
    return getDifferentNeighbors().getHyphae().size();
  }

  public int biomassDegree() {
    return neighbors.getBiomass().size();
  }

  public int sameBiomassDegree() {
    return getSameNeighbors().getBiomass().size();
  }

  public MycoNode getRandomNeighbor() {
    return neighbors.getRandom();
  }

  public MycoNode getRandomSameNeighbor() {
    return neighbors.getRandomOfType(myNode.getHyphaData().getType());
  }

  public MycoNode getRandomSameHypha() {
    return neighbors.getHyphae().getRandomOfType(myNode.getHyphaData().getType());
  }

  public MycoNode getNeighbor(int i) {
    return neighbors.get(i);
  }

  public int getNeighborhoodCapacity() {
    int cap = 0;
    for (MycoNode n : neighbors.getType(myNode.getHyphaData().getType())) {
      cap += n.getHyphaData().getCapacity();
    }
    return cap;
  }

  public int getNeighborhoodQueueLength() {
    int len = 0;
    for (MycoNode n : neighbors.getType(myNode.getHyphaData().getType())) {
      len += n.getHyphaData().getQueueLength();
    }
    return len;
  }

  // Return a list of all nodes two jumps away in the graph
  public MycoList get2Neighbors() {
    Set<MycoNode> set = new HashSet<MycoNode>();
    for (MycoNode n : getHyphae()) {
      set.addAll(n.getHyphaLink().getNeighbors());
    }
    set.remove(myNode);
    set.remove(neighbors);
    return new MycoList(set);
  }

  // Return a list of all nodes within two jump
  public MycoList get2Neighborhood() {
    Set<MycoNode> set = new HashSet<MycoNode>();
    for (MycoNode n : getHyphae()) {
      set.add(n);
      set.addAll(n.getHyphaLink().getNeighbors());
    }
    set.remove(myNode);
    return new MycoList(set);
  }


  // get the highest capacity biomass node from among all neighbors
  // (except those specified in exclude) (of same type)
  public MycoList getMaxNeighborBiomass(Collection<MycoNode> exclude) {
    MycoNode maxBiomass = null;
    int maxBiomassCapacity = 0;

    MycoNode t;
    MycoNode owningNeighbor = null;
    HyphaData d;
    for (MycoNode n : getSameNeighbors().getHyphae()) {
      if (exclude.contains(n)) { continue; }
      t = n.getHyphaLink().getMaxBiomass();
      if (t == null) { continue; }
      d = t.getHyphaData();
      if (d.getMaxCapacity() > maxBiomassCapacity) {
        maxBiomassCapacity = d.getMaxCapacity();
        maxBiomass = t;
        owningNeighbor = n;
      }
    }
    // Return max biomass node as the first list element,
    //  and the owning neighbor as the second
    MycoList ret = new MycoList();
    if (maxBiomass != null) {
      ret.add(maxBiomass);
      ret.add(owningNeighbor);
    }
    return ret;
  }

  // get the highest capacity child biomass node (of the same type)
  public MycoNode getMaxBiomass() {
    HyphaData d;
    MycoNode maxBiomass = null;
    int maxBiomassCapacity = 0;
    for (MycoNode n : getSameNeighbors().getBiomass()) {
      d = n.getHyphaData();
      if (d.isBiomass() && (d.getMaxCapacity() > maxBiomassCapacity)) {
        maxBiomassCapacity = d.getMaxCapacity();
        maxBiomass = n;
      }
    }
    return maxBiomass;
  }

  public void transferBiomass(MycoNode target, int quantity) {
    int transferred = 0;
    for (MycoNode bio : getBiomass()) {
      if (transferred >= quantity) { break; }
      transferNeighbor(bio, target);
      transferred++;
    }
    log.log(Level.FINE, myNode + " TRANSFERRED " + quantity + " BIOMASS " +
            " TO " + target, myNode);
  }

  public boolean areNeighbors(MycoNode a, MycoNode b) {
    HyphaLink al = a.getHyphaLink();
    return al.isNeighbor(b);
  }

  public boolean isNeighbor(MycoNode b) {
    return neighbors.contains(b);
  }

  public MycoNode getParent() {
    if (myNode.getHyphaData().isBiomass()) {
      log.log(Level.FINEST, myNode + " QUERIED FOR PARENT; IS BIOMASS", myNode);
      MycoList h = getHyphae();
      if (h.size() > 0) {
        if (h.size() > 1) {
          // FIXME: Handle multiple parents better!!!
          log.log(Level.FINE, myNode + " MULTIPLE HYPHAE FOR BIOMASS NODE!", myNode);
        }
        return h.get(0);
      }
    }
    return null;
  }

  public void transferNeighbor(MycoNode neighbor, MycoNode target) {
    if (neighbor != null && target != null) {
      if (neighbor == target) {
        log.log(Level.FINER, myNode + " TRIED TO TRANSFER " +
                neighbor + " TO ITSELF.",
                new Object[] { myNode, neighbor });
        return;
      }
      neighbors.remove(neighbor);
      fireLinkRemoved(neighbor);
      HyphaLink neighborLink =
          ((HyphaLink) neighbor.getProtocol(hyphaLinkPid));
      neighborLink.pruneNeighbor(myNode);

      if (areNeighbors(neighbor,target)) {
        log.log(Level.FINER, myNode + " TRIED TO TRANSFER "+
                target + " TO " +
                neighbor + " BUT ARE ALREADY " + "CONNECTED",
                new Object[] {myNode, neighbor, target});
        return;
      }
      log.log(Level.FINER, myNode + " TRANSFERS " + neighbor
              + " TO " + target, new Object[] {myNode, neighbor, target});
      HyphaLink targetLink = target.getHyphaLink();
      targetLink.addNeighbor(neighbor);
    } else {
      log.log(Level.WARNING, "BAD TRANSFER attempted from " + myNode +
              " TO " + target, new Object[] {myNode, neighbor, target});
      (new Throwable()).printStackTrace();
      //FIXME: Warning
    }
  }

  public void absorbHypha(MycoNode target) {

    HyphaData myData = (HyphaData) myNode.getProtocol(hyphaDataPid);
    HyphaLink tl = (HyphaLink) target.getProtocol(hyphaLinkPid);
    HyphaData targetData = (HyphaData) target.getProtocol(hyphaDataPid);

    log.log(Level.FINER, myNode + " (" + myData.getState() + ") ABSORBS " +
            target + " (" + targetData.getState() + ")",
            new Object[] {myNode, target});

    if (targetData.isBiomass()) {
      log.log(Level.FINE, myNode.getID() +
              " TRIED TO ABSORB BIOMASS NODE " + target.getID(),
              new Object[] { myNode, target });
      return;
    }

    // Move all target's hyphae to self
    MycoList hyphae = tl.getHyphae();
    for (MycoNode n : hyphae) {
      tl.transferNeighbor(n, myNode);
    }

    // Move all target's biomass to self
    MycoList biomass = tl.getBiomass();
    for (MycoNode n : biomass) {
      tl.transferNeighbor(n, myNode);
    }

    // Demote absorbed target to biomass
    targetData.becomeBiomass(target);
    //System.out.println("DEMOTED absorbed target " + target);
    //System.out.println("NEW NODE " + myNode);
  }

  public void swapHyphae(MycoNode target) {
    log.log(Level.FINE, myNode + " SWAPS WITH " + target,
            new Object[] {myNode, target});
    // Move all connected hyphae to target
    MycoList hyphae = this.getHyphae();
    for (MycoNode n : hyphae) {
      this.transferNeighbor(n, target);
    }

    // Move all connected biomass to target
    MycoList biomass = this.getBiomass();
    for (MycoNode n : biomass) {
      this.transferNeighbor(n, target);
    }

    // Promote target to same state as self

    HyphaData data = (HyphaData)
        (myNode.getProtocol(hyphaDataPid));
    HyphaData targetData = (HyphaData) target.getProtocol(hyphaDataPid);
    targetData.become(target,data.getState());

    // Demote self to biomass state
    data.becomeBiomass(myNode);
  }

  public MycoNode growHypha() {
    MycoNode newHypha = getMaxBiomass();
    if (newHypha != null) {
      log.log(Level.FINER, myNode + " GROWS HYPHA INTO " +
              newHypha, new Object[] {myNode, newHypha} );
      HyphaData newHyphaData = (HyphaData) newHypha.getProtocol(hyphaDataPid);
      newHyphaData.becomeExtending(newHypha);
    }
    return newHypha;
  }

  public Object clone() {
    HyphaLink ret = null;
    try {
      ret = (HyphaLink) super.clone();
      ret.neighbors = new MycoList();
    } catch (CloneNotSupportedException e) {
      // Never happens
    }
    return ret;
  }

  @Override
  public void onKill() {
    if (myNode == null) {
      log.log(Level.FINER, "Cycle " + CDState.getCycle() +
              ": TRIED TO KILL UNINITIALIZED NODE");
      return;
    }
    HyphaData myData = (HyphaData) myNode.getProtocol(hyphaDataPid);
    log.log(Level.FINER, myNode + " (" + myData.getState() +
            ") HAS BEEN KILLED ", myNode);

    fireFailing(myNode, myData.getState(), degree(), myData.getParentTarget(),
                neighbors);
    MycoList removing = new MycoList(neighbors);
    for (MycoNode neighbor : removing) {
      HyphaLink nl = neighbor.getHyphaLink();
      nl.removeNeighbor(myNode);
    }
    neighbors.clear();

    /*
      List<Node> nl = new ArrayList<Node>(neighbors);
      for (Node neighbor : nl) {
      HyphaData nd = (HyphaData) neighbor.getProtocol(hyphaDataPid);
      log.finest(myNode.getID() + " SEVERING NEIGHBOR " +
      neighbor.getID() + " (" + nd.getState() + ")");
      removeNeighbor(neighbor);
      }
    */
  }

  public void pack() {}

  public static void addHyphaLinkListener(HyphaLinkListener l) {
    HyphaLink.listeners.add(l);
  }

  public static void removeHyphaLinkListener(HyphaLinkListener l) {
    HyphaLink.listeners.remove(l);
  }

  protected void fireLinkAdded(MycoNode neighbor) {
    if (myNode != null) {
      for (HyphaLinkListener l : HyphaLink.listeners) {
        l.linkAdded(myNode, neighbor);
      }
    }
  }

  protected void fireLinkRemoved(MycoNode neighbor) {
    if (myNode != null) {
      for (HyphaLinkListener l :HyphaLink.listeners) {
        l.linkRemoved(myNode, neighbor);
      }
    }
  }

  public static void addFailureListener(FailureAnnouncementListener l) {
    HyphaLink.failureListeners.add(l);
  }

  public static void removeFailureListener(FailureAnnouncementListener l) {
    HyphaLink.failureListeners.remove(l);
  }

  protected static void fireFailing(MycoNode n, HyphaType t, int degree,
                                    double parentTarget, MycoList neighbors) {
    for (FailureAnnouncementListener l : failureListeners) {
      l.notifyFailing(n, t, degree, parentTarget, neighbors);
    }
  }

  protected static void fireSevering(MycoNode n, HyphaType t, int degree,
                                     double parentTarget, MycoList neighbors) {
    for (FailureAnnouncementListener l : failureListeners) {
      l.notifySevering(n, t, degree, parentTarget, neighbors);
    }
  }

}
