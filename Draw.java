import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import javax.swing.JComponent;
import java.util.Random;


public class Draw extends JComponent{

    private int width;
    private int height;
    private Ellipse2D.Double[] circles;
    private Color[] colours;
    

    public Draw(int w, int h, int numPlanets){
        width = w;
        height = h;
        circles = new Ellipse2D.Double[numPlanets];

        Random rand = new Random();

        colours = new Color[numPlanets];
        for(int i = 0; i < numPlanets; i++){
            int r = rand.nextInt(230);
            int g = rand.nextInt(230);
            int b = rand.nextInt(230);
            colours[i] = new Color(r, g, b);
        }
    }

    @Override
    protected void paintComponent(Graphics g){
        Graphics2D g2d = (Graphics2D)g;
        for(int i = 0; i < circles.length; i++){
            g2d.setColor(colours[i]);
            g2d.fill(circles[i]);
        }
    }

    public void addCircle(int id, double x, double y, double r){
        double xx = x-(r/2);
        double yy = y-(r/2);
        //System.out.println("Adding circle " + id + ": " + xx + ", " + yy);
        circles[id] = new Ellipse2D.Double(xx, yy, r, r);
    }
    public void moveCircle(int id, double x, double y){
        double oldx = circles[id].getX();
        double oldy = circles[id].getY();
        double r = circles[id].getWidth();
        
        circles[id] = new Ellipse2D.Double(oldx + x, oldy + y, r, r);
    }

    public void redo(){
        repaint();
    }


}