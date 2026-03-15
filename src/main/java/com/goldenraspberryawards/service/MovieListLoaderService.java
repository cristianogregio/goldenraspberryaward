package com.goldenraspberryawards.service;

import com.goldenraspberryawards.model.MovieList;
import com.goldenraspberryawards.model.Produtor;
import com.goldenraspberryawards.repository.MovieListRepository;
import com.goldenraspberryawards.repository.ProdutorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class MovieListLoaderService implements ApplicationRunner
{

    private static final Logger log = LoggerFactory.getLogger(MovieListLoaderService.class);
    private static final String DELIMITER = ";";
    private static final String PRODUCERS_AND = " and ";
    private static final String PRODUCERS_COMMA = ",";
    private static final int EXPECTED_COLUMNS = 5;

    private final MovieListRepository repository;
    private final ProdutorRepository produtorRepository;
    private final ResourceLoader resourceLoader;
    private final String csvPath;

    public MovieListLoaderService(MovieListRepository repository, ProdutorRepository produtorRepository, ResourceLoader resourceLoader, @Value("${movielist.csv.path}") String csvPath)
    {
        this.repository = repository;
        this.produtorRepository = produtorRepository;
        this.resourceLoader = resourceLoader;
        this.csvPath = csvPath;
    }

    @Override
    public void run(ApplicationArguments args)
    {
        loadCsv();
    }

    public void loadCsv()
    {
        Resource resource = resourceLoader.getResource(csvPath);
        if (!resource.exists())
        {
            log.warn("CSV não encontrado: {}", csvPath);
            return;
        }
        try
        {
            // 1. Limpar todo o banco. Fiquei na dúvida se isso era necessário, 
            // mas fiz para garantir que o banco de dados está limpo.
            // Poderia usar o spring.jpa.hibernate.ddl-auto=create-drop no application.properties para garantir que o banco de dados está limpo.
            repository.deleteAll();
            produtorRepository.deleteAll();
            log.info("Banco de dados limpo.");

            // 2. Primeira passagem: ler apenas a coluna de produtores e cadastrar cada produtor (evitar duplicatas)
            registerProducersFromCsv(resource);
            log.info("Produtores cadastrados: {} no total.", produtorRepository.count());

            // 3. Segunda passagem: ler o CSV e cadastrar os filmes já com os produtores associados
            registerMoviesFromCsv(resource);
            log.info("Carregados {} filmes do Movielist.csv no H2.", repository.count());
        }
        catch (Exception e)
        {
            log.error("Erro ao carregar Movielist.csv de {}", csvPath, e);
        }
    }

    /**
     * Primeira passagem: ler apenas a coluna de produtores; para cada nome (separado por "," e " and "). 
     * Issos acontece pq tem filmes que tem o mesmo produtor. Tem que cadastrar o produtor para cada filme verificando se já está cadastrado.
     */
    private void registerProducersFromCsv(Resource resource) throws Exception
    {
        try (var reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)))
        {
            String line = reader.readLine();
            if (line == null) return;
            while ((line = reader.readLine()) != null)
            {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split(DELIMITER, -1);
                if (parts.length < EXPECTED_COLUMNS) continue; // Este eh o caso de um filme sem produtores. O que nao ocorre pq o cvs está bem formado.
                String producersColumn = parts[3].trim();
                for (String name : parseProducerNames(producersColumn))
                {
                    if (produtorRepository.findByName(name).isEmpty())
                    {
                        produtorRepository.save(new Produtor(name));
                    }
                }
            }
        }
    }

    /**
     * Segunda passagem: ler o CSV completo e cadastrar cada filme com seus produtores (busca por nome).
     * Isso faz que tenha relacionamento 1 x N (N produtores por filme)
     */
    private void registerMoviesFromCsv(Resource resource) throws Exception
    {
        List<MovieList> entities = new ArrayList<>();
        try (var reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)))
        {
            String line = reader.readLine();
            if (line == null) return;
            while ((line = reader.readLine()) != null)
            {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split(DELIMITER, -1);
                if (parts.length < EXPECTED_COLUMNS) continue;
                Integer year = parseYear(parts[0].trim());
                if (year == null) continue;
                String title = parts[1].trim();
                String studios = parts[2].trim();
                List<Produtor> producers = findProducersByNames(parseProducerNames(parts[3].trim()));
                boolean winner = "yes".equalsIgnoreCase(parts[4].trim());
                entities.add(new MovieList(year, title, studios, producers, winner));
            }
        }
        if (!entities.isEmpty())
        {
            repository.saveAll(entities);
        }
    }

    /**
     * Analisa a coluna de produtores: nomes podem ser separados por vírgula e/ou " and ".
     * Exemplo: "Bob Cavallo, Joe Ruffalo and Steve Fargnoli" -> ["Bob Cavallo", "Joe Ruffalo", "Steve Fargnoli"]
     */
    private List<String> parseProducerNames(String producersColumn)
    {
        if (producersColumn == null || producersColumn.isEmpty())
        {
            return List.of();
        }
        List<String> names = new ArrayList<>();
        for (String part : producersColumn.split(PRODUCERS_AND))
        {
            for (String name : part.split(PRODUCERS_COMMA))
            {
                String trimmed = name.trim();
                if (!trimmed.isEmpty())
                {
                    names.add(trimmed);
                }
            }
        }
        return names;
    }

    private List<Produtor> findProducersByNames(List<String> names)
    {
        return names.stream()
                .map(n -> produtorRepository.findByName(n).orElseThrow(() -> new IllegalStateException("Produtor não encontrado: " + n)))
                .collect(Collectors.toList());
    }

    private static Integer parseYear(String s)
    {
        if (s == null || s.isEmpty())
        {
            return null;
        }
        try
        {
            return Integer.parseInt(s);
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }
}
