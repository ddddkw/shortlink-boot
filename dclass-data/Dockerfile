FROM adoptopenjdk/openjdk11:jre11u-nightly

COPY target/dclass-data.jar dclass-data.jar
COPY agent /usr/local/agent

ENV SW_AGENT_NAME "dclass-data"

ENTRYPOINT ["java","-javaagent:/usr/local/agent/skywalking-agent.jar","-jar","/dclass-data.jar"]