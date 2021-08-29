package com.edheijer.weatherclient.repositories;

import com.edheijer.weatherclient.models.WeatherData;
import com.edheijer.weatherclient.models.WeatherDataDB;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WeatherDataDBRepository extends JpaRepository<WeatherDataDB, Long>{

}
