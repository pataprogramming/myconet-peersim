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
import peersim.core.CommonState;

public class MultiplicativeAdaptationStrategy
  implements ReversionAdaptationStrategy
{
  public final String prefix = "config.multadapt.";

  public boolean initialized = false;
  public static double upMult;
  public static double downMult;
  public static int cooldown;
  public static double noise;

  public MultiplicativeAdaptationStrategy() {
    if (initialized == false) {
      upMult = Configuration.getDouble(prefix + "up");
      downMult = Configuration.getDouble(prefix + "down");
      cooldown = Configuration.getInt(prefix + "cooldown");
      noise = Configuration.getDouble(prefix + "noise");
      initialized = true;
    }
  }

  public double addNoise(double val) {
    // I.e., noise of 0.1 means that the multiplier varies by +/-10%
    return val + ((CommonState.r.nextDouble() * noise * 2) - noise);
  }

  public int adaptDelay(int oldDelay, int elapsed, boolean alerting) {
    int newDelay;

    if (alerting) {
      // Entering Bulwark state; if too soon, lengthen delay
      if (elapsed <= cooldown) {
        newDelay = (int) (oldDelay * upMult);
      } else {
        newDelay = oldDelay;
      }
    } else {
      // De-exciting after Bulwark, shorten delay for next time
      newDelay = (int) (oldDelay * addNoise(downMult));
      //newDelay =oldDelay;
    }

    if (newDelay < 0) { newDelay = 0; }
    return newDelay;
  }
}
