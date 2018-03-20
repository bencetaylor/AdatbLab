package model;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.regex.Pattern;

import dal.exceptions.ValidationException;

public class Order234 {
    public Order234() {
    
    }
    private String orderId;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public void parseOrderId(String orderId) throws ValidationException {
    	String reg1 = "[1-9]\\d{3}\\/\\d{6}";
		String compare1 = orderId;
		if (!Pattern.matches(reg1, compare1)) {
			throw new ValidationException("orderId");		}
		else
			setOrderId(orderId);
    }
    
    private String description;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void parseDescription(String description) throws ValidationException {
        setDescription(description);
    }
    
    private String vehicleType;

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public void parseVehicleType(String vehicleType) throws ValidationException {
        setVehicleType(vehicleType);
    }
    
    private Integer quantity;

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public void parseQuantity(String quantity) throws ValidationException {
        //TODO: validate
    	String reg1 = "[1-9]\\d*";
		// A megvizsgalando String
		String compare1 = quantity;
		// Itt tortenik az osszehasonlitas
		if (!Pattern.matches(reg1, compare1))
			throw new ValidationException("quantity");
		else 
			setQuantity(Integer.parseInt(quantity));
    }
    
    private String origin;

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public void parseOrigin(String origin) throws ValidationException {
        setOrigin(origin);
    }
    private String destination;

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public void parseDestination(String destination) throws ValidationException {
        setDestination(destination);
    }
    
    private LocalDate deadline;

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public void parseDeadline(String deadline) throws ValidationException, ParseException {
    	String reg2 = "[1-9]\\d{3}\\-[0-1]\\d\\-[0-3]\\d";
    	String compare2 = deadline;
    	if (!Pattern.matches(reg2, compare2)) {
    		throw new ValidationException("deadline");
    	}
    	else {
    		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    		java.util.Date date0;
    		date0 = sdf.parse(deadline);
    		java.sql.Date date1 = new Date(date0.getTime());
    		LocalDate ld = date1.toLocalDate();
    		setDeadline(ld);
		}
    }

}