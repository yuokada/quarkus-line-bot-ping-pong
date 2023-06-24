package io.github.yuokada;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.response.BotApiResponse;
import com.linecorp.bot.parser.WebhookParseException;
import com.linecorp.bot.parser.WebhookParser;
import com.linecorp.bot.webhook.model.CallbackRequest;
import com.linecorp.bot.webhook.model.Event;
import com.linecorp.bot.webhook.model.MessageEvent;
import com.linecorp.bot.webhook.model.TextMessageContent;
import io.github.yuokada.model.AddObject;
import io.github.yuokada.model.ResultObject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.RestHeader;

@ApplicationScoped
@Path("/callback")
public class CallbackResource {

    @Inject
    WebhookParser webhookParser;
    @Inject
    LineMessagingClient lineMessagingClient;

//    @Inject
//    MessagingApiClient messagingApiClient;

    @Inject
    Logger logger;

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public String callback(String requestBody,
        @RestHeader(WebhookParser.SIGNATURE_HEADER_NAME) Optional<String> lineHeader) {
        if (lineHeader.isEmpty()) {
            return "X-Line-Signature does not exist.";
        }
        logger.debugf("%s: %s", WebhookParser.SIGNATURE_HEADER_NAME, lineHeader.get());
        logger.debug(requestBody);
        String signature = lineHeader.get();

        try {
            CallbackRequest callbackRequest =
                webhookParser.handle(signature, requestBody.getBytes(StandardCharsets.UTF_8));
            if (callbackRequest.events().isEmpty()) {
                logger.warn("There is no event in the message");
                return "invalid request message";
            }
            List<Event> events = callbackRequest.events();
            for (Event event : events) {
                logger.info(event.getClass().toString());
                if (event instanceof MessageEvent messageEvent) {
                    handleTextMessageEvent(messageEvent);
                }
            }
        } catch (IOException | WebhookParseException e) {
            throw new RuntimeException(e);
        }
        return "ok";
    }

    private void handleTextMessageEvent(MessageEvent event)
        throws IOException {
        TextMessageContent userMessage = (TextMessageContent) event.message();
        ReplyMessage message = new ReplyMessage(
            event.replyToken(),
            new TextMessage(userMessage.text() + " pong!")
        );
        CompletableFuture<BotApiResponse> futureResponse =
            lineMessagingClient.replyMessage(message);
        try {
            BotApiResponse response = futureResponse.get();
            logger.info(response);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @POST
    @Path("/add")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public ResultObject callbackAdd2(AddObject requestBody) {
        logger.info("info message");
        ResultObject result = new ResultObject();
        result.setResult(requestBody.getLeft() + requestBody.getRight());

        return result;
    }

}
