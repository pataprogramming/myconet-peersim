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
import java.util.concurrent.*;
import java.util.logging.*;
import java.io.*;
//import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import edu.uci.ics.jung.algorithms.importance.*;
import edu.uci.ics.jung.algorithms.layout.*;
import edu.uci.ics.jung.algorithms.shortestpath.*;
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

public class MessageObserver implements Control {

  public static long gossipCurrent = 0;
  public static long loadQueryCurrent = 0;
  public static long loadActionCurrent = 0;
  public static long topoQueryCurrent = 0;
  public static long topoActionCurrent = 0;

  public static long gossipCount = 0;
  public static long loadQueryCount = 0;
  public static long loadActionCount = 0;
  public static long topoQueryCount = 0;
  public static long topoActionCount = 0;

  public static long gossipTotal = 0;
  public static long loadQueryTotal = 0;
  public static long loadActionTotal = 0;
  public static long topoQueryTotal = 0;
  public static long topoActionTotal = 0;

  public MessageObserver(String name) {
    clearStats();

    ExperimentWriter.addMetric(new Metric<Long>("mycoMessagesCount") {
        public Long fetch() { return MessageObserver.mycoCount(); }});
    ExperimentWriter.addMetric(new Metric<Long>("mycoMessagesTotal") {
        public Long fetch() { return MessageObserver.mycoTotal(); }});
  }

  public static void gossipMessages(int count) {
    gossipCount += count;
    gossipTotal += count;
  }

  public static void loadQueryMessages(int count) {
    loadQueryCount += count;
    loadQueryTotal += count;
  }

  public static void loadActionMessages(int count) {
    loadActionCount += count;
    loadActionTotal += count;
  }

  public static void topoQueryMessages(int count) {
    topoQueryCount += count;
    topoQueryTotal += count;
  }

  public static void topoActionMessages(int count) {
    topoActionCount += count;
    topoActionTotal += count;
  }

  public static long mycoCount() {
    return gossipCurrent + topoQueryCurrent + topoActionCurrent;
  }

  public static long loadCount() {
    return loadQueryCurrent + loadActionCurrent;
  }

  public static long mycoTotal() {
    return gossipTotal + topoQueryTotal + topoActionTotal;
  }

  public static long loadTotal() {
    return loadQueryTotal + loadActionTotal;
  }

  private static void clearStats() {
    gossipCurrent = gossipCount;
    loadQueryCurrent = loadQueryCount;
    loadActionCurrent = loadActionCount;
    topoQueryCurrent = topoQueryCount;
    topoActionCurrent = topoActionCount;

    gossipCount = 0;
    loadQueryCount = 0;
    loadActionCount = 0;
    topoQueryCount = 0;
    topoActionCount = 0;
  }

  private static Logger log =
      Logger.getLogger(MessageObserver.class.getName());


  public boolean execute() {
    log.info("Message counts (total) - Myco: " + mycoCount()
             + " (" + mycoTotal() + ") - Load: " + loadCount()
             + " (" + loadTotal() + ")");
    clearStats();
    return false;
  }

}
