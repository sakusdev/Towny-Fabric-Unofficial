package dev.sakus.fabrictowny.model;
import java.util.UUID;
public final class TownBlock {
  public String key;
  public String townId;
  public UUID owner;
  public PlotType type = PlotType.DEFAULT;
  public String group;
  public boolean forSale;
  public double salePrice;
  public double tax;
  public boolean taxed = true;
  public PermissionMatrix permissions = new PermissionMatrix();
  public boolean pvp;
  public boolean explosion;
  public boolean fire;
  public boolean mobs = true;
  public boolean outpost;
  public TownBlock() {}
  public TownBlock(String key, String townId) { this.key = key; this.townId = townId; }
}
