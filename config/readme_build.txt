Here are some steps to build the step application:
1. make sure subversion and ant are installed


## this gets all of opensha
2. check out source code from repo:
svn --username xxxx --password xxxx  co https://intensity.usc.edu/svn/opensha/branches/bpeng/copy_trunk opensha



3. build the application:
cd opensha/src/org/opensha/step/
ant init
ant package

the jar and tar file are in opensha/build, 
-- step-aftershock.jar: the java application ready to run
-- step-aftershock.tar: archived package containing everything needed to
run the application, including data, scripts etc.