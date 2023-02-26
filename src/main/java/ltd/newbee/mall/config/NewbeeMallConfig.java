package ltd.newbee.mall.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "newbee-mall")
public class NewbeeMallConfig {

    private String serverUrl;
    private Boolean viewModel;
    private String viewModelTip;
    private String uploadDir;
}
