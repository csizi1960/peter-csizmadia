Prefix: /usr
%define bin /usr/bin
Summary: Slope fitting for transverse momentum spectra
Name: slopefit
Version: 1.2.3
Release: 0
Copyright: GPL
Vendor: Peter Csizmadia
Group: Applications/Physics
Source: http://www.kfki.hu/~cspeter/slopefit/slopefit-%{PACKAGE_VERSION}.tar.gz
URL: http://www.kfki.hu/~cspeter/slopefit/
BuildRoot: /tmp/slopefit-root

%changelog
* Sun May 27 2001 Peter Csizmadia
- version 1.2.3; more datafiles; new options: --mtmin, --mtmax, --ymin, --ymax
* Thu Apr 12 2001 Peter Csizmadia
- version 1.2.2; new options: --ptmin and --ptmax; optimized compilation (-O2)
* Wed Mar 1 2000 Peter Csizmadia
- initial release 1.2.1

%description
Slopefit is a fast, easily scriptable, command line program for fitting
functions to particle momentum spectra.

%prep
%setup -n slopefit
sed 's/^CXXFLAGS =.*/CXXFLAGS = -Wall -O2/' <GNUmakefile >makefile.optimized

%build
make -f makefile.optimized
strip slopefit

%install
rm -rf $RPM_BUILD_ROOT/
mkdir -p ${RPM_BUILD_ROOT}%{bin}
cp -p slopefit ${RPM_BUILD_ROOT}%{bin}/slopefit

#
# finish install
#
chown -R root.root ${RPM_BUILD_ROOT}

%clean
rm -rf $RPM_BUILD_ROOT
 
%files
%doc COPYING
%{bin}/slopefit
