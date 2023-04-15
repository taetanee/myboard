package com.web.dto;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "STWeather")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class STWeatherDTO {
	private String baseDate;
	private String baseTime;
	private String category;

	private int nx;
	private int ny;

	private String obsrValue;
}
