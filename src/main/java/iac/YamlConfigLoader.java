package iac;

import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.InputStream;

public class YamlConfigLoader {

    public static AwsConfig loadConfig(String filePath) {
        Yaml yaml = new Yaml(new Constructor(AwsConfig.class, new LoaderOptions()));
        InputStream inputStream = YamlConfigLoader.class
                .getClassLoader()
                .getResourceAsStream(filePath);
        return yaml.load(inputStream);
    }

}
