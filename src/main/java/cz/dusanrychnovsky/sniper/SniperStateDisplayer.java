package cz.dusanrychnovsky.sniper;

import javax.swing.*;

import static cz.dusanrychnovsky.sniper.MainWindow.*;

public class SniperStateDisplayer implements SniperListener {

  private MainWindow ui;

  public SniperStateDisplayer(MainWindow ui) {
    this.ui = ui;
  }

  @Override
  public void sniperLost() {
    showStatus(MainWindow.STATUS_LOST);
  }

  @Override
  public void sniperBidding() {
    showStatus(STATUS_BIDDING);
  }

  @Override
  public void sniperWinning() {
    showStatus(STATUS_WINNING);
  }

  @Override
  public void sniperWon() {
    showStatus(MainWindow.STATUS_WON);
  }

  private void showStatus(String status) {
    SwingUtilities.invokeLater(() -> ui.showStatus(status));
  }
}
