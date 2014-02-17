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
import java.io.BufferedWriter;
import java.io.PrintWriter;
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

public class GraphWriter implements Control {
  private static final String PAR_BASENAME = "config.basename";
  private static final String PAR_CHEMICALMANAGER_PROTO =
      "network.node.chemicalmanager_proto";

  private static int chemicalManagerPid;
  private static Logger log =
      Logger.getLogger(GraphWriter.class.getName());

  private final String name;
  private final String basename;

  private final String dirname = "graph";
  private final String filename;

  public GraphWriter(String name) {
    this.name = name;

    this.basename = Configuration.getString(PAR_BASENAME);
    chemicalManagerPid = Configuration.getPid(PAR_CHEMICALMANAGER_PROTO);

    File graphDir = new File("graph");
    if (!graphDir.exists()) {
      graphDir.mkdir();
    }

    StringBuffer fn = new StringBuffer();
    Formatter f = new Formatter(fn, Locale.US);
    f.format("%s/graph-%s-%d.txt", graphDir.getPath(), basename,
             CommonState.seed);
    this.filename = fn.toString();
  }

  public boolean execute() {
    try {
      FileWriter out = new FileWriter(filename, true);
      //GZIPOutputStream gzout = new GZIPOutputStream(out);
      PrintWriter p = new PrintWriter(out);
      p.print("{");
      for (int i = 0; i < Network.size(); i++) {
        MycoNode n = (MycoNode) Network.get(i);
        p.print(n.getID());
        p.print(" {:s " + n.getHyphaData().getState());
        p.print(" :c ");
        p.print(n.getHyphaData().getCapacity());
        p.print(" :h ");
        p.print(((ChemicalManager) n.getProtocol(chemicalManagerPid))
                .getConcentration(AlertHormone.class));
        p.print(" :l #{");
        for (MycoNode o : n.getHyphaLink().getNeighbors()) {
          p.print(o.getID());
          p.print(" ");
        }
        p.print("}} ");
      }
      p.println("}");
      p.flush();
      out.close();
    } catch (IOException e) {
      log.severe("Couldn't open file " + filename + " for writing");
    }
    return false;
  }
}
