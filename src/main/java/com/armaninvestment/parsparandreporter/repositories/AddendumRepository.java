package com.armaninvestment.parsparandreporter.repositories;


import com.armaninvestment.parsparandreporter.entities.Addendum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AddendumRepository extends JpaRepository<Addendum, Long> {
}