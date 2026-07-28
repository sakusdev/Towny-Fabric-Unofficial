package dev.sakus.fabrictowny.protection;

import dev.sakus.fabrictowny.ClaimKey;
import dev.sakus.fabrictowny.model.PermissionAction;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;

public final class ProtectionHooks {
  private static final Component DENIED =
      Component.literal("この土地ではその操作は許可されていません。");

  private ProtectionHooks() {}

  public static void register(ProtectionEngine engine) {
    PlayerBlockBreakEvents.BEFORE.register((level, player, pos, state, blockEntity) ->
        player instanceof ServerPlayer serverPlayer
            && check(engine, serverPlayer, ClaimKey.of(level, pos), PermissionAction.DESTROY));

    AttackBlockCallback.EVENT.register((player, level, hand, pos, direction) -> {
      if (level.isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
        return InteractionResult.PASS;
      }
      return result(check(engine, serverPlayer, ClaimKey.of(level, pos), PermissionAction.DESTROY));
    });

    UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
      if (level.isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
        return InteractionResult.PASS;
      }
      return result(check(engine, serverPlayer, ClaimKey.of(level, hit.getBlockPos()), PermissionAction.SWITCH));
    });

    UseItemCallback.EVENT.register((player, level, hand) -> {
      if (level.isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
        return InteractionResult.PASS;
      }
      return result(check(engine, serverPlayer,
          ClaimKey.of(level, serverPlayer.blockPosition()), PermissionAction.ITEM_USE));
    });

    AttackEntityCallback.EVENT.register((player, level, hand, entity, hitResult) -> {
      if (level.isClientSide()
          || !(player instanceof ServerPlayer attacker)
          || !(entity instanceof ServerPlayer victim)) {
        return InteractionResult.PASS;
      }
      String key = ClaimKey.of(level, victim.blockPosition());
      if (engine.pvp(attacker.getUUID(), victim.getUUID(), key, false)) {
        return InteractionResult.PASS;
      }
      attacker.sendSystemMessage(Component.literal("この土地ではPVPは許可されていません。"), true);
      return InteractionResult.FAIL;
    });
  }

  private static InteractionResult result(boolean allowed) {
    return allowed ? InteractionResult.PASS : InteractionResult.FAIL;
  }

  private static boolean check(
      ProtectionEngine engine,
      ServerPlayer player,
      String key,
      PermissionAction action
  ) {
    boolean allowed = engine.allowed(player.getUUID(), key, action, false);
    if (!allowed) {
      player.sendSystemMessage(DENIED, true);
    }
    return allowed;
  }
}
