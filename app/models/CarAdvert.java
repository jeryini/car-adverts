package models;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;

import java.util.Date;
import java.util.Map;
import java.util.HashMap;

public class CarAdvert {

    public enum FuelType { GASOLINE, DIESEL, ELECTRIC }

    private String id;
    private String title;
    private FuelType fuel;
    private Integer price;
    private Boolean isNew;
    private Integer mileage;
    private Date first_registration;

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
        this.title = title;
        this.fuel = fuel;
        this.price = price;
        this.isNew = isNew;
        this.mileage = mileage;
        this.first_registration = first_registration;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public FuelType getFuel() {
        return fuel;
    }

    public void setFuel(FuelType fuel) {
        this.fuel = fuel;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public Boolean getNew() {
        return isNew;
    }

    public void setNew(Boolean aNew) {
        isNew = aNew;
    }

    public Integer getMileage() {
        return mileage;
    }

    public void setMileage(Integer mileage) {
        this.mileage = mileage;
    }

    public Date getFirst_registration() {
        return first_registration;
    }

    public void setFirst_registration(Date first_registration) {
        this.first_registration = first_registration;
    }

    public Map<String, AttributeValue> toDynamoMap() {
        Map<String, AttributeValue> map = new HashMap<String, AttributeValue>();
        map.put("title", new AttributeValue(title));
        map.put("fuel", new AttributeValue(fuel.toString()));
        map.put("price", new AttributeValue().withN(price.toString()));
        map.put("isNew", new AttributeValue().withBOOL(isNew));
        map.put("mileage", new AttributeValue().withN(mileage.toString()));
        map.put("first_registration", new AttributeValue(first_registration.toString()));
        return map;
    }
}
