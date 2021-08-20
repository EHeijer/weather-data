package com.edheijer.weatherclient.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.edheijer.weatherclient.models.WeatherData;

public interface WeatherDataRepository extends JpaRepository<WeatherData, Long>{

}
