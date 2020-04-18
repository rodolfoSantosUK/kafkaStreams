package com.github.kafka.streams;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KStreamBuilder;
import org.apache.kafka.streams.kstream.KTable;

import java.io.File;
import java.util.Arrays;
import java.util.Properties;

public class StreamStarterApp {

    public static void main(String[] args) {

        Properties config = new Properties();
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "streams-starter-app");
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        new File("/tmp/kafka-streams/streams-starter-app").mkdirs();
        KStreamBuilder builder = new KStreamBuilder();
        KStream<String, String> wordCountInput = builder.stream("word-count-input");

        KTable<String, Long> wordCounts =  wordCountInput
                .mapValues(textLine -> textLine.toLowerCase()) // pra cada um dos values vou colocar como minuscula
                .flatMapValues(lowercasedTextLine -> Arrays.asList(lowercasedTextLine.split(" "))) //vou mapear os valores e separar cada um por espaço
                .selectKey((ignoredKey, word) -> word) // vai pegar a chave e substituir por value
                .groupByKey().count();

        wordCounts.to(Serdes.String(), Serdes.Long(), "word-count-output");

        KafkaStreams streams = new KafkaStreams(builder, config);
        streams.start();
        System.out.println(streams.toString());

        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));


        // ** Criando os tópicos **
        // sudo ./bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 2 --topic word-count-input
        // sudo ./bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 2 --topic word-count-output
        // ** Produzindo os tópicos **
//       sudo ./bin/kafka-console-producer.sh --broker-list localhost:9092 --topic word-count-input

//        sudo ./bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 \
//        --topic word-count-output \
//        --from-beginning \
//        --formatter kafka.tools.DefaultMessageFormatter \
//        --property print.key=true \
//        --property print.value=true \
//        --property key.deserializer=org.apache.kafka.common.serialization.StringDeserializer \
//        --property value.deserializer=org.apache.kafka.common.serialization.LongDeserializer




    }

}

















