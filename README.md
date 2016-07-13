Algo Viewer
===========

An attempt to visualize the steps taken in performing a particular algorithm - in this case set union with immutable sets.

To run:

    $ mvn compile exec:java -Dexec.mainClass=net.betaengine.immutableunion.UnionAlgoViewer

[`Sets`](src/main/java/net/betaengine/immutableunion/Sets.java) contains the data type, i.e `Set`, and the algorithm, i.e. `union`, that we want to investigate.

Algo and EDT thread
-------------------

`UnionAlgoViewer.UnionDrawer.runAlgo()` in [`UnionAlgoViewer`](src/main/java/net/betaengine/immutableunion/UnionAlgoViewer.java) does **not** run in the [EDT](https://en.wikipedia.org/wiki/Event_dispatching_thread).

The algo is run on a separate thread and each step results in a call to `Drawer.draw(String, Runnable)` in [`Drawer`](src/main/java/net/betaengine/algoviewer/Drawer.java) and this triggers an event on the EDT.

The algo is blocked until the EDT has done the visualization for the current step and lets it proceed (by calling `Drawer.advance()`).
