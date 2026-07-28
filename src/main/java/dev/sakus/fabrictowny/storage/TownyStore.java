package dev.sakus.fabrictowny.storage;
import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;
import java.io.*;
import java.nio.file.*;
import java.util.concurrent.CopyOnWriteArrayList;
public final class TownyStore {
  private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
  private final Path file = FabricLoader.getInstance().getConfigDir().resolve("fabric-towny/data.json");
  private final CopyOnWriteArrayList<Runnable> listeners = new CopyOnWriteArrayList<>();
  private TownyData data = new TownyData();
  public synchronized void load() {
    if (!Files.exists(file)) return;
    try (Reader r=Files.newBufferedReader(file)) { TownyData loaded=gson.fromJson(r,TownyData.class); data=TownyDataSanitizer.sanitize(loaded); }
    catch (IOException e) { throw new IllegalStateException("Could not load "+file,e); }
  }
  public synchronized void save() {
    data=TownyDataSanitizer.sanitize(data);
    try { Files.createDirectories(file.getParent()); Path temp=file.resolveSibling(file.getFileName()+".tmp");
      try (Writer w=Files.newBufferedWriter(temp)) { gson.toJson(data,w); }
      Files.move(temp,file,StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE);
    } catch (AtomicMoveNotSupportedException e) { try { Files.move(file.resolveSibling(file.getFileName()+".tmp"),file,StandardCopyOption.REPLACE_EXISTING); } catch(IOException ex){throw new IllegalStateException(ex);} }
      catch(IOException e){throw new IllegalStateException("Could not save "+file,e);} listeners.forEach(Runnable::run);
  }
  public TownyData data(){return data;}
  public void onChange(Runnable listener){listeners.add(listener);}
}
