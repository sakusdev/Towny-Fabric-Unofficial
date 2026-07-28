package dev.sakus.fabrictowny.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class TownyConfig {
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
  private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("fabric-towny/config.json");

  public double townCreationCost = 0;
  public double nationCreationCost = 0;
  public double claimCost = 0;
  public double outpostCost = 0;
  public int baseTownBlocks = 8;
  public int blocksPerResident = 8;
  public int maxOutposts = 3;
  public long inviteLifetimeMillis = 300_000;
  public long newDayIntervalMillis = 86_400_000;
  public boolean requireAdjacentClaims = true;
  public boolean allowFriendlyFire = false;
  public boolean wildernessBuild = true;

  public static TownyConfig load() {
    try {
      Files.createDirectories(FILE.getParent());
      if (!Files.exists(FILE)) {
        TownyConfig defaults = new TownyConfig();
        defaults.validate();
        write(defaults);
        return defaults;
      }

      try (Reader reader = Files.newBufferedReader(FILE)) {
        TownyConfig loaded = GSON.fromJson(reader, TownyConfig.class);
        if (loaded == null) loaded = new TownyConfig();
        loaded.validate();
        return loaded;
      }
    } catch (IOException | RuntimeException exception) {
      throw new IllegalStateException("Could not load Towny config from " + FILE, exception);
    }
  }

  public void save() {
    validate();
    try {
      write(this);
    } catch (IOException exception) {
      throw new IllegalStateException("Could not save Towny config to " + FILE, exception);
    }
  }

  private void validate() {
    townCreationCost = nonNegative(townCreationCost, "townCreationCost");
    nationCreationCost = nonNegative(nationCreationCost, "nationCreationCost");
    claimCost = nonNegative(claimCost, "claimCost");
    outpostCost = nonNegative(outpostCost, "outpostCost");
    if (baseTownBlocks < 1) throw new IllegalArgumentException("baseTownBlocks must be at least 1");
    if (blocksPerResident < 0) throw new IllegalArgumentException("blocksPerResident must be non-negative");
    if (maxOutposts < 0) throw new IllegalArgumentException("maxOutposts must be non-negative");
    if (inviteLifetimeMillis < 1_000) throw new IllegalArgumentException("inviteLifetimeMillis must be at least 1000");
    if (newDayIntervalMillis < 60_000) throw new IllegalArgumentException("newDayIntervalMillis must be at least 60000");
  }

  private static double nonNegative(double value, String name) {
    if (!Double.isFinite(value) || value < 0) throw new IllegalArgumentException(name + " must be finite and non-negative");
    return value;
  }

  private static void write(TownyConfig config) throws IOException {
    Path temporary = FILE.resolveSibling(FILE.getFileName() + ".tmp");
    try (Writer writer = Files.newBufferedWriter(temporary)) {
      GSON.toJson(config, writer);
    }
    try {
      Files.move(temporary, FILE, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    } catch (java.nio.file.AtomicMoveNotSupportedException ignored) {
      Files.move(temporary, FILE, StandardCopyOption.REPLACE_EXISTING);
    }
  }
}
