package dev.sakus.fabrictowny.service;
import dev.sakus.fabrictowny.model.*;
import java.util.*;
public final class NewDayService {
 private final TownyService s; public NewDayService(TownyService s){this.s=s;}
 public synchronized void run(){
  var d=s.store().data();
  for(Town t:new ArrayList<>(d.towns.values())){
   for(UUID id:new ArrayList<>(t.residents)){
    if(id.equals(t.mayor))continue;var r=d.residents.get(id);if(r==null)continue;double tax=t.taxPercent?r.balance*t.taxes/100.0:t.taxes;
    for(TownBlock b:d.blocks.values())if(id.equals(b.owner)&&b.taxed)tax+=b.tax>0?b.tax:t.plotTax;
    if(s.economy().withdraw(id,tax,"town-tax"))t.bank+=tax;else {t.residents.remove(id);r.townId=null;}
   }
   t.bank-=t.upkeep;if(t.bank<0)t.bankrupt=true;
  }
  for(Nation n:new ArrayList<>(d.nations.values())){
   for(String tid:new ArrayList<>(n.towns)){Town t=d.towns.get(tid);if(t==null)continue;double tax=n.taxPercent?t.bank*n.taxes/100.0:n.taxes;if(t.bank>=tax){t.bank-=tax;n.bank+=tax;}else if(!tid.equals(n.capitalTownId)){n.towns.remove(tid);t.nationId=null;}}
   n.bank-=n.upkeep;
  }
  d.invitations.values().removeIf(i->i.expiresAt<System.currentTimeMillis());d.nextNewDayAt=System.currentTimeMillis()+s.config().newDayIntervalMillis;s.store().save();
 }
}
