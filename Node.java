import java.util.ArrayList;

public class Node {
    public double topLeftX;
    public double topLeftY;
    public double height;
    public double width;

    public String id;
    public int level;

    public Node[] quadrant;

    public Planet planet;

    public double centerX;
    public double centerY;
    public double mass;
    public double xcomp;
    public double ycomp;

    public Node(double x, double y, double height, double width, String id, int level){
        this.id = id;
        this.level = level;
        this.topLeftX = x;
        this.topLeftY = y;
        this.height = height;
        this.width = width;
        quadrant = new Node[4];
        for(int i = 0; i < 4; i++){
            quadrant[i] = null;
        }
        centerX = -1;
        centerY = -1;
        mass = 0;
        planet = null;
        xcomp = 0;
        ycomp = 0;
    }

    public void addPlanet(Planet planet){ // add(Planet newPlanet)

        // if a planet "passes through" this node, it's mass should be included in this node's total
        this.mass += planet.mass;
        this.xcomp += planet.mass * planet.getX();
        this.ycomp += planet.mass * planet.getY();

        if(!this.hasPlanet() && !this.hasChildren()){ // does it have child nodes?
            // no planet in node and no child nodes -> add planet to node
            this.planet = planet;
            //System.out.println(this.planet.toString());
        }
        else{
            // planet already in node -> split node into 4, add planets to the corresponding nodes

            if (!this.hasChildren()) {
                quadrant[0] = new Node(topLeftX, topLeftY, height/2, width/2, id + "-0", this.level + 1);
                quadrant[1] = new Node(topLeftX + width/2, topLeftY, height/2, width/2, id + "-1", this.level + 1);
                quadrant[2] = new Node(topLeftX + width/2, topLeftY + height/2, height/2, width/2, id + "-2", this.level + 1);
                quadrant[3] = new Node(topLeftX, topLeftY + height/2, height/2, width/2, id + "-3", this.level + 1);
            }
            
            // Figure out which quadrant a new planet goes to and put it there
            sendPlanet(planet);

            // If there is a planet in this node already, send it to the children
            if(this.hasPlanet()){
                sendPlanet(this.planet);
                this.planet = null;
            }
        }

        // center stuff
        /* 
        System.out.println(" hasPlanet: " + this.hasPlanet());
        if(this.hasPlanet()){
            // no children -> center of gravity is the position of your only planet
            centerX = this.planet.getX();
            centerY = this.planet.getY();
            System.out.println("One planet center of " + id + ": " + centerX + ":" + centerY);
        }
        else{
            this.calculateCenter();
            //System.out.println("Calculated center of " + id + ": " + centerX + ":" + centerY);
        }

        */

        this.centerX = this.xcomp / this.mass;
        this.centerY = this.ycomp / this.mass;
    } 

    /*private void calculateCenter(){
        double xTotal = 0;
        double yTotal = 0;
        for(int i = 0; i < 4; i++){
            xTotal += quadrant[i].centerX * quadrant[i].mass;
            yTotal += quadrant[i].centerY * quadrant[i].mass;        
        }
        this.centerX = xTotal / mass;
        this.centerY = yTotal / mass;
    }*/

    public boolean hasPlanet(){
        return this.planet != null;
    }
    
    public boolean hasChildren(){
        return this.quadrant[0] != null;
    }

    public void sendPlanet(Planet planet){
        if (planet.getX() < (topLeftX + (width / 2))) {
            // left of vertical line -> quadrant 1 or 4
            if (planet.getY() < (topLeftY + (height / 2))) {
                // above horizontal line -> quadrant 1
                quadrant[0].addPlanet(planet);
                //System.out.println("Sending planet " + planet.id + " to q1");
            }
            else if(planet.getY() < (topLeftY + height)){
                // below horizontal line -> quadrant 4
                quadrant[3].addPlanet(planet);
                //System.out.println("Sending planet " + planet.id + " to q4");
            }
            return;
        }
        else if(planet.getX() < topLeftX + width){
            // right of vertical line -> quadrant 2 or 3
            if (planet.getY() < (topLeftY + (height / 2))) {
                // above horizontal line -> quadrant 2
                quadrant[1].addPlanet(planet);
                //System.out.println("Sending planet " + planet.id + " to q2");
            }
            else if(planet.getY() < (topLeftY + height)){
                // below horizontal line -> quadrant 3
                quadrant[2].addPlanet(planet);
                //System.out.println("Sending planet " + planet.id + " to q3");
            }
            return;
        }

        System.err.println("Can't send planet. Something is weird with it. " + planet.toString());
    }
}
