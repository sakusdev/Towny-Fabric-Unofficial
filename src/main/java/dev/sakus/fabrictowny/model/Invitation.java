package dev.sakus.fabrictowny.model;
import java.util.UUID;
public final class Invitation {
  public enum Kind { TOWN, NATION, ALLY, TRUCE }
  public String id;
  public Kind kind;
  public String senderId;
  public String targetId;
  public UUID targetPlayer;
  public long expiresAt;
}
