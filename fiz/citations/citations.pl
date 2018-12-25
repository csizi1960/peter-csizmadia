#!/usr/bin/perl -w
use strict;
use utf8;
use File::stat;
use Getopt::Std;
use vars qw / $opt_t /;

getopts('t:');

if($#ARGV < 0) {
	print <<EOF;
Usage: perl citations.pl [-t year] tex|mtatpa
EOF
	exit;
}

my $opt_f = $ARGV[0];

binmode(STDOUT, ":utf8");

open(IN, "citations.src.txt") or die "citations.src.txt cannot be opened";

my %journal_long_name = (
	"ActaPhysHung", "Acta Physica Hungarica",
	"ActaPhysHungNewHIP",
		"Acta Physica Hungarica New Series-Heavy Ion Physics",
	"ActaPhysPolon", "Acta Physica Polonica",
	"BrazJPhys", "Brazilian Journal of Physics",
	"CentralEurJPhys", "Central European Journal of Physics",
	"ClassQuantGrav", "Classical and Quantum Gravity",
	"EurPhysJ", "European Physical Journal",
	"EurPhysJST", "European Physical Journal Special Topics",
	"HIP", "Heavy Ion Physics",
	"IJMP", "International Journal of Modern Physics",
	"JPhys", "Journal of Physics",
	"NewJPhys", "New Journal of Physics",
	"NuclPhys", "Nuclear Physics",
	"NPProc", "Nucler Physics B-Proceedings Supplements",
	"PhysLett", "Physics Letters",
	"PhysRev", "Physics Review",
	"PhysRevLett", "Physics Review Letters",
	"Pramana", "Pramana");

my %journal_short_name = (
	"ActaPhysHung", "Acta Phys.Hung.",
	"ActaPhysHungNewHIP", "Acta Phys.Hung.New Ser.Heavy Ion Phys.",
	"ActaPhysPolon", "Acta Phys.Polon.",
	"AIPConfProc", "AIP Conf.Proc.",
	"BrazJPhys", "Braz.J.Phys.",
	"CentralEurJPhys", "Central Eur.J.Phys.",
	"ClassQuantGrav", "Class.Quant.Grav.",
	"EurPhysJ", "Eur.Phys.J.",
	"EurPhysJST", "Eur.Phys.J.ST",
	"HIP", "Heavy Ion Phys.",
	"IJMP", "Int.J.Mod.Phys.",
	"JPhys", "J.Phys.",
	"NewJPhys", "New J.Phys.",
	"NuclPhys", "Nucl.Phys.",
	"NPProc", "Nucl.Phys.Proc.Suppl.",
	"PhysLett", "Phys.Lett.",
	"PhysRev", "Phys.Rev.",
	"PhysRevLett", "Phys.Rev.Lett.",
	"Pramana", "Pramana");

if($opt_f && $opt_f eq "tex") {
	open(OUTSCI, ">citations.sci.tex")
		or die "cannot write citations.sci.tex";
	open(OUTEPRINT, ">citations.eprint.tex")
		or die "cannot write citations.sci.tex";
	open(OUTPROC, ">citations.proc.tex")
		or die "cannot write citations.sci.tex";
}

sub space_needed_between_journal_and_number {
	my ($journal, $number) = @_;
	if(($journal =~ /.*\.[A-Z].*\.$/)) {
#		return ($number =~ /^[A-Z].*/)? 0 : 1;
		return 0;
	}
	return 1;
}

sub invert_author {
	my ($orig) = @_;
	my $s = $orig;
	$s =~ s/([A-Za-z\.]+\.) (.+) (\(.+\)) et al\./$2 $1 $3 et al./;
	$s =~ s/([A-Za-z\.]+\.) (.+) et al\./$2 $1 et al./ if $s eq $orig;
	$s =~ s/([A-Za-z\.]+\.) (.+)/$2 $1/ if $s eq $orig;
	return $s;
}

sub invert_author_list {
	my ($orig) = @_;
	my @list = split(/, /, $orig);
	my $s = invert_author($list[0]);
	for(my $i = 1; $i < @list; ++$i) {
		$s .= ", ".invert_author($list[$i]);
	}
	return $s;
}

sub remove_dots {
	my ($orig) = @_;
	my @list = split(/\./, $orig);
	my $s = $list[0];
	for(my $i = 1; $i < @list; ++$i) {
		$s =~ s/^(.*[A-Z])[a-z]$/$1/; # Sinyukov Yu.M. --> Sinyukov YM
		$s .= $list[$i];
	}
	return $s;
}

sub decode_tex {
	my ($s) = @_;
	my $aa = chr(0x00e1);
	my $ee = chr(0x00e9);
	my $oo = chr(0x00f3);
	my $odd = chr(0x00f6);
	my $Ho = chr(0x0151);
	my $Hu = chr(0x0161);
	$s =~ s/{\\'a}/$aa/g;
	$s =~ s/{\\'e}/$ee/g;
	$s =~ s/{\\'o}/$oo/g;
	$s =~ s/{\\"o}/$odd/g;
	$s =~ s/{\\H o}/$Ho/g;
	$s =~ s/{\\H u}/$Hu/g;
	$s =~ s/\$\\(psi|phi|Omega|rho)\$/$1/g;
	return $s;
}

###########################################################################
##### LaTeX Output ########################################################
###########################################################################

sub print_cit0_tex {
	my ($f, $where) = @_;
	my ($journal, $number, $year, $pages) = split(/ /, $where);
	if($journal_short_name{$journal}) {
		$journal = $journal_short_name{$journal};
		if(space_needed_between_journal_and_number($journal, $number)) {
			printf($f "%s {\\bf %s}", $journal, $number);
		} else {
			printf($f "%s{\\bf %s}", $journal, $number);
		}
		printf($f " (%s) %s", $year, $pages);
	} else {
		printf($f "%s", $where);
	}
}

sub print_cit_tex {
	my ($f) = @_;
	my ($authors, $where1, $where2, $where3) = split(/\t/);
	if(!$where1) {
		die "incomplete citation $authors";
	}
	printf($f "      \\item %s, ", $authors);
	print_cit0_tex($f, $where1);
	if($where2) {
		printf($f ", ");
		print_cit0_tex($f, $where2);
	}
	if($where3) {
		printf($f ", ");
		print_cit0_tex($f, $where3);
	}
	printf($f "\n");
}

sub print_record_tex {
	my ($f, $sec, $authors, $title, $where, $eprint, $impactf,
	    $citSCI, $citIndep, $citCoauthorSCI, $citCoauthor) = @_;
	printf($f "\\item %s\\\\\n", $authors);
	printf($f "      {\\it %s}\\\\\n", $title);
	my ($journal, $number, $year, $pages) = split(/ /, $where);
	if($journal_long_name{$journal}) {
  		printf($f "      %s {\\bf %s} (%s) %s",
			$journal_long_name{$journal}, $number, $year, $pages);
	} else {
		if($sec eq "SCI") {
			die "unknown journal ".$journal;
		} else {
			printf($f "      %s", $where);
		}
	}
	if($eprint) {
		printf($f "      {\\hfill {\\small \\mbox{e-print: %s}}}\\\\\n",
		       $eprint);
	} else {
		printf($f "\\\\");
	}
	if($impactf || scalar(@$citSCI) + scalar(@$citIndep) != 0
		|| scalar(@$citCoauthorSCI) + scalar(@$citCoauthor) != 0) {
		printf($f "      \\IFIndepDep{%s}{%d}{%d}\n", $impactf,
			scalar(@$citSCI) + scalar(@$citIndep),
			scalar(@$citCoauthorSCI) + scalar(@$citCoauthor));
	}
	if(scalar(@$citSCI) > 0) {
		printf($f "      \\begin{CitListSCI}\n");
		foreach (@$citSCI) {
			print_cit_tex($f);
		}
		printf($f "      \\end{CitListSCI}\n");
	} elsif(scalar($citIndep) > 0
			|| scalar($citCoauthorSCI) > 0
			|| scalar($citCoauthor) > 0) {
		printf($f "      \\setcounter{saveCitList}{0}\n");
	}
	if(scalar(@$citIndep) > 0) {
		printf($f "      \\begin{CitListIndep}\n");
		foreach (@$citIndep) {
			print_cit_tex($f);
		}
		printf($f "      \\end{CitListIndep}\n");
	}
	if(scalar(@$citCoauthorSCI) > 0) {
		printf($f "      \\begin{CitListCoauthorSCI}\n");
		foreach (@$citCoauthorSCI) {
			print_cit_tex($f);
		}
		printf($f "      \\end{CitListCoauthorSCI}\n");
	}
	if(scalar(@$citCoauthor) > 0) {
		printf($f "      \\begin{CitListCoauthor}\n");
		foreach (@$citCoauthor) {
			print_cit_tex($f);
		}
		printf($f "      \\end{CitListCoauthor}\n");
	}
	printf($f "\n");
}

###########################################################################
##### MTA TPA Output ######################################################
###########################################################################

sub conv_author_list_mtatpa {
	my ($s) = @_;
	return remove_dots(invert_author_list(decode_tex($s)));
}

sub print_cit0_mtatpa {
	my ($where) = @_;
	my ($journal, $number, $year, $pages) = split(/ /, $where);
	if($journal_long_name{$journal}) {
		$journal = $journal_long_name{$journal};
		printf("%s %s", $journal, $number);
		printf(" %s (%s)", $pages, $year);
	} else {
		printf("%s", $where);
	}
}

sub print_cit_mtatpa {
	my ($authors, $where1, $where2, $where3) = split(/\t/);
	if(!$where1) {
		die "incomplete citation $authors";
	}
	printf("\t%s, ", conv_author_list_mtatpa($authors));
	print_cit0_mtatpa($where1);
	if($where2) {
		printf(", ");
		print_cit0_mtatpa($where2);
	}
	if($where3) {
		printf(", ");
		print_cit0_mtatpa($where3);
	}
	printf("\n");
}

sub print_record_mtatpa {
	my ($sec, $authors, $title, $where, $eprint, $impactf,
	    $citSCI, $citIndep, $citCoauthorSCI, $citCoauthor) = @_;
	return if $opt_t && scalar(@$citSCI) + scalar(@$citIndep)
			+ scalar(@$citCoauthorSCI) + scalar(@$citCoauthor) == 0;
	printf("%s\n", conv_author_list_mtatpa($authors));
	printf("%s\n", decode_tex($title));
	my ($journal, $number, $year, $pages) = split(/ /, $where);
	if($journal_long_name{$journal}) {
  		printf("%s %s %s (%s)", $journal_long_name{$journal},
		$number, $pages, $year);
	} else {
		if($sec eq "SCI") {
			die "unknown journal ".$journal;
		} else {
			printf("%s", $where);
		}
	}
	printf("\n");
	if($eprint) {
		printf("[%s]\n", $eprint);
	}
	if(scalar(@$citSCI) > 0) {
		foreach (@$citSCI) {
			print_cit_mtatpa();
		}
	}
	if(scalar(@$citIndep) > 0) {
		printf("\t********** Non-SCI:\n");
		foreach (@$citIndep) {
			print_cit_mtatpa();
		}
	}
	if(scalar(@$citCoauthorSCI) > 0) {
		printf("\t********** Coauthor SCI:\n");
		foreach (@$citCoauthorSCI) {
			print_cit_mtatpa();
		}
	}
	if(scalar(@$citCoauthor) > 0) {
		printf("\t********** Coauthor non-SCI:\n");
		foreach (@$citCoauthor) {
			print_cit_mtatpa();
		}
	}
	printf("\n");
}

###########################################################################
##### Conversion ##########################################################
###########################################################################

sub read_citations {
	my @A;
	my $i = 0;
	while(<IN>) {
		chomp;
		if(/^[ \t]*$/) {
			return @A;
		} elsif(/^\* \d+\t/) {
			if($opt_t) {
			       	if(/^\* $opt_t\t/) {
					s/^\* \d+\t//;
					$A[$i++] = $_;
				}
			} else {
				s/^\* \d+\t//;
				$A[$i++] = $_;
			}
		} else {
			die "invalid line $_";
		}
	}
	return @A;
}

sub convert {
	my ($sec) = @_;
	while(<IN>) {
		if(/^[ \t]*$/) {
			return;
		}
		chomp(my $authors = $_);
		chomp(my $title = <IN>);
		chomp($_ = <IN>);
		my ($where, $eprint) = split(/\t/);
		my $impactf = "";
		if($sec eq "SCI") {
			chomp($_ = <IN>);
			$impactf = $_;
		}
		my @citSCI = ();
		my @citIndep = ();
		my @citCoauthorSCI = ();
		my @citCoauthor = ();

		chomp($_ = <IN>);
		if(/^\tCITATIONS_SCI/) {
			@citSCI = read_citations();
			chomp($_ = <IN>);
		}
		if(/^\tCITATIONS_INDEP/) {
			@citIndep = read_citations();
			chomp($_ = <IN>);
		}
		if(/^\tCITATIONS_COAUTHOR_SCI/) {
			@citCoauthorSCI = read_citations();
			chomp($_ = <IN>);
		}
		if(/^\tCITATIONS_COAUTHOR/) {
			@citCoauthor = read_citations();
			chomp($_ = <IN>);
		}
		next if !$opt_f;
		if($opt_f eq "tex") {
			if($sec eq "SCI") {
				print_record_tex(*OUTSCI, $sec, $authors,
					$title, $where, $eprint, $impactf,
					\@citSCI, \@citIndep,
					\@citCoauthorSCI, \@citCoauthor);
			} elsif($sec eq "EPRINT") {
				print_record_tex(*OUTEPRINT, $sec, $authors,
					$title, $where, $eprint, $impactf,
					\@citSCI, \@citIndep,
					\@citCoauthorSCI, \@citCoauthor);
			} elsif($sec eq "PROC") {
				print_record_tex(*OUTPROC, $sec, $authors,
					$title, $where, $eprint, $impactf,
					\@citSCI, \@citIndep,
					\@citCoauthorSCI, \@citCoauthor);
			}
		} elsif($opt_f eq "mtatpa") {
			print_record_mtatpa($sec, $authors,
				$title, $where, $eprint, $impactf,
				\@citSCI, \@citIndep,
				\@citCoauthorSCI, \@citCoauthor);
		}
	}
}

while(<IN>) {
	if(/^MY_SCI$/) {
		convert("SCI");
	} elsif(/^MY_EPRINT$/) {
		convert("EPRINT");
	} elsif(/^MY_PROC$/) {
		convert("PROC");
	}
}
