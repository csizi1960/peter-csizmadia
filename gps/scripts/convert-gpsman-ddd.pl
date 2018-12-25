#!/usr/bin/perl -w
#
# convert-gpsman-ddd.pl / Peter Csizmadia, 12/12/2005
#
# Converts GPSMan waypoint/track datafiles from DMS or DDD to DMM format.
# Additional conversions:
# - date in DD-MMM-YY format is converted to YYYY.MM.DD
# - time is aligned by converting H:MM:SS to 0H:MM:SS
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

while(<>) {
	s/^!Format: D(MS|MM) 1 WGS 84/!Format: DDD 1 WGS 84/;

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
	print;
}
