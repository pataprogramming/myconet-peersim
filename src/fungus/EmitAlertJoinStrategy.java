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

public class EmitAlertJoinStrategy extends JoinStrategy {
  private static final String PAR_FAILUREALERTER_PROTO =
      "network.node.failurealerter_proto";

  private static int failureAlerterPid;

  private static Logger log =
      Logger.getLogger(EmitAlertJoinStrategy.class.getName());

  public EmitAlertJoinStrategy() {
    failureAlerterPid = Configuration.getPid(PAR_FAILUREALERTER_PROTO);
  }

  public void doJoin(MycoNode entering, MycoNode connected) {
    HyphaData eData = entering.getHyphaData();
    HyphaData cData = connected.getHyphaData();

    FailureAlerter cAlerter =
        ((FailureAlerter) connected.getProtocol(failureAlerterPid));

    if (eData.wasDisconnectDetected()) {
      cAlerter.announceReconnect(eData.getParentTarget());
    } else {
      double pt = cData.getParentTarget();
      eData.setParentTarget(pt);
    }
    // Mirror parent target (for SemiBulwarkStrategy)
  }
}
