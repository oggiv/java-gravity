import java.util.ArrayDeque;
import java.util.Deque;

public class Tree {
    Node root;
    double height;
    double width;

    public Tree(double height, double width) {
        this.height = height;
        this.width = width;
        this.root = null;
    }

    public void createTree(Planet[] planets){
        this.root = new Node(0, 0, height, width, "0", 0);
        for(Planet pl : planets){
            this.root.addPlanet(pl);
        }
    }

    public void printTree(){
        Deque<Node> queue = new ArrayDeque<>();
        queue.addLast(this.root);

        Node current;
        while (queue.peekFirst() != null) {
            current = queue.pollFirst();
            if (current.hasPlanet()) {
                System.out.println(current.id + ": " + current.planet.toString());
            }
            else {
                System.out.println(current.id + ": Empty node");
            }
            Node child;
            for (int i = 3; i > -1; i--) {
                child = current.quadrant[i];
                if (child != null) {
                    queue.addFirst(child);
                }
            }
        }
    }

    public void prettyPrint(){
        Deque<Node> queue = new ArrayDeque<>();
        queue.addLast(this.root);

        Node current;
        while (queue.peekFirst() != null) {
            current = queue.pollFirst();
            System.out.print("╠");
            //System.out.print("mass: " + current.mass + "\t╠");
            //System.out.print("center: " + current.centerX + ":" + current.centerY + "\t\t╠");
            for(int i = 0; i < current.level; i++){
                System.out.print("═══");
            }
            if (current.hasPlanet()) {
                System.out.println(current.planet.toString());
            }
            else {
                System.out.println("Empty node");
            }
            Node child;
            for (int i = 3; i > -1; i--) {
                child = current.quadrant[i];
                if (child != null) {
                    queue.addFirst(child);
                }
            }
        }
    }
}
