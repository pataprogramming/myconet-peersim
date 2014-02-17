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
import peersim.util.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.*;

import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import edu.uci.ics.jung.algorithms.importance.*;
import edu.uci.ics.jung.algorithms.layout.*;
import edu.uci.ics.jung.algorithms.transformation.*;
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

public class JungGraphObserver implements Control, HyphaDataListener,
                                          HyphaLinkListener {
    private static final String PAR_HYPHADATA_PROTO =
        "network.node.hyphadata_proto";
    private static final String PAR_HYPHALINK_PROTO =
        "network.node.hyphalink_proto";
    private static final String PAR_MYCOCAST_PROTO =
        "network.node.mycocast_proto";
    private static final String PAR_WALK_DELAY =
        "walk_delay";

    private static Logger log =
        Logger.getLogger(JungGraphObserver.class.getName());

    private static final String PAR_PERIOD = "period";
    private static int period;
    private static int walkDelay;

    private final String name;
    private final int hyphadataPid;
    private final int hyphalinkPid;
    private final int mycocastPid;

    //private static Lock = new ReentrantLock();

    private static MycoGraph graph = new MycoGraph();
    private static VisualizationViewer<MycoNode,MycoEdge> visualizer;

    private static Set<ChangeListener> changeListeners;

    //private class TypePredicate extends Predicate<{

    //}

    public static void addChangeListener(ChangeListener cl) {
        changeListeners.add(cl);
    }

    public static void removeChangeListener(ChangeListener cl) {
        if (changeListeners.contains(cl)) {
            changeListeners.remove(cl);
        }
    }

    private static Thread mainThread;
    public static boolean stepBlocked = false;
    public static boolean noBlock = true;
    public static boolean walking = false;

    public JungGraphObserver(String name) {
        this.name = name;
        this.hyphadataPid = Configuration.getPid(PAR_HYPHADATA_PROTO);
        this.hyphalinkPid = Configuration.getPid(PAR_HYPHALINK_PROTO);
        this.mycocastPid = Configuration.getPid(PAR_MYCOCAST_PROTO);
        this.period = Configuration.getInt(name + "." + PAR_PERIOD);

        this.walkDelay = Configuration.getInt(name + "." + PAR_WALK_DELAY);
        mainThread = Thread.currentThread();
        this.changeListeners = new HashSet<ChangeListener>();

        visualizer = null;

        //HyphaData.addHyphaDataListener(this);
        //HyphaLink.addHyphaLinkListener(this);
    }

    public static void
        setVisualizer(VisualizationViewer<MycoNode,MycoEdge> visualizer) {
        JungGraphObserver.visualizer = visualizer;
        addChangeListener(visualizer);
    }

    public static MycoGraph getGraph() {
        return graph;
    }

  public void nodeStateChanged(MycoNode n, HyphaType t, HyphaType oldState) {
        if (t != HyphaType.DEAD) {
            if (! graph.containsVertex(n)) {
                graph.addVertex(n);
            }
        } else {
            if (graph.containsVertex(n)) {
                graph.removeVertex(n);
            }
        }
    }

    public void linkAdded(MycoNode a, MycoNode b) {
        if (graph.findEdge(a, b) == null) {
            MycoEdge edge = new MycoEdge();
            graph.addEdge(edge, a, b, EdgeType.DIRECTED);
        }
    }

    public void linkRemoved(MycoNode a, MycoNode b) {
        MycoEdge edge = graph.findEdge(a, b);
        while (edge != null) {
            graph.removeEdge(edge);
            edge = graph.findEdge(a,b);
        }
    }

    public boolean execute() {
        if (CDState.getCycle() % period != 0)
                return false;

        MycoCast mycocast = (MycoCast) Network.get(0).getProtocol(mycocastPid);

        int bio = mycocast.countBiomass();
        int ext = mycocast.countExtending();
        int bra = mycocast.countBranching();
        int imm = mycocast.countImmobile();


        // Update vertices
        Set<MycoNode> activeNodes = new HashSet<MycoNode>();
        for (int i = 0; i < Network.size(); i++) {
            MycoNode n = (MycoNode) Network.get(i);
            activeNodes.add(n);
            HyphaData data = n.getHyphaData();
            //if (data.isBiomass()) { continue; }
            if (graph.containsVertex(n)) {
                graph.removeVertex(n);
            }
            if (!graph.containsVertex(n)) {
                graph.addVertex(n);
            }
        }
        Set<MycoNode> jungNodes =
            new HashSet<MycoNode>(graph.getVertices());
        jungNodes.removeAll(activeNodes);

        for (MycoNode n : jungNodes) {
            graph.removeVertex(n);
        }

        // Update edges
        for (int i = 0; i < Network.size(); i++) {
            MycoNode n = (MycoNode) Network.get(i);
            HyphaData data = n.getHyphaData();
            HyphaLink link = n.getHyphaLink();

            synchronized (graph) {

                // We now add in all links and tune out display in Visualizer
                java.util.List<MycoNode> neighbors =
                    (java.util.List<MycoNode>) link.getNeighbors();

                //// Adding only links to hypha thins out links to biomass
                    //    (java.util.List<MycoNode>) link.getHyphae();

                Collection<MycoNode> jungNeighbors = graph.getNeighbors(n);

                // Remove edges from Jung graph that are not in peersim graph
                for (MycoNode o : jungNeighbors) {
                    if (!neighbors.contains(o)) {
                        MycoEdge edge = graph.findEdge(n,o);
                        while (edge != null) {
                            graph.removeEdge(edge);
                            edge = graph.findEdge(n,o);
                        }
                    }
                }

                // Add missing edges to Jung graph that are in peersim graph
                for (MycoNode o : neighbors) {
                    if (graph.findEdge(n,o) == null) {
                        MycoEdge edge = new MycoEdge();
                        graph.addEdge(edge, n, o, EdgeType.DIRECTED);
                    }
                }
            }

            //log.finest("VERTICES: " + graph.getVertices());
            //log.finest("EDGES: " + graph.getEdges());
        }


        for (ChangeListener cl : changeListeners) {
            cl.stateChanged(new ChangeEvent(graph));
        }
        if (walking) {
            try {
                Thread.sleep(walkDelay);
            } catch (InterruptedException e) {}
            stepBlocked = false;
        }

        try {
            while (stepBlocked && !noBlock) {
                synchronized(JungGraphObserver.class) {
                    JungGraphObserver.class.wait();
                }
            }
        } catch (InterruptedException e) {
            stepBlocked = true;
        }
        stepBlocked = true;
        //System.out.println(graph.toString());
        return false;
    }

    public static synchronized  void stepAction() {
        walking = false;
        stepBlocked = false;
        noBlock = false;
        JungGraphObserver.class.notifyAll();
    }

    public static synchronized void walkAction() {
        walking = true;
        stepBlocked = false;
        noBlock = false;
        JungGraphObserver.class.notifyAll();
    }

    public static synchronized void pauseAction() {
        walking = false;
        stepBlocked = true;
        noBlock = false;
    }

    public static synchronized void runAction() {
        walking = false;
        stepBlocked = false;
        noBlock = true;
        JungGraphObserver.class.notifyAll();
    }
}
