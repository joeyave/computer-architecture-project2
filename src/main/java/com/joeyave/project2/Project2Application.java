package com.joeyave.project2;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

@SpringBootApplication
public class Project2Application implements ApplicationRunner {

    public static void main(String[] args) {
        SpringApplication.run(Project2Application.class, args);
    }

    @Override
    public void run(ApplicationArguments args) {
        if (args.containsOption("help")) {
            System.out.println("""
                    Usage:
                    get-size --folder=/home/myFolder
                    """);
            
            System.out.println("""
                    Options:
                    -i, --ignore REGEX\\t - ignore files that correspond to the given regex
                    --ignore-hidden\\t\\t - ignore hidden files
                    --ignore-readonly\\t - ignore readonly files
                    -h, --help\\t\\t - get help
                    """);

            System.exit(0);
        }

        long bytes = getFolderSize(Path.of(args.getNonOptionArgs().get(0)), args);


        System.out.println(bytes / 1024 / 1024 + " Mb");
    }

    private static long getFolderSize(Path folder, ApplicationArguments args) {
        try {
            Stream<Path> fileStream = Files.walk(folder)
                    .filter(p -> p.toFile().isFile());

            if (args.containsOption("ignore-hidden")) {
                fileStream = fileStream.filter(p -> !p.toFile().isHidden());
            }

            if (args.containsOption("ignore-readonly")) {
                fileStream = fileStream.filter(p -> p.toFile().canWrite());
            }

            String regex = args.getOptionValues("ignore").get(0);
            if (!regex.isEmpty()) {
                fileStream = fileStream.filter(p -> !p.getFileName().toString().matches(regex));
            }

            return fileStream.mapToLong(p -> p.toFile().length())
                    .sum();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
            return 0L;
        }
    }
}

