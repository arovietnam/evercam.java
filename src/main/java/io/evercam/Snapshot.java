package io.evercam;


import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Snapshot {
    private JSONObject snapshotJSONObject;

    Snapshot(JSONObject snapshotJSONObject)
    {
        this.snapshotJSONObject = snapshotJSONObject;
    }

    public JSONObject getUris() throws JSONException {
        return snapshotJSONObject.getJSONObject("uris");
    }

    public JSONObject getFormats() throws JSONException {
        return snapshotJSONObject.getJSONObject("formats");
    }

    public JSONObject getAuth() throws JSONException {
        return snapshotJSONObject.getJSONObject("auth");
    }

    public static Snapshot getSnapshot(String stream) throws EvercamException {
        Snapshot snapshot;
        try
        {
            HttpResponse<JsonNode> response = Unirest.get(Stream.URL + '/' + stream + "/snapshots/new").header("accept", "application/json").asJson();
            JSONObject modelJSONObject = response.getBody().getObject();
            snapshot = new Snapshot(modelJSONObject);
        }
        catch (UnirestException e)
        {
            throw new EvercamException(e);
        }
        return snapshot;
    }
}
