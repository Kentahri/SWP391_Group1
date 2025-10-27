package com.group1.swp.pizzario_swp391.service;

import com.group1.swp.pizzario_swp391.config.Setting;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class YamlService {

    private final Setting setting;

    @Value("${pizzario.settings-file-path}")
    private String filePath;

    private Map<String, Object> loadDataFromYaml(){
        Map<String, Object> leaf = new LinkedHashMap<>();
        leaf.put("conflict-reservation-minutes", setting.getConflictReservationMinutes());
        leaf.put("auto-lock-reservation-minutes", setting.getAutoLockReservationMinutes());
        leaf.put("no-show-wait-minutes", setting.getNoShowWaitMinutes());
        return leaf;
    }

    public void persit() throws IOException {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        Yaml yaml = new Yaml(options);

        Map<String, Object> leaf = loadDataFromYaml();

        Map<String, Object> root = new LinkedHashMap<>();
        root.put("pizzario", Map.of("settings", leaf));

        Path path = Paths.get(filePath).toAbsolutePath();
        Files.createDirectories(path.getParent());
        try(var os = Files.newOutputStream(path, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            var writer = new OutputStreamWriter(os, StandardCharsets.UTF_8)) {
                yaml.dump(root, writer);
        }
    }


}
