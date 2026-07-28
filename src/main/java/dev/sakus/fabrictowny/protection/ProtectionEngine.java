package dev.sakus.fabrictowny.protection;

import dev.sakus.fabrictowny.model.*;
import dev.sakus.fabrictowny.service.TownyService;

import java.util.UUID;

public final class ProtectionEngine {
  private final TownyService service;

  public ProtectionEngine(TownyService service) { this.service = service; }

  public boolean allowed(UUID actor, String key, PermissionAction action, boolean explicitAdmin) {
    if (explicitAdmin || hasBypass(actor)) return true;
    TownBlock block = service.store().data().blocks.get(key);
    if (block == null) return service.config().wildernessBuild;
    Town owner = service.store().data().towns.get(block.townId);
    if (owner == null) return true;
    if (block.owner != null && block.owner.equals(actor)) return true;
    PermissionGroup group = service.group(owner, actor);
    // Plot permissions override town permissions instead of broadening them.
    if (block.owner != null || block.type != PlotType.DEFAULT) return block.permissions.allows(group, action);
    return owner.permissions.allows(group, action);
  }

  public boolean pvp(UUID attacker, UUID victim, String key, boolean explicitAdmin) {
    if (explicitAdmin || hasBypass(attacker)) return true;
    TownBlock block = service.store().data().blocks.get(key);
    if (block == null) return true;
    Town town = service.store().data().towns.get(block.townId);
    if (town == null) return true;
    if (!service.config().allowFriendlyFire) {
      var attackerTown = service.town(attacker).orElse(null);
      var victimTown = service.town(victim).orElse(null);
      if (attackerTown != null && victimTown != null && (attackerTown.id.equals(victimTown.id)
          || (attackerTown.nationId != null && attackerTown.nationId.equals(victimTown.nationId)))) return false;
    }
    return block.pvp || town.pvp;
  }

  private boolean hasBypass(UUID actor) {
    if (actor == null) return false;
    Resident resident = service.store().data().residents.get(actor);
    return resident != null && resident.adminBypass;
  }
}
