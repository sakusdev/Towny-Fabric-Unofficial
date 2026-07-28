package dev.sakus.fabrictowny;

import dev.sakus.fabrictowny.model.*;
import dev.sakus.fabrictowny.storage.*;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TownyDataSanitizerTest {
  @Test void repairsReferencesAndInvalidNumbers() {
    UUID mayor = UUID.randomUUID();
    TownyData data = new TownyData();
    Resident resident = new Resident(mayor, "Mayor");
    resident.balance = Double.NaN;
    data.residents.put(mayor, resident);

    Town town = new Town("alpha", "Alpha", mayor);
    town.bank = -10;
    town.permissions = null;
    data.towns.put("alpha", town);

    TownBlock block = new TownBlock("minecraft:overworld:0:0", "alpha");
    block.salePrice = Double.POSITIVE_INFINITY;
    data.blocks.put(block.key, block);

    TownyData sanitized = TownyDataSanitizer.sanitize(data);
    assertEquals(TownyDataSanitizer.CURRENT_SCHEMA, sanitized.schemaVersion);
    assertEquals(0, sanitized.residents.get(mayor).balance);
    assertEquals(0, sanitized.towns.get("alpha").bank);
    assertTrue(sanitized.towns.get("alpha").residents.contains(mayor));
    assertTrue(sanitized.towns.get("alpha").blocks.contains(block.key));
    assertNotNull(sanitized.towns.get("alpha").permissions);
    assertEquals(0, sanitized.blocks.get(block.key).salePrice);
  }

  @Test void removesDanglingMemberships() {
    TownyData data = new TownyData();
    Resident resident = new Resident(UUID.randomUUID(), "Lost");
    resident.townId = "missing";
    data.residents.put(resident.id, resident);
    TownyDataSanitizer.sanitize(data);
    assertNull(resident.townId);
  }
}
