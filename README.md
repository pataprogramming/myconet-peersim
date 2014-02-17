<div id="table-of-contents">
<h2>Table of Contents</h2>
<div id="text-table-of-contents">
<ul>
<li><a href="#sec-1">1. Preliminaries</a></li>
<li><a href="#sec-2">2. Running a Simulation</a></li>
<li><a href="#sec-3">3. Visualizer</a></li>
</ul>
</div>
</div>
# Preliminaries

-   All needed libraries (including PeerSim) should already be in the
    `lib/` directory.  The primary prerequisite is for Apache Ant, which
    is used for the build scripts. A relatively recent version of Java
    is also needed.

-   To compile, simply run `ant compile` from the root. The unchecked
    conversion warnings from PeerSim can be safely ignored.

# Running a Simulation

-   A convenience script is used to launch the simulation. This script,
    `run`, is a Bourne shell script, and will only work on UNIX-like
    systems.  If you are using Myconet on Windows, you will need to
    create an equivalent `run.bat` or `run.cmd` file by using this file
    as a guide for how to set the Java classpath.

-   The configuration for an individual experiment is performed using a
    text file. Relevant parameters can be found in the
    `example-*.txt` files in the root of the repository.  Additional
    discussion can be found in the `docs/` subdirectory.

# Visualizer

Once you run the simulation with the visualizer enabled, you will see
a number of buttons.  Note that the position of the nodes on the
screen does not represent their actual location&#x2026;it is a layout
algorith that attempts to make the neighbor relationships clear, which
is the significant aspect.  With the basic `VisualizerTransformers`
enabled, small blue circles are biomass, red triangles are extending
hyphae, yellow squares are branching hyphae, and green pentagons are
immobile hyphae.

The JUNG layout code has a couple of bugs in it, which results in an
occasional race condition and null pointer exception (halting the
layout update algorithm).  Should this occur, just kill the simulation
and restart.

-   `freeze` pauses the layout algorithm, which can be useful when you
    are letting the simulation run for a number of rounds and don't
    care about what's happening in between.  Clicking the button again
    restarts the layout engine.

-   `capture` takes a picture of the displayed graph and saves it to
    the `capture/` directory using the basename specified in the config
    file.

-   `pause` stops a running or walking simulation.

-   `step` advances the simulation by one cycle

-   `walk` starts the simulation advancing with a short delay between
    each cycle. If this delay is too short, you'll need to change it
    in the code.

-   `run` starts the simulation running without pausing between cycles.
    If you click `run` (say, because you want to get to round 80, fast),
    it's best to freeze the layout to reduce the chance of the null
    pointer error.
