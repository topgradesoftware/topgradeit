package topgrade.parent.com.parentseeks.Parent.Model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import topgrade.parent.com.parentseeks.Shared.Models.SharedStatus;

import java.lang.reflect.Type;

/**
 * Custom deserializer for StatusModel to handle empty status objects.
 * When API returns {"status":{}}, this ensures a SharedStatus object is created
 * with null fields instead of leaving statusModel.status as null.
 */
public class StatusModelDeserializer implements JsonDeserializer<StatusModel> {

    @Override
    public StatusModel deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        
        StatusModel statusModel = new StatusModel();
        
        if (json == null || json.isJsonNull()) {
            // If null, return StatusModel with null status
            return statusModel;
        }
        
        // When Gson calls this deserializer for StatusModel, it passes the JSON element
        // that corresponds to the status field value. So if the JSON is {"status":{}},
        // this deserializer receives just {} (the value of the status field).
        if (!json.isJsonObject()) {
            // If not an object, return StatusModel with null status
            return statusModel;
        }
        
        JsonObject statusObject = json.getAsJsonObject();
        
        // Always create a SharedStatus object, even if the object is empty {}
        // This ensures statusModel.status is never null when a status object exists
        SharedStatus sharedStatus = new SharedStatus();
        
        // Extract fields if they exist
        if (statusObject.has("code")) {
            JsonElement codeElement = statusObject.get("code");
            if (codeElement != null && !codeElement.isJsonNull()) {
                sharedStatus.setCode(codeElement.getAsString());
            }
        }
        
        if (statusObject.has("message")) {
            JsonElement messageElement = statusObject.get("message");
            if (messageElement != null && !messageElement.isJsonNull()) {
                sharedStatus.setMessage(messageElement.getAsString());
            }
        }
        
        // Note: "status" field in SharedStatus is a String, not nested object
        if (statusObject.has("status")) {
            JsonElement statusStringElement = statusObject.get("status");
            if (statusStringElement != null && !statusStringElement.isJsonNull()) {
                sharedStatus.setStatus(statusStringElement.getAsString());
            }
        }
        
        if (statusObject.has("data")) {
            JsonElement dataElement = statusObject.get("data");
            if (dataElement != null && !dataElement.isJsonNull()) {
                sharedStatus.setData(dataElement);
            }
        }
        
        // Always set the SharedStatus object, even if all fields are null
        // This is the key fix: ensure statusModel.status is never null when status object exists
        statusModel.status = sharedStatus;
        
        return statusModel;
    }
}

