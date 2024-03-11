// Barnes.java
import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Time;
import java.util.concurrent.*;
import java.awt.Dimension;
import javax.swing.*;

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
            System.out.println("ARGS YES");
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
                System.out.println("File " + args[0] + " could not be opened.");
            }
        }
        else{
            System.out.println("ARGS NO");
            try (BufferedReader buff = new BufferedReader(new FileReader("testPlanets.csv"))) {
                // read the amount of planets
                String line = buff.readLine(); // does it remove the new line sign?
                amountOfPlanets = Integer.parseInt(line);
                System.out.println("AMOUNT OF PLANETS: " + amountOfPlanets);
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
                System.out.println("File could not be opened.");
            }
        }
            
        //Space space = new Space();
        System.out.println("Planets in the order they were added");
        for (Planet planet : planets) {
            System.out.println(planet.toString());
        }

        int height = 32;
        int width = 32;

        int wm = 10;
        int hm = 10;

        // Graphics
        JFrame frame = new JFrame();
        Draw draw = new Draw(width*wm+50, height*hm+50, amountOfPlanets);

        for(int i = 0; i < amountOfPlanets; i++){
            draw.addCircle(i, planets[i].getX()*wm, planets[i].getY()*hm, 30);
        }

        frame.setSize(width*wm+50, height*hm+50);
        frame.setTitle("N-Body Problem");

        frame.add(draw);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        //System.out.println("Height: " + height + ", Width: " + width);

        Tree tree = new Tree(height, width);
        tree.createTree(planets);

        System.out.println("\nTree");
        tree.prettyPrint();

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

        tree.prettyPrint();

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
        private final double secondsPerFrame = 1;
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

        // Arguments:
        //  barrier, tree, planets, startPlanetIndex, endPlanetIndex
        // split array of planets among workers
        // for each planet assigned to the worker
        //  traverse tree and calculate forces (needs a queue or whatever)
        //  write new position
        // wait at barrier (needs barrier)

        // function(planet)
        private void calculateForce(Planet planet, Node node){
            // calculateForce takes a planet and a node,
            // calculates the force that the planet will feel from the node,
            // calculates the acceleration created by that force,
            // and adds this acceleration to a sum of accelerations that the planet feels

            /* Formulas
                Newton's first law
                    F = mass * acceleration
                Gravitational force
                    F = G * (mass1 * mass2) / distance^2
                (Gravitational acceleration)
                    a = F / mass1 = G * mass2 / distance^2
                Distance moved
                    distance = (current_velocity * time) + total_acceleration * (time^2) / 2
            */

            double deltaX = node.centerX - planet.getX();
            double deltaY = node.centerY - planet.getY();
            
            planet.ax += deltaX == 0 ? 0 : gforce * node.mass / (deltaX*deltaX);
            planet.ay += deltaY == 0 ? 0 : gforce * node.mass / (deltaY*deltaY);

            if (deltaX < 0) {
                planet.ax = -planet.ax;
            }
            if (deltaY < 0) {
                planet.ay = -planet.ay;
            }
        }

        private void calcNewCoords(Planet planet){
            // Calculate where the planet 
                // distance = (current_velocity * time) + total_acceleration * (time^2) / 2
                double distanceX = (planet.xVel * secondsPerFrame) + planet.ax * secondsPerFrame*secondsPerFrame / 2;
                double distanceY = (planet.yVel * secondsPerFrame) + planet.ay * secondsPerFrame*secondsPerFrame / 2;

                System.out.println("Distance X for planet " + planet.id + " is: " + distanceX);

                // System.out.println("What is distX: " + distanceX);
                // System.out.println("What is distY: " + distanceY);
                double newX = planet.getX() + distanceX; //planet.getX() + 1;
                double newY = planet.getY() + distanceY; //planet.getY() + 1;

                System.out.println("New X for planet " + planet.id + " is: " + distanceX);

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

        private void traverseTree(Planet planet, Node node) {
            double distance;

            if (!node.hasChildren()) {
                if (!node.hasPlanet() || node.planet.id == planet.id) {
                    return;
                }

                // Calculate the sum of accelerations acting on the planet
                calculateForce(planet, node);

                calcNewCoords(planet);
            }
            else {
                // calculate distance from planet to node's center of mass
                distance = Math.sqrt(Math.pow(planet.getX() - node.centerX, 2) + Math.pow(planet.getY() - node.centerY, 2));
                
                if (far < distance) {
                    // Approximate the force using this node
                    calculateForce(planet, node);

                    calcNewCoords(planet);
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
                    System.out.println(planets[i].toString());
                    draw.addCircle(i, planets[i].getX()*10, planets[i].getY()*10, 30);
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
                    TimeUnit.MILLISECONDS.sleep(1000);
                } catch(InterruptedException ex) {}
                System.out.println("\nSTEP\n" + j);
            }
        }
    }
}

