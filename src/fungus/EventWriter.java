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

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.util.zip.GZIPOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Formatter;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.logging.Logger;

import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.CommonState;
import peersim.core.Network;
import peersim.cdsim.CDState;

public class EventWriter implements Control, HyphaDataListener,
                                    HyphaLinkListener, CleanupListener {
  private static final String PAR_BASENAME = "config.basename";
  private static final String PAR_CHEMICALMANAGER_PROTO =
      "network.node.chemicalmanager_proto";

  private static int chemicalManagerPid;

  private static Logger log =
      Logger.getLogger(EventWriter.class.getName());

  private final String name;
  private final String basename;

  private final String dirname = "events";
  private final String filename;

  private static PrintWriter p;

  public EventWriter(String name) {
    this.name = name;
    chemicalManagerPid = Configuration.getPid(PAR_CHEMICALMANAGER_PROTO);
    this.basename = Configuration.getString(PAR_BASENAME);

    File dir = new File("events");
    if (!dir.exists()) {
      dir.mkdir();
    }

    StringBuffer fn = new StringBuffer();
    Formatter f = new Formatter(fn, Locale.US);
    f.format("%s/events-%s-%d.txt", dir.getPath(), basename,
             CommonState.seed);
    this.filename = fn.toString();

    try {
      FileWriter out = new FileWriter(filename, true);
      //GZIPOutputStream gzout = new GZIPOutputStream(out);
      p = new PrintWriter(out);
    } catch (IOException e) {
      log.severe("Couldn't open file " + filename + " for writing");
    }
    p.println("(experiment " + basename + " " + CommonState.seed + ")");

    HyphaLink.addHyphaLinkListener(this);
    HyphaData.addHyphaDataListener(this);
    CleanupControl.addCleanupListener(this);
  }

  public boolean execute() {
    p.println("(cycle " + CDState.getCycle() + ")");
    p.print("(hormone {");
    for (int i = 0; i < Network.size(); i++) {
      MycoNode n = (MycoNode) Network.get(i);
      p.print(n.getID() + " " +
              ((ChemicalManager) n.getProtocol(chemicalManagerPid))
              .getConcentration(AlertHormone.class)
              + ", ");
    }
    p.println("})");
    p.flush();
    return false;
  }

  public void cleanup() {
    p.close();
  }

  public void nodeStateChanged(MycoNode n, HyphaType t, HyphaType oldState) {
    p.println("(n " + n.getID() + " :s " + t.toString()
              + " :c " + n.getHyphaData().getCapacity() + " :t "
              + n.getHyphaData().getType());
  }

  public void linkAdded(MycoNode a, MycoNode b) {
    p.println("(e+ " + a.getID() + " " + b.getID() + ")");
  }

  public void linkRemoved(MycoNode a, MycoNode b) {
    p.println("(e- " + a.getID() + " " + b.getID() + ")");
  }
}
