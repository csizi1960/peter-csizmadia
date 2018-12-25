import java.applet.Applet;
import java.awt.Color;
import java.awt.Event;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.net.URL;
import java.net.MalformedURLException;

/**
 * Magamon Kivuli Linkek applet.
 *
 * @version	1.0 08/26/1998
 * @author	Peter Csizmadia
 */ 
public class MagamonKivuliLinkek extends Applet
{
    int[] numlnks; // total number of links
    String linx_s[][][]; // the links are stored here
    boolean linx_visited[][];
    boolean crsr_onapplet, crsr_onlinx;
    int linx_w, linx_h;
    int crsr_x, crsr_y;
    int linx_y0;
    Font linkfont;
    int activetitle;
    int activelink;
    Color linkcolor=Color.blue, alinkcolor=Color.red, vlinkcolor=Color.magenta;
    Image buftwo;

    public String getAppletInfo() {
	return "MagamonKivuliLinkek 1.0, (C) 1998 Peter Csizmadia";
    }
    private Color getColorParameter(String name) {
	return new Color(Integer.parseInt(getParameter(name).substring(1), 16));
    }
    private Font getFontParameter(String name) {
	String s = getParameter(name);
	int k1 = s.indexOf("-", 0);
	String s1 = s.substring(0, k1);
	String s2 = s.substring(k1+1);
	int style = s1.equals("bold")? Font.BOLD : Font.PLAIN;
	int size = Integer.parseInt(s2);
	return new Font("TimesRoman", style, size);
    }
    public void init() {
// read applet parameters
	try {
	    setBackground(getColorParameter("bgcolor"));
	    linkcolor = getColorParameter("linkcolor");
	    alinkcolor = getColorParameter("alinkcolor");
	    vlinkcolor = getColorParameter("vlinkcolor");
	} catch(NumberFormatException e) {
	}
	int n = Integer.parseInt(getParameter("n0"));
	numlnks = new int[n+1];
	linx_s = new String[n+1][][];
	linx_visited = new boolean[n+1][];
	for(int i=0; i<=n; ++i) {
	    String si = String.valueOf(i);
	    int m = Integer.parseInt(getParameter("n".concat(si)));
	    linx_s[i] = new String[m][2];
	    String si_ = si.concat("_");
	    for(int j=0; j<m; ++j) {
		String sj = String.valueOf(j+1);
		if(i == 0) {
		    linx_s[i][j][0] = "";
		    linx_s[i][j][1] = getParameter("title".concat(sj));
		} else {
		    String s = si_.concat(sj);
		    linx_s[i][j][0] = getParameter("l".concat(s));
		    linx_s[i][j][1] = getParameter("t".concat(s));
		}
	    }
	    linx_visited[i] = new boolean[m];
	}
	linkfont = getFontParameter("linkfont");
	buftwo = createImage(size().width, size().height);
    }
    public void start() {
	crsr_onapplet = crsr_onlinx = false;
	crsr_x = crsr_y = linx_y0 = 0;
	activetitle = 0;
	activelink = -1;
    }

    public boolean handleEvent(Event e) {
	// deprecated event handling
	switch(e.id) {
	case Event.MOUSE_ENTER:
	    crsr_onapplet = true;
	    linx_y0 = 0;
	    activetitle = 0;
	    activelink = -1;
	    repaint();
	    return true;
	case Event.MOUSE_EXIT:
	    crsr_onapplet = crsr_onlinx = false;
	    linx_y0 = 0;
	    activetitle = 0;
	    activelink = -1;
	    repaint();
	    return true;
	case Event.MOUSE_MOVE: case Event.MOUSE_DRAG:
	    int minx = (size().width - linx_w) / 2;
	    int maxx = (size().width + linx_w) / 2;
	    int crsr_x_prev = crsr_x;
	    int crsr_y_prev = crsr_y;
	    crsr_x = e.x;
	    crsr_y = e.y;
	    int dy = crsr_y - crsr_y_prev;
	    if(crsr_onlinx = crsr_x >= minx && crsr_x <= maxx)
		linx_y0 = (linx_y0 + dy + linx_h) % linx_h;
	    else
		linx_y0 = (linx_y0 - dy + linx_h) % linx_h;
	    repaint();
	    return true;
	case Event.MOUSE_DOWN:
	    if(activelink != -1) {
		linx_visited[activetitle][activelink] = true;
		if(activetitle == 0) {
		    activetitle = activelink+1;
		    activelink = -1;
		    repaint();
		} else {
		    try {
			URL u = new URL(linx_s[activetitle][activelink][0]);
			boolean m = (e.modifiers & Event.ALT_MASK) != 0;
			getAppletContext().showDocument(u, m? "_blank":"_self");
		    } catch(MalformedURLException exc) {
		    }
		}
	    }
	    return true;
	default: return super.handleEvent(e);
	}
    }
    public void paint(Graphics g) {
	update(g);
    }
    public void update(Graphics this_g) {
	Graphics g = buftwo.getGraphics();
	g.setColor(getBackground());
	g.fillRect(0, 0, size().width, size().height);
	if(linx_w == 0) {
	    FontMetrics m = g.getFontMetrics(linkfont);
	    linx_w = widthLinks(g) + 2*m.charWidth('H');
	    linx_h = size().height - 2*m.getHeight();
	}
	paintLinx(g);
	this_g.drawImage(buftwo, 0, 0, null);
    }

    public void paintLinx(Graphics g) {
	g.setFont(linkfont);
	FontMetrics m = g.getFontMetrics();
	int centerx = size().width/2;
	int leftx = centerx - linx_w/2;
	int rightx = centerx + linx_w/2;
	activelink = -1;
	for(int i=0; i<linx_s[activetitle].length; ++i) {
	    String s = linx_s[activetitle][i][1];
	    int w = m.stringWidth(s);
	    int x;
	    if(crsr_onlinx)
		x = leftx + (linx_w - w)*(crsr_x - leftx)/linx_w;
	    else
		x = (crsr_x <= centerx)? leftx : rightx - w;
	    int y = m.getHeight() + (linx_y0 + i*m.getHeight()) % linx_h;
	    if(crsr_x >= x && crsr_x < x + w &&
	       crsr_y >= y - m.getAscent() && crsr_y < y + m.getDescent()) {
		String u = linx_s[activetitle][i][0];
		g.setColor(alinkcolor);
		showStatus(u);
		activelink = i;
	    } else
		g.setColor(linx_visited[activetitle][i]? vlinkcolor:linkcolor);
	    g.drawString(s, x, y);
	    g.drawLine(x, y+1, x+w-1, y+1);
	}
    }

    private int widthLinks(Graphics g) {
	int max = 0;
	FontMetrics m = g.getFontMetrics(linkfont);
	for(int i=0; i<linx_s[activetitle].length; ++i) {
	    int w = m.stringWidth(linx_s[activetitle][i][1]);
	    if(w > max)
		max = w;
	}
	return max;
    }
}
