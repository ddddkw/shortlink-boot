FROM adoptopenjdk/openjdk11:jre11u-nightly

COPY target/dclass-account.jar dclass-account.jar
COPY agent /usr/local/agent

ENV SW_AGENT_NAME "dclass-account"

ENTRYPOINT ["java","-javaagent:/usr/local/agent/skywalking-agent.jar","-jar","/dclass-account.jar"]