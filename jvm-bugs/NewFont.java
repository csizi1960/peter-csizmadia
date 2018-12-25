import java.awt.*;

public class NewFont extends java.applet.Applet implements Runnable {
    Font font;
    long dt = 0;
    long N = 4;
    String s = "testing font creation speed...";
    Thread runner;

    public void start() {
        if(runner == null) {
            (runner = new Thread(this)).start();
        }
    }
    public void stop() {
        runner = null;
    }
    public void run() {
	Thread thread = Thread.currentThread();
	while(runner == thread) {
	    if(dt < 2000)
		N <<= 1;
	    long time = System.currentTimeMillis();
	    for(int i=0; i<N; ++i) {
		font = new Font("Helvetica", Font.PLAIN, 12);
		font = new Font("TimesRoman", Font.PLAIN, 12);
		font = new Font("Courier", Font.PLAIN, 12);
	    }
	    dt = System.currentTimeMillis()-time;
	    System.gc();
	    StringBuffer sbuf = new StringBuffer("font creation speed = ");
	    sbuf.append(3*N);
	    sbuf.append(" / ");
	    sbuf.append(dt);
	    sbuf.append("ms");
	    sbuf.append(" = ");
	    sbuf.append((dt > 0)? String.valueOf(3000.0*N/dt) : "???");
	    sbuf.append(" / s");
	    synchronized(this) {
		s = sbuf.toString();
		System.out.println(s);
		repaint();
		try {
		    wait();
		} catch(InterruptedException e) {
		}
	    }
	}
    }
    public void paint(Graphics g) {
	update(g);
    }
    public synchronized void update(Graphics g) {
	FontMetrics fm = g.getFontMetrics();
	Dimension d = size();
	g.setColor(Color.lightGray);
	g.fillRect(0, 0, d.width, d.height);
	g.setColor(Color.black);
	g.drawString(s, (d.width-fm.stringWidth(s))/2, d.height/2);
	notifyAll();
    }
}
