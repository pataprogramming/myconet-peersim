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
import peersim.core.CommonState;

public class BasicBulwarkStrategy extends DynamicsStrategy {

  private static Logger log =
      Logger.getLogger(BasicBulwarkStrategy.class.getName());

  public void doDynamics(MycoNode node, HyphaData data, HyphaLink link) {
    MycoCast mycoCast = (MycoCast) node.getMycoCast();

    if (link.degree() < data.getBulwarkMin()) {
      log.log(Level.FINER, "Bulwark node " + node +
              " is under minimum neighbor count", node);
      MycoNode candidate = mycoCast.pickRandomNode();
      if (candidate == null) {
        log.log(Level.FINE, "Bulwark node " + node +
                " could not find candidate connection", node);
      } else {
        log.log(Level.FINER, "Bulwark node " + node +
                " connecting to " + candidate,
                new Object [] { node, candidate });
        link.addNeighbor(candidate);
      }
    } else if (link.degree() > data.getBulwarkMax()) {
      log.log(Level.FINER, "Bulwark node " + node +
              " is over maximum neighbor count", node);
      MycoNode candidate = link.getNeighbors().getRandom();
      if (candidate == null) {
        log.log(Level.FINE, "Bulwark node " + node +
                " could not find neighbor to drop!!!", node);
      } else {
        log.log(Level.FINER, "Bulwark node " + node +
                " dropping connection to " + candidate,
                new Object [] { node, candidate });
        link.removeNeighbor(candidate);
      }
    } else {
      log.log(Level.FINER, "Bulwark node " + node +
              " is within neighbor tolerances (" + link.degree() + ")",
              node);
    }
  }
}
