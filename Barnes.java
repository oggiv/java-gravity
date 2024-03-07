// Barnes.java

import java.io.BufferedReader;
import java.io.FileReader;

public class Barnes {

    public static void main(String[] args) {

        /*
            Reads planets from a .csv file.
            First line should be the amount of planets to read as an integer.
            Following lines should be planets of the format:
                mass positionX positionY velocityX velocityY
        */

        int amountOfPlanets = 0;
        Planet[] planets;

        if (0 < args.length) {
            try (BufferedReader buff = new BufferedReader(new FileReader(file))) {
                // read the amount of planets
                String line = buff.readLine(); // does it remove the new line sign?
                amountOfPlanets = Integer.parseInt(line);

                // allocate array for planets
                planets = new Planet[amountOfPlanets];

                // read and create planets
                int id = 0;
                while ((line = buff.readLine()) != null) {
                    String[] row = line.split(",");
                    planets[id] = new Planet(id, Double.parseDouble(line[0]), Double.parseDouble(line[1]), Double.parseDouble(line[2]), Double.parseDouble(line[3]), Double.parseDouble(line[4]));
                }
            } catch (Exception e) {
                System.out.println("File " + file + " not found or corrupt!");
            }
        }
        //Space space = new Space();
    }

    private void Worker() {

    }
}
