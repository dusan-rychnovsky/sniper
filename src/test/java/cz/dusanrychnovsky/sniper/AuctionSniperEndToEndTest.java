package cz.dusanrychnovsky.sniper;

import org.jivesoftware.smack.XMPPException;
import org.junit.After;
import org.junit.Test;

import static cz.dusanrychnovsky.sniper.ApplicationRunner.SNIPER_XMPP_ID;

public class AuctionSniperEndToEndTest {

  private final FakeAuctionServer auction = new FakeAuctionServer("item-54321");
  private final ApplicationRunner application = new ApplicationRunner();

  @Test
  public void sniperJoinsAuctionUntilAuctionCloses() throws Exception {
    auction.startSellingItem();
    application.startBiddingIn(auction);
    auction.hasRecievedJoinRequestFromSniper();
    auction.announceClosed();
    application.showsSniperHasLostAuction();
  }

  @Test
  public void sniperMakesAHigherBidButLooses()
      throws XMPPException, InterruptedException {

    auction.startSellingItem();

    application.startBiddingIn(auction);
    auction.hasReceivedJoinRequestFrom(SNIPER_XMPP_ID);

    auction.reportPrice(1000, 98, "other bidder");
    application.hasShownSniperIsBidding();

    auction.hasReceivedBid(1098, SNIPER_XMPP_ID);

    auction.announceClosed();
    application.showsSniperHasLostAuction();
  }

  @Test
  public void sniperWinsAnAuctionByBiddingHigher()
      throws XMPPException, InterruptedException {

    auction.startSellingItem();

    application.startBiddingIn(auction);
    auction.hasReceivedJoinRequestFrom(SNIPER_XMPP_ID);

    auction.reportPrice(1000, 98, "other bidder");
    application.hasShownSniperIsBidding();

    auction.hasReceivedBid(1098, SNIPER_XMPP_ID);

    auction.reportPrice(1098, 97, SNIPER_XMPP_ID);
    application.hasShownSniperIsWinning();

    auction.announceClosed();
    application.showSniperHasWonAuction();
  }

  @After
  public void stopAuction() {
    auction.stop();
  }

  @After
  public void stopApplication() {
    application.stop();
  }
}
