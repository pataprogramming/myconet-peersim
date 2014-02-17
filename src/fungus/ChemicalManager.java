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
import java.util.Collections;

public class ChemicalManager implements CDProtocol, Cleanable {
  private static final String PAR_START_CYCLE = "start_cycle";
  private static final String PAR_PERIOD = "period";

  private static final String PAR_CHEMICALMANAGER_PROTO =
      "network.node.chemicalmanager_proto";

  private static int chemicalManagerPid;
  private static int startCycle;
  private static int period;

  private MycoNode myNode = null;

  private static Logger log =
      Logger.getLogger(ChemicalManager.class.getName());

  private Map<Class,Chemical> chemicals;

  public ChemicalManager(String prefix) {
    startCycle = Configuration.getInt(prefix + "." + PAR_START_CYCLE);
    period = Configuration.getInt(prefix + "." + PAR_PERIOD);
    chemicalManagerPid = Configuration.getPid(PAR_CHEMICALMANAGER_PROTO);
    chemicals = new HashMap<Class,Chemical>();
  }

  public double getConcentration(Class chemType) {
    Chemical chem = chemicals.get(chemType);
    if (chem == null) {
      return 0.0;
    } else {
      return chem.amount;
    }
  }

  public void setConcentration(Class chemType, double conc) {
    try {
      Chemical chem = (Chemical) chemType.newInstance();
      chem.setAmount(conc);
      chemicals.put(chemType, chem);
    } catch (InstantiationException e) {
      throw new RuntimeException(e);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  public Object clone() {
    ChemicalManager ret = null;
    try {
      ret = (ChemicalManager) super.clone();
      ret.chemicals = new HashMap<Class,Chemical>();
    } catch (CloneNotSupportedException e) {
      // Never happens
    }
    return ret;
  }

  public void wipe() {
    chemicals.clear();
  }

  @Override
  public void onKill() {
    wipe();
  }

  public MycoNode getMycoNode() {
    return myNode;
  }

  public void nextCycle(Node node, int pid) {
    myNode = (MycoNode) node;

    if (CDState.getCycle() < startCycle || CDState.getCycle() % period != 0)
        return;

    List<ChemicalManager> neighbors = new LinkedList<ChemicalManager>();

    for (MycoNode n : myNode.getHyphaLink().getNeighbors()) {
      neighbors.add((ChemicalManager) n.getProtocol(chemicalManagerPid));
    }
    Collections.shuffle(neighbors);
    for (Chemical c : chemicals.values()) {
      c.doDynamics(this, neighbors);
    }
    for (Chemical c : chemicals.values()) {
      if (c.amount == 0.0) { chemicals.remove(c.getClass()); }
    }
  }

  public void add(Chemical c) {
    Class chemType = c.getClass();
    Chemical current = chemicals.get(chemType);

    double newAmount;
    if (current != null) {
      newAmount = current.amount + c.amount;
      chemicals.get(chemType).addAmount(newAmount);
    } else {
      current = c;
      newAmount = current.amount;
      chemicals.put(chemType,c);
    }
    log.log(Level.FINER, "Local concentration of " + chemType.getName() +
            " is now " + newAmount, myNode);
  }

  public Chemical extract(Class chemType, double quantity) {
    Chemical c = chemicals.get(chemType);

    if (c == null) {
      try {
        return (Chemical) chemType.newInstance();
      } catch (InstantiationException e) {
        throw new RuntimeException(e);
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      }
    } else {
      return c.extract(quantity);
    }
  }

  public void receive(Chemical c, MycoNode from) {
    log.log(Level.FINE, myNode +  " receiving " + c + " from " + from,
            new Object[] { myNode, from });
    add(c);
  }

  public void send(Chemical c, ChemicalManager to) {
    Class chemType = c.getClass();
    Chemical current = chemicals.get(chemType);

    // Local amount of chemical should already have been
    // received before send() is called
    log.log(Level.FINE, "Sending " + c + " from " +
            myNode + " to " + to.getMycoNode(),
            new Object[] { myNode, to.getMycoNode() });
    to.receive(c, myNode);
  }

  public void send(Chemical c, MycoNode to) {
    send(c, (ChemicalManager) to.getProtocol(chemicalManagerPid));
  }

}
