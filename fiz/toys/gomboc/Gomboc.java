import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.Random;

/**
 * Simulation of a system of particles with the interaction
 * potential V(r) = cos(3.5*pi*r/(1+r))/r.
 * @version 05/10/2006
 * @since 05/07/2006
 * @author Peter Csizmadia
 */
public class Gomboc extends JApplet {

    // simulation
    private static double DT = 1/256.0;
    private static double XMAX = 14;
    private static double YMAX = 14;
    private int numParticles = 128;
    private int rngSeed = (int)(System.currentTimeMillis() & 0x7fff);
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
    private double[] acceleration;
    private double energy0 = 0;
    private static final double[] POTENTIAL;
    private static final double[] FORCE;
    private static final double FORCE_DR;

    // visualization
    private int particleDensitiesNx = 28;
    private int particleDensitiesNy = 28;
    private double particleDensitiesDx;
    private double particleDensitiesDy;
    private int[] particleDensities0;
    private int[] particleDensities1;
    private int[] particleDensities2;
    private int[] particleDensities3;
    private double[] particleVelocitiesX0;
    private double[] particleVelocitiesY0;
    private double[] particleVelocitiesX1;
    private double[] particleVelocitiesY1;
    private double[] particleVelocitiesX2;
    private double[] particleVelocitiesY2;
    private double[] particleVelocitiesX3;
    private double[] particleVelocitiesY3;
    private int stepsPerUpdate = 1;
    private long delay = 100;
    private JButton stopContinueButton;
    private JButton restartButton;
    private JTextField seedText;
    private Canvas canvas;

    static {
	double dr = 0.0001;
	int n = (int)Math.round(Math.sqrt(XMAX*XMAX + YMAX*YMAX)/dr) + 2;
	POTENTIAL = new double[n];
	FORCE = new double[n];
	FORCE_DR = dr;
	for(int i = 0; i < FORCE.length; ++i) {
	    double r = i*dr;
	    POTENTIAL[i] = potentialExact(r);
	    FORCE[i] = forceExact(r);
	}
    }

    private static double potentialExact(double r) {
	double phi = 3.5*Math.PI*r/(1+r);
	return Math.cos(phi)/r;
    }

    private static double forceExact(double r) {
	double w = 3.5*Math.PI/(1+r);
	double phi = w*r;
	return -(Math.sin(phi)*w/(1+r) + Math.cos(phi)/r)/(r*r);
    }

    private static double potential(double r) {
	double dr = FORCE_DR;
	double x = r/dr;
	double ix = Math.floor(x);
	int i = (int)ix;
	if(i == 0) {
	    return potentialExact(r);
	} else {
	    double d = x - ix;
	    return POTENTIAL[i]*(1-d) + POTENTIAL[i + 1]*d;
	}
    }

    private static double force(double r) {
	double dr = FORCE_DR;
	double x = r/dr;
	double ix = Math.floor(x);
	int i = (int)ix;
	if(i == 0) {
	    return forceExact(r);
	} else {
	    double d = x - ix;
	    double f = FORCE[i]*(1-d) + FORCE[i + 1]*d;
	    return f;
	}
    }

    private void createInitialState() {
	int n = numParticles;
	Random rng = new Random(rngSeed);
	xCoords = new double[4*n];
	xCoords2 = new double[4*n];
	xCoords3 = new double[4*n];
	xCoords4 = new double[4*n];
	rk4k1 = new double[4*n];
	rk4k2 = new double[4*n];
	rk4k3 = new double[4*n];
	rk4k4 = new double[4*n];
	acceleration = new double[2*n*n];
	double[] x = xCoords;
	for(int i = 0; i < n; ++i) {
	    x[i] = -XMAX + 2*XMAX*rng.nextDouble();
	    x[i + n] = -YMAX + 2*YMAX*rng.nextDouble();
	    double vr = rng.nextDouble();
	    double phi = 2*Math.PI*rng.nextDouble();
	    x[i + 2*n] = vr*Math.cos(phi);
	    x[i + 3*n] = vr*Math.sin(phi);
	}
	correctMomentum(); // initial state should have zero total momentum

	stepsPerUpdate = 5;
	stepCount = 0;
	physicalTime = 0;
	energy0 = calcEnergy();

	int Nx = particleDensitiesNx;
	int Ny = particleDensitiesNy;
	particleDensitiesDx = 2*XMAX/Nx;
	particleDensitiesDy = 2*YMAX/Ny;
	particleDensities0 = new int[Nx*Ny];
	particleDensities1 = new int[Nx*Ny];
	particleDensities2 = new int[Nx*Ny];
	particleDensities3 = new int[Nx*Ny];
	particleVelocitiesX0 = new double[Nx*Ny];
	particleVelocitiesY0 = new double[Nx*Ny];
	particleVelocitiesX1 = new double[Nx*Ny];
	particleVelocitiesY1 = new double[Nx*Ny];
	particleVelocitiesX2 = new double[Nx*Ny];
	particleVelocitiesY2 = new double[Nx*Ny];
	particleVelocitiesX3 = new double[Nx*Ny];
	particleVelocitiesY3 = new double[Nx*Ny];
    }

    private double calcEnergy() {
	int n = numParticles;
	double energy = 0;
	double[] x = xCoords;
	for(int i = 0; i < n; ++i) {
	    for(int j = i + 1; j < n; ++j) {
		double xij = x[j] - x[i];
		if(xij > XMAX) {
		    xij -= 2*XMAX;
		} else if(xij < -XMAX) {
		    xij += 2*XMAX;
		}
		double yij = x[j + n] - x[i + n];
		if(yij > YMAX) {
		    yij -= 2*YMAX;
		} else if(yij < -YMAX) {
		    yij += 2*YMAX;
		}
		double r = Math.sqrt(xij*xij + yij*yij);
		energy += potential(r);
	    }
	    double vx = xCoords[i + 2*n];
	    double vy = xCoords[i + 3*n];
	    energy += (vx*vx + vy*vy)/2;
	}
	return energy;
    }

    private void correctMomentum() {
	int n = numParticles;
	double dvx = 0;
	double dvy = 0;
	double sumw = 0;
	for(int i = 0; i < n; ++i) {
	    double vx = xCoords[i + 2*n];
	    double vy = xCoords[i + 3*n];
	    dvx -= vx;
	    dvy -= vy;
	    double w = 1/(vx*vx + vy*vy);
	    sumw += w;
	}
	dvx /= sumw;
	dvy /= sumw;
	for(int i = 0; i < n; ++i) {
	    double vx = xCoords[i + 2*n];
	    double vy = xCoords[i + 3*n];
	    double w = 1/(vx*vx + vy*vy);
	    xCoords[i + 2*n] = vx + w*dvx;
	    xCoords[i + 3*n] = vy + w*dvy;
	}
    }

    private void correctEnergyMomentum() {
	correctMomentum();
	double dE = calcEnergy() - energy0;
	if(dE < 0) {
	    int n = numParticles;
	    double w = -2*dE/n;
	    for(int i = 0; i < n; ++i) {
		double vx = xCoords[i + 2*n];
		double vy = xCoords[i + 3*n];
		double phi = Math.atan2(vy, vx);
		double v = Math.sqrt(vx*vx + vy*vy + w);
		xCoords[i + 2*n] = v*Math.cos(phi);
		xCoords[i + 3*n] = v*Math.sin(phi);
	    }
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
	calcForces(xCoords);
	for(int i = 0; i < n; ++i) {
	    getDx(i, xCoords, dx);
	    k1[i] = dx[0];
	    k1[i + n] = dx[1];
	    k1[i + 2*n] = dx[2];
	    k1[i + 3*n] = dx[3];
	    xCoords2[i] = xCoords[i] + dx[0]*dt/2;
	    xCoords2[i + n] = xCoords[i + n] + dx[1]*dt/2;
	    xCoords2[i + 2*n] = xCoords[i + 2*n] + dx[2]*dt/2;
	    xCoords2[i + 3*n] = xCoords[i + 3*n] + dx[3]*dt/2;
	}
	calcForces(xCoords2);
	for(int i = 0; i < n; ++i) {
	    getDx(i, xCoords2, dx);
	    k2[i] = dx[0];
	    k2[i + n] = dx[1];
	    k2[i + 2*n] = dx[2];
	    k2[i + 3*n] = dx[3];
	    xCoords3[i] = xCoords[i] + dx[0]*dt/2;
	    xCoords3[i + n] = xCoords[i + n] + dx[1]*dt/2;
	    xCoords3[i + 2*n] = xCoords[i + 2*n] + dx[2]*dt/2;
	    xCoords3[i + 3*n] = xCoords[i + 3*n] + dx[3]*dt/2;
	}
	calcForces(xCoords3);
	for(int i = 0; i < n; ++i) {
	    getDx(i, xCoords3, dx);
	    k2[i] = dx[0];
	    k2[i + n] = dx[1];
	    k2[i + 2*n] = dx[2];
	    k2[i + 3*n] = dx[3];
	    xCoords4[i] = xCoords[i] + dx[0]*dt;
	    xCoords4[i + n] = xCoords[i + n] + dx[1]*dt;
	    xCoords4[i + 2*n] = xCoords[i + 2*n] + dx[2]*dt;
	    xCoords4[i + 3*n] = xCoords[i + 3*n] + dx[3]*dt;
	}
	calcForces(xCoords4);
	for(int i = 0; i < n; ++i) {
	    getDx(i, xCoords4, dx);
	    k4[i] = dx[0];
	    k4[i + n] = dx[1];
	    k4[i + 2*n] = dx[2];
	    k4[i + 3*n] = dx[3];
	}
	for(int i = 0; i < 4*n; ++i) {
	    xCoords[i] += (k1[i] + k2[i] + k3[i] + k4[i])*dt/6;
	}
	// periodic boundaries:
	for(int i = 0; i < n; ++i) {
	    double x = xCoords[i];
	    if(x >= XMAX) {
		xCoords[i] = x - 2*XMAX;
	    } else if(x < -XMAX) {
		xCoords[i] = x + 2*XMAX;
	    }
	    double y = xCoords[i + n];
	    if(y >= YMAX) {
		xCoords[i + n] = y - 2*YMAX;
	    } else if(y < -YMAX) {
		xCoords[i + n] = y + 2*YMAX;
	    }
	}
	++stepCount;
	physicalTime += dt;
	if((stepCount % 10) == 0) {
	    // Correct the numerical violation of energy and momentum
	    // conservation in every 10th steps.
	    correctEnergyMomentum();
	}
    }

/*    private void step(double dt) {
	int n = numParticles;
	double[] dx = new double[4];
	calcForces(xCoords);
	for(int i = 0; i < n; ++i) {
	    getDx(i, xCoords, dx);

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

    private void calcForces(double[] x) {
	int n = numParticles;
	double[] acc = acceleration;
	for(int i = 0; i < acc.length; ++i) {
	    acc[i] = 0;
	}
	int k = 0;
	for(int i = 0; i < n; ++i) {
	    k += 2*(i + 1);
	    int k1 = 2*(n*(i + 1) + i);
	    for(int j = i + 1; j < n; ++j) {
		double xij = x[j] - x[i];
		if(xij > XMAX) {
		    xij -= 2*XMAX;
		} else if(xij < -XMAX) {
		    xij += 2*XMAX;
		}
		double yij = x[j + n] - x[i + n];
		if(yij > YMAX) {
		    yij -= 2*YMAX;
		} else if(yij < -YMAX) {
		    yij += 2*YMAX;
		}
		double r = Math.sqrt(xij*xij + yij*yij);
		double a = force(r);
		double ax = a*xij;
		double ay = a*yij;
		acc[k++] = ax;
		acc[k++] = ay;
		acc[k1] = -ax;
		acc[k1 + 1] = -ay;
		k1 += 2*n;
	    }
	}
    }

    private void getDx(int i, double[] x, double[] dx) {
	int n = numParticles;
	double ax = 0;
	double ay = 0;
	int k = 2*n*i;
	for(int j = 0; j < n; ++j) {
	    ax += acceleration[k++];
	    ay += acceleration[k++];
	}
	dx[0] = x[i + 2*n]; // dx/dt = vx
	dx[1] = x[i + 3*n]; // dy/dt = vy
	dx[2] = ax;         // dvx/dt = ax
	dx[3] = ay;         // dvy/dt = ay
    }

    private void calcParticleDensities() {
	if(particleDensities0 == null) {
	    return;
	}
	int Nx = particleDensitiesNx;
	int Ny = particleDensitiesNy;
	int N = Nx*Ny;
	for(int i = 0; i < N; ++i) {
	    particleDensities0[i] = 0;
	    particleDensities1[i] = 0;
	    particleDensities2[i] = 0;
	    particleDensities3[i] = 0;
	    particleVelocitiesX0[i] = 0;
	    particleVelocitiesY0[i] = 0;
	    particleVelocitiesX1[i] = 0;
	    particleVelocitiesY1[i] = 0;
	    particleVelocitiesX2[i] = 0;
	    particleVelocitiesY2[i] = 0;
	    particleVelocitiesX3[i] = 0;
	    particleVelocitiesY3[i] = 0;
	}
	int n = numParticles;
	for(int i = 0; i < n; ++i) {
	    double x = xCoords[i];
	    double y = xCoords[i + n];
	    int kx = (int)Math.floor(2*(XMAX + x)/particleDensitiesDx);
	    int ky = (int)Math.floor(2*(YMAX + y)/particleDensitiesDy);
	    int kx0 = kx/2;
	    int ky0 = ky/2;
	    int kx1 = ((kx + 1)/2) % Nx;
	    int ky1 = ((ky + 1)/2) % Ny;
	    int k0 = ky0*Nx + kx0;
	    int k1 = ky0*Nx + kx1;
	    int k2 = ky1*Nx + kx0;
	    int k3 = ky1*Nx + kx1;
	    particleDensities0[k0]++;
	    particleDensities1[k1]++;
	    particleDensities2[k2]++;
	    particleDensities3[k3]++;
	    double vx = xCoords[i + 2*n];
	    double vy = xCoords[i + 3*n];
	    particleVelocitiesX0[k0] += vx;
	    particleVelocitiesY0[k0] += vy;
	    particleVelocitiesX1[k1] += vx;
	    particleVelocitiesY1[k1] += vy;
	    particleVelocitiesX1[k2] += vx;
	    particleVelocitiesY1[k2] += vy;
	    particleVelocitiesX1[k3] += vx;
	    particleVelocitiesY1[k3] += vy;
	}
	for(int i = 0; i < n; ++i) {
	    double x = xCoords[i];
	    double y = xCoords[i + n];
	    int kx = (int)Math.floor(2*(XMAX + x)/particleDensitiesDx);
	    int ky = (int)Math.floor(2*(YMAX + y)/particleDensitiesDy);
	    int kx0 = kx/2;
	    int ky0 = ky/2;
	    int kx1 = ((kx + 1)/2) % Nx;
	    int ky1 = ((ky + 1)/2) % Ny;
	    int k0 = ky0*Nx + kx0;
	    int k1 = ky0*Nx + kx1;
	    int k2 = ky1*Nx + kx0;
	    int k3 = ky1*Nx + kx1;
	    int n0 = particleDensities0[k0];
	    int n1 = particleDensities0[k1];
	    int n2 = particleDensities0[k2];
	    int n3 = particleDensities0[k3];
	    particleVelocitiesX0[k0] /= n0;
	    particleVelocitiesY0[k0] /= n0;
	    particleVelocitiesX0[k1] /= n1;
	    particleVelocitiesY0[k1] /= n1;
	    particleVelocitiesX0[k2] /= n2;
	    particleVelocitiesY0[k2] /= n2;
	    particleVelocitiesX0[k3] /= n3;
	    particleVelocitiesY0[k3] /= n3;
	}
    }

    private int getParticleDensity(int i) {
	int n = numParticles;
	int Nx = particleDensitiesNx;
	int Ny = particleDensitiesNy;
	double x = xCoords[i];
	double y = xCoords[i + n];
	int kx = (int)Math.floor(2*(XMAX + x)/particleDensitiesDx);
	int ky = (int)Math.floor(2*(YMAX + y)/particleDensitiesDy);
	int kx0 = kx/2;
	int ky0 = ky/2;
	int kx1 = ((kx + 1)/2) % Nx;
	int ky1 = ((ky + 1)/2) % Ny;
	int k0 = ky0*Nx + kx0;
	int k1 = ky0*Nx + kx1;
	int k2 = ky1*Nx + kx0;
	int k3 = ky1*Nx + kx1;
	int n0 = particleDensities0[k0];
	int n1 = particleDensities1[k1];
	int n2 = particleDensities2[k2];
	int n3 = particleDensities3[k3];
	return n0 + n1 + n2 + n3 - 3;
    }

    private double getParticleRelativeFlow(int i) {
	int n = numParticles;
	int Nx = particleDensitiesNx;
	int Ny = particleDensitiesNy;
	double x = xCoords[i];
	double y = xCoords[i + n];
	double vx = xCoords[i + 2*n];
	double vy = xCoords[i + 3*n];
	int kx = (int)Math.floor(2*(XMAX + x)/particleDensitiesDx);
	int ky = (int)Math.floor(2*(YMAX + y)/particleDensitiesDy);
	int kx0 = kx/2;
	int ky0 = ky/2;
	int kx1 = ((kx + 1)/2) % Nx;
	int ky1 = ((ky + 1)/2) % Ny;
	int k0 = ky0*Nx + kx0;
	int k1 = ky0*Nx + kx1;
	int k2 = ky1*Nx + kx0;
	int k3 = ky1*Nx + kx1;
	double vx0 = particleVelocitiesX0[k0] - vx;
	double vy0 = particleVelocitiesY0[k0] - vy;
	double vx1 = particleVelocitiesX1[k1] - vx;
	double vy1 = particleVelocitiesY1[k1] - vy;
	double vx2 = particleVelocitiesX2[k2] - vx;
	double vy2 = particleVelocitiesY2[k2] - vy;
	double vx3 = particleVelocitiesX3[k3] - vx;
	double vy3 = particleVelocitiesY3[k3] - vy;
	double v0 = vx0*vx0 + vy0*vy0;
	double v1 = vx0*vx0 + vy0*vy0;
	double v2 = vx0*vx0 + vy0*vy0;
	double v3 = vx0*vx0 + vy0*vy0;
	return Math.sqrt((v0*v0 + v1*v1 + v2*v2 + v3*v3)/4);
    }

    public void stop() {
	canvas.stop();
    }
    public void destroy() {
	canvas.destroy();
    }

    class Canvas extends JComponent implements Runnable {
	private Thread runner;
	private boolean startIt = true;
	private long firstPaintTime = 0;
	private int screenwidth, screenheight;
	private Image backimg;
	private Graphics2D backg;
	private double scrCenterX = 0;
	private double scrCenterY = 0;
	private double scrMaxX = XMAX;
	private double scrMaxY = YMAX;
	private Point mousePressPoint = null;
	private Rectangle zoomRect = null;
	Canvas() {
	    addMouseListener(new MouseAdapter() {
		public void mouseClicked(MouseEvent ev) {
		    scrCenterX = 0;
		    scrCenterY = 0;
		    scrMaxX = XMAX;
		    scrMaxY = YMAX;
		    if(runner == null) {
			paintState();
		    }
		}
		public void mousePressed(MouseEvent ev) {
		    mousePressPoint = ev.getPoint();
		}
		public void mouseReleased(MouseEvent ev) {
		    Rectangle r = zoomRect;
		    if(r != null) {
			Dimension ss = getSize();
			double c = Math.min(ss.width/(2*scrMaxX),
					ss.height/(2*scrMaxY));
			double x = (r.x - ss.width/2)/c + scrCenterX;
			double y = (ss.height/2 - r.y)/c + scrCenterY;
			double q = (double)r.width/ss.width;
			scrMaxX *= q;
			scrMaxY *= q;
			scrCenterX = x + scrMaxX;
			scrCenterY = y - scrMaxY;
			zoomRect = null;
			if(runner == null) {
			    paintState();
			}
		    }
		}
	    });
	    addMouseMotionListener(new MouseMotionAdapter() {
		public void mouseDragged(MouseEvent ev) {
		    if(mousePressPoint != null) {
			zoomRect = getZoomRect(ev.getPoint());
			if(runner == null) {
			    paintState();
			}
		    }
		}
	    });
	}
	public void paint(Graphics g) {
	    if(startIt) {
		createBackBuffer(getSize());
		if(runner == null) {
		    start();
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
	public void run() {
	    Thread thisthread = Thread.currentThread();
	    if(thisthread == null) {
		return;
	    }
	    callPaintAndWait();
	    long startTime = System.currentTimeMillis();
	    long time = startTime;
	    loop: while(runner == thisthread) {
		callPaintAndWait();
		long t0 = System.currentTimeMillis();
		for(int i = 0; i < stepsPerUpdate; ++i) {
		    step(DT);
		}
		long t1 = System.currentTimeMillis();
		long dt = t1 - t0;
		if(dt == 0) {
		    stepsPerUpdate *= delay/2;
		} else {
		    stepsPerUpdate = Math.max((int)((long)stepsPerUpdate
					      *delay/dt), 1);
		}
		if(stepsPerUpdate > 100) {
		    stepsPerUpdate = 100;
		}
		time += delay;
		try {
		    thisthread.sleep(Math.max(1, time - t1));
		} catch(InterruptedException exc) {
		}
	    }
	    SwingUtilities.invokeLater(new Runnable() {
		public void run() {
		    stopContinueButton.setText("Continue");
		    stopContinueButton.setEnabled(true);
		    restartButton.setEnabled(true);
		    seedText.setEnabled(true);
		}
	    });
	}
	synchronized void callPaintAndWait() {
	    paintState();
	    try {
		wait();
	    } catch(InterruptedException exc) {
	    }
	}
	void start() {
	    (runner = new Thread(this)).start(); // start the thread
	}
	void stop() {
	    runner = null; // stop the thread
	}
	void destroy() {
	    if(backimg != null) {
		backimg.flush();
		backimg = null;
	    }
	}
	void paintState() {
	    if(firstPaintTime == 0) {
		firstPaintTime = System.currentTimeMillis();
	    }
	    Dimension d = getSize();
	    if(d.width != screenwidth || d.height != screenheight) {
		createBackBuffer(d);
	    }
	    backg.setColor(new Color(0x000033));
	    backg.fillRect(0, 0, d.width, d.height);
	    long time = System.currentTimeMillis() - firstPaintTime;
	    if(time < 5000) {
		int opacity = 255 - (int)(256*time/5000);
		backg.setColor(new Color((opacity << 24) | 0x00ffff00, true));
		String s = "Drag the mouse to zoom.";
		backg.setFont(new Font("SansSerif", Font.PLAIN, 18));
		int w = backg.getFontMetrics().stringWidth(s);
		backg.drawString(s, (d.width - w)/2, d.height/2);
	    }
	    backg.setColor(Color.white);
	    backg.setFont(new Font("SansSerif", Font.PLAIN, 14));
	    int th = backg.getFontMetrics().getHeight();
	    backg.drawString(""+stepCount+" steps, "
		+ stepsPerUpdate*1000/delay+"/s", 0, th);
	    backg.setFont(new Font("SansSerif", Font.PLAIN, 12));
	    backg.drawString("t="+physicalTime, 0, 2*th);
	    backg.drawString("E="+calcEnergy(), 0, 3*th);
	    calcParticleDensities();
	    double c = Math.min(d.width/(2*scrMaxX), d.height/(2*scrMaxY));
	    for(int i = 0; i < numParticles; ++i) {
		double x = d.width/2 + c*(xCoords[i] - scrCenterX);
		double y = d.height/2 - c*(xCoords[i + numParticles]
			    - scrCenterY);
		if(x < 0) {
		    x += 2*XMAX*c;
		} else if(x >= d.width) {
		    x -= 2*XMAX*c;
		}
		if(y < 0) {
		    y += 2*YMAX*c;
		} else if(y >= d.height) {
		    y -= 2*YMAX*c;
		}
		if(x >= 0 && y >= 0 && x < d.width && y < d.height) {
		    double r = c/10;
		    int density = getParticleDensity(i);
		    double flow = getParticleRelativeFlow(i);
		    double red = flow/(3+flow);
		    double green = (double)density/(1+density)
					    *(1 - flow/(3+flow));
		    backg.setColor(new Color((int)(256*red), (int)(256*green),
				0x80));
		    backg.fill(new Ellipse2D.Double(x-r, y-r, 2*r, 2*r));
		}
	    }
	    if(zoomRect != null) {
		backg.setColor(Color.white);
		backg.drawRect(zoomRect.x, zoomRect.y,
			zoomRect.width, zoomRect.height);
	    }
	    repaint();
	}
	void createBackBuffer(Dimension d) {
	    if(backimg != null) {
		backimg.flush();
	    }
	    backimg = createImage(d.width, d.height);
	    backg = (Graphics2D)backimg.getGraphics();
	    backg.setColor(new Color(0x000033));
	    backg.fillRect(0, 0, d.width, d.height);
	    backg.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
		    RenderingHints.VALUE_ANTIALIAS_ON);
	    screenwidth = d.width;
	    screenheight = d.height;
	}
	Rectangle getZoomRect(Point p) {
	    Dimension ss = getSize();
	    int dx = p.x - mousePressPoint.x;
	    int dy = p.y - mousePressPoint.y;
	    double qx = (double)Math.abs(dx)/ss.width;
	    double qy = (double)Math.abs(dy)/ss.height;
	    int x1, y1, x2, y2;
	    if(qx > qy) {
		double q = dy > 0? qx : -qx;
		int py = (int)Math.round(mousePressPoint.y + q*ss.height);
		x1 = Math.min(mousePressPoint.x, p.x);
		x2 = Math.max(mousePressPoint.x, p.x);
		y1 = Math.min(mousePressPoint.y, py);
		y2 = Math.max(mousePressPoint.y, py);
	    } else {
		double q = dx > 0? qy : -qy;
		int px = (int)Math.round(mousePressPoint.x + q*ss.width);
		x1 = Math.min(mousePressPoint.x, px);
		x2 = Math.max(mousePressPoint.x, px);
		y1 = Math.min(mousePressPoint.y, p.y);
		y2 = Math.max(mousePressPoint.y, p.y);
	    }
	    return new Rectangle(x1, y1, x2 - x1, y2 - y1);
	}
    }

    private void restartSimulation() {
	try {
	    rngSeed = Integer.parseInt(seedText.getText());
	} catch(NumberFormatException ex) {
	    JOptionPane.showMessageDialog(this,
		    "Random number generator's seed number must be integer.",
		    "Bad seed number", JOptionPane.ERROR_MESSAGE);
	    return;
	}
	stopContinueButton.setText("Stop");
	restartButton.setEnabled(false);
	seedText.setEnabled(false);
	createInitialState();
	canvas.start();
    }

    public Gomboc() {
	createInitialState();

	Container p = getContentPane();
	GridBagLayout gbl = new GridBagLayout();
	p.setLayout(gbl);
	GridBagConstraints gbc = new GridBagConstraints();
	gbc.gridx = 0;
	gbc.gridy = 0;
	gbc.weightx = 0;
	gbc.weighty = 0;
	gbc.fill = GridBagConstraints.NONE;
	gbc.anchor = GridBagConstraints.NORTHWEST;
	gbl.setConstraints(p.add(stopContinueButton
				 = new JButton("Stop")), gbc);
	stopContinueButton.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent ev) {
		if(canvas.runner != null) {
		    stopContinueButton.setEnabled(false);
		    canvas.stop();
		} else {
		    stopContinueButton.setText("Stop");
		    restartButton.setEnabled(false);
		    seedText.setEnabled(false);
		    canvas.start();
		}
	    }
	});
	gbc.gridx++;
	gbc.fill = GridBagConstraints.VERTICAL;
	gbl.setConstraints(p.add(new JLabel("      seed=", JLabel.RIGHT)), gbc);
	gbc.gridx++;
	gbl.setConstraints(p.add(seedText = new JTextField(4)), gbc);
	seedText.setText(String.valueOf(rngSeed));
	seedText.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent ev) {
		restartSimulation();
	    }
	});
	seedText.setEnabled(false);
	gbc.gridx++;
	gbc.weightx = 1;
	gbc.fill = GridBagConstraints.NONE;
	gbl.setConstraints(p.add(restartButton = new JButton("Restart")), gbc);
	restartButton.setEnabled(false);
	restartButton.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent ev) {
		restartSimulation();
	    }
	});
	gbc.gridx = 0;
	gbc.gridy++;
	gbc.gridwidth = 4;
	gbc.weighty = 1;
	gbc.fill = GridBagConstraints.BOTH;
	gbl.setConstraints(p.add(canvas = new Canvas()), gbc);
    }
    public static void main(String[] args) {
	final JFrame f = new JFrame();
	f.setTitle("G\u00f6mb\u00f6c\u00f6k");
	final JApplet applet = new Gomboc();
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
