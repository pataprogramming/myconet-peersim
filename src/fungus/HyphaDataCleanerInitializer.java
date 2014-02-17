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

import java.util.Random;
import java.lang.Math;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Network;
import peersim.core.Node;
import peersim.core.Control;
import peersim.dynamics.NodeInitializer;

//import peersim.util.*;
import java.util.logging.Logger;
import java.util.logging.LogManager;

public class HyphaDataCleanerInitializer implements Control, NodeInitializer {

  public static String name;

  public HyphaDataCleanerInitializer(String name) {
    this.name = name;
  }

  public static void initialize(MycoNode n) {
    HyphaData d = n.getHyphaData();
    d.clean();
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
