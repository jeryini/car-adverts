package controllers;

import com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.CarAdvert;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import services.DynamoDBService;
import util.Util;

import java.util.List;

public class CarAdvertApiController extends Controller {

    /**
     * Create new advert
     *
     * @return
     */
    public Result create() {
        JsonNode json = request().body().asJson();
        if (json == null){
            return badRequest(Util.createResponse("Expecting Json data", false));
        }

        // Save item into DB
        CarAdvert carAdvert = Json.fromJson(json, CarAdvert.class);
        carAdvert.assignUUID();

        DynamoDBService.saveItem(carAdvert);
        JsonNode jsonObject = Json.toJson(DynamoDBService.getItem(carAdvert.getId()));
        return created(Util.createResponse(jsonObject, true));
    }

    /**
     * Update existing advert.
     *
     * @param id
     * @return
     */
    public Result update(String id) {
        JsonNode json = request().body().asJson();
        if (json == null){
            return badRequest(Util.createResponse("Expecting Json data", false));
        }

        CarAdvert carAdvert = DynamoDBService.getItem(id);
        if (carAdvert == null) {
            return notFound(Util.createResponse("CarAdvert not found", false));
        }

        // update the instance and then save it to DB
        carAdvert.update(json);
        DynamoDBService.saveItem(carAdvert);

        JsonNode jsonObject = Json.toJson(carAdvert);
        return ok(Util.createResponse(jsonObject, true));
    }

    /**
     * Get advert by id.
     *
     * @param id
     * @return
     */
    public Result retrieve(String id) {
        CarAdvert carAdvert = DynamoDBService.getItem(id);
        if (carAdvert == null) {
            return notFound(Util.createResponse("CarAdvert with id:" + id + " not found", false));
        }
        JsonNode jsonObjects = Json.toJson(carAdvert);
        return ok(Util.createResponse(jsonObjects, true));
    }

    /**
     * Delete advert by id
     *
     * @param id
     * @return
     */
    public Result delete(String id) {
        CarAdvert carAdvert;
        try {
            carAdvert = DynamoDBService.getItem(id);
        } catch (ResourceNotFoundException rsne) {
            return notFound(Util.createResponse("CarAdvert with id:" + id + " not found", false));
        }
        DynamoDBService.deleteItem(carAdvert);
        return ok(Util.createResponse("CarAdvert with id:" + id + " deleted", true));
    }

    public Result listCarAdverts() {
        List<CarAdvert> result = DynamoDBService.getAll();
        ObjectMapper mapper = new ObjectMapper();

        JsonNode jsonData = mapper.convertValue(result, JsonNode.class);
        return ok(Util.createResponse(jsonData, true));
    }
}
