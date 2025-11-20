package io.nats;

import java.nio.file.Path;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NatsServerRunnerOptionsImpl implements NatsServerRunnerOptions {
    private final Integer port;
    private final DebugLevel debugLevel;
    private final boolean jetstream;
    private final Path configFilePath;
    private final List<String> configInserts;
    private final List<String> customArgs;
    private final Path executablePath;
    private final Logger logger;
    private final Level logLevel;

    public NatsServerRunnerOptionsImpl(Integer port,
                                       DebugLevel debugLevel,
                                       boolean jetstream,
                                       Path configFilePath,
                                       List<String> configInserts,
                                       List<String> customArgs,
                                       Path executablePath,
                                       Logger logger,
                                       Level logLevel) {
        this.port = port;
        this.debugLevel = debugLevel;
        this.jetstream = jetstream;
        this.configFilePath = configFilePath;
        this.configInserts = configInserts;
        this.customArgs = customArgs;
        this.executablePath = executablePath;
        this.logger = logger;
        this.logLevel = logLevel;
    }

    @Override
    public Integer port() {
        return port;
    }

    @Override
    public DebugLevel debugLevel() {
        return debugLevel;
    }

    @Override
    public boolean jetStream() {
        return jetstream;
    }

    @Override
    public Path configFilePath() {
        return configFilePath;
    }

    @Override
    public List<String> configInserts() {
        return configInserts;
    }

    @Override
    public List<String> customArgs() {
        return customArgs;
    }

    @Override
    public Path executablePath() {
        return executablePath;
    }

    @Override
    public Logger logger() {
        return logger;
    }

    @Override
    public Level logLevel() {
        return logLevel;
    }
}
