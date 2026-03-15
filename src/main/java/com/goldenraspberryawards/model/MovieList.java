package com.goldenraspberryawards.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "movielist")
public class MovieList
{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "release_year")
    private Integer year;

    @Column(length = 500)
    private String title;

    @Column(length = 1000)
    private String studios;

    @ManyToMany
    @JoinTable(name = "movielist_produtores", joinColumns = @JoinColumn(name = "movielist_id"), inverseJoinColumns = @JoinColumn(name = "produtor_id"))
    private List<Produtor> producers = new ArrayList<>();

    private Boolean winner;

    public MovieList()
    {
    }

    public MovieList(Integer year, String title, String studios, List<Produtor> producers, Boolean winner)
    {
        this.year = year;
        this.title = title;
        this.studios = studios;
        if (producers != null)
        {
            this.producers = new ArrayList<>(producers);
        }
        this.winner = winner;
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Integer getYear()
    {
        return year;
    }

    public void setYear(Integer year)
    {
        this.year = year;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getStudios()
    {
        return studios;
    }

    public void setStudios(String studios)
    {
        this.studios = studios;
    }

    public List<Produtor> getProducers()
    {
        return producers;
    }

    public void setProducers(List<Produtor> producers)
    {
        this.producers = producers != null ? new ArrayList<>(producers) : new ArrayList<>();
    }

    public Boolean getWinner()
    {
        return winner;
    }

    public void setWinner(Boolean winner)
    {
        this.winner = winner;
    }
}
