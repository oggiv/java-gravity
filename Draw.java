import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.time.Duration;
import java.time.Instant;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

import javafx.scene.shape.Ellipse;

public class Draw extends JComponent{

    private int width;
    private int height;
    private Ellipse2D.Double[] circles;
    private Color[] colours;
    

    public Draw(int w, int h, int numPlanets){
        width = w;
        height = h;
        circles = new Ellipse2D.Double[numPlanets];

        colours = new Color[10];
        colours[0] = new Color(211, 114, 143);
        colours[1] = new Color(218, 172, 249);
        colours[2] = new Color(140, 208, 242);
        colours[3] = new Color(52, 117, 76);
        colours[4] = new Color(128, 196, 107);
        colours[5] = new Color(171, 59, 219);
        colours[6] = new Color(109, 100, 63);
        colours[7] = new Color(03, 11, 183);
        colours[8] = new Color(27, 247, 129);
        colours[9] = new Color(61, 0, 32);
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
        System.out.println("Adding circle " + id + ": " + xx + ", " + yy);
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