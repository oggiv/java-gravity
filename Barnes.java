// Barnes.java
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.concurrent.*;

public class Barnes {

    public static void main(String[] args) {

        /*
            Reads planets from a .csv file.
            First line should be the amount of planets to read as an integer.
            Following lines should be planets of the format:
                mass,positionX,positionY,velocityX,velocityY
        */

        int gNumBodies = 1;
        int numWorkers = 1;
        double far = 100;
        int numSteps = 10;

        int amountOfPlanets = 0;
        Planet[] planets = new Planet[0];

        if (0 < args.length) {
            try (BufferedReader buff = new BufferedReader(new FileReader(args[0]))) {
                // read the amount of planets
                String line = buff.readLine(); // does it remove the new line sign?
                amountOfPlanets = Integer.parseInt(line);
                gNumBodies = amountOfPlanets;

                // allocate array for planets
                planets = new Planet[amountOfPlanets];

                // read and create planets
                int id = 0;
                String[] row;
                while ((line = buff.readLine()) != null) {
                    row = line.split(",");
                    planets[id] = new Planet(id, Double.parseDouble(row[0]), Double.parseDouble(row[1]), Double.parseDouble(row[2]), Double.parseDouble(row[3]), Double.parseDouble(row[4]));
                    id++;
                }
            } catch (Exception e) {
                //System.out.println("File " + args[0] + " could not be opened.");
            }
        }

        int height = 32;
        int width = 32;

        System.out.println("Height: " + height + ", Width: " + width);

        Tree tree = new Tree(height, width);
        tree.createTree(planets);

        //System.out.println("\nTree");
        //tree.prettyPrint();

        //System.out.println("Planets in the order they were added");
        System.out.println("\nBefore");
        for (Planet planet : planets) {
            System.out.println(planet.toString());
        }

        CyclicBarrier barrier = new CyclicBarrier(numWorkers);
        int stripSize = (gNumBodies % numWorkers == 0) ? (gNumBodies / numWorkers) : ((gNumBodies / numWorkers) + 1);
        int start;
        int end;

        // Create workers
        Worker[] workers = new Worker[numWorkers];
        for (int i = 0; i < numWorkers; i++) {
            start = i * stripSize;
            end = (i == numWorkers - 1) ? (gNumBodies - 1) : (start + stripSize - 1); // edge case. Giving the last worker extra work if the division is uneven
            workers[i] = new Worker(i, barrier, tree, planets, start, end, far, numSteps);
            workers[i].start();
        }

        // Wait for workers to complete their 
        for (int i = 0; i < numWorkers; i++) {
            try{
                workers[i].join();
            } catch (InterruptedException ex){
                System.out.println("JOIN INTERRUPTED");
                return;
            }
        }

        /*System.out.println("\nAfter");
        for (Planet planet : planets) {
            System.out.println(planet.toString());
        }*/

        //tree.prettyPrint();

    }
    
    private static class Worker extends Thread {
        int id;
        CyclicBarrier barrier;
        Tree tree;
        Planet[] planets;
        int startPlanetIndex;
        int endPlanetIndex;
        double far;
        int steps;
        private final double gforce = 6.67 * Math.pow(10, -11);
        private final double secondsPerFrame = 0.3;
        
        public Worker(int id, CyclicBarrier barrier, Tree tree, Planet[] planets, int startPlanetIndex, int endPlanetIndex, double far, int steps) {
            this.id = id;
            this.barrier = barrier;
            this.tree = tree;
            this.planets = planets;
            this.startPlanetIndex = startPlanetIndex;
            this.endPlanetIndex = endPlanetIndex;
            this.far = far;
            this.steps = steps;
        }

        private void calculateForce(Planet planet, Node node){
            // calculateForce takes a planet and a node,
            // calculates the force that the planet will feel from the node,
            // calculates the acceleration created by that force,
            // and adds this acceleration to a sum of accelerations that the planet feels

            /* Formulas
                Newton's second law
                    F = mass * acceleration
                Gravitational force
                    F = G * (mass1 * mass2) / distance^2
            */

            double distanceX = node.centerX - planet.getX();
            double distanceY = node.centerY - planet.getY();
            
            double distance = Math.sqrt(distanceX*distanceX + distanceY*distanceY);

            double force;
            if (distance <= 0.5 ) {
                // !!! temporary fix to avoid collision weirdness. Change to (distance == 0) when fix isn't needed anymore
                force = 0;
            }
            else {
                force = gforce * planet.mass * node.mass / (distance*distance);
            }

            double angle = Math.atan2(distanceY, distanceX);

            double forceX = force * Math.cos(angle);
            double forceY = force * Math.sin(angle);

            double accelerationX = forceX / planet.mass;
            double accelerationY = forceY / planet.mass;

            planet.ax += accelerationX;
            planet.ay += accelerationY;
        }

        private void traverseTree(Planet planet, Node node) {
            double distance;

            if (!node.hasChildren()) {
                if (!node.hasPlanet() || node.planet.id == planet.id) {
                    return;
                }

                // Calculate the sum of accelerations acting on the planet
                calculateForce(planet, node);

                // Calculate how far the planet will move based on current velocity and acceleration
                //  distance = (current_velocity * time) + total_acceleration * (time^2) / 2
                double distanceX = (planet.xVel * secondsPerFrame) + planet.ax * secondsPerFrame*secondsPerFrame / 2;
                double distanceY = (planet.yVel * secondsPerFrame) + planet.ay * secondsPerFrame*secondsPerFrame / 2;

                // Calculate new velocity
                //  v = v0 + a * t
                planet.xVel += planet.ax * secondsPerFrame;
                planet.yVel += planet.ay * secondsPerFrame;

                // System.out.println("What is distX: " + distanceX);
                // System.out.println("What is distY: " + distanceY);
                double newX = planet.getX() + distanceX; //planet.getX() + 1;
                double newY = planet.getY() + distanceY; //planet.getY() + 1;

                if (newX < 0) {
                    newX = 0;
                }
                else if (tree.width - 1 <= newX) {
                    newX = tree.width - 1;
                }
                if (newY < 0) {
                    newY = 0;
                }
                else if (tree.height - 1 <= newY) {
                    newY = tree.height - 1;
                }

                planet.setX(newX);
                planet.setY(newY);
            }
            else {
                // calculate distance from planet to node's center of mass
                distance = Math.sqrt(Math.pow(planet.getX() - node.centerX, 2) + Math.pow(planet.getY() - node.centerY, 2));
                
                if (far < distance) {
                    // Approximate the force using this node
                    calculateForce(planet, node);
                }
                else {
                    // Continue down the tree
                    for(int i = 0; i < 4; i++){
                        traverseTree(planet, node.quadrant[i]);
                    }
                }
            }

        }
        
        public void run() {

            for(int j = 0; j < steps; j++){

                // Calculate force
                // for each of the worker's planets
                //  calculate next position of the planet
                for(int i = startPlanetIndex; i <= endPlanetIndex; i++){
                    traverseTree(planets[i], tree.root);
                }
                
                // await other workers to finish their calculations
                try{
                    barrier.await();
                } catch(InterruptedException ex) {
                    System.out.println("Interrupted exception barrier.");
                    return;
                } catch(BrokenBarrierException ex) {
                    System.out.println("Broken barrier exception.");
                    return;
                }
                
                // move planets
                // for each of the worker's planets
                //  call planet.update()
                //  (also draw on screen)
                for(int i = startPlanetIndex; i <= endPlanetIndex; i++){
                    planets[i].updateCoordinates();
                }
                
                
                // wait for all planets to update
                try{
                barrier.await();
                } catch(InterruptedException ex) {
                    System.out.println("Interrupted exception barrier.");
                    return;
                } catch(BrokenBarrierException ex) {
                    System.out.println("Broken barrier exception.");
                    return;
                }
                
                // First worker will rebuid the tree and other will wait for it to finish
                if(id == 0){
                    tree.createTree(planets);

                    System.out.printf("\nstep %d\n", j);
                    for (Planet planet : planets) {
                        System.out.println(planet.toString());
                    }
                }
                
                try{
                    barrier.await();
                } catch(InterruptedException ex) {
                    System.out.println("Interrupted exception barrier.");
                    return;
                } catch(BrokenBarrierException ex) {
                    System.out.println("Broken barrier exception.");
                    return;
                }
            }
        }
    }
}