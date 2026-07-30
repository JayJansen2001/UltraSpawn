# UltraSpawn

UltraSpawn is een zelfstandige spawn- en lobbyplugin voor Paper-netwerken met Velocity- of BungeeCord-proxy.

## Functies

- Lokale spawn per backendserver
- Automatische join-teleport
- `/spawn`, `/hub`, `/lobby`, `/setspawn`
- Countdown, bewegings- en schadeannulering, cooldown
- Instelbare berichten, sounds, particles en titles
- `/ultraspawn help|info|reload|version`
- Permissiegestuurde tab-completion
- GitHub Releases-updatechecker

## Vereisten

- Java 21
- Paper 1.21.4 of nieuwer
- Optioneel Velocity of BungeeCord

## Bouwen

```bash
mvn clean package
```

De JAR verschijnt in `target/UltraSpawn-1.0.0.jar`.

## Installatie

1. Plaats de JAR in de map `plugins` van iedere Paper-backendserver.
2. Start iedere server eenmaal.
3. Stel per server `server.name` in `plugins/UltraSpawn/config.yml` in.
4. Voer op iedere server `/setspawn` uit.
5. Zet `join.teleport-to-spawn` op `true` waar spelers na binnenkomst altijd naar de lokale spawn moeten.

### Velocity

Zet in `velocity.toml` de BungeeCord-pluginmessagecompatibiliteit aan:

```toml
bungee-plugin-message-channel = true
```

De waarde `network.lobby-server` moet exact gelijk zijn aan de lobbyservernaam in Velocity.

### BungeeCord

Er is geen aparte proxyplugin nodig. UltraSpawn gebruikt het ingebouwde `BungeeCord` plugin messaging-kanaal.

## Gedrag bij `/server skywars`

Velocity/BungeeCord verbindt de speler met `skywars`. De UltraSpawn-JAR op die Paper-server ontvangt daarna de normale join en teleporteert de speler naar de lokale spawn wanneer `join.teleport-to-spawn: true` staat.

## GitHub-updatechecker

Vul in `config.yml` in:

```yaml
update-checker:
  github:
    owner: "jouw-github-naam"
    repository: "UltraSpawn"
```

Publiceer versies als echte GitHub Releases met tags zoals `v1.0.0` en `v1.1.0`. Alleen een losse Git-tag is niet genoeg voor het endpoint `releases/latest`.

## Releases maken

1. Bouw de JAR met Maven of download hem uit de GitHub Actions-artifacts.
2. Open de repositorypagina **Releases**.
3. Maak een nieuwe release met tag `v1.0.0`.
4. Upload `UltraSpawn-1.0.0.jar` als release asset.
5. Publiceer de release.

## Belangrijk

`/hub` en `/lobby` worden vanaf een backendserver via plugin messaging uitgevoerd. Er moet daarom een speler online zijn die het bericht draagt; dat is automatisch het geval omdat de speler zelf het commando uitvoert.
