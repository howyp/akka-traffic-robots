akka-traffic-robots
=========================

A simulation of traffic-collecting robots written in Scala and the Akka framework.

### About

Traffic robots are sent locations to visit by a dispatcher. The robot must visit the location, determine whether it is near to the Tube Station, and then report on the traffic if it is.

There are two main types of `Actor`:

* `TrafficDispatcher` is responsible for holding the master list of waypoints, starting robots as child actors, gradually delivering them points, and then shutting the simulation down once the robots have visited all waypoints
* `Robot` visits the points it is assigned to. It requests or 'pulls' 10 waypoints at a time from the dispatcher, to avoid having its queue overloaded. Events relating to robot movement and traffic conditions are broadcast onto the `eventStream` where any other actor my subscribe to them.

Waypoint and tube station data is read from CSV files using Scala's `ParserCombinator` library.

Individual tests for each actor class prove much of the functionality, whilst the `SimulationIntegrationSpec` demonstrates all actors integrating over a simple scenario.

### Running

To run a simulation using the sample data provided in the `data` directory, run:

```
sbt run
```

For clarity, all actor communication is currently being logged.

### Future Improvements

The simulation uses the Haversine formula (specifically [a Scala implementation from Rosetta Code](http://rosettacode.org/wiki/Haversine_formula#Scala)) to determine distance between two points. Whilst accurate, it is computationally expensive, so it may be preferable to assume a flat cartesian grid system, which would have the advantage of being able to test nearness using bounding boxes.