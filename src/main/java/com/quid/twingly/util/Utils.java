package com.quid.twingly.util;

import com.google.gson.JsonObject;
import com.quid.twingly.model.TwinglyForumRequest;
import org.apache.commons.lang3.time.FastDateFormat;

import java.util.TimeZone;

public class Utils {

    public static final int PAGE_SIZE = 100;
    private static final String ORDER = "asc";
    private static final FastDateFormat DATE_FORMAT = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss'Z'",
            TimeZone.getTimeZone("UTC"));

    static public String getRequestBody(TwinglyForumRequest requestInfo) {
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("all_documents_for_location",
                requestInfo.country()
                        .getCode());
        requestBody.addProperty("size", PAGE_SIZE);
        requestBody.addProperty("offset", requestInfo.offset());
        requestBody.addProperty("order", ORDER);

        JsonObject insertedTime = new JsonObject();
        insertedTime.addProperty("since", formatDate(requestInfo.from()));
        insertedTime.addProperty("until", formatDate(requestInfo.to()));

        requestBody.add("inserted_time", insertedTime);
        return requestBody.toString();
    }

    static private String formatDate(long timestamp) {
        return DATE_FORMAT.format(timestamp);
    }
}
