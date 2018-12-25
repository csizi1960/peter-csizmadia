import java.applet.Applet;
import java.awt.GridLayout;
import java.awt.TextArea;

public class WhichJVM extends Applet {
    int contamination_level =
	((System.getProperty("os.name").indexOf("Windows ")==0)? 40:0) +
	(System.getProperty("java.vendor").equals("Microsoft Corp.")? 60:0);
    public void init() {
	TextArea txt = new TextArea();
	setLayout(new GridLayout(1,1));
	add(txt);
	for(int i=0; i<props.length; ++i) {
	    String s;
	    try {
		s = props[i]+'='+System.getProperty(props[i])+'\n';
	    } catch(SecurityException e) {
		s = "cannot get "+props[i]+'\n';
	    }
	    txt.appendText(s);
	}
	txt.appendText("contamination level="+contamination_level+"%\n\n");
	try {
	    Class.forName("java.rmi.Remote");
	    txt.appendText("RMI found");
	} catch(ClassNotFoundException ex) {
	    txt.appendText("no RMI");
	}
	try {
	    Class.forName("javax.swing.JComponent");
	    txt.appendText("; Swing found");
	} catch(ClassNotFoundException ex) {
	    txt.appendText("; no Swing");
	}
    }
    protected final static String props[] = {
	"os.arch", "os.name", "os.version", "os.vendor",
	"browser", "browser.version", "browser.vendor", "browser.vendor.url",
	"java.vendor", "java.vendor.url", "java.version"};
};

/***** Properties in AppletViewer/JDK 1.1.3 ****************
acl.read.default=
acl.read=+
acl.write.default=
acl.write=+
appletloader.bail=Interrupted: bailing out.
appletloader.death=killed
appletloader.destroyed=Applet destroyed.
appletloader.disposed=Applet disposed.
appletloader.error2=error: %0: %1.
appletloader.error=error: %0.
appletloader.exception2=exception: %0: %1.
appletloader.exception=exception: %0.
appletloader.filedeath=%0 killed when loading: %1
appletloader.fileerror=%0 error when loading: %1
appletloader.fileexception=%0 exception when loading: %1
appletloader.fileformat=File format exception when loading: %0
appletloader.fileioexception=I/O exception when loading: %0
appletloader.filenotfound=File not found when looking for: %0
appletloader.inited=Applet initialized.
appletloader.loaded=Applet loaded.
appletloader.nocode=APPLET tag missing CODE parameter.
appletloader.noconstruct=load: %0 is not public or has no public constructor.
appletloader.nocreate=load: %0 can't be instantiated.
appletloader.notdestroyed=Dispose: applet not destroyed.
appletloader.notdisposed=Load: applet not disposed.
appletloader.notfound=load: class %0 not found.
appletloader.notinited=Start: applet not initialized.
appletloader.notloaded=Init: applet not loaded.
appletloader.notstarted=Stop: applet not started.
appletloader.notstopped=Destroy: applet not stopped.
appletloader.started=Applet started.
appletloader.stopped=Applet stopped.
appletviewer.security.allowUnsigned=true
appletviewer.version=1.1
browser.vendor=Sun Microsystems Inc.
browser.version=1.06
browser=sun.applet.AppletViewer
file.encoding.pkg=sun.io
file.encoding=8859_1
file.separator.applet=true
file.separator=/
firewallHost=sunweb.ebay
firewallPort=80
firewallSet=true
hotjava.charset=8859_1
hotjava.version=1.366, 03/31/97
hotlistframe.height=601
hotlistframe.width=300
http.agent=JDK/1.1
java.class.path=.:/usr/lib/jdk-1.1.3/lib/classes.zip:/usr/lib/java/bongo/lib/bongo.zip:/usr/share/guavac/classes.zip:/usr/lib/jdk-1.1.3/classes:/usr/lib/jdk-1.1.3/lib/classes.jar:/usr/lib/jdk-1.1.3/lib/rt.jar:/usr/lib/jdk-1.1.3/lib/i18n.jar:/usr/lib/jdk-1.1.3/lib/classes.zip
java.class.version.applet=true
java.class.version=45.3
java.home=/usr/lib/jdk-1.1.3
java.vendor.applet=true
java.vendor.url.applet=true
java.vendor.url=http://java.blackdown.org/java-linux.html
java.vendor=Sun Microsystems Inc., ported by Randy Chapman and Steve Byrne
java.version.applet=true
java.version=sbb:07/12/97-20:08
line.separator.applet=true
line.separator=

os.arch.applet=true
os.arch=x86
os.name.applet=true
os.name=Linux
os.version.applet=true
os.version=2.0.30
package.restrict.access.netscape=true
package.restrict.access.sun=true
package.restrict.definition.java=true
package.restrict.definition.netscape=true
package.restrict.definition.sun=true
path.separator.applet=true
path.separator=:
user.dir=/home/peter/homepage
user.home=/home/peter
user.language=en
user.name=peter
user.timezone=MET
***********************************************************/

/***********************************************************
os.arch=Pentium
os.name=Windows 95
os.version=4.0
browser=Netscape Navigator
browser.vendor=Netscape Communications Corp.
cannot get browser.vendor.url
browser.version=2.0b4
java.vendor=Netscape Communications Corporation
java.vendor.url=http://home.netscape.com
java.version=1.021

os.arch=mips
os.name=IRIX
os.version=5.3
browser=Netscape Navigator
browser.vendor=Netscape Communications Corporation
cannot get browser.vendor.url
browser.version=3.0
java.vendor=Netscape Communications Corporation
java.vendor.url=http://home.netscape.com
java.version=1.02

os.arch=Pentium
os.name=Windows NT
os.version=4.0
browser=Netscape Navigator
browser.vendor=Netscape Communications Corporation
cannot get browser.vendor.url
browser.version=3.0
java.vendor=Netscape Communications Corporation
java.vendor.url=http://home.netscape.com
java.version=1.02

# MSIE 3.0
os.arch=x86
os.name=Windows NT
os.version=4.0
browser=ActiveX Scripting Host
cannot get browser.vendor
cannot get browser.vendor.url
cannot get browser.version
java.vendor=Microsoft Corp.
java.vendor.url=http://www.microsoft.com/
java.version=1.0.2

os.arch=i486
os.name=Linux
os.version=2.0.30
browser=Netscape Communicator
browser.vendor=Netscape Communications Corporation
cannot get browser.vendor.url
browser.version=4.0
java.vendor=Netscape Communications Corporation
java.vendor.url=http://home.netscape.com/
java.version=1.1.2

# MSIE 5.5
os.arch=x86
os.name=Windows 95
os.version=4.0
cannot get os.vendor
browser=ActiveX Scripting Host
cannot get browser.version
cannot get browser.vendor
cannot get browser.vendor.url
java.vendor=Microsoft Corp.
java.vendor.url=http://www.microsoft.com/
java.version=1.1.4
contamination level=100%

no RMI; no Swing
***********************************************************/
