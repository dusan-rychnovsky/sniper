package cz.dusanrychnovsky.sniper;

import com.objogate.wl.swing.AWTEventQueueProber;
import com.objogate.wl.swing.driver.JFrameDriver;
import com.objogate.wl.swing.driver.JLabelDriver;
import com.objogate.wl.swing.gesture.GesturePerformer;

import static org.hamcrest.Matchers.equalTo;

public class ApplicationRunner {

  private static final String XMPP_HOSTNAME = "localhost";
  private static final String SNIPER_ID = "sniper";
  private static final String SNIPER_PASSWORD = "sniper";

  private AuctionSniperDriver driver;

  public void startBiddingIn(final FakeAuctionServer auction) {
    Thread thread = new Thread("Test Application") {
      @Override
      public void run() {
        try {
          Main.main(XMPP_HOSTNAME, SNIPER_ID, SNIPER_PASSWORD, auction.getItemId());
        }
        catch (Exception ex) {
          ex.printStackTrace();
        }
      }
    };
    thread.setDaemon(true);
    thread.start();
    driver = new AuctionSniperDriver(1000);
    driver.showsSniperStatus(Main.MainWindow.STATUS_JOINING);
  }

  public void showsSniperHasLostAuction() {
    driver.showsSniperStatus(Main.MainWindow.STATUS_LOST);
  }

  public void stop() {
    if (driver != null) {
      driver.dispose();
    }
  }

  private static class AuctionSniperDriver extends JFrameDriver {

    public AuctionSniperDriver(int timeoutMillis) {
      super(
        new GesturePerformer(),
        JFrameDriver.topLevelFrame(
          named(Main.MainWindow.MAIN_WINDOW_NAME),
          showingOnScreen()
        ),
        new AWTEventQueueProber(
          timeoutMillis,
          100
        )
      );
    }

    public void showsSniperStatus(String statusText) {
      new JLabelDriver(this, named(Main.MainWindow.SNIPER_STATUS_NAME))
        .hasText(equalTo(statusText));
    }
  }
}