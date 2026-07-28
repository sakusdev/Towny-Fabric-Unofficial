package dev.sakus.fabrictowny.protection;
import dev.sakus.fabrictowny.ClaimKey;
import dev.sakus.fabrictowny.model.PermissionAction;
import net.fabricmc.fabric.api.event.player.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
public final class ProtectionHooks {
 private ProtectionHooks(){}
 public static void register(ProtectionEngine e){
  PlayerBlockBreakEvents.BEFORE.register((w,p,pos,state,be)->
    (p instanceof ServerPlayer sp) && check(e,sp,ClaimKey.of(w,pos),PermissionAction.DESTROY));
  AttackBlockCallback.EVENT.register((p,w,h,pos,d)->{if(w.isClientSide()||!(p instanceof ServerPlayer sp))return InteractionResult.PASS;return check(e,sp,ClaimKey.of(sp.level(),pos),PermissionAction.DESTROY)?InteractionResult.PASS:InteractionResult.FAIL;});
  UseBlockCallback.EVENT.register((p,w,h,hit)->{if(w.isClientSide()||!(p instanceof ServerPlayer sp))return InteractionResult.PASS;return check(e,sp,ClaimKey.of(sp.level(),hit.getBlockPos()),PermissionAction.SWITCH)?InteractionResult.PASS:InteractionResult.FAIL;});
  UseItemCallback.EVENT.register((p,w,h)->{if(w.isClientSide()||!(p instanceof ServerPlayer sp))return InteractionResult.PASS;return check(e,sp,ClaimKey.of(sp.level(),sp.blockPosition()),PermissionAction.ITEM_USE)?InteractionResult.PASS:InteractionResult.FAIL;});
 }
 private static boolean check(ProtectionEngine e,ServerPlayer p,String key,PermissionAction a){boolean ok=e.allowed(p.getUUID(),key,a,false);if(!ok)p.sendSystemMessage(Component.literal("この土地ではその操作は許可されていません。"),true);return ok;}
}
