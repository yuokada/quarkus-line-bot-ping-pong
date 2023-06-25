package io.github.yuokada;

import com.linecorp.bot.client.base.Result;
import com.linecorp.bot.messaging.client.MessagingApiClient;
import com.linecorp.bot.messaging.model.Message;
import com.linecorp.bot.messaging.model.ReplyMessageRequest;
import com.linecorp.bot.messaging.model.TextMessage;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.RestHeader;

@ApplicationScoped
@Path("/callback")
public class CallbackResource {

    @Inject
    WebhookParser webhookParser;

    @Inject
    MessagingApiClient messagingApiClient;

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

        ReplyMessageRequest message = new ReplyMessageRequest(
            event.replyToken(),
            List.of(new TextMessage(userMessage.text() + " pong!")),
            false
        );
        CompletableFuture<Result<Void>> futureResponse = messagingApiClient.replyMessage(
            message);
        try {
            Result<Void> result = futureResponse.get();
            logger.info(result);
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
