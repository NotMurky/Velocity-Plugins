# Velocity-Plugins

Velocity Plugins I've made for my own servers.

---

## CommandTimings

**Version:** 1.0-SNAPSHOT  
**Platform:** Velocity Proxy  

CommandTimings allows server administrators to schedule commands to run automatically at specific times each day. Commands are executed on the Velocity console with logging to confirm execution.

---

## Features

- Schedule commands by hour and minute in a simple config file (`plugins/commandstimings/config.txt`).  
- Automatically creates a default config file if none exists.  
- Executes commands daily at the specified times.  
- Logs successful and failed command executions.

---

## Installation

1. Place the JAR file in your Velocity server's `plugins` folder.  
2. Start the server. On first run, a default config will be generated:  
