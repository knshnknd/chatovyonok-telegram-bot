package ru.knshnknd.chatovyonok.service;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class ChronicleService {

    // Коллекция поговорок и их количество
    private final List<String> pvlChronicle;
    private final List<String> tvlChronicle;

    // Чтение файла и запись поговорок в коллекцию в конструкторе
    public ChronicleService() {
        ArrayList<String> pvl = new ArrayList<>(100);
        ArrayList<String> tvl = new ArrayList<>(100);
        Resource resource = new ClassPathResource("chronicles/PVL.txt");

        try (BufferedReader bufferedReader =
                     new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            String readLine;

            while ((readLine = bufferedReader.readLine()) != null) {
                pvl.add(readLine);
            }

        } catch (IOException e) {
            System.out.println(e);
        }

        resource = new ClassPathResource("chronicles/TVL.txt");

        try (BufferedReader bufferedReader =
                     new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            String readLine;

            while ((readLine = bufferedReader.readLine()) != null) {
                tvl.add(readLine);
            }

        } catch (IOException e) {
            System.out.println(e);
        }

        this.pvlChronicle = pvl;
        this.tvlChronicle = tvl;
    }

    public String getRandomChronicleLine() {
        int randomChronicle = new Random().nextInt(2);

        switch (randomChronicle) {
            case 0 -> {
                int randomLine = new Random().nextInt(pvlChronicle.size());
                return pvlChronicle.get(randomLine) + "\n\n" + "– Повесть временных лет (по Ипатьевскому списку)";
            }
            case 1 -> {
                int randomLine = new Random().nextInt(tvlChronicle.size());
                return tvlChronicle.get(randomLine) + "\n\n" + "– Летописный сборник, именуемый Тверской летописью";
            }
        }

        return "";
    }
}
