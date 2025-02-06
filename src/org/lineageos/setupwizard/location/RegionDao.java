package org.lineageos.setupwizard.location;

import android.database.Cursor;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface RegionDao {
    @Query("select * from REGION_INFO ORDER BY COUNTRY_NAME DESC")
    List<RegionInfo> getAllAddress();

    @Query("SELECT DISTINCT  COUNTRY_NAME  FROM REGION_INFO")
    List<String> getAllZhCoutry();

    @Query("SELECT DISTINCT  COUNTRY_NAME,COUNTRY_NAME_EN  FROM REGION_INFO")
    Cursor getAllCoutry();
    @Query("SELECT DISTINCT  COUNTRY_NAME_EN  FROM REGION_INFO")
    List<String> getAllEnCoutry();

    @Query("SELECT DISTINCT  PROVINCE_NAME  FROM REGION_INFO WHERE COUNTRY_NAME = :courtyName")
    List<String> getAllZhProvincesByCoutryId(String courtyName);

    @Query("SELECT DISTINCT  PROVINCE_NAME_EN  FROM REGION_INFO WHERE COUNTRY_NAME_EN = :courtyName")
    List<String> getAllEnProvincesByCoutryId(String courtyName);


    @Query("SELECT DISTINCT  PROVINCE_NAME,PROVINCE_NAME_EN  FROM REGION_INFO")
    Cursor getAllProvinces();

    @Query("select * from REGION_INFO WHERE PROVINCE_NAME = :provinceName")
    List<RegionInfo> getAllCitysByProvinceId(String provinceName);

    @Query("select * from REGION_INFO WHERE PROVINCE_NAME_EN = :provinceName")
    List<RegionInfo> getAllEnCitysByProvinceId(String provinceName);

    @Query("select * from REGION_INFO WHERE PROVINCE_NAME = :provinceName")
    Cursor getAllCitys(String provinceName);

    @Query("DELETE FROM REGION_INFO")
    void deleteAll();

    @Insert
    void insert(RegionInfo regionInfo);
}
