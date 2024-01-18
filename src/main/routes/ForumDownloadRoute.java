import com.quid.twingly.model.Country;
import com.quid.twingly.model.TwinglyForumDocument;
import com.quid.twingly.model.TwinglyForumRequest;
import com.quid.twingly.model.TwinglyForumResponse;
import com.quid.twingly.util.Utils;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Processor;
import org.apache.camel.builder.ThreadPoolBuilder;
import org.apache.camel.builder.ThreadPoolProfileBuilder;
import org.apache.camel.component.netty.http.NettyHttpOperationFailedException;
import org.apache.camel.http.base.HttpOperationFailedException;
import org.apache.camel.support.builder.PredicateBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.util.concurrent.ExecutorService;

public class ForumDownloadRoute extends RouteBuilder {
    protected static final String NB_RECEIVE_TIME = "NBreceiveTime";
    protected static final String PROP_IN_PAGINATION = "inPagination";
    protected static final int MAX_REQ_PER_SEC = 5;

    private static final int MAX_RETRY_NUM = 3;
    private static final long RETRY_DELAY = Duration.ofSeconds(10)
            .toMillis();
    private static final long MAX_RETRY_DELAY = Duration.ofMinutes(10)
            .toMillis();

    @Override
    public void configure() throws Exception {
        /// Configure exception-specific handler
        onException(HttpOperationFailedException.class, NettyHttpOperationFailedException.class)
//                .onWhen(header("CamelHttpResponseCode").isEqualTo(404))
                /// retry config
                .maximumRedeliveries(MAX_RETRY_NUM)
                .maximumRedeliveryDelay(MAX_RETRY_DELAY)
                .redeliveryDelay(RETRY_DELAY)
                /// exponential back-off
                .useExponentialBackOff()
                .backOffMultiplier(2)
                /// log retry message
                .logRetryAttempted(true)
                .retryAttemptedLogLevel(LoggingLevel.WARN)
                .log("Exception: ${exception.statusCode} ${exception.responseBody}")
                .to("log:error?showCaughtException=true&showStackTrace=true")
                .handled(true);

        /*
         * Camel Route Definitions
         */

        /// Route to download data for all countries within a time range
        from("direct:downloadForumDataWithinTimeRange").routeId("download-within-time-range")
                /// Prepare date time to download
                .process(new BeforeTimeRangeDownloadProcessor())
                .log("BEGIN: trigger parallel download for ${body} within the time range ${exchangeProperty.since} - ${exchangeProperty.until}")
                /// Process download for each country in parallel
                /// "parallelProcessing().synchronous()" supported by 3.21 forces the use of calling thread after parallel processing ends
                .split(body()).parallelProcessing()
                    .setProperty("country", body())
                    /// Download all data for a country within a time range
                    .to("direct:paginateForumDataByCountry")
                .end()
                /// After all data is downloaded within the time range
                .process(new AfterTimeRangeDownloadProcessor())
                .log("END: data from ${body} are downloaded within the time range")
                .end();

        /// Route to download all pages for a country
        from("direct:paginateForumDataByCountry").routeId("api-pagination")
                .loopDoWhile(PredicateBuilder.or(exchangeProperty(PROP_IN_PAGINATION).isNull(),
                        exchangeProperty(PROP_IN_PAGINATION)))
                /// Allow breaking the loop when shutting down
                .breakOnShutdown()
                /// Prepare request data to be sent out
                .process(new RequestProcessor())
                .setHeader("Content-Type", constant("application/json; charset=utf-8"))
                .setHeader("Accept", constant("application/json; charset=utf-8"))
                .setHeader("Authorization", constant("apikey {{twingly.apikey}}"))
                .setHeader(Exchange.HTTP_METHOD, constant("POST"))
                /// Config a throttler
                .throttle(MAX_REQ_PER_SEC)
                .asyncDelayed() /// avoid blocking current thread when throttled
                .log("Sending HTTP request for ${exchangeProperty.country}, payload: ${body}")
                /// Send pagination count to metrics registry
                .toD("micrometer:counter:pagination.count?tags=id=${exchangeId},country=${exchangeProperty.country}")
                .to("{{twingly.endpoint}}")
                .unmarshal().json(JsonLibrary.Jackson, TwinglyForumResponse.class)
                /// Handle HTTP response, and decide whether there is next page
                .process(new ResponseProcessor())
                .log("HTTP response received, country: ${exchangeProperty.country}, offset: ${exchangeProperty.offset}")
                .to("direct:processResponse");

        /// Route to handle docs in the HTTP response
        from("direct:processResponse").routeId("response-handler")
                //.setHeader("", constant(""))
                /// Split all posts (TwinglyForumDocument)
                .split(body())
                .streaming()
                /// Process each post in parallel
                .parallelProcessing()
                //.convertBodyTo(String.class)
                /// Patch timestamp to each TwinglyDocument
                .bean(PatchTimestampBean.class)
                .marshal()
                .json(JsonLibrary.Jackson, TwinglyForumDocument.class)
                /// Append JSON object to a file (%0A is the URL encoded line-break)
                .toD("file:data?fileName=data-${date:now:yyyy-MM-dd-HH}.json&fileExist=Append&appendChars=%0A")
                .end();
    }

    /*
     * Processor Definitions
     */
    class BeforeTimeRangeDownloadProcessor implements Processor {
        @Override
        public void process(Exchange exchange) {
            // Calculate download range from last download time to current time
            long since = exchange.getProperty("lastDownloadTime", Long.class);
            // limit until to keep range small
            long until = Math.min(System.currentTimeMillis(), since + 600000);
            exchange.setProperty("since", since);
            exchange.setProperty("until", until);
        }
    }

    class AfterTimeRangeDownloadProcessor implements Processor {
        @Override
        public void process(Exchange exchange) throws IOException {
            // Save current time as last download time
            File lastDownloadFile = new File("last_download_time.txt");
            Files.write(lastDownloadFile.toPath(),
                    exchange.getProperty("until")
                            .toString()
                            .getBytes());
        }
    }

    class RequestProcessor implements Processor {
        @Override
        public void process(Exchange exchange) {
            String country = exchange.getProperty("country", String.class);

            // Calculate download range from last download time to current time
            long since = exchange.getProperty("since", Long.class);
            long until = exchange.getProperty("until", Long.class);
            int offset = exchange.getProperty("offset", 0, Integer.class);

            TwinglyForumRequest requestInfo = new TwinglyForumRequest(since,
                    until,
                    Country.valueOf(country),
                    offset);

            exchange.getIn()
                    .setBody(Utils.getRequestBody(requestInfo));
        }
    }

    class ResponseProcessor implements Processor {

        @Override
        public void process(Exchange exchange) {
            int offset = exchange.getProperty("offset", 0, Integer.class);

            // Decide if we need to do pagination
            TwinglyForumResponse response = exchange.getIn().getBody(TwinglyForumResponse.class);
            int totalMatches = response.getNumberOfMatchesTotal();
            int matchesReturned = response.getNumberOfMatchesReturned();

            boolean isPaginating = (totalMatches > Utils.PAGE_SIZE) && (offset + matchesReturned < totalMatches);
            exchange.setProperty(PROP_IN_PAGINATION, isPaginating);

            // Update offset to the next page
            if (isPaginating) {
                exchange.setProperty("offset", offset + Utils.PAGE_SIZE);
            }

            exchange.getIn().setBody(response.getDocuments());
        }
    }

    class PatchTimestampBean {
        public static TwinglyForumDocument map(TwinglyForumDocument document) {
            document.setNbReceiveTime(System.currentTimeMillis());
            return document;
        }
    }
}
