import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class MainRoute extends RouteBuilder {
    @Override
    public void configure() {

        /*
         * Camel Route Definitions
         */

         from("timer:tick?repeatCount=1&delay=3000").routeId("main")
                .setBody(constant(List.of("FRANCE")))
                .process("beforeMainRouteStartProcessor").id("beforeMainRouteStartProcessor")
                .log("lastDownloadTime = ${exchangeProperty.lastDownloadTime}, now = " + System.currentTimeMillis())
                .choice()
                    .when().simple("${exchangeProperty.lastDownloadTime} <= ${date:now-10m}")
                    /// When time range is long enough to poll
                    /// - we can also use:
                    /// .groovy("System.currentTimeMillis() - exchange.getProperty('lastDownloadTime') >= 10 * 60 * 1000")
                        .to("direct:choiceStartDownload")
                    .otherwise()
                    /// Otherwise, time range is still too short
                        .to("direct:choiceWaitAndRetry")
                .end();

        from("direct:choiceStartDownload").routeId("choice-start-download")
                .to("direct:downloadForumDataWithinTimeRange")
                /// Try to go to "direct:start"
                .to("direct:checkRestart")
                .end();

        from("direct:choiceWaitAndRetry").routeId("choice-wait-and-retry")
                .log("Waiting for 30 sec and try again...")
                .delay(30 * 1000L)
                /// Try to go to "direct:start"
                .to("direct:checkRestart")
                .end();

        from("direct:checkRestart").routeId("check-restart")
                .log("Check if Camel is shutting down...")
                .choice()
                    /// Go to "direct:start" when Camel is not stopping or stopped
                    .when().simple("${exchange.context.isStopping} == false && ${exchange.context.isStopped} == false")
                        .to("direct:start")
                .end();
    }

    /*
    * Processor Definitions
    */

    @Component("beforeMainRouteStartProcessor")
    public static class BeforeMainRouteStartProcessor implements Processor {
        @Override
        public void process(Exchange exchange) throws IOException {
            /// Load last download time from file (or use 1 hour ago if file does not exist)
            File lastDownloadFile = new File("last_download_time.txt");
            Long lastDownloadTime = lastDownloadFile.exists()
                    ? Long.parseLong(new String(Files.readAllBytes(lastDownloadFile.toPath())))
                    : Instant.now()
                    .minus(Duration.ofHours(1))
                    .toEpochMilli();
            exchange.setProperty("lastDownloadTime", lastDownloadTime);
        }
    }
}
