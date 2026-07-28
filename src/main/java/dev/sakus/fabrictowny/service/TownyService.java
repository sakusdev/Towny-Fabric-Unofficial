package dev.sakus.fabrictowny.service;

import dev.sakus.fabrictowny.economy.EconomyProvider;
import dev.sakus.fabrictowny.model.*;
import dev.sakus.fabrictowny.storage.TownyStore;

import java.util.*;

public final class TownyService {
  private final TownyStore store;
  private final EconomyProvider economy;
  private final TownyConfig config;

  public TownyService(TownyStore store, EconomyProvider economy, TownyConfig config) {
    this.store = Objects.requireNonNull(store);
    this.economy = Objects.requireNonNull(economy);
    this.config = Objects.requireNonNull(config);
  }

  public TownyStore store() { return store; }
  public TownyConfig config() { return config; }
  public EconomyProvider economy() { return economy; }

  public Resident resident(UUID id, String name) {
    Objects.requireNonNull(id, "id");
    var resident = store.data().residents.computeIfAbsent(id, key -> new Resident(id, safeName(name, id.toString())));
    resident.lastKnownName = safeName(name, resident.lastKnownName);
    resident.lastOnline = System.currentTimeMillis();
    return resident;
  }

  public Optional<Town> town(UUID player) {
    var resident = store.data().residents.get(player);
    return resident == null || resident.townId == null ? Optional.empty() : Optional.ofNullable(store.data().towns.get(resident.townId));
  }

  public Optional<Town> townByName(String name) {
    if (name == null) return Optional.empty();
    return store.data().towns.values().stream().filter(town -> town.name != null && town.name.equalsIgnoreCase(name)).findFirst();
  }

  public Optional<Nation> nationByName(String name) {
    if (name == null) return Optional.empty();
    return store.data().nations.values().stream().filter(nation -> nation.name != null && nation.name.equalsIgnoreCase(name)).findFirst();
  }

  public Optional<TownBlock> block(String key) { return Optional.ofNullable(store.data().blocks.get(key)); }

  public synchronized Town createTown(UUID player, String playerName, String requestedName, String homeKey) {
    String name = requireName(requestedName, "町名");
    if (homeKey == null || homeKey.isBlank()) throw new IllegalArgumentException("ホームブロックが不正です。");
    resident(player, playerName);
    if (town(player).isPresent()) throw new IllegalStateException("すでに町に所属しています。");
    if (townByName(name).isPresent()) throw new IllegalStateException("その町名は使用済みです。");
    if (store.data().blocks.containsKey(homeKey)) throw new IllegalStateException("この土地はすでに領有されています。");
    charge(player, config.townCreationCost, "town-create", "町作成費用が不足しています。");

    try {
      String id = uniqueTownId(name);
      Town town = new Town(id, name, player);
      town.homeBlock = homeKey;
      town.blocks.add(homeKey);
      store.data().towns.put(id, town);
      store.data().blocks.put(homeKey, new TownBlock(homeKey, id));
      resident(player, playerName).townId = id;
      store.save();
      return town;
    } catch (RuntimeException exception) {
      economy.deposit(player, config.townCreationCost, "town-create-refund");
      throw exception;
    }
  }

  public synchronized void deleteTown(Town town) {
    Objects.requireNonNull(town, "town");
    if (town.nationId != null) {
      var nation = store.data().nations.get(town.nationId);
      if (nation != null) {
        nation.towns.remove(town.id);
        if (Objects.equals(nation.capitalTownId, town.id)) deleteNationInternal(nation);
      }
    }
    for (UUID id : new HashSet<>(town.residents)) {
      var resident = store.data().residents.get(id);
      if (resident != null) resident.townId = null;
    }
    new HashSet<>(town.blocks).forEach(store.data().blocks::remove);
    store.data().towns.remove(town.id);
    store.save();
  }

  public synchronized void deleteNation(Nation nation) {
    deleteNationInternal(Objects.requireNonNull(nation));
    store.save();
  }

  private void deleteNationInternal(Nation nation) {
    for (String townId : new HashSet<>(nation.towns)) {
      var town = store.data().towns.get(townId);
      if (town != null) town.nationId = null;
    }
    store.data().nations.remove(nation.id);
  }

  public boolean canClaim(Town town) {
    return town.blocks.size() < config.baseTownBlocks + town.residents.size() * config.blocksPerResident;
  }

  public synchronized void claim(UUID actor, Town town, String key, boolean outpost) {
    Objects.requireNonNull(actor, "actor");
    Objects.requireNonNull(town, "town");
    if (key == null || parse(key) == null) throw new IllegalArgumentException("チャンク識別子が不正です。");
    if (store.data().blocks.containsKey(key)) throw new IllegalStateException("領有済みです。");
    if (!canClaim(town)) throw new IllegalStateException("領有可能数を超えています。");
    if (!outpost && config.requireAdjacentClaims && !adjacent(town, key)) throw new IllegalStateException("既存領地に隣接していません。");
    if (outpost && outpostCount(town) >= config.maxOutposts) throw new IllegalStateException("前哨地の上限に達しています。");

    double cost = outpost ? config.outpostCost : config.claimCost;
    charge(actor, cost, outpost ? "town-outpost" : "town-claim", "領有費用が不足しています。");
    try {
      TownBlock block = new TownBlock(key, town.id);
      block.outpost = outpost;
      town.blocks.add(key);
      store.data().blocks.put(key, block);
      store.save();
    } catch (RuntimeException exception) {
      economy.deposit(actor, cost, "town-claim-refund");
      throw exception;
    }
  }

  /** Compatibility overload for older callers; charges the mayor. */
  public void claim(Town town, String key, boolean outpost) { claim(town.mayor, town, key, outpost); }

  public synchronized void unclaim(Town town, String key) {
    TownBlock block = store.data().blocks.get(key);
    if (block == null || !town.id.equals(block.townId)) throw new IllegalStateException("自分の町の領地ではありません。");
    if (key.equals(town.homeBlock)) throw new IllegalStateException("ホームブロックは解除できません。");
    town.blocks.remove(key);
    store.data().blocks.remove(key);
    store.save();
  }

  public synchronized Nation createNation(UUID actor, Town capital, String requestedName) {
    String name = requireName(requestedName, "国家名");
    if (capital.nationId != null) throw new IllegalStateException("すでに国家所属です。");
    if (nationByName(name).isPresent()) throw new IllegalStateException("その国家名は使用済みです。");
    charge(actor, config.nationCreationCost, "nation-create", "国家作成費用が不足しています。");
    try {
      String id = uniqueNationId(name);
      Nation nation = new Nation(id, name, capital.id);
      store.data().nations.put(id, nation);
      capital.nationId = id;
      store.save();
      return nation;
    } catch (RuntimeException exception) {
      economy.deposit(actor, config.nationCreationCost, "nation-create-refund");
      throw exception;
    }
  }

  /** Compatibility overload for older callers; charges the capital mayor. */
  public Nation createNation(Town capital, String name) { return createNation(capital.mayor, capital, name); }

  public synchronized void joinTown(UUID player, String playerName, Town town) {
    if (town(player).isPresent()) throw new IllegalStateException("すでに町に所属しています。");
    town.residents.add(player);
    resident(player, playerName).townId = town.id;
    store.save();
  }

  public synchronized void leaveTown(UUID player) {
    Town town = town(player).orElseThrow(() -> new IllegalStateException("町に所属していません。"));
    if (Objects.equals(town.mayor, player)) throw new IllegalStateException("町長は辞任または町削除が必要です。");
    town.residents.remove(player);
    var resident = store.data().residents.get(player);
    if (resident != null) resident.townId = null;
    for (var block : store.data().blocks.values()) if (player.equals(block.owner)) block.owner = null;
    store.save();
  }

  public PermissionGroup group(Town ownerTown, UUID actor) {
    var actorTown = town(actor).orElse(null);
    if (actorTown != null && actorTown.id.equals(ownerTown.id)) return PermissionGroup.RESIDENT;
    if (ownerTown.nationId != null && actorTown != null && ownerTown.nationId.equals(actorTown.nationId)) return PermissionGroup.NATION;
    if (ownerTown.nationId != null && actorTown != null && actorTown.nationId != null) {
      var nation = store.data().nations.get(ownerTown.nationId);
      if (nation != null && nation.allies.contains(actorTown.nationId)) return PermissionGroup.ALLY;
    }
    return PermissionGroup.OUTSIDER;
  }

  private int outpostCount(Town town) {
    int count = 0;
    for (String key : town.blocks) {
      TownBlock block = store.data().blocks.get(key);
      if (block != null && block.outpost) count++;
    }
    return count;
  }

  private boolean adjacent(Town town, String key) {
    String[] parsed = parse(key);
    if (parsed == null) return false;
    int x;
    int z;
    try {
      x = Integer.parseInt(parsed[1]);
      z = Integer.parseInt(parsed[2]);
    } catch (NumberFormatException ignored) {
      return false;
    }
    for (int[] offset : new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
      if (town.blocks.contains(parsed[0] + ":" + (x + offset[0]) + ":" + (z + offset[1]))) return true;
    }
    return false;
  }

  private static String[] parse(String key) {
    if (key == null) return null;
    int last = key.lastIndexOf(':');
    int previous = key.lastIndexOf(':', last - 1);
    return last < 0 || previous < 0 ? null : new String[]{key.substring(0, previous), key.substring(previous + 1, last), key.substring(last + 1)};
  }

  private String uniqueTownId(String name) { return uniqueId(slug(name), store.data().towns.keySet()); }
  private String uniqueNationId(String name) { return uniqueId(slug(name), store.data().nations.keySet()); }

  private static String uniqueId(String base, Set<String> existing) {
    if (!existing.contains(base)) return base;
    for (int suffix = 2; suffix < Integer.MAX_VALUE; suffix++) {
      String candidate = base + "_" + suffix;
      if (!existing.contains(candidate)) return candidate;
    }
    throw new IllegalStateException("IDを生成できませんでした。");
  }

  private static String slug(String name) {
    String slug = name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_-]", "_").replaceAll("_+", "_");
    if (slug.isBlank() || slug.equals("_")) slug = "town";
    return slug + "_" + Integer.toUnsignedString(name.hashCode(), 36);
  }

  private static String requireName(String value, String label) {
    if (value == null) throw new IllegalArgumentException(label + "を入力してください。");
    String normalized = value.strip();
    if (normalized.isEmpty()) throw new IllegalArgumentException(label + "を入力してください。");
    if (normalized.length() > 32) throw new IllegalArgumentException(label + "は32文字以内にしてください。");
    if (normalized.chars().anyMatch(Character::isISOControl)) throw new IllegalArgumentException(label + "に制御文字は使用できません。");
    return normalized;
  }

  private static String safeName(String value, String fallback) {
    return value == null || value.isBlank() ? fallback : value;
  }

  private void charge(UUID actor, double amount, String reason, String insufficientMessage) {
    if (amount <= 0) return;
    if (!economy.withdraw(actor, amount, reason)) throw new IllegalStateException(insufficientMessage);
  }
}
