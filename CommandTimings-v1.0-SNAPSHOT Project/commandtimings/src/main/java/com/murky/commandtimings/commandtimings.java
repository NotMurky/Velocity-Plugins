package com.murky.commandtimings;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Plugin(
        id = "commandtimings",
        name = "commandtimings",
        version = "1.0-SNAPSHOT"
)
public class commandtimings {

    private final ProxyServer proxy;
    private final Logger logger;
    private final File configFile;

    @Inject
    public commandtimings(ProxyServer proxy, Logger logger) {
        this.proxy = proxy;
        this.logger = logger;
        this.configFile = new File("plugins/commandstimings/config.txt");
    }

    // Called after the plugin is fully loaded
    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        loadAndSchedule();
    }

    private void loadAndSchedule() {
        if (!configFile.exists()) {
            try {
                configFile.getParentFile().mkdirs();
                Files.write(configFile.toPath(), List.of(
                        "# hour minute command",
                        "0 0 alert Server restarted successfully!",
                        "12 30 alert Hello players! It’s 12:30PM!"
                ));
                logger.info("Created default config at {}", configFile.getAbsolutePath());
            } catch (IOException e) {
                logger.error("Failed to create default config file", e);
                return;
            }
        }

        try {
            List<String> lines = Files.readAllLines(configFile.toPath()).stream()
                    .filter(line -> !line.trim().isEmpty() && !line.startsWith("#"))
                    .collect(Collectors.toList());

            for (String line : lines) {
                String[] parts = line.split(" ", 3);
                if (parts.length < 3) {
                    logger.warn("Invalid line in config: {}", line);
                    continue;
                }

                int hour = Integer.parseInt(parts[0]);
                int minute = Integer.parseInt(parts[1]);
                String command = parts[2];
                scheduleCommand(hour, minute, command);
            }

        } catch (IOException e) {
            logger.error("Error reading config file", e);
        }
    }

    private void scheduleCommand(int hour, int minute, String command) {
        long delay = getInitialDelaySeconds(hour, minute);
        long repeat = 24 * 60 * 60; // repeat every day

        proxy.getScheduler().buildTask(this, () -> executeCommand(command))
                .delay(delay, TimeUnit.SECONDS)
                .repeat(repeat, TimeUnit.SECONDS)
                .schedule();

        logger.info(String.format("Scheduled command '%s' at %02d:%02d daily", command, hour, minute));
    }

    private long getInitialDelaySeconds(int hour, int minute) {
        LocalTime now = LocalTime.now();
        LocalTime target = LocalTime.of(hour, minute);
        long diff = java.time.Duration.between(now, target).getSeconds();
        if (diff < 0) diff += 24 * 60 * 60;
        return diff;
    }

    private void executeCommand(String command) {
        proxy.getCommandManager().executeAsync(proxy.getConsoleCommandSource(), command)
                .thenAccept(result -> {
                    if (result) {
                        logger.info("[Scheduler] Successfully executed '{}'", command);
                    } else {
                        logger.warn("[Scheduler] Failed to execute '{}'", command);
                    }
                });
    }
}
