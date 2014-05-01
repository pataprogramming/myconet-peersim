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

import java.util.HashSet;
import java.util.Set;
import java.util.Arrays;
import java.util.logging.*;
import java.lang.reflect.Field;

import peersim.core.Control;
import peersim.config.Configuration;

public class LogObserver implements Control {
  private static final String PAR_CONSOLE_LEVEL = "console_level";
  private static final String PAR_LOG_LEVEL = "log_level";

  private static LogManager manager = LogManager.getLogManager();
  private static Logger log = Logger.getLogger("fungus");

  private final String name;
  private final Level consoleLevel;
  private final Level logLevel;
  //private final int pid;

  private static Set<String> classSet;

  private static Filter classFilter = new Filter() {
      public boolean isLoggable(LogRecord record) {
        if (classSet.contains(record.getSourceClassName()))
            return true;
        else
            return false;
      }
    };

  public LogObserver(String name) {
    this.name = name;

    String ll = Configuration.getString(name + "." + PAR_LOG_LEVEL);
    logLevel = Level.parse(ll);
    String cl = Configuration.getString(name + "." + PAR_CONSOLE_LEVEL);
    consoleLevel = Level.parse(cl);

    String [] names = Configuration.getNames(name + ".classes");

    classSet = new HashSet<String>(names.length);

    for (String n : names) {
      classSet.add(Configuration.getClass(n).getName());
    }

    Formatter formatter = new MiniFormatter();
    Handler console = new ConsoleHandler();
    log.addHandler(console);
    console.setFormatter(formatter);

    console.setLevel(consoleLevel);
    log.setLevel(logLevel);

    if (classSet.size() > 0) {
      log.info("Only logging events from: " + classSet);
      console.setFilter(classFilter);
    }
    //this.name = name;
    ///this.pid = Configuration.getPid(name + "." + PAR_PROTO);
  }

  public boolean execute() {
    return false;
  }
}
