package com.goldenraspberryawards.repository;

import com.goldenraspberryawards.model.MovieList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MovieListRepository extends JpaRepository<MovieList, Long>
{
    @Query("SELECT DISTINCT m FROM MovieList m JOIN FETCH m.producers WHERE m.winner = true ORDER BY m.year ASC")
    List<MovieList> findByWinnerTrue();
}
