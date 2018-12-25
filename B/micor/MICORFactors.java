import java.awt.*;
import java.util.Random;


/**
 * MICOR Coalescence Factor calculation
 *
 * @version     0.1 11/09/1998
 * @author      Peter Csizmadia
 */
public class MICORFactors extends java.applet.Applet implements Runnable
{
    double Tq = 0.15;
    double vt = 0.67;
    double etamax = 2.2;
    int Nmcs = 0;
    String[] procname = {"qq", "qs", "ss",
			 "qqq", "qqs", "qsq", "qss", "ssq", "sss"};

    /** Mass of first incoming particle */
    double[] mass1 = {0.30, 0.30, 0.45, 0.60, 0.60, 0.75, 0.75, 0.90, 0.90};

    /** Mass of second incoming particle */
    double[] mass2 = {0.30, 0.45, 0.45, 0.30, 0.45, 0.30, 0.45, 0.30, 0.45};

    /** Reduced mass of incoming particles */
    double[] rmass = new double[9];

    /** A constant needed at cross section calculation */
    double[] a_sigma = new double[9];

    /**
     * The nominator of the coalescence factors.
     */
    double[] Cfnom = new double[9];

    /**
     * The denominator of the coalescence factors.
     */
    double[] Cfdenom = new double[9];

    Label labNmcs;
    Label[] labCf = new Label[9];

    TextField Tqtxt;
    TextField vttxt;
    TextField etamaxtxt;
    Button startbtn;
    Button initbtn;
    Font numfnt = new Font("Courier", Font.PLAIN, 12);

    Thread thread;

    Random rnd;

    public void init() {
	GridBagLayout gbl = new GridBagLayout();
	GridBagConstraints gbc = new GridBagConstraints();
	setLayout(gbl);
	gbc.gridx = 0;
	gbc.gridy = 0;
	gbc.anchor = GridBagConstraints.NORTHWEST;
	gbc.fill = GridBagConstraints.HORIZONTAL;
	add(startbtn = new Button("Start"));
	gbl.setConstraints(startbtn, gbc);
	gbc.gridx = 1;
	add(initbtn = new Button("Init"));
	gbl.setConstraints(initbtn, gbc);
	Label l = new Label("Quark temperature");
	gbc.gridx = 2;
	add(l);
	gbl.setConstraints(l, gbc);
	gbc.gridx = 3;
	add(Tqtxt = new TextField(String.valueOf(Tq)));
	gbl.setConstraints(Tqtxt, gbc);
	Tqtxt.setFont(numfnt);
	gbc.gridx = 2;
	gbc.gridy = 1;
	add(l = new Label("Maximum rapidity"));
	gbl.setConstraints(l, gbc);
	gbc.gridx = 3;
	add(etamaxtxt = new TextField(String.valueOf(etamax)));
	gbl.setConstraints(etamaxtxt, gbc);
	etamaxtxt.setFont(numfnt);
	gbc.gridx = 2;
	gbc.gridy = 2;
	add(l = new Label("Transverse velocity"));
	gbl.setConstraints(l, gbc);
	gbc.gridx = 3;
	add(vttxt = new TextField(String.valueOf(vt)));
	gbl.setConstraints(vttxt, gbc);
	vttxt.setFont(numfnt);
	add(labNmcs = new Label());
	labNmcs.setFont(numfnt);
	gbc.gridx = 2;
	gbc.gridy = 3;
	gbl.setConstraints(labNmcs, gbc);
	Panel respan = new Panel();
	add(respan);
	gbc.gridx = 4;
	gbc.gridy = 0;
	gbc.gridheight = 4;
	gbc.weightx = 1;
	gbc.weighty = 1;
	gbl.setConstraints(respan, gbc);

	GridBagLayout pbl = new GridBagLayout();
	respan.setLayout(pbl);
	gbc.gridheight = 1;
	gbc.weighty = 0;
	for(int i=0; i<labCf.length; ++i) {
	    gbc.gridx = 0;
	    gbc.fill = GridBagConstraints.NONE;
	    gbc.weightx = 0;
	    respan.add(l = new Label(procname[i]));
	    pbl.setConstraints(l, gbc);
	    gbc.gridx = 1;
	    gbc.fill = GridBagConstraints.HORIZONTAL;
	    gbc.weightx = 1;
	    respan.add(labCf[i] = new Label());
	    labCf[i].setFont(numfnt);
	    pbl.setConstraints(labCf[i], gbc);
	    gbc.gridy++;
	}

	rnd = new Random();

	initcalc();
    }

   /**
    * Returns a string containing the version and the author.
    */
    public String getAppletInfo() {
	return "MICOR Coalescence Factor Calculation 0.1\n"+
	       "1998, Peter Csizmadia <cspeter@rmki.kfki.hu>\n";
    }

    public void stop() {
	thread = null;
    }

    public void run() {
	int dNmcs = 16;
	int k = 0;
	while(thread != null) {
	    long t0 = System.currentTimeMillis();
	    for(int i=0; i<dNmcs; ++i) {
		for(int j=0; j<Cfnom.length; ++j) {
		    mcstep(j);
		}
	    }
	    Nmcs += dNmcs;
	    if((k & 31) == 0) {
		for(int i=0; i<Cfnom.length; ++i) {
		    labCf[i].setText(String.valueOf(Cfnom[i]/Cfdenom[i]));
		}
		labNmcs.setText(String.valueOf(Nmcs).concat(" MCS"));
	    }
	    long dt = System.currentTimeMillis() - t0;
	    if(dt < 50)
		dNmcs <<= 1;
	    else if(dt >= 100) {
		if((dNmcs >>= 1) == 1)
		    dNmcs = 2;
	    }
	    try {
		Thread.sleep(10);
	    } catch(InterruptedException e) {
	    }
	    ++k;
	}
    }

    /**
     * Set the editability of labels.
     */
    private void seteditable(boolean x) {
	Tqtxt.setEditable(x);
	vttxt.setEditable(x);
	etamaxtxt.setEditable(x);
    }

    /**
     * Handles the Start/Stop events.
     */
    public boolean action(Event e, Object arg) {
	if(e.target == startbtn) {
	    if(thread != null) {
		seteditable(true);
		thread = null;
		startbtn.setLabel("Start");
		initbtn.enable();
	    } else {
		seteditable(false);
		Tq = Double.valueOf(Tqtxt.getText()).doubleValue();
		vt = Double.valueOf(vttxt.getText()).doubleValue();
		etamax = Double.valueOf(etamaxtxt.getText()).doubleValue();
		(thread = new Thread(this)).start();
		startbtn.setLabel("Stop");
		initbtn.disable();
	    }
	} else if(e.target == initbtn) {
	    initcalc();
	}
	return false;
    }

    /**
     * Initialize the calculation.
     */
    private void initcalc() {
	String s;
	if((s = getParameter("Tq")) != null) {
	    Tq = Double.valueOf(s).doubleValue();
	    Tqtxt.setText(String.valueOf(Tq));
	}
	if((s = getParameter("vt")) != null) {
	    vt = Double.valueOf(s).doubleValue();
	    vttxt.setText(String.valueOf(vt));
	}
	if((s = getParameter("etamax")) != null) {
	    etamax = Double.valueOf(s).doubleValue();
	    etamaxtxt.setText(String.valueOf(etamax));
	}
	for(int i=0; i<rmass.length; ++i) {
	    double M = mass1[i] + mass2[i];
	    double m = rmass[i] = mass1[i]*mass2[i]/M;
	    a_sigma[i] = 16*0.3*0.3*0.3*Math.sqrt(Math.PI)*M*M/m;
	}
	Nmcs = 0;
	for(int i=0; i<Cfnom.length; ++i) {
	    Cfnom[i] = 0;
	    Cfdenom[i] = 0;
	    labCf[i].setText("");
	}
	labNmcs.setText("");
    }

    /**
     * Generates a pseudorandom number in the specified finite interval.
     */
    private double mcgen_min_max(double min, double max, double[] d) {
	double x = rnd.nextFloat();
	d[0] *= max - min;
	return min + (max - min)*x;
    }

    /**
     * Generates a pseudorandom number between the specified minimum
     * and infinity.
     */
    private double mcgen_min_a(double min, double a, double[] d) {
	double x = rnd.nextFloat();
	double g = a/(1-x);
	d[0] *= g/(1-x);
	return min + g*x;
    }

    /**
     * Generates a pseudorandom real number.
     */
    private double mcgen_a(double a, double[] d) {
	double x = rnd.nextFloat();
	double y = 2*x-1;
	double absy = (y>=0)? y : -y;
	double g = a/(1-absy);
	d[0] *= 2*g/(1-absy);
	return g*y;
    }

    /**
     * Coalescence cross section.
     */
    private double sigma(int i, double s, double lambda) {
//	double d = 0.186*Math.log(qsquare/0.04); // 7/(12pi)*...
//	if(d > 0)
//	    d = 0;
//	double alpha = 1/(2.17-d); // 1/(1/0.46-d)
	double alpha = 0.46;
	double alpham = alpha*rmass[i];
	double a = 4*alpham*alpham;
	double t = 1 + 0.25*lambda/(alpham*s);
	return a_sigma[i]*alpha*alpha/(a*t*t);
    }

    /**
     * A Monte Carlo step in the coalescence factor calculation.
     */
    private void mcstep(int i) {
	double chtheta = 1/Math.sqrt(1-vt*vt);
	double shtheta = vt*chtheta;
	double expetamax = Math.exp(etamax);
	double shetamax = 0.5*expetamax - 0.5/expetamax;
	double[] d = {1.0};

	// spacetime coordinates in the comoving frame
	double R = 1.0; // radius of quark matter, only a constant factor so it
			// can be any positive number
	double sigma1 = R*1e-6; // a small number (sigma1 << R)
	double sigma2 = R*1e-6; // a small number (sigma2 << R)
	double rho1 = mcgen_min_max(0, R, d);
	double rho2 = mcgen_min_max(0, R, d);

	// spacetime coordinates
	double tau1 = sigma1*chtheta + rho1*shtheta;
	double tau2 = sigma2*chtheta + rho2*shtheta;
	double r1 = sigma1*shtheta + rho1*chtheta;
	double r2 = sigma2*shtheta + rho2*chtheta;
	double phi1 = mcgen_min_max(0, 2*Math.PI, d);
	double phi2 = mcgen_min_max(0, 2*Math.PI, d);
	double sheta1 = mcgen_min_max(-shetamax, shetamax, d);
	double sheta2 = mcgen_min_max(-shetamax, shetamax, d);
	double cheta1 = Math.sqrt(1 + sheta1*sheta1);
	double cheta2 = Math.sqrt(1 + sheta2*sheta2);

	// momentum coordinates
	double m1 = mass1[i];
	double m2 = mass2[i];
	double shy1 = mcgen_a(shetamax, d);
	double shy2 = mcgen_a(shetamax, d);
	double chy1 = Math.sqrt(1 + shy1*shy1);
	double chy2 = Math.sqrt(1 + shy2*shy2);
	double chrpt = Tq/shtheta; // characteristic p_T value
	double pt1 = mcgen_min_a(0, chrpt, d);
	double pt2 = mcgen_min_a(0, chrpt, d);
	double mt1 = Math.sqrt(m1*m1 + pt1*pt1);
	double mt2 = Math.sqrt(m2*m2 + pt2*pt2);
	double varphi1 = mcgen_min_max(0, 2*Math.PI, d);
	double varphi2 = mcgen_min_max(0, 2*Math.PI, d);
	double p1t = mt1*chy1;
	double p1x = pt1*Math.cos(varphi1);
	double p1y = pt1*Math.sin(varphi1);
	double p1z = mt1*shy1;
	double p2t = mt2*chy2;
	double p2x = pt2*Math.cos(varphi2);
	double p2y = pt2*Math.sin(varphi2);
	double p2z = mt2*shy2;
	double p3t = p1t + p2t;
	double p3x = p1x + p2x;
	double p3y = p1y + p2y;
	double p3z = p1z + p2z;
	double s = p3t*p3t + p3x*p3x + p3y*p3y + p3z*p3z;
	double lambda = (s - (m1-m2)*(m1-m2))*(s - (m1+m2)*(m1+m2));

	// Jacobians
	d[0] *= pt1*mt1*chy1*pt2*mt2*chy2;
	d[0] *= tau1*r1*tau2*r2/(chtheta*chtheta*cheta1*cheta2);

	// exp
	double u1p1 = mt1*(chy1*cheta1 - shy1*sheta1)*chtheta
		    - pt1*Math.cos(phi1 - varphi1)*shtheta;
	double u2p2 = mt2*(chy2*cheta2 - shy2*sheta2)*chtheta
		    - pt2*Math.cos(phi2 - varphi2)*shtheta;
	d[0] *= Math.exp(-(u1p1 + u2p2)/Tq);

	Cfdenom[i] += d[0];

	// cross section

	Cfnom[i] += Math.sqrt(lambda)*sigma(i, s, lambda)*d[0];
    }
}
