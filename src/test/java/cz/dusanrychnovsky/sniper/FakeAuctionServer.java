package cz.dusanrychnovsky.sniper;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

public class FakeAuctionServer {

  private static final String XMPP_HOSTNAME = "localhost";
  private static final String ITEM_ID_AS_LOGIN = "auction-%s";
  private static final String AUCTION_PASSWORD = "auction";
  private static final String AUCTION_RESOURCE = "Auction";

  private final String itemId;
  private final XMPPConnection connection;
  private Chat currentChat;
  private SingleMessageListener messageListener = new SingleMessageListener();

  public FakeAuctionServer(String itemId) {
    this.itemId = itemId;
    this.connection = new XMPPConnection(XMPP_HOSTNAME);
  }

  public String getItemId() {
    return itemId;
  }

  public void startSellingItem() throws XMPPException {
    connection.connect();
    connection.login(
      format(ITEM_ID_AS_LOGIN, itemId),
      AUCTION_PASSWORD,
      AUCTION_RESOURCE
    );
    connection.getChatManager().addChatListener(
      new ChatManagerListener() {
        public void chatCreated(Chat chat, boolean createdLocally) {
          currentChat = chat;
          chat.addMessageListener(messageListener);
        }
      }
    );
  }

  public void hasRecievedJoinRequestFromSniper() throws InterruptedException {
    messageListener.receivesAMessage();
  }

  public void announceClosed() throws XMPPException {
    currentChat.sendMessage(
      "SOL Version: 1.1; Event: CLOSE;"
    );
  }

  public void stop() {
    connection.disconnect();
  }

  public void reportPrice(int price, int increment, String bidder)
    throws XMPPException {

    currentChat.sendMessage(
      format(
        "SOLVersion: 1.1; Event: PRICE; " +
        "CurrentPrice: %d; Increment: %d; Bidder: %s;",
        price, increment, bidder
      )
    );
  }

  public void hasReceivedBid(int bid, String sniperId)
    throws InterruptedException {

    receivesAMessageMatching(
      sniperId,
      equalTo(format(Main.BID_COMMAND_FORMAT, bid))
    );
  }

  public void hasReceivedJoinRequestFrom(String sniperId)
    throws InterruptedException {

    receivesAMessageMatching(
      sniperId,
      equalTo(Main.JOIN_COMMAND_FORMAT)
    );
  }

  private void receivesAMessageMatching(String sniperId, Matcher<String> messageMatcher)
    throws InterruptedException {

    messageListener.receivesAMessage(messageMatcher);
    assertThat(currentChat.getParticipant(), equalTo(sniperId));
  }

  private static class SingleMessageListener implements MessageListener {
    private final ArrayBlockingQueue<Message> messages = new ArrayBlockingQueue<Message>(1);

    public void processMessage(Chat chat, Message message) {
      messages.add(message);
    }

    public void receivesAMessage() throws InterruptedException {
      assertThat("Message", messages.poll(5, TimeUnit.SECONDS), is(notNullValue()));
    }

    public void receivesAMessage(Matcher<? super String> messageMatcher)
      throws InterruptedException {

      Message message = messages.poll(5, TimeUnit.SECONDS);
      assertThat("Message", message, is(Matchers.<Message>notNullValue()));
      assertThat(message.getBody(), messageMatcher);
    }
  }
}
