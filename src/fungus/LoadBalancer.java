package fungus;

import peersim.cdsim.*;
import peersim.config.*;
import peersim.core.*;
import java.util.*;
import java.util.logging.*;
import org.apache.commons.collections15.*;
import edu.uci.ics.jung.graph.*;
import edu.uci.ics.jung.graph.util.*;

public class LoadBalancer implements CDProtocol {
    private static final String PAR_HYPHADATA_PROTO = "hyphadata_proto";
    private static final String PAR_HYPHALINK_PROTO = "hyphalink_proto";
    private static final String PAR_START_CYCLE = "start_cycle";
    private static final String PAR_PERIOD = "period";
    private static final String PAR_FIXED = "neighborhood_balancing";

    private static int hyphaDataPid;
    private static int hyphaLinkPid;
    private static int startCycle;
    private static int period;
    private static boolean neighborhoodBalancing;

    private MycoNode myNode = null;

    private static Logger log = Logger.getLogger(LoadBalancer.class.getName());

    public LoadBalancer(String prefix) {
        hyphaDataPid = Configuration.getPid(prefix + "." + PAR_HYPHADATA_PROTO);
        hyphaLinkPid = Configuration.getPid(prefix + "." + PAR_HYPHALINK_PROTO);
        startCycle = Configuration.getInt(prefix + "." + PAR_START_CYCLE);
        period = Configuration.getInt(prefix + "." + PAR_PERIOD);
        neighborhoodBalancing = Configuration.getBoolean(prefix + "." + PAR_FIXED);
    }

    public Object clone() {
        LoadBalancer ret = null;
        try {
            ret = (LoadBalancer) super.clone();
        } catch (CloneNotSupportedException e) {
            // Never happens
        }
        return ret;
    }

    public void balanceWith(MycoNode n) {
        HyphaData data = myNode.getHyphaData();
        HyphaData theirData = n.getHyphaData();

        double myCapacity = data.getCapacity();
        double theirCapacity = theirData.getCapacity();
        int myLength = data.getQueueLength();
        double theirLength = theirData.getQueueLength();


        double myWeight = myCapacity;
        double theirWeight = theirCapacity;

        double scale = (myWeight + theirWeight);
        double totalLength = myLength + theirLength;

        double myRatio = myWeight / scale;

        int myPortion = (int) (totalLength * myRatio);
        int theirPortion = (int) (totalLength - myPortion);

        Object[] alerts = { myNode, n };

        MessageObserver.loadQueryMessages(2);

        if (myPortion == myLength) {
        // if ((myPortion == myLength) || (myPortion <= myWeight)
        //     || (theirPortion <= theirWeight)) {
          log.log(Level.FINER, "Node" + myNode.getID() + " ("
                  + (int) myWeight + "/" + myLength
                  + ") is already balanced with node " + n.getID() + " ("
                  + (int) theirWeight + "/" + (int) theirLength
                  + "), not balancing again", alerts);

        } else if (myPortion < myLength) {
          int toTransfer = myLength - myPortion;
          log.log(Level.FINER, "Node " + myNode.getID() + " ("
                  + (int) myWeight + "/" + myLength
                  + ") BALANCING by sending " + toTransfer + " jobs to node "
                  + n.getID() + " (" + (int) theirWeight + "/"
                  + (int) theirLength + ")", alerts);
          data.transferJobs(myNode, n, toTransfer); //FIXME
          MessageObserver.loadActionMessages(2);
          log.log(Level.FINER, "New status: node " + myNode.getID() + " ("
                  + (int) myWeight + "/" + data.getQueueLength()
                  + ") and node "
                  + n.getID() + " (" + (int) theirWeight + "/"
                  + theirData.getQueueLength() + ")", alerts);

        } else if (myPortion > myLength) {
          int toTransfer = myPortion - myLength;
          log.log(Level.FINER, "Node " + myNode.getID() + " ("
                  + (int) myWeight + "/" + myLength
                  + ") BALANCING by pulling " + toTransfer
                  + " jobs from node " + n.getID() + " ("
                  + (int) theirWeight + "/" + (int) theirLength + ")",
                  alerts);
          theirData.transferJobs(n, myNode, toTransfer);
          MessageObserver.loadActionMessages(2);
          log.log(Level.FINER, "New status: node " + myNode.getID() + " ("
                  + (int) myWeight + "/" + data.getQueueLength()
                  + ") and node "
                  + n.getID() + " (" + (int) theirWeight + "/"
                  + theirData.getQueueLength() + ")", alerts);

        } else {
          log.log(Level.WARNING,
                  "SOMETHING UNEXPECTED HAPPENED when "
                  + " attempting to balance node " + myNode.getID()
                  + " (" + (int) myWeight + "/" + myLength
                  + ") with node " + n.getID() + " ("
                  + (int) theirWeight + "/" + (int) theirLength
                  + ")", alerts);
        }
    }

    public void nextCycle(Node node, int pid) {

        if (CDState.getCycle() < startCycle || CDState.getCycle() % period != 0)
                return;

        HyphaLink link = (HyphaLink) node.getProtocol(hyphaLinkPid);

        myNode = (MycoNode) node;

        if (!myNode.isUp()) {
            return;
        }

        MycoNode target = link.getRandomSameHypha();

        if (target == null) {
            log.log(Level.FINE, "Node " + myNode.getID()
                    + " has no hyphal neighbors of the same type; not balancing",
                    myNode);
        } else {
            log.log(Level.FINER, "Node " + myNode.getID() + " balancing with "
                    + target.getID(), new Object[] { myNode, target });
            balanceWith(target);
        }
    }
}
