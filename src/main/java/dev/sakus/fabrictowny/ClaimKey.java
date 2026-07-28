package dev.sakus.fabrictowny;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
public final class ClaimKey { private ClaimKey(){} public static String of(Level w,BlockPos p){return w.dimension().identifier()+":"+(p.getX()>>4)+":"+(p.getZ()>>4);} }
