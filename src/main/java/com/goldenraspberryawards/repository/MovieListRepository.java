package com.goldenraspberryawards.repository;

import com.goldenraspberryawards.model.MovieList;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovieListRepository extends JpaRepository<MovieList, Long>
{
    // A ideia aqui é usar Query Methods ao invés de uma query SQL
    // A query poderia ser assim: @Query("SELECT m FROM MovieList m WHERE m.winner = true")
    List<MovieList> findByWinnerTrue();
}
