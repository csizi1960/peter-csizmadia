import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class Ns6JsACEWorkaround extends java.applet.Applet implements Runnable
{
    private Vector methodCalls;

    private TextArea txt;

    public void init() {
	setLayout(new GridLayout(1, 1));
	add(txt = new TextArea());
    }

    /**
     * I/O methods are called from a thread created here, which will be
     * independent on the JavaScript thread.
     */
    public void start() {
	methodCalls = new Vector();
	new Thread(this).start();
    }

    public synchronized void run() {
	while(methodCalls != null) {
	    if(methodCalls.size() != 0) {
		doLoading((String)methodCalls.elementAt(0));
		methodCalls.removeElementAt(0);
	    }
	    try {
		wait();
	    } catch(InterruptedException ex) {
	    }
	}
    }

    /** Stop the thread. */
    public synchronized void stop() {
	methodCalls = null;
	notifyAll();
    }

    /** Method to call from JavaScript. */
    public synchronized void load(String s) {
	methodCalls.addElement(s);
	notifyAll();
    }

    /** I/O methods are called from here. */
    private void doLoading(String s) {
	try {
	    URL url = new URL(getDocumentBase(), s);
	    URLConnection conn = url.openConnection();
	    InputStream in = conn.getInputStream();
	    int c;
	    while((c = in.read()) != -1) {
		txt.appendText(String.valueOf((char)c));
	    }
	} catch(Exception ex) {
	    // print exception in the text area
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    PrintStream ps = new PrintStream(baos);
	    ex.printStackTrace(ps);
	    byte[] bytes = baos.toByteArray();
	    StringBuffer sbuf = new StringBuffer();
	    for(int i = 0; i < bytes.length; ++i) {
		sbuf.append(String.valueOf((char)bytes[i]));
	    }
	    txt.appendText(sbuf.toString());
	}
    }
}
