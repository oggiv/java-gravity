public class Node {
    double topLeftX;
    double topLeftY;
    double height;
    double width;

    Node[] quadrants;

    double centerX;
    double centerY;
    double mass;

    public Node(double x, double y, double height, double width){
        topLeftX = x;
        topLeftY = y;
        this.height = height;
        this.width = width;
        for(int i = 0; i < 4; i++){
            quadrants[i] = null;
        }
        centerX = -1;
        centerY = -1;
        mass = -1;
    }

    public void add(int pos, Node node){
        quadrants[pos - 1] = node;
    } 

    public double[] calculateCenter(){
        double x = 0;
        double y = 0;
        double xTotal = 0;
        double yTotal = 0;
        for(int i = 0; i < 4; i++){
            
        }
        return null;
    }

    public double calculateMass(){
        mass = 0;
        for(int i = 0; i < 4; i++){
            mass += quadrants[i].mass;
        }
        return mass;
    }
}
