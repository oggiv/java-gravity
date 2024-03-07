public class Planet {
    int id;
    double mass;

    double x1, y1;
    double x2, y2;

    double xVel, yVel;

    public String toString() {
        // pretty string formatter
        return String.format("Planet id:%d mass:%d position1:(%d,%d) position2:(%d,%d) velocity:(%d,%d)", this.id, this.mass, this.x1, this.y1, this.x2, this.y2, this.xVel, this.yVel);
    }

    public Planet(int id, double mass, double x1, double y1, double xVel, double yVel) {
        this.id = id;
        this.mass = mass;
        this.x1 = x1;
        this.y1 = y1;
        this.xVel = xVel;
        this.yVel = yVel;
    }
}
