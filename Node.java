public class Node {
    double topLeftX;
    double topLeftY;
    double height;
    double width;

    Node[] quadrant;

    Planet planet;

    double centerX;
    double centerY;
    double mass;

    public Node(double x, double y, double height, double width){
        topLeftX = x;
        topLeftY = y;
        this.height = height;
        this.width = width;
        for(int i = 0; i < 4; i++){
            quadrant[i] = null;
        }
        centerX = -1;
        centerY = -1;
        mass = 0;
    }

    public void addPlanet(Planet planet){ // add(Planet newPlanet)

        // if a planet "passes through" this node, it's mass should be included in this node's total
        this.mass += planet.mass;

        if(this.hasPlanet() && this.hasChildren()){ // does it have child nodes?
            // no planet in node and no child nodes -> add planet to node
            this.planet = planet;
            return;
        }
        else{
            // planet already in node -> split node into 4, add planets to the corresponding nodes

            if (this.hasChildren()) {
                quadrant[0] = new Node(topLeftX, topLeftY, height/2, width/2);
                quadrant[1] = new Node(topLeftX + width/2, topLeftY, height/2, width/2);
                quadrant[2] = new Node(topLeftX + width/2, topLeftY + height/2, height/2, width/2);
                quadrant[3] = new Node(topLeftX, topLeftY + height/2, height/2, width/2);
            }
            
            // Figure out which quadrant a new planet goes to and put it there
            if (planet.getX() < (topLeftX + (width / 2))) {
                // left of vertical line -> quadrant 1 or 4
                if (planet.getY() < (topLeftY + (height / 2))) {
                    // above horizontal line -> quadrant 1
                    quadrant[0].addPlanet(planet);
                }
                else {
                    // below horizontal line -> quadrant 4
                    quadrant[3].addPlanet(planet);
                }
            }
            else {
                // right of vertical line -> quadrant 2 or 3
                if (planet.getY() < (topLeftY + (height / 2))) {
                    // above horizontal line -> quadrant 2
                    quadrant[1].addPlanet(planet);
                }
                else {
                    // below horizontal line -> quadrant 3
                    quadrant[2].addPlanet(planet);
                }
            }

            // If there is a planet in this node already, send it to the children
            if(this.hasPlanet()){
                if (this.planet.getX() < (topLeftX + (width / 2))) {
                    // left of vertical line -> quadrant 1 or 4
                    if (this.planet.getY() < (topLeftY + (height / 2))) {
                        // above horizontal line -> quadrant 1
                        quadrant[0].addPlanet(planet);
                    }
                    else {
                        // below horizontal line -> quadrant 4
                        quadrant[3].addPlanet(planet);
                    }
                }
                else {
                    // right of vertical line -> quadrant 2 or 3
                    if (this.planet.getY() < (topLeftY + (height / 2))) {
                        // above horizontal line -> quadrant 2
                        quadrant[1].addPlanet(planet);
                    }
                    else {
                        // below horizontal line -> quadrant 3
                        quadrant[2].addPlanet(planet);
                    }
                }
                this.planet = null;
            }
        }

        // center stuff
        if(this.hasChildren()){
            // no children -> center of gravity is the position of your only planet
            centerX = planet.getX();
            centerY = planet.getY();
        }
        else{
            this.calculateCenter();
        }
    } 

    private void calculateCenter(){
        double xTotal = 0;
        double yTotal = 0;
        for(int i = 0; i < 4; i++){
            if(quadrant[i].hasPlanet()) continue;
            xTotal += quadrant[i].centerX * quadrant[i].mass;
            yTotal += quadrant[i].centerY * quadrant[i].mass;        
        }
        this.centerX = xTotal / mass;
        this.centerY = yTotal / mass;
    }

    public boolean hasPlanet(){
        return this.planet != null;
    }
    
    public boolean hasChildren(){
        return this.quadrant[0] != null;
    }
}
