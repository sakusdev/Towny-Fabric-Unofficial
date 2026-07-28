package dev.sakus.fabrictowny.economy;
import java.util.UUID;
public interface EconomyProvider {
  double balance(UUID player);
  boolean withdraw(UUID player,double amount,String reason);
  void deposit(UUID player,double amount,String reason);
  String format(double amount);
}
