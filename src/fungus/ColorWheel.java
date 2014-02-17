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

import java.util.*;
import java.awt.Color;

import peersim.core.*;

public class ColorWheel {
    private static Random r = new java.util.Random(System.currentTimeMillis());

    // n colors equally distributed around the HSB color wheel for an S and B
    public static List<Color> getColorMap(int n, float s, float b) {
        List<Color> ret = new ArrayList<Color>();
        float step = 1.0f / (float) n;

        float h = r.nextFloat();
        for (int i = 0; i < n; i++) {
            ret.add(Color.getHSBColor(h, s, b));
            h += step;
        }

        return ret;
    }
}

