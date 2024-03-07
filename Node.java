public class Node {
    double topLeftX;
    double topLeftY;
    double height;
    double width;

    Node[] quadrant;

    double centerX;
    double centerY;
    double mass;

    public Node(double x, double y, double height, double width, Planet planet){
        topLeftX = x;
        topLeftY = y;
        this.height = height;
        this.width = width;
        for(int i = 0; i < 4; i++){
            quadrant[i] = null;
        }
        centerX = planet.x1;
        centerY = planet.y1;
        mass = planet.mass;
    }

    public void add(int pos, Node node){
        quadrant[pos - 1] = node;
    } 

    public void calculateCenter(){
        double xTotal = 0;
        double yTotal = 0;
        for(int i = 0; i < 4; i++){
            if(quadrant[i] == null) continue;
            xTotal += quadrant[i].centerX * quadrant[i].mass;
            yTotal += quadrant[i].centerY * quadrant[i].mass;        
        }
        centerX = xTotal / mass;
        centerY = yTotal / mass;
    }

    public double calculateMass(){
        mass = 0;
        for(int i = 0; i < 4; i++){
            if(quadrant[i] == null) continue;
            mass += quadrant[i].mass;
        }
        return mass;
    }
}
