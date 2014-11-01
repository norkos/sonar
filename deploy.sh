TEST_DIR=~/tmp
mkdir $TEST_DIR
kill -9 `ps -ef | grep sonar-app | grep jar | awk '{print $2}'`
rm -Rf $TEST_DIR/sonar*
unzip sonar-application/target/sonar-*.zip -d $TEST_DIR
