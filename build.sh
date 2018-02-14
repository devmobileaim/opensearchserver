git pull
mvn -Dgpg.skip=true clean package -DskipTests
sudo cp -R target/opensearchserver-1.5.14/* /var/lib/opensearchserver/server/ROOT/
sudo service opensearchserver restart
