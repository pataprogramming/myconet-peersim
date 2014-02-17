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

import peersim.core.Control;
import peersim.core.CommonState;
import peersim.core.Network;
import peersim.config.Configuration;

import java.util.logging.Level;
import java.util.logging.Logger;

public class RandomChemicalGenerator implements Control {

  private static final String PAR_CHEMICALMANAGER_PROTO =
      "network.node.chemicalmanager_proto";

  private static int chemicalManagerPid;

  private static int count = 4;
  private static double magnitude = 100.0;

  private static Logger log =
      Logger.getLogger(RandomChemicalGenerator.class.getName());

  public RandomChemicalGenerator(String prefix) {
    chemicalManagerPid = Configuration.getPid(PAR_CHEMICALMANAGER_PROTO);
  }

  public MycoNode pickRandomNode() {
    return (MycoNode) Network.get(CommonState.r.nextInt(Network.size()));
  }

  public boolean execute() {
    for (int i = 0; i < count; i++) {
      MycoNode target = pickRandomNode();
      ChemicalManager cm =
          (ChemicalManager) target.getProtocol(chemicalManagerPid);

      Chemical c = new AlertHormone(magnitude);
      log.log(Level.INFO, "Introducing " + c + " at " + target, target);
      cm.add(c);
    }
    return false;
  }

}
