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

import peersim.cdsim.*;
import peersim.config.*;
import peersim.core.*;
import java.util.*;
import java.util.logging.*;
import org.apache.commons.collections15.*;
import edu.uci.ics.jung.graph.*;
import edu.uci.ics.jung.graph.util.*;

public class JobProcessor implements CDProtocol {
    private static final String PAR_HYPHADATA_PROTO = "hyphadata_proto";

    private static int hyphaDataPid;

    private MycoNode myNode = null;

    private static Logger log = Logger.getLogger(JobProcessor.class.getName());

    public JobProcessor(String prefix) {
        hyphaDataPid = Configuration.getPid(prefix + "." + PAR_HYPHADATA_PROTO);
    }

    public Object clone() {
        JobProcessor ret = null;
        try {
            ret = (JobProcessor) super.clone();
        } catch (CloneNotSupportedException e) {
            // Never happens
        }
        return ret;
    }

    public void nextCycle(Node node, int pid)
    {
        HyphaData data = (HyphaData) node.getProtocol(hyphaDataPid);

        myNode = (MycoNode) node;

        if (!myNode.isUp()) {
            return;
        }
        log.log(Level.FINER, "Node "  + myNode.getID() + " ready to perform " +
                data.getMaxCapacity() + " unit(s) of work", myNode);
        data.doWork(data.getMaxCapacity());
    }

}
