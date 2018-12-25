import java.util.Vector;
import java.util.Hashtable;
import java.util.StringTokenizer;

/**
 * Calculator.
 * @version 08/06/2009
 * @since 05/02/2006 (05/24/1992 realfunc.cpp)
 * @author Peter Csizmadia
 */
public class Calculator {
    // see Particle Data Group 2004 (pdg.lbl.gov)
    public static final double SI_c = 299792458.0; // m/s
    public static final double SI_hbar = 1.054571628e-34; // J s (2006 CODATA)
    public static final double SI_G = 6.67428e-11; // m^3/(kg s^2) (2006 CODATA)
    public static final double SI_e = 1.602176487e-19; // C (2006 CODATA)
    public static final double SI_GeV = SI_e*1e9; // J
    public static final double SI_lb = 0.45359237; // 1 pound
    public static final double SI_oz = 0.028349523; // 1 ounce
    public static final double GeV_m_e = 0.000510998910; // GeV (2006 CODATA)
    public static final double GeV_m_p = 0.938272013; // GeV (2006 CODATA)
    public static final double GeV_m_n = 0.939565346; // GeV (2006 CODATA)
    public static final double GeV_m_d = 1.87561282; // GeV (2002 CODATA)
    public static final double GeV_m_alpha = 3.72737917; // GeV (2002 CODATA)
    public static final double GeV_m_h = 2.80839142; // GeV (2002 CODATA)
    public static final double SI_m_Sun = 1.9891e30; // kg (2008 wikipedia)
    public static final double SI_m_Mercury = 3.3022e23; // kg (2008 wikipedia)
    public static final double SI_m_Venus = 4.8685e24; // kg (2008 wikipedia)
    public static final double SI_m_Earth = 5.9736e24; // kg (2008 wikipedia)
    public static final double SI_m_Mars = 6.4185e23; // kg (2008 wikipedia)
    public static final double SI_m_Jupiter = 1.8986e27; // kg (2008 wikipedia)
    public static final double SI_m_Saturn = 5.6846e26; // kg (2008 wikipedia)
    public static final double SI_m_Uranus = 8.6810e25; // kg (2008 wikipedia)
    public static final double SI_m_Neptune = 1.0243e26; // kg (2008 wikipedia)
    public static final double CONST_Avogadro = 6.0221415e+23;
    public static final double CONST_Boltzmann = 1.3806505e-23;
    public static final double CONST_alpha = 1/137.035999679;//0.007297352568;

    public static final double SI_mile = 1609.344; // m (Wikipedia 2007)
    public static final double SI_feet = 0.3048; // m (Wikipedia 2008)
    public static final double SI_AU = 149597870691.0; // m (Wikipedia 2007)
    public static final double SI_pc = SI_AU*(360*60*60)/(2*Math.PI); // m

    private static final byte T_END = 0;
    private static final byte T_FUNCTION = 1;
    private static final byte T_NUMBER = 2;
    private static final byte T_VARIABLE = 3;
    private static final int P_NONE = 0;
    private static final int P_OPENING_PARANTHESIS = 1;
    private static final int P_CLOSING_PARANTHESIS = 2;
    private static final int P_OPERATOR = 3;
    private static final int P_NUMBER = 4;
    private static final int P_FUNCTION = 5;
    private static final int P_VARIABLE = 6;

    public static interface Func {
	int eval(double[] stk, int i);
    }
    private static final Func F0MINUS = new Func() {
	public int eval(double[] x, int i) {
	    x[i-1] = -x[i-1];
	    return i;
	}
    };
    private static final Func FDIV = new Func() {
	public int eval(double[] x, int i) {
	    x[i-2] /= x[i-1];
	    return i-1;
	}
    };
    private Vector FUNCS;
    private Hashtable FUNCHASH;
    private static Hashtable VARHELP;
    private byte[] dtype;
    private Func[] funcs;
    private double[] numbers;
    private int[] variablePointers;
    private int variableCount = 0;
    private String[] variableNames = new String[1];
    private double[] variableValues = new double[1];
    private boolean[] variableSet = new boolean[1];
    private double val_meter;
    private double val_sec;
    private double val_kg;
    private double val_Kelvin;

    static {
	VARHELP = new Hashtable();
	initHelp();
    }

    public Calculator(String str) throws IllegalArgumentException {
	storeFuncs();
	init(str);
    }

    private void storeFuncs() {
	FUNCS = new Vector();
	FUNCHASH = new Hashtable();
	store("+", new Func() {
	    public int eval(double[] x, int i) {
		x[i-2] += x[i-1];
		return i-1;
	    }
	});
	store("-", new Func() {
	    public int eval(double[] x, int i) {
		x[i-2] -= x[i-1];
		return i-1;
	    }
	});
	store("*", new Func() {
	    public int eval(double[] x, int i) {
		x[i-2] *= x[i-1];
		return i-1;
	    }
	});
	store("/", FDIV);
	store("^", new Func() {
	    public int eval(double[] x, int i) {
		x[i-2] = Math.pow(x[i-2], x[i-1]);
		return i-1;
	    }
	});
	store("abs", new Func() {
	    public int eval(double[] x, int i) {
		x[i-1] = Math.abs(x[i-1]);
		return i;
	    }
	});
	store("sgn", new Func() {
	    public int eval(double[] x, int i) {
		x[i-1] = x[i-1] < 0? -1 : x[i-1] > 0? 1 : 0;
		return i;
	    }
	});
	store("Heaviside", new Func() {
	    public int eval(double[] x, int i) {
		x[i-1] = x[i-1] > 0? 1 : x[i-1] < 0? 0 : 0.5;
		return i;
	    }
	});
	store("sqrt", new Func() {
	    public int eval(double[] x, int i) {
		x[i-1] = Math.sqrt(x[i-1]);
		return i;
	    }
	});
	store("exp", new Func() {
	    public int eval(double[] x, int i) {
		x[i-1] = Math.exp(x[i-1]);
		return i;
	    }
	});
	Func log = new Func() {
	    public int eval(double[] x, int i) {
		x[i-1] = Math.log(x[i-1]);
		return i;
	    }
	};
	store("log", log);
	store("log10", new Func() {
	    public int eval(double[] x, int i) {
		x[i-1] = Math.log(x[i-1])/Math.log(10.0);
		return i;
	    }
	});
	store("sin", new Func() {
	    public int eval(double[] x, int i) {
		x[i-1] = Math.sin(x[i-1]);
		return i;
	    }
	});
	store("asin", new Func() {
	    public int eval(double[] x, int i) {
		x[i-1] = Math.asin(x[i-1]);
		return i;
	    }
	});
	store("cos", new Func() {
	    public int eval(double[] x, int i) {
		x[i-1] = Math.cos(x[i-1]);
		return i;
	    }
	});
	store("acos", new Func() {
	    public int eval(double[] x, int i) {
		x[i-1] = Math.acos(x[i-1]);
		return i;
	    }
	});
	store("tan", new Func() {
	    public int eval(double[] x, int i) {
		x[i-1] = Math.tan(x[i-1]);
		return i;
	    }
	});
	store("atan", new Func() {
	    public int eval(double[] x, int i) {
		x[i-1] = Math.atan(x[i-1]);
		return i;
	    }
	});
	store("cot", new Func() {
	    public int eval(double[] x, int i) {
		x[i-1] = Math.cos(x[i-1])/Math.sin(x[i-1]);
		return i;
	    }
	});
	store("acot", new Func() {
	    public int eval(double[] x, int i) {
		x[i-1] = x[i-1] == 0? Math.PI/2 : Math.atan(1/x[i-1]);
		return i;
	    }
	});
	store("sinh", new Func() {
	    public int eval(double[] x, int i) {
		double a = Math.exp(x[i-1]);
		double b = 1/a;
		x[i-1] = (a - b)/2;
		return i;
	    }
	});
	store("asinh", new Func() {
	    public int eval(double[] x, int i) {
		double a = x[i-1];
		x[i-1] = Math.log(a + Math.sqrt(a*a + 1));
		return i;
	    }
	});
	store("cosh", new Func() {
	    public int eval(double[] x, int i) {
		double a = Math.exp(x[i-1]);
		double b = 1/a;
		x[i-1] = (a + b)/2;
		return i;
	    }
	});
	store("acosh", new Func() {
	    public int eval(double[] x, int i) {
		double a = x[i-1];
		x[i-1] = Math.log(a + Math.sqrt(a*a - 1));
		return i;
	    }
	});
	store("tanh", new Func() {
	    public int eval(double[] x, int i) {
		double a = Math.exp(x[i-1]);
		double b = 1/a;
		x[i-1] = (a - b)/(a + b);
		return i;
	    }
	});
	store("atanh", new Func() {
	    public int eval(double[] x, int i) {
		double a = x[i-1];
		x[i-1] = 0.5*Math.log((1+a)/(1-a));
		return i;
	    }
	});
	store("LambertW", new Func() {
	    public int eval(double[] x, int i) {
		x[i-1] = LambertW(x[i-1]);
		return i;
	    }
	});
	Func rand = new Func() {
	    public int eval(double[] x, int i) {
		x[i-1] *= Math.random();
		return i;
	    }
	};
	store("random", rand);
	store("GaussRandom", new Func() {
	    public int eval(double[] x, int i) {
		x[i-1] *= Math.random() + Math.random() + Math.random()
			+ Math.random() + Math.random() + Math.random()
			+ Math.random() + Math.random() + Math.random()
			+ Math.random() + Math.random() + Math.random() - 6;
		return i;
	    }
	});
	store("T_atm", new Func() {
	    public int eval(double[] x, int i) {
		// NASA Earth Atmosphere Model
		double h = x[i-1]/val_meter; // altitude
		double T = calcAtmosphericTemperature_SI(h); // in Kelvin
		x[i-1] = T*val_Kelvin;
		return i;
	    }
	});
	store("p_atm", new Func() {
	    public int eval(double[] x, int i) {
		// NASA Earth Atmosphere Model
		double h = x[i-1]/val_meter; // altitude
		double p = calcAtmosphericPressure_SI(h); // in Pascal
		x[i-1] = p*val_kg/(val_meter*val_sec*val_sec);
		return i;
	    }
	});
	store("rho_atm|\u03f1_atm", new Func() {
	    public int eval(double[] x, int i) {
		// NASA Earth Atmosphere Model
		double h = x[i-1]/val_meter; // altitude
		double T = calcAtmosphericTemperature_SI(h); // in Kelvin
		double p = calcAtmosphericPressure_SI(h); // in Pascal
		double rho = p/(286.9*T);
		x[i-1] = rho*val_kg/(val_meter*val_meter*val_meter);
		return i;
	    }
	});
	store("wgs84_r", new Func() {
	    public int eval(double[] x, int i) {
		// WGS84 Earth radius
		double a = 6378137;
		double b = 6356752.314245;
		double phi = x[i-1]/180*Math.PI;
		double acos = a*Math.cos(phi);
		double bsin = b*Math.sin(phi);
		x[i-1] = Math.sqrt((a*a*acos*acos + b*b*bsin*bsin)
				 /(acos*acos + bsin*bsin))*val_meter;
		return i;
	    }
	});
    }

    private static final double LAMBERT_W_ARG_MIN = -Math.exp(-1.0);
    private static final double LAMBERT_W_ARG_DOUBLE_PREC = 1.1102230246251565e-16;
    private static final double LAMBERT_W_DOUBLE_PREC = 1e-14;

    private static double LambertW(double x) {
	if(x < LAMBERT_W_ARG_MIN - LAMBERT_W_ARG_DOUBLE_PREC) {
	    throw new IllegalArgumentException("LambertW("+x+") is complex");
	} else if(x < LAMBERT_W_ARG_MIN + LAMBERT_W_ARG_DOUBLE_PREC) {
	    return -1;
	}
	// initial guess using desy_lambert_W
	double w;
	if(x <= 500.0) {
	    double lx1 = Math.log(x + 1.0);
	    w = 0.665 * (1 + 0.0195 * lx1) * lx1 + 0.04;
	} else {
	    w = Math.log(x - 4.0) - (1.0 - 1.0/Math.log(x))
		    * Math.log(Math.log(x));
	}
	for(int i = 0; i < 100; ++i) {
	    double we = w * Math.exp(w);
	    double w1e = (w + 1) * Math.exp(w);
	    if(Math.abs((x - we) / w1e) < LAMBERT_W_DOUBLE_PREC) {
		return w;
	    }
	    w -= (we - x) / (w1e - (w+2) * (we-x) / (2*w+2));
	}
	throw new RuntimeException("LambertW(" + x + ") does not converge");
    }

    private static double calcAtmosphericTemperature_SI(double h) {
	// NASA Earth Atmosphere Model
	double T; // temperature in Kelvin
	if(h < 11000) { // Troposphere
	    T = 288.19 - 0.0065*h;
	} else if(h < 25000) { // Lower Stratosphere
	    T = 216.69;
	} else { // Upper stratosphere
	    T = 216.69 + 0.00299*(h - 25000);
	}
	return T;
    }

    private static double calcAtmosphericPressure_SI(double h) {
	// NASA Earth Atmosphere Model
	double p; // pressure in Pascal
	if(h < 11000) { // Troposphere
	    double T = 288.19 - 0.0065*h;
	    p = 101200*Math.pow(T/288.08, 5.256);
	} else if(h < 25000) { // Lower Stratosphere
	    p = 22654.19912097127*Math.exp(0.00015778*(11000-h));
	} else { // Upper stratosphere
	    double T = 216.69 + 0.00299*(h - 25000);
	    p = 2487.8657798111417*Math.pow(T/216.69, -11.388);
	}
	return p;
    }

    private void init(String str) {
	str = eatWhitespace(str);
	checkSyntax(str);
	Func[] stk = new Func[256];
	int sp = 0;
	int ifunc = 0;
	int inumber = 0;
	int k = 0;
	int n = str.length();
	dtype = new byte[n+1];
	funcs = new Func[n/2 + 1];
	numbers = new double[n/2 + 1];
	variablePointers = new int[n/2 + 2];
	int dp = 0;
	int prev = P_NONE;
	for(int istr = 0; istr < str.length(); ++istr) {
	    char cstr = str.charAt(istr);
	    switch(cstr) {
	    case '(': stk[sp++] = null;
		    prev = P_OPENING_PARANTHESIS;
		    break;
	    case ')': {
		    int e = --sp;
		    while(stk[e] != null) {
			funcs[ifunc++] = stk[e];
			dtype[dp++] = T_FUNCTION;
			e = --sp;
		    }
		    prev = P_CLOSING_PARANTHESIS;
		} break;
	    default:
		if(isOp(cstr)) {
		    boolean var1 = istr == 0
			    || (istr != 0 && (str.charAt(istr-1) == '('
					      || isOp(str.charAt(istr - 1))));
		    if(!(var1 && isNum(str.charAt(istr + 1)))) {
			String s = String.valueOf(cstr);
			Func func;
			if(var1) {
			    func = cstr == '-'? F0MINUS : null;
			} else {
			    func = (Func)FUNCHASH.get(s);
			}
			if(func == null) {
			    break;
			}
			int precendence = prec(func);
			int e = sp - 1;
			while(sp != 0 && precendence <= prec(stk[e])
				&& stk[e] != null) {
			    funcs[ifunc++] = stk[--sp];
			    dtype[dp++] = T_FUNCTION;
			    e = sp - 1;
			}
			if(func != null) {
			    stk[sp++] = func;
			}
			prev = P_OPERATOR;
			break;
		    }
		}
		if(isNum(cstr) || cstr == '+' || cstr == '-') {
		    int[] p = new int[1];
		    p[0] = istr;
		    numbers[inumber++] = strtod(str, p);
		    dtype[dp++] = T_NUMBER;
		    istr = p[0] - 1;
		    prev = P_NUMBER;
		} else if(isAlpha(cstr)) {
		    if(dp > 0
			    && prev != P_NONE
			    && prev != P_OPENING_PARANTHESIS
			    && prev != P_OPERATOR
			    && prev != P_FUNCTION) {
			throw new IllegalArgumentException("multiplication " +
			    "missing, try " +
			    str.substring(0, istr) + "*...");
		    }
		    int p;
		    for(p = istr; p < str.length()
			    && isIdChar(str.charAt(p)); ++p);
		    String name = str.substring(istr, p);
		    if(p < str.length() && str.charAt(p) == '(') {
			stk[sp++] = (Func)FUNCHASH.get(name);
			if(stk[sp - 1] == null) {
			    throw new IllegalArgumentException(name
				+ "( ): undefined function");
			}
			stk[sp++] = null;
			++p;
			prev = P_FUNCTION;
		    } else {
			int i = variableIndex(name);
			variablePointers[k++] = i;
			dtype[dp++] = T_VARIABLE;
			prev = P_VARIABLE;
		    }
		    istr = p-1;
		}
	    }
	}
	while(sp != 0) {
	    funcs[ifunc++] = stk[--sp];
	    dtype[dp++] = T_FUNCTION;
	}
	dtype[dp] = T_END;
    }

    private static void initHelp() {
	setHelp("+", "Addition.");
	setHelp("-", "Subtraction.");
	setHelp("*", "Multiplication.");
	setHelp("/", "Division.");
	setHelp("^", "Exponentiation.");
	setHelp("abs", "Absolute value function.");
	setHelp("sgn", "Sign function: "
		    + "sgn(x) = -1 if x < 0, +1 if x > 0, 0 if x = 0.");
	setHelp("sqrt", "Square root function.");
	setHelp("exp", "Exponential function.");
	setHelp("log", "Natural logarithm function.");
	setHelp("log10", "Base 10 logarithm function.");
	setHelp("sin", "Sine function.");
	setHelp("cos", "Cosine function.");
	setHelp("tan", "Tangent function.");
	setHelp("cot", "Cotangent function.");
	setHelp("asin", "Arcsine function.");
	setHelp("acos", "Arccosine function.");
	setHelp("atan", "Arctangent function.");
	setHelp("acot", "Arccotangent function.");
	setHelp("sinh", "Hyperbolic sine function.");
	setHelp("cosh", "Hyperbolic cosine function.");
	setHelp("tanh", "Hyperbolic tangent function.");
	setHelp("asinh", "Inverse hyperbolic sine function.");
	setHelp("acosh", "Inverse hyperbolic cosine function.");
	setHelp("atanh", "Inverse hyperbolic tangent function.");
	setHelp("Heaviside", "Heaviside step function: "
		    + "Heaviside(x) = 0 if x < 0, 1 if x > 0, 1/2 if x = 0.");
	setHelp("LambertW", "The Lambert W function is the inverse of "
		+ "f(w) = w*exp(w).");
	setHelp("random", "Uniformly distributed random number: "
		    + "0 < random(max) < max.");
	setHelp("GaussRandom", "Gaussian random number with mean 0 and "
		    + "specified standard deviation.");
	setHelp("T_atm", "Atmospheric temperature in the specified altitude."
		+ " Estimated using the NASA Earth Atmosphere Model.");
	setHelp("p_atm", "Atmospheric pressure in the specified altitude."
		+ " Estimated using the NASA Earth Atmosphere Model.");
	setHelp("rho_atm|\u03f1_atm",
		"Atmospheric air density in the specified altitude."
		+ " Estimated using the NASA Earth Atmosphere Model.");
	setHelp("wgs84_r", "WGS84 Earth radius at given latitude.");
	setHelp("pi|\u03c0", "Ludolph's number.");
	setHelp("N_A", "Avogadro constant.");
	setHelp("k_B", "Boltzmann constant.");
	setHelp("alpha|\u03b1", "Fine-structure constant. Experimental values:\n"+
"- 1/(137.03599911 \u00b1 4.6e-7) = 0.007297352568 \u00b1 2.4e-11\n"+
"  (CODATA 2002; P.J. Mohr and B.N.Taylor, Rev. Mod. Phys. 77 (2005) 1-107)\n"+
"- 1/(137.035999710 \u00b1 9.6e-8)\n"+
"  (G. Gabrielse et al., Phys. Rev. Lett. 97 (2006) 030802)\n"+
"Formulas:\n"+
"- 137/cos(\u03c0/137)*\u03c0/(137*29)/tan(\u03c0/(137*29))  - J.Gilson, PIRT Conf. 2000\n"+
"- 4*\u03c0^3+\u03c0^2+\u03c0 = 137.03630  (error=3.0e-4)");
	setHelp("c", "Speed of light in vacuum.");
	setHelp("hbar|\u210f", "Reduced Planck constant: h/2\u03c0.");
	setHelp("G", "Gravitational constant.");
	setHelp("m_planck", "Planck mass: sqrt(\u210f*c/G).");
	setHelp("E_planck", "Planck energy: sqrt(\u210f*c^5/G).");
	setHelp("l_planck", "Planck length: sqrt(G*\u210f/c^3).");
	setHelp("t_planck", "Planck time: sqrt(G*\u210f/c^5).");
	setHelp("u", "Unified mass unit." +
		"One twelfth of the mass of an unbound C-12 atom at rest and "+
		"in ground state.");
	setHelp("e", "Electron charge.");
	setHelp("m_e", "Electron mass.");
	setHelp("m_p", "Proton mass.");
	setHelp("m_n", "Neutron mass.");
	setHelp("m_d", "Deuteron mass.");
	setHelp("m_h", "Mass of the helion particle (3He2+).");
	setHelp("m_alpha", "Mass of the alpha particle (4He2+).");
	setHelp("lbs", "1 pound");
	setHelp("oz", "1 ounce");
	setHelp("m_Sun", "Mass of the Sun.");
	setHelp("m_Mercury", "Mass of Mercury.");
	setHelp("m_Venus", "Mass of Venus.");
	setHelp("m_Earth", "Earth mass.");
	setHelp("m_Mars", "Mass of Mars.");
	setHelp("m_Jupiter", "Mass of Jupiter.");
	setHelp("m_Saturn", "Mass of Saturn.");
	setHelp("m_Uranus", "Mass of Uranus.");
	setHelp("m_Neptune", "Mass of Neptune.");
	setHelp("s", "Second.");
	setHelp("ms", "Millisecond.");
	setHelp("mus|\u03bcs", "Microsecond.");
	setHelp("ns", "Nanosecond.");
	setHelp("ps", "Picosecond.");
	setHelp("minute|minutes", "Minute: 60 seconds.");
	setHelp("hour|hours", "Hour: 3600 seconds.");
	setHelp("day|days", "Day: 86400 seconds.");
	setHelp("year|years", "Year: 365.25*86400 seconds.");
	setHelp("m", "Meter.");
	setHelp("cm", "Centimeter.");
	setHelp("mm", "Millimeter.");
	setHelp("mum|\u03bcm", "Micrometer.");
	setHelp("nm", "Nanometer.");
	setHelp("\u00c5|angstrom", "\u00c5ngstr\u00f6m.");
	setHelp("pm", "Picometer.");
	setHelp("fm", "Femtometer.");
	setHelp("km", "Kilometer.");
	setHelp("mile|miles", "Statute mile.");
	setHelp("yard|yd", "International yard.");
	setHelp("feet|ft", "International foot.");
	setHelp("inch|in", "International inch.");
	setHelp("AU", "Astronomical Unit: " +
		      "semi-major axis of Earth's orbit around the Sun.");
	setHelp("ly", "Light year: c*365.25*86400s.");
	setHelp("pc", "Parsec: 360*60*60/(2\u03c0) AU");
	setHelp("kpc", "Kiloparsec.");
	setHelp("Mpc", "Megaparsec.");
	setHelp("eV", "Electronvolt.");
	setHelp("keV", "Kiloelectronvolt.");
	setHelp("MeV", "Megaelectronvolt.");
	setHelp("GeV", "Gigaelectronvolt.");
	setHelp("TeV", "Teraelectronvolt.");
	setHelp("SI", "Use the International System of Units: m = s = kg = 1.");
	setHelp("nuclear", "Use nuclear units: c = hbar = GeV = 1.");
	setHelp("Planck", "Use Planck units: c = hbar = G = 1.");
    }

    public static Calculator createSI(String expr) {
	Calculator c = new Calculator(expr);
	c.set("pi|\u03c0", Math.PI);
	c.set("N_A", CONST_Avogadro);
	c.set("k_B", CONST_Boltzmann);
	c.set("alpha|\u03b1", CONST_alpha);
	c.set("c", SI_c);
	c.set("hbar|\u210f", SI_hbar);
	c.set("G", SI_G);
	c.set("m_planck", Math.sqrt(SI_hbar*SI_c/SI_G));
	c.set("E_planck", Math.sqrt(SI_hbar*SI_c/SI_G)*(SI_c*SI_c));
	c.set("l_planck", Math.sqrt(SI_G*SI_hbar/SI_c)/SI_c);
	c.set("t_planck", Math.sqrt(SI_G*SI_hbar/SI_c)/(SI_c*SI_c));
	c.set("u", 0.001/CONST_Avogadro);
	c.set("e", SI_e);
	c.set("m_e", GeV_m_e*SI_GeV/(SI_c*SI_c));
	c.set("m_p", GeV_m_p*SI_GeV/(SI_c*SI_c));
	c.set("m_n", GeV_m_n*SI_GeV/(SI_c*SI_c));
	c.set("m_d", GeV_m_d*SI_GeV/(SI_c*SI_c));
	c.set("m_h", GeV_m_h*SI_GeV/(SI_c*SI_c));
	c.set("m_alpha", GeV_m_alpha*SI_GeV/(SI_c*SI_c));
	c.set("lbs", SI_lb);
	c.set("oz", SI_oz);
	c.set("m_Sun", SI_m_Sun);
	c.set("m_Mercury", SI_m_Mercury);
	c.set("m_Venus", SI_m_Venus);
	c.set("m_Earth", SI_m_Earth);
	c.set("m_Mars", SI_m_Mars);
	c.set("m_Jupiter", SI_m_Jupiter);
	c.set("m_Saturn", SI_m_Saturn);
	c.set("m_Uranus", SI_m_Uranus);
	c.set("m_Neptune", SI_m_Neptune);
	c.set("s", 1.0);
	c.set("ms", 1e-3);
	c.set("mus|\u03bcs", 1e-6);
	c.set("ns", 1e-9);
	c.set("ps", 1e-12);
	c.set("minute|minutes", 60.0);
	c.set("hour|hours", 3600.0);
	c.set("day|days", 86400.0);
	c.set("year|years", 365.25*86400.0);
	c.set("m", 1.0);
	c.set("cm", 1e-2);
	c.set("mm", 1e-3);
	c.set("mum|\u03bcm", 1e-6);
	c.set("nm", 1e-9);
	c.set("\u00c5|angstrom", 1e-10);
	c.set("pm", 1e-12);
	c.set("fm", 1e-15);
	c.set("km", 1000.0);
	c.set("mile|miles", SI_mile);
	c.set("yard|yd", SI_feet*3);
	c.set("feet|ft", SI_feet);
	c.set("inch|in", SI_feet/12);
	c.set("AU", SI_AU);
	c.set("ly", SI_c*365.25*24*60*60);
	c.set("pc", SI_pc);
	c.set("kpc", 1e3*SI_pc);
	c.set("Mpc", 1e6*SI_pc);
	c.set("eV", 1e-9*SI_GeV);
	c.set("keV", 1e-6*SI_GeV);
	c.set("MeV", 1e-3*SI_GeV);
	c.set("GeV", SI_GeV);
	c.set("TeV", 1e+3*SI_GeV);
	c.check();
	c.val_meter = 1;
	c.val_sec = 1;
	c.val_kg = 1;
	c.val_Kelvin = 1;
	return c;
    }

    public static Calculator createNuclear(String expr) {
	Calculator c = new Calculator(expr);
	c.set("pi|\u03c0", Math.PI);
	c.set("N_A", CONST_Avogadro);
	c.set("k_B", 1);
	c.set("alpha|\u03b1", CONST_alpha);
	c.set("c", 1.0);
	c.set("hbar|\u210f", 1.0);
	double GeV_per_c2 = SI_GeV/(SI_c*SI_c);
	double G = SI_G*GeV_per_c2*GeV_per_c2/(SI_c*SI_hbar);
	c.set("G", G);
	c.set("m_planck", Math.pow(SI_c, 2.5)*Math.sqrt(SI_hbar/SI_G)/SI_GeV);
	c.set("E_planck", Math.pow(SI_c, 2.5)*Math.sqrt(SI_hbar/SI_G)/SI_GeV);
	c.set("l_planck", Math.sqrt(SI_G/(SI_hbar*SI_c))*SI_GeV/(SI_c*SI_c));
	c.set("t_planck", Math.sqrt(SI_G/(SI_hbar*SI_c))*SI_GeV/(SI_c*SI_c));
	c.set("u", 0.001/CONST_Avogadro*SI_c*SI_c/SI_GeV);
	c.set("e", 1.0);
	c.set("m_e", GeV_m_e);
	c.set("m_p", GeV_m_p);
	c.set("m_n", GeV_m_n);
	c.set("m_d", GeV_m_d);
	c.set("m_h", GeV_m_h);
	c.set("m_alpha", GeV_m_alpha);
	c.set("lbs", SI_lb*SI_c*SI_c/SI_GeV);
	c.set("oz", SI_oz*SI_c*SI_c/SI_GeV);
	c.set("m_Sun", SI_m_Sun*SI_c*SI_c/SI_GeV);
	c.set("m_Mercury", SI_m_Mercury*SI_c*SI_c/SI_GeV);
	c.set("m_Venus", SI_m_Venus*SI_c*SI_c/SI_GeV);
	c.set("m_Earth", SI_m_Earth*SI_c*SI_c/SI_GeV);
	c.set("m_Mars", SI_m_Mars*SI_c*SI_c/SI_GeV);
	c.set("m_Jupiter", SI_m_Jupiter*SI_c*SI_c/SI_GeV);
	c.set("m_Saturn", SI_m_Saturn*SI_c*SI_c/SI_GeV);
	c.set("m_Uranus", SI_m_Uranus*SI_c*SI_c/SI_GeV);
	c.set("m_Neptune", SI_m_Neptune*SI_c*SI_c/SI_GeV);
	double meter = SI_GeV/(SI_hbar*SI_c);
	double sec = SI_GeV/SI_hbar;
	c.set("s", sec);
	c.set("ms", 1e-3*sec);
	c.set("mus|\u03bcs", 1e-6*sec);
	c.set("ns", 1e-9*sec);
	c.set("ps", 1e-12*sec);
	c.set("minute|minutes", 60.0*sec);
	c.set("hour|hours", 3600.0*sec);
	c.set("day|days", 86400.0*sec);
	c.set("year|years", 365.25*86400.0*sec);
	c.set("m", meter);
	c.set("cm", 1e-2*meter);
	c.set("mm", 1e-3*meter);
	c.set("mum|\u03bcm", 1e-6*meter);
	c.set("nm", 1e-9*meter);
	c.set("\u00c5|angstrom", 1e-10*meter);
	c.set("pm", 1e-12*meter);
	c.set("fm", 1e-15*meter);
	c.set("km", 1000.0*meter);
	c.set("mile|miles", SI_mile*meter);
	c.set("yard|yd", SI_feet*meter*3);
	c.set("feet|ft", SI_feet*meter);
	c.set("inch|in", SI_feet*meter/12);
	c.set("AU", SI_AU*meter);
	c.set("ly", 365.25*24*60*60*sec);
	c.set("pc", SI_pc*meter);
	c.set("kpc", 1e3*SI_pc*meter);
	c.set("Mpc", 1e6*SI_pc*meter);
	c.set("eV", 1e-9);
	c.set("keV", 1e-6);
	c.set("MeV", 1e-3);
	c.set("GeV", 1.0);
	c.set("TeV", 1e+3);
	c.check();
	c.val_meter = meter;
	c.val_sec = sec;
	c.val_kg = SI_c*SI_c/SI_GeV;
	c.val_Kelvin = CONST_Boltzmann/SI_GeV;
	return c;
    }

    public static Calculator createPlanck(String expr) {
	Calculator c = new Calculator(expr);
	c.set("pi|\u03c0", Math.PI);
	c.set("N_A", CONST_Avogadro);
	c.set("k_B", 1);
	c.set("alpha|\u03b1", CONST_alpha);
	c.set("c", 1.0);
	c.set("hbar|\u210f", 1.0);
	c.set("G", 1.0);
	c.set("m_planck", 1.0);
	c.set("E_planck", 1.0);
	c.set("l_planck", 1.0);
	c.set("t_planck", 1.0);
	double cm = SI_GeV*Math.pow(SI_c, -2.5)*Math.sqrt(SI_G/SI_hbar);
	c.set("u", 0.001/CONST_Avogadro/Math.sqrt(SI_hbar*SI_c/SI_G));
	c.set("e", 1.0);
	c.set("m_e", cm*GeV_m_e);
	c.set("m_p", cm*GeV_m_p);
	c.set("m_n", cm*GeV_m_n);
	c.set("m_d", cm*GeV_m_d);
	c.set("m_h", cm*GeV_m_h);
	c.set("m_alpha", cm*GeV_m_alpha);
	c.set("lbs", SI_lb*Math.sqrt(SI_G/(SI_hbar*SI_c)));
	c.set("oz", SI_oz*Math.sqrt(SI_G/(SI_hbar*SI_c)));
	c.set("m_Sun", SI_m_Sun*Math.sqrt(SI_G/(SI_hbar*SI_c)));
	c.set("m_Mercury", SI_m_Mercury*Math.sqrt(SI_G/(SI_hbar*SI_c)));
	c.set("m_Venus", SI_m_Venus*Math.sqrt(SI_G/(SI_hbar*SI_c)));
	c.set("m_Earth", SI_m_Earth*Math.sqrt(SI_G/(SI_hbar*SI_c)));
	c.set("m_Mars", SI_m_Mars*Math.sqrt(SI_G/(SI_hbar*SI_c)));
	c.set("m_Jupiter", SI_m_Jupiter*Math.sqrt(SI_G/(SI_hbar*SI_c)));
	c.set("m_Saturn", SI_m_Saturn*Math.sqrt(SI_G/(SI_hbar*SI_c)));
	c.set("m_Uranus", SI_m_Uranus*Math.sqrt(SI_G/(SI_hbar*SI_c)));
	c.set("m_Neptune", SI_m_Neptune*Math.sqrt(SI_G/(SI_hbar*SI_c)));
	double meter = SI_c/Math.sqrt(SI_G*SI_hbar/SI_c);
	double sec = SI_c*SI_c/Math.sqrt(SI_G*SI_hbar/SI_c);
	c.set("s", sec);
	c.set("ms", 1e-3*sec);
	c.set("mus|\u03bcs", 1e-6*sec);
	c.set("ns", 1e-9*sec);
	c.set("ps", 1e-12*sec);
	c.set("m", meter);
	c.set("minute|minutes", 60.0*sec);
	c.set("hour|hours", 3600.0*sec);
	c.set("day|days", 86400.0*sec);
	c.set("year|years", 365.25*86400.0*sec);
	c.set("cm", 1e-2*meter);
	c.set("mm", 1e-3*meter);
	c.set("mum|\u03bcm", 1e-6*meter);
	c.set("nm", 1e-9*meter);
	c.set("\u00c5|angstrom", 1e-10*meter);
	c.set("pm", 1e-12*meter);
	c.set("fm", 1e-15*meter);
	c.set("km", 1000.0*meter);
	c.set("mile|miles", SI_mile*meter);
	c.set("yard|yd", SI_feet*meter*3);
	c.set("feet|ft", SI_feet*meter);
	c.set("inch|in", SI_feet*meter/12);
	c.set("AU", SI_AU*meter);
	c.set("ly", 365.25*24*60*60*sec);
	c.set("pc", SI_pc*meter);
	c.set("kpc", 1e3*SI_pc*meter);
	c.set("Mpc", 1e6*SI_pc*meter);
	c.set("eV", 1e-9*cm);
	c.set("keV", 1e-6*cm);
	c.set("MeV", 1e-3*cm);
	c.set("GeV", cm);
	c.set("TeV", 1e+3*cm);
	c.check();
	c.val_meter = meter;
	c.val_sec = sec;
	double si_E_planck = Math.sqrt(SI_hbar*SI_c/SI_G)*(SI_c*SI_c);
	c.val_kg = SI_c*SI_c/si_E_planck;
	c.val_Kelvin = CONST_Boltzmann / si_E_planck;
	return c;
    }

    public static String getHelpSI() {
	return "Operators and functions: + - * / ^ abs, sgn,"+
" sqrt, exp, log, log10.\n"+
"- Trigonometry: sin, cos, tan, cot, asin, acos, atan, acot.\n"+
"- Hyperbolic: sinh, cosh, tanh, asinh, acosh, atanh.\n"+
"- Other math: Heaviside, random, GaussRandom, LambertW.\n"+
"- Earth: wgs84_r(lat.), T_atm(alt.), p_atm(alt.), \u03f1_atm=rho_atm(alt.)\n"+
"Constants: \u03c0=pi, N_A, k_B, \u03b1=alpha, c, \u210f=hbar, G\n"+
"- Time: s, ms, \u03bcs=mus, ns, ps, minute, hour, year, t_planck.\n"+
"- Distance: m, cm, mm, \u03bcm=mum, nm, \u00c5=angstrom,"+
" pm, fm, km, yard, yd, feet, ft, inch, in, mile,\n"+
"            AU, ly, pc, kpc, Mpc, l_planck.\n"+
"- Mass: lbs, oz, u, m_e, m_p, m_n, m_d, m_h, m_alpha, m_planck,\n"+
"    m_Sun, m_Mercury, m_Venus, m_Earth, m_Mars, m_Jupiter, m_Saturn,"+
" m_Uranus, m_Neptune\n"+
"- Energy: eV, keV, MeV, GeV, TeV, E_planck.";
    }

    private int variableIndex(String name) {
	int k = variableCount;
	for(int i = 0; i < k; ++i) {
	    if(variableNames[i].equals(name)) {
		return i;
	    }
	}
	if(k == variableNames.length) {
	    String[] tmps = new String[(k + 2)*3/2];
	    double[] tmpx = new double[tmps.length];
	    boolean[] tmpv = new boolean[tmps.length];
	    System.arraycopy(variableNames, 0, tmps, 0, k);
	    System.arraycopy(variableValues, 0, tmpx, 0, k);
	    System.arraycopy(variableSet, 0, tmpv, 0, k);
	    variableNames = tmps;
	    variableValues = tmpx;
	    variableSet = tmpv;
	}
	variableNames[k] = name;
	variableValues[k] = 0;
	variableSet[k] = false;
	variableCount = k + 1;
	return k;
    }

    public static String getHelp(String name) {
	return (String)VARHELP.get(name);
    }

    public static void setHelp(String name, String s) {
	StringTokenizer st = new StringTokenizer(name, "|");
	while(st.hasMoreTokens()) {
	    name = st.nextToken();
	    VARHELP.put(name, s);
	}
    }

    public void set(String name, double x) {
	StringTokenizer st = new StringTokenizer(name, "|");
	while(st.hasMoreTokens()) {
	    name = st.nextToken();
	    for(int i = 0; i < variableCount; ++i) {
		if(variableNames[i].equals(name)) {
		    variableValues[i] = x;
		    variableSet[i] = true;
		}
	    }
	}
    }

    public void check() throws IllegalArgumentException {
	for(int i = 0; i < variableCount; ++i) {
	    String name = variableNames[i];
	    if(!variableSet[i]) {
		throw new IllegalArgumentException(variableNames[i]
			+ ": undefined variable");
	    }
	    if(VARHELP.get(name) == null) {
		throw new IllegalArgumentException("no help for "+name);
	    }
	}
    }

    public double eval() {
	double[] stk = new double[256];
	int sp = 0;
	int a = 0;
	int v = 0;
	int f = 0;
	stk[0] = 0;
	for(int dp = 0; dtype[dp] != T_END; ++dp) {
	    switch(dtype[dp]) {
	    case T_FUNCTION: sp = funcs[f++].eval(stk, sp); break;
	    case T_NUMBER:   stk[sp++] = numbers[a++]; break;
	    case T_VARIABLE: stk[sp++] = variableValues[variablePointers[v++]];
			     break;
	    }
	}
	return stk[0];
    }

    private static void checkSyntax(String str) {
	int z = 0;
	for(int i = 0; i < str.length(); ++i) {
	    char c = str.charAt(i);
	    if(c == '(') {
		z++;
	    } else if(c == ')') {
		z--;
	    } else if(!isIdChar(c) && !isNum(c) && !isOp(c)) {
		throw new IllegalArgumentException("unknown character `"
					    + String.valueOf(c) + "'");
	    }
	    if(z < 0) {
		throw new IllegalArgumentException("opening paranthesis "
						   + "missing");
	    }
	}
	if(z > 0) {
	    throw new IllegalArgumentException("closing paranthesis "
					     + "missing");
	}
    }

    private static boolean isAlpha(char c) {
	return Character.isLetter(c);
    }
    private static boolean isDigit(char c) {
	return c >= '0' && c <='9';
    }
    private static boolean isNum(char c) {
	return isDigit(c) || c == '.';
    }
    private static boolean isIdChar(char c) {
	return isAlpha(c) || isNum(c) || c == '_';
    }
    private static boolean isWhitespace(char c) {
	return c == ' ' || c == '\t' || c == '\n' || c == '\r';
    }
    private static boolean isOp(char c) {
       	return c == '+' || c == '-' || c == '*' || c == '/' || c == '^';
    }

    private int prec(Func op) {
	if(op == F0MINUS) {
	    return prec(FDIV);
	} else {
	    for(int i = 0; i < FUNCS.size(); ++i) {
		if(FUNCS.get(i) == op) {
		    return i;
		}
	    }
	    return 0;
	}
    }

    private static double strtod(String str, int[] p) {
	int i = p[0];
	char c = str.charAt(i);
	if(c == '-' || c == '+') {
	    ++i;
	}
	while(i < str.length() && isNum(str.charAt(i))) {
	    ++i;
	}
	if(i < str.length() - 1) {
	    c = str.charAt(i);
	    char c1 = str.charAt(i + 1);
	    if((c == 'e' || c == 'E')
		    && (c1 == '-' || c1 == '+' || isDigit(c1))) {
		if((c1 == '-' || c1 == '+') && i < str.length() - 2) {
		    char c2 = str.charAt(i + 2);
		    if(isDigit(c2)) {
			i += 2;
			while(i < str.length() && isDigit(str.charAt(i))) {
			    ++i;
			}
		    }
		} else {
		    i += 1;
		    while(i < str.length() && isDigit(str.charAt(i))) {
			++i;
		    }
		}
	    }
	}
	try {
	    double x = Double.valueOf(str.substring(p[0], i)).doubleValue();
	    p[0] = i;
	    return x;
	} catch(NumberFormatException ex) {
	    return 0;
	}
    }

    private void store(String name, Func f) {
	StringTokenizer st = new StringTokenizer(name, "|");
	while(st.hasMoreTokens()) {
	    name = st.nextToken();
	    FUNCS.add(f);
	    FUNCHASH.put(name, f);
	}
    }

    private static String eatWhitespace(String s) {
	StringBuffer sb = new StringBuffer();
	for(int i = 0; i < s.length(); ++i) {
	    char c = s.charAt(i);
	    if(!isWhitespace(c)) {
		sb.append(c);
	    }
	}
	return sb.toString();
    }

    public static void main(String[] args) {
	if(args.length == 0) {
	    System.out.println(
"Usage: calc expression\n\n" + getHelpSI());
	    System.exit(0);
	}
	try {
	    Calculator c = createSI(args[0]);
	    System.out.println("" + c.eval());
	} catch(IllegalArgumentException ex) {
	    System.err.println(ex.getMessage());
	    System.exit(1);
	}
    }
};
