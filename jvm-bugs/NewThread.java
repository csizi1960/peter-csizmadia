import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class NewThread extends java.applet.Applet implements Runnable
{
    TextArea text = new TextArea();
    public void init() {
	setLayout(new GridLayout(1, 1));
	add(text);
    }
    public void start() {
	System.out.println("applet started");
	text.append("applet started\n");
	Thread thread;
	try {
	    thread = new Thread(this);
	} catch(Throwable ex) {
	    ex.printStackTrace();
	    StringWriter w = new StringWriter();
	    ex.printStackTrace(new PrintWriter(w));
	    text.append("ERROR: cannot create thread\n"+w.toString());
	    return;
	}
	thread.start();
    }
    public void run() {
	System.out.println("thread running");
	text.append("thread running\n");
    }
}
