![NATS](src/main/javadoc/images/large-logo.png)

# Java Nats Server Runner

Run the [NATS messaging system](https://nats.io) Server from your Java code. 

Useful for running unit or integration tests on the localhost.


![3.1.0](https://img.shields.io/badge/Current_Release-3.1.0-27AAE0?style=for-the-badge)
![3.1.1](https://img.shields.io/badge/Current_Snapshot-3.1.1--SNAPSHOT-27AAE0?style=for-the-badge)

[![Build Main Badge](https://github.com/nats-io/java-nats-server-runner/actions/workflows/build-main.yml/badge.svg?event=push)](https://github.com/nats-io/java-nats-server-runner/actions/workflows/build-main.yml)
[![Coverage Status](https://coveralls.io/repos/github/nats-io/java-nats-server-runner/badge.svg?branch=main)](https://coveralls.io/github/nats-io/java-nats-server-runner?branch=main)
[![Javadoc](http://javadoc.io/badge/io.nats/jnats-server-runner.svg?branch=main)](http://javadoc.io/doc/io.nats/jnats-server-runner?branch=main)
[![License Apache 2](https://img.shields.io/badge/License-Apache2-blue)](https://www.apache.org/licenses/LICENSE-2.0)

### JDK Version

This project uses Java 8 Language Level api, but builds jars compiled with and targeted for Java 8, 17, 21 and 25.
It creates different artifacts for each. All have the same group id `io.nats` and the same version but have different artifact names.

| Java Target Level | Artifact Id                 |                                                                              Maven Central                                                                               |
|:-----------------:|-----------------------------|:------------------------------------------------------------------------------------------------------------------------------------------------------------------------:|
|        1.8        | `jnats-server-runner`       |      [![Maven JDK 1_8](https://img.shields.io/maven-central/v/io.nats/jnats-server-runner?label=)](https://mvnrepository.com/artifact/io.nats/jnats-server-runner)       |
|        17         | `jnats-server-runner-jdk17` | [![Maven JDK 17](https://img.shields.io/maven-central/v/io.nats/jnats-server-runner-jdk17?label=)](https://mvnrepository.com/artifact/io.nats/jnats-server-runner-jdk17) |
|        21         | `jnats-server-runner-jdk21` | [![Maven JDK 21](https://img.shields.io/maven-central/v/io.nats/jnats-server-runner-jdk21?label=)](https://mvnrepository.com/artifact/io.nats/jnats-server-runner-jdk21) |
|        25         | `jnats-server-runner-jdk25` | [![Maven JDK 25](https://img.shields.io/maven-central/v/io.nats/jnats-server-runner-jdk25?label=)](https://mvnrepository.com/artifact/io.nats/jnats-server-runner-jdk25) |

### Executable

By default, the server is found in your path in this order: 
- the `executablePath` set in the builder 
- the path found in the `nats_server_path` environment variable
- `nats-server` somewhere in the machine's path.

For simple setup, constructors work well 
```java
try (NatsServerRunner server = new NatsServerRunner()) {
    System.out.println("Server running on port: " + server.getPort())
    Connection c = Nats.connect(server.getURI());
    ...
}
```

### Builder

For more complicated setup, use the `NatsServerRunnerBuilder`
```java
String[] customInserts = new String[] {
    "server_name=srv1",
    "cluster {",
    "  name: testcluster",
    "  listen: 127.0.0.1:4222",
    "  routes: [",
    "    nats-route://127.0.0.2:4222",
    "    nats-route://127.0.0.3:4222",
    "  ]",
    "}",
    ""
};

NatsServerRunner.Builder builder = NatsServerRunner.builder()
    .port(4567)
    .debugLevel(DebugLevel.DEBUG_TRACE)
    .jetstream(true)
    .executablePath("/this/run/only/nats-server")
    .configFilePath("/mypath/custom.conf")
    .configInserts(customInserts);

try (NatsServerRunner server = builder.build()) {
} 
catch (Exception e) {
    throw new RuntimeException(e);
}
```

### Static Settings

If you want to run multiple instances of the server, for instance, in unit tests, you can do
some setup statically to reduce the code in the builders. A simple way to do this would be
to have a static initializer in a base test class or something that is called before all uses.
There are several methods available in `NatsRunnerUtils` and can be overridden from the builder, 
except setting the server path. 

```java
static {
    NatsRunnerUtils.setDefaultOutputSupplier(ConsoleOutput::new);
    NatsRunnerUtils.setDefaultOutputLevel(Level.SEVERE); // will reduce the output, nice for tests
    NatsRunnerUtils.setDefaultProcessAliveCheckTries(10);
    NatsRunnerUtils.setDefaultProcessAliveCheckWait(100);
    NatsRunnerUtils.setDefaultConnectValidateTries(3);
    NatsRunnerUtils.setDefaultConnectValidateTimeout(100); // milliseconds
    NatsRunnerUtils.setDefaultOutputThreadProvider(myOutputThreadProvider);
    NatsRunnerUtils.setManualStartPort(1234); // the port used to initialize the port number for auto generated port numbers
    NatsRunnerUtils.setDefaultLocalhostHost(LocalHost.name);
    NatsRunnerUtils.setPreferredServerPath("/path/to/nats-server");
}
```

### nats-server path

To start the NATS server, the program must know the way to run the `nats-server` executable.
By default, it assumes it is in the path and just tries `nats-server`. You can tell the program
where to find the server in two ways. 
1. Call `NatsRunnerUtils.setPreferredServerPath` statically
2. Set the `nats_server_path` environment variable.

The program uses the set path first if it was set. 
If it was not set, it tries the environment path if it was set. 
Last, if neither was set it uses the default.


### Dependency Management

The JNATS Server Runner is available in the Maven central repository,
and can be imported as a standard dependency in your `build.gradle` or `pom.xml` file,
The examples shown use the Jdk 8 version. To use other versions, change the artifact id.

#### Gradle

```groovy
dependencies {
    implementation 'io.nats:jnats-server-runner:3.1.0'
}
```

If you need the latest and greatest before Maven central updates, you can use:

```groovy
repositories {
    mavenCentral()
    maven {
        url "https://repo1.maven.org/maven2/"
    }
}
```

If you need a snapshot version, you must add the url for the snapshots and change your dependency.

```groovy
repositories {
    mavenCentral()
    maven {
        url "https://central.sonatype.com/repository/maven-snapshots"
    }
}

dependencies {
   implementation 'io.nats:jnats-server-runner:3.1.1-SNAPSHOT'
}
```

#### Maven

```xml
<dependency>
    <groupId>io.nats</groupId>
    <artifactId>jnats-server-runner</artifactId>
    <version>3.1.0</version>
</dependency>
```

If you need the absolute latest, before it propagates to maven central, you can use the repository:

```xml
<repositories>
    <repository>
        <id>sonatype releases</id>
        <url>https://repo1.maven.org/maven2/</url>
        <releases>
           <enabled>true</enabled>
        </releases>
    </repository>
</repositories>
```

If you need a snapshot version, you must enable snapshots and change your dependency.

```xml
<repositories>
    <repository>
        <id>sonatype snapshots</id>
        <url>https://central.sonatype.com/repository/maven-snapshots</url>
        <snapshots>
            <enabled>true</enabled>
        </snapshots>
    </repository>
</repositories>

<dependency>
    <groupId>io.nats</groupId>
    <artifactId>jnats-server-runner</artifactId>
    <version>3.1.1-SNAPSHOT</version>
</dependency>
```

## License

Unless otherwise noted, the NATS source files are distributed
under the Apache Version 2.0 license found in the LICENSE file.
