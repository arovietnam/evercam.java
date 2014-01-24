package io.evercam;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;


public class Camera extends EvercamObject {
    static String URL = API.URL + "cameras";

    Camera(JSONObject cameraJSONObject)
    {
        this.jsonObject = cameraJSONObject;
    }

    public static Camera create(CameraDetail cameraDetail) throws EvercamException
    {
        Camera camera = null;
        if(API.isAuth())
        {
        try
        {
            JSONObject cameraJSONObject = buildCameraJSONObject(cameraDetail) ;
            DefaultHttpClient c = new DefaultHttpClient();
            c.getCredentialsProvider().setCredentials(
                    new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
                    new UsernamePasswordCredentials(API.getAuth()[0], API.getAuth()[1]));
            HttpPost post = new HttpPost(URL);
            post.setHeader("Content-type", "application/json");
            post.setEntity(new StringEntity(cameraJSONObject.toString()));
            org.apache.http.HttpResponse r = c.execute(post);
            JsonNode jsonNode = new JsonNode(EntityUtils.toString(r.getEntity()));
            JSONObject jsonObject = jsonNode.getObject().getJSONArray("cameras").getJSONObject(0);
            return new Camera(jsonObject);

        } catch (JSONException e)
        {
            throw new EvercamException(e);
        } catch (ClientProtocolException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        }
        else
        {
            throw new EvercamException("Auth is necessary for creating camera");
        }
        return camera;
    }

    public static Camera getById(String cameraId) throws EvercamException
    {
        Camera camera;
        try
        {
            HttpResponse<JsonNode> response;
            if(API.isAuth())
            {
                response = Unirest.get(URL + '/' + cameraId).header("accept", "application/json").basicAuth(API.getAuth()[0],API.getAuth()[1]).asJson();
            }
            else
            {
                response = Unirest.get(URL + '/' + cameraId).header("accept", "application/json").asJson();
            }
            JSONObject userJSONObject = response.getBody().getObject().getJSONArray("cameras").getJSONObject(0);
            camera = new Camera(userJSONObject);
        }
        catch (JSONException e)
        {
            throw new EvercamException(e);
        }
        catch (UnirestException e)
        {
            throw new EvercamException(e);
        }
        return camera;
    }

    public ArrayList<String> getEndpoints()
    {
        ArrayList<String> endpoints = new ArrayList<String>();
        try
        {
            JSONArray endpointJSONArray = jsonObject.getJSONArray("endpoints");
            for(int i=0; i<endpointJSONArray.length(); i++)
            {
                endpoints.add(endpointJSONArray.getString(i));
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return endpoints;
    }

    public String getId() throws EvercamException
    {
        try
        {
            return jsonObject.getString("id");
        } catch (JSONException e)
        {
            throw new EvercamException(e);
        }
    }

    public String getOwner() throws EvercamException
    {
        try
        {
            return jsonObject.getString("owner");
        } catch (JSONException e)
        {
            throw new EvercamException(e);
        }
    }

    public boolean isPublic() throws EvercamException
    {
        try
        {
            return jsonObject.getBoolean("is_public");
        } catch (JSONException e)
        {
            throw new EvercamException(e);
        }
    }

    public Auth getAuth(String type) throws EvercamException
    {
        Auth auth;
        try
        {
            JSONObject authJSONObject = jsonObject.getJSONObject("auth").getJSONObject(type);
            auth = new Auth(type,authJSONObject);
        }
        catch (JSONException e)
        {
            throw new EvercamException(e);
        }
        return auth;
    }

    public String getName() throws EvercamException
    {
        try
        {
            return jsonObject.getString("name");
        } catch (JSONException e)
        {
            throw new EvercamException(e);
        }
    }

    public String getVendor() throws EvercamException
    {
        try
        {
            return jsonObject.getString("vendor");
        } catch (JSONException e)
        {
            throw new EvercamException(e);
        }
    }

    public String getTimezone() throws EvercamException
    {
        try
        {
            return jsonObject.getString("timezone");
        } catch (JSONException e)
        {
            throw new EvercamException(e);
        }
    }

    public String getModel() throws EvercamException
    {
        try
        {
            return jsonObject.getString("model");
        } catch (JSONException e)
        {
            throw new EvercamException(e);
        }
    }

    public String isOnline() throws EvercamException
    {
        try
        {
            return jsonObject.getString("is_online");
        } catch (JSONException e)
        {
            throw new EvercamException(e);
        }
    }

    public String getSnapshotPath(String type) throws EvercamException
    {
        try
        {
            return jsonObject.getJSONObject("snapshots").getString(type);
        } catch (JSONException e)
        {
            throw new EvercamException(e);
        }
    }

    public InputStream getSnapshotStream() throws EvercamException
    {
        InputStream inputStream;
        String endpoint = selectEndpoint();
        if(endpoint != null)
        {
            String url = endpoint + getSnapshotPath("jpg");
            try
            {
                HttpResponse<String> response = Unirest.get(url).basicAuth(getAuth(Auth.TYPE_BASIC).getUsername(),getAuth(Auth.TYPE_BASIC).getPassword()).asString();
                inputStream = response.getRawBody();
            }
            catch (UnirestException e)
            {
                throw new EvercamException(e);
            }
        }
        else
        {
            throw new EvercamException("Endpoint not available");
        }
        return inputStream;
    }

    private String selectEndpoint() throws EvercamException
    {
        String snapshot = getSnapshotPath("jpg");

        for (String endpoint: getEndpoints())
        {
            String url = getFullURL(endpoint,snapshot);
            try
            {
                HttpResponse<String> response = Unirest.get(url).asString();
                if(response.getCode()!=400)
               {
                    return endpoint;
               }
            } catch (UnirestException e)
            {
                throw new EvercamException(e);
            }
        }
        return null;
    }

    private String getFullURL(String endpoint,String snapshot)
    {
        if(endpoint.endsWith("/") && snapshot.startsWith("/"))
        {
            endpoint = endpoint.substring(0,endpoint.lastIndexOf("/"));
        }
        return  endpoint + snapshot;
    }

    private static JSONObject buildCameraJSONObject(CameraDetail cameraDetail) throws JSONException
    {
        JSONObject cameraJSONObject = new JSONObject();
        JSONObject authJSONObject = new JSONObject();
        JSONObject basicJSONObject = new JSONObject();
        JSONObject snapshotJSONObject = new JSONObject();
        snapshotJSONObject.put("jpg", cameraDetail.getSnapshotJPG());
        basicJSONObject.put("username", cameraDetail.getBasicAuth()[0]);
        basicJSONObject.put("password", cameraDetail.getBasicAuth()[1]);
        authJSONObject.put("basic",basicJSONObject);

        cameraJSONObject.put("id", cameraDetail.getId());
        cameraJSONObject.put("name", cameraDetail.getName());
        cameraJSONObject.put("model", cameraDetail.getModel());
        cameraJSONObject.put("vendor",cameraDetail.getVendor());
        cameraJSONObject.put("timezone",cameraDetail.getTimezone());
        cameraJSONObject.put("is_public", true);
        cameraJSONObject.put("snapshots", snapshotJSONObject);
        cameraJSONObject.put("endpoints", cameraDetail.getEndpoints());
        cameraJSONObject.put("auth",authJSONObject);

        return cameraJSONObject;
    }
}
