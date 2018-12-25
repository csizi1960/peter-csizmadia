# mycbc.sh - 2009.03.27 - Peter Csizmadia
# Shell functions for searches.
# GRB search: mygrb, mygrb_trigger, mygrb_plot. Based on
# https://www.lsc-group.phys.uwm.edu/ligovirgo/cbcnote/JointS5/080110064941TrigSearchHowToRunTheSearch
# Low mass: mymonth, ... Based on
# https://www.lsc-group.phys.uwm.edu/ligovirgo/cbcnote/JointS5/081010152636LigoVirgoLowMassInstructions_for_Running_Your_Month
# Source it from your .profile, .bash_profile or .zprofile:
# . $HOME/mycbc.sh

_my_colorecho() {
	case $TERM in
	xterm*)
		echo -e "\e[0;33m$*\e[0m"
		;;
	*)
		echo "$*"
		;;
	esac
}

_my_run() {
	_my_colorecho "$*"
	/bin/sh -c "$*"
}

_my_lscsoft_run() {
	_my_colorecho "(. $LSCSOFT_PREFIX/lscsoft-user-env.sh; . $LSCSOFT_PREFIX/pylal/etc/pylal-user-env.sh; $*)"
	/bin/sh -c ". $LSCSOFT_PREFIX/lscsoft-user-env.sh; . $LSCSOFT_PREFIX/pylal/etc/pylal-user-env.sh; $*"
}
 
_my_cd() {
	_my_colorecho "cd $1"
	cd $1
}

_my_question() {
	answer="___"
	while echo $answer | egrep -v $2 >/dev/null; do
		case $TERM in
		xterm*)
			# green message, green user input
			echo -ne "\e[0;32m$1 \e[0;32m"
			;;
		*)
			echo -n "$1 "
			;;
		esac
		read answer
	done
	case $TERM in
	xterm*)
		echo -ne '\e[0m'
		;;
	esac
}

_my_shwordsplit_begin() {
	if [ -n "$ZSH_VERSION" ]; then
		_orig_shwordsplit=`setopt|awk '{if($1=="shwordsplit") print $2}'`
		setopt shwordsplit
	fi
}

_my_shwordsplit_end() {
	if [ -n "$ZSH_VERSION" -a "$_orig_shwordsplit" = "off" ]; then
		unsetopt shwordsplit
	fi
}

_my_test_condor_dag_running() {
	[ -f "$1.dagman.log" ] || return 1
	condor_wait -wait 0 "$1.dagman.log" >/dev/null
}

_my_test_cit() {
	grep '^search .*ligo.caltech.edu' /etc/resolv.conf >/dev/null
}

_my_test_llo() {
	grep '^search .*ligo-la.caltech.edu' /etc/resolv.conf >/dev/null
}

_my_test_lho() {
	grep '^search .*ligo-wa.caltech.edu' /etc/resolv.conf >/dev/null
}

_my_test_nemo() {
	grep '^search .*.phys.uwm.edu' /etc/resolv.conf >/dev/null
}

_my_test_atlas() {
	grep '^search .*atlas' /etc/resolv.conf >/dev/null
}

_my_check_lscsoft() {
	if [ -z "$LSCSOFT_PREFIX" ]; then
		cat <<EOF
LSCSOFT_PREFIX not defined.
LSC software is not installed or configured properly.
EOF
		return 1
	else
		cat <<EOF

Does LSCSOFT_PREFIX=$LSCSOFT_PREFIX contain
the current "$1" version of the LSC software?
EOF
		_my_question "[y/n/?]" "^(y|n)$"
		if [ "$answer" = "n" ]; then
			cat <<EOF
Please set LSCSOFT_PREFIX to point to the "$1" version,
then rerun $2.
EOF
			return 1
		fi
	fi
	return 0
}

_my_set_html_dir() {
	tag=`basename $ANALYSIS_DIR`
	MY_HTML_OUT_DIR=""
	MY_HTML_OUT_URL=""
	if _my_test_cit; then
		MY_HTML_OUT_DIR="$HOME/public_html/$tag"
		MY_HTML_OUT_URL="http://ldas-jobs.ligo.caltech.edu/~$USER/$tag"
	elif _my_test_llo; then
		MY_HTML_OUT_DIR="$HOME/public_html/$tag"
		MY_HTML_OUT_URL="http://ldas-jobs.ligo-la.caltech.edu/~$USER/$tag"
	elif _my_test_lho; then
		MY_HTML_OUT_DIR="$HOME/public_html/$tag"
		MY_HTML_OUT_URL="http://ldas-jobs.ligo-wa.caltech.edu/~$USER/$tag"
	elif _my_test_nemo; then
		MY_HTML_OUT_DIR="/archive/users/$USER/$tag"
		MY_HTML_OUT_URL="http://ldas-jobs.phys.uwm.edu/~$USER/$tag"
	else
		echo "WARNING! Could not set MY_HTML_OUT_DIR and MY_HTML_OUT_URL"
	fi
	_my_colorecho "export MY_HTML_OUT_DIR=\"$MY_HTML_OUT_DIR\""
	_my_colorecho "export MY_HTML_OUT_URL=\"$MY_HTML_OUT_URL\""
	export MY_HTML_OUT_DIR MY_HTML_OUT_URL
}

########################################################################
########################################################################
############################## GRB Search ##############################
########################################################################
########################################################################

_mygrb_help() {
	cat <<EOF
Usage: mygrb GRB TYPE
- GRB = YYMMDD, selected from
  https://ldas-jobs.ligo.caltech.edu/~dietz/pages/s5/GRB/CVS/overviewS5.html
- TYPE = h1h2 or h1h2l1

Note that mygrb_* requires the "exttrig" version of the LSC software.
EOF
}

_mygrb_help2() {
	cat <<EOF

You can use these commands:
        mygrb_trigger   -  creates condor dag for GRB search, submits the job
        mygrb_plot      -  creates plot dag, submits the job
EOF
}

mygrb() {
	MY_GRB=`echo $1|sed 's/^\(GRB\|grb\)//'`
	if [ -z "$MY_GRB" ]; then
		_mygrb_help
	fi
	if [ -z "$RUN_DIR" ]; then
		echo
		_my_colorecho RUN_DIR=$HOME/run
		RUN_DIR=$HOME/run
		if [ ! -d "$RUN_DIR" ]; then
			_my_run mkdir $RUN_DIR
		fi
#		for w in /local/$USER /usr1/$USER /people/$USER; do
#			if [ -d $w ]; then
#				_my_colorecho "RUN_DIR=\"$w\""
#				RUN_DIR=$w
#				break;
#			fi
#		done
	fi
	if [ -z "$RUN_DIR" ]; then
		cat <<EOF
You should set RUN_DIR in your .profile (or bash_profile or .zprofile)
Example:
export RUN_DIR=\$HOME/run
EOF
		return
	fi
	if [ -z "$MY_GRB" ]; then
		#
		# Try to find an already prepared GRB directory.
		#
		_my_shwordsplit_begin
		w=`/bin/sh -c "/bin/ls -1 -tr $RUN_DIR|grep '^GRB'" 2>/dev/null`
		grbdirs=""
		for d in $w; do
			if [ -d "$RUN_DIR/$d" ]; then
				if [ -z "$grbdirs" ]; then
					grbdirs=$d
				else
					grbdirs="$grbdirs $d"
				fi
			fi
		done
		if [ -n "$grbdirs" ]; then
			echo
			_origdir=`pwd`
			echo You might want to run
			for d in $grbdirs; do
				cd $RUN_DIR/$d
				g=`echo $d | sed "s:^GRB::"`
				w=`/bin/sh -c "echo S5GRB_*_trigger_hipe_uberdag.dag" 2>/dev/null`
				if [ -n "$w" ]; then
					w=`echo $w | sed "s:^S5GRB_::;s:_trigger_hipe_uberdag.dag$::"`
					if [ "$w" != '*' ]; then
						w=`echo $w | tr HL hl`
						echo "mygrb $g $w"
					else
						echo "mygrb $g TYPE"
					fi
				else
					echo mygrb $g TYPE
				fi
			done
			cd $_origdir
		fi
		_my_shwordsplit_end
		return
	fi
	if [ ! -d "$RUN_DIR/condor" ]; then
		_my_run mkdir -p $RUN_DIR/condor
	fi
	export MY_GRB
	_my_colorecho "export MY_GRB=\"$MY_GRB\""
	MY_IFOS=`echo $2 | tr '[:lower:]' '[:upper:]'`
	export MY_IFOS
	_my_colorecho "export MY_IFOS=\"$MY_IFOS\""
	detectors_uc=""
	thopts="--grb $MY_GRB"
	phopts="--config-file ./plot_hipe.ini"
	ifos=`echo $2 | sed 's/[0-9]/& /g'`
	_my_shwordsplit_begin
	numifos=`for d in $ifos; do echo $d; done | wc -l`
	for d in $ifos; do
		d_lc=`echo $d | tr '[:upper:]' '[:lower:]'`
		d_uc=`echo $d | tr '[:lower:]' '[:upper:]'`
		detectors_uc="${detectors_uc}${d_uc}"
		thopts="$thopts --${d_lc}-segments S5${d_uc}_selectedsegs.txt"
		phopts="$phopts --${d_lc}-data"
	done
	_my_shwordsplit_end
	if [ -z "$detectors_uc" ]; then
		_mygrb_help
		return
	fi
	MY_UBERDAG="S5GRB_${detectors_uc}_trigger_hipe_uberdag.dag"
	export MY_UBERDAG
	_my_colorecho "export MY_UBERDAG=\"$MY_UBERDAG\""
	MY_INI_FILE="S5GRB_${detectors_uc}_trigger_hipe.ini"
	thopts="$thopts --list listGRB.xml"
	thopts="$thopts --onsource-left 5 --onsource-right 1"
	thopts="$thopts --config-file $MY_INI_FILE"
	thopts="$thopts --log-path=$RUN_DIR/condor"
	thopts="$thopts --number-buffer-left 8 --number-buffer-right 8"
	thopts="$thopts --num-trials 340 --padding-time 72 --verbose"
	thopts="$thopts --overwrite-dir"
	LALAPPS_TRIGGER_HIPE_OPTS=$thopts
	if [ "$numifos" = "1" ]; then
		phopts="$phopts --one-ifo"
	elif [ "$numifos" = "2" ]; then
		phopts="$phopts --two-ifo"
	elif [ "$numifos" = "3" ]; then
		phopts="$phopts --three-ifo"
	elif [ "$numifos" = "4" ]; then
		phopts="$phopts --four-ifo"
	elif [ "$numifos" = "5" ]; then
		phopts="$phopts --five-ifo"
	else
		echo unsupported number of interferometers: $numifos
		return
	fi
	phopts="$phopts --plotinspiral --plotethinca --plotthinca \
--plotnumtemplates --plotinjnum \
--plotinspmissed --plotinspinj --plotinspiralrange \
--plotsnrchi --plotgrbtimeslidestats"
	phopts="$phopts --second-stage"
	phopts="$phopts --log-path $RUN_DIR"
	phopts="$phopts --write-script"
	LALAPPS_PLOT_HIPE_OPTS=$phopts
	export ANALYSIS_DIR=$RUN_DIR/GRB$MY_GRB
	_my_colorecho "export ANALYSIS_DIR=\"$ANALYSIS_DIR\""
	_my_set_html_dir
	export CVSROOT=":pserver:$USER@gravity.phys.uwm.edu:/usr/local/cvs/ligovirgo"
	_my_colorecho "export CVSROOT=\"$CVSROOT\""
	export CBC_CVS_ROOT=$RUN_DIR/cbccvs
	_my_colorecho "export CBC_CVS_ROOT=\"$CBC_CVS_ROOT\""
	_my_colorecho "MY_INI_FILE=\"$MY_INI_FILE\""
	_my_colorecho "LALAPPS_TRIGGER_HIPE_OPTS=\"$LALAPPS_TRIGGER_HIPE_OPTS\""
	_my_colorecho "LALAPPS_PLOT_HIPE_OPTS=\"$LALAPPS_PLOT_HIPE_OPTS\""
	echo
	if [ -d "$ANALYSIS_DIR" ]; then
		_my_cd $ANALYSIS_DIR
		if [ -f "LSCdataFind" ]; then
			cat <<EOF
Analysis directory $ANALYSIS_DIR seems to be prepared.
EOF
			_mygrb_help2
			return
		fi
	fi
	_my_check_lscsoft "exttrig" "mygrb" || return

	#
	# Checkout the CBC CVS directories
	#
	if [ -d "${CBC_CVS_ROOT}.tmp" ]; then
		_my_run rm -rf ${CBC_CVS_ROOT}.tmp
	fi
	if [ -d "${CBC_CVS_ROOT}" ]; then
		_my_run rm -rf ${CBC_CVS_ROOT}
	fi
	_my_run mkdir -p ${CBC_CVS_ROOT}.tmp
	_my_cd ${CBC_CVS_ROOT}.tmp
	if ! grep `echo $CVSROOT|sed 's/:\//:.*\//'` $HOME/.cvspass 2>/dev/null >/dev/null
	then
		cvs login || return
	fi
	_my_run cvs co cbc/protected/projects/s5/grb/S5 || return
	_my_run cvs co cbc/protected/projects/s5/ihope || return
	_my_run cvs co cbc/public/segments/S5/H1/dq_segments.901987214.txt || return
	_my_run cvs co cbc/public/segments/S5/H2/dq_segments.901987214.txt || return
	_my_run cvs co cbc/public/segments/S5/L1/dq_segments.901987214.txt || return
	_my_cd $HOME
	_my_run mv ${CBC_CVS_ROOT}.tmp/cbc ${CBC_CVS_ROOT}
	_my_cd ${CBC_CVS_ROOT}
	_my_run rmdir ${CBC_CVS_ROOT}.tmp
	_my_run mkdir -p $ANALYSIS_DIR
	_my_run cp 'protected/projects/s5/grb/S5/*.txt' $ANALYSIS_DIR || return
	_my_run cp 'protected/projects/s5/grb/S5/*.ini' $ANALYSIS_DIR || return
	_my_run cp 'protected/projects/s5/grb/S5/listGRB.xml' $ANALYSIS_DIR || return
	_my_run cp 'protected/projects/s5/ihope/*.txt' $ANALYSIS_DIR || return
	_my_run cp 'public/segments/S5/H1/dq_segments.901987214.txt' $ANALYSIS_DIR/H1_dq_segments.901987214.txt || return
	_my_run cp 'public/segments/S5/H2/dq_segments.901987214.txt' $ANALYSIS_DIR/H2_dq_segments.901987214.txt || return
	_my_run cp 'public/segments/S5/L1/dq_segments.901987214.txt' $ANALYSIS_DIR/L1_dq_segments.901987214.txt || return

	#
	# Copy executables
	#
	if [ -d "$LSCSOFT_PREFIX/lalapps" ]; then
		LALAPPS_LOCATION="$LSCSOFT_PREFIX/lalapps"
	fi
	_my_cd $LALAPPS_LOCATION/bin || return
	_my_run cp lalapps_coherent_inspiral lalapps_coherentbank lalapps_coire \
lalapps_frjoin lalapps_inca lalapps_inspinj lalapps_inspiral \
lalapps_inspiral_hipe lalapps_sire lalapps_thinca lalapps_tmpltbank \
lalapps_trigbank lalapps_plot_hipe lalapps_trigger_hipe $ANALYSIS_DIR
	if [ -d "$LSCSOFT_PREFIX/glue" ]; then
		GLUE_LOCATION="$LSCSOFT_PREFIX/glue"
	fi
	_my_cd $GLUE_LOCATION/bin
	_my_run cp LSCdataFind ligolw_add $ANALYSIS_DIR || return
	_my_cd $ANALYSIS_DIR

	_mygrb_help2
}

mygrb_trigger() {
	if [ -z "$ANALYSIS_DIR" -o -z "$LALAPPS_TRIGGER_HIPE_OPTS"\
		-o -z "$MY_UBERDAG" ]
	then
		echo "Run mygrb first."
		return
	fi
	dag=$MY_UBERDAG
	if [ -f "$dag" ]; then
		echo "File $dag exists."
	else
		injini="$1"
		exttriginjstop="$2"
		if [ ! -f "$injini" ]; then
			injinis=""
			for i in injections*.ini; do
				if [ -z "$injinis" ]; then
					injinis="$i"
				else
					injinis="$injinis $i"
				fi
			done
			cat <<EOF
Please edit an existing ini file
        $injinis
or create a new one. To speed up the search, you might want
to decrease exttrig-inj-stop and delete waveforms.

Then call this command by specifying the ini filename as argument:
EOF
			w=""
			for i in injections*.ini; do
				if [ -z "$w" ]; then
					w="mygrb_trigger $i"
				else
					w="or   mygrb_trigger $i"
				fi
				echo "        $w"
			done
cat <<EOF
Optionally, you can specify the exttrig-inj-stop parameter as the second
argument to override the default, e.g. mygrb_trigger injections.ini 500
EOF
			return
		fi
		_my_cd $ANALYSIS_DIR || return
		if [ -n "$exttriginjstop" ]; then
			sed "s/^exttrig-inj-stop .*$/exttrig-inj-stop = $exttriginjstop/g" $injini >${injini}.tmp
			mv ${injini}.tmp $injini
		fi
		_my_lscsoft_run ./lalapps_trigger_hipe $LALAPPS_TRIGGER_HIPE_OPTS \
--injection-config $injini || return
		[ -f "$dag" ] || return
	fi
	if [ "$dag" -nt "${dag}.dagman.out" -o ! -f "${dag}.dagman.out" ]; then
		_my_question "Submit condor job [y/n/?]" "^(y|n)$"
		if [ "$answer" = "y" ]; then
			_my_colorecho unset X509_USER_PROXY
			unset X509_USER_PROXY
			_my_run grid-proxy-init -valid 99:00 || return
			_my_colorecho export _CONDOR_DAGMAN_LOG_ON_NFS_IS_ERROR=FALSE
			export _CONDOR_DAGMAN_LOG_ON_NFS_IS_ERROR=FALSE
			_my_lscsoft_run condor_submit_dag $MY_UBERDAG || return
		fi
	else
		echo "File ${dag}.dagman.out is newer than the dag."
	fi
}

mygrb_plot() {
	if [ -z "$CBC_CVS_ROOT" -o -z "$ANALYSIS_DIR" -o -z "$MY_GRB"\
			-o -z "$MY_UBERDAG" -o -z "$MY_IFOS" ]; then
		echo "Run mygrb first."
		return
	fi
	_my_check_lscsoft "exttrig" "mygrb_plot" || return
	if [ -d "$LSCSOFT_PREFIX/pylal" ]; then
		PYLAL_LOCATION="$LSCSOFT_PREFIX/pylal"
	elif [ -z "$PYLAL_LOCATION" ]; then
		if [ -n "$PYLAL_PREFIX" ]; then
			PYLAL_LOCATION=$PYLAL_PREFIX
		else
			echo "pyLAL not installed or configured properly."
			return
		fi
	fi
	dag="$ANALYSIS_DIR/GRB$MY_GRB/Plothipe/plot_hipe.GRB${MY_GRB}.dag"
	if [ -f "$dag" ]; then
		echo "File $dag exists."
		_my_cd $ANALYSIS_DIR/GRB$MY_GRB/Plothipe
	else
		_my_cd $CBC_CVS_ROOT/protected/projects/s5/grb/S5
		_my_run cvs update -dA
		_my_cd $ANALYSIS_DIR/GRB$MY_GRB
		gpsStartTime=`awk '{if($1==0) print $2}' offSourceSeg.txt`
		gpsEndTime=`awk '{if($1==0) print $3}' offSourceSeg.txt`
		_my_run mkdir -p Plothipe
		_my_cd Plothipe
		_my_run mkdir -p executables
		_my_run cp ${PYLAL_LOCATION}'/bin/*' executables
		w="s:^input-user-tag =.*:input-user-tag = GRB$MY_GRB:"
		w="$w;s:^exttrig =.*:exttrig = $ANALYSIS_DIR/GRB$MY_GRB:"
		w="$w;s:^user-tag =.*:user-tag = GRB$MY_GRB:"
		w="$w;s:^cache-file =.*:cache-file = ../GRB$MY_GRB.cache:"
		w="$w;s:^gps-start-time =.*:gps-start-time = $gpsStartTime:"
		w="$w;s:^gps-end-time =.*:gps-end-time = $gpsEndTime:"
		f="$CBC_CVS_ROOT/protected/projects/s5/grb/S5/plot_hipe.ini"
		_my_colorecho "sed \"$w\" $f >plot_hipe.ini"
		sed "$w" $f >plot_hipe.ini
		echo
		_my_lscsoft_run ../../lalapps_plot_hipe $LALAPPS_PLOT_HIPE_OPTS
		[ -f "$dag" ] || return
		_my_run "sed -r 's/--followup-tag \*injections\*//' plot_hipe.GRB${MY_GRB}.sh > new.file && mv new.file plot_hipe.GRB${MY_GRB}.sh"
		for i in plot_hipe.plotinspmissed_SECOND_*.GRB${MY_GRB}.sub; do
			_my_run "sed -r 's/--followup-tag \*injections\*//' $i > new.file && mv new.file $i"
		done
	fi
	if ! _my_test_condor_dag_running "$ANALYSIS_DIR/$MY_UBERDAG"; then
		cat <<EOF

Analysis job still running. Logfile:
$ANALYSIS_DIR/${MY_UBERDAG}.dagman.log
Please wait for its completion, then rerun mygrb_plot to submit the plot job.
EOF
		return
	fi
	if [ "$dag" -nt "${dag}.dagman.out" -o ! -f "${dag}.dagman.out" ]; then
		_my_question "Submit condor job [y/n/?]" "^(y|n)$"
		if [ "$answer" = "y" ]; then
			_my_colorecho unset X509_USER_PROXY
			unset X509_USER_PROXY
			_my_run grid-proxy-init -valid 99:00 || return
			_my_colorecho export _CONDOR_DAGMAN_LOG_ON_NFS_IS_ERROR=FALSE
			export _CONDOR_DAGMAN_LOG_ON_NFS_IS_ERROR=FALSE
			_my_lscsoft_run condor_submit_dag "plot_hipe.GRB${MY_GRB}.dag" || return
		fi
	else
		echo "File ${dag}.dagman.out is newer than the dag."
	fi
	[ -f "${dag}.dagman.out" ] || return
	if [ -f runLikelihoodPipe_relic.sh ]; then
		echo File runLikelihoodPipe_relic.sh exists.
		return
	fi
	if ! _my_test_condor_dag_running "$dag"; then
		cat <<EOF

Plot job still running. Logfile:
$dag.dagman.log
Please wait for its completion, then rerun mygrb_plot to continue.
EOF
		return
	fi
	[ -d "$MY_HTML_OUT_DIR" ] || _my_run mkdir -p $MY_HTML_OUT_DIR
	_my_run "cp -pr plots/* $MY_HTML_OUT_DIR"
	_my_lscsoft_run executables/exttrig_likelihood_pipe \
--config-file plot_hipe.ini --stage relic \
--inj-coinc-pattern THINCA_SECOND*injections \
--injection-pattern INJECTION*injection --ifo-tag $MY_IFOS || return
	_my_lscsoft_run sh runLikelihoodPipe_relic.sh || return
}

########################################################################
########################################################################
############################## Low Mass ################################
########################################################################
########################################################################

_mymonth_find_existing_work_dirs() {
	/bin/sh -c "cd $RUN_DIR; for d in \`/bin/ls -1tr\`; do [ -d \$d/firststage ] &&echo \$d; done" 2>/dev/null
}

_mymonth_help() {
	cat <<EOF
Usage: mymonth DIR
- DIR  - working directory in $RUN_DIR (relative path)

Note that mymonth_* requires the "lowcbc" version of the LSC software.

Example: mymonth mycbcmonth
EOF
}

########################################################################
####################### Preparation ####################################
########################################################################

mymonth() {
	if [ -z "$RUN_DIR" ]; then
		_my_colorecho RUN_DIR=$HOME/run
		RUN_DIR=$HOME/run
		[ -d $RUN_DIR ] || _my_run mkdir -p $RUN_DIR
#		for w in /local/$USER /usr1/$USER /people/$USER; do
#			if [ -d $w ]; then
#				_my_colorecho "RUN_DIR=\"$w\""
#				RUN_DIR=$w
#				break;
#			fi
#		done
	fi
	if [ -z "$RUN_DIR" ]; then
		cat <<EOF
You should set RUN_DIR in your .profile (or bash_profile or .zprofile)
Example:
export RUN_DIR=\$HOME/run
EOF
		return
	fi
	if [ -z "$1" ]; then
		#
		# Try to find an already prepared CBC months directory.
		#
		w=`_mymonth_find_existing_work_dirs`
		_my_shwordsplit_begin
		if [ -n "$w" ]; then
			_mymonth_help
			echo
			echo You might want to run
			for d in $w; do
				echo mymonth $d
			done
		else
			_mymonth_help
		fi
		_my_shwordsplit_end
		return
	fi
	export ANALYSIS_DIR=$RUN_DIR/$1
	_my_colorecho "export ANALYSIS_DIR=\"$ANALYSIS_DIR\""
	if [ ! -d "$ANALYSIS_DIR" ]; then
		_my_run mkdir -p $ANALYSIS_DIR
	fi
	export CVSROOT=":pserver:$USER@gravity.phys.uwm.edu:/usr/local/cvs/ligovirgo"
	_my_set_html_dir
	_my_colorecho "export CVSROOT=\"$CVSROOT\""
	_my_cd $ANALYSIS_DIR
	cat <<EOF

You can run mymonth_firststage, mymonth_secondstage, mymonth_thirdstage
or mymonth_fourthstage. Note that fourth stage (plotting) may require a
different LSC software version, you should set LSCSOFT_PREFIX correctly.
Example:
LSCSOFT_PREFIX=\$HOME/opt/lscsoft-lowcbc20081207 mymonth_fourthstage
EOF
}

_mymonth_set_gpstimes() {
	_dir=`pwd`
	cd "$1"
	w=""
	for f in *-*; do
		if [ -d "$f" ]; then
			w="y"
		fi
	done
	if [ "$w" != "y" ]; then
		cd $_dir
		gpstimes=""
		return 1
	fi
	echo "Select GPS start-end times:"
	times=()
	i=0
	for f in *-*; do
		if [ -d "$f" ]; then
			i=$(($i+1))
			times[$i]=$f
			echo "[$i] ${times[$i]}"
		fi
	done
	answer=""
	while [ -z "$answer" ]; do
		q="["
		i=0
		for f in ${times[@]}; do
			i=$(($i+1))
			q="${q}$i/"
		done
		q="${q}?]"
		_my_question "$q" "^[0-9]+$"
		if [ -n "$answer" ]; then
			if [ -z ${times[$answer]} ]; then
				answer=""
			fi
		fi
	done
	cd $_dir
	gpstimes=${times["$answer"]}
	return 0
}



mymonth_firststage() {
	if [ -z "$ANALYSIS_DIR" -o -z "$CVSROOT" ]; then
		echo "Run mymonth first."
		return
	fi
	if [ -n "$1" ]; then
		gpstimes="$1"
	else
		_mymonth_set_gpstimes $ANALYSIS_DIR/firststage
	fi
	tstart=`echo $gpstimes | sed 's/-.*$//'`
	tend=`echo $gpstimes | sed 's/^.*-//'`
	if [ -z "$tstart" -o -z "$tend" ]; then
		cat <<EOF
Usage: mymonth_firststage STARTTIME-ENDTIME
EOF
		return
	fi
	_firststage_initialized="n"
	if [ ! -d "$ANALYSIS_DIR/firststage" ]; then
		_my_run mkdir -p $ANALYSIS_DIR/firststage
	fi
	if [ ! -d $ANALYSIS_DIR/cvs\
			-o ! -f $ANALYSIS_DIR/firststage/ihope.ini ]; then
		#
		# Copy the cat veto files and the ini file from CVS
		#
		_my_cd $ANALYSIS_DIR
		if ! grep `echo $CVSROOT|sed 's/:\//:.*\//'` $HOME/.cvspass 2>/dev/null >/dev/null
		then
			cvs login || return
		fi
		_my_run cvs co -P -d cvs cbc/protected/projects/s5/lv_lowmass || return
		_my_run cp 'cvs/*cat*txt' firststage
		_my_run cp cvs/ihope_firststageonly.ini firststage/ihope.ini
		_firststage_initialized="y"
	fi
	if [ "$_firststage_initialized" = "y"\
		-o ! -f $ANALYSIS_DIR/firststage/LSCdataFind ]
	then
		#
		# Copy executables
		#
		_my_cd $ANALYSIS_DIR/firststage
		_my_check_lscsoft "lowcbc" "mymonth_firststage" || return
		_my_run ../cvs/getexecutables.sh $LSCSOFT_PREFIX || return
		cat <<EOF

Depending on where you run, you may need to uncomment match=localhost
and url-type=file in ihope.ini. Then rerun mymonth_firststage $1
EOF
		return
	fi
	_my_cd $ANALYSIS_DIR/firststage || return
	[ -d "$ANALYSIS_DIR/logs/firststage" ] || _my_run mkdir -p $ANALYSIS_DIR/logs/firststage
	dag="inspiral_hipe_datafind.DATAFIND.dag"
	if [ -f "$gpstimes/datafind/$dag" ]; then
		echo "File $gpstimes/datafind/$dag exists."
	else
		_my_lscsoft_run ./lalapps_ihope \
--gps-start-time $tstart --gps-end-time $tend \
--config-file ihope.ini --log-path $ANALYSIS_DIR/logs/firststage/ \
--skip-followup --skip-data-quality || return
		[ -f "$gpstimes/datafind/$dag" ] || return
	fi
	if [ "$gpstimes/datafind/$dag" -nt "$gpstimes/datafind/${dag}.dagman.out" ]; then
		_my_cd $ANALYSIS_DIR/firststage/$gpstimes || return
		_my_colorecho export _CONDOR_DAGMAN_LOG_ON_NFS_IS_ERROR=FALSE
		export _CONDOR_DAGMAN_LOG_ON_NFS_IS_ERROR=FALSE
		_my_colorecho unset X509_USER_PROXY
		unset X509_USER_PROXY
		_my_run grid-proxy-init -hours 72 || return
		_my_cd $ANALYSIS_DIR/firststage/$gpstimes/datafind || return
		_my_lscsoft_run condor_submit_dag -f $dag || return
		_my_run condor_wait ${dag}.dagman.log || return
		_my_cd $ANALYSIS_DIR/firststage/$gpstimes/datafind/cache || return
		_my_run "ls -l | sort -nr -k 5" || return
		if [ -n "`/bin/ls -l|awk '{if($5==0) print}'`" ]; then
			# there is at least one cache file with zero size
			cat <<EOF
User intervention needed, see "Running ihope over the first stage" in
https://www.lsc-group.phys.uwm.edu/ligovirgo/cbcnote/JointS5/081010152636LigoVirgoLowMassInstructions_for_Running_Your_Month
Then rerun this command and answer "n" for the first question.
EOF
			return
		fi
	fi
	_my_cd $ANALYSIS_DIR/firststage/$gpstimes || return
	dag="ihope.dag"
	if [ "$dag" -nt "${dag}.dagman.out" ]; then
		_my_question "Submit condor job [y/n/?]" "^(y|n)$"
		if [ "$answer" = "y" ]; then
			_my_lscsoft_run condor_submit_dag $dag
		fi
	fi
	if [ ! -f "write_ihope_page.ini" ]; then
		_my_check_lscsoft "lowcbc" "mymonth_firststage $gpstimes" || return
		htmldir="$MY_HTML_OUT_DIR/firststage"
		url="$MY_HTML_OUT_URL/firststage/"
		s="s|^gps-start-time.*|gps-start-time = $tstart|\
;s|^gps-end-time .*|gps-end-time = $tend|\
;s|^ihope-directory .*|ihope-directory = $ANALYSIS_DIR/firststage/|\
;s|YOURLALAPPSINSTALLDIR|$LSCSOFT_PREFIX/lalapps|"
		if [ -n "$htmldir" ]; then
			s="$s;s|^html-directory .*|html-directory = $htmldir|\
;s|^url .*|url = $url|"
		fi
		_my_run mkdir -p $htmldir || return
		_my_run "sed \"$s\" \
$LSCSOFT_PREFIX/lalapps/share/lalapps/write_ihope_page.ini > write_ihope_page.ini"
		cat write_ihope_page.ini
		cat <<EOF

If there are invalid settings, then you should edit write_ihope_page.ini.
EOF
		_my_question "Proceed now [y/n/?]" "^(y|n)$"
		if [ "$answer" = "n" ]; then
			cat <<EOF
Edit write_ihope_page.ini, then rerun mymonth_firststage $1
EOF
			return
		fi
	fi
	if [ "${dag}.dagman.log" -nt "$dag" ]; then
		htmldir=`grep '^html-directory' write_ihope_page.ini |sed 's/^html-directory[ ]\+=[ ]\+//'`
		answer=""
		if [ -n "`/bin/ls $htmldir 2>/dev/null`" ]; then
			echo "$htmldir not empty."
			_my_question "Remove and rewrite ihope page when condor job finished [y/n/?]" "^(y|n)$"
			if [ "$answer" = "y" ]; then
				_my_run "rm -rf $htmldir"
			fi
		else
			_my_question "Write ihope page when condor job finished [y/n/?]" "^(y|n)$"
		fi
		if [ "$answer" = "y" ]; then
			_my_run condor_wait ${dag}.dagman.log || return
			_my_run lalapps_write_ihope_page --config-file write_ihope_page.ini --skip-upperlimit --skip-injection --verbose --skip-tuning || return
		fi
	fi
}

########################################################################
############################ Second stage ##############################
########################################################################

mymonth_secondstage() {
	if [ -z "$ANALYSIS_DIR" -o -z "$CVSROOT" ]; then
		echo "Run mymonth first."
		return
	fi
	_my_check_lscsoft "lowcbc" "mymonth_secondstage" || return
	if [ ! -d "$ANALYSIS_DIR/firststage" ]; then
		echo "Run mymonth_firststage first."
		return
	fi
	if [ ! -d $ANALYSIS_DIR/cvs\
			-o ! -f $ANALYSIS_DIR/secondstage/ihope.ini ]; then
		#
		# Copy the cat veto files and the ini file from CVS
		#
		_my_cd $ANALYSIS_DIR
		if ! grep `echo $CVSROOT|sed 's/:\//:.*\//'` $HOME/.cvspass 2>/dev/null >/dev/null
		then
			cvs login || return
		fi
		_my_run cvs co -P -d cvs cbc/protected/projects/s5/lv_lowmass || return
		[ -d secondstage ] || _my_run mkdir secondstage || return
		_my_run cp 'cvs/*cat*txt' secondstage || return
		_my_run cp cvs/ihope_secondstageonly.ini secondstage/ihope.ini
	fi

	_my_cd $ANALYSIS_DIR/secondstage
	if [ ! -f LSCdataFind ]; then
		#
		# Copy executables
		#
		_my_run ../cvs/getexecutables.sh $LSCSOFT_PREFIX || return
		_my_run "CVSROOT=:pserver:anonymous@gravity.phys.uwm.edu:2402/usr/local/cvs/lscsoft cvs checkout -r 1.4 -d lalapps_inspiral_cvs lalapps/src/inspiral/link_old_ihope.in" || return
	fi
	_mymonth_set_gpstimes $ANALYSIS_DIR/firststage
	tstart=`echo $gpstimes | sed 's/-.*$//'`
	tend=`echo $gpstimes | sed 's/^.*-//'`
	dag="inspiral_hipe_datafind.DATAFIND.dag"
	if [ -f "$gpstimes/datafind/$dag" ]; then
		echo "File $gpstimes/datafind/$dag exists."
	else
		[ -d "$ANALYSIS_DIR/logs/secondstage" ] || _my_run mkdir -p $ANALYSIS_DIR/logs/secondstage
		_my_lscsoft_run ./lalapps_ihope \
--gps-start-time $tstart --gps-end-time $tend \
--config-file ihope.ini --log-path $ANALYSIS_DIR/logs/secondstage/ \
--skip-followup --skip-data-quality || return
		[ -f "$gpstimes/datafind/$dag" ] || return
		_my_cd $ANALYSIS_DIR/secondstage/$gpstimes
		_my_lscsoft_run python ../lalapps_inspiral_cvs/link_old_ihope.in \
--tmpltbank --inspiral-first --make-links \
--old-cache-file $ANALYSIS_DIR/firststage/$gpstimes/ihope.cache \
--append-cache ihope.cache \
--run-names playground,full_data,bbh001inj,bns001inj,fullrange001inj,nsbh001inj,spin001inj
	fi
	_my_cd "$ANALYSIS_DIR/secondstage/$gpstimes/datafind" || return
	if [ "$dag" -nt "${dag}.dagman.out" ]; then
		_my_colorecho export _CONDOR_DAGMAN_LOG_ON_NFS_IS_ERROR=FALSE
		export _CONDOR_DAGMAN_LOG_ON_NFS_IS_ERROR=FALSE
		_my_colorecho unset X509_USER_PROXY
		unset X509_USER_PROXY
		_my_run grid-proxy-init -hours 72 || return
		_my_lscsoft_run condor_submit_dag -f $dag || return
		_my_run condor_wait ${dag}.dagman.log || return
		_my_cd $ANALYSIS_DIR/secondstage/$gpstimes/datafind/cache || return
		_my_run "ls -l | sort -nr -k 5" || return
		if [ -n "`/bin/ls -l|awk '{if($5==0) print}'`" ]; then
			# there is at least one cache file with zero size
			cat <<EOF
User intervention needed, see "Running ihope over the first stage" in
https://www.lsc-group.phys.uwm.edu/ligovirgo/cbcnote/JointS5/081010152636LigoVirgoLowMassInstructions_for_Running_Your_Month
Then rerun this command.
EOF
			return
		fi
	fi
	_my_cd $ANALYSIS_DIR/secondstage/$gpstimes || return
	dag="ihope.dag"
	if [ "$dag" -nt "${dag}.dagman.out" ]; then
		if grep 'DIR datafind$' ihope.dag >/dev/null; then
			_my_run "sed 's/DIR datafind$/DIR datafind DONE/' ihope.dag > ihope.dag.tmp && mv ihope.dag.tmp ihope.dag"
		fi
		_my_question "Submit condor job [y/n/?]" "^(y|n)$"
		if [ "$answer" = "y" ]; then
			_my_lscsoft_run condor_submit_dag ihope.dag
		fi
	fi
}

########################################################################
############################ Third stage ###############################
########################################################################

mymonth_thirdstage() {
	if [ -z "$ANALYSIS_DIR" -o -z "$CVSROOT" ]; then
		echo "Run mymonth first."
		return
	fi
	_my_check_lscsoft "lowcbc" "mymonth_thirdstage" || return
	if [ ! -d "$ANALYSIS_DIR/firststage" ]; then
		echo "Run mymonth_firststage first."
		return
	fi
	if [ ! -d "$ANALYSIS_DIR/secondstage" ]; then
		echo "Run mymonth_secondstage first."
		return
	fi
	if [ ! -d $ANALYSIS_DIR/cvs\
			-o ! -f $ANALYSIS_DIR/thirdstage/ihope.ini ]; then
		#
		# Copy the cat veto files and the ini file from CVS
		#
		_my_cd $ANALYSIS_DIR
		if ! grep `echo $CVSROOT|sed 's/:\//:.*\//'` $HOME/.cvspass 2>/dev/null >/dev/null
		then
			cvs login || return
		fi
		_my_run cvs co -P -d cvs cbc/protected/projects/s5/lv_lowmass || return
		[ -d thirdstage ] || _my_run mkdir thirdstage || return
		_my_run cp 'cvs/*cat*txt' thirdstage || return
		_my_run cp cvs/ihope_thirdstageonly.ini thirdstage/ihope.ini
	fi

	_my_cd $ANALYSIS_DIR/thirdstage
	if [ ! -f LSCdataFind ]; then
		#
		# Copy executables
		#
		_my_run ../cvs/getexecutables.sh $LSCSOFT_PREFIX || return
		_my_run "CVSROOT=:pserver:anonymous@gravity.phys.uwm.edu:2402/usr/local/cvs/lscsoft cvs checkout -r 1.4 -d lalapps_inspiral_cvs lalapps/src/inspiral/link_old_ihope.in" || return
	fi
	_mymonth_set_gpstimes $ANALYSIS_DIR/secondstage
	tstart=`echo $gpstimes | sed 's/-.*$//'`
	tend=`echo $gpstimes | sed 's/^.*-//'`
	dag="inspiral_hipe_datafind.DATAFIND.dag"
	w="y"
	if [ -f "$gpstimes/datafind/$dag" ]; then
		echo "File $gpstimes/datafind/$dag exists."
	else
		[ -d "$ANALYSIS_DIR/logs/thirdstage" ] || _my_run mkdir -p $ANALYSIS_DIR/logs/thirdstage
		_my_lscsoft_run ./lalapps_ihope \
--gps-start-time $tstart --gps-end-time $tend \
--config-file ihope.ini --log-path $ANALYSIS_DIR/logs/thirdstage/ \
--skip-followup --skip-data-quality || return
		[ -f "$gpstimes/datafind/$dag" ] || return
		_my_cd $ANALYSIS_DIR/thirdstage/$gpstimes
		for stage in firststage secondstage; do
			_my_lscsoft_run python ../lalapps_inspiral_cvs/link_old_ihope.in \
--tmpltbank --inspiral-first --make-links \
--old-cache-file $ANALYSIS_DIR/$stage/$gpstimes/ihope.cache \
--append-cache ihope.cache \
--run-names playground,full_data,bbh001inj,bns001inj,fullrange001inj,nsbh001inj,spin001inj
		done
	fi
	_my_cd "$ANALYSIS_DIR/thirdstage/$gpstimes/datafind" || return
	if [ "$dag" -nt "${dag}.dagman.out" ]; then
		_my_colorecho export _CONDOR_DAGMAN_LOG_ON_NFS_IS_ERROR=FALSE
		export _CONDOR_DAGMAN_LOG_ON_NFS_IS_ERROR=FALSE
		_my_colorecho unset X509_USER_PROXY
		unset X509_USER_PROXY
		_my_run grid-proxy-init -hours 72 || return
		_my_lscsoft_run condor_submit_dag -f $dag || return
		_my_run condor_wait ${dag}.dagman.log || return
		_my_cd $ANALYSIS_DIR/firststage/$gpstimes/datafind/cache || return
		_my_run "ls -l | sort -nr -k 5" || return
		if [ -n "`/bin/ls -l|awk '{if($5==0) print}'`" ]; then
			# there is at least one cache file with zero size
			cat <<EOF
User intervention needed, see "Running ihope over the first stage" in
https://www.lsc-group.phys.uwm.edu/ligovirgo/cbcnote/JointS5/081010152636LigoVirgoLowMassInstructions_for_Running_Your_Month
Then rerun this command.
EOF
			return
		fi
	fi
	_my_cd $ANALYSIS_DIR/thirdstage/$gpstimes || return
	dag="ihope.dag"
	if [ "$dag" -nt "${dag}.dagman.out" ]; then
		if grep 'DIR datafind$' ihope.dag >/dev/null; then
			_my_run "sed 's/DIR datafind$/DIR datafind DONE/' ihope.dag > ihope.dag.tmp && mv ihope.dag.tmp ihope.dag"
		fi
		_my_question "Submit condor job [y/n/?]" "^(y|n)$"
		if [ "$answer" = "y" ]; then
			_my_lscsoft_run condor_submit_dag ihope.dag || return
		fi
	fi
}

########################################################################
############################ Fourth stage ##############################
########################################################################

mymonth_fourthstage() {
	if [ -z "$ANALYSIS_DIR" -o -z "$CVSROOT" ]; then
		echo "Run mymonth first."
		return
	fi
	_my_check_lscsoft "lowcbc/plots" "mymonth_fourthstage" || return
	for i in firststage secondstage thirdstage; do
		if [ ! -d "$ANALYSIS_DIR/$i" ]; then
			echo "Run mymonth_$i first."
			return
		fi
	done
	if [ ! -d $ANALYSIS_DIR/cvs\
			-o ! -f $ANALYSIS_DIR/fourthstage/ihope.ini ]; then
		#
		# Copy the cat veto files and the ini file from CVS
		#
		_my_cd $ANALYSIS_DIR
		if ! grep `echo $CVSROOT|sed 's/:\//:.*\//'` $HOME/.cvspass 2>/dev/null >/dev/null
		then
			cvs login || return
		fi
		_my_run cvs co -P -d cvs cbc/protected/projects/s5/lv_lowmass || return
		[ -d fourthstage ] || _my_run mkdir fourthstage || return
		_my_run cp 'cvs/*cat*txt' fourthstage || return
		_my_run cp cvs/ihope_fourthstageonly.ini fourthstage/ihope.ini
	fi

	_my_cd $ANALYSIS_DIR/fourthstage
	if [ ! -f LSCdataFind ]; then
		#
		# Copy executables
		#
		_my_run ../cvs/getexecutables.sh $LSCSOFT_PREFIX || return
	fi
	_mymonth_set_gpstimes $ANALYSIS_DIR/thirdstage
	tstart=`echo $gpstimes | sed 's/-.*$//'`
	tend=`echo $gpstimes | sed 's/^.*-//'`
	if [ ! -d $gpstimes/segments ]; then
		_my_run mkdir -p $gpstimes/segments
	fi
	if [ -n "`/bin/ls $gpstimes/segments 2>/dev/null`" ]; then
		cat <<EOF
Directory $gpstimes/segments contains files, hopefully nothing is missing.
EOF
	else
		_my_run "cp $ANALYSIS_DIR/thirdstage/$gpstimes/segments/* $gpstimes/segments/" || return
		[ -d "$ANALYSIS_DIR/logs/fourthstage" ] || _my_run mkdir -p $ANALYSIS_DIR/logs/fourthstage || return
	fi
	_my_cd "$ANALYSIS_DIR/fourthstage" || return
	dag="$gpstimes/ihope.dag"
	if [ "$dag" -nt "ihope.ini" ]; then
		echo File $dag seems to be up to date.
	else
		_my_lscsoft_run ./lalapps_ihope \
--gps-start-time $tstart --gps-end-time $tend \
--config-file ihope.ini --log-path $ANALYSIS_DIR/logs/fourthstage/ \
--skip-followup --skip-generate-segments --skip-generate-veto-segments \
--skip-search --skip-data-quality --skip-datafind || return
	fi
	_my_cd "$ANALYSIS_DIR/fourthstage/$gpstimes" || return
	_my_run ln -sf $ANALYSIS_DIR/thirdstage/$gpstimes/ihope.cache ihope.cache || return
	cp /scratch2/jclayton/s5_2yr_lv_lowcbc_20081207/opt/lscsoft/pylal/bin/plotinspmissed executables/
	if [ ! -f countplots.sh ]; then
		_my_run "cp ../../cvs/runfourthstageplots* ."
		_my_run "cp ../../cvs/countplots.sh ."
		dt=`expr $tend - $tstart`
		_my_run "perl -pe 's/\d{9}-\d{9}/$gpstimes/;s/\d{9}-\d{1,8}/$tstart-$dt/;s/\d{9}/$tstart/' ../../cvs/makesummary > makesummary"
		_my_run "chmod +x runfourthstageplots*sh countplots.sh makesummary"
	fi
	if [ -f "myrunfourthstageplots3.out" ]; then
		echo "Plot job seems to be completed."
	fi
	_my_question "(Re)run the plotting job [y/n/?]" "^(y|n)$"
	if [ "$answer" = "y" ]; then
		cat <<EOF

You should run plotting jobs on pcdev1.
If we are not on pcdev1, then please log in and run
mymonth `basename $ANALYSIS_DIR`; mymonth_fourthstage $gpstimes
EOF
		_my_question "Proceed now (only on pcdev1!) [y/n/?]" "^(y|n)$"
	fi
	if [ "$answer" = "y" ]; then
		echo "Creating plots..."
		./runfourthstageplots1.sh &> myrunfourthstageplots1.out &
		./runfourthstageplots2.sh &> myrunfourthstageplots2.out &
		./runfourthstageplots3.sh &> myrunfourthstageplots3.out &
		wait
	fi
	if [ -f "myrunfourthstageplots3.out" ]; then
		echo You should edit makesummary to create a proper summary page.
		_my_question "Make summary page now [y/n/?]" "^(y|n)$"
		if [ "$answer" = "y" ]; then
			if [ ! -d "$MY_HTML_OUT_DIR/fourthstage" ]; then
				_my_run mkdir -p $MY_HTML_OUT_DIR/fourthstage || return
			fi
			_my_lscsoft_run "python makesummary --output-dir $MY_HTML_OUT_DIR/fourthstage"
		fi
	fi
}
