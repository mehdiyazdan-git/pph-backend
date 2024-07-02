package com.armaninvestment.parsparandreporter.repositories;

import com.armaninvestment.parsparandreporter.entities.AppSetting;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AppSettingRepository extends JpaRepository<AppSetting, Long> {
    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "UPDATE public.app_setting SET\t" +
            "vat = :vat, " +
            "WHERE id = :id ")
    void updateAppSettingById(@Param("vat") Double claims,
                              @Param("id") Long id);
}