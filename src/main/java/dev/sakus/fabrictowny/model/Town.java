package dev.sakus.fabrictowny.model;
import java.util.*;
public final class Town {
  public String id;
  public String name;
  public UUID mayor;
  public Set<UUID> residents = new HashSet<>();
  public Set<String> blocks = new HashSet<>();
  public Set<UUID> outlaws = new HashSet<>();
  public Set<UUID> trusted = new HashSet<>();
  public Map<String, Set<UUID>> ranks = new HashMap<>();
  public String nationId;
  public String homeBlock;
  public String spawnDimension;
  public double spawnX, spawnY, spawnZ;
  public float spawnYaw, spawnPitch;
  public double bank;
  public double taxes;
  public boolean taxPercent;
  public double plotTax;
  public double upkeep;
  public boolean open;
  public boolean publicSpawn;
  public boolean pvp;
  public boolean explosion;
  public boolean fire;
  public boolean mobs = true;
  public boolean bankrupt;
  public boolean forSale;
  public double salePrice;
  public PermissionMatrix permissions = new PermissionMatrix();
  public long foundedAt = System.currentTimeMillis();
  public Town() {}
  public Town(String id, String name, UUID mayor) { this.id=id; this.name=name; this.mayor=mayor; residents.add(mayor); }
  public boolean isMember(UUID id) { return residents.contains(id); }
  public boolean hasRank(UUID id, String rank) { return Objects.equals(mayor, id) || (rank != null && ranks.getOrDefault(rank.toLowerCase(Locale.ROOT), Set.of()).contains(id)); }
}
