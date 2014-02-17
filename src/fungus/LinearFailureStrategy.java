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

public class LinearFailureStrategy implements FailureStrategy {
    private static final String PREFIX = "config.linearfailure.";
    private static final String PAR_SCALE = PREFIX + "scale";
    private static final String PAR_BULWARK_SCALE = PREFIX + "bulwark_scale";

    private double scale;
    private double bulwarkScale;

    public LinearFailureStrategy() {
        scale = Configuration.getDouble(PAR_SCALE);
        bulwarkScale = Configuration.getDouble(PAR_BULWARK_SCALE);
    }

  public Double apply(MycoNode thisNode, HyphaType thisType,
                      int thisDegree, MycoNode failedNode,
                      HyphaType failedType, int failedDegree) {
        if ((failedType == HyphaType.BIOMASS) ||
            (failedType == HyphaType.BULWARK)) {
            // For failed degree 1 neighbor (biomass), don't generate alert
            // For failed bulwark neighbor, don't generate alert

            return 0.0;
        }
        if (thisType != HyphaType.BULWARK) {
            //if (n.getHyphaData().getState() == HyphaType.EXTENDING) {
            //    return 1.0 * scale;
            //}
            return failedDegree * scale;
        } else {
            return failedDegree * bulwarkScale;
        }
    }
}
