package io.github.yuokada.filter;

import com.linecorp.bot.parser.WebhookParser;
import io.quarkus.vertx.http.runtime.filters.Filters;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

@ApplicationScoped
public class MyExperimentalFilter {

    private static Logger logger;

    @Inject
    public MyExperimentalFilter(Logger logger) {
        MyExperimentalFilter.logger = logger;
    }

    private static void practiceFilter(RoutingContext rc, Logger logger) {
        logger.debug("Filter is Called!");
        HttpServerResponse response = rc.response();
        response.putHeader("X-Super-Header", "intercepting the request");
        HttpServerRequest request = rc.request();
        if (request.headers().contains("X-Super-Header")) {
            logger.info("Start stopping propagation");
            rc.response().setStatusCode(400);
            rc.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json");
            rc.end("{\"message\": \"Stop propagation\"}");
            // rc.fail(400, new HttpException(400, "invalid request"));
        } else {
            rc.next();
        }
    }

    private static void lineHeaderCheckFilter(RoutingContext rc) {
        HttpServerRequest request = rc.request();
        if (!rc.request().path().equals("/callback")) {
            rc.next();
            return;
        }
        if (!request.headers().contains(WebhookParser.SIGNATURE_HEADER_NAME)) {
            rc.response().setStatusCode(400);
            rc.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json");
            rc.end(String.format("{\"message\": \"%s header is required\"}", WebhookParser.SIGNATURE_HEADER_NAME));
        } else if (request.headers().get(WebhookParser.SIGNATURE_HEADER_NAME).isEmpty()) {
            rc.response().setStatusCode(400);
            rc.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json");
            rc.end(String.format("{\"message\": \"%s header is empty\"}", WebhookParser.SIGNATURE_HEADER_NAME));
        } else {
            rc.next();
        }
    }

    public void register(@Observes Filters filters) {
        filters.register(rc -> {
            practiceFilter(rc, logger);
        }, 100);
        filters.register(rc -> {
            lineHeaderCheckFilter(rc);
        }, 200);
        logger.info(String.format("%s is registered", MyExperimentalFilter.class.getName()));
    }
}
