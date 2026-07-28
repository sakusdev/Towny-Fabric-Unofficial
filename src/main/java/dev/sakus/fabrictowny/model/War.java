package dev.sakus.fabrictowny.model;
import java.util.*;
public final class War {
  public enum State { DECLARED, ACTIVE, TRUCE, ENDED }
  public String id;
  public String attackerNationId;
  public String defenderNationId;
  public State state = State.DECLARED;
  public long declaredAt = System.currentTimeMillis();
  public long startsAt;
  public long endsAt;
  public int attackerPoints;
  public int defenderPoints;
  public Set<String> occupiedTownIds = new HashSet<>();
}
