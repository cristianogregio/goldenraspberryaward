package com.goldenraspberryawards.repository;

import com.goldenraspberryawards.model.Produtor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProdutorRepository extends JpaRepository<Produtor, Long>
{
    //"SELECT p FROM Produtor p WHERE p.name = :name")
    Optional<Produtor> findByName(String name);
}
