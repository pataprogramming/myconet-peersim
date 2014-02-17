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

import peersim.config.*;

public class FlatMetabolismStrategy implements MetabolismStrategy {
  private static final String PREFIX = "config.metabolism.";
  private static final String PAR_NORMAL_RATE = PREFIX + "normal_rate";
  private static final String PAR_BULWARK_RATE = PREFIX + "bulwark_rate";
  private static final String PAR_CHEMICALMANAGER_PROTO =
      "network.node.chemicalmanager_proto";


  private double normalRate;
  private double bulwarkRate;
  private static int chemicalManagerPid;

  public FlatMetabolismStrategy() {
    normalRate = Configuration.getDouble(PAR_NORMAL_RATE);
    bulwarkRate = Configuration.getDouble(PAR_BULWARK_RATE);
    chemicalManagerPid = Configuration.getPid(PAR_CHEMICALMANAGER_PROTO);
  }

  public double apply(MycoNode n) {
    ChemicalManager cm = (ChemicalManager) n.getProtocol(chemicalManagerPid);
    double ah = cm.getConcentration(AlertHormone.class);
    double reduction;
    if (n.getHyphaData().getState() == HyphaType.BULWARK) {
      reduction = ah * bulwarkRate;
    } else {
      reduction = ah * normalRate;
    }
    return cm.extract(AlertHormone.class, reduction).amount;
  }
}
