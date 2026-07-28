package dev.sakus.fabrictowny.model;
import java.util.*;
public final class Resident {
  public UUID id;
  public String lastKnownName;
  public String townId;
  public double balance;
  public Set<UUID> friends = new HashSet<>();
  public Set<String> townRanks = new HashSet<>();
  public Set<String> nationRanks = new HashSet<>();
  public String title = "";
  public String surname = "";
  public boolean adminBypass;
  public long registeredAt = System.currentTimeMillis();
  public long lastOnline = System.currentTimeMillis();
  public PermissionMatrix plotDefaults = new PermissionMatrix();
  public Resident() {}
  public Resident(UUID id, String name) { this.id=id; this.lastKnownName=name; }
}
