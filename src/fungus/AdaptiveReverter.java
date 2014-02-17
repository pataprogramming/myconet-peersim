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

import java.util.logging.Logger;

import peersim.cdsim.CDProtocol;
import peersim.core.Node;
import peersim.config.Configuration;

public class AdaptiveReverter implements CDProtocol, HyphaDataListener {
  private static final String PAR_ADAPTATION_STRATEGY =
      "adaptation_strategy";

  private static int adaptiveReverterPid;

  private static Class adaptationClass;
  private static ReversionAdaptationStrategy strategy;

  private static Logger log =
      Logger.getLogger(AdaptiveReverter.class.getName());

  private MycoNode myNode;
  private HyphaType lastState;

  private int elapsed; // Successive cycles spent in non-bulwark state

  public AdaptiveReverter(String prefix) {
    lastState = HyphaType.BIOMASS;
    //adaptiveReverterPid = Configuration.getPid(PAR_ADAPTATION_STRATEGY);
    if (strategy == null) {
      try {
        adaptationClass =
            Configuration.getClass(prefix + "." + PAR_ADAPTATION_STRATEGY);
        if (ReversionAdaptationStrategy.class.isAssignableFrom(adaptationClass))
        {
          strategy =
              (ReversionAdaptationStrategy) adaptationClass.newInstance();
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }


  public Object clone() {
    AdaptiveReverter ret = null;
    try {
      ret = (AdaptiveReverter) super.clone();
      // FIXME: Will need to be updated if individual strategies aquire state
      ret.strategy = strategy;
    } catch (CloneNotSupportedException e) {
      // Never happens
    }
    return ret;
  }


  public void nodeStateChanged(MycoNode n, HyphaType t, HyphaType old) {
    HyphaData d = n.getHyphaData();
    if (t == HyphaType.BULWARK) {
      d.setIrevert(strategy.adaptDelay(d.getIrevert(), elapsed, true));
      elapsed = 0;
    } else if (old == HyphaType.BULWARK) {
      elapsed = 0;
      d.setIrevert(strategy.adaptDelay(d.getIrevert(), elapsed, false));
    }
  }


  public void nextCycle(Node node, int pid) {
    myNode = (MycoNode) node;
    elapsed += 1;

    HyphaType state = myNode.getHyphaData().getState();

    if (state != lastState) {
      nodeStateChanged(myNode, state, lastState);
    }
    lastState = state;
  }
}
