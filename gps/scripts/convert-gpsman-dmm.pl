#!/usr/bin/perl -w
#
# convert-gpsman-dmm.pl / Peter Csizmadia, 01/27/2005-12/11/2005
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

sub dms2dmm {
	my ($d, $m, $s) = @_;
	my $a = substr($d, 0, 1);
	$d = substr($d, 1, length($d) - 1);
	$m += $s/60;
	return sprintf("%s%d %06.3f", $a, $d, $m);
}

sub ddd2dmm {
        my ($arg) = @_;
        my $a = substr($arg, 0, 1);
        my $ddd = substr($arg, 1, length($arg) - 1);
        $ddd = (int(36000*$ddd + 0.5) + 0.1)/36000.0;
        my $d = int($ddd);
        my $m = 60*($ddd - $d);
        return sprintf("%s%d %06.3f", $a, $d, $m);
}

my @monthlist = ("jan", "feb", "mar", "apr", "may", "jun",
		  "jul", "aug", "sep", "oct", "nov", "dec");
my $monthspat = join("|", @monthlist);
my %monthhash = ("jan" => "01", "feb" => "02", "mar" => "03", "apr" => "04",
		 "may" => "05", "jun" => "06", "jul" => "07", "aug" => "08",
		 "sep" => "09", "oct" => "10", "nov" => "11", "dec" => "12");

while(<>) {
	s/^!Format: D(MS|DD) 1 WGS 84/!Format: DMM 1 WGS 84/;

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

	# convert DMS to DMM
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
			my $lat = dms2dmm($fields[$coordi],
					  $fields[$coordi + 1],
					  $fields[$coordi + 2]);
			my $long = dms2dmm($fields[$coordi + 3],
					   $fields[$coordi + 4],
					   $fields[$coordi + 5]);
			$line =~ s/$fields[$coordi] $fields[$coordi+1] $fields[$coordi+2]/$lat/;
			$line =~ s/$fields[$coordi+3] $fields[$coordi+4] $fields[$coordi+5]/$long/;
		}
		$_=$line;

	# convert DDD to DMM
	} elsif(/\t[NS][\d\.]+[ \t]+[EW][\d\.]+/) {
                my $line = $_;
                my @fields = split(/[ \t\r\n]+/);
                my $coordi = -1;
                for(my $i = 0; $i < @fields && $coordi < 0; ++$i) {
                        if($fields[$i] =~ /[NS][\d\.]+/
                                        && $fields[$i + 1] =~ /[EW][\d\.]+/) {
                                $coordi = $i;
                        }
                }
                if($coordi >= 0) {
                        my $lat = ddd2dmm($fields[$coordi]);
                        my $long = ddd2dmm($fields[$coordi + 1]);
                        $line =~ s/$fields[$coordi]/$lat/;
                        $line =~ s/$fields[$coordi+1]/$long/;
                }
                $_=$line;
	}
	print;
}
