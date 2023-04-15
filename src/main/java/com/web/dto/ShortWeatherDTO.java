package com.web.dto;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "ShortWeather")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ShortWeatherDTO {
	private String baseDate;
	private String baseTime;
	private String category;

	private int nx;
	private int ny;

	private String obsrValue;
}
