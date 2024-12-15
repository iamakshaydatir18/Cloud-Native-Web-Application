package serverless.lambda.function;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.time.LocalDateTime;

public class CustomLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {
    @Override
    public LocalDateTime deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        JsonNode node = jp.getCodec().readTree(jp);
        int[] dateTimeComponents = new int[7];
        for (int i = 0; i < 7; i++) {
            dateTimeComponents[i] = node.get(i).asInt();
        }
        return LocalDateTime.of(
            dateTimeComponents[0], dateTimeComponents[1], dateTimeComponents[2],
            dateTimeComponents[3], dateTimeComponents[4], dateTimeComponents[5],
            dateTimeComponents[6]
        );
    }
}
