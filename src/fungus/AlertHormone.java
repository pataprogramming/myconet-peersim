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

public class AlertHormone extends Chemical {

  private static final String PAR_DIFFUSION_RATE =
      "config.alert.diffusion_rate";
  private static final String PAR_DECAY_RATE =
      "config.alert.decay_rate";
  private static final String PAR_MAX_CONCENTRATION =
      "config.alert.max_concentration";

  private static double diffusionRate;
  private static double decayRate;
  private static double maxConcentration;

  private boolean initialized = false;
  public AlertHormone(double amount) {
    super();
    name = "AlertHormone";

    if (!initialized) {
      diffusionRate = Configuration.getDouble(PAR_DIFFUSION_RATE);
      decayRate = Configuration.getDouble(PAR_DECAY_RATE);
      maxConcentration = Configuration.getDouble(PAR_MAX_CONCENTRATION);
    }
    this.setAmount(amount);
  }


  public AlertHormone() {
    this(0.0);
  }


  public double getDiffusionRate() { return diffusionRate; }
  public double getDecayRate() { return decayRate; }
  public double getMaxConcentration() { return maxConcentration; }

  public Object clone() {
    AlertHormone ret = new AlertHormone(amount);

    return ret;

  }
}
