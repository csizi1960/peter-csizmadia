import java.awt.*;

public class CloseFreezeFix extends java.applet.Applet {
    public void init() {
        add(new Label("Close this window"));
    }
 
    /**
     * Workaround for Netscape 4.5-4.7/MSWINDOWS bug, should be called by
     * Applet.destroy().
     * The browser may freeze at window closing without this.
     */
    private static void recursiveRemoveAll(Container con) {
	Component[] comps = con.getComponents();
	for(int i=0; i<comps.length; ++i) {
	    if(comps[i] instanceof Container)
		recursiveRemoveAll((Container)comps[i]);
	}
	con.removeAll(); // must be at the end of the recursive method
		// otherwise the freezing bug comes back - mysterious
    }

    public void destroy() {
	// A single removeAll() is enough for this applet,
	// but the following also works in the general case:
	recursiveRemoveAll(this);
    }
}
