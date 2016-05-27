# advanced_enterprise_computing_2016

Before you can start, you need to download https://github.com/dbermbach/hermes and run `mvn source:jar install`.

Compile this project then with `mvn clean package`.

Run by `java -jar target/advanced_enterprise_computing_2016-0.0.1-SNAPSHOT-jar-with-dependencies.jar`. The jar can be executed everywhere. It does not have to be located inside the target folder. Do not forget to provide the necessary arguments (you can provide urls or paths for hostURI and replicationPathsURI):

usage:
 * -h,--hostsURI <arg>              URI to download hosts file
 * -n,--myNode <arg>                the name of this node
 * -p,--replicationPathsURI <arg>   URI to download replicationPaths file
 * -r,--receivePort <arg>           used port for receiving [8086]

 Example configuration:
 -p src/main/resources/examplepath.xml -h src/main/resources/examplehosts.xml -n nodeA -r 8081
