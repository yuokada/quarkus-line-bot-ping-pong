package io.github.yuokada;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.response.BotApiResponse;
import com.linecorp.bot.parser.WebhookParser;
import com.linecorp.bot.webhook.model.CallbackRequest;
import com.linecorp.bot.webhook.model.Event;
import com.linecorp.bot.webhook.model.MessageEvent;
import com.linecorp.bot.webhook.model.TextMessageContent;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

@QuarkusTest
public class CallbackResourceTest {

    @InjectMock
    WebhookParser webhookParser;

    @InjectMock
    LineMessagingClient lineMessagingClient;

    @Test
    public void testCallbackWithTextMessageRepliesPong() throws Exception {
        TextMessageContent content = mock(TextMessageContent.class);
        when(content.text()).thenReturn("Hello");

        MessageEvent event = mock(MessageEvent.class);
        when(event.replyToken()).thenReturn("test-reply-token");
        when(event.message()).thenReturn(content);

        CallbackRequest request = mock(CallbackRequest.class);
        when(request.events()).thenReturn(List.of(event));
        when(webhookParser.handle(anyString(), any(byte[].class))).thenReturn(request);

        when(lineMessagingClient.replyMessage(any()))
            .thenReturn(CompletableFuture.completedFuture(mock(BotApiResponse.class)));

        RestAssured.given()
            .contentType(ContentType.JSON)
            .header("X-Line-Signature", "valid-signature")
            .body("\"test body\"")
            .when().post("/callback")
            .then()
            .statusCode(200)
            .body(equalTo("ok"));

        ArgumentCaptor<ReplyMessage> captor = ArgumentCaptor.forClass(ReplyMessage.class);
        verify(lineMessagingClient).replyMessage(captor.capture());
        TextMessage sentMessage = (TextMessage) captor.getValue().getMessages().get(0);
        assertThat(sentMessage.getText(), equalTo("Hello pong!"));
    }

    @Test
    public void testCallbackWithEmptyEventsReturnsInvalid() throws Exception {
        CallbackRequest request = mock(CallbackRequest.class);
        when(request.events()).thenReturn(List.of());
        when(webhookParser.handle(anyString(), any(byte[].class))).thenReturn(request);

        RestAssured.given()
            .contentType(ContentType.JSON)
            .header("X-Line-Signature", "valid-signature")
            .body("\"test body\"")
            .when().post("/callback")
            .then()
            .statusCode(200)
            .body(equalTo("invalid request message"));

        verify(lineMessagingClient, never()).replyMessage(any());
    }

    @Test
    public void testCallbackWithNonMessageEventDoesNotReply() throws Exception {
        Event nonMessageEvent = mock(Event.class);
        CallbackRequest request = mock(CallbackRequest.class);
        when(request.events()).thenReturn(List.of(nonMessageEvent));
        when(webhookParser.handle(anyString(), any(byte[].class))).thenReturn(request);

        RestAssured.given()
            .contentType(ContentType.JSON)
            .header("X-Line-Signature", "valid-signature")
            .body("\"test body\"")
            .when().post("/callback")
            .then()
            .statusCode(200)
            .body(equalTo("ok"));

        verify(lineMessagingClient, never()).replyMessage(any());
    }
}
