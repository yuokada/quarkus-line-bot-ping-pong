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
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.RestHeader;

@ApplicationScoped
@Path("/callback")
public class CallbackResource {

    private final WebhookParser webhookParser;
    private final LineMessagingClient lineMessagingClient;

    private final Logger logger;

    @Inject
    public CallbackResource(WebhookParser webhookParser, LineMessagingClient lineMessagingClient, Logger logger) {
        this.webhookParser = webhookParser;
        this.lineMessagingClient = lineMessagingClient;
        this.logger = logger;
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public String callback(String requestBody,
        @RestHeader(WebhookParser.SIGNATURE_HEADER_NAME) String signature) {
        logger.debugf("%s: %s", WebhookParser.SIGNATURE_HEADER_NAME, signature);
        logger.debug(requestBody);

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
    public Response callbackAdd2(AddObject requestBody) {
        logger.info("info message");
        ResultObject result = new ResultObject();
        result.setResult(requestBody.getLeft() + requestBody.getRight());

        return Response.ok(result).build();
    }

}
