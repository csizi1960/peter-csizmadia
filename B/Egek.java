import java.awt.*;
import java.util.Vector;
import java.util.StringTokenizer;
import java.util.Date;

public class Egek extends java.applet.Applet implements Runnable
{
    static final int MINZ = 170;
    static final int MAXZ = 4096;
    int delay;
    int starsx[], starsy[], starsz[];
    char starsc[];
    Color grayscale[];
    Image backbuf;
    Thread thread;
    Font[] boldFontCache = null;
    Font[] italicFontCache = null;
    Font[] plainFontCache = null;
    Font[] smallFontCache = null;
    String[] texts;
    String[] textWhen1;
    String[] textWhen2;
    String[] textUnit;
    double[] textX;
    double[] textY;
    double[] textZ;
    String whatStr;
    String when1Str;
    String when2Str;
    String unitStr;
    String myage;

    public void init() {
	String text = getTextParameter("text");
	int nstars = Integer.valueOf(getParameter("nstars")).intValue();
	delay = Integer.valueOf(getParameter("delay")).intValue();
	grayscale = new Color[256];
	for(int i = 0; i < 256; ++i) {
	    grayscale[i] = new Color(i, i, i);
	}
	starsx = new int[nstars];
	starsy = new int[nstars];
	starsz = new int[nstars];
	starsc = new char[nstars];
	int inearest = 0;
	for(int i = 0; i < starsx.length; ++i) {
	    starsx[i] = (int)((Math.random()-0.5)*2048);
	    starsy[i] = (int)((Math.random()-0.5)*2048);
	    starsz[i] = (MAXZ - MINZ)*i/starsx.length + MINZ;
	    starsc[i] = text.charAt(i % text.length());
	}
	backbuf = createImage(size().width, size().height);
	String s;
	Vector v = new Vector();
	for(int i = 0; (s = getTextParameter("text" + i)) != null; ++i) {
	    v.addElement(s);
	}
	texts = new String[v.size()];
	textX = new double[texts.length];
	textY = new double[texts.length];
	textZ = new double[texts.length];
	textWhen1 = new String[texts.length];
	textWhen2 = new String[texts.length];
	textUnit = new String[texts.length];
	for(int i = 0; i < texts.length; ++i) {
	    double f = (double)(texts.length - i - 1)/(texts.length - 1);
	    f = 100*Math.pow(f, 0.5);
	    textX[i] = (Math.random() - 0.5)*f;
	    textY[i] = (Math.random() - 0.5)*f;
	    textZ[i] = MINZ + 0.15*(MAXZ - MINZ)*(i + 2);
	}
	v.copyInto(texts);
	for(int i = 0; i < texts.length; ++i) {
	    StringTokenizer st = new StringTokenizer(texts[i], "|");
	    String what = st.nextToken();
	    String when1 = st.nextToken();
	    String when2 = st.nextToken();
	    String unit = st.nextToken();
	    if(when1.equals("*")) {
		when1 = null;
	    }
	    if(when2.equals("*")) {
		when2 = null;
	    }
	    if(unit.equals("*")) {
		unit = null;
	    }
	    texts[i] = what;
	    textWhen1[i] = when1;
	    textWhen2[i] = when2;
	    textUnit[i] = unit;
	}
    }
    public void start() {
	if(thread == null) {
	    (thread = new Thread(this)).start();
	}
    }
    public void stop() {
	thread = null;
    }
    public void run() {
	long time = System.currentTimeMillis();
	long prevrealtime = time;
	myage = calcMyAge();
	while(thread != null) {
	    try {
		time += delay;
		Thread.sleep(Math.max(0, time - System.currentTimeMillis()));
	    } catch(InterruptedException exc) {
	    }
	    long realtime = System.currentTimeMillis();
	    int dz = (int)(50*(realtime-prevrealtime)/delay);
	    prevrealtime = realtime;
	    for(int i = 0; i < starsx.length; ++i) {
		starsz[i] -= dz;
		if(starsz[i] < MINZ) {
		    starsx[i] = (int)((Math.random()-0.5)*2048);
		    starsy[i] = (int)((Math.random()-0.5)*2048);
		    starsz[i] = 4095;
		}
	    }
	    for(int i = 0; i < texts.length; ++i) {
		textZ[i] -= 0.6*dz;
	    }
	    repaint();
	}
    }
    public void update(Graphics frontg) {
	int x0 = size().width/2;
	int y0 = size().height/2;
	Graphics g = backbuf.getGraphics();
	g.setColor(grayscale[0]);
	g.fillRect(0, 0, size().width, size().height);
	if(plainFontCache == null) {
	    plainFontCache = new Font[256];
	    smallFontCache = new Font[256];
	    boldFontCache = new Font[256];
	    italicFontCache = new Font[256];
	    int m = (int)(MAXZ/(0.5*MINZ) + 0.5);
	    for(int i = 1; i <= m && i < plainFontCache.length; ++i) {
		plainFontCache[i] = new Font("Times", Font.PLAIN, i);
		int sz = 3*i/4;
		smallFontCache[i] = new Font("Times", Font.PLAIN,
					     (sz > 0)? sz : 1);
		boldFontCache[i] = new Font("Times", Font.BOLD, i);
		italicFontCache[i] = new Font("Times", Font.ITALIC, i);
	    }
	}
	for(int i = 0; i < starsx.length; ++i) {
	    int x = x0 + (starsx[i]*512)/starsz[i];
	    int y = y0 + (starsy[i]*512)/starsz[i];
	    int d = MAXZ/starsz[i];
	    setColorForStar(g, x, y, starsz[i]);
	    if(d <= 1) {
		g.drawLine(x, y, x, y);
	    } else if(d <= 3) {
		g.fillRect(x-(d>>1), y-(d>>1), d, d);
	    } else {
		char c = starsc[i];
		if(boldFontCache[d] != null) {
		    g.setFont(boldFontCache[d]);
		    FontMetrics fm = g.getFontMetrics();
		    g.drawString(String.valueOf(c), x-fm.charWidth(c),
			    y-fm.getDescent());
		}
	    }
	}
	for(int i = texts.length - 1; i >= 0; --i) {
	    double z = textZ[i];
	    if(z >= 0.5*MINZ && z < MAXZ) {
		int d = (int)(MAXZ/z);
		if(plainFontCache[d] != null) {
		    int x = x0 + (int)((textX[i]*512)/z);
		    int y = y0 + (int)((textY[i]*512)/z);
		    String txt = texts[i];
		    if(txt.startsWith("\\red ")) {
			txt = txt.substring(5);
			g.setColor(Color.red);
		    } else {
			setColorForStar(g, x, y, (int)z);
		    }
		    int j = txt.indexOf("\\myage");
		    if(j >= 0) {
			txt = txt.substring(0, j) + myage + txt.substring(j + 6);
		    }
		    Font font = plainFontCache[d];
		    g.setFont(font);
		    FontMetrics fm = g.getFontMetrics();
		    for(int k = 0; k < txt.length(); ++k) {
			int l = txt.indexOf('$', k);
			if(l < 0) {
			    l = txt.length();
			}
			String s = txt.substring(k, l);
			int w = fm.stringWidth(s);
			g.drawString(s, x - w/2, y);
			y += fm.getHeight();
			k = l;
		    }

		    // draw second line
		    FontMetrics sfm = null;
		    int w = 0;
		    if(textWhen1[i] != null) {
			w += fm.stringWidth(textWhen1[i]);
		    }
		    Font sfont = smallFontCache[d];
		    if(textWhen2[i] != null && sfont != null) {
			sfm = g.getFontMetrics(sfont);
			w += sfm.stringWidth(textWhen2[i]);
		    }
		    if(textUnit[i] != null) {
			w += fm.stringWidth(textUnit[i]);
		    }
		    x -= w/2;
		    if(textWhen1[i] != null) {
			g.drawString(textWhen1[i], x, y);
			x += fm.stringWidth(textWhen1[i]);
		    }
		    if(textWhen2[i] != null && sfont != null) {
			g.setFont(sfont);
			g.drawString(textWhen2[i], x, y - sfm.getAscent()/2);
			x += sfm.stringWidth(textWhen2[i]);
			g.setFont(font);
		    }
		    if(textUnit[i] != null) {
			g.drawString(textUnit[i], x, y);
		    }
		}
	    }
	}
	frontg.drawImage(backbuf, 0, 0, null);
    }
    public void paint(Graphics g) {
	update(g);
    }
    private void setColorForStar(Graphics g, int x, int y, int z) {
	int dx = size().width/10;
	int dy = size().height/10;
	int bx = 256;
	int by = 256;
	if(x >= 0 && x < dx) {
	    bx = 256*x/dx;
	} else if(x > size().width-dx && x <= size().width) {
	    bx = 256*(size().width-x)/dx;
	}
	if(y >= 0 && y < dy) {
	    by = 256*y/dy;
	} else if(y > size().height-dy && y <= size().height) {
	    by = 256*(size().height-y)/dy;
	}
	int brightness = ((4607-z)*bx*by>>19);
	g.setColor(grayscale[(brightness > 255)? 255 : brightness]);
    }
    private String calcMyAge() {
	Date today = new Date();
	int szho = 10;
	int sznap = 9;
	int ev = today.getYear();
	Date szdate = new Date(ev, szho-1, sznap, 23, 59, 59);
	Date elozoszdate = new Date(ev, szho-1, sznap);
	double kor = ev - 72;
	if(today.getTime() < szdate.getTime()) {
	    elozoszdate = new Date(ev-1, szho-1, sznap);
	    kor -= 1;
	}
	kor += (today.getTime()-elozoszdate.getTime())/(1000.0*60*60*24*365);
	String s = String.valueOf(kor);
	int i = s.indexOf('.');
	if(i >= 0) {
	    i += 7;
	    if(i < s.length()) {
		s = s.substring(0, i);
	    }
	}
	return s;
    }
    private String getTextParameter(String name) {
	String text = getParameter(name);
	if(text == null) {
	    return null;
	}
	StringBuffer sbuf = new StringBuffer();
	for(int i = 0; i < text.length(); ++i) {
	    char c = text.charAt(i);
	    if(c == '\\') {
		c = text.charAt(++i);
		if(c >= 'a' && c <= 'z') {
		    // control sequence
		    c = text.charAt(--i);
		} else {
		    String acutes = null;
		    if(c == '\'') {
			c = text.charAt(++i);
			acutes = "aáAÁeéEÉiíIÍoóOÓuúUÚ";
		    } else if(c == 'H') {
			c = text.charAt(i += 2);
			acutes = "oõOÕuûUÛ";
		    } else if(c == ':') {
			c = text.charAt(++i);
			acutes = "oöOÖuüUÜ";
		    }
		    c = acutes.charAt(acutes.indexOf(c) + 1);
		}
	    }
	    sbuf.append(c);
	}
	return sbuf.toString();
    }
}
