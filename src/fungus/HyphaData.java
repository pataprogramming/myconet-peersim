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

import java.io.StringWriter;
import java.io.PrintWriter;
import java.lang.*;
import java.lang.Math;
import java.util.*;
import java.util.logging.*;
import peersim.cdsim.*;
import peersim.core.*;
import peersim.config.*;

public class HyphaData implements CDProtocol, Cleanable {
  private static final String PAR_MYCOCAST_PROTO =
      "network.node.mycocast_proto";
  //  private static final String PAR_IDEAL_IMMOBILE = "ideal_immobile";
  private static final String PAR_IDEAL_HYPHAE = "ideal_hyphae";
  private static final String PAR_IDEAL_BIOMASS = "ideal_biomass";
  private static final String PAR_MAX_CAPACITY = "max_capacity";
  private static final String PAR_OTHER_BIOMASS_RATIO = "other_biomass_ratio";
  private static final String PAR_IDEAL_OTHER_HYPHAE = "ideal_other_hyphae";
  private static final String PAR_BULWARK_MAX = "bulwark_max";
  private static final String PAR_BULWARK_MIN = "bulwark_min";

  private static int mycoCastPid;

  private static Logger log = Logger.getLogger(HyphaData.class.getName());

  private int max_capacity;
  //private int ideal_immobile;
  private int ideal_hyphae;
  private int ideal_biomass;

  private int bulwark_max;
  private int bulwark_min;

  // ah: number of times node has entered bulwark state
  private int bulwark_entries;
  // ah: new local vals
  private int irevert;
  private int brevert;

  private double other_biomass_ratio;
  private int ideal_other_hyphae;

  private int max_other_biomass;

  private int wasted_units = 0;

  private int type;
  public static int numTypes; // FIXME: hackish
  private JobQueue queue;

  private double parent_target = 1;

  private HyphaType state;
  private MycoNode myNode;

  private boolean enteringNode = true;
  private boolean failureDetected = false;
  private boolean disconnectDetected = false;
  private double knownDisconnect = 0.0;
  private int observedParents = 0;

  private static List<HyphaDataListener> listeners =
      new ArrayList<HyphaDataListener>();

  public HyphaData(String prefix) {
    //this.ideal_immobile = 2;
    this.setIdealHyphae(5);
    this.setIdealOtherHyphae(3);
    this.other_biomass_ratio = 2.0;
    this.setMax(15);
    this.type = 0;

    state = HyphaType.BIOMASS;
    queue = new JobQueue();

    this.setIdealHyphae(Configuration.getInt(prefix + "."
                                             + PAR_IDEAL_HYPHAE));
    this.setIdealOtherHyphae(Configuration.getInt(prefix + "."
                                                  + PAR_IDEAL_OTHER_HYPHAE));
    other_biomass_ratio = Configuration.getDouble(prefix + "."
                                                  + PAR_OTHER_BIOMASS_RATIO);
    // ideal_immobile = Configuration
    //     .getInt(prefix + "." + PAR_IDEAL_IMMOBILE);
    // ideal_biomass=Configuration.getInt(prefix + "." + PAR_IDEAL_BIOMASS);
    this.setMax(Configuration.getInt(prefix + "." + PAR_MAX_CAPACITY));

    bulwark_max = Configuration.getInt(prefix + "." + PAR_BULWARK_MAX);
    bulwark_min = Configuration.getInt(prefix + "." + PAR_BULWARK_MIN);

    this.bulwark_entries = 0;
    this.irevert = 0;
    this.brevert = 0;

    HyphaData.mycoCastPid = Configuration.getPid(PAR_MYCOCAST_PROTO);
  }

  public void setEnteringNode() {
    enteringNode = true;
  }

  public void clearEnteringNode() {
    enteringNode = false;
  }

  public boolean isEnteringNode() {
    return enteringNode;
  }

  @Override
  public void nextCycle(Node node, int pid) {
    myNode = (MycoNode) node;
  }

  public void enqueue(Job j) {
    queue.add(j);
  }

  public int getQueueLength() {
    return queue.size();
  }

  // 'source' is only needed for logging purposes as myNode is null, FIXME
  public void transferJobs(MycoNode source, MycoNode target, int count) {

    HyphaData targetData = target.getHyphaData();
    int oldLength = getQueueLength();
    JobQueue transfers = queue.extract(count);

    log.log(Level.FINER, source.getID() +
            " transfering " + count + " jobs out of "
            + oldLength + " to " + target.getID()
            + ": " + transfers, new Object[] { source, target });

    while (!transfers.isEmpty()) {
      Job j = transfers.poll();
      j.transfer(target);
      targetData.enqueue(j);
    }
  }

  public void doWork(int units) {
    log.log(Level.FINER, "Cycle " + CDState.getCycle() + ": node " + myNode.getID() +
            " Ready to do " + units + " units of work, "
            + queue.size() + " jobs in the queue", myNode);
    StringWriter sw = new StringWriter();
    (new Throwable()).printStackTrace(new PrintWriter(sw));
    log.log(Level.FINER, sw.toString(), myNode);
    int remaining = units;
    while (remaining > 0) {
      if (!queue.isEmpty()) {
        Job j = queue.peek();
        remaining = j.process(remaining);
        log.log(Level.FINER, remaining
                + " work units left after working on job " + j, myNode);
        if (j.isCompleted()) {
          queue.remove();
          log.finer("job " + j + " is complete");
        }
      } else {
        wasted_units = remaining;
        log.log(Level.FINER, "Queue is exhausted; " + wasted_units
                + " work units idle", myNode);
        remaining = 0;
      }
    }
    log.log(Level.FINER, queue.size() + " jobs left in queue", myNode);
  }

  public String toString() {
    String ret = "<" + state + " cap: " + max_capacity + ">";
    return ret;
  }

  public MycoNode getNode() {
    return myNode;
  }

  public void setType(int t) {
    type = t;
  }

  public int getType() {
    return type;
  }

  public int getMaxCapacity() {
    return max_capacity;
  }

  public int getCapacity() {
    return max_capacity;
  }

  public int getMax() {
    return max_capacity;
  }

  public int getIdealBiomass() {
    return ideal_biomass;
  }

  public int getIdealHyphae() {
    return ideal_hyphae;
  }

  public int getIdealOtherHyphae() {
    return ideal_other_hyphae;
  }

  // public int getIdealImmobile() {
  //   return ideal_immobile;
  // }

  public int getBulwarkMax() {
    return bulwark_max;
  }

  public int getBulwarkMin() {
    return bulwark_min;
  }

  public int getBulwarkEntries() {
    return bulwark_entries;
  }

  public void incrementBulwarkEntries() {
    bulwark_entries++;
  }

  public int getIrevert() {
    return irevert;
  }

  public void setIrevert(int irevert) {
    this.irevert = irevert;
  }

  public int getBrevert() {
    return brevert;
  }

  public void setBrevert(int brevert) {
    this.brevert = brevert;
  }

  public HyphaType getState() {
    return state;
  }

  public boolean isBiomass() {
    return (state == HyphaType.BIOMASS);
  }

  public boolean isHypha() {
    return (state == HyphaType.IMMOBILE || state == HyphaType.BRANCHING
            || state == HyphaType.EXTENDING);
  }

  public boolean isBulwark() {
    return (state == HyphaType.BULWARK);
  }

  public boolean isExtending() {
    return (state == HyphaType.EXTENDING);
  }

  public boolean isBranching() {
    return (state == HyphaType.BRANCHING);
  }

  public boolean isImmobile() {
    return (state == HyphaType.IMMOBILE);
  }

  public boolean isDead() {
    return (state == HyphaType.DEAD);
  }

  public double getKnownDisconnect() {
    return knownDisconnect;
  }

  public void setKnownDisconnect(double parents) {
    knownDisconnect = parents;
  }

  public void recordKnownDisconnect(double parents) {
    knownDisconnect = Math.max(parents, knownDisconnect);
  }

  public void clearKnownDisconnect() {
    // if (knownDisconnect > 0.0) {
    //   System.out.println(myNode + " clearing known disconnects (was " +
    //                      knownDisconnect + ")");
    // }
    knownDisconnect = 0;
  }

  public void setFailureDetected() {
    failureDetected = true;
  }

  public void clearFailureDetected() {
    failureDetected = false;
  }

  public boolean wasFailureDetected() {
    return failureDetected;
  }

  public void setDisconnectDetected() {
    disconnectDetected = true;
  }

  public void clearDisconnectDetected() {
    disconnectDetected = false;
  }

  public boolean wasDisconnectDetected() {
    return disconnectDetected;
  }

  // public void become(HyphaType t) {
  //     //log.log(Level.FINER, myNode.getID() + " BECOMES " + t + " (WAS "
  //     //        + this.state + ")", myNode);
  //     state = t;
  //     log.log(Level.WARNING, "PINGITY: " + myNode);
  //     MycoCast mycocast = myNode.getMycoCast();
  //     log.log(Level.WARNING, "PONGITY: " + t);
  //     mycocast.become(myNode, t);
  // }

  public void become(MycoNode n, HyphaType t) {
    HyphaData d = n.getHyphaData();
    HyphaType oldState = this.state;
    state = t;

    if (n != null) {
      log.log(Level.FINER, n.getID() + " BECOMES " + t + " (WAS "
              + oldState + ")", n);
      fireNodeStateChanged(n,t, oldState);
      MycoCast mycocast = n.getMycoCast();
      mycocast.become(n, t);
    }
  }

  public void becomeBiomass(MycoNode n) {
    become(n, HyphaType.BIOMASS);
  }

  public void becomeBulwark(MycoNode n) {
    become(n, HyphaType.BULWARK);
  }

  public void becomeExtending(MycoNode n) {
    become(n, HyphaType.EXTENDING);
  }

  public void becomeBranching(MycoNode n) {
    become(n, HyphaType.BRANCHING);
  }

  public void becomeImmobile(MycoNode n) {
    become(n, HyphaType.IMMOBILE);
  }

  public void becomeDead(MycoNode n) {
    become(n, HyphaType.DEAD);
  }

  public void setMax(int max) {
    this.max_capacity = max;
    this.max_other_biomass = (int) (max * other_biomass_ratio);
    // int ib = max - ideal_hyphae;
    int ib = max;
    if (ib < 0) {
      ib = 0;
    }
    setIdealBiomass(ib);
  }

  public void setIdealBiomass(int ideal) {
    this.ideal_biomass = ideal;
  }

  public void setIdealHyphae(int ideal) {
    this.ideal_hyphae = ideal;
  }

  public void setIdealOtherHyphae(int other) {
    this.ideal_other_hyphae = other;
  }

  public double getParentTarget() {
    return this.parent_target;
  }

  public void setParentTarget(double pt) {
    this.parent_target = pt;
  }

  public void setObservedParents(int op) {
    observedParents = op;
  }

  public int getObservedParents() {
    return observedParents;
  }

  public void pack() {
  }

  public void clean() {
    //System.out.println("Cleaning!");
    parent_target = 1;
    failureDetected = false;
    disconnectDetected = false;
    knownDisconnect = 0.0;
    enteringNode = true;
    observedParents = 0;
  }

  @Override
  public void onKill() {
    enteringNode = true;
    becomeDead(myNode);
  }

  public Object clone() {
    HyphaData ret = null;
    try {
      ret = (HyphaData) super.clone();
      ret.queue = new JobQueue();
    } catch (CloneNotSupportedException e) {
      // Never happens
    }
    return ret;
  }

  public static void addHyphaDataListener(HyphaDataListener l) {
    HyphaData.listeners.add(l);
  }

  public static void removeHyphaDataListener(HyphaDataListener l) {
    HyphaData.listeners.remove(l);
  }

  protected void fireNodeStateChanged(MycoNode n, HyphaType t,
                                      HyphaType oldState) {
    if (n != null) {
      for (HyphaDataListener l : HyphaData.listeners) {
        l.nodeStateChanged(n, t, oldState);
      }
    }
  }
}
