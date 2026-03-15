package com.goldenraspberryawards.service;

import com.goldenraspberryawards.api.dto.ProducerIntervalRecord;
import com.goldenraspberryawards.api.dto.ProducerIntervalResponse;
import com.goldenraspberryawards.model.MovieList;
import com.goldenraspberryawards.model.Produtor;
import com.goldenraspberryawards.repository.MovieListRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * Retorna quem teve o menor e o maior intervalo entre dois prêmios.
 */
@Service
public class ProducerIntervalService
{

    private final MovieListRepository movieListRepository;

    public ProducerIntervalService(MovieListRepository movieListRepository)
    {
        this.movieListRepository = movieListRepository;
    }

    /**
     * Obtém os intervalos de vitórias dos produtores: lista de quem venceu mais rápido (menor intervalo)
     * e lista de quem demorou mais entre duas vitórias (maior intervalo).
     */
    @Transactional(readOnly = true)
    public ProducerIntervalResponse getProducerIntervals()
    {
        // Só os vencedores de cada ano
        List<MovieList> winningMovies = movieListRepository.findByWinnerTrue(); 

        // Monta mapa: nome do produtor -> anos em que venceu (ordenados, sem repetição)
        Map<String, TreeSet<Integer>> producerWinYears = new LinkedHashMap<>(); 
        for (MovieList movie : winningMovies)
        {
            int year = movie.getYear();
            for (Produtor p : movie.getProducers())
            {
                String name = p.getName();
                producerWinYears.computeIfAbsent(name, k -> new TreeSet<>()).add(year);
            }
        }

        // Para cada produtor com 2+ vitórias, calcula o intervalo entre vitórias consecutivas
        List<ProducerIntervalRecord> allIntervals = new ArrayList<>();
        for (Map.Entry<String, TreeSet<Integer>> e : producerWinYears.entrySet())
        {
            String producer = e.getKey();
            List<Integer> years = new ArrayList<>(e.getValue());
            if (years.size() < 2) continue; // Se o produtor tiver menos de 2 vitórias, não calcula o intervalo.
            for (int i = 0; i < years.size() - 1; i++)
            {
                int prev = years.get(i);
                int next = years.get(i + 1);
                int interval = next - prev;
                allIntervals.add(new ProducerIntervalRecord(producer, interval, prev, next));
            }
        }

        if (allIntervals.isEmpty())
        {
            return new ProducerIntervalResponse(List.of(), List.of());
        }

        // Menor e maior intervalo encontrados
        int minInterval = allIntervals.stream().mapToInt(ProducerIntervalRecord::interval).min().orElseThrow();
        int maxInterval = allIntervals.stream().mapToInt(ProducerIntervalRecord::interval).max().orElseThrow();

        // Filtra e ordena: quem teve o menor intervalo; quem teve o maior intervalo
        List<ProducerIntervalRecord> minList = allIntervals.stream()
                .filter(r -> r.interval() == minInterval)
                .sorted(Comparator.comparing(ProducerIntervalRecord::producer)
                        .thenComparing(ProducerIntervalRecord::previousWin))
                .toList();
        List<ProducerIntervalRecord> maxList = allIntervals.stream()
                .filter(r -> r.interval() == maxInterval)
                .sorted(Comparator.comparing(ProducerIntervalRecord::producer)
                        .thenComparing(ProducerIntervalRecord::previousWin))
                .toList();

        return new ProducerIntervalResponse(
                Collections.unmodifiableList(minList),
                Collections.unmodifiableList(maxList)
        );
    }
}
