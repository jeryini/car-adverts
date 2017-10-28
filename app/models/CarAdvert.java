package models;

import java.util.Date;

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
}
