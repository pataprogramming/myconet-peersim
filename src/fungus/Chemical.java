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

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class Chemical {
    public abstract double getDiffusionRate(); // frac of difference w/neighbor
    public abstract double getDecayRate();     // fraction lost per cycle
    public abstract double getMaxConcentration(); // Max concentration in a node

    public double cutoff = 1e-02;

    private static Logger log =
        Logger.getLogger(Chemical.class.getName());

    public String name = "Chemical";

    public double amount;

    public Chemical duplicate() {
        Chemical ret;
        //        try {
            ret = (Chemical) this.clone();
            return ret;
            //} catch (CloneNotSupportedException e) {
            //log.severe("Clone failed; should never happen");
            //return null;
            // }
    }

    public Object clone() {
        try {
            Chemical ret = this.getClass().newInstance();
            ret.setAmount(amount);
            //ret.diffusionRate = this.diffusionRate;
            //ret.decayRate = this.decayRate;

            return ret;
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    protected Chemical extract(double quantity) {
        double amountExtracted = removeAmount(quantity);

        Chemical ret = duplicate();
        ret.setAmount(amountExtracted);
        return ret;
    }

    public void doDynamics(ChemicalManager local,
                           Collection<ChemicalManager> neighbors) {
        if (amount < cutoff) {
            log.log(Level.FINE, "No local concentration of " + name + " at " +
                     local.getMycoNode(), local.getMycoNode());
            return;
        }
        diffuse(local, neighbors);
        decay(local);
    }

    public void diffuse(ChemicalManager local,
                        Collection<ChemicalManager> neighbors) {
        log.log(Level.FINE, "Local concentration of " + name + " at " +
                local.getMycoNode() + " is " + amount, local.getMycoNode());

        double totalInNeighborhood = 0.0;
        for (ChemicalManager neighbor : neighbors) {
            totalInNeighborhood += neighbor.getConcentration(this.getClass());
        }
        double average = totalInNeighborhood / ((double) neighbors.size());
        for (ChemicalManager neighbor : neighbors) {
            neighbor.setConcentration(this.getClass(), average);
            setAmount(average);
        }

        //     if (neighbor.getConcentration(this.getClass()) >=
        //         //amount * getDiffusionRate()
        //         amount
        //         ) continue; // FIXME: Inaccurate
        //     Chemical msg = extract(amount * getDiffusionRate());
        //     if (msg.getAmount() < cutoff) {
        //         log.log(Level.FINE, "No " + name + " remaining at " + local.getMycoNode() +
        //                  ", stopping diffusion", local.getMycoNode());
        //     }
        //     log.log(Level.FINE, "Diffusing" + msg + " from " +
        //             local.getMycoNode() + " to " + neighbor.getMycoNode(),
        //              new Object[] { local.getMycoNode(),
        //                             neighbor.getMycoNode() });
        //     local.send(msg, neighbor);
        // }
    }

    public void decay(ChemicalManager local) {
        double newAmount = amount * (1 - getDecayRate());
        log.log(Level.FINE, name + " decaying at " + local.getMycoNode() +
                 ", old amount = " + amount +
                 ", new amount = " + newAmount,
                 local.getMycoNode());
        setAmount(newAmount);
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double newAmount) {
        if (newAmount < cutoff) { newAmount = 0.0; }
        if (newAmount > getMaxConcentration()) {
            newAmount = getMaxConcentration();
        }
        this.amount = newAmount;
    }

    public void addAmount(double quantity) {
        setAmount(amount + quantity);
    }

    public double removeAmount(double quantity) {
        double oldAmount = amount;
        double newAmount = amount - quantity;
        if (newAmount < cutoff) {
            newAmount = 0.0;
        }
        setAmount(newAmount);
        return oldAmount - newAmount; // Return amount actually removed
    }

    public String toString() {
        return name + "(" + getAmount() + ")";
    }
}