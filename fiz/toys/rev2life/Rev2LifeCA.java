import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;

/**
 * Reversible cellular automaton.
 * @since 04/30/2006
 * @author Peter Csizmadia
 */
public class Rev2LifeCA extends JApplet implements Runnable {

    private static int XSIZE = 32;
    private static int YSIZE = 32;
    private int mirrorStep = 80;
    private int stepCount = 0;
    private byte[] prevState = null;
    private byte[] currentState = null;
    private boolean startIt = true;

    private Thread runner;
    private long time;
    private long delay = 250;

    // screen
    private int screenwidth, screenheight;

    // double buffering
    private Image backimg;
    private Graphics2D backg;

    private void createInitialState() {
	prevState = new byte[XSIZE*YSIZE];
	currentState = new byte[XSIZE*YSIZE];
	initCells();
	ProgressMonitor pm = new ProgressMonitor(this,
	    "Thinking...", null, 0, mirrorStep);
	for(int i = 0; i < mirrorStep + 1; ++i) {
	    step();
	    if(pm.isCanceled()) {
		mirrorStep = i + 1;
		break;
	    }
	    pm.setProgress(i + 1);
	}
	invert();
	stepCount = 0;
    }

    private void initCells() {
	for(int k = 0; k < currentState.length; ++k) {
	    prevState[k] = 0;
	    currentState[k] = 0;
	}
	int i0 = (YSIZE - 7)/2;
	int j0 = (XSIZE - 3)/2;
	// t
	setState(currentState, i0, j0, 1);
	setState(currentState, i0 + 1, j0, 1);
	setState(currentState, i0 + 2, j0 - 1, 1);
	setState(currentState, i0 + 2, j0, 1);
	setState(currentState, i0 + 2, j0 + 1, 1);
	setState(currentState, i0 + 2, j0 + 2, 1);
	setState(currentState, i0 + 3, j0, 1);
	setState(currentState, i0 + 4, j0, 1);
	setState(currentState, i0 + 5, j0, 1);
	setState(currentState, i0 + 5, j0 + 3, 1);
	setState(currentState, i0 + 6, j0 + 1, 1);
	setState(currentState, i0 + 6, j0 + 2, 1);
	// arrow
	for(int k = 0; k < 24; ++k) {
	    setState(currentState, i0 + 8, j0 - 8 + k, 1);
	}
	j0 += 13;
	setState(currentState, i0 + 6, j0 - 3, 1);
	setState(currentState, i0 + 6, j0 - 2, 1);
	setState(currentState, i0 + 7, j0 - 1, 1);
	setState(currentState, i0 + 7, j0, 1);
	setState(currentState, i0 + 9, j0, 1);
	setState(currentState, i0 + 9, j0 - 1, 1);
	setState(currentState, i0 + 10, j0 - 2, 1);
	setState(currentState, i0 + 10, j0 - 3, 1);
    }

    private static int getState(byte[] state, int i, int j) {
	i = (i + YSIZE) % YSIZE;
	j = (j + XSIZE) % XSIZE;
	int k = i*XSIZE + j;
	return state[k];
    }

    private static void setState(byte[] state, int i, int j, int x) {
	i = (i + YSIZE) % YSIZE;
	j = (j + XSIZE) % XSIZE;
	int k = i*XSIZE + j;
	state[k] = (byte)x;
    }

    public void step() {
	step(prevState, currentState);
	++stepCount;
    }

    public static void step(byte[] prevstate, byte[] state) {
	int n = state.length;
	byte[] newstate = new byte[n];
	int k = 0;
	for(int i = 0; i < YSIZE; ++i) {
	    for(int j = 0; j < XSIZE; ++j) {
		int nn = 0;
		for(int ii = -1; ii <= 1; ++ii) {
		    for(int jj = -1; jj <= 1; ++jj) {
			if(ii != 0 || jj != 0) {
			    if(getState(state, i + ii, j + jj) == 1) {
				++nn;
			    }
			}
		    }
		}
		int fn;
		if(nn == 3 || (nn == 2 && state[k] == 1)) {
		    fn = 1;
		} else {
		    fn = 0;
		}
		newstate[k] = (byte) (fn ^ prevstate[k]);
		++k;
	    }
	}
	System.arraycopy(state, 0, prevstate, 0, n);
	System.arraycopy(newstate, 0, state, 0, n);
    }

    public void invert() {
	byte[] tmp = prevState;
	prevState = currentState;
	currentState = tmp;
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
	if(currentState == null) {
	    String s = "Creating initial state...";
	    backg.setFont(new Font("SansSerif", Font.PLAIN, 18));
	    FontMetrics fm = backg.getFontMetrics();
	    int tw = fm.stringWidth(s);
	    backg.drawString(s, (d.width - tw)/2, d.height/2);
	    return;
	}
	int k = 0;
	double c = Math.min((double)d.width/XSIZE, (double)d.height/YSIZE);
	for(int i = 0; i < YSIZE; ++i) {
	    double y = c*i;
	    for(int j = 0; j < XSIZE; ++j) {
		if(currentState[k] != 0) {
		    double x = c*j;
		    Shape r = new Rectangle2D.Double(x, y, c, c);
		    backg.setColor(new Color(0x999999));
		    backg.fill(r);
		    backg.setColor(Color.black);
		    backg.draw(r);
		}
		++k;
	    }
	}
	backg.setFont(new Font("SansSerif", Font.PLAIN, 14));
	FontMetrics fm = backg.getFontMetrics();
	String s = ""+stepCount+" steps";
	int tw = fm.stringWidth(s);
	int th = fm.getHeight();
	int tasc = fm.getAscent();
	int tdesc = fm.getDescent();
	backg.setColor(new Color(0xccffffff, true));
	backg.fillRect(0, th - tasc, tw, tasc + tdesc);
	backg.setColor(Color.black);
	backg.drawString(s, 0, th);
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
	    step();
	    step();
	    time += delay;
	    try {
		thisthread.sleep(Math.max(1, time - System.currentTimeMillis()));
	    } catch(InterruptedException exc) {
	    }
	    if(stepCount == mirrorStep) {
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
    }
    public static void main(String[] args) {
	final JFrame f = new JFrame();
	final JApplet applet = new Rev2LifeCA();
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
