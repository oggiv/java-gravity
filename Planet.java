public class Planet {
    int id;
    double mass;

    boolean phase;
    double x1, y1;
    double x2, y2;

    double xVel, yVel;

    public String toString() {
        // pretty string formatter
        return String.format("Planet id:%d mass:%.2f position1:(%.2f,%.2f) position2:(%.2f,%.2f) velocity:(%.2f,%.2f)", this.id, this.mass, this.x1, this.y1, this.x2, this.y2, this.xVel, this.yVel);
    }

    public double getX() {
        if (phase) {
            return x1;
        }
        else {
            return x2;
        }
    }

    public double getY() {
        if (phase) {
            return y1;
        }
        else {
            return y2;
        }
    }

    public void setX(double newX) {
        if (phase) {
            x2 = newX;
        }
        else {
            x1 = newX;
        }
    }

    public void setY(double newY) {
        if (phase) {
            y2 = newY;
        }
        else {
            y1 = newY;
        }
    }

    public void updateCoordinates() {
        // switch to new values for getX/Y and setX/Y
        this.phase = !this.phase;
    }

    public Planet(int id, double mass, double x1, double y1, double xVel, double yVel) {
        this.id = id;
        this.mass = mass;
        this.phase = true;
        this.x1 = x1;
        this.y1 = y1;
        this.xVel = xVel;
        this.yVel = yVel;
    }
}
