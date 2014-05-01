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

import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;

public class TwoClassesInitializer implements Control {

  private static final String FRACTION_OF_PRIMARY_NODES = "fraction_of_primary_nodes";
  private static final String CAPACITY_OF_PRIMARY_NODES = "capacity_of_primary_nodes";
  private static final String CAPACITY_OF_SECONDARY_NODES = "capacity_of_secondary_nodes";

  private double fractionOfPrimaryNodes;
  private int capacityOfPrimaryNodes;
  private int capacityOfSecondaryNodes;

  public TwoClassesInitializer(String name) {

    fractionOfPrimaryNodes = Configuration.getDouble(name + "."
                                                     + FRACTION_OF_PRIMARY_NODES);

    capacityOfPrimaryNodes = Configuration.getInt(name + "."
                                                  + CAPACITY_OF_PRIMARY_NODES);

    capacityOfSecondaryNodes = Configuration.getInt(name + "."
                                                    + CAPACITY_OF_SECONDARY_NODES);
  }

  public boolean execute() {

    int numberOfNodesOfTheFirstType = (int) Math
        .round((Network.size() * fractionOfPrimaryNodes));

    MycoNode n;
    HyphaData d;

    for (int i = 0; i < Network.size(); i++) {

      n = (MycoNode) Network.get(i);
      d = n.getHyphaData();

      if (i < numberOfNodesOfTheFirstType)
          d.setMax(capacityOfPrimaryNodes);
      else
          d.setMax(capacityOfSecondaryNodes);
    }

    return false;
  }

}
