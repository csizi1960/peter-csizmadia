import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;

/**
 * Simulation of a system of interacting particles with decreasing entropy.
 * @since 04/29/2006
 * @author Peter Csizmadia
 */
public class Ido extends JApplet implements Runnable {

    // simulation
    private static double DT = 1/8192.0;
    private static double XSIZE = 10;
    private static double YSIZE = 10;
    private int numParticles;
    private int mirrorStep = 51200;
    private int stepCount = 0;
    private double physicalTime = 0;
    private double[] xCoords = null;
    private double[] xCoords2;
    private double[] xCoords3;
    private double[] xCoords4;
    private double[] rk4k1;
    private double[] rk4k2;
    private double[] rk4k3;
    private double[] rk4k4;

    // visualization
    private Thread runner;
    private boolean startIt = true;
    private long time;
    private long delay = 50;
    private int stepsPerUpdate = 1;
    private int screenwidth, screenheight;
    private Image backimg;
    private Graphics2D backg;

    private void createInitialState() {
	createIdoInitialState();
	ProgressMonitor pm = new ProgressMonitor(this,
	    "Thinking...", null, 0, mirrorStep);
	long t0 = System.currentTimeMillis();
	for(int i = 0; i < mirrorStep; ++i) {
	    step(DT);
	    if(pm.isCanceled()) {
		mirrorStep = i + 1;
		break;
	    }
	    pm.setProgress(i + 1);
	}
	long t = System.currentTimeMillis() - t0;
	if(t == 0) {
	    stepsPerUpdate = 500;
	} else {
	    stepsPerUpdate = Math.max((int)(0.9*mirrorStep*delay/t), 1);
	    if(stepsPerUpdate > 500) {
		stepsPerUpdate = 500;
	    }
	}
	int n = numParticles;
	for(int i = 0; i < n; ++i) {
	    // change the signs of velocities
	    xCoords[i + 2*n] = -xCoords[i + 2*n];
	    xCoords[i + 3*n] = -xCoords[i + 3*n];
	}
	stepCount = 0;
	physicalTime = 0;
    }

    private void createIdoInitialState() {
	int n = 24;
	numParticles = n;
	xCoords = new double[4*n];
	xCoords2 = new double[4*n];
	xCoords3 = new double[4*n];
	xCoords4 = new double[4*n];
	rk4k1 = new double[4*n];
	rk4k2 = new double[4*n];
	rk4k3 = new double[4*n];
	rk4k4 = new double[4*n];
	double[] x = xCoords;
	double x0 = XSIZE/2 - 2.5;
	double y0 = YSIZE/2 - 1.5;
	// I
	x[0] = x0; x[0 + n] = y0;
	x[1] = x0; x[1 + n] = y0 + 0.5;
	x[2] = x0; x[2 + n] = y0 + 1.0;
	x[3] = x0; x[3 + n] = y0 + 1.5;
	// D
	x0 += 1.0;
	x[4] = x0; x[4 + n] = y0;
	x[5] = x0; x[5 + n] = y0 + 0.5;
	x[6] = x0; x[6 + n] = y0 + 1.0;
	x[7] = x0; x[7 + n] = y0 + 1.5;
	x0 += 0.7;
	x[8] = x0; x[8 + n] = y0;
	x[9] = x0; x[9 + n] = y0 + 1.5;
	x0 += 0.5;
	x[10] = x0; x[10 + n] = y0 + 0.5;
	x[11] = x0; x[11 + n] = y0 + 1.0;
	// O
	x0 += 1.0;
	x[12] = x0; x[12 + n] = y0 + 0.5;
	x[13] = x0; x[13 + n] = y0 + 1.0;
	x0 += 0.5;
	x[14] = x0; x[14 + n] = y0;
	x[15] = x0; x[15 + n] = y0 + 1.5;
	x[16] = x0; x[16 + n] = y0 + 2.5;
	x[17] = x0; x[17 + n] = y0 + 3.0;
	x0 += 0.75;
	x[18] = x0; x[18 + n] = y0;
	x[19] = x0; x[19 + n] = y0 + 1.5;
	x[20] = x0; x[20 + n] = y0 + 2.5;
	x[21] = x0; x[21 + n] = y0 + 3.0;
	x0 += 0.5;
	x[22] = x0; x[22 + n] = y0 + 0.5;
	x[23] = x0; x[23 + n] = y0 + 1.0;
	for(int i = 0; i < n; ++i) {
	    double vr = 1;
	    double phi = Math.PI/2 + 2*i*Math.PI/n;
	    x[i + 2*n] = vr*Math.cos(phi);
	    x[i + 3*n] = vr*Math.sin(phi);
	}
    }

    private void step(double dt) {
	// 4th order Runge-Kutta method

	int n = numParticles;
	double[] dx = new double[4];
	double[] k1 = rk4k1;
	double[] k2 = rk4k2;
	double[] k3 = rk4k3;
	double[] k4 = rk4k4;
	for(int i = 0; i < n; ++i) {
	    calcForce(i, n, xCoords, dx);
	    k1[i] = dx[0];
	    k1[i + n] = dx[1];
	    k1[i + 2*n] = dx[2];
	    k1[i + 3*n] = dx[3];
	    xCoords2[i] = xCoords[i] + dx[0]*dt/2;
	    xCoords2[i + n] = xCoords[i + n] + dx[1]*dt/2;
	    xCoords2[i + 2*n] = xCoords[i + 2*n] + dx[2]*dt/2;
	    xCoords2[i + 3*n] = xCoords[i + 3*n] + dx[3]*dt/2;
	}
	for(int i = 0; i < n; ++i) {
	    calcForce(i, n, xCoords2, dx);
	    k2[i] = dx[0];
	    k2[i + n] = dx[1];
	    k2[i + 2*n] = dx[2];
	    k2[i + 3*n] = dx[3];
	    xCoords3[i] = xCoords[i] + dx[0]*dt/2;
	    xCoords3[i + n] = xCoords[i + n] + dx[1]*dt/2;
	    xCoords3[i + 2*n] = xCoords[i + 2*n] + dx[2]*dt/2;
	    xCoords3[i + 3*n] = xCoords[i + 3*n] + dx[3]*dt/2;
	}
	for(int i = 0; i < n; ++i) {
	    calcForce(i, n, xCoords3, dx);
	    k2[i] = dx[0];
	    k2[i + n] = dx[1];
	    k2[i + 2*n] = dx[2];
	    k2[i + 3*n] = dx[3];
	    xCoords4[i] = xCoords[i] + dx[0]*dt;
	    xCoords4[i + n] = xCoords[i + n] + dx[1]*dt;
	    xCoords4[i + 2*n] = xCoords[i + 2*n] + dx[2]*dt;
	    xCoords4[i + 3*n] = xCoords[i + 3*n] + dx[3]*dt;
	}
	for(int i = 0; i < n; ++i) {
	    calcForce(i, n, xCoords4, dx);
	    k4[i] = dx[0];
	    k4[i + n] = dx[1];
	    k4[i + 2*n] = dx[2];
	    k4[i + 3*n] = dx[3];
	}
	for(int i = 0; i < 4*n; ++i) {
	    xCoords[i] += (k1[i] + k2[i] + k3[i] + k4[i])*dt/6;
	}
	++stepCount;
	physicalTime += dt;
    }

/*    private void step(double dt) {
	int n = numParticles;
	double[] dx = new double[4];
	for(int i = 0; i < n; ++i) {
	    calcForce(i, n, xCoords, dx);

	    double x = xCoords[i];
	    double y = xCoords[i + n];
	    double vx = dx[0];
	    double vy = dx[1];
	    double ax = dx[2];
	    double ay = dx[3];
	    xCoords2[i] = x + (vx + ax*dt/2)*dt;     // dx = x + (vx+ax*dt/2)*dt
	    xCoords2[i + n] = y + (vy + ay*dt/2)*dt; // dy = y + (vy+ay*dt/2)*dt
	    xCoords2[i + 2*n] = vx + ax*dt;          // dvx = vx + ax*dt
	    xCoords2[i + 3*n] = vy + ay*dt;          // dvy = vy + ay*dt
	}
	System.arraycopy(xCoords2, 0, xCoords, 0, xCoords.length);
	++stepCount;
	physicalTime += dt;
    }*/

    private static void calcForce(int i, int n, double[] x,
				  double[] dx) {
	double ax = 0;
	double ay = 0;
	for(int j = 0; j < n; ++j) {
	    if(i != j) {
		double xij = x[i] - x[j];
		double yij = x[i + n] - x[j + n];
		double r = Math.sqrt(xij*xij + yij*yij);
		double a = (1/r - 1)/(r*r*r);
		ax += a*xij;
		ay += a*yij;
	    }
	}
	dx[0] = x[i + 2*n]; // dx/dt = vx
	dx[1] = x[i + 3*n]; // dy/dt = vy
	dx[2] = ax;         // dvx/dt = ax
	dx[3] = ay;         // dvy/dt = ay
    }

    private void createBackBuffer(Dimension d) {
	if(backimg != null) {
	    backimg.flush();
	}
	backimg = createImage(d.width, d.height);
	backg = (Graphics2D)backimg.getGraphics();
	backg.setColor(Color.white);
	backg.fillRect(0, 0, d.width, d.height);
	backg.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
			       RenderingHints.VALUE_ANTIALIAS_ON);
	screenwidth = d.width;
	screenheight = d.height;
    }

    public void stop() {
	if(runner != null) {
	    runner = null;
	}
    }
    public void destroy() {
	if(backimg != null) {
	    backimg.flush();
	}
    }
    public void paint(Graphics g) {
	if(startIt) {
	    createBackBuffer(getSize());
	    time = System.currentTimeMillis();
	    if(runner == null) {
		(runner = new Thread(this)).start();
	    }
	    startIt = false;
	}
	update(g);
    }
    public synchronized void update(Graphics g) {
	if(backimg != null) {
	    g.drawImage(backimg, 0, 0, null);
	}
	notifyAll();
    }

    private void paintState() {
	Dimension d = getSize();
	if(d.width != screenwidth || d.height != screenheight) {
	    createBackBuffer(d);
	}
	backg.setColor(Color.white);
	backg.fillRect(0, 0, d.width, d.height);
	backg.setColor(Color.black);
	if(xCoords == null) {
	    String s = "Creating initial state...";
	    backg.setFont(new Font("SansSerif", Font.PLAIN, 18));
	    int w = backg.getFontMetrics().stringWidth(s);
	    backg.drawString(s, (d.width - w)/2, d.height/2);
	    return;
	}
	backg.setFont(new Font("SansSerif", Font.PLAIN, 14));
	int th = backg.getFontMetrics().getHeight();
	backg.drawString(""+stepCount+" steps", 0, th);
	backg.setFont(new Font("SansSerif", Font.PLAIN, 12));
	backg.drawString("t="+physicalTime, 0, 2*th);
	double c = Math.min(d.width/XSIZE, d.height/YSIZE);
	for(int i = 0; i < numParticles; ++i) {
	    double x = c*xCoords[i];
	    double y = d.height - c*xCoords[i + numParticles];
	    if(x >= 0 && y >= 0 && x < d.width && y < d.height) {
		double r = c/4;
		backg.setColor(new Color(0x999999));
		backg.fill(new Ellipse2D.Double(x-r, y-r, 2*r, 2*r));
	    }
	}
    }

    public void run() {
	Thread thisthread = Thread.currentThread();
	if(thisthread == null) {
	    return;
	}
	synchronized(this) {
	    paintState();
	    repaint();
	    try {
		wait();
	    } catch(InterruptedException exc) {
	    }
	}
	createInitialState();
	long startTime = System.currentTimeMillis();
	synchronized(this) {
	    time = startTime;
	}
	loop: while(runner == thisthread) {
	    // request update() and wait for it to complete
	    synchronized(this) {
		paintState();
		repaint(); // must be inside synchronized!!! otherwise bug in
			   // in java 1.3
		try {
		    wait();
		} catch(InterruptedException exc) {
		}
	    }
	    for(int i = 0; i < stepsPerUpdate; ++i) {
		step(DT);
		if(stepCount == mirrorStep) {
		    time += delay;
		    try {
			thisthread.sleep(Math.max(1,
				    time - System.currentTimeMillis()));
		    } catch(InterruptedException exc) {
		    }
		    synchronized(this) {
			paintState();
			repaint();
		    }
		    try {
			thisthread.sleep(2000);
		    } catch(InterruptedException exc) {
		    }
		    time = System.currentTimeMillis();
		    continue loop;
		}
	    }
	    time += delay;
	    try {
		thisthread.sleep(Math.max(1, time - System.currentTimeMillis()));
	    } catch(InterruptedException exc) {
	    }
	}
    }
    public static void main(String[] args) {
	final JFrame f = new JFrame();
	final JApplet applet = new Ido();
	f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	f.setContentPane(applet);
	f.pack();
	f.setSize(new Dimension(400, 400));
	SwingUtilities.invokeLater(new Runnable() {
	    public void run() {
		f.setVisible(true);
	    }
	});
    }
}
