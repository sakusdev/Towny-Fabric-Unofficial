package dev.sakus.fabrictowny.integration;
import de.bluecolored.bluemap.api.*;
import de.bluecolored.bluemap.api.markers.*;
import de.bluecolored.bluemap.api.math.*;
import dev.sakus.fabrictowny.model.*;
import dev.sakus.fabrictowny.storage.TownyStore;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
public final class BlueMapIntegration {
 private static final String SET_ID="fabrictowny.claims"; private final TownyStore store; private volatile BlueMapAPI api;
 public BlueMapIntegration(TownyStore store){this.store=store;}
 public void register(){BlueMapAPI.onEnable(a->{api=a;sync();});BlueMapAPI.onDisable(a->api=null);}
 public synchronized void sync(){var current=api;if(current==null)return;for(BlueMapMap map:current.getMaps()){MarkerSet set=new MarkerSet("Town Claims",true,false);for(TownBlock b:store.data().blocks.values()){Town t=store.data().towns.get(b.townId);Parsed p=Parsed.parse(b.key);if(t==null||p==null||!belongs(p.dimension,map))continue;int rgb=color(t.name);ShapeMarker m=new ShapeMarker(t.name,Shape.createRect(p.x*16.0,p.z*16.0,p.x*16.0+16,p.z*16.0+16),64);m.setColors(new Color(rgb,.9f),new Color(rgb,.3f));m.setLineWidth(2);m.setDepthTestEnabled(false);m.setDetail("<b>"+html(t.name)+"</b><br>"+b.type+(b.owner==null?"":"<br>Resident plot"));set.getMarkers().put("ft_"+Integer.toUnsignedString(b.key.hashCode(),36),m);}map.getMarkerSets().put(SET_ID,set);}}
 private static boolean belongs(String d,BlueMapMap m){String x=(m.getWorld().getId()+"|"+m.getId()).toLowerCase(Locale.ROOT);return x.contains(d.toLowerCase(Locale.ROOT))||(d.equals("minecraft:overworld")&&(m.getId().equals("world")||m.getId().equals("overworld")))||(d.equals("minecraft:the_nether")&&x.contains("nether"))||(d.equals("minecraft:the_end")&&x.contains("end"));}
 private static int color(String n){int h=0x345678;for(byte b:n.toLowerCase(Locale.ROOT).getBytes(StandardCharsets.UTF_8))h=(h*1000003)^(b&255);return ((80+(h&127))<<16)|((80+((h>>8)&127))<<8)|(80+((h>>16)&127));}
 private static String html(String s){return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\"","&quot;").replace("'","&#39;");}
 private record Parsed(String dimension,int x,int z){static Parsed parse(String v){int a=v.lastIndexOf(':');int b=v.lastIndexOf(':',a-1);if(a<0||b<0)return null;try{return new Parsed(v.substring(0,b),Integer.parseInt(v.substring(b+1,a)),Integer.parseInt(v.substring(a+1)));}catch(Exception e){return null;}}}
}
