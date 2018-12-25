import java.awt.*;
import java.awt.image.*;

/**
 * This applet animates a scroll text in a 3D whirlwind-like path.<p>
 * Changes:
 * <ul>
 * <li>1.01 (09/29/1999): g.dispose() removed, backimg.flush() in destroy()</li>
 * <li>1.0 (09/01/1998)</li>
 * </ul>
 *
 * @version	1.02, 01/13/2002
 * @since	09/29/1999 (or earlier)
 * @author	Peter Csizmadia
 */
public class ForgoszelTekercselo extends java.applet.Applet implements Runnable
{
    //////////////////////////////
    // Applet variables
    //////////////////////////////

    private long time;
    private int delay;
    private Thread runner;
    // screen
    private int screenwidth, screenheight;
    // double buffering
    private Image backimg;
    private Graphics backg;

    private int x_mousedown, y_mousedown;
    private int z_offset;

    private Font asefont;
    private FontMetrics asefm;

    //////////////////////////////
    // WhirlwindScroll variables
    //////////////////////////////

    // string-to-bitmap rendering
    private int scrollwidth, scrollheight, nlayers;
    private int fontsize;
    private int scrollbm[];
    private String[] text;
    private Font tfont, sfont;
    private FontMetrics tfm, sfm;
    private int spacewidth;

    // distortion map generation
    private int mapwidth, mapheight;
    private int nframes;
    private int nwaves;
    private int[][] accmap0, accdistortmap0;
    private int[][] accmap, accdistortmap;

    // progress indicator
    private double progress0, dprogress;
    private int progress = 0;

    // animation
    private int outwidth, outheight;
    private MemoryImageSource outsrc;
    private int[] outbm, blankoutbm;
    private int scrollspeed = 1;
    private int ticker = 0;
    private int frame = 0, dframe = 1;
 
    public void init() {
	// parse applet parameters
	Color bg = new Color(Integer.parseInt(getParameter("bgcolor").substring(1), 16));
	setBackground(bg);
	delay = Integer.parseInt(getParameter("delay"));
	// width and height must be dividable by 4
	screenwidth = size().width;
	screenheight = size().height;
	// create image for double buffering
	backimg = createImage(screenwidth, screenheight);
	backg = backimg.getGraphics();
	backg.setColor(bg);
	backg.fillRect(0, 0, screenwidth, screenheight);
	// create scroll, render distortion map
	int nf = Integer.parseInt(getParameter("frazisszam"));
	text = new String[2*nf];
	for(int i=0; i<nf; ++i) {
	    text[2*i] = getTextParameter("t"+i);
	    text[2*i+1] = getTextParameter("s"+i);
	}

	asefm = getFontMetrics(asefont = new Font("SansSerif", Font.BOLD, 24));

	///////////////////////
	// initialize scroller
	///////////////////////
	// string-to-bitmap initializations
	nframes = Integer.parseInt(getParameter("fazisszam"));
	nlayers = Integer.parseInt(getParameter("layers"));
	nwaves = Integer.parseInt(getParameter("hullamszam"));
	fontsize = Integer.parseInt(getParameter("fontsize"));
	tfont = new Font("SansSerif", Font.BOLD, fontsize);
	sfont = new Font("SansSerif", Font.PLAIN, fontsize);
	tfm = getFontMetrics(tfont);
	sfm = getFontMetrics(sfont);
	spacewidth = tfm.charWidth('H');
	scrollwidth = 0;
	scrollheight = tfm.getAscent() + tfm.getDescent();
	for(int i=0; i<text.length; i+=2) {
	    String t = text[i];
	    String s = text[i+1];
	    scrollwidth += tfm.stringWidth(t) + sfm.stringWidth(s) + 4*spacewidth;
	}

	// distortion map initializations
	mapwidth = (int)Math.ceil(0.89*screenheight) & ~1;
	mapheight = screenheight & ~1;
	outwidth = mapwidth;
	outheight = mapheight;

	scrollspeed = Integer.parseInt(getParameter("scrollspeed"));
    }
    public void start() {
	time = System.currentTimeMillis();
	if(runner == null) {
	    (runner = new Thread(this)).start();
	}
    }
    public void stop() {
	if(runner != null) {
	    runner.stop();
	    runner = null;
	}
    }
    public void destroy() {
	if(backimg != null)
	    backimg.flush();
    }
    public void paint(Graphics g) {
	update(g);
    }
    public synchronized void update(Graphics g) {
	g.drawImage(backimg, 0, 0, null);
	notifyAll();
    }
    public void run() {
	Thread thisthread = Thread.currentThread();
	if(thisthread == null)
	    return;
	synchronized(this) {
	    if(progress == 0) {
		prepare();
		ticker = (int)(scrollwidth*Math.random());
		frame = (int)(nframes*Math.random());
		if(frame <= 0)
		    dframe = 1;
		else if(frame >= nframes - 1)
		    dframe = -1;
		setOutHeight(screenheight);
	    }
	    if(progress != 100)
		return;
	    time = System.currentTimeMillis();
	}
	while(runner == thisthread) {
	    synchronized(this) {
		Image img = getNextImage();
		backg.setColor(getBackground());
		backg.fillRect(0, 0, screenwidth, screenheight);
		backg.drawImage(img, 0, 0, null);
		img.flush(); // memory overflow in Netscape 4.5 without this!
	    }

	    // request update() and wait for it to complete
	    synchronized(this) {
		repaint(); // must be inside synchronized!!! otherwise bug in
			   // in java 1.3
		try {
		    wait();
		} catch(InterruptedException exc) {
		}
	    }

	    time += delay;
	    try {
		thisthread.sleep(Math.max(1, time - System.currentTimeMillis()));
	    } catch(InterruptedException exc) {
	    }
	}
    }
    private String getTextParameter(String name) {
	String text = getParameter(name);
	StringBuffer sbuf = new StringBuffer();
	for(int i = 0; i < text.length(); ++i) {
	    char c = text.charAt(i);
	    if(c == '\\') {
		c = text.charAt(++i);
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
	    sbuf.append(c);
	}
	return sbuf.toString();
    }
    public boolean handleEvent(Event e) {
	switch(e.id) {
	    case Event.MOUSE_DOWN:
		x_mousedown = e.x;
		y_mousedown = e.y;
		return true;
	    case Event.MOUSE_DRAG:
		if(y_mousedown != e.y) {
		    int dy = e.y - y_mousedown;
		    if((z_offset += dy) > 0xff)
			z_offset = 0xff;
		}
		setOutHeight(screenheight*(0x100 - z_offset) >> 8);
		return true;
	    default:
		return super.handleEvent(e);
	}
    }

    private void countdown() {
	backg.setColor(getBackground());
	backg.fillRect(0, 0, size().width, size().height);
	backg.setColor(Color.white);
	backg.setFont(asefont);
	String s = String.valueOf(100-progress);
	backg.drawString(s, (screenwidth - asefm.stringWidth(s))/2,
			 screenheight/2);
	// request update() and wait for it to complete
	repaint();
	synchronized(this) {
	    try {
		wait();
	    } catch(InterruptedException exc) {
	    }
	}
    }

    /**
     * Sets the size of the sprite on the screen.
     * @param   h       height
     */
    public synchronized void setOutHeight(int h) {
	outwidth = (int)Math.ceil(0.89*h) & ~1;
	outheight = h & ~1;
	for(int frame=0; frame<nframes; ++frame)
	    scale(frame);
	initBitmaps();
    }

    private void initBitmaps() {
	outbm = new int[outwidth*outheight];
	blankoutbm = new int[outwidth];
	int blankpixel = getBackground().getRGB();
	for(int i=0; i<outwidth; ++i)
	    blankoutbm[i] = blankpixel;
	outsrc = new MemoryImageSource(outwidth, outheight,
		outbm, 0, outwidth);
    }

    /**
     * Get the next animation frame.
     * @return  Image of the next frame.
     */
    private Image getNextImage() {
	ticker = (ticker + scrollspeed) % scrollwidth;
	if((frame += dframe) >= nframes) {
	    frame = nframes - 1;
	    dframe = -1;
	} else if(frame < 0) {
	    frame = 0;
	    dframe = 1;
	}
	animate();
	return createImage(outsrc);
    }

    private void prepare() {
	// string-to-bitmap conversion
	try {
	    int[] bm = initScrollLayer0();
	    text = null;
	    tfont = sfont = null;
	    tfm = sfm = null;
	    for(int layer=1; layer<nlayers; ++layer) {
		progress = 10*layer/nlayers;
		countdown();
		initScrollLayer(bm, layer);
	    }
	    progress = 10;
	    scrollbm = bm;
	} catch(InterruptedException exc) {
	    text = null;
	    tfont = sfont = null;
	    tfm = sfm = null;
	    progress = -1;
	    return;
	}
	text = null;
	tfont = sfont = null;
	tfm = sfm = null;

	// make distortion map
	int[] X_tmp = new int[scrollwidth];
	int[] topY_tmp = new int[scrollwidth];
	int[] botY_tmp = new int[scrollwidth];
	int[] Z_tmp = new int[scrollwidth];
	int mapsize = mapwidth*mapheight;
	int[] distortmap_tmp = new int[mapsize];
	int[] zbuf_tmp = new int[mapsize];
	dprogress = 90.0/nframes;
	for(int frame=0; frame<nframes; ++frame) {
	    progress0 = 10 + frame*dprogress;
	    progress = (int)progress0;
	    countdown();
	    generatePath(Math.PI*frame/nframes,
			 X_tmp, topY_tmp, botY_tmp, Z_tmp);
	    renderDistortionMap(X_tmp, topY_tmp, botY_tmp, Z_tmp,
				distortmap_tmp, zbuf_tmp);
	    accelerateMap(frame, distortmap_tmp);
	}
	X_tmp = topY_tmp = botY_tmp = null;
	distortmap_tmp = null;
	zbuf_tmp = null;
	progress = 100;
	initBitmaps();
    }

// string-to-bitmap

    private int[] initScrollLayer0() throws InterruptedException {
	Image img = createImage(scrollwidth, scrollheight);
	Graphics g = img.getGraphics();
	g.setColor(Color.black);
	g.fillRect(0, 0, scrollwidth, scrollheight);
	int x = 0;
	int y = tfm.getAscent();
	for(int i=0; i<text.length; i+=2) {
	    String t = text[i];
	    g.setColor(Color.red);
	    g.setFont(tfont);
	    g.drawString(t, x, y);
	    x += tfm.stringWidth(t) + spacewidth;
	    String s = text[i+1];
	    g.setColor(Color.white);
	    g.setFont(sfont);
	    g.drawString(s, x, y);
	    x += sfm.stringWidth(s) + 3*spacewidth;
	}
	int[] bm = new int[scrollwidth*(scrollheight*nlayers+1)];
	new PixelGrabber(img, 0, 0, scrollwidth, scrollheight,
			 bm, 0, scrollwidth).grabPixels();
	img.flush();
	for(int i=scrollwidth*scrollheight-1; i>=0; --i) {
	    if((bm[i] & 0x00ffffff) == 0)
		bm[i] = 0;
	}
	int offset = scrollwidth*scrollheight*nlayers;
	for(int i=offset+scrollwidth-1; i>=offset; --i)
	    bm[i] = 0;
	return bm;
    }

    private void initScrollLayer(int bm[], int layer) {
	int size = scrollwidth*scrollheight;
	int offset = layer*size;
	int lognlayers = 0;
	while((1 << lognlayers) != nlayers)
	    ++lognlayers;
	int lr = lognlayers;
	int lg = lognlayers;
	int lb = lognlayers+1;
	for(int i=0; i<size; ++i) {
	    int c = bm[i];
	    if(c != 0) {
		int r = (c & 0x00ff0000) >> 16;
		int g = (c & 0x0000ff00) >> 8;
		int b = (c & 0x000000ff);
		r -= layer*r >> lr;
		g -= layer*g >> lg;
		b -= layer*b >> lb;
		bm[offset+i] = 0xff000000 | (r<<16) | (g<<8) | b;
	    }
	}
    }

// distortion map rendering

    private void generatePath(double phi,
			      int[] X, int[] topY, int[] botY, int[] Z)
    {
	// correct calculation of the length of path (length of scroll text):
	//     int(sqrt(1/4+(1-1/4)*sin(f/2)^2+sin(f)^2), f=0..2*Pi);
	// _approximation_:
	//     int(sqrt(1/4+n^2*sin(f/2)^2), f=0..2*Pi);
	float tmaxes[] = {3.141593f, 5.270367f, 8.814276f,
			  12.61162f, 16.49506f, 20.41852f,
			  24.36404f, 28.32309f, 32.29108f,
			  36.26529f, 40.24404f, 44.22620f,
			  48.21098f, 52.19783f, 56.18635f,
			  60.17623f, 64.16724f, 68.15918f};
	double tmax = (double)tmaxes[nwaves];
	// distance between neighboring vertices
	double dt = tmax/scrollwidth;
	// height of scroll text (in the same units as t)
	double h = tmax*scrollheight/scrollwidth;
	// center of screen (in pixel units)
	int X0 = mapwidth/2;
	int Y0 = (int)(0.55*mapheight+0.5);
	double C = 1.1*(mapheight<<8);
	double nn = (double)nwaves*nwaves;
	double f = 2*Math.PI;
	double zshift = Math.cos(phi)*Math.PI/8;
	for(int i=0; i<scrollwidth; ++i) {
	    double rxy = Math.sin(f/2);
	    double rxyrxy = rxy*rxy;
	    double x = rxy*Math.cos(nwaves*f);
	    double z = rxy*Math.sin(nwaves*f + zshift);
	    double y = Math.cos(f);
	    double zz = 768 - 255.999*z;
	    double Cz = C/zz;
	    X[i] = (int)(X0 + Cz*x);
	    topY[i] = (int)(Y0 + Cz*y);
	    botY[i] = (int)(Y0 + Cz*(y+h));
	    Z[i] = (int)zz;
	    f -= dt/Math.sqrt(0.25 + nn*rxyrxy);
	}
    }

    private void renderDistortionMap(int[] X, int[] topY, int[] botY, int[] Z,
				     int[] map, int[] zbuf)
    {
	int hash = scrollwidth/50;
	if(hash < 2)
	    hash = 2;
	for(int i=mapwidth*mapheight-1; i>=0; --i) {
	    zbuf[i] = 200000;
	    map[i] = -1;
	}
	for(int scrollx=1; scrollx<scrollwidth; ++scrollx) {
	    int x1 = X[scrollx-1];
	    int x2 = X[scrollx];
	    double topy1 = topY[scrollx-1];
	    double topy2 = topY[scrollx];
	    double boty1 = botY[scrollx-1];
	    double boty2 = botY[scrollx];
	    double z1 = Z[scrollx-1];
	    double z2 = Z[scrollx];
	    if(x1>x2) { // x of the first point must be smaller
		x2 = x1;
		topy2 = topy1;
		boty2 = boty1;
		z2 = z1;
		x1 = X[scrollx];
		topy1 = topY[scrollx];
		boty1 = botY[scrollx];
		z1 = Z[scrollx];
	    }
	    if(x1 < 0) {
		if(x2 < 0)
		    continue;
		x1 = 0;
	    }
	    if(x2 > mapwidth - 1) {
		if(x1 > mapwidth - 1)
		    continue;
		x2 = mapwidth - 1;
	    }
	    // simple texture mapping to fill the gaps
	    int deltax = x2-x1+1;
	    texm(scrollx, x1, x2,
		 topy1, boty1, (topy2-topy1)/deltax, (boty2-boty1)/deltax,
		 z1, z2, (z2-z1)/deltax, map, zbuf);
	    progress = (int)(progress0+dprogress*(scrollx+1)/(scrollwidth+1));
	    if(scrollx % hash == 0)
		countdown();
	}
    }

    private void texm(int scrollx, int x, int x2,
		      double topy, double boty, double dtopy, double dboty,
		      double z, double z2, double dz, int[] map, int[] zbuf)
    {
	int scrollsize = scrollwidth*scrollheight;
	int scrollheight256 = scrollheight<<8;
	while(x<=x2) {
	    int iz = (int)z;
	    int fscrolly = 0;
	    int dscrolly = (int)(scrollheight256/(boty-topy+1));
	    int top = (int)topy;
	    int bot = (int)boty;
	    int scrolloffset = scrollx + scrollsize*((nlayers*(iz-512))>>9);
	    if(top < 0) {
		fscrolly -= top*dscrolly;
		top = 0;
	    }
	    if(bot >= mapheight)
		bot = mapheight - 1;
	    int imax = x + bot*mapwidth;
	    for(int i=x+top*mapwidth; i<=imax; i+=mapwidth) {
		if(iz < zbuf[i]) {
		    map[i] = scrolloffset + scrollwidth*(fscrolly>>8);
		    zbuf[i] = iz;
		}
		fscrolly += dscrolly;
	    }
	    topy += dtopy;
	    boty += dboty;
	    z += dz;
	    ++x;
	}
    }

    private void scale(int frame) {
	long mapsize = mapwidth*mapheight;
	long outsize = outwidth*outheight;
	int[] am = accmap0[frame];
	int[] dm = accdistortmap0[frame];
	int len = (int)(am.length*outsize/mapsize) & ~3;
	int[] nam = accmap[frame] = new int[len];
	int[] ndm = accdistortmap[frame] = new int[len];
	for(int i=len-1; i>=0; --i) {
	    int j = (int)(i*mapsize/outsize);
	    int k = am[j];
	    nam[i] = ((k/mapwidth)*outheight/mapheight)*outwidth
		   + (k%mapwidth)*outwidth/mapwidth;
	    ndm[i] = dm[j];
	}
    }

// animation

    private void accelerateMap(int frame, int[] distortmap) {
	int[] dmap = distortmap;
	int j = 0;
	int[] dm, am;
	int size = 0;
	for(int i=mapwidth*mapheight-1; i>=0; --i)
	    if(dmap[i] >= 0)
		++size;
	if((size & 3) != 0)
	    size += 4 - (size & 3);
	dm = new int[size];
	am = new int[size];
	for(int i=mapwidth*mapheight-1; i>=0; --i) {
	    int d = dmap[i];
	    if(d >= 0) {
		am[j] = i;
		dm[j] = d;
		++j;
	    }
	}
	while(j < size) {
	    am[j] = outwidth*outheight - 1;
	    dm[j] = scrollwidth*scrollheight*nlayers;
	    ++j;
	}
	if(accmap0 == null) {
	    accmap0 = new int[nframes][];
	    accdistortmap0 = new int[nframes][];
	    accmap = new int[nframes][];
	    accdistortmap = new int[nframes][];
	}
	accmap[frame] = accmap0[frame] = am;
	accdistortmap[frame] = accdistortmap0[frame] = dm;
    }

// animation

    private void animate() {
	// put 4 most frequently used variables in register
	int t = ticker;
	int[] dm = accdistortmap[frame];
	int i = dm.length;
	int[] scrlbm = scrollbm;
	// put less frequently used variables in stack
	int[] am = accmap[frame];
	int[] scrnbm = outbm;
	for(int j=0; j<outheight; ++j)
	    System.arraycopy(blankoutbm, 0,
			     scrnbm, j*outwidth, outwidth);
	while(i > 0) {
	    int c = scrlbm[dm[--i] + t];
	    if(c != 0)
		scrnbm[am[i]] = c;
	    c = scrlbm[dm[--i] + t];
	    if(c != 0)
		scrnbm[am[i]] = c;
	    c = scrlbm[dm[--i] + t];
	    if(c != 0)
		scrnbm[am[i]] = c;
	    c = scrlbm[dm[--i] + t];
	    if(c != 0)
		scrnbm[am[i]] = c;
	}
    }
}
