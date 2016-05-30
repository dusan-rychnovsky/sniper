package cz.dusanrychnovsky.sniper;

public interface SniperListener {
  void sniperLost();
  void sniperBidding();
  void sniperWinning();
  void sniperWon();
}
