FROM adoptopenjdk/openjdk11:jre11u-nightly

COPY target/dclass-link.jar dclass-link.jar
COPY agent /usr/local/agent

ENV SW_AGENT_NAME "dclass-link"

ENTRYPOINT ["java","-javaagent:/usr/local/agent/skywalking-agent.jar","-jar","/dclass-link.jar"]