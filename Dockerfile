FROM openjdk:17
ADD target/ci-cd.jar ci-cd.jar
ENTRYPOINT ["java","-jar","/ci-cd.jar"]