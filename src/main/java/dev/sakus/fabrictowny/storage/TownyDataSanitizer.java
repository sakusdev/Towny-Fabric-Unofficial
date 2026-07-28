package dev.sakus.fabrictowny.storage;

import dev.sakus.fabrictowny.model.*;

import java.util.*;

/** Normalizes persisted data so old or manually edited files cannot crash the server. */
public final class TownyDataSanitizer {
  public static final int CURRENT_SCHEMA = 3;

  private TownyDataSanitizer() {}

  public static TownyData sanitize(TownyData data) {
    final TownyData normalizedData = data == null ? new TownyData() : data;
    data = normalizedData;
    data.residents = linked(data.residents);
    data.towns = linked(data.towns);
    data.nations = linked(data.nations);
    data.blocks = linked(data.blocks);
    data.invitations = linked(data.invitations);
    data.wars = linked(data.wars);

    data.residents.entrySet().removeIf(entry -> entry.getKey() == null || entry.getValue() == null);
    for (Map.Entry<UUID, Resident> entry : data.residents.entrySet()) {
      Resident resident = entry.getValue();
      resident.id = entry.getKey();
      resident.friends = set(resident.friends);
      resident.townRanks = set(resident.townRanks);
      resident.nationRanks = set(resident.nationRanks);
      resident.title = string(resident.title);
      resident.surname = string(resident.surname);
      if (resident.plotDefaults == null) resident.plotDefaults = new PermissionMatrix();
      resident.plotDefaults.normalize();
      resident.balance = finiteNonNegative(resident.balance);
    }

    data.towns.entrySet().removeIf(entry -> blank(entry.getKey()) || entry.getValue() == null);
    for (Map.Entry<String, Town> entry : data.towns.entrySet()) {
      Town town = entry.getValue();
      town.id = entry.getKey();
      town.name = blank(town.name) ? town.id : town.name;
      town.residents = set(town.residents);
      town.blocks = set(town.blocks);
      town.outlaws = set(town.outlaws);
      town.trusted = set(town.trusted);
      town.ranks = rankMap(town.ranks);
      if (town.mayor != null) town.residents.add(town.mayor);
      if (town.permissions == null) town.permissions = new PermissionMatrix();
      town.permissions.normalize();
      town.bank = finiteNonNegative(town.bank);
      town.taxes = finiteNonNegative(town.taxes);
      town.plotTax = finiteNonNegative(town.plotTax);
      town.upkeep = finiteNonNegative(town.upkeep);
      town.salePrice = finiteNonNegative(town.salePrice);
    }

    data.nations.entrySet().removeIf(entry -> blank(entry.getKey()) || entry.getValue() == null);
    for (Map.Entry<String, Nation> entry : data.nations.entrySet()) {
      Nation nation = entry.getValue();
      nation.id = entry.getKey();
      nation.name = blank(nation.name) ? nation.id : nation.name;
      nation.towns = set(nation.towns);
      nation.allies = set(nation.allies);
      nation.enemies = set(nation.enemies);
      nation.ranks = rankMap(nation.ranks);
      if (!blank(nation.capitalTownId)) nation.towns.add(nation.capitalTownId);
      nation.allies.remove(nation.id);
      nation.enemies.remove(nation.id);
      nation.allies.removeAll(nation.enemies);
      nation.bank = finiteNonNegative(nation.bank);
      nation.taxes = finiteNonNegative(nation.taxes);
      nation.upkeep = finiteNonNegative(nation.upkeep);
    }

    data.blocks.entrySet().removeIf(entry -> blank(entry.getKey()) || entry.getValue() == null);
    for (Map.Entry<String, TownBlock> entry : data.blocks.entrySet()) {
      TownBlock block = entry.getValue();
      block.key = entry.getKey();
      if (block.type == null) block.type = PlotType.DEFAULT;
      if (block.permissions == null) block.permissions = new PermissionMatrix();
      block.permissions.normalize();
      block.salePrice = finiteNonNegative(block.salePrice);
      block.tax = finiteNonNegative(block.tax);
      Town town = data.towns.get(block.townId);
      if (town != null) town.blocks.add(block.key);
    }

    // Remove references to entities that no longer exist.
    for (Resident resident : data.residents.values()) {
      if (resident.townId != null && !data.towns.containsKey(resident.townId)) resident.townId = null;
    }
    for (Town town : data.towns.values()) {
      town.residents.removeIf(id -> !normalizedData.residents.containsKey(id));
      town.blocks.removeIf(key -> !normalizedData.blocks.containsKey(key));
      if (town.nationId != null && !data.nations.containsKey(town.nationId)) town.nationId = null;
    }
    for (Nation nation : data.nations.values()) {
      nation.towns.removeIf(id -> !normalizedData.towns.containsKey(id));
      nation.allies.removeIf(id -> !normalizedData.nations.containsKey(id));
      nation.enemies.removeIf(id -> !normalizedData.nations.containsKey(id));
    }

    data.schemaVersion = CURRENT_SCHEMA;
    if (data.nextNewDayAt < 0) data.nextNewDayAt = 0;
    return data;
  }

  private static <K, V> Map<K, V> linked(Map<K, V> map) {
    return map == null ? new LinkedHashMap<>() : new LinkedHashMap<>(map);
  }

  private static <T> Set<T> set(Set<T> set) {
    return set == null ? new HashSet<>() : new HashSet<>(set);
  }

  private static Map<String, Set<UUID>> rankMap(Map<String, Set<UUID>> ranks) {
    Map<String, Set<UUID>> normalized = new HashMap<>();
    if (ranks == null) return normalized;
    ranks.forEach((name, members) -> {
      if (!blank(name)) normalized.put(name.toLowerCase(Locale.ROOT), set(members));
    });
    return normalized;
  }

  private static String string(String value) { return value == null ? "" : value; }
  private static boolean blank(String value) { return value == null || value.isBlank(); }
  private static double finiteNonNegative(double value) { return Double.isFinite(value) && value >= 0 ? value : 0; }
}
