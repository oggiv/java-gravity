import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.Duration;
import java.time.Instant;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

public class Draw {

    public static void main(String[] args) {
        new Draw();
    }

    public Draw() {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                JFrame frame = new JFrame();
                frame.add(new TestPane());
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            }
        });
    }

    public static class Ticker {

        public interface Callbck {
            public void didTick(Ticker ticker);
        }

        private Timer timer;

        private Callbck callback;

        public void setCallback(Callbck tick) {
            this.callback = tick;
        }

        public void start() {
            if (timer != null) {
                return;
            }
            timer = new Timer(5, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (callback == null) {
                        return;
                    }
                    callback.didTick(Ticker.this);
                }
            });
            timer.start();
        }

        public void stop() {
            if (timer == null) {
                return;
            }
            timer.stop();
            timer = null;
        }

    }

    public class TestPane extends JPanel {

        int posX;

        private Ticker ticker;
        private Instant startedAt;
        private Duration duration = Duration.ofSeconds(5);

        public TestPane() {
            ticker = new Ticker();
            ticker.setCallback(new Ticker.Callbck() {
                @Override
                public void didTick(Ticker ticker) {
                    if (startedAt == null) {
                        startedAt = Instant.now();
                    }
                    Duration runtime = Duration.between(startedAt, Instant.now());
                    double progress = runtime.toMillis() / (double) duration.toMillis();

                    if (progress >= 1.0) {
                        stopAnimation();
                    }

                    posX = (int) (getWidth() * progress);
                    repaint();
                }
            });
        }

        protected void startAnimtion() {
            ticker.start();
        }

        protected void stopAnimation() {
            ticker.stop();
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(200, 200);
        }

        @Override
        public void addNotify() {
            super.addNotify();
            startAnimtion();
        }

        @Override
        public void removeNotify() {
            super.removeNotify();
            stopAnimation();
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setColor(Color.BLACK);
            int midY = getHeight() / 2;
            g2d.fillOval(posX, midY - 5, 10, 10);
            g2d.dispose();
        }

    }
}