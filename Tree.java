public class Tree {
    Node root;

    public Tree(Node root) {
        this.root = root;
    }

    public void createTree(Planet[] planets){
        for(Planet pl : planets){
            this.root.addPlanet(pl);
        }
    }

    public void printTree(){
        
    }
}
