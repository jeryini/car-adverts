package util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;

public class Util {

    /**
     * Create a standard JSON responses with a boolean isSuccessfull key and the response body.
     *
     * @param response
     * @param ok
     * @return
     */
    public static ObjectNode createResponse(Object response, boolean ok) {
        ObjectNode result = Json.newObject();
        result.put("isSuccessfull", ok);

        if (response instanceof String) {
            result.put("body", (String) response);
        }
        else {
            result.put("body", (JsonNode) response);
        }

        return result;
    }
}
