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

import java.util.logging.Level;
import java.util.logging.Logger;

import peersim.config.Configuration;

public class MirrorBulwarkJoinStrategy extends JoinStrategy {
  private static final String PAR_CHEMICALMANAGER_PROTO =
      "network.node.chemicalmanager_proto";

  private static int chemicalManagerPid;

  private static Logger log =
      Logger.getLogger(MirrorBulwarkJoinStrategy.class.getName());

  public MirrorBulwarkJoinStrategy() {
    chemicalManagerPid = Configuration.getPid(PAR_CHEMICALMANAGER_PROTO);
  }

  public void doJoin(MycoNode entering, MycoNode connected) {
    HyphaData eData = entering.getHyphaData();
    HyphaData cData = connected.getHyphaData();

    ChemicalManager eManager =
        ((ChemicalManager) entering.getProtocol(chemicalManagerPid));;
    ChemicalManager cManager =
        ((ChemicalManager) connected.getProtocol(chemicalManagerPid));

    if (cData.getState() == HyphaType.BULWARK) {
      // If the node we connect to is in bulwark state, become bulwark
      // ourself
      eData.become(entering, HyphaType.BULWARK);
    }

    // start with an equivalent amount of alert hormone
    double ah = cManager.getConcentration(AlertHormone.class);
    eManager.add(new AlertHormone(ah));

    // Mirror parent target (for SemiBulwarkStrategy)
    double pt = cData.getParentTarget();
    eData.setParentTarget(pt);
  }
}
