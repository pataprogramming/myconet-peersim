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
import peersim.core.*;
import peersim.util.*;
import java.util.*;
import java.util.logging.*;

import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import edu.uci.ics.jung.algorithms.importance.*;
import edu.uci.ics.jung.algorithms.layout.*;
import edu.uci.ics.jung.algorithms.util.*;
import edu.uci.ics.jung.graph.*;
import edu.uci.ics.jung.graph.util.*;
import edu.uci.ics.jung.visualization.*;
import edu.uci.ics.jung.visualization.control.*;
import edu.uci.ics.jung.visualization.decorators.*;
import edu.uci.ics.jung.visualization.picking.*;
import edu.uci.ics.jung.visualization.renderers.*;
import org.apache.commons.collections15.*;
import org.apache.commons.collections15.functors.*;

public class InfoFrameVertexListener implements GraphMouseListener<MycoNode> {
  public void graphClicked(MycoNode v, MouseEvent e) {
    JFrame f = new MycoNodeFrame(v);
  }
  public void graphPressed(MycoNode v, MouseEvent e) { }
  public void graphReleased(MycoNode v, MouseEvent e) { }
}
