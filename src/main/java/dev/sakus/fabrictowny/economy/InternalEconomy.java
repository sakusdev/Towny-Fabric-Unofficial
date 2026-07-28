package dev.sakus.fabrictowny.economy;

import dev.sakus.fabrictowny.storage.TownyStore;

import java.util.UUID;

public final class InternalEconomy implements EconomyProvider {
  private final TownyStore store;

  public InternalEconomy(TownyStore store) { this.store = store; }

  public synchronized double balance(UUID id) {
    var resident = store.data().residents.get(id);
    return resident == null || !Double.isFinite(resident.balance) ? 0 : Math.max(0, resident.balance);
  }

  public synchronized boolean withdraw(UUID id, double amount, String reason) {
    if (!validAmount(amount)) return false;
    if (amount == 0) return true;
    var resident = store.data().residents.get(id);
    if (resident == null || !Double.isFinite(resident.balance) || resident.balance + 1e-8 < amount) return false;
    resident.balance -= amount;
    return true;
  }

  public synchronized void deposit(UUID id, double amount, String reason) {
    if (!validAmount(amount) || amount == 0) return;
    var resident = store.data().residents.get(id);
    if (resident != null) resident.balance = balance(id) + amount;
  }

  public String format(double amount) {
    return String.format("¥%,.0f", Double.isFinite(amount) ? amount : 0);
  }

  private static boolean validAmount(double amount) {
    return Double.isFinite(amount) && amount >= 0;
  }
}
