FROM adoptopenjdk/openjdk11:jre11u-nightly

COPY target/dclass-app.jar dclass-app.jar
COPY agent /usr/local/agent

ENV SW_AGENT_NAME "dclass-app"

ENTRYPOINT ["java","-javaagent:/usr/local/agent/skywalking-agent.jar","-jar","/dclass-app.jar"]