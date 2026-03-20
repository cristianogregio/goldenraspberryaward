package com.goldenraspberryawards.service;

import com.goldenraspberryawards.api.dto.ProducerIntervalRecord;
import com.goldenraspberryawards.api.dto.ProducerIntervalResponse;
import com.goldenraspberryawards.model.MovieList;
//import com.goldenraspberryawards.model.Produtor;
import com.goldenraspberryawards.repository.MovieListRepository;

//import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
//import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.SortedSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

        // 1. Busca apenas filmes vencedores
        List<MovieList> winningMovies = movieListRepository.findByWinnerTrue();

        // 2. Mapeia produtor -> anos em que venceu
        //    - TreeSet garante ordenação automática e remoção de duplicados
        Map<String, SortedSet<Integer>> producerWinYears =
                winningMovies.stream()
                        .flatMap(movie -> movie.getProducers().stream()
                                .map(producer -> Map.entry(producer.getName(), movie.getYear())))
                        .collect(Collectors.groupingBy(
                                Map.Entry::getKey,
                                LinkedHashMap::new, 
                                Collectors.mapping(
                                        Map.Entry::getValue,
                                        Collectors.toCollection(TreeSet::new)
                                )
                        ));

        // 3. Calcula os intervalos entre vitorias consecutivas de cada produtor
        List<ProducerIntervalRecord> intervals =
                producerWinYears.entrySet().stream()
                        // ignora produtores com menos de 2 vitórias
                        .filter(entry -> entry.getValue().size() > 1)
                        .flatMap(entry ->
                        {
                            String producer = entry.getKey();
                            List<Integer> years = new ArrayList<>(entry.getValue());

                            // cria pares consecutivos (ano atual vs próximo)
                            return IntStream.range(0, years.size() - 1)
                                    .mapToObj(i ->
                                    {
                                        int previous = years.get(i);
                                        int next = years.get(i + 1);
                                        int interval = next - previous;

                                        return new ProducerIntervalRecord(
                                                producer,
                                                interval,
                                                previous,
                                                next
                                        );
                                    });
                        })
                        .toList();

        // 4. Caso noo existam intervalos validos
        if (intervals.isEmpty())
        {
            return new ProducerIntervalResponse(List.of(), List.of());
        }

        // 5. Determina menor e maior intervalo
        int minInterval = intervals.stream()
                .mapToInt(ProducerIntervalRecord::interval)
                .min()
                .orElse(0);

        int maxInterval = intervals.stream()
                .mapToInt(ProducerIntervalRecord::interval)
                .max()
                .orElse(0);

        // 6. Define ordenação padrão: primeiro por produtor / depois pelo ano da vitoria anterior
        Comparator<ProducerIntervalRecord> comparator =
                Comparator.comparing(ProducerIntervalRecord::producer)
                          .thenComparing(ProducerIntervalRecord::previousWin);

        // 7. Filtra produtores com menor intervalo
        List<ProducerIntervalRecord> minList =
                intervals.stream()
                        .filter(i -> i.interval() == minInterval)
                        .sorted(comparator)
                        .toList();

        // 8. Filtra produtores com maior intervalo
        List<ProducerIntervalRecord> maxList =
                intervals.stream()
                        .filter(i -> i.interval() == maxInterval)
                        .sorted(comparator)
                        .toList();

        // 9. Retorna resposta (listas já são imutáveis com toList())
        return new ProducerIntervalResponse(minList, maxList);
    }

    // Minha opnião após refatorar é que às vezes o "for de for"  é mais legivel que o stream rsrs
    // Streams tem o problema de ter que ficar decorrando todos os métodos da API para funcionar.
    // Exemplo do flatmap e map e mapToInt Tive que recorrer ao chatGPT para relembrar a diferença entre eles. 
    // map -> 1 elemento para 1 elemento. flatmap -> 1 elemento para n elementos.
    // Mas o uso da API é mais performatico que o for de for.

}
