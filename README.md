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

## Repo Structure

### install_flink.sh

This script is used by the `.gitpod.yml` to install Flink. However, if you are setting up a local environment, you may want to refer to this script (or even execute it) to get your own Flink installation ready.