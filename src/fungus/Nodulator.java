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
import peersim.dynamics.*;
import java.util.*;
import java.util.Collections;
import java.util.logging.*;
import cern.jet.random.Poisson;

public class Nodulator extends DynamicNetwork {
  private static final String PAR_FROM = "from";
  private static final String PAR_TO = "to";
  private static final String PAR_TYPE = "type";
  private static final String PAR_PERIOD = "period";
  private static final String PAR_POISSON = "poisson";
  protected final double from;
  protected final double to;
  protected final int period;
  protected final String type;
  protected final boolean poisson;

  protected final MycoNodeDegreeComparator degreeComparator =
      new MycoNodeDegreeComparator();

  private static Logger log = Logger.getLogger(Nodulator.class.getName());

  protected static Poisson gen;
  protected MycoList candidates;

  public Nodulator(String prefix) {
    super(prefix);
    from = Configuration.getDouble(prefix + "." + PAR_FROM);
    to = Configuration.getDouble(prefix + "." + PAR_TO);
    type = Configuration.getString(prefix + "." + PAR_TYPE);
    period = Configuration.getInt(prefix + "." + PAR_PERIOD);
    poisson = Configuration.getBoolean(prefix + "." + PAR_POISSON);

    if (poisson) {
      if (gen == null) {
        gen = new Poisson(1.0, new cern.jet.random.engine.MersenneTwister(CommonState.r.nextInt(Integer.MAX_VALUE)));
      }
    }

    log.info("Initialized with params (from=" +Double.toString(from) + ",to=" +
             Double.toString(to) + ",add=" + Double.toString(add) + ",min=" +
             Double.toString(minsize) + ",max=" + Double.toString(maxsize) +
             ",subst=" + Boolean.toString(substitute) + ",period=" +
             Integer.toString(period) + ",poisson=" +
             Boolean.toString(poisson) +")");
  }

  public boolean execute() {
    if (CDState.getCycle() % period != 0)
        return false;
    if (type.equals("hypha")) {
      candidates = MycoCast.getAllHyphae();
    } else if (type.equals("immobile")) {
      candidates = MycoCast.getImmobileHyphae();
    } else if (type.equals("branching")) {
      candidates = MycoCast.getBranchingHyphae();
    } else if (type.equals("extending")) {
      candidates = MycoCast.getExtendingHyphae();
    } else if (type.equals("biomass")) {
      candidates = MycoCast.getAllBiomass();
    } else {
      // Default to selecting from all nodes
      candidates = MycoCast.getAllNodes();
    }
    if (type.equals("largest")) {
      Collections.sort(candidates, degreeComparator);
    } else {
      Collections.shuffle(candidates);
    }

    log.finer("CHECKING WHETHER TO SHRINK OR EXPAND SET OF " +
              Integer.toString(candidates.size()) + " " +
              type.toUpperCase() +
              " (ADD=" + Double.toString(add) +
              ",MIN=" + Double.toString(minsize) +
              ",MAX=" + Double.toString(maxsize) +
              ",FROM=" + Double.toString(from) +
              ",TO=" + Double.toString(to) +
              ",SUBST=" + Boolean.toString(substitute) +
              ")");

    if (add == 0)
        return false;
    if (!substitute) {
      if ((maxsize <= Network.size() && add > 0)
          || (minsize >= Network.size() && add < 0))
          return false;
    }
    if (CDState.getCycle() < from || CDState.getCycle() > to) {
      return false;
    }
    int toadd = 0;
    int toremove = 0;
    if (add > 0) {
      toadd = (int) Math.round(add < 1 ? add * candidates.size() : add);
      if (!substitute && toadd > maxsize - candidates.size())
          toadd = maxsize - candidates.size();
      if (substitute)
          toremove = toadd;
    } else if (add < 0) {
      toremove =
          (int) Math.round(add > -1 ? -add * candidates.size() : -add);
      if (!substitute && toremove > candidates.size() - minsize)
          toremove = candidates.size() - minsize;
      if (substitute)
          toadd = toremove;
    }
    if (poisson) {
      toadd = gen.nextInt(toadd);
      toremove = gen.nextInt(toremove);
      if (substitute) { // Correct so identical, if necessary
        toadd = toremove;
      }
    }

    log.info("KILLING " + Integer.toString(toremove) + " NODES, ADDING " +
             Integer.toString(toadd) + " NODES");

    if (substitute) {
      log.fine("REPLACING " + toadd + " NODES");
      //replace(toadd);
      remove(toremove);
      add(toadd);
    } else {
      remove(toremove);
      add(toadd);
    }
    return false;
  }

  /**
   * Adds n nodes to the network. Extending classes can implement
   * any algorithm to do that. The default algorithm adds the given
   * number of nodes after calling all the configured initializers
   * on them.
   *
   * @param n
   *            the number of nodes to add, must be non-negative.
   */
  protected void add(int n) {
    System.out.println("Adding " + n + " nodes to the network");
    log.fine("Adding " + n + " nodes to the network");
    for (int i = 0; i < n; i++) {
      Node newnode = (Node) Network.prototype.clone();
      for (int j = 0; j < inits.length; ++j) {
        inits[j].initialize(newnode);
      }
      Network.add(newnode);
      log.finer("ADDED NEW NODE " + newnode.getID());
    }
  }

  // ------------------------------------------------------------------

  /**
   * Removes n nodes from the network. Extending classes can
   * implement any algorithm to do that. The default algorithm
   * removes <em>random</em> nodes <em>permanently</em> simply by
   * calling {@link Network#remove(int)}.
   *
   * @param n
   *            the number of nodes to remove
   */
  protected void remove(int n) {
    log.fine("Killing " + n + " " + type);
    System.out.println("Killing " + n + " " + type);

    for (int i = 0; i < n; i++) {
      // int pick = CommonState.r.nextInt(candidates.size());
      // log.finer("Picked " + candidates.get(pick) + " for death");
      int toKill = Network.findIndex(candidates.get(i));
      if (toKill >= 0) {
        log.finer(candidates.get(i) + " has index " + toKill);
        Network.remove(toKill);
        // log.finer("KILLED " + toKill.getID());
      } else {
        log.finer("Couldn't find index for " + candidates.get(i));
      }
      MycoCast.kill(candidates.get(i));
    }
    /*
     * for (int i = 0; i < n; ++i) { int toKill =
     * CommonState.r.nextInt(Network.size());
     *
     * Network.remove(toKill); }
     */
  }

  protected void replace(int n) {
    log.fine("Replacing " + n + " " + type);
    for (int i = 0; i < n; i++) {
      int toReplace = Network.findIndex(candidates.get(i));
      MycoNode node = (MycoNode) Network.get(toReplace);
      //Network.remove(toReplace);

      //for (MycoNode node : myNode.getHyphaLink().getNeighbors()) {
        //node.getHyphaLink().onKill();
        // node.getFailureAlerter().neighborFailed(myNode,
        //         myNode.getHyphaData().getState(),
        //         myNode.getHyphaLink().degree());
        // myNode.getHyphaLink().removeNeighbor(node);
      //}

      // Call onKill() for all protocols on the node to ensure proper operation
      System.out.println("Killing " + node);
      node.setFailState(Fallible.DEAD);
      for (int j = 0; j < inits.length; ++j) {
        inits[j].initialize(node);
      }
      node.setFailState(Fallible.OK);
      //node.getHyphaData().clean();
      //node.getHyphaData().becomeBiomass(node);
      //MycoCast.become(myNode, HyphaType.BIOMASS);
    }
  }

}
