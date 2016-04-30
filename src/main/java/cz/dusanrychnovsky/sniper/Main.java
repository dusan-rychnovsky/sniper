package cz.dusanrychnovsky.sniper;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

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
    SwingUtilities.invokeAndWait(new Runnable() {
      public void run() {
        ui = new MainWindow();
      }
    });
  }

  private void joinAuction(XMPPConnection connection, String itemId)
    throws XMPPException {

    final Chat chat = connection.getChatManager().createChat(
      auctionId(itemId, connection),
      new MessageListener() {
        public void processMessage(Chat chat, Message message) {
          SwingUtilities.invokeLater(
            new Runnable() {
              public void run() {
                ui.showStatus(MainWindow.STATUS_LOST);
              }
            }
          );
        }
      }
    );
    this.notToBeGCd = chat;

    chat.sendMessage(new Message());
  }

  private static String auctionId(String itemId, XMPPConnection connection) {
    return format(AUCTION_ID_FORMAT, itemId, connection.getServiceName());
  }

  public static class MainWindow extends JFrame {

    public static final String MAIN_WINDOW_NAME = "Auction Sniper Main";

    public static final String SNIPER_STATUS_NAME = "sniper status";
    public static final String STATUS_JOINING = "joining";
    public static final String STATUS_LOST = "lost";

    private final JLabel sniperStatus = createLabel(STATUS_JOINING);

    public MainWindow() {
      super("Auction Sniper");
      setName(MAIN_WINDOW_NAME);
      add(sniperStatus);
      pack();
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      setVisible(true);
    }

    private static JLabel createLabel(String initialText) {
      JLabel result = new JLabel(initialText);
      result.setName(SNIPER_STATUS_NAME);
      result.setBorder(new LineBorder(Color.BLACK));
      return result;
    }

    public void showStatus(String status) {
      sniperStatus.setText(status);
    }
  }
}
