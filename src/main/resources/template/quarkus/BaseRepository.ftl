package [=package];

import [=entityPath];
import io.vertx.core.json.JsonObject;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;

import javax.inject.Inject;
import java.io.IOException;


public abstract class BaseRepository<T extends BaseEntity> {

    @Inject
    RestClient restClient;

    public void index(T data, String index) throws IOException {
        String endpoint = "/".concat(index).concat("/_doc/").concat(String.valueOf(data.id));
        Request request = new Request(
        "PUT",
        endpoint);
        request.setJsonEntity(JsonObject.mapFrom(data).toString());
        restClient.performRequest(request);
    }

    public void delete(T data, String index) throws IOException {
        String endpoint = "/".concat(index).concat("/_doc/").concat(String.valueOf(data.id));
        Request request = new Request(
        "DELETE",
        endpoint);
        restClient.performRequest(request);
    }

    public BaseEntity get(T data, String index) throws IOException {
        String endpoint = "/".concat(index).concat("/_doc/").concat(String.valueOf(data.id));
        Request request = new Request(
        "GET",
        endpoint);
        Response response = restClient.performRequest(request);
        String responseBody = EntityUtils.toString(response.getEntity());
        JsonObject json = new JsonObject(responseBody);
        return json.getJsonObject("_source").mapTo(data.getClass());
    }

    protected abstract String getIndex();

    protected abstract Class<T> getEntityClass();
}
