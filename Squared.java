// Barnes.java
import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Time;
import java.util.concurrent.*;
import java.awt.Dimension;
import javax.swing.*;
import java.util.Random;

public class Squared {

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
        *  3 numWorkers  -> how many parallel workers 
        *  4 graphics    -> false or true : show graphics or not
        *  5 file bool   -> false or true : read from file 
        *  6 file        -> file name. If empty, default would be used
        */

        int gNumBodies;
        int numSteps;
        int numWorkers;
        Boolean graphics;
        Boolean fileb;

        if(args.length > 0){
            gNumBodies = Integer.valueOf(args[0]) > 0 ? Integer.valueOf(args[0]) : 1;
            numSteps = Integer.valueOf(args[1]) > 0 ? Integer.valueOf(args[1]) : 300;
            numWorkers = Integer.valueOf(args[3]) > 0 ? Integer.valueOf(args[3]) : 1;
            graphics = args.length > 4 ? Boolean.valueOf(args[4]) : false;
            fileb = args.length > 5 ? Boolean.valueOf(args[5]) : false;
        }
        else{
            gNumBodies = 4;
            numSteps = 10000;
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
                    planets[id++] = new Planet(id, Double.parseDouble(row[0]), Double.parseDouble(row[1]), Double.parseDouble(row[2]), Double.parseDouble(row[3]), Double.parseDouble(row[4]), Double.parseDouble(row[5]));
                }
            } catch (Exception e) {
                //System.out.println("File " + args[0] + " could not be opened.");
            }
        }
        else {
            // Randomize planets
            for(int i = 0; i < gNumBodies; i++){    
                planets[i] = new Planet(
                    i, 
                    (rand.nextDouble()*Math.pow(10, 13)), 
                    rand.nextDouble()+1, 
                    rand.nextDouble()*width, 
                    rand.nextDouble()*height, 
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
            workers[i] = new Worker(i, width, height, barrier, planets, start, end, numSteps, draw);
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
        Planet[] planets;
        int startPlanetIndex;
        int endPlanetIndex;
        int steps;
        private final double gforce = 6.67 * Math.pow(10, -11);
        private final double secondsPerFrame = 0.01;
        private final double elasticity = 1.0;
        Draw draw;
        int width;
        int height;
        
        public Worker(int id, int width, int height, CyclicBarrier barrier, Planet[] planets, int startPlanetIndex, int endPlanetIndex, int steps, Draw draw) {
            this.id = id;
            this.barrier = barrier;
            this.planets = planets;
            this.startPlanetIndex = startPlanetIndex;
            this.endPlanetIndex = endPlanetIndex;
            this.steps = steps;
            this.draw = draw;
            this.width = width;
            this.height = height;
        }

        private void calculateForce(int index, Planet[] planets){
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

            for(int i = 0; i < planets.length; i++){
                if(planets[i].id == planets[index].id) continue;

                double distanceX = planets[i].getX() - planets[index].getX();
                double distanceY = planets[i].getY() - planets[index].getY();
                double distance = Math.sqrt(distanceX*distanceX + distanceY*distanceY);
    
                double force;
    
                // Calculate the gravitational force
                if (distance == 0 ) {
                    force = 0;
                }
                else {
                    force = gforce * planets[index].mass * planets[i].mass / (distance*distance);
                }
    
                // Calculate the directional components of the gravitational force
                double angle = Math.atan2(distanceY, distanceX);
    
                double forceX = force * Math.cos(angle);
                double forceY = force * Math.sin(angle);
    
                // Calculate and add the accelerations in x and y directions
                double accelerationX = forceX / planets[index].mass;
                double accelerationY = forceY / planets[index].mass;
    
                // Handle an eventual collision
                if (distance <= (planets[index].size + planets[i].size)) {
                    // If a collision is detected we ignore the gravitational force of this planet for this time step. This should produce a bouncing effect.
    
                    // Find directional vector between planets
                    double directionX = planets[i].getX() - planets[index].getX();
                    double directionY = planets[i].getY() - planets[index].getY();
    
                    // Normalize the directional vector
                    double directionalLength = Math.sqrt(directionX*directionX + directionY*directionY);
                    directionX = directionX / directionalLength;
                    directionY = directionY / directionalLength;
    
                    // Project velocities onto directinal vector, producing two scalars
                    double v1 = planets[index].xVel * directionX + planets[index].yVel * directionY;
                    double v2 = planets[i].xVel * directionX + planets[i].yVel * directionY;
    
                    // Calculate scalar velocity after collision
                    double collisionVelocity = (planets[index].mass*v1 + planets[i].mass*v2 - planets[i].mass * elasticity * (v1 - v2)) / (planets[index].mass + planets[i].mass);
    
                    // Calculate the directional acceleration
                    double collisionAcceleration = (collisionVelocity) - v1; // + (collisionVelocity < 0 ? -0.2 : 0.2)
    
                    // Multiply acceleration with directional vector to produce directional acceleration vector
                    double collisionX = directionX * collisionAcceleration;
                    double collisionY = directionY * collisionAcceleration;
    
                    // Add collisions acceleration to the planet's accelerations
                    planets[index].ax += collisionX + -(accelerationX * ((planets[index].size + planets[i].size) / distance));
                    planets[index].ay += collisionY + -(accelerationY * ((planets[index].size + planets[i].size) / distance));
                }
                else {
                    planets[index].ax += accelerationX;
                    planets[index].ay += accelerationY;
                }
            }
        }
        
        public void run() {

            for(int j = 0; j < steps; j++){

                // Calculate force
                // for each of the worker's planets
                //  calculate next position of the planet
                for(int i = startPlanetIndex; i <= endPlanetIndex; i++){
                    calculateForce(i, planets);
                    
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
                    else if (width - 1 <= newX) {
                        newX = width - 1;
                    }
                    if (newY < 0) {
                        newY = 0;
                    }
                    else if (height - 1 <= newY) {
                        newY = height - 1;
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