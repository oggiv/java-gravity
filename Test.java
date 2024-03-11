import java.awt.Dimension;
import java.util.concurrent.TimeUnit;
import javax.swing.*;

public class Test {
    public static void main(String[] args) {
        int w = 600;
        int h = 600;
        int np = 3;

        JFrame f = new JFrame();
        Draw ds = new Draw(w, h, np);
        ds.setPreferredSize(new Dimension(w, h));

        ds.addCircle(0, 20, 20, 30);
        ds.addCircle(1, 500, 560, 20);
        ds.addCircle(2, 400, 40, 60);

        f.setSize(w, h);
        f.setTitle("N-Body Problem");

        f.add(ds);

        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setVisible(true);

        while(true){
            try{
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException ex){}
            for(int i = 0; i < np; i++){
                ds.moveCircle(i, 1, 1);
            }
            ds.redo();
        }
    }
    
}
