# akka-sample-tracing-java

## Building java project
Run ```mvn clean install``` to build the project.
Once the maven install is complete you will find ```akka-sample-tracing-java-1.0-allinone.jar``` jar file in ```target``` folder.

In the ```application.conf``` file in ```resources``` folder you will find that we have configured Zipkin server to run at ```localhost``` and port ```9410``` which is the default port for running Zipkin server.

## Installing Zipkin and running its server
You can find details of downloading and installing Zipkin on [zipkin website] (http://zipkin.io/pages/quickstart.html).

Download the toolkit jar and save it in a folder.

From the folder run the jar using following command: 

```java -jar zipkin.jar
```

Now you will see in the java project logs that is connects to Zipkin server.

Also read my blog on [Distributed Tracing for Microservices] (http://javaredhot.blogspot.com/2017/02/akka-with-zipkin-async-framework-with.html)

