package dev.sakus.fabrictowny.command;
import com.mojang.brigadier.arguments.*;
import dev.sakus.fabrictowny.ClaimKey;
import dev.sakus.fabrictowny.model.*;
import dev.sakus.fabrictowny.service.*;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import java.util.*;
import static net.minecraft.commands.Commands.*;
public final class TownyCommands {
 private TownyCommands(){}
 public static void register(TownyService s,NewDayService day){CommandRegistrationCallback.EVENT.register((d,r,e)->{
  d.register(literal("town").executes(c->townInfo(c.getSource(),s))
   .then(literal("new").then(argument("name",StringArgumentType.word()).executes(c->guard(c.getSource(),()->{var p=p(c.getSource());s.createTown(p.getUUID(),p.getName().getString(),StringArgumentType.getString(c,"name"),ClaimKey.of(p.level(),p.blockPosition()));return "町を作成しました。";}))))
   .then(literal("claim").executes(c->guard(c.getSource(),()->{var p=p(c.getSource());var t=mayor(s,p);s.claim(p.getUUID(),t,ClaimKey.of(p.level(),p.blockPosition()),false);return "領有しました。";}))
    .then(literal("outpost").executes(c->guard(c.getSource(),()->{var p=p(c.getSource());s.claim(p.getUUID(),mayor(s,p),ClaimKey.of(p.level(),p.blockPosition()),true);return "アウトポストを領有しました。";}))))
   .then(literal("unclaim").executes(c->guard(c.getSource(),()->{var p=p(c.getSource());s.unclaim(mayor(s,p),ClaimKey.of(p.level(),p.blockPosition()));return "領有を解除しました。";})))
   .then(literal("join").then(argument("town",StringArgumentType.word()).executes(c->guard(c.getSource(),()->{var p=p(c.getSource());var t=s.townByName(StringArgumentType.getString(c,"town")).orElseThrow(()->new IllegalStateException("町が見つかりません。"));if(!t.open)throw new IllegalStateException("この町は公開参加を許可していません。");s.joinTown(p.getUUID(),p.getName().getString(),t);return "町に参加しました。";}))))
   .then(literal("leave").executes(c->guard(c.getSource(),()->{var p=p(c.getSource());s.leaveTown(p.getUUID());return "町を脱退しました。";})))
   .then(literal("delete").executes(c->guard(c.getSource(),()->{var p=p(c.getSource());s.deleteTown(mayor(s,p));return "町を削除しました。";})))
   .then(literal("deposit").then(argument("amount",DoubleArgumentType.doubleArg(0.01)).executes(c->guard(c.getSource(),()->{var p=p(c.getSource());var t=s.town(p.getUUID()).orElseThrow();double a=DoubleArgumentType.getDouble(c,"amount");if(!s.economy().withdraw(p.getUUID(),a,"town-deposit"))throw new IllegalStateException("残高不足です。");t.bank+=a;s.store().save();return s.economy().format(a)+"を入金しました。";}))))
   .then(literal("withdraw").then(argument("amount",DoubleArgumentType.doubleArg(0.01)).executes(c->guard(c.getSource(),()->{var p=p(c.getSource());var t=mayor(s,p);double a=DoubleArgumentType.getDouble(c,"amount");if(t.bank<a)throw new IllegalStateException("町銀行の残高不足です。");t.bank-=a;s.economy().deposit(p.getUUID(),a,"town-withdraw");s.store().save();return s.economy().format(a)+"を出金しました。";}))))
   .then(literal("set").then(literal("taxes").then(argument("amount",DoubleArgumentType.doubleArg(0)).executes(c->guard(c.getSource(),()->{var p=p(c.getSource());var t=mayor(s,p);t.taxes=DoubleArgumentType.getDouble(c,"amount");s.store().save();return "住民税を変更しました。";}))))
    .then(literal("plottax").then(argument("amount",DoubleArgumentType.doubleArg(0)).executes(c->guard(c.getSource(),()->{var p=p(c.getSource());var t=mayor(s,p);t.plotTax=DoubleArgumentType.getDouble(c,"amount");s.store().save();return "プロット税を変更しました。";})))))
   .then(literal("toggle").then(argument("setting",StringArgumentType.word()).executes(c->guard(c.getSource(),()->{var p=p(c.getSource());var t=mayor(s,p);String x=StringArgumentType.getString(c,"setting");switch(x){case"open"->t.open=!t.open;case"public"->t.publicSpawn=!t.publicSpawn;case"pvp"->t.pvp=!t.pvp;case"fire"->t.fire=!t.fire;case"explosion"->t.explosion=!t.explosion;case"mobs"->t.mobs=!t.mobs;case"taxpercent"->t.taxPercent=!t.taxPercent;default->throw new IllegalStateException("不明な設定です。");}s.store().save();return x+"を切り替えました。";}))))
  );
  d.register(literal("nation").executes(c->nationInfo(c.getSource(),s))
   .then(literal("new").then(argument("name",StringArgumentType.word()).executes(c->guard(c.getSource(),()->{var p=p(c.getSource());s.createNation(p.getUUID(),mayor(s,p),StringArgumentType.getString(c,"name"));return "国家を作成しました。";}))))
   .then(literal("delete").executes(c->guard(c.getSource(),()->{var p=p(c.getSource());var t=mayor(s,p);var n=nation(s,t);if(!n.capitalTownId.equals(t.id))throw new IllegalStateException("首都の町長のみ実行できます。");s.deleteNation(n);return "国家を削除しました。";})))
  );
  d.register(literal("plot").then(literal("claim").executes(c->guard(c.getSource(),()->{var p=p(c.getSource());var t=s.town(p.getUUID()).orElseThrow();var b=s.block(ClaimKey.of(p.level(),p.blockPosition())).orElseThrow();if(!b.townId.equals(t.id))throw new IllegalStateException("自分の町の土地ではありません。");if(b.owner!=null)throw new IllegalStateException("所有者がいます。");b.owner=p.getUUID();s.store().save();return "プロットを取得しました。";})))
   .then(literal("unclaim").executes(c->guard(c.getSource(),()->{var p=p(c.getSource());var b=s.block(ClaimKey.of(p.level(),p.blockPosition())).orElseThrow();if(!p.getUUID().equals(b.owner))throw new IllegalStateException("自分のプロットではありません。");b.owner=null;s.store().save();return "プロットを返却しました。";})))
   .then(literal("set").then(literal("type").then(argument("type",StringArgumentType.word()).executes(c->guard(c.getSource(),()->{var p=p(c.getSource());var b=s.block(ClaimKey.of(p.level(),p.blockPosition())).orElseThrow();b.type=PlotType.valueOf(StringArgumentType.getString(c,"type").toUpperCase(Locale.ROOT));s.store().save();return "プロット種別を変更しました。";})))))
  );
  d.register(literal("resident").executes(c->{var p=p(c.getSource());var r0=s.resident(p.getUUID(),p.getName().getString());return ok(c.getSource(),"Resident: "+r0.lastKnownName+" | Balance: "+s.economy().format(r0.balance));}));
  d.register(literal("townyadmin").requires(src->isAdmin(src,s)).then(literal("newday").executes(c->{day.run();return ok(c.getSource(),"Towny New Dayを実行しました。");})).then(literal("save").executes(c->{s.store().save();return ok(c.getSource(),"保存しました。");})));
 });}
 private static int townInfo(CommandSourceStack src,TownyService s){var p=p(src);var t=s.town(p.getUUID()).orElse(null);return t==null?fail(src,"町に所属していません。"):ok(src,"町: "+t.name+" | 住民: "+t.residents.size()+" | 領地: "+t.blocks.size()+" | 銀行: "+s.economy().format(t.bank));}
 private static int nationInfo(CommandSourceStack src,TownyService s){var p=p(src);var t=s.town(p.getUUID()).orElse(null);if(t==null||t.nationId==null)return fail(src,"国家に所属していません。");var n=s.store().data().nations.get(t.nationId);return ok(src,"国家: "+n.name+" | 町: "+n.towns.size()+" | 銀行: "+s.economy().format(n.bank));}
 private static boolean isAdmin(CommandSourceStack source,TownyService service){
  if(source.getEntity()==null)return true;
  if(!(source.getEntity() instanceof ServerPlayer player))return false;
  var resident=service.store().data().residents.get(player.getUUID());
  return resident!=null&&resident.adminBypass;
 }
 private static Town mayor(TownyService s,ServerPlayer p){var t=s.town(p.getUUID()).orElseThrow(()->new IllegalStateException("町に所属していません。"));if(!t.mayor.equals(p.getUUID()))throw new IllegalStateException("町長のみ実行できます。");return t;}
 private static Nation nation(TownyService s,Town t){if(t.nationId==null)throw new IllegalStateException("国家に所属していません。");return s.store().data().nations.get(t.nationId);}
 private static ServerPlayer p(CommandSourceStack s){try{return (ServerPlayer)s.getEntity();}catch(Exception e){throw new IllegalStateException("プレイヤーのみ実行できます。");}}
 private interface Op{String run()throws Exception;} private static int guard(CommandSourceStack s,Op op){try{return ok(s,op.run());}catch(Exception e){return fail(s,e.getMessage()==null?"処理に失敗しました。":e.getMessage());}}
 private static int ok(CommandSourceStack s,String m){s.sendSuccess(()->Component.literal(m),false);return 1;}private static int fail(CommandSourceStack s,String m){s.sendFailure(Component.literal(m));return 0;}
}
