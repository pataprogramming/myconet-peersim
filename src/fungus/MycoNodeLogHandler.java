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

import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;
import java.util.logging.*;
import javax.swing.JTextArea;
import javax.swing.event.*;
import com.google.common.collect.Iterables;

public class MycoNodeLogHandler extends Handler {
    private static LogManager manager = LogManager.getLogManager();
    private static Logger rootLogger = Logger.getLogger("fungus");

    private MycoNode n;
    private Formatter formatter;
    private JTextArea textArea;

    private Set<ChangeListener> changeListeners;


    public MycoNodeLogHandler(MycoNode n, JTextArea textArea) {
        this.n = n;
        this.textArea = textArea;
        this.formatter = new MiniFormatter();
        this.changeListeners = new HashSet<ChangeListener>();
        this.setFormatter(formatter);
        this.setFilter(makeFilter(n));
        this.setLevel(Level.FINER);
        rootLogger.addHandler(this);
    }

    public void addChangeListener(ChangeListener cl) {
        changeListeners.add(cl);
    }

    public void removeChangeListener(ChangeListener cl) {
        if (changeListeners.contains(cl)) {
            changeListeners.remove(cl);
        }
    }


    public void update(String message) {
        textArea.append(message);
        textArea.setCaretPosition(textArea.getDocument().getLength());
        for (ChangeListener cl : changeListeners) {
            cl.stateChanged(new ChangeEvent(this));
        }
    }

    public void publish(LogRecord r) {
        String message = null;
        if (!isLoggable(r)) {
            return;
        }
        try {
            message = getFormatter().format(r);
            update(message);
        } catch (Exception e) {
            reportError(null, e, ErrorManager.FORMAT_FAILURE);
        }
    }

    public void flush() {
    }

    public void close() {
    }

    private Filter makeFilter(final MycoNode n) {
        return new Filter() {
            public boolean isLoggable(LogRecord r) {

                if (r.getParameters() == null) {
                                  return false;
                }
                return Arrays.asList(r.getParameters()).contains(n);
            }
        };
    }
}