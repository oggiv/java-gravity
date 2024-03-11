// Barnes.java
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.concurrent.*;
import javax.swing.*;
import java.util.Random;

public class Barnes {

    public static void main(String[] args) {

        /*
            Reads planets from a .csv file.
            First line should be the amount of planets to read as an integer.
            Following lines should be planets of the format:
                mass,radius,positionX,positionY,velocityX,velocityY
        */

        /** Command Line arguments
        *  1 gNumBodies  -> How many planets
        *  2 numSteps    -> Amount of steps in the program
        *  3 far         -> Distance which will be considered too far for Barnes-Hut approximation
        *  4 numWorkers  -> how many parallel workers 
        *  5 graphics    -> false or true : show graphics or not
        *  6 file bool   -> false or true : read from file 
        *  7 file        -> file name. If empty, default would be used
        */

        int gNumBodies;
        int numSteps;
        double far;
        int numWorkers;
        Boolean graphics;
        Boolean fileb;

        if(args.length > 0){
            gNumBodies = Integer.valueOf(args[0]) > 0 ? Integer.valueOf(args[0]) : 1;
            numSteps = Integer.valueOf(args[1]) > 0 ? Integer.valueOf(args[1]) : 300;
            far = Integer.valueOf(args[2]) > 0 ? Double.valueOf(args[2]) : 100;
            numWorkers = Integer.valueOf(args[3]) > 0 ? Integer.valueOf(args[3]) : 1;
            graphics = args.length > 4 ? Boolean.valueOf(args[4]) : false;
            fileb = args.length > 5 ? Boolean.valueOf(args[5]) : false;
        }
        else{
            gNumBodies = 4;
            numSteps = 10000;
            far = 100;
            numWorkers = 4;
            graphics = true;
            fileb = false;
        }

        // Random number generator. Used for planets
        Random rand = new Random();

        // Planets
        Planet[] planets = new Planet[gNumBodies];

        // Space size
        int height = 32;
        int width = 32;

        // Frame size multiplicator
        int wm = 10;
        int hm = 10;
        
        // Create planets
        if(fileb) {
            String file = args.length > 6 ? args[6] : "testPlanets.csv";

            try (BufferedReader buff = new BufferedReader(new FileReader(file))) {
                // read the amount of planets
                String line;
    
                // read and create planets
                int id = 0;
                String[] row;
                while ((line = buff.readLine()) != null) {
                    row = line.split(",");
                    planets[id++] = new Planet(id, Double.parseDouble(row[0]), ((Double.parseDouble(row[0])/Math.pow(10, 11))+1), Double.parseDouble(row[1]), Double.parseDouble(row[2]), Double.parseDouble(row[3]), Double.parseDouble(row[4]));
                }
            } catch (Exception e) {
                //System.out.println("File " + args[0] + " could not be opened.");
            }
        }
        else {
            // Randomize planets
            double tempMassModifier;
            for(int i = 0; i < gNumBodies; i++){
                tempMassModifier = rand.nextDouble();
                planets[i] = new Planet(
                    i, 
                    (tempMassModifier*Math.pow(10, 11)), 
                    tempMassModifier+1, 
                    rand.nextDouble()*(width - 0.2) + 0.1, 
                    rand.nextDouble()*(height - 0.2) + 0.1, 
                    rand.nextDouble()*0, 
                    rand.nextDouble()*0);
            }
        }
        
        // Print out all the planets
        System.out.println("Planets in the order they were added");
        for (Planet planet : planets) {
            System.out.println(planet.toString());
        }
        /*
        */

        // Graphics
        Draw draw = new Draw(width*wm+50, height*hm+50, gNumBodies);
        if(graphics){
            JFrame frame = new JFrame();
            draw = new Draw(width*wm+50, height*hm+50, gNumBodies);
    
            for(int i = 0; i < gNumBodies; i++){
                draw.addCircle(i, planets[i].getX()*wm, planets[i].getY()*hm, 30);
            }
    
            frame.setSize(width*wm+50, height*hm+50);
            frame.setTitle("N-Body Problem");
            frame.add(draw);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
        }

        // Create tree
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
            workers[i] = new Worker(i, barrier, tree, planets, start, end, far, numSteps, draw);
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
        private final double secondsPerFrame = 0.01;
        private final double elasticity = 1.0;
        Draw draw;
        
        public Worker(int id, CyclicBarrier barrier, Tree tree, Planet[] planets, int startPlanetIndex, int endPlanetIndex, double far, int steps, Draw draw) {
            this.id = id;
            this.barrier = barrier;
            this.tree = tree;
            this.planets = planets;
            this.startPlanetIndex = startPlanetIndex;
            this.endPlanetIndex = endPlanetIndex;
            this.far = far;
            this.steps = steps;
            this.draw = draw;
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

            // Calculate the distances between the planets
            double distanceX = node.centerX - planet.getX();
            double distanceY = node.centerY - planet.getY();
            double distance = Math.sqrt(distanceX*distanceX + distanceY*distanceY);

            double force;

            // Calculate the gravitational force
            if (distance == 0 ) {
                force = 0;
            }
            else {
                force = gforce * planet.mass * node.mass / (distance*distance);
            }

            // Calculate the directional components of the gravitational force
            double angle = Math.atan2(distanceY, distanceX);

            double forceX = force * Math.cos(angle);
            double forceY = force * Math.sin(angle);

            // Calculate and add the accelerations in x and y directions
            double accelerationX = forceX / planet.mass;
            double accelerationY = forceY / planet.mass;

            // Handle an eventual collision
            if (node.hasPlanet() && distance <= (planet.size + node.planet.size)) {
                // If a collision is detected we ignore the gravitational force of this planet for this time step. This should produce a bouncing effect.

                // Find directional vector between planets
                double directionX = node.planet.getX() - planet.getX();
                double directionY = node.planet.getY() - planet.getY();

                // Normalize the directional vector
                double directionalLength = Math.sqrt(directionX*directionX + directionY*directionY);
                directionX = directionX / directionalLength;
                directionY = directionY / directionalLength;

                // Project velocities onto directinal vector, producing two scalars
                double v1 = planet.xVel * directionX + planet.yVel * directionY;
                double v2 = node.planet.xVel * directionX + node.planet.yVel * directionY;

                // Calculate scalar velocity after collision
                double collisionVelocity = (planet.mass*v1 + node.planet.mass*v2 - node.planet.mass * elasticity * (v1 - v2)) / (planet.mass + node.planet.mass);

                // Calculate the directional acceleration
                double collisionAcceleration = (collisionVelocity) - v1; // + (collisionVelocity < 0 ? -0.2 : 0.2)

                // Multiply acceleration with directional vector to produce directional acceleration vector
                double collisionX = directionX * collisionAcceleration;
                double collisionY = directionY * collisionAcceleration;

                // Add collisions acceleration to the planet's accelerations
                planet.ax += collisionX + -(accelerationX * ((planet.size + node.planet.size) / distance));
                planet.ay += collisionY + -(accelerationY * ((planet.size + node.planet.size) / distance));
            }
            else {
                planet.ax += accelerationX;
                planet.ay += accelerationY;
            }
        }

        private void traverseTree(Planet planet, Node node) {
            double distance;

            if (!node.hasChildren()) {
                if (!node.hasPlanet() || node.planet.id == planet.id) {
                    return;
                }

                // Calculate the sum of accelerations acting on the planet
                calculateForce(planet, node);
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
                    
                    // Calculate how far the planet will move based on current velocity and acceleration
                    //  distance = (current_velocity * time) + total_acceleration * (time^2) / 2
                    double distanceX = (planets[i].xVel * secondsPerFrame) + planets[i].ax * secondsPerFrame*secondsPerFrame / 2;
                    double distanceY = (planets[i].yVel * secondsPerFrame) + planets[i].ay * secondsPerFrame*secondsPerFrame / 2;

                    // Calculate new velocity
                    //  v = v0 + a * t
                    planets[i].xVel += planets[i].ax * secondsPerFrame;
                    planets[i].yVel += planets[i].ay * secondsPerFrame;

                    // System.out.println("What is distX: " + distanceX);
                    // System.out.println("What is distY: " + distanceY);
                    double newX = planets[i].getX() + distanceX; //planets[i].getX() + 1;
                    double newY = planets[i].getY() + distanceY; //planets[i].getY() + 1;

                    System.out.println("New X for planets[i] " + planets[i].id + " is: " + distanceX);

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

                    planets[i].setX(newX);
                    planets[i].setY(newY);
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
                    System.out.println(planets[i].toString());
                    draw.addCircle(i, planets[i].getX()*10, planets[i].getY()*10, 20*planets[i].size);
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
                    draw.redo();
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
                try{
                    TimeUnit.MILLISECONDS.sleep(10);
                } catch(InterruptedException ex) {}

            }
        }
    }
}