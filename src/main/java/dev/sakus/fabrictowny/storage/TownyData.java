package dev.sakus.fabrictowny.storage;
import dev.sakus.fabrictowny.model.*;
import java.util.*;
public final class TownyData {
  public int schemaVersion = TownyDataSanitizer.CURRENT_SCHEMA;
  public Map<UUID, Resident> residents = new LinkedHashMap<>();
  public Map<String, Town> towns = new LinkedHashMap<>();
  public Map<String, Nation> nations = new LinkedHashMap<>();
  public Map<String, TownBlock> blocks = new LinkedHashMap<>();
  public Map<String, Invitation> invitations = new LinkedHashMap<>();
  public Map<String, War> wars = new LinkedHashMap<>();
  public long nextNewDayAt;
}
