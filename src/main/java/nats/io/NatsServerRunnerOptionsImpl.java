package nats.io;

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

    public NatsServerRunnerOptionsImpl(NatsServerRunner.Builder builder) {
        this.port = builder.ports.get(NatsRunnerUtils.CONFIG_PORT_KEY);
        this.debugLevel = builder.debugLevel;
        this.jetstream = builder.jetstream;
        this.configFilePath = builder.configFilePath;
        this.configInserts = builder.configInserts;
        this.customArgs = builder.customArgs;
        this.executablePath = builder.executablePath;
        this.logLevel = builder.outputLevel;
        this.logger = builder.output == null ? null : builder.output.getLogger();
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
