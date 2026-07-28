package dev.sakus.fabrictowny;

import dev.sakus.fabrictowny.model.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PermissionMatrixTest {
  @Test void defaultsAreResidentOnly() {
    PermissionMatrix matrix = new PermissionMatrix();
    for (PermissionAction action : PermissionAction.values()) {
      assertTrue(matrix.allows(PermissionGroup.RESIDENT, action));
      assertFalse(matrix.allows(PermissionGroup.ALLY, action));
      assertFalse(matrix.allows(PermissionGroup.OUTSIDER, action));
    }
  }

  @Test void normalizeRepairsMissingMaps() {
    PermissionMatrix matrix = new PermissionMatrix();
    matrix.values = null;
    matrix.normalize();
    assertTrue(matrix.allows(PermissionGroup.RESIDENT, PermissionAction.BUILD));
    assertFalse(matrix.allows(PermissionGroup.OUTSIDER, PermissionAction.BUILD));
  }
}
