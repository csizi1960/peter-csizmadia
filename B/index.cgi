#!/usr/bin/perl
use CGI qw(param);
use Time::Local;

$host=$ENV{"HTTP_HOST"};

my $nyelv = param("nyelv");
if(!$nyelv) {
	$nyelv = (index($ENV{"HTTP_USER_AGENT"}, "[hu]") >= 0)? "hu" : "en";
}
my $d1, $d2, $d3, $mday_now, $mon_now, $year_now;
my $time_now = time();
($d1,$d2,$d3,$mday_now,$mon_now,$year_now) = localtime($time_now);
$year_now += 1900;

sub myage {
	my $szdate = timegm(59,59,23,9,10-1,$year_now);
	my $elozoszdate = timegm(0,0,0,9,10-1,$year_now);
	my $kor = $year_now-1972;
	if($time_now < $szdate) {
		$elozoszdate = timegm(0,0,0,9,10-1,$year_now-1);
		$kor -= 1;
	}
	$kor += ($time_now-$elozoszdate)/(60*60*24*365);
	return ""+$kor;
}

sub happyday {
	my $s = $1;
	my $subj = $2;
	printf('<p ALIGN=CENTER><font SIZE="+2"><b>\n');
	printf("%s\n", $s);
#	if($nyelv eq "hu") {
#		printf('<a HREF="mailto:Csizmadia Peter ');
#	} else {
#		printf('<a HREF="mailto:Peter Csizmadia ');
#	}
#	printf('<cspeter at rmki.kfki.hu>?subject=');
#	printf('%s">%s</a></b></font></p>\n', $subj, $s);
}

print <<EOF;
Content-Type: text/html

<html>
<head>
<meta HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=iso-8859-1">
<title>Csizmadia P&eacute;ter</title>

<script LANGUAGE="JavaScript">
<!--
jsver="1.0";
function hamagyarvagy() {
	var x = confirm('...és ha írni is akarsz nekem');
	return x;
}
//-->
</script>
<script LANGUAGE="JavaScript1.1">
<!--
jsver="1.1";
slopefitON = new Image();
slopefitON.src = "img/slopefit-mo.gif";
Moffimg = new Image();
//-->
</script>
<script LANGUAGE="JavaScript">
<!--
function Mon(img) {
	if(jsver!="1.0") {
		if(img) {
			Moffimg.src = eval("document."+img.name+".src")
			img.src = eval(img.name+"ON.src")
		}
	}
}
function Moff(img) {
	if(jsver!="1.0") {
		if(img) {
			img.src = Moffimg.src
		}
	}
}
//-->
</script>
</head>

<body BGCOLOR="#202040" TEXT="#c0c0ff" LINK="#8080ff" VLINK="#7820d8" ALINK="#c0c0ff">
EOF

if($mon_now==10-1 && $mday_now==9) {
	if(nyelv eq "hu") {
		happyday("Boldog sz&uuml;let&eacute;snapomat!", "Boldog szuletesnapot!");
	} else {
		happyday("Happy Birthday to Me!", "Happy Birthday!");
	}
}
if($mon_now==7-1 && $mday_now==29) {
	if(nyelv eq "hu") {
		happyday("Boldog P&eacute;ternapot!", "Boldog nevnapot!");
	} else {
		happyday("Happy Peter's day!", "Happy Peter's day!");
	}
}

printf('<p ALIGN=RIGHT><font FACE="Arial, Helvetica">');
if($nyelv eq "hu") {
	printf('<a HREF="index.cgi?nyelv=en"><strong>Mostly English version</strong></a>');
} else {
	printf('<a HREF="index.cgi?nyelv=hu"><strong>Magyarabb v&aacute;ltozat</strong></a>');
}
printf("</font></p>\n");
printf('<p><font SIZE=-1 FACE="Arial, Helvetica">');
if($nyelv eq "hu") {
	printf("<font COLOR='#ffffff'><b>NE HAGYD KI!</b></font> A l&eacute;tez&eacute;s <a HREF='letezes.html' onMouseOver='location=href'>");
	printf("%s", myage());
	printf("</a> &eacute;vvel ezel&otilde;tt kezd&otilde;d&ouml;tt.\n");
	printf("</font><p><hr WIDTH='75%'><font FACE='Arial, Helvetica' SIZE=-1>\n");
#	printf("<i>2001. m&aacute;rcius 17.</i>\n");
#	printf("<a HREF='gombvillam.html'>Ism&eacute;t l&aacute;ttam g&ouml;mbvill&aacute;mot.</a>\n");
} else {
	printf("<font COLOR='#ffffff'><b>DON'T MISS IT!</b></font> Existence began <a HREF='existence.html' onMouseOver='location=href'>");
	printf("%s", myage());
	printf("</a> years ago.\n");
	printf("</font><p><hr WIDTH='75%'><font FACE='Arial, Helvetica' SIZE=-1>\n");
#	printf("<i>March 17, 2001.</i>\n");
#	printf("Another ball lightning...\n");
#	printf("<a HREF='gombvillam.html'><small>(Ism&eacute;t l&aacute;ttam g&ouml;mbvill&aacute;mot.)</small></a>\n");
}
printf("</font></p>\n");

print <<EOF;
<center>
<table BORDER=0 CELLPADDING=0 CELLSPACING=0>
<tr>

<td VALIGN=TOP>
<table BORDER=0 CELLPADDING=0 CELLSPACING=5><!-- Physics -->
<tr><th ALIGN=CENTER><font FACE="Times" SIZE=3><font SIZE=5>P</font>HYSICS</font></th>
    </tr>
<tr><td NOWRAP><font FACE="Arial, Helvetica"><a HREF="../fiz/cv.html">CV &amp; publication list</a></font></td></tr>
<tr><td NOWRAP><font FACE="Arial, Helvetica"><a HREF="../fiz/phd/">PhD thesis</a></font></td></tr>
<tr><td NOWRAP><font FACE="Arial, Helvetica">Research</font></td></tr>
<tr><td NOWRAP><ul>
	<li><font FACE="Arial, Helvetica"><a HREF="micor/">Quark matter hadronization</a></font></li>
	</ul></td></tr>
<tr><td NOWRAP><font FACE="Arial, Helvetica">Tools</font></td></tr>
<tr><td NOWRAP><ul>
	<li><font FACE="Arial, Helvetica"><a HREF="../grdig/">Graph Redigitizer</a></font></li>
	<li><font FACE="Arial, Helvetica"><a HREF="../slopefit/">Slope Parametric Fitting</a></font></li>
	</ul></td></tr>
</table><!-- Physics -->
</td>

<td ROWSPAN=3 VALIGN=TOP>
<table>
<tr><td ALIGN=RIGHT COLSPAN=2 NOWRAP><b><font FACE="Times" SIZE=5>I <font SIZE=3>AM</font> </font></b></td>
    <td ALIGN=CENTER><b><font FACE="Times" SIZE=5>P</font></b></td></tr>
<tr><td ALIGN=CENTER></td><td></td>
    <td ALIGN=CENTER><font FACE="Times" SIZE=3><b>&Eacute;</b></font></td></tr>
<tr><td ALIGN=CENTER><font FACE="Times" SIZE=3><b>M</b></font></td><td></td>
    <td ALIGN=CENTER><font FACE="Times" SIZE=3><b>T</b></font></td></tr>
<tr><td ALIGN=CENTER><font FACE="Times" SIZE=3><b>E</b></font></td><td></td>
    <td ALIGN=CENTER><font FACE="Times" SIZE=3><b>E</b></font></td></tr>
<tr><td ALIGN=CENTER><font FACE="Times" SIZE=3><b>S</b></font></td><td></td>
    <td ALIGN=CENTER><font FACE="Times" SIZE=3><b>R</b></font></td></tr>
<tr><td ALIGN=CENTER><font FACE="Times" SIZE=3><b>Z</b></font></td><td></td>
    <td ALIGN=CENTER></td></tr>
<tr><td ALIGN=CENTER><font FACE="Times" SIZE=3><b>D</b></font></td><td></td>
    <td ALIGN=CENTER><font FACE="Times" SIZE=5><b>C</b></font></td></tr>
<tr><td ALIGN=CENTER><font FACE="Times" SIZE=3><b>&Eacute;</b></font></td><td></td>
    <td ALIGN=CENTER><font FACE="Times" SIZE=3><b>S</b></font></td></tr>
<tr><td ALIGN=CENTER><font FACE="Times" SIZE=3><b>P</b></font></td><td></td>
    <td ALIGN=CENTER><font FACE="Times" SIZE=3><b>I</b></font></td></tr>
<tr><td ALIGN=CENTER><font FACE="Times" SIZE=3><b>M</b></font></td><td></td>
    <td ALIGN=CENTER><font FACE="Times" SIZE=3><b>Z</b></font></td></tr>
<tr><td ALIGN=CENTER><font FACE="Times" SIZE=3><b>&Oacute;</b></font></td><td></td>
    <td ALIGN=CENTER><font FACE="Times" SIZE=3><b>M</b></font></td></tr>
<tr><td ALIGN=CENTER><font FACE="Times" SIZE=5><b>H</b></font></td><td></td>
    <td ALIGN=CENTER><font FACE="Times" SIZE=3><b>A</b></font></td></tr>
<tr><td ALIGN=CENTER></td><td></td>
    <td ALIGN=CENTER><font FACE="Times" SIZE=3><b>D</b></font></td></tr>
<tr><td ALIGN=CENTER><font FACE="Times" SIZE=3><b>N</b></font></td><td></td>
    <td ALIGN=CENTER><font FACE="Times" SIZE=3><b>I</b></font></td></tr>
<tr><td ALIGN=CENTER><font FACE="Times" SIZE=5><b>&Eacute;</b></font></td><td></td>
    <td ALIGN=CENTER><font FACE="Times" SIZE=3><b>A</b></font></td></tr>
<tr><td ALIGN=CENTER COLSPAN=3 NOWRAP><font FACE="Times" SIZE=3><b>ZA Z<font SIZE=5>E</font> S&Eacute;</b></font></td></tr>
</table>
</td>

<td VALIGN=TOP>
<table BORDER=0 CELLPADDING=0 CELLSPACING=5><!-- Software -->
<tr><th COLSPAN=3 ALIGN=CENTER><font FACE="Times" SIZE=3><font SIZE=5>S</font>OFTWARE</font></th>
    </tr>
<tr>
<td><a HREF="../slopefit/" onMouseOver="Mon(document.slopefit)" onMouseOut="Moff(slopefit)"><img SRC="img/slopefit.gif" ALT="Slope Parametric Fitting" NAME="slopefit" WIDTH=77 HEIGHT=44 BORDER=0></a></td>
</tr>
<tr><td COLSPAN=3><font FACE="Helvetica, Arial"><a HREF="java.html">Java</a>
	- <font SIZE=-2>Marvin, MAR, Graph Redigitizer, Bug
	Collection</font></font></td></tr>
<tr><td COLSPAN=3><font FACE="Helvetica, Arial"><a HREF="../util/">Scripts,
    small utilities</a>
	- <font SIZE=-2>
	  intelligent command line completion for zsh,
	  half-automatic ftp,
	  remote directory synchronization with ssh.
	</font></font></td></tr>
</table><!-- Software -->
</td>
</tr>

<tr><td></td><td></td></tr>

<tr>
<td VALIGN=BOTTOM>
<table BORDER=0 CELLPADDING=0 CELLSPACING=5><!-- Other stuff -->
<tr><th COLSPAN=3 ALIGN=CENTER><font FACE="Times"><font SIZE=5>F</font>ELH&Otilde;K, NAPLEMENT&Eacute;K, ZOMBIK</font></th>
    </tr>
<tr><td><font FACE="Arial, Helvetica"><a HREF="../kepek/">Felh&otilde;k</a>
	<font SIZE=-2>feletti &eacute;s alatti k&eacute;pek</font>
	</font>
	</td>
    <td ROWSPAN=2 VALIGN=MIDDLE><a HREF="../kepek/"><img SRC="img/alienugrik.jpg" WIDTH=50 HEIGHT=77 BORDER=0 ALT="K&eacute;pek"></a></td>
    <td ROWSPAN=2 VALIGN=MIDDLE><a HREF="lohalal/"><img SRC="img/lohalal.gif" WIDTH=80 HEIGHT=48 BORDER=0 ALT="L&oacute;hal&aacute;l"></a></td>
    </tr>
<tr><td><font FACE="Arial, Helvetica"><a HREF="lohalal/">L&oacute;hal&aacute;l</a>
	- <font SIZE=-2>Bizakodva divatoznak
	a sell&otilde;l&aacute;nyok.
	Civakodva viharoznak a libasorok.
	De iszonyodva kalimp&aacute;lnak az uszonyosok...</font>
	</font>
	</td>
	</tr>
</table><!-- Other stuff -->
</td>

<td>
<hr WIDTH="75%">
<p ALIGN=CENTER><font SIZE=-2 FACE="Arial, Helvetica">
email: <img SRC="../img/cspeter-email-lightblue-tiny.gif" WIDTH=78 HEIGHT=8>
<br>
<br>
<a HREF="pubkey.txt">My PGP public key</a><br>
<br>
phone \@ kfki: (+36)-1-3922222 / 1613
</font>
</p>
</td>
</tr>
</table>
</center>

<p ALIGN=CENTER>
<applet CODE="MagamonKivuliLinkek" WIDTH=320 HEIGHT=120>
<param NAME="bgcolor" VALUE="#202040">
<param NAME="linkcolor" VALUE="#8080ff">
<param NAME="alinkcolor" VALUE="#c0c0ff">
<param NAME="vlinkcolor" VALUE="#7820d8">
<param NAME="titlefont" VALUE="plain-12">
<param NAME="linkfont" VALUE="plain-14">
<param NAME="n0" VALUE=4>
<param NAME="title1" VALUE="Physics links">
<param NAME="n1" VALUE=4>
<param NAME="l1_1" VALUE="http://xxx.lanl.gov">
<param NAME="t1_1" VALUE="xxx e-print archive">
<param NAME="l1_2" VALUE="http://www-spires.slac.stanford.edu/find/hep/">
<param NAME="t1_2" VALUE="HEP search">
<param NAME="l1_3" VALUE="http://www-pdg.lbl.gov">
<param NAME="t1_3" VALUE="Particle Data Group">
<param NAME="l1_4" VALUE="http://www.lerc.nasa.gov/WWW/PAO/warp.htm">
<param NAME="t1_4" VALUE="Warp Drive When?">
<param NAME="title2" VALUE="Linux and Java links">
<param NAME="n2" VALUE=2>
<param NAME="l2_1" VALUE="http://rpm.pbone.net">
<param NAME="t2_1" VALUE="RPM PBone Search">
<param NAME="l2_2" VALUE="http://www.ibiblio.org/javafaq/">
<param NAME="t2_2" VALUE="Cafe au Lait Java News and Resources">
<param NAME="title3" VALUE="Music links">
<param NAME="n3" VALUE=4>
<param NAME="l3_1" VALUE="http://www.mediastorm.hu/vhk/">
<param NAME="t3_1" VALUE="VHK">
<param NAME="l3_2" VALUE="http://www.laibach.nsk.si">
<param NAME="t3_2" VALUE="Laibach">
<param NAME="l3_3" VALUE="http://www.deadcandance.com">
<param NAME="t3_3" VALUE="Dead Can Dance">
<param NAME="l3_4" VALUE="http://site.voila.fr/chrisb/natlas.html">
<param NAME="t3_4" VALUE="Natacha Atlas">
<param NAME="title4" VALUE="Heights">
<param NAME="n4" VALUE=4>
<param NAME="l4_1" VALUE="http://www.met.hu/cloudalbum/cloud.htm">
<param NAME="t4_1" VALUE="Little cloud album">
<param NAME="l4_2" VALUE="http://ludens.elte.hu/climb/">
<param NAME="t4_2" VALUE="Hungarian Climbing Homepage">
<param NAME="l4_3" VALUE="http://www.dtek.chalmers.se/Climbing/contents.html">
<param NAME="t4_3" VALUE="Climbing Archive">
<param NAME="l4_4" VALUE="http://www.climbing.nl/europe/map_ein.html">
<param NAME="t4_4" VALUE="Map - European climbing websites">
<strong>(YOU CANNOT SEE A JAVA APPLET HERE)</strong><br>
<script LANGUAGE="JavaScript">
<!--
function wropt(s) {
	var a = s.split("/");
	document.write("<font COLOR=red>");
	for(var i=0; i<a.length; ++i) {
		document.write(a[i]);
		if(i<a.length-1) {
			document.write("<font COLOR=yellow>/</font>");
		}
	}
	document.write("</font>");
}
if(navigator.appName=="Netscape") {
	var v = navigator.appVersion.substring(0,2);
	if(navigator.userAgent.indexOf("Win16") != -1) {
		document.writeln("ERROR: Java does not work in 16 bit mswindows browsers.<br>");
		document.writeln("Get a 32 bit version.<br>");
	} else if(!navigator.javaEnabled()) {
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
</p>

</body>
</html>
EOF
