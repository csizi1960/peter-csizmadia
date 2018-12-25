import netscape.javascript.JSObject;

public class JavaScripter extends java.applet.Applet {
    public void start() {
	JSObject jso = JSObject.getWindow(this);
	jso.eval("alert('hi!')");
    }
}
