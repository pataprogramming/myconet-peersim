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

import java.lang.Math;
import java.util.logging.Level;
import java.util.logging.Logger;

import peersim.core.CommonState;

public class TotalFailureParentStrategy extends ParentStrategy {

  private static Logger log =
      Logger.getLogger(TotalFailureParentStrategy.class.getName());

  // public void applyFailure(MycoNode thisNode, HyphaType thisType,
  //                          int thisDegree, MycoNode failedNode,
  //                          HyphaType failedType, int failedDegree) {
  //   HyphaData thisData = thisNode.getHyphaData();
  //   HyphaLink thisLink = thisNode.getHyphaLink();
  //   double oldParents = thisData.getParentTarget();

  //   if (thisDegree < 1) {
  //     double newParents = oldParents + 1;
  //     thisData.setParentTarget(newParents);
  //     log.log(Level.FINE, "All neighbors failed, increasing parent target " +
  //             "from " + oldParents + " to " + newParents, thisNode);
  //   }
  // }

  public double sigmoidDecayProb(double target, double stretch, double shift) {
    return 1.0 / (1.0 + Math.pow(Math.E, -((target / stretch) + shift)));
  }

  @Override
  public void apply(MycoNode thisNode) {
    HyphaData thisData = thisNode.getHyphaData();
    double oldParents = thisData.getParentTarget();

    if (thisData.wasDisconnectDetected()) {
      double newParents = oldParents + 1;
      thisData.setParentTarget(newParents);
      //log.log(Level.INFO, "All neighbors failed, increasing parent target " +
      //        "from " + oldParents + " to " + newParents, thisNode);
      //// } else ((oldParents > 1) && (CommonState.r.nextDouble() < 0.25)) {
      ////     int newParents = oldParents - 1;
      ////     thisData.setParentTarget(newParents);
      ////     log.log(Level.FINE, "No disconnect, decreasing parent target " +
      ////           "from " + oldParents + " to " + newParents, thisNode);
      //// }
    } else if (thisData.getKnownDisconnect() > thisData.getParentTarget()) {
      double newParents = oldParents + 1;
      thisData.setParentTarget(newParents);
      // log.log(Level.INFO, "Failed node reconnected in 2-neighborhood, " +
      //         "increasing parent target from " + oldParents + " to " +
      //         newParents, thisNode);
    } else if (thisData.getKnownDisconnect() < thisData.getParentTarget() - 1) {
      //} else {
      //double decayProb = 0.15;
      double decayProb = sigmoidDecayProb(oldParents, 2.5, -3.0);
      double decay = 0.15;
      if (CommonState.r.nextDouble() < decayProb) {
        double newParents = oldParents;
        //newParents = Math.max(1.0, newParents * (1.0 - decay));
        if (oldParents > 1.0) {
          newParents = Math.max(1.0, newParents - 1.0);
        }
        log.log(Level.FINER, "No disconnect, old parent target " +
                "was " + oldParents + ", new is " + newParents, thisNode);
        thisData.setParentTarget(newParents);
      }
    }
  }
}
