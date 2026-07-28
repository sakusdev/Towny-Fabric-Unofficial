package dev.sakus.fabrictowny;
import dev.sakus.fabrictowny.command.TownyCommands;
import dev.sakus.fabrictowny.economy.*;
import dev.sakus.fabrictowny.integration.BlueMapIntegration;
import dev.sakus.fabrictowny.protection.*;
import dev.sakus.fabrictowny.service.*;
import dev.sakus.fabrictowny.storage.TownyStore;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.*;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.*;
public final class FabricTowny implements ModInitializer {
 public static final String MOD_ID="fabrictowny";public static final Logger LOGGER=LoggerFactory.getLogger(MOD_ID);
 private final TownyStore store=new TownyStore();
 public void onInitialize(){store.load();TownyConfig config=TownyConfig.load();EconomyProvider economy=new InternalEconomy(store);TownyService service=new TownyService(store,economy,config);NewDayService day=new NewDayService(service);TownyCommands.register(service,day);ProtectionHooks.register(new ProtectionEngine(service));
  if(FabricLoader.getInstance().isModLoaded("bluemap")){BlueMapIntegration bm=new BlueMapIntegration(store);bm.register();store.onChange(bm::sync);}
  ServerLifecycleEvents.SERVER_STARTED.register(server->{if(store.data().nextNewDayAt==0)store.data().nextNewDayAt=System.currentTimeMillis()+config.newDayIntervalMillis;});
  ServerTickEvents.END_SERVER_TICK.register(server->{if(System.currentTimeMillis()>=store.data().nextNewDayAt)day.run();});
  ServerLifecycleEvents.SERVER_STOPPING.register(server->store.save());LOGGER.info("Fabric Towny loaded: {} towns, {} nations, {} blocks",store.data().towns.size(),store.data().nations.size(),store.data().blocks.size());
 }
}
