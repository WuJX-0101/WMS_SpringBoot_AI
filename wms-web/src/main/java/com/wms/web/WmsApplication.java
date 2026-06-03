package com.wms.web;

import io.github.cdimascio.dotenv.Dotenv;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@SpringBootApplication
@ComponentScan(basePackages = "com.wms")
@MapperScan("com.wms.dao.mapper")
@EnableElasticsearchRepositories(basePackages = "com.wms.dao.elasticsearch")
public class WmsApplication {

    public static void main(String[] args) {
        // 加载.env文件（从项目根目录加载）
        Dotenv dotenv = Dotenv.configure()
                .directory("./")
                .ignoreIfMalformed()
                .ignoreIfMissing()
                .load();
        
        // 将.env中的变量设置为系统属性
        dotenv.entries().forEach(entry -> {
            System.setProperty(entry.getKey(), entry.getValue());
        });
        
        SpringApplication.run(WmsApplication.class, args);
    }
}
