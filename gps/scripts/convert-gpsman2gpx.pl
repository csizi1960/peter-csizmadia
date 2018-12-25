#!/usr/bin/perl -w
#
# convert-gpsman2gpx.pl / Peter Csizmadia, 09/14/2006
#
# Converts GPSMan waypoint/track datafiles to GPX.
# Additional conversions:
# - time in standard ISO 8601 format (yyyy-mm-ddThh:mm:ssZ) is recognized
#   and moved to the <time> block
# - date in DD-MMM-YY format is converted to YYYY.MM.DD and moved to <cmt>
# - time is aligned by converting H:MM:SS to 0H:MM:SS and moved to <cmt>
#
# Using with GPSbabel 1.2.5:
#   Example: gpsbabel -w -t -i garmin -f usb: -o gpsman -F out.gpsman
#   Unfortunately, gpsbabel skips the altitude and the icon and only writes 8
#   character names in the exported gpsman files. To fix these annoying
#   limitations, you should edit style/gpsman.style and recompile the program.
#   The style file should contain the following lines:
#   IFIELD	SHORTNAME, "", "%-10.10s"
#   IFIELD	DESCRIPTION, "", "%s"
#   IFIELD	LAT_DIRDECIMAL, "", "%c%f"
#   IFIELD	LON_DIRDECIMAL, "", "%c%f"
#   IFIELD	ICON_DESCR, "", "symbol=%s"
#   IFIELD	ALT_METERS, "", "alt=%.1f"
#   IFIELD	TIMET_TIME, "", "timet=%ld"
#   IFIELD	IGNORE, "", "%s"

use strict;

sub dms2ddd {
	my ($d, $m, $s) = @_;
	my $a = substr($d, 0, 1);
	$d = substr($d, 1, length($d) - 1);
	$d += ($m + $s/60)/60;
	return sprintf("%s%09.6f", $a, $d);
}

my @monthlist = ("jan", "feb", "mar", "apr", "may", "jun",
		  "jul", "aug", "sep", "oct", "nov", "dec");
my $monthspat = join("|", @monthlist);
my %monthhash = ("jan" => "01", "feb" => "02", "mar" => "03", "apr" => "04",
		 "may" => "05", "jun" => "06", "jul" => "07", "aug" => "08",
		 "sep" => "09", "oct" => "10", "nov" => "11", "dec" => "12");

print <<EOF;
<?xml version="1.0" encoding="ISO-8859-1" standalone="yes"?>
<gpx
 version="1.0"
 creator="GPSMan" 
 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
 xmlns="http://www.topografix.com/GPX/1/0"
 xmlns:topografix="http://www.topografix.com/GPX/Private/TopoGrafix/0/2"
 xsi:schemaLocation="http://www.topografix.com/GPX/1/0 http://www.topografix.com/GPX/1/0/gpx.xsd http://www.topografix.com/GPX/Private/TopoGrafix/0/2 http://www.topografix.com/GPX/Private/TopoGrafix/0/2/topografix.xsd">
EOF

while(<>) {
	chomp;
	next if /^!/;
	next if /^[ \t\n\r]*$/;

	# convert date in DD-MMM-YYYY format for YYYY.MM.DD
	s/\t(\d\d)-($monthspat)-(\d\d\d\d)/\t$3.$monthhash{lc($2)}.$1/i;

	# convert date in DD-MMM-YY format for YYYY.MM.DD
	s/\t(\d\d)-($monthspat)-(\d\d)/\t20$3.$monthhash{lc($2)}.$1/i;

	# convert time in H:MM:SS format to HH:MM:SS
	s/ (\d):(\d\d):(\d\d)/ 0$1:$2:$3/;

	# convert Garmin icon names to gpsman format
	s/\tsymbol=Summit/\tsymbol=summit/;
	s/\tsymbol=Trail Head/\tsymbol=trail_head/;
	s/\tsymbol=Parking Area/\tsymbol=parking/;
	s/\tsymbol=Residence/\tsymbol=house/;
	s/\tsymbol=Crossing/\tsymbol=crossing/;

	# workaround for gpsbabel track export
	if(/[ ]+\t\t[NS]\d+\.\d+.*/) {
		s/[ ]+(\t.*)/$1/;
		s/\tsymbol=\t/\t/; # remove empty symbol column
		s/\talt=/\t/; # remove "alt="
		if(/\ttimet=(\d+)/) {
			# convert timet time to yyyy.mm.dd hh:mm:ss format
			my ($sec,$min,$hr,$day,$mon,$year) = localtime($1);
			my $t = sprintf("%04d.%02d.%02d %02d:%02d:%02d",
					$year + 1900, $mon + 1, $day,
					$hr, $min, $sec);
			s/\ttimet=(\d+)//;
			s/^\t\t/\t$t\t/;
		}
	}

	# convert DMS to DDD
	if(/\t[NS]\d+ \d+ [\d\.]+[ \t]+[EW]\d+ \d+ [\d\.]+/) {
		my $line = $_;
		my @fields = split(/[ \t\r\n]+/);
		my $coordi = -1;
		for(my $i = 0; $i < @fields && $coordi < 0; ++$i) {
			if($fields[$i] =~ /[NS][0-9]+/
					&& $fields[$i + 1] =~ /\d+/
					&& $fields[$i + 2] =~ /[\d\.]+/
					&& $fields[$i + 3] =~ /[EW]\d+/
					&& $fields[$i + 4] =~ /\d+/
					&& $fields[$i + 5] =~ /[\d\.]+/) {
				$coordi = $i;
			}
		}
		if($coordi >= 0) {
			my $lat = dms2ddd($fields[$coordi],
					  $fields[$coordi + 1],
					  $fields[$coordi + 2]);
			my $long = dms2ddd($fields[$coordi + 3],
					   $fields[$coordi + 4],
					   $fields[$coordi + 5]);
			$line =~ s/$fields[$coordi] $fields[$coordi+1] $fields[$coordi+2]/$lat/;
			$line =~ s/$fields[$coordi+3] $fields[$coordi+4] $fields[$coordi+5]/$long/;
		}
		$_=$line;

	# convert DMM to DDD
	} elsif(/\t[NS]\d+ [\d\.]+[ \t]+[EW]\d+ [\d\.]+/) {
		my $line = $_;
		my @fields = split(/[ \t\r\n]+/);
		my $coordi = -1;
		for(my $i = 0; $i < @fields && $coordi < 0; ++$i) {
			if($fields[$i] =~ /[NS][0-9]+/
					&& $fields[$i + 1] =~ /[\d\.]+/
					&& $fields[$i + 2] =~ /[EW]\d+/
					&& $fields[$i + 3] =~ /[\d\.]+/) {
				$coordi = $i;
			}
		}
		if($coordi >= 0) {
			my $lat = dms2ddd($fields[$coordi],
					  $fields[$coordi + 1], 0);
			my $long = dms2ddd($fields[$coordi + 2],
					   $fields[$coordi + 3], 0);
			$line =~ s/$fields[$coordi] $fields[$coordi+1]/$lat/;
			$line =~ s/$fields[$coordi+2] $fields[$coordi+3]/$long/;
		}
		$_=$line;
	}

	my @fields = split(/\t/);
	(my $name = $fields[0]) =~ s/[ \t\n\r]*$//;
	my $cmt = "";
	my $time = "";
	# extract UTC time in ISO 8601 format
	if($fields[1] =~ /^(.*) (\d\d\d\d-\d\d-\d\dT\d\d:\d\d:\d\dZ)/) {
		$time = $fields[1];
		$time =~ s/^(.*) (\d\d\d\d-\d\d-\d\dT\d\d:\d\d:\d\dZ)/$2/;
		$cmt = $fields[1];
		$cmt =~ s/^(.*) (\d\d\d\d-\d\d-\d\dT\d\d:\d\d:\d\dZ)/$1/;
	} elsif($fields[1] =~ /^(.*) (\d\d\d\d-\d\d-\d\dT\d\d:\d\dZ)/) {
		$time = $fields[1];
		$time =~ s/^(.*) (\d\d\d\d-\d\d-\d\dT\d\d:\d\dZ)/$2/;
		$cmt = $fields[1];
		$cmt =~ s/^(.*) (\d\d\d\d-\d\d-\d\dT\d\d:\d\dZ)/$1/;
	} else {
		($cmt = $fields[1]) =~ s/[ \t\n\r]*$//;
	}
	my $lat = $fields[2];
	if($lat =~ /^N/) {
		$lat =~ s/^N//;
	} elsif($lat =~ /^S/) {
		$lat =~ s/^S//;
		$lat = "-".$lat;
	}
	my $lon = $fields[3];
	if($lon =~ /^E.*/) {
		$lon =~ s/^E//;
	} elsif($lon =~ /^W.*/) {
		$lon =~ s/^W//;
		$lon = "-".$lon;
	}
	my $elevation = "";
	my $symbol = "";
	for(my $i = 4; $i < @fields; ++$i) {
		if($fields[$i] =~ /^alt=/) {
			($elevation = $fields[$i]) =~ s/^alt=(.*)$/$1/;
		} elsif($fields[$i] =~ /^symbol=/) {
			($symbol = $fields[$i]) =~ s/^symbol=(.*)$/$1/;
		}
	}
	printf("<wpt lat=\"%s\" lon=\"%s\">", $lat, $lon);
	if(length($elevation) != 0) {
		printf("<ele>%s</ele>", $elevation);
	}
	printf("\n  <name>%s</name>", $name);
	if(length($symbol) != 0) {
		printf("<sym>%s</sym>", $symbol);
	}
	if(length($cmt) != 0 && length($time) != 0) {
		printf("\n  <cmt>%s</cmt>", $cmt);
		printf("<time>%s</time>", $time);
	} elsif(length($cmt) != 0) {
		printf("\n  <cmt>%s</cmt>", $cmt);
	} elsif(length($time) != 0) {
		printf("\n  <time>%s</time>", $time);
	}
	printf("</wpt>\n");
}

print <<EOF;
</gpx>
EOF
