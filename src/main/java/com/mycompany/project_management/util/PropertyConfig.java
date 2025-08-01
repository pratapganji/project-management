package com.mycompany.project_management.util;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Data
public class PropertyConfig {

    @Value("${bulkapiconfig.rateLimit}")
    private double rateLimit;

    @Value("${bulkapiconfig.burstTime}")
    private double burstTime;

}
