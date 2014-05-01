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
import peersim.core.*;
import peersim.config.*;

import java.util.logging.*;

public class FailureAlerter implements CDProtocol, FailureAnnouncementListener,
                                       NeighborFailureListener {

  private static final String PAR_FAILUREALERTER_PROTO =
      "network.node.failurealerter_proto";
  private static final String PAR_FAILURE_STRATEGY = "failure_strategy";
  private static final String PAR_BULWARK_STRATEGY = "bulwark_strategy";
  private static final String PAR_INHIBITION_STRATEGY = "inhibition_strategy";
  private static final String PAR_METABOLISM_STRATEGY = "metabolism_strategy";
  private static final String PAR_CLEAR_ON_REVERT = "clear_on_revert";

  private static final String PAR_CHEMICALMANAGER_PROTO =
      "network.node.chemicalmanager_proto";

  private static Logger log =
      Logger.getLogger(FailureAlerter.class.getName());

  private MycoNode myNode;
  private HyphaType oldState = HyphaType.BIOMASS;

  private static int failureAlerterPid;
  private static int chemicalManagerPid;
  private static double multiplier;
  private static Class failureClass;
  private static Class bulwarkClass;
  private static Class inhibitionClass;
  private static Class metabolismClass;
  private static boolean clearOnRevert;

  private static FailureStrategy failureStrategy;
  private static BulwarkStrategy bulwarkStrategy;
  private static InhibitionStrategy inhibitionStrategy;
  private static MetabolismStrategy metabolismStrategy;

  private int switchAttempts;

  public FailureAlerter(String prefix) {
    failureAlerterPid = Configuration.getPid(PAR_FAILUREALERTER_PROTO);
    chemicalManagerPid = Configuration.getPid(PAR_CHEMICALMANAGER_PROTO);

    switchAttempts = 0;
    oldState = HyphaType.BIOMASS;

    if (failureStrategy == null) {
      try {
        failureClass =
            Configuration.getClass(prefix + "." + PAR_FAILURE_STRATEGY);
        if (FailureStrategy.class.isAssignableFrom(failureClass)) {
          failureStrategy = (FailureStrategy) failureClass.newInstance();
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
      try {
        bulwarkClass =
            Configuration.getClass(prefix + "." + PAR_BULWARK_STRATEGY);
        if (BulwarkStrategy.class.isAssignableFrom(bulwarkClass)) {
          bulwarkStrategy =
              (BulwarkStrategy) bulwarkClass.newInstance();
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
      try {
        inhibitionClass =
            Configuration.getClass(prefix + "." +
                                   PAR_INHIBITION_STRATEGY);
        if (InhibitionStrategy.class.isAssignableFrom(inhibitionClass))
        {
          inhibitionStrategy =
              (InhibitionStrategy) inhibitionClass.newInstance();
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
      try {
        metabolismClass =
            Configuration.getClass(prefix + "." +
                                   PAR_METABOLISM_STRATEGY);
        if (MetabolismStrategy.class.isAssignableFrom(metabolismClass))
        {
          metabolismStrategy =
              (MetabolismStrategy) metabolismClass.newInstance();
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
      clearOnRevert =
          Configuration.getBoolean(prefix + "." + PAR_CLEAR_ON_REVERT);
    }

    HyphaLink.addFailureListener(this);
  }

  public double getScalingFactor() {
    return bulwarkStrategy.getScalingFactor();
  }

  public Object clone() {
    FailureAlerter ret = null;
    try {
      ret = (FailureAlerter) super.clone();
      ret.oldState = HyphaType.BIOMASS;
    } catch (CloneNotSupportedException e) {
      // Never happens
    }
    ret.switchAttempts = 0;
    ret.oldState = HyphaType.BIOMASS;
    return ret;
  }

  public void notifyFailing(MycoNode n, HyphaType t, int degree,
                            double parentTarget, MycoList neighbors) {
    for (MycoNode neighbor : neighbors) {
      FailureAlerter fa =
          (FailureAlerter) neighbor.getProtocol(failureAlerterPid);
      fa.neighborFailed(n, t, degree, parentTarget);
    }
  }
  public void notifySevering(MycoNode n, HyphaType t, int degree,
                             double parentTarget, MycoList neighbors) {
    for (MycoNode neighbor : neighbors) {
      FailureAlerter fa =
          (FailureAlerter) neighbor.getProtocol(failureAlerterPid);
      fa.neighborFailed(n, t, degree, parentTarget);
    }
  }

  public void neighborFailed(MycoNode n, HyphaType t, int degree,
                             double parentTarget) {
    double amount =
        failureStrategy.apply(myNode, myNode.getHyphaData().getState(),
                              myNode.getHyphaLink().degree() - 1,
                              n, t, degree);
    if (amount > 0.0) {
      log.log(Level.FINE, myNode + " detected failed neighbor " +
              n.getID() +
              " state: " + t + ", degree: " + degree +
              ", generating " + amount + " of AlertHormone",
              new Object[] { myNode, n });
      ((ChemicalManager) myNode.getProtocol(chemicalManagerPid))
      .add(new AlertHormone(amount));
    }

    // System.out.println(myNode + " detected failed neighbor " + n + " with " +
    //                    parentTarget + " parent_target");
    //myNode.getHyphaData()
    //    .recordKnownDisconnect(parentTarget);

    // Note that self-degree (in HyphaLink is *before* neighbor is
    // severed, so need to decrement degree to determine the post-degree
    //parentStrategy.applyFailure(myNode, myNode.getHyphaData().getState(),
    //                            myNode.getHyphaLink().degree() - 1,
    //                            n, t, degree);
    //n.getHyphaData().setFailureDetected();
    // if (myNode.getHyphaLink().neighborCount() - 1 < 1) {
    //   n.getHyphaData().setDisconnectDetected();
    // }
  }

  public void neighborSevered(MycoNode n, HyphaType t, int degree,
                              double parentTarget) {
    neighborFailed(n, t, degree, parentTarget);
  }

  public void severAllNeighbors(HyphaType t) {
    MycoList neighbors = myNode.getHyphaLink().getNeighbors();

    for (MycoNode neighbor : neighbors) {
      FailureAlerter fa = (FailureAlerter)
          neighbor.getProtocol(failureAlerterPid);
      fa.neighborSevered(myNode, t, neighbors.size(),
                         myNode.getHyphaData().getParentTarget());
    }
  }

  public void announceReconnect(double parentTarget) {
    //System.out.println(myNode + " heard reconnect of size " + parentTarget);
    myNode.getHyphaData().recordKnownDisconnect(parentTarget);
    //for (MycoNode n : myNode.getHyphaLink().getNeighbors()) {
    //  n.getHyphaData().recordKnownDisconnect(parentTarget);
    //}
  }

  public void becomeBulwark() {
    HyphaType state = myNode.getHyphaData().getState();
    if (state != HyphaType.BULWARK) {
      switchAttempts += 1;
      if (inhibitionStrategy.apply(myNode, state, switchAttempts)) {
        oldState = state;
        severAllNeighbors(oldState);
        myNode.getHyphaData().become(myNode,HyphaType.BULWARK);
        switchAttempts = 0;
      }
    } else {
      switchAttempts = 0;
    }
  }

  public void revertBulwark() {
    HyphaType state = myNode.getHyphaData().getState();
    if (state == HyphaType.BULWARK) {
      switchAttempts += 1;
      if (inhibitionStrategy.apply(myNode, state, switchAttempts)) {
        myNode.getHyphaData().become(myNode,oldState);
        switchAttempts = 0;
      }
      if (clearOnRevert) {         // FIXME: wipes ALL chems
        ((ChemicalManager) myNode.getProtocol(chemicalManagerPid)).wipe();
      }
    } else {
      switchAttempts = 0;
    }
  }

  public void nextCycle(Node node, int pid) {
    myNode = (MycoNode) node;

    //HyphaLink link = myNode.getHyphaLink();

    if (! myNode.isUp()) {
      return;
    }

    if (bulwarkStrategy.apply(myNode)) {
      becomeBulwark();
    } else {
      revertBulwark();
    }

    metabolismStrategy.apply(myNode);

    // myNode.getHyphaData().clearFailureDetected();
    // myNode.getHyphaData().clearDisconnectDetected();
    // Only clear disconnect state in JoinStrategy, upon reconnect
  }
}
