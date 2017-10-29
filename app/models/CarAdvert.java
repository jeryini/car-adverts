package models;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConvertedEnum;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import play.data.validation.Constraints;
import util.JsonDateSerializer;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@DynamoDBTable(tableName="AdvertsCatalog")
public class CarAdvert {

    // interfaces defining two groups of All and Used cars
    public interface All {}
    public interface Used {}

    public enum FuelType { GASOLINE, DIESEL, ELECTRIC }
    private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    private String id;
    @Constraints.Required(groups = All.class)
    private String title;
    @Constraints.Required(groups = All.class)
    private FuelType fuel;
    @Constraints.Required(groups = All.class)
    private Integer price;
    @Constraints.Required(groups = All.class)
    private Boolean isNew;
    @Constraints.Required(groups = Used.class)
    private Integer mileage;
    @Constraints.Required(groups = Used.class)
    private Date first_registration;

    public CarAdvert() {}

    public CarAdvert(String id, String title, FuelType fuel, Integer price, Boolean isNew, Integer mileage,
                     Date first_registration) {
        this.id = id;
        this.title = title;
        this.fuel = fuel;
        this.price = price;
        this.isNew = isNew;
        this.mileage = mileage;
        this.first_registration = first_registration;
    }

    public CarAdvert(String title, FuelType fuel, Integer price, Boolean isNew, Integer mileage,
                     Date first_registration) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.fuel = fuel;
        this.price = price;
        this.isNew = isNew;
        this.mileage = mileage;
        this.first_registration = first_registration;
    }

    @DynamoDBHashKey(attributeName="Id")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @DynamoDBAttribute(attributeName="Title")
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @DynamoDBAttribute(attributeName="Fuel")
    @DynamoDBTypeConvertedEnum
    public FuelType getFuel() {
        return fuel;
    }

    public void setFuel(FuelType fuel) {
        this.fuel = fuel;
    }

    @DynamoDBAttribute(attributeName="Price")
    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    @DynamoDBAttribute(attributeName="IsNew")
    public Boolean getIsNew() {
        return isNew;
    }

    public void setIsNew(Boolean isNew) {
        this.isNew = isNew;
    }

    @DynamoDBAttribute(attributeName="Mileage")
    public Integer getMileage() {
        return mileage;
    }

    public void setMileage(Integer mileage) {
        this.mileage = mileage;
    }

    @DynamoDBAttribute(attributeName="FirstRegistration")
    @JsonSerialize(using=JsonDateSerializer.class)
    public Date getFirst_registration() {
        return first_registration;
    }

    public void setFirst_registration(Date first_registration) {
        this.first_registration = first_registration;
    }

    public void assignUUID() {
        this.id = UUID.randomUUID().toString();
    }

    /**
     * Updates POJO object with values defined in JSON.
     *
     * @param values
     */
    public void update(JsonNode values) {
        if (values.has("title")) {
            this.title = values.get("title").textValue();
        }
        if (values.has("fuel")) {
            try {
                this.fuel = FuelType.valueOf(values.get("fuel").textValue());
            } catch (IllegalArgumentException | NullPointerException ex) {
                this.fuel = null;
            }
        }
        if (values.has("price")) {
            this.price = values.get("price").intValue();
        }
        if (values.has("isNew")) {
            this.isNew = values.get("isNew").booleanValue();
        }
        if (values.has("mileage")) {
            this.mileage = values.get("mileage").intValue();
        }
        if (values.has("first_registration")) {
            Date first_registration = null;
            try {
                first_registration = dateFormat.parse(values.get("first_registration").textValue());
                this.first_registration = first_registration;
            } catch (ParseException | NullPointerException e) {
                this.first_registration = null;
            }
        }
    }

    /**
     * Mapper for translating POJO to DynamoDB map for creating requests via Document API.
     *
     * @return
     */
    public Map<String, AttributeValue> toDynamoMap() {
        Map<String, AttributeValue> map = new HashMap<String, AttributeValue>();
        map.put("id", new AttributeValue(id));
        map.put("title", new AttributeValue(title));
        map.put("fuel", new AttributeValue(fuel.toString()));
        map.put("price", new AttributeValue().withN(price.toString()));
        map.put("isNew", new AttributeValue().withBOOL(isNew));
        map.put("mileage", new AttributeValue().withN(mileage.toString()));
        map.put("first_registration", new AttributeValue(dateFormat.format(first_registration)));
        return map;
    }

    /**
     * Mapper for translating DynamoDB map to POJO when getting response via Document API.
     *
     * @param item
     * @return
     * @throws ParseException
     */
    public static CarAdvert fromDynamoMapAttribute(Map<String, AttributeValue> item) throws ParseException {
        Date first_registration = dateFormat.parse(item.get("first_registration").getS());

        return new CarAdvert(
            item.get("id").getS(),
            item.get("title").getS(),
            FuelType.valueOf(item.get("fuel").getS()),
            Integer.parseInt(item.get("price").getN()),
            item.get("isNew").getBOOL(),
            Integer.parseInt(item.get("mileage").getN()),
            first_registration
        );
    }

    /**
     * Mapper for translating POJO to item for creating requests via Document API.
     *
     * @param item
     * @return
     * @throws ParseException
     */
    public static CarAdvert fromDynamoItem(Item item) throws ParseException {
        Date first_registration = dateFormat.parse(item.getString("first_registration"));

        return new CarAdvert(
                item.getString("id"),
                item.getString("title"),
                FuelType.valueOf(item.getString("fuel")),
                item.getInt("price"),
                item.getBoolean("isNew"),
                item.getInt("mileage"),
                first_registration
        );
    }


}
