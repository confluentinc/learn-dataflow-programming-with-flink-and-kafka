# Dataflow Programming with Flink and Kafka

This repository is a companion to the **Building Flink Applications in Java** course provided by Confluent Developer.

[https://developer.confluent.io/courses/flink-java](https://developer.confluent.io/courses/flink-java)

It contains additional examples to connect Flink to Kafka using Java.

## Requirements

- Java 11 
	- Flink does not currently support anything newer than Java 11. The code in the repo assumes that you are using Java 11.
- A Java development environment.
- A Flink installation.
- A Confluent Cloud account.

## Gitpod

A [Gitpod](https://gitpod.io/) configuration is available for this repository. You can use this to construct a pre-configured environment suitable for trying out the code:

[![Open in Gitpod](https://gitpod.io/button/open-in-gitpod.svg)](https://gitpod.io/#https://github.com/confluentinc/learn-dataflow-programming-with-flink-and-kafka)

## Click Stream Connector

This job will pull data from a topic named clickstream. It assumes you have created the topic in your cluster and are using the Confluent datagen connector to produce clickstream data to the topic.

## Repo Structure

### install_flink.sh

This script is used by the `.gitpod.yml` to install Flink. However, if you are setting up a local environment, you may want to refer to this script (or even execute it) to get your own Flink installation ready.

### pom.xml

The Maven configuration file for the Java project.

### src

This folder contains all of the source code for the project.

### start_clickstream.sh

A helper script for compiling and running the Flink job.

### consumer.properties & producer.properties

Inside the src/main/resources folder you will find two properties files named `consumer.properties` and `producer.properties`. These files contain configurations required to connect to the Kafka cluster. However, they contain placeholders for the `<BOOTSTRAP_SERVER>` `<USERNAME>` and `<PASSWORD>`. You will need to provide your own values if you want to run the code.