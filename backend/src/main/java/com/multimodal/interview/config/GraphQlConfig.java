package com.multimodal.interview.config;

import graphql.language.StringValue;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * GraphQL 运行时配置。
 *
 * <p>职责：</p>
 * <p>
 * - 为项目注册运行时需要的自定义标量（scalar）
 * </p>
 * <p>
 * - 解决 Java 实体字段类型与 GraphQL schema 标量之间的转换问题
 * </p>
 * <p>
 * 当前项目重点处理两类扩展标量：
 * </p>
 * <p>
 * - {@code DateTime}：兼容 {@link LocalDateTime}、{@link OffsetDateTime}、{@link Instant}
 * </p>
 * <p>
 * - {@code JSON}：复用 graphql-java-extended-scalars 的 Json 标量，供 AI 结构化结果等动态对象返回使用
 * </p>
 */
@Configuration
public class GraphQlConfig {

    private static final ZoneId DEFAULT_ZONE_ID = ZoneId.systemDefault();
    private static final DateTimeFormatter OUTPUT_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    /**
     * 注册项目使用到的扩展标量。
     *
     * @return GraphQL 运行时装配配置
     */
    @Bean
    public RuntimeWiringConfigurer runtimeWiringConfigurer() {
        GraphQLScalarType dateTimeScalar = GraphQLScalarType.newScalar()
                .name("DateTime")
                .description("DateTime scalar compatible with LocalDateTime and OffsetDateTime")
                .coercing(new Coercing<>() {
                    @Override
                    public String serialize(Object input) {
                        try {
                            return formatDateTime(input);
                        } catch (RuntimeException ex) {
                            throw new CoercingSerializeException("Invalid DateTime value", ex);
                        }
                    }

                    @Override
                    public LocalDateTime parseValue(Object input) {
                        try {
                            return parseDateTime(input);
                        } catch (RuntimeException ex) {
                            throw new CoercingParseValueException("Invalid DateTime input", ex);
                        }
                    }

                    @Override
                    public LocalDateTime parseLiteral(Object input) {
                        if (input instanceof StringValue stringValue) {
                            try {
                                return parseDateTime(stringValue.getValue());
                            } catch (RuntimeException ex) {
                                throw new CoercingParseLiteralException("Invalid DateTime literal", ex);
                            }
                        }
                        throw new CoercingParseLiteralException("DateTime literal must be a string");
                    }
                })
                .build();

        return wiringBuilder -> wiringBuilder
                .scalar(dateTimeScalar)
                .scalar(graphql.scalars.ExtendedScalars.Json);
    }

    private static String formatDateTime(Object input) {
        // GraphQL 对外统一输出带时区的 ISO 8601 字符串，避免前端拿到本地时间对象时歧义。
        if (input instanceof LocalDateTime localDateTime) {
            return OUTPUT_FORMATTER.format(localDateTime.atZone(DEFAULT_ZONE_ID).toOffsetDateTime());
        }
        if (input instanceof OffsetDateTime offsetDateTime) {
            return OUTPUT_FORMATTER.format(offsetDateTime);
        }
        if (input instanceof Instant instant) {
            return OUTPUT_FORMATTER.format(instant.atZone(DEFAULT_ZONE_ID).toOffsetDateTime());
        }
        if (input instanceof String stringValue) {
            return formatDateTime(parseDateTime(stringValue));
        }
        throw new IllegalArgumentException("Unsupported DateTime value type: " + input);
    }

    private static LocalDateTime parseDateTime(Object input) {
        // GraphQL 输入在服务端统一收敛为 LocalDateTime，便于与项目实体层时间类型保持一致。
        if (input instanceof LocalDateTime localDateTime) {
            return localDateTime;
        }
        if (input instanceof OffsetDateTime offsetDateTime) {
            return offsetDateTime.toLocalDateTime();
        }
        if (input instanceof Instant instant) {
            return LocalDateTime.ofInstant(instant, DEFAULT_ZONE_ID);
        }
        if (input instanceof String stringValue) {
            try {
                return OffsetDateTime.parse(stringValue).toLocalDateTime();
            } catch (DateTimeParseException ignore) {
                return LocalDateTime.parse(stringValue);
            }
        }
        throw new IllegalArgumentException("Unsupported DateTime input type: " + input);
    }
}
