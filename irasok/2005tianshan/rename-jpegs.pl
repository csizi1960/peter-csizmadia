#!/usr/bin/perl -w

use strict;
use File::stat;

my @fs;
$fs[0]="p1204+13-05-pici.jpg";
$fs[1]="p0624-pici.jpg";
$fs[2]="vz01000037-pici.jpg";
$fs[3]="p0201-pici.jpg";
$fs[4]="p0233-pici.jpg";
$fs[5]="p0423+28+31+33-pici.jpg";
$fs[6]="p0503+02+01.jpg";
$fs[7]="p0527+31-pici.jpg"; 
$fs[8]="p0623+22-pici.jpg";
$fs[9]="p0633+34+37-pici.jpg";
$fs[10]="p0703+04-pici.jpg";
$fs[11]="p0706+13-pici.jpg";
$fs[12]="p0708+09+10+11+12-pici.jpg";
$fs[13]="p0836+35+37-pici.jpg";
$fs[14]="p1029+11-02-pici.jpg";
$fs[15]="p1128+27+25+24+16+15+14.jpg";
$fs[16]="p1320-pici.jpg";
$fs[17]="p1414+13-pici.jpg";
$fs[18]="p1513+22+27+32+16-05-pici.jpg";
$fs[19]="p1810+07-pici.jpg";
$fs[20]="p1704+03+02+30-pici.jpg";
$fs[21]="p1714-csucson+15+19+18+16+17-pici.jpg";

my $sb = stat("index.html");
my $tmpfile = "index.html.rename-jpegs.tmp";

sub compressed_name {
	my ($k) = @_;
	my $s = $k < 10? "$k" : chr(ord('a') + $k - 10);
	return $s.".jpg";
}

sub esc {
	my ($s) = @_;
	$s =~ s/\+/\\+/g;
	$s =~ s/\./\\./g;
	return $s;
}

my $compressed = 1;
my $decompressed = 1;

for my $f (@fs) {
	if(! -e $f) {
		$decompressed = 0;
	}
}
for(my $i = 0; $i < @fs; ++$i) {
	my $f = compressed_name($i);
	if(! -e $f) {
		$compressed = 0;
	}
}
if($compressed && $decompressed) {
	print STDERR "jpegs exist both with compressed and decompressed names\n";
	exit 1;
} elsif(!$compressed && !$decompressed) {
	print STDERR "jpegs missing\n";
	exit 1;
} elsif(!$compressed && $decompressed) {
	print "compressing names\n";

	open(IN, "index.html") or die "index.html: cannot open";
	open(OUT, "> $tmpfile") or die "$tmpfile: cannot open";

	for(my $i = 0; $i < @fs; ++$i) {
		rename($fs[$i], compressed_name($i));
	}
	while(<IN>) {
		for(my $i = 0; $i < @fs; ++$i) {
			my $sd = esc($fs[$i]);
			my $sc = compressed_name($i);
			s/\"$sd\"/\"$sc\"/g;
		}
		print OUT;
	}
	close(OUT) or die "$tmpfile: cannot close";
	close(IN) or die "index.html: cannot close";
	rename($tmpfile, "index.html") or die "$tmpfile: cannot rename";

} elsif($compressed && !$decompressed) {
    	print "decompressing names\n";

	open(IN, "index.html") or die "index.html: cannot open";
	open(OUT, "> $tmpfile") or die "$tmpfile: cannot open";

	for(my $i = 0; $i < @fs; ++$i) {
		rename(compressed_name($i), $fs[$i]);
	}
	while(<IN>) {
		for(my $i = 0; $i < @fs; ++$i) {
			my $sc = compressed_name($i);
			my $sd = $fs[$i];
			s/\"$sc\"/\"$sd\"/g;
		}
		print OUT;
	}
	close(OUT) or die "$tmpfile: cannot close";
	close(IN) or die "index.html: cannot close";
	rename($tmpfile, "index.html") or die "$tmpfile: cannot rename";
}

#utime $sb->atime, $sb->mtime, "index.html";
