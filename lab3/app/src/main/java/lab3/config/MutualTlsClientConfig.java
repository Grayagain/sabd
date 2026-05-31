package lab3.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(MutualTlsClientProperties.class)
public class MutualTlsClientConfig {
}
