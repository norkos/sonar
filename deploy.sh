TEST_DIR=~/tmp
mkdir $TEST_DIR
rm -Rf $TEST_DIR/sonar*
unzip sonar-application/target/sonar-*.zip -d $TEST_DIR
$TEST_DIR/sonar-*/bin/linux-x86-64/sonar.sh restart
