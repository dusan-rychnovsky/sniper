package cz.dusanrychnovsky.sniper;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.packet.Message;

import java.util.HashMap;
import java.util.Map;

import static java.lang.Integer.parseInt;

public class AuctionMessageTranslator implements MessageListener {

  private final AuctionEventListener listener;

  public AuctionMessageTranslator(AuctionEventListener listener) {
    this.listener = listener;
  }

  public void processMessage(Chat chat, Message message) {

    Map<String, String> event = unpackEventFrom(message);

    String type = event.get("Event");
    if ("CLOSE".equals(type)) {
      listener.auctionClosed();
    }
    else if ("PRICE".equals(type)) {
      listener.currentPrice(
        parseInt(event.get("CurrentPrice")),
        parseInt(event.get("Increment"))
      );
    }
  }

  private Map<String, String> unpackEventFrom(Message message) {

    Map<String, String> event = new HashMap<>();
    for (String element : message.getBody().split(";")) {
      String[] pair = element.split(":");
      event.put(pair[0].trim(), pair[1].trim());
    }

    return event;
  }
}
