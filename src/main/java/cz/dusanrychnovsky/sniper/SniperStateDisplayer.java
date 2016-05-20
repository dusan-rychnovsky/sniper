package cz.dusanrychnovsky.sniper;

import javax.swing.*;

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
    showStatus(MainWindow.STATUS_BIDDING);
  }

  private void showStatus(String status) {
    SwingUtilities.invokeLater(() -> ui.showStatus(status));
  }
}
