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
import peersim.config.*;
import peersim.core.*;
import peersim.util.*;
import peersim.dynamics.NodeInitializer;
import java.util.logging.*;
import cern.jet.random.engine.*;
import cern.jet.random.Distributions.*;

public class StateInitializer implements Control, NodeInitializer {
  public static final String PAR_INITIAL_STATE = "initial_state";

  private static Logger log =
      Logger.getLogger(StateInitializer.class.getName());
  private final String name;
  private static HyphaType initialState;

  public StateInitializer(String name) {
    this.name = name;
    if (initialState == null) {
      String stateName =
          Configuration.getString(name + "." + PAR_INITIAL_STATE);
      try {
        initialState = Enum.valueOf(HyphaType.class, stateName);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  public static void initialize(MycoNode n) {
    n.getHyphaData().become(n, initialState);
  }

  public void initialize(Node n) {
    initialize((MycoNode) n);
  }

  public boolean execute() {
    MycoNode n;
    for (int i = 0; i < Network.size(); i++) {
      n = (MycoNode) Network.get(i);
      initialize(n);
    }
    return false;
  }
}
