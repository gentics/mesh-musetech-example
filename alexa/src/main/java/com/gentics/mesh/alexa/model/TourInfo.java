package com.gentics.mesh.alexa.model;

import java.time.OffsetDateTime;

public class TourInfo {

	private String uuid;
	private String title;
	private String description;
	private String location;
	private OffsetDateTime date;
	private double price;
	private int seats;
	private int size;
	private String dateStr;

	public TourInfo(String uuid, String title, String location, OffsetDateTime date, double price, int seats, int size, String dateStr) {
		this.uuid = uuid;
		this.title = title;
		this.location = location;
		this.date = date;
		this.price = price;
		this.seats = seats;
		this.size = size;
		this.dateStr = dateStr;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getTitle() {
		return title;
	}

	public OffsetDateTime getDate() {
		return date;
	}

	public String getDescription() {
		return description;
	}

	public String getLocation() {
		return location;
	}

	public double getPrice() {
		return price;
	}

	public int getSeats() {
		return seats;
	}

	public int getSize() {
		return size;
	}

	public String getDateStr() {
		return dateStr;
	}

}
