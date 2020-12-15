package org.apache.kafka.common.config.internals;

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.security.scram.internals.ScramMechanism;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Define the dynamic quota configs. Note that these are not normal configurations that exist in properties files, but
 * rather only ever exist as dynamic configs.
 */
public class QuotaConfigs {
    public static final String PRODUCER_BYTE_RATE_OVERRIDE_CONFIG = "producer_byte_rate";
    public static final String CONSUMER_BYTE_RATE_OVERRIDE_CONFIG = "consumer_byte_rate";
    public static final String REQUEST_PERCENTAGE_OVERRIDE_CONFIG = "request_percentage";
    public static final String CONTROLLER_MUTATION_RATE_OVERRIDE_CONFIG = "controller_mutation_rate";

    public static final String PRODUCER_BYTE_RATE_DOC = "A rate representing the upper bound (bytes/sec) for producer traffic.";
    public static final String CONSUMER_BYTE_RATE_DOC = "A rate representing the upper bound (bytes/sec) for consumer traffic.";
    public static final String REQUEST_PERCENTAGE_DOC = "A percentage representing the upper bound of time spent for processing requests.";
    public static final String CONTROLLER_MUTATION_RATE_DOC = "The rate at which mutations are accepted for the create " +
        "topics request, the create partitions request and the delete topics request. The rate is accumulated by " +
        "the number of partitions created or deleted.";

    private static Set<String> configNames = new HashSet<>(Arrays.asList(
        PRODUCER_BYTE_RATE_OVERRIDE_CONFIG, CONSUMER_BYTE_RATE_OVERRIDE_CONFIG,
        REQUEST_PERCENTAGE_OVERRIDE_CONFIG, CONTROLLER_MUTATION_RATE_OVERRIDE_CONFIG
    ));

    private static void buildQuotaConfigDef(ConfigDef configDef) {
        configDef.define(PRODUCER_BYTE_RATE_OVERRIDE_CONFIG, ConfigDef.Type.LONG, Long.MAX_VALUE,
            ConfigDef.Importance.MEDIUM, PRODUCER_BYTE_RATE_DOC);

        configDef.define(CONSUMER_BYTE_RATE_OVERRIDE_CONFIG, ConfigDef.Type.LONG, Long.MAX_VALUE,
            ConfigDef.Importance.MEDIUM, CONSUMER_BYTE_RATE_DOC);

        configDef.define(REQUEST_PERCENTAGE_OVERRIDE_CONFIG, ConfigDef.Type.DOUBLE,
            Integer.valueOf(Integer.MAX_VALUE).doubleValue(),
            ConfigDef.Importance.MEDIUM, REQUEST_PERCENTAGE_DOC);

        configDef.define(CONTROLLER_MUTATION_RATE_OVERRIDE_CONFIG, ConfigDef.Type.DOUBLE,
            Integer.valueOf(Integer.MAX_VALUE).doubleValue(),
            ConfigDef.Importance.MEDIUM, CONTROLLER_MUTATION_RATE_DOC);
    }

    public static boolean isQuotaConfig(String name) {
        return configNames.contains(name);
    }

    public static ConfigDef userConfigs() {
        ConfigDef configDef = new ConfigDef();
        ScramMechanism.mechanismNames().forEach(mechanismName -> {
            configDef.define(mechanismName, ConfigDef.Type.STRING, null, ConfigDef.Importance.MEDIUM,
                "User credentials for SCRAM mechanism " + mechanismName);
        });
        buildQuotaConfigDef(configDef);
        return configDef;
    }

    public static ConfigDef clientConfigs() {
        ConfigDef configDef = new ConfigDef();
        buildQuotaConfigDef(configDef);
        return configDef;
    }
}
