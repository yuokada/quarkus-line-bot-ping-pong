package io.github.yuokada.provider;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.messaging.client.MessagingApiClient;
import com.linecorp.bot.parser.LineSignatureValidator;
import com.linecorp.bot.parser.WebhookParser;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class ApiClientProvider {

    @ConfigProperty(name = "line.bot.channel-secret", defaultValue = "")
    String channelSecret;
    @ConfigProperty(name = "line.bot.channel-token", defaultValue = "")
    String channelAccessToken;

    @ConfigProperty(name = "line.bot.handler.path", defaultValue = "/callback")
    String callbackPath;

    @Produces
    public LineMessagingClient provideApiClient() {
        if (channelAccessToken.isEmpty()) {
            throw new RuntimeException(
                "channel token is empty. Please set line.bot.channel-token in application.properties or somewhere."
            );
        }
        return LineMessagingClient.builder(channelAccessToken).build();
    }

    @Produces
    public MessagingApiClient provideNewApiClient() {
        return MessagingApiClient.builder(channelSecret).build();
    }

    @Produces
    public WebhookParser provideParse() {
        if (channelSecret.isEmpty()) {
            throw new RuntimeException(
                "channel secret is empty. Please set line.bot.channel-secret in application.properties or somewhere."
            );
        }

        return new WebhookParser(
            new LineSignatureValidator(channelSecret.getBytes()));
    }

}
