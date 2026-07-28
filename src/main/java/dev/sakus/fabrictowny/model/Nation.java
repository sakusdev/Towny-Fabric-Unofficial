package dev.sakus.fabrictowny.model;
import java.util.*;
public final class Nation {
  public String id;
  public String name;
  public String capitalTownId;
  public Set<String> towns = new HashSet<>();
  public Set<String> allies = new HashSet<>();
  public Set<String> enemies = new HashSet<>();
  public Map<String, Set<UUID>> ranks = new HashMap<>();
  public double bank;
  public double taxes;
  public boolean taxPercent;
  public double upkeep;
  public boolean open;
  public boolean publicSpawn;
  public long foundedAt = System.currentTimeMillis();
  public Nation() {}
  public Nation(String id, String name, String capitalTownId) { this.id=id; this.name=name; this.capitalTownId=capitalTownId; towns.add(capitalTownId); }
}
