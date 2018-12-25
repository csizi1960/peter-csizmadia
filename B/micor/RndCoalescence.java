import java.awt.*;

/**
 * Quark coalescence simulation.
 *
 * @version     0.3 11/05/1998
 * @author      Peter Csizmadia
 */
public class RndCoalescence extends java.applet.Applet implements Runnable {
    String[] quarknames = {"u", "d", "s"};
    String[] diquarknames = {"uu", "ud", "dd", "us", "ds", "ss"};
    String[] mesonnames = {"uu~", "ud~", "us~", "du~", "dd~", "ds~",
			   "su~", "sd~", "ss~"};
    String[] baryonnames = {"uuu", "uud", "udd", "ddd",
			    "uus", "uds", "dds", "uss", "dss", "sss"};

    /** Diquark indices, qq */
    int[][] outdiquark = {{0, 1, 3}, {1, 2, 4}, {3, 4, 5}};

    /** Meson indices, qq~ */
    int[][] outmeson = {{0, 1, 2}, {3, 4, 5}, {6, 7, 8}};

    /** Baryon indices, qqq */
    int[][] outbaryon = {{0, 1, 4}, {1, 2, 5}, {2, 3, 6}, {4, 5, 7}, {5, 6, 8},
			 {7, 8, 9}};

    /** Number of quarks, u, d, s */
    int[] Nquarks = new int[3];

    /** Number of antiquarks, u~, d~, s~ */
    int[] Nantiquarks = new int[3];

    /** Number of diquarks, uu, ud, dd, us, ds, ss */
    int[] Ndiquarks = new int[6];

    /** Number of antidiquarks */
    int[] Nantidiquarks = new int[6];

    /** Number of mesons, uu~, ud~, us~, du~, dd~, ds~, su~, sd~, ss~ */
    int[] Nmesons = new int[9];

    /** Number of baryons, uuu, uud, udd, ddd, uus, uds, dds, uss, dss, sss */
    int[] Nbaryons = new int[10];

    /** Number of antibaryons. */
    int[] Nantibaryons = new int[10];

    /** Process weights. */
    double[] procweights = new double[24+9+36];

    double[][] CD = new double[3][3];

    double[][] CM = new double[3][3];

    double[][] CB = new double[6][3];

    double[] Npart = new double[22];

    Button startbtn;
    TextField[] Lquarks = new TextField[3];
    TextField[] Lantiquarks = new TextField[3];
    TextField[] Ldiquarks = new TextField[6];
    TextField[] Lantidiquarks = new TextField[6];
    TextField[] Lmesons = new TextField[9];
    TextField[] Lbaryons = new TextField[10];
    TextField[] Lantibaryons = new TextField[10];
    TimeevolCanvas tcan;

    Thread thread = null;

    boolean stopped = false;

    public void init() {
	// incoming quark numbers
	for(int i=0; i<quarknames.length; ++i) {
	    String s = "N".concat(quarknames[i]);
	    String t = getParameter(s);
	    Nquarks[i] = (t == null)? 0 : Integer.parseInt(t);
	    t = getParameter(s.concat("~"));
	    Nantiquarks[i] = (t == null)? 0 : Integer.parseInt(t);
	}

	// diquark and meson coalescence factors
	double Cqq = Double.valueOf(getParameter("Cqq")).doubleValue();
	double Cqs = Double.valueOf(getParameter("Cqs")).doubleValue();
	double Css = Double.valueOf(getParameter("Css")).doubleValue();
	CD[0][0] = CD[0][1] = CD[1][0] = CD[1][1] = Cqq;
	CD[0][2] = CD[2][0] = CD[1][2] = CD[2][1] = Cqs;
	CD[2][2] = Css;
	CM[0][0] = CM[0][1] = CM[1][0] = CM[1][1] = Cqq*0.77*0.77/(0.6*0.6);
	CM[0][2] = CM[2][0] = CM[1][2] = CM[2][1] = Cqs*0.89*0.89/(0.75*0.75);
	CM[2][2] = Css*1.02*1.02/(0.9*0.9);
	for(int i=0; i<3; ++i) {
	    for(int j=0; j<3; ++j) {
		CD[i][j] *= 4.0/9.0;
		CM[i][j] *= 16.0/9.0;
	    }
	}

	// baryon coalescence factors
	double Cqqq = Double.valueOf(getParameter("Cqqq")).doubleValue();
	double Cqqs = Double.valueOf(getParameter("Cqqs")).doubleValue();
	double Cqsq = Double.valueOf(getParameter("Cqsq")).doubleValue();
	double Cqss = Double.valueOf(getParameter("Cqss")).doubleValue();
	double Cssq = Double.valueOf(getParameter("Cssq")).doubleValue();
	double Csss = Double.valueOf(getParameter("Csss")).doubleValue();
	Cqqq *= 1.23*1.23/(0.9*0.9);
	Cqqs *= 1.38*1.38/(1.05*1.05);
	Cqsq *= 1.38*1.38/(1.05*1.05);
	Cqss *= 1.53*1.53/(1.2*1.2);
	Cssq *= 1.53*1.53/(1.2*1.2);
	Csss *= 1.67*1.67/(1.35*1.35);
	CB[0][0] = CB[2][1] = Cqqq; // (uu)u and (dd)d
	CB[0][1] = CB[2][0] = Cqqq/3; // (uu)d and (dd)u
	CB[1][0] = CB[1][1] = Cqqq*2/3; // (ud)u and (ud)d
	CB[0][2] = CB[1][2] = CB[2][2] = Cqqs/3; // (uu)s, (dd)s and (ud)s
	CB[3][0] = CB[4][1] = Cqsq*2/3; // (us)u and (ds)d
	CB[3][1] = CB[4][0] = Cqsq/3; // (us)d and (ds)u
	CB[3][2] = CB[4][2] = Cqss*2/3; // (us)s and (ds)s
	CB[5][0] = CB[5][1] = Cssq/3; // (ss)u and (ss)d
	CB[5][2] = Csss;
	for(int i=0; i<6; ++i) {
	    for(int j=0; j<3; ++j)
		CB[i][j] *= 16.0/9.0;
	}

	// lay out GUI components
	GridBagLayout gbl = new GridBagLayout();
	GridBagConstraints gbc = new GridBagConstraints();
	setLayout(gbl);
	Panel numpan = new Panel();
	add(numpan);
	gbc.gridx = 0;
	gbc.gridy = 0;
	gbl.setConstraints(numpan, gbc);
	add(numpan);
	numpan.setLayout(new GridLayout(8,11));
	numpan.add(startbtn = new Button("Start"));
	for(int i=0; i<3; ++i) {
	    numpan.add(new Label(quarknames[i], Label.CENTER));
	}
	numpan.add(new Label());
	for(int i=0; i<6; ++i) {
	    numpan.add(new Label(diquarknames[i], Label.CENTER));
	}
	numpan.add(new Label("quark"));
	for(int i=0; i<3; ++i) {
	    numpan.add(Lquarks[i] = new TextField());
	}
	numpan.add(new Label("diquark"));
	for(int i=0; i<6; ++i) {
	    numpan.add(Ldiquarks[i] = new TextField());
	}
	numpan.add(new Label("antiq."));
	for(int i=0; i<3; ++i) {
	    numpan.add(Lantiquarks[i] = new TextField());
	}
	numpan.add(new Label("antid."));
	for(int i=0; i<6; ++i) {
	    numpan.add(Lantidiquarks[i] = new TextField());
	}
	numpan.add(new Label());
	for(int i=0; i<9; ++i) {
	    numpan.add(new Label(mesonnames[i], Label.CENTER));
	}
	numpan.add(new Label());
	numpan.add(new Label("meson"));
	for(int i=0; i<9; ++i) {
	    numpan.add(Lmesons[i] = new TextField());
	}
	numpan.add(new Label());
	numpan.add(new Label());
	for(int i=0; i<10; ++i) {
	    numpan.add(new Label(baryonnames[i], Label.CENTER));
	}
	numpan.add(new Label("baryon"));
	for(int i=0; i<10; ++i) {
	    numpan.add(Lbaryons[i] = new TextField());
	}
	numpan.add(new Label("antib."));
	for(int i=0; i<10; ++i) {
	    numpan.add(Lantibaryons[i] = new TextField());
	}
	String[] titles = {
	    "8080ffu, d quarks",
	    "404080s quarks",
	    "ff8080(qq) diquarks",
	    "804040(qs) diquarks",
	    "402020(ss) diquarks",
	    "ffffff(qqq) Delta",
	    "c0c0c0(qqs) Sigma*",
	    "808080(qss) Xi*",
	    "404040(sss) Omega",
	    "e0e0e0qq~ rho",
	    "808080qs~ K*",
	    "404040ss~ phi",
	    "808080sq~ K*~",
	    "404040(sss)~ Omega~",
	    "808080(qss)~ Xi~",
	    "c0c0c0(qqs)~ Sigma*~",
	    "ffffff(qqq)~ Delta~",
	    "402020(ss)~ antidiquarks",
	    "804040(qs)~ antidiquarks",
	    "ff8080(qq)~ antidiquarks",
	    "404080s~ antiquarks",
	    "8080ffu~, d~ antiquarks"};
	add(tcan = new TimeevolCanvas(titles));
	gbc.gridy = 1;
	gbc.weightx = 1.0;
	gbc.weighty = 1.0;
	gbc.fill = GridBagConstraints.BOTH;
	gbl.setConstraints(tcan, gbc);
	setlabels();
    }
    public void start() {
	if(stopped) {
	    thread = new Thread(this);
	    stopped = false;
	}
    }
    public void stop() {
	thread = null;
	stopped = true;
    }
    public void run() {
	while(thread != null) {
	    try {
		Thread.sleep(200);
	    } catch(InterruptedException e) {
	    }
	    if(thread != null) {
		step();
		setlabels();
	    }
	}
    }

    /**
     * A Monte Carlo step.
     */
    private void step() {
	double[] weights = procweights;
	double sum = 0;
	int count = 0;

	// calculation of quark-quark (diquark) coalescence weights
	for(int i=0; i<3; ++i) {
	    for(int j=i; j<3; ++j) {
		double c = CD[i][j];
		if(i != j || Nquarks[i] > 1)
		    sum += weights[count] = c*Nquarks[i]*Nquarks[j];
		else
		    weights[count] = 0;
		count++;
		if(i != j || Nantiquarks[i] > 1)
		    sum += weights[count] = c*Nantiquarks[i]*Nantiquarks[j];
		else
		    weights[count] = 0;
		count++;
	    }
	}

	// calculation of diquark decay weights
	for(int i=0; i<3; ++i) {
	    for(int j=i; j<3; ++j) {
		double c = 3*CD[i][j];
		int n = Ndiquarks[outdiquark[i][j]];
		if(n > 0)
		    sum += weights[count] = c*n;
		else
		    weights[count] = 0;
		count++;
		n = Nantidiquarks[outdiquark[i][j]];
		if(n > 0)
		    sum += weights[count] = c*n;
		else
		    weights[count] = 0;
		count++;
	    }
	}

	// calculation of quark-antiquark (meson) coalescence weights
	for(int i=0; i<3; ++i) {
	    for(int j=0; j<3; ++j) {
		double c = CM[i][j];
		sum += weights[count++] = c*Nquarks[i]*Nantiquarks[j];
	    }
	}

	// calculation of diquark-quark (baryon) coalescence weights
	for(int i=0; i<6; ++i) {
	    for(int j=0; j<3; ++j) {
		double c = CB[i][j];
		sum += weights[count++] = c*Ndiquarks[i]*Nquarks[j];
		sum += weights[count++] = c*Nantidiquarks[i]*Nantiquarks[j];
	    }
	}

	// choose a coalescence process
	rndproc(sum);
    }

    /**
     * Choose a coalescence process randomly.
     */
    private void rndproc(double sum) {
	double x = sum*Math.random();
	int count = 0;
	double[] weights = procweights;
	sum = 0;
	for(int i=0; i<3; ++i) {
	    for(int j=i; j<3; ++j) {
		sum += weights[count++];
		int k = outdiquark[i][j];
		if(x < sum) {
		    Nquarks[i]--;
		    Nquarks[j]--;
		    Ndiquarks[k]++;
		    return;
		}
		sum += weights[count++];
		if(x < sum) {
		    Nantiquarks[i]--;
		    Nantiquarks[j]--;
		    Nantidiquarks[k]++;
		    return;
		}
	    }
	}
	for(int i=0; i<3; ++i) {
	    for(int j=i; j<3; ++j) {
		sum += weights[count++];
		int k = outdiquark[i][j];
		if(x < sum) {
		    Nquarks[i]++;
		    Nquarks[j]++;
		    Ndiquarks[k]--;
		    return;
		}
		sum += weights[count++];
		if(x < sum) {
		    Nantiquarks[i]++;
		    Nantiquarks[j]++;
		    Nantidiquarks[k]--;
		    return;
		}
	    }
	}
	for(int i=0; i<3; ++i) {
	    for(int j=0; j<3; ++j) {
		sum += weights[count++];
		if(x < sum) {
		    Nquarks[i]--;
		    Nantiquarks[j]--;
		    Nmesons[outmeson[i][j]]++;
		    return;
		}
	    }
	}
	for(int i=0; i<6; ++i) {
	    for(int j=0; j<3; ++j) {
		sum += weights[count++];
		int k = outbaryon[i][j];
		if(x < sum) {
		    Ndiquarks[i]--;
		    Nquarks[j]--;
		    Nbaryons[k]++;
		    return;
		}
		sum += weights[count++];
		if(x < sum) {
		    Nantidiquarks[i]--;
		    Nantiquarks[j]--;
		    Nantibaryons[k]++;
		    return;
		}
	    }
	}
    }

    /**
     * Get the labels containing the particle numbers.
     */
    private void getlabels() {
	for(int i=0; i<3; ++i) {
	    Nquarks[i] = Integer.parseInt(Lquarks[i].getText());
	    Nantiquarks[i] = Integer.parseInt(Lantiquarks[i].getText());
	}
	for(int i=0; i<6; ++i) {
	    Ndiquarks[i] = Integer.parseInt(Ldiquarks[i].getText());
	    Nantidiquarks[i] = Integer.parseInt(Lantidiquarks[i].getText());
	}
	for(int i=0; i<9; ++i) {
	    Nmesons[i] = Integer.parseInt(Lmesons[i].getText());
	}
	for(int i=0; i<10; ++i) {
	    Nbaryons[i] = Integer.parseInt(Lbaryons[i].getText());
	    Nantibaryons[i] = Integer.parseInt(Lantibaryons[i].getText());
	}
    }

    /**
     * Set the labels containing the particle numbers.
     */
    private void setlabels() {
	for(int i=0; i<3; ++i) {
	    Lquarks[i].setText(String.valueOf(Nquarks[i]));
	    Lantiquarks[i].setText(String.valueOf(Nantiquarks[i]));
	}
	for(int i=0; i<6; ++i) {
	    Ldiquarks[i].setText(String.valueOf(Ndiquarks[i]));
	    Lantidiquarks[i].setText(String.valueOf(Nantidiquarks[i]));
	}
	for(int i=0; i<9; ++i) {
	    Lmesons[i].setText(String.valueOf(Nmesons[i]));
	}
	for(int i=0; i<10; ++i) {
	    Lbaryons[i].setText(String.valueOf(Nbaryons[i]));
	    Lantibaryons[i].setText(String.valueOf(Nantibaryons[i]));
	}
	Npart[0] = Nquarks[0] + Nquarks[1];
	Npart[1] = Nquarks[2];
	Npart[2] = 2*(Ndiquarks[0] + Ndiquarks[1] + Ndiquarks[2]);
	Npart[3] = 2*(Ndiquarks[3] + Ndiquarks[4]);
	Npart[4] = 2*Ndiquarks[5];
	Npart[5] = 3*(Nbaryons[0] + Nbaryons[1] + Nbaryons[2] + Nbaryons[3]);
	Npart[6] = 3*(Nbaryons[4] + Nbaryons[5] + Nbaryons[6]);
	Npart[7] = 3*(Nbaryons[7] + Nbaryons[8]);
	Npart[8] = 3*Nbaryons[9];
	Npart[9] = 2*(Nmesons[0] + Nmesons[1] + Nmesons[3] + Nmesons[4]);
	Npart[10] = 2*(Nmesons[2] + Nmesons[5]);
	Npart[11] = 2*Nmesons[8];
	Npart[12] = 2*(Nmesons[6] + Nmesons[7]);
	Npart[13] = 3*Nantibaryons[9];
	Npart[14] = 3*(Nantibaryons[7] + Nantibaryons[8]);
	Npart[15] = 3*(Nantibaryons[4] + Nantibaryons[5] + Nantibaryons[6]);
	Npart[16] = 3*(Nantibaryons[0] + Nantibaryons[1] + Nantibaryons[2] + Nantibaryons[3]);
	Npart[17] = 2*Nantidiquarks[5];
	Npart[18] = 2*(Nantidiquarks[3] + Nantidiquarks[4]);
	Npart[19] = 2*(Nantidiquarks[0] + Nantidiquarks[1] + Nantidiquarks[2]);
	Npart[20] = Nantiquarks[2];
	Npart[21] = Nantiquarks[0] + Nantiquarks[1];
	tcan.addP(Npart);
    }

    /**
     * Set the editability of labels.
     */
    private void seteditable(boolean x) {
	for(int i=0; i<3; ++i) {
	    Lquarks[i].setEditable(x);
	    Lantiquarks[i].setEditable(x);
	}
	for(int i=0; i<6; ++i) {
	    Ldiquarks[i].setEditable(x);
	    Lantidiquarks[i].setEditable(x);
	}
	for(int i=0; i<9; ++i) {
	    Lmesons[i].setEditable(x);
	}
	for(int i=0; i<10; ++i) {
	    Lbaryons[i].setEditable(x);
	    Lantibaryons[i].setEditable(x);
	}
    }

    /**
     * Handles the Start/Stop events.
     */
    public boolean action(Event e, Object arg) {
	if(e.target == startbtn) {
	    if(thread != null) {
		thread = null;
		seteditable(true);
		startbtn.setLabel("Start");
	    } else {
		seteditable(false);
		getlabels();
		(thread = new Thread(this)).start();
		startbtn.setLabel("Stop");
	    }
	}
	return false;
    }
}
