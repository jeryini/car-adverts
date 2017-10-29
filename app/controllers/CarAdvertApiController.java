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

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.List;
import java.util.Set;

public class CarAdvertApiController extends Controller {

    private static ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private static Validator validator = factory.getValidator();

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

        // Convert to POJO and assign UUID
        CarAdvert carAdvert = Json.fromJson(json, CarAdvert.class);
        carAdvert.assignUUID();

        // Check for validation errors for All types of cars (user/new)
        Set<ConstraintViolation<CarAdvert>> errors = validator.validate(carAdvert, CarAdvert.All.class);
        if (!errors.isEmpty()) {
            return badRequest(Util.createResponse("Missing fields: " + errors.toString(), false));
        }

        // Check for validation errors only if used
        if (!carAdvert.getIsNew()) {
            Set<ConstraintViolation<CarAdvert>> errorsUsed = validator.validate(carAdvert, CarAdvert.Used.class);
            if (!errorsUsed.isEmpty()) {
                return badRequest(Util.createResponse("Missing fields: " + errors.toString(), false));
            }
        }

        // Save data to DB
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

        carAdvert.update(json);

        Set<ConstraintViolation<CarAdvert>> errors = validator.validate(carAdvert, CarAdvert.All.class);
        if (!errors.isEmpty()) {
            return badRequest(Util.createResponse("Missing fields: " + errors.toString(), false));
        }

        // Check for validation errors only if used
        if (!carAdvert.getIsNew()) {
            Set<ConstraintViolation<CarAdvert>> errorsUsed = validator.validate(carAdvert, CarAdvert.Used.class);
            if (!errorsUsed.isEmpty()) {
                return badRequest(Util.createResponse("Missing fields: " + errorsUsed.toString(), false));
            }
        }

        // update the instance and then save it to DB
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

    /**
     * List all adverts.
     *
     * @return
     */
    public Result listCarAdverts(String sort) {
        List<CarAdvert> result = DynamoDBService.getAll(sort);
        ObjectMapper mapper = new ObjectMapper();

        JsonNode jsonData = mapper.convertValue(result, JsonNode.class);
        return ok(Util.createResponse(jsonData, true));
    }
}
