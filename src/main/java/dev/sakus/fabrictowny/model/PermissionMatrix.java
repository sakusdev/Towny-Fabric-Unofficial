package dev.sakus.fabrictowny.model;

import java.util.EnumMap;
import java.util.Map;

public final class PermissionMatrix {
  public Map<PermissionGroup, Map<PermissionAction, Boolean>> values = new EnumMap<>(PermissionGroup.class);

  public PermissionMatrix() {
    resetDefaults();
  }

  public void resetDefaults() {
    values = new EnumMap<>(PermissionGroup.class);
    for (PermissionGroup group : PermissionGroup.values()) {
      EnumMap<PermissionAction, Boolean> actions = new EnumMap<>(PermissionAction.class);
      for (PermissionAction action : PermissionAction.values()) {
        actions.put(action, group == PermissionGroup.RESIDENT);
      }
      values.put(group, actions);
    }
  }

  /** Repairs partially missing maps after loading older or hand-edited JSON. */
  public void normalize() {
    if (values == null) values = new EnumMap<>(PermissionGroup.class);
    for (PermissionGroup group : PermissionGroup.values()) {
      Map<PermissionAction, Boolean> existing = values.get(group);
      EnumMap<PermissionAction, Boolean> normalized = new EnumMap<>(PermissionAction.class);
      for (PermissionAction action : PermissionAction.values()) {
        boolean defaultValue = group == PermissionGroup.RESIDENT;
        normalized.put(action, existing == null ? defaultValue : existing.getOrDefault(action, defaultValue));
      }
      values.put(group, normalized);
    }
  }

  public boolean allows(PermissionGroup group, PermissionAction action) {
    normalize();
    return values.get(group).get(action);
  }

  public void set(PermissionGroup group, PermissionAction action, boolean value) {
    normalize();
    values.get(group).put(action, value);
  }

  public void setAll(boolean value) {
    normalize();
    for (PermissionGroup group : PermissionGroup.values()) {
      for (PermissionAction action : PermissionAction.values()) {
        values.get(group).put(action, value);
      }
    }
  }
}
