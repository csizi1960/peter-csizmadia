import java.awt.*;
import java.util.Vector;


/**
 * Graph that displays the time evolution of the matter.
 * The quark/diquark/baryon portions are displayed.
 *
 * @version     0.1 11/03/1998
 * @author      Peter Csizmadia
 */
public class TimeevolCanvas extends Canvas {
    String[] titles;
    Color[] colors;
    Image backimg;
    Graphics backg;
    Vector[] NV;
    int graphx;

    public TimeevolCanvas(String[] t) {
	titles = new String[t.length];
	colors = new Color[t.length];
	NV = new Vector[t.length];
	for(int i=0; i<t.length; ++i) {
	    titles[i] = t[i].substring(6);
	    colors[i] = new Color(Integer.parseInt(t[i].substring(0, 6), 16));
	    NV[i] = new Vector();
	}
    }

    public void addP(double[] N) {
	double sum = 0;
	for(int i=0; i<N.length; ++i)
	    sum += N[i];
	double x = 0;
	for(int i=0; i<N.length; ++i) {
	    x += N[i]/sum;
	    NV[i].addElement(new Double(x));
	}
	repaint();
    }

    /**
     * Draw the titles and update the graph.
     */
    public void paint(Graphics frontg) {
	Dimension dim = size();
	int w = dim.width;
	int h = dim.height;
	if(backimg == null ||
	   backimg.getWidth(null) != w || backimg.getHeight(null) != h)
	{
	    if(backimg != null)
		backg.dispose();
	    backimg = createImage(w, h);
	    backg = backimg.getGraphics();
	}
	Graphics g = backg;
	g.setColor(getBackground());
	g.fillRect(0, 0, w, h);
	FontMetrics fm = g.getFontMetrics();
	int y = 0;
	int dx = fm.stringWidth("W");
	int dy = fm.getHeight();
	int x0 = 3*dx;
	int x1 = 4*dx;
	graphx = 0;
	for(int i=0; i<titles.length; ++i) {
	    g.setColor(colors[i]);
	    g.fillRect(0, y, x0, fm.getHeight());
	    g.setColor(Color.black);
	    String s = titles[i];
	    g.drawString(s, x1, y + fm.getAscent());
	    y += dy;
	    int x2 = x1 + fm.stringWidth(s) + dx;
	    if(graphx < x2)
		graphx = x2;
	}
	update(frontg);
    }

    /**
     * Update the graph.
     */
    public void update(Graphics frontg) {
	Dimension dim = size();
	int w = dim.width - graphx;
	int h = dim.height;
	Graphics g = backg;
	g.setColor(colors[0]);
	g.fillRect(graphx, 0, w, h);
	int nt = NV[0].size();
	int nt1 = (nt > 1)? nt-1 : 1;
	int npoints = nt1 + 3;
	int[] xpoints = new int[npoints];
	int[] ypoints = new int[npoints];
	for(int i=0; i<nt; ++i) {
	    xpoints[i] = graphx + (w-1)*i/nt1;
	}
	if(nt == 1)
	    xpoints[1] = graphx + w-1;
	xpoints[nt1+1] = xpoints[nt1];
	ypoints[nt1+1] = h-1;
	xpoints[nt1+2] = xpoints[0];
	ypoints[nt1+2] = h-1;
	for(int i=0; i<NV.length-1; ++i) {
	    Vector v = NV[i];
	    for(int j=0; j<nt; ++j) {
		ypoints[j] = (int)(h*((Double)v.elementAt(j)).doubleValue());
	    }
	    if(nt == 1)
		ypoints[1] = ypoints[0];
	    g.setColor(colors[i+1]);
	    g.fillPolygon(xpoints, ypoints, npoints);
	}
	frontg.drawImage(backimg, 0, 0, null);
    }
}
