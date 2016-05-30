package cz.dusanrychnovsky.sniper;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import static java.lang.String.format;

public class Main {

  private static final int ARG_HOSTNAME = 0;
  private static final int ARG_USERNAME = 1;
  private static final int ARG_PASSWORD = 2;
  private static final int ARG_ITEM_ID = 3;

  private static final String AUCTION_RESOURCE = "Auction";
  private static final String ITEM_ID_AS_LOGIN = "auction-%s";
  private static final String AUCTION_ID_FORMAT = ITEM_ID_AS_LOGIN + "@%s/" + AUCTION_RESOURCE;

  private MainWindow ui;
  private Chat notToBeGCd;

  public static void main(String... args) throws Exception {
    Main main = new Main();
    main.joinAuction(
      connectTo(args[ARG_HOSTNAME], args[ARG_USERNAME], args[ARG_PASSWORD]),
      args[ARG_ITEM_ID]
    );
  }

  private static XMPPConnection connectTo(String hostname, String username, String password)
    throws XMPPException {

    XMPPConnection connection = new XMPPConnection(hostname);
    connection.connect();
    connection.login(username, password, AUCTION_RESOURCE);

    return connection;
  }

  public Main() throws Exception {
    startUserInterface();
  }

  private void startUserInterface() throws Exception {
    SwingUtilities.invokeAndWait(() -> ui = new MainWindow());
  }

  private void joinAuction(XMPPConnection connection, String itemId)
    throws XMPPException {

    disconnectWhenUICloses(connection);

    final Chat chat = connection.getChatManager().createChat(
      auctionId(itemId, connection), null
    );
    this.notToBeGCd = chat;

    XMPPAuction auction = new XMPPAuction(chat);

    chat.addMessageListener(
      new AuctionMessageTranslator(
        connection.getUser(),
        new AuctionSniper(auction, new SniperStateDisplayer(ui))
      )
    );

    auction.join();
  }

  private void disconnectWhenUICloses(final XMPPConnection connection) {
    ui.addWindowListener(
      new WindowAdapter() {
        @Override
        public void windowClosed(WindowEvent e) {
          connection.disconnect();
        }
      }
    );
  }

  private static String auctionId(String itemId, XMPPConnection connection) {
    return format(AUCTION_ID_FORMAT, itemId, connection.getServiceName());
  }
}
