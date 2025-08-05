FROM adoptopenjdk/openjdk11:jre11u-nightly

COPY target/dclass-shop.jar dclass-shop.jar
COPY agent /usr/local/agent

ENV SW_AGENT_NAME "dclass-shop"

ENTRYPOINT ["java","-javaagent:/usr/local/agent/skywalking-agent.jar","-jar","/dclass-shop.jar"]