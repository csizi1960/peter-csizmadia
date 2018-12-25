import java.awt.*;

public class CloseFreezeSleepFix extends java.applet.Applet {
    public void init() {
        add(new Label("Close this window"));
    }
 
    public void destroy() {
	if(System.getProperty("java.version").equals("1.1.5")) {
	    // Dan Kok's workaround for Netscape 4.5-4.7/MSWINDOWS freezing bug
	    try {
		Thread.sleep(2000);
	    } catch(InterruptedException ex) {
	    }
	}
    }
}
