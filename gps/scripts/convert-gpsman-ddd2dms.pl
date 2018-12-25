#!/usr/bin/perl -w
#
# Converts waypoints in a GPSMan DDD format datafile to DMS format
#
# Peter Csizmadia, 11/28/2004
#

use strict;

sub ddd2dms {
	my ($arg) = @_;
	my $a = substr($arg, 0, 1);
	my $ddd = substr($arg, 1, length($arg) - 1);
	$ddd = (int(36000*$ddd + 0.5) + 0.1)/36000.0;
	my $d = int($ddd);
	my $m = int(60*($ddd - $d));
	my $s = 60*(60*($ddd - $d) - $m);
	return sprintf("%s%d %02d %04.1f", $a, $d, $m, $s);
}

while(<>) {
	s/^!Format: DDD 1 WGS 84/!Format: DMS 1 WGS 84/;
	if(/^[A-Z0-9].*\t[NS][0-9\.]+[ \t]+[EW][0-9\.]+/) {
		my $line = $_;
		my @fields = split(/[ \t\r\n]+/);
		my $coordi = -1;
		for(my $i = 0; $i < @fields && $coordi < 0; ++$i) {
			if($fields[$i] =~ /[NS][0-9\.]+/
					&& $fields[$i + 1] =~ /[EW][0-9\.]+/) {
				$coordi = $i;
			}
		}
		if($coordi >= 0) {
			my $lat = ddd2dms($fields[$coordi]);
			my $long = ddd2dms($fields[$coordi + 1]);
			$line =~ s/$fields[$coordi]/$lat/;
			$line =~ s/$fields[$coordi+1]/$long/;
		}
		print $line;
	} else {
		print;
	}
}
