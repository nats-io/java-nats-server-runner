![NATS](src/main/javadoc/images/large-logo.png)

# Java Nats Server Runner

Run the [NATS messaging system](https://nats.io) Server from your Java code. 

**Current Release**: 2.0.1 &nbsp; **Current Snapshot**: 2.0.2-SNAPSHOT

[![License Apache 2](https://img.shields.io/badge/License-Apache2-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.nats/jnats-server-runner/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.nats/jnats-server-runner)
[![Javadoc](http://javadoc.io/badge/io.nats/jnats-server-runner.svg?branch=main)](http://javadoc.io/doc/io.nats/jnats-server-runner?branch=main)
[![Coverage Status](https://coveralls.io/repos/github/nats-io/java-nats-server-runner/badge.svg?branch=main)](https://coveralls.io/github/nats-io/java-nats-server-runner?branch=main)
[![Build Main Badge](https://github.com/nats-io/java-nats-server-runner/actions/workflows/build-main.yml/badge.svg?event=push)](https://github.com/nats-io/java-nats-server-runner/actions/workflows/build-main.yml)
[![Release Badge](https://github.com/nats-io/java-nats-server-runner/actions/workflows/build-release.yml/badge.svg?event=release)](https://github.com/nats-io/java-nats-server-runner/actions/workflows/build-release.yml)

Useful for running unit or integration tests on the localhost.

### Executable

By default, the server is found in your path in this order: 
- the `executablePath` set in the builder 
- the path found in the `nats_server_path` environment variable
- the path set via `NatsRunnerUtils.setServerPath` (deprecated, prefer setting in builder)
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
There are 3 methods available:

```java
static {
    NatsServerRunner.setDefaultOutputSupplier(ConsoleOutput::new);
    NatsServerRunner.setDefaultOutputLevel(Level.WARNING);
    NatsServerRunner.setPreferredServerPath("/path/to/nats-server");
}
```

## License

Unless otherwise noted, the NATS source files are distributed
under the Apache Version 2.0 license found in the LICENSE file.
