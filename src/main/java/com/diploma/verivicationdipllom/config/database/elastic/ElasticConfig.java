package com.diploma.verivicationdipllom.config.database.elastic;

import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;

import javax.net.ssl.SSLContext;

@Configuration
public class ElasticConfig extends ElasticsearchConfiguration {

    // Чтение переменных окружения с помощью @Value
    @Value("${ELASTIC_HOST:localhost}")
    private String elasticHost;

    @Value("${ELASTIC_PORT:9200}")
    private int elasticPort;

    @Value("${ELASTIC_USERNAME:elastic}")
    private String elasticUsername;

    @Value("${ELASTIC_PASSWORD:yourpassword}")
    private String elasticPassword;

    @Override
    public ClientConfiguration clientConfiguration() {
        return ClientConfiguration.builder()
                .connectedTo(elasticHost + ":" + elasticPort)  // Подключение к указанному хосту и порту
//                .usingSsl(buildSSLContext())  // Использование SSL
                .withBasicAuth(elasticUsername, elasticPassword)  // Аутентификация через переменные
                .build();
    }

    // Метод для настройки SSL-контекста
    private SSLContext buildSSLContext() {
        try {
            return new SSLContextBuilder().loadTrustMaterial(null, (chain, authType) -> true).build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}