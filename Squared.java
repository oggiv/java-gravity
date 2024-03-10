// Barnes.java

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.concurrent.*;

public class Squared {


    public static void main(String[] args) {

        /*
            Reads planets from a .csv file.
            First line should be the amount of planets to read as an integer.
            Following lines should be planets of the format:
                mass,positionX,positionY,velocityX,velocityY
        */

        int gNumBodies = 4;
        int numWorkers = 1;

        int amountOfPlanets = 0;
        Planet[] planets = new Planet[0];

        if (0 < args.length) {
            try (BufferedReader buff = new BufferedReader(new FileReader(args[0]))) {
                // read the amount of planets
                String line = buff.readLine(); // does it remove the new line sign?
                amountOfPlanets = Integer.parseInt(line);

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
        //Space space = new Space();
        System.out.println("Planets in the order they were added");
        for (Planet planet : planets) {
            System.out.println(planet.toString());
        }

        int height = 32;
        int width = 32;

        System.out.println("Height: " + height + ", Width: " + width);

        Tree tree = new Tree(height, width);
        tree.createTree(planets);

        System.out.println("\nTree");
        tree.prettyPrint();

        CyclicBarrier barrier = new CyclicBarrier(numWorkers);
        //BagOfPlanets bag = new BagOfPlanets(gNumBodies);
        int stripSize = (gNumBodies % numWorkers == 0) ? (gNumBodies / numWorkers) : ((gNumBodies / numWorkers) + 1);
        int start;
        int end;

        // Create workers
        Worker[] workers = new Worker[numWorkers];
        for (int i = 0; i < numWorkers; i++) {
            start = i * stripSize;
            end = (i == numWorkers - 1) ? (gNumBodies - 1) : (start + stripSize - 1); // edge case. Giving the last worker extra work if the division is uneven
            workers[i] = new Worker(barrier, tree, planets, start, end);
            workers[i].start();
        }

        for (int i = 0; i < numWorkers; i++) {
            workers[i].join();
        }

    }
    
    private static class Worker extends Thread {
        
        CyclicBarrier barrier;
        Tree tree;
        Planet[] planets;
        int startPlanetIndex;
        int endPlanetIndex;
        private final double gforce = 6.67 * Math.pow(10, -11);
        private final double secondsPerFrame = 1;
        
        public Worker(CyclicBarrier barrier, Tree tree, Planet[] planets, int startPlanetIndex, int endPlanetIndex) {
            this.barrier = barrier;
            this.planets = planets;
            this.startPlanetIndex = startPlanetIndex;
            this.endPlanetIndex = endPlanetIndex;
        }

        // Arguments:
        //  barrier, tree, planets, startPlanetIndex, endPlanetIndex
        // split array of planets among workers
        // for each planet assigned to the worker
        //  traverse tree and calculate forces (needs a queue or whatever)
        //  write new position
        // wait at barrier (needs barrier)

        // function(planet)
        private void calculateForce(int index){
            Planet pl = planets[index];
            double distance, magnitude, xDir, yDir;

            
            for(int i = 0; i < planets.length; i++){
                if(i == index) continue;

                distance = Math.sqrt(Math.pow((pl.getX() - planets[i].getX()), 2) + Math.pow((pl.getY() - planets[i].getY()), 2));
                magnitude = (gforce*pl.mass*planets[i].mass) / Math.pow(distance, 2);

                xDir = planets[i].getX() - pl.getX();
                yDir = planets[i].getY() - pl.getY();
                
                pl.setX(magnitude*xDir/distance + pl.getX());
                pl.setY(magnitude*yDir/distance + pl.getY());                


                // Planets p1 and p2
                // F = G * (p1.mass * p2.mass) / (distance)^2
                // Fx = G * (p1.mass * p2.mass) / distanceX^2
                // Fy = G * (p1.mass * p2.mass) / distanceY^2
                // p1.ax = Fx / p1.mass = G * p2.mass / distanceX^2
                // p1.x = p1.x + (p1.velocityX * time + p1.ax * (time^2) / 2)
                // and so on...

                // p1.x = p1.x + time * (p1.velocityX + gforce * p2.mass / abs)

                /*

                // Given Planet objects planet1 and planet2

                // Calculate distances in X and Y
                distanceX2 = (planet1.x - planet2.x) * (planet1.x - planet2.x);
                distanceY2 = (planet1.y - planet2.y) * (planet1.y - planet2.y);

                // Calculate accelleration in X and Y
                accelerationX = 
                */

            }

        }
        
        public void run() {

            // Calculate force
            // for each of the worker's planets
            //  calculate next position of the planet
            for(int i = startPlanetIndex; i <= endPlanetIndex; i++){
                calculateForce(i);
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
        }
    }
}

