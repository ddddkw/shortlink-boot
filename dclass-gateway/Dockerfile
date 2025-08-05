FROM adoptopenjdk/openjdk11:jre11u-nightly

COPY target/dclass-gateway.jar dclass-gateway.jar
COPY agent /usr/local/agent

ENV SW_AGENT_NAME "dclass-gateway"

ENTRYPOINT ["java","-javaagent:/usr/local/agent/skywalking-agent.jar","-jar","/dclass-gateway.jar"]