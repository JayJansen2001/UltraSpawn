package nl.ultraspawn.update;

import nl.ultraspawn.UltraSpawnPlugin;
import nl.ultraspawn.config.MessageManager;
import org.bukkit.entity.Player;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class UpdateChecker {
    private static final Pattern TAG = Pattern.compile("\\\"tag_name\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"");
    private static final Pattern URL = Pattern.compile("\\\"html_url\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"");
    private static final Pattern PRERELEASE = Pattern.compile("\\\"prerelease\\\"\\s*:\\s*(true|false)");

    private final UltraSpawnPlugin plugin;
    private final MessageManager messages;
    private volatile Result result = Result.unknown();

    public UpdateChecker(UltraSpawnPlugin plugin, MessageManager messages) {
        this.plugin = plugin;
        this.messages = messages;
    }

    public void reload() {
        result = Result.unknown();
        if (plugin.getConfig().getBoolean("update-checker.enabled", true)) checkAsync();
    }

    public void checkAsync() {
        if (!plugin.getConfig().getBoolean("update-checker.enabled", true)) return;
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, this::checkNow);
    }

    private void checkNow() {
        String owner = plugin.getConfig().getString("update-checker.github.owner", "OWNER");
        String repo = plugin.getConfig().getString("update-checker.github.repository", "UltraSpawn");
        if (owner == null || owner.isBlank() || owner.equalsIgnoreCase("OWNER") || repo == null || repo.isBlank()) {
            result = Result.unknown();
            plugin.getLogger().info("Updatechecker overgeslagen: vul GitHub owner/repository in config.yml in.");
            return;
        }

        int timeout = Math.max(2, plugin.getConfig().getInt("update-checker.timeout-seconds", 5));
        URI uri = URI.create("https://api.github.com/repos/" + owner + "/" + repo + "/releases/latest");
        try {
            HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(timeout)).build();
            HttpRequest request = HttpRequest.newBuilder(uri)
                    .timeout(Duration.ofSeconds(timeout))
                    .header("Accept", "application/vnd.github+json")
                    .header("X-GitHub-Api-Version", "2022-11-28")
                    .header("User-Agent", "UltraSpawn-UpdateChecker")
                    .GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                result = Result.unknown();
                plugin.getLogger().warning("GitHub updatecheck gaf HTTP " + response.statusCode() + ".");
                return;
            }

            String json = response.body();
            String latest = find(TAG, json).orElse("");
            String url = find(URL, json).orElse("https://github.com/" + owner + "/" + repo + "/releases");
            boolean prerelease = find(PRERELEASE, json).map(Boolean::parseBoolean).orElse(false);
            if (latest.isBlank() || (prerelease && !plugin.getConfig().getBoolean("update-checker.include-prereleases", false))) {
                result = Result.unknown();
                return;
            }

            String current = plugin.getDescription().getVersion();
            boolean available = compareVersions(normalize(latest), normalize(current)) > 0;
            result = new Result(true, available, current, latest, url);
            if (available && plugin.getConfig().getBoolean("update-checker.notify-console", true)) {
                plugin.getLogger().warning("Nieuwe UltraSpawn-versie beschikbaar: " + current + " -> " + latest);
                plugin.getLogger().warning(url);
            }
        } catch (Exception ex) {
            result = Result.unknown();
            plugin.getLogger().warning("Updatecontrole mislukt: " + ex.getMessage());
        }
    }

    public void notifyPlayer(Player player) {
        Result current = result;
        if (current.checked && current.available) {
            messages.send(player, "update.available", Map.of(
                    "current", current.current, "latest", current.latest, "url", current.url));
        }
    }

    public String statusMessage() {
        Result current = result;
        if (!current.checked) return messages.prefixed("update.unavailable");
        if (current.available) return messages.prefixed("update.available", Map.of(
                "current", current.current, "latest", current.latest, "url", current.url));
        return messages.prefixed("update.latest", Map.of("current", current.current));
    }

    private static Optional<String> find(Pattern pattern, String input) {
        Matcher matcher = pattern.matcher(input);
        return matcher.find() ? Optional.of(matcher.group(1)) : Optional.empty();
    }

    private static String normalize(String version) {
        return version.trim().replaceFirst("^[vV]", "").split("[-+]", 2)[0];
    }

    private static int compareVersions(String a, String b) {
        String[] left = a.split("\\.");
        String[] right = b.split("\\.");
        int length = Math.max(left.length, right.length);
        for (int i = 0; i < length; i++) {
            int x = i < left.length ? parsePart(left[i]) : 0;
            int y = i < right.length ? parsePart(right[i]) : 0;
            if (x != y) return Integer.compare(x, y);
        }
        return 0;
    }

    private static int parsePart(String value) {
        try { return Integer.parseInt(value.replaceAll("\\D.*$", "")); }
        catch (NumberFormatException ignored) { return 0; }
    }

    private record Result(boolean checked, boolean available, String current, String latest, String url) {
        static Result unknown() { return new Result(false, false, "", "", ""); }
    }
}
