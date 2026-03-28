# 𓁟 Thoth Tool — Scratch Pad

A lightweight plugin tool for Thoth agents to keep temporary notes during complex tasks. Notes appear in the system prompt on subsequent turns, helping the agent remember important context, decisions, and findings.

## Requirements

- Java 25+ (JDK)
- Thoth 0.1.0+ (with plugin system)

### 1. Configure Thoth

Add the tool to your `thoth.conf`:

```json
{
  ...,
  "tool": [
    {
      "name": "scratch-pad",
      "version": "0.1.0-SNAPSHOT",
      "enabled": true
    },
    ...
  ]
}
```

### 2. Place the JAR

Copy `build/libs/scratch-pad-0.1.0-SNAPSHOT.jar` into Thoth's plugin directory `providers/`.

## Usage

Once installed and configured, Thoth will automatically discover and load the Scratch Pad tool. You can verify it's active via Thoth's monitoring socket or logs.

## License

GNU General Public License v3.0 — see [LICENSE](LICENSE).
