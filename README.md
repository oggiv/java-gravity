# id1217group42
Project for ID1217 Concurrent Programming, The Gravitational N-Body Problem

This repositiory includes two implementations. They are both able to show graphics which can be activated by setting the graphics command argument to 'true'. It is turned off by default.

Squared.java brute forces the simulation at a time complexity of O(n^2)
Run it from the command line with
  java Squared gNumBodies numSteps numWorkers (graphics : true or false)

Barnes.java uses Barnes-Hut method approximation for faster performance.
Run it from the command line with
  java Barnes gNumBodies numSteps far numWorkers (graphics : true or false)