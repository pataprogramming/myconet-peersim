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

import edu.uci.ics.jung.algorithms.filters.*;
import edu.uci.ics.jung.algorithms.shortestpath.*;
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

public class MycoGraph extends DirectedSparseGraph<MycoNode,MycoEdge> {
  private static List<VertexTypeFilter> filters =
      new ArrayList<VertexTypeFilter>();

  private static Logger log =
      Logger.getLogger(MycoGraph.class.getName());

  public static VertexTypeFilter getFilter(int t) {
    log.fine("looking for filter " + t);
    // If predicates aren't created yet, create and cache
    while (t >= filters.size()) {
      filters.add(new VertexTypeFilter(filters.size()));
    }
    return filters.get(t);
  }

  // Get List of subgraphs; subgraph i consists only of nodes of type i
  public List<Graph<MycoNode,MycoEdge>> getTypeGraphs() {
    List<Graph<MycoNode,MycoEdge>> ret =
        new ArrayList<Graph<MycoNode,MycoEdge>>(HyphaData.numTypes);
    for (int t = 0; t < HyphaData.numTypes; t++) {
      VertexTypeFilter f = getFilter(t);
      ret.add(f.transform(this));
    }
    log.finer("Returning " + ret.size() + " type graphs");
    return ret;
  }

  public static Factory<Forest<MycoNode,MycoEdge>> typeForestFactory =
      new Factory<Forest<MycoNode,MycoEdge>>() {
        public Forest<MycoNode,MycoEdge> create() {
          return new DelegateForest<MycoNode,MycoEdge>();
        }
      };

  /*public static Factory<UndirectedGraph<MycoNode,MycoEdge>>
    undirectedGraphFactory =  new Factory<UndirectedGraph<MycoNode,MycoEdge>>() {
    public UndirectedGraph<MycoNode,MycoEdge> create() {
    return new UndirectedSparseMultigraph<MycoNode,MycoEdge>();
    }
    };

    public static Factory<MycoEdge> undirectedEdgeFactory =
    new Factory<MycoEdge>() {
    public MycoEdge create() {
    return new MycoEdge();
    }
    };

    public static UndirectedGraph<MycoNode,MycoEdge> toUndirected(MycoGraph g) {
    return DirectionTransformer.toUndirected(g,
    undirectedGraphFactory,
    undirectedEdgeFactory,
    false);
    }

    public UndirectedGraph<MycoNode,MycoEdge> toUndirected() {
    return toUndirected(this);
    }*/

  public static Forest<MycoNode,MycoEdge> getMinimumSpanningForest(Graph<MycoNode, MycoEdge> g) {
    //log.finer("Trying to find MST for " + g);
    MinimumSpanningForest<MycoNode,MycoEdge> msf =
        new MinimumSpanningForest<MycoNode,MycoEdge>(g,
                                                     typeForestFactory.create(),
                                                     null);
    return msf.getForest();
  }

  public Forest<MycoNode,MycoEdge> getMinimumSpanningForest() {
    return getMinimumSpanningForest(this);
  }

  public Set<Set<MycoNode>> findConnectedComponents() {
    Set<Set<MycoNode>> components = new HashSet<Set<MycoNode>>();
    Set<MycoNode> unseen = new HashSet<MycoNode>(this.getVertices());
    Queue<MycoNode> queue = new LinkedList<MycoNode>();

    Set<MycoNode> workingComponent = null;
    MycoNode current;
    while ( (! unseen.isEmpty()) || (! queue.isEmpty()) ) {
      if (queue.isEmpty()) {
        // Queue an arbitary unvisited node
        MycoNode n  = unseen.iterator().next();
        queue.offer(n);
        unseen.remove(n);
        // Start new component
        workingComponent = new HashSet<MycoNode>();
        components.add(workingComponent);
      }
      current = queue.remove();
      workingComponent.add(current);
      for (MycoNode neighbor : current.getHyphaLink().getNeighbors()) {
        if (unseen.contains(neighbor)) {
          queue.offer(neighbor);
          unseen.remove(neighbor);
        }
      }
    }
    return components;
  }
}
