package com.sky.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
//  配置属性类，读取配置文件；读取配置中的sky.alioss，然后封装成Java对象里面有参数如下；
//(1)写配置文件的时候可以有下面参数的提示
//(2)此类与配置挂钩
@ConfigurationProperties(prefix = "sky.alioss")
@Data
public class AliOssProperties {

    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;

}
