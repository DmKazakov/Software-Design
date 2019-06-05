FROM gradle:5.0 AS builder
USER root
RUN mkdir /workspace
ADD . /workspace
RUN cd /workspace && gradle build

FROM openjdk:8-jre-slim
USER root
RUN mkdir /workspace
COPY --from=builder /workspace/build/libs /workspace
ENTRYPOINT ["java", "-jar", "/workspace/docker.jar"]