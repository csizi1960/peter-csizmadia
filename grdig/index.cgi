#!/usr/bin/perl

use CGI qw(param);

$TIMEOUT = 300; # 5 minutes
$IMGOFF = 64;
$demo = param('demo');
$img = param('img');
$buf = 0;
$offset = $IMGOFF;

while(($len=read($img,$buf,1024,$offset)) > 0) {
	$offset += $len;
}
$len = $offset;
$time = time;
$head = pack("II", $len, $time);
substr($buf, 0, length($head)) = $head;

$shmid = shmget(IPC_PRIVATE, $len, IPC_CREAT|0644);

if(($pid = fork) == 0) { # child process
	my $tmp;

	# Dissociate from parent process.
	close(STDIN);
	close(STDOUT);
	close(STDERR);

	# write GIF to shared memory
	shmwrite($shmid, $buf, 0, $len);

	# client has $TIMEOUT seconds to download GIF
	sleep($TIMEOUT);

	# delete shared memory
	if(shmread($shmid, $tmp, 0, length($head))) {
		if($tmp == $head) {
			shmctl($shmid, IPC_RMID, 0);
		}
	}
	exit(0);
}

print <<EOF;
Content-type: text/html

<html>
<head>
<meta NAME="description" CONTENT="Graphics tool for reading coordinates of data points from linear and logscaled graphs. A Java application/applet.">
<meta NAME="keywords" CONTENT="graph, read data points, coordinates, Java, applet, Graph Redigitizer, grdig">
<title>
EOF

if($img) {
	printf("Graph Redigitizer - $img\n");
} elsif($demo) {
	printf("Graph Redigitizer - $demo\n");
} else {
	printf("Graph Redigitizer\n");
}

print <<EOF;
</title>
</head>
<body BGCOLOR="#e0e0e0" TEXT="#224400" LINK="#0000FF" VLINK="#9E0061" ALINK="#FFFF00">
<font FACE="Helvetica, Arial">
Java 1.1 compatible browser (such as Netscape 4.06 or later) required
for this demo.
<p>
EOF
if(!$img && !$demo) {
	printf("<h1 ALIGN=CENTER>Graph Redigitizer</h1>\n");
}
print <<EOF;
<form NAME="fileform" ENCTYPE="multipart/form-data" ACTION="index.cgi" METHOD="post">
<input TYPE="HIDDEN" NAME="width" VALUE="576">
<input TYPE="HIDDEN" NAME="height" VALUE="500">
<input TYPE="HIDDEN" NAME="sort" VALUE="1">
<table BORDER=0 CELLSPACING=10 CELLPADDING=0>
<tr>
<td><font FACE="Helvetica, Arial"><input TYPE="FILE" NAME="img" VALUE="/home/cspeter/PhD/NA49/qm96_pi.gif"></font></td>
<td><font FACE="Helvetica, Arial"><input TYPE="SUBMIT" VALUE="Load GIF"></font></td>
EOF
if($img || $demo) {
	print <<EOF;
<td><font FACE="Helvetica, Arial"><a HREF="./">Grdig home</a></font></td>
EOF
}
print <<EOF;
</tr>
</table>
</form>
EOF

if($img || $demo) {
	printf("<center>\n");
	if($len <= $IMGOFF && !$demo) {
		printf("<strong>file $img not found</strong>\n");
	} elsif($len < $IMGOFF+10 && !$demo) {
		printf("<strong>file $img corrupted</strong>\n");
	} else {
		my $width;
		my $height;
		my $imgurl;
		$width = param('width');
		$height = param('height');
		$p0 = param('p0');
		$q0 = param('q0');
		$p1 = param('p1');
		$q1 = param('q1');
		$p2 = param('p2');
		$q2 = param('q2');
		$x0 = param('x0');
		$y0 = param('y0');
		$x1 = param('x1');
		$y2 = param('y2');
		if(!length($logx = param('logx'))) {
			$logx = 0;
		}
		if(!length($logy = param('logy'))) {
			$logy = 0;
		}
		if(!length($sort = param('sort'))) {
			$sort = 0;
		}
		if($demo) {
			$imgurl = $demo;
			if(!length($p0)) {
				$p0 = 10;
			}
			if(!length($q0)) {
				$q0 = 90;
			}
			if(!length($p1)) {
				$p1 = 100;
			}
			if(!length($q1)) {
				$q1 = 90;
			}
			if(!length($p2)) {
				$p2 = 10;
			}
			if(!length($q2)) {
				$q2 = 10;
			}
		} else {
			my $w0 = ord(substr($buf, $IMGOFF+6, 1));
			my $w1 = ord(substr($buf, $IMGOFF+7, 1));
			my $h0 = ord(substr($buf, $IMGOFF+8, 1));
			my $h1 = ord(substr($buf, $IMGOFF+9, 1));
			$w = $w0+256*$w1;
			$h = $h0+256*$h1;
			$imgurl = "gif.cgi?shmid=$shmid&len=$len&time=$time";

			if(!length($p0)) {
				$p0 = $w/5;
			}
			if(!length($q0)) {
				$q0 = $h*4/5;
			}
			if(!length($p1)) {
				$p1 = $w*4/5;
			}
			if(!length($q1)) {
				$q1 = $h*4/5;
			}
			if(!length($p2)) {
				$p2 = $w/5;
			}
			if(!length($q2)) {
				$q2 = $h/4;
			}
		}
		if(!length($x0) && !length($x1)) {
			if($logx) {
				$x0 = 1;
				$x1 = 10;
			} else {
				$x0 = 1;
				$x1 = 0;
			}
		}
		if(!length($y0) && !length($y1)) {
			if($logy) {
				$y0 = 1;
				$y2 = 10;
			} else {
				$y0 = 0;
				$y2 = 1;
			}
		}
		print <<EOF;
<applet ARCHIVE="grdig.jar" CODE="GRApplet" WIDTH=576 HEIGHT=500>
<param NAME="img" VALUE="$imgurl">
<param NAME="bgcolor" VALUE="#e0e0e0">
<param NAME="logx" VALUE="$logx">
<param NAME="logy" VALUE="$logy">
<param NAME="sort" VALUE="$sort">
<param NAME="p0" VALUE="$p0">
<param NAME="q0" VALUE="$q0">
<param NAME="p1" VALUE="$p1">
<param NAME="q1" VALUE="$q1">
<param NAME="p2" VALUE="$p2">
<param NAME="q2" VALUE="$q2">
<param NAME="x0" VALUE="$x0">
<param NAME="y0" VALUE="$y0">
<param NAME="x1" VALUE="$x1">
<param NAME="y2" VALUE="$y2">
<strong>(YOU CANNOT SEE A JAVA APPLET HERE)</strong><br>
<script LANGUAGE="JavaScript1.1">
<!--
function wropt(s) {
	var a = s.split("/");
	document.write("<font COLOR=red>");
	for(var i=0; i<a.length; ++i) {
		document.write(a[i]);
		if(i<a.length-1) {
			document.write("<font COLOR=blue>/</font>");
		}
	}
	document.write("</font>");
}
if(navigator.appName=="Netscape") {
	var v = navigator.appVersion.substring(0,2);
	if(!navigator.javaEnabled()) {
		document.writeln("ERROR: Java is disabled in your browser.<br>");
		if(v=="3.") {
			document.write("To enable it, check ");
			wropt("Options/Network Preferences/Languages/Enable Java");
			document.writeln("<br>");
		} else if(v=="4.") {
			document.write("To enable it, check ");
			wropt("Edit/Preferences/Advanced/Enable Java");
			document.writeln("<br>");
		} else {
			document.write("You should enable it in the options to see this applet.<br>");
		}
	} else {
		document.writeln("ERROR: Java does not work in your browser.<br>");
		document.writeln("Consult the system administrator.<br>");
	}
}
//-->
</script>
</applet>
EOF
	}
	printf("</center>\n");
} else {
	print <<EOF;
<h2>News</h2>
<ul>
<li><i>March 17, 2004.</i> Grdig 0.6:
    errorbars, Swing GUI</li>
</ul>

<h2>Introduction</h2>
This program helps you to read the coordinates of data points from a
linear or logscaled graph. The axes of the graph can be non-horizontal,
non-vertical, non-perpendicular. The input is a GIF file, the output is a
two-column table containing the coordinates marked by you.<br>
Especially useful if you are a theoretical particle physicist and you need
the experimental data which the experimental physicists publicized only
in some graphical form (as always).
<p>
This is <em>free</em> software,
see the <a HREF="COPYING.txt">GPL</a> for details.

<h2>Usage</h2>
<ol>
<li>Make a GIF file from your graph.<font COLOR=red><b>*</b></font></li>
<li><b>Load</b> the GIF into the applet (application).</li>
<li>Choose three <b>calibration points</b>
	(<i>1.</i> origin, <i>2.</i> a point on the X axis,
	<i>3.</i> a point on the Y axis).
    </li>
<li>Set the real X, Y coordinates of the calibration points.</li>
<li><b>Mark</b> the data points.</li>
<li>Click the <b>Show</b> button to see the coordinates.</li>
<li>To save the data table: copy it into a text editor with the mouse
    (in the applet)<font COLOR=red><b>**</b></font> or select File/Save
    (in the application)</li>
</ol>

If you are a beginner, skip the first three steps and
<a HREF="index.cgi?demo=demo.gif&logx=0&logy=1&sort=1&x0=0.0&y0=40&x1=0.8&y2=300&p0=56.0&q0=393.0&p1=348.0&q1=393.0&p2=56.0&q2=116.0">try the demo</a>.

<p>

The program can be used not just as an applet but also as an application.
Download it if you want to use it on your own
machine.

<p>

<font COLOR=red><b>*</b></font>
<small>If the graph is in a Postscript file, view it with
<b>ghostview</b> or <b>gv</b> (or any Postscript viewer you like),
grab the graph with <b>xv</b>, and then save the grabbed image in
 GIF format (from xv).</small>

<p>

<font COLOR=red><b>**</b></font>
<small>The applet has no File menu because applets from the web are not
allowed to access files on your winchester (except the signed ones, but sign
is money).<br>
</small>

<h2><a NAME="download">Downloading and Installation</a></h2>

To use Graph Redigitizer as an application, you need
<a HREF="http://java.sun.com/jdk/">Java</a> 1.3 or later.

<ol>
<li>Download <a HREF="grdig-0.6.tar.gz">grdig-0.6.tar.gz</a>.
    </li>
<li>Extract its contents in your home directory: do<br>
    <center><tt>gunzip < grdig-0.6.tar.gz | tar xvf -</tt></center>
    in unix, or the equivalent in windows.
<li>Edit the grdig/bin/grdig shell script (in unix) or the
    grdig\\dos\\grdig.bat batch file (in windows)
    and copy it into a directory which is in your <tt>PATH</tt>.</li>
<li>Now you can run the program by typing
    <tt>grdig</tt> or <tt>grdig <i>file.gif</i></tt></li>
</ol>

The program can also be downloaded as an RPM package (for RedHat-based
linuxes):
<ul>
<li><a HREF="grdig-0.6-0.noarch.rpm">grdig-0.6-0.noarch.rpm</a>
    (binaries)</li>
<li><a HREF="grdig-0.6-0.src.rpm">grdig-0.6-0.src.rpm</a>
    (source)</li>
</ul>

<p>
EOF
}

print <<EOF;
<hr WIDTH="90%">
<p ALIGN=center><font SIZE=-2>
&copy; 1998-2000 <a HREF="http://www.kfki.hu/~cspeter/">Peter Csizmadia</a>
</font></p>
</font>
</body>
</html>
EOF
