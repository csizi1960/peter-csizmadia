public class Fine {
    static final double[] VALUES = {137.0359991};

    static double calc(int M, int[] c) {
	double v = 0;
	for(int i = 1; i < c.length; ++i) {
	    double factor = c[i] < 0? -Math.pow(-c[i], 1.0/M)
				    : Math.pow(c[i], 1.0/M);
	    v += factor*Math.pow(Math.PI, (double)i/M);
	}
	return v;
    }

    static String formatFactor(int M, int c) {
	if(M == 2) {
	    if(c > 0) {
		int x = (int)Math.sqrt(c);
		return x*x == c? String.format("+%d", x)
			       : String.format("+%d^0.5", c);
	    } else {
		int x = (int)Math.sqrt(-c);
		return x*x == -c? String.format("-%d", x)
				: String.format("-%d^0.5", -c);
	    }
	} else {
	    return String.format("%+d", c);
	}
    }

    static String formatExp(int M, int i) {
	if(i == M) {
	    return "";
	} else if((i/M)*M == i) {
	    return String.format("^%d", i/M);
	} else {
	    return String.format("^(%d/%d)", i, M);
	}
    }

    static String format(int M, int c, int i) {
	if(c == 0) {
	    return "";
	} else if(i == 0) {
	    return String.format("%+d", c);
	} else {
	    String exp = formatExp(M, i);
	    return c == 1? String.format("+pi%s", exp) :
		   c == -1? String.format("-pi%s", exp)
			  : String.format("%s*pi%s", formatFactor(M, c), exp);
	}
    }

    static String format(int M, int[] c) {
	StringBuffer sb = new StringBuffer();
	for(int i = c.length - 1; i >= 0; --i) {
	    String s = format(M, c[i], i);
	    sb.append(s);
	}
	String s = sb.toString();
	if(s.startsWith("+")) {
	    s = s.substring(1);
	}
	return s;
    }

    static void check(int M, int[] c, double err) {
	double v = calc(M, c);
	for(int i = 0; i < VALUES.length; ++i) {
	    if(Math.abs(VALUES[i] - v) < err) {
		System.out.printf("%.1e   %s = %.9f\n",
				  Math.abs(VALUES[i] - v), format(M, c), v);
	    }
	}
    }

    static void calc(int M, int[] c, int off, int max, double err) {
	for(int i = -max; i <= max; ++i) {
	    c[off] = i;
	    if(off == c.length - 1) {
		check(M, c, err);
	    } else {
		calc(M, c, off + 1, max, err);
	    }
	}
    }

    public static void main(String[] args) throws Exception {
	int[] c = new int[6];
//	calc(1, c, 0, 10, 4e-4);
//	System.err.println("-------");
	c = new int[8];
	calc(2, c, 1, 16, 1e-6);
    }
}
