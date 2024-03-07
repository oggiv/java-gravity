// Barnes.java

import java.io.BufferedReader;
import java.io.FileReader;

public class Barnes {

    public static void main(String[] args) {

        /*
            Reads planets from a .csv file.
            First line should be the amount of planets to read as an integer.
            Following lines should be planets of the format:
                mass,positionX,positionY,velocityX,velocityY
        */

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
                }
            } catch (Exception e) {
                System.out.println("File " + args[0] + " could not be opened.");
            }
        }
        //Space space = new Space();

        for (Planet planet : planets) {
            System.out.println(planet.toString());
        }
    }

    private void Worker() {

    }
}
