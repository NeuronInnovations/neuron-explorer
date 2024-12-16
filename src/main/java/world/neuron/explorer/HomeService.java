package world.neuron.explorer;

import io.smallrye.mutiny.Multi;
import io.vertx.core.json.JsonObject;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.Client;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import world.neuron.hedera.DeviceInfo;
import world.neuron.hedera.HederaClient;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class HomeService {

    @Inject
    HederaClient hederaClient;

    JsonObject globalStats;

    private Client client;
    private Multi<List<DeviceInfo>> sharedMulti;

    @PostConstruct
    public void init() {
        globalStats = new JsonObject();

        globalStats.put("totalNumberOfTransactions", 0L);
        globalStats.put("totalNumberOfActiveDevices", 0L);
        globalStats.put("totalNumberOfDevices", 0L);
        globalStats.put("totalNumberOfConnections", 0L);
        client = ResteasyClientBuilder.newClient();
        sharedMulti = Multi.createFrom().ticks().every(Duration.ofMillis(1500L))
                .onItem().transform(item -> {
                    var deviceInfoMap = hederaClient.getDeviceInfoMap();
                    var l = deviceInfoMap.values()
                            .stream()
                            .sorted((deviceInfo1, deviceInfo2) -> deviceInfo2.lastStdOutMessageTimestamp.compareTo(deviceInfo1.lastStdOutMessageTimestamp))
                            .collect(Collectors.toList());
                          return l;
                }).broadcast().toAllSubscribers();
    }

    @PreDestroy
    public void windDown() {
        client.close();
    }

    public Multi<List<DeviceInfo>> emitDevices() {
        return sharedMulti;
    }

    public JsonObject getStats() {
        if (globalStats.containsKey("totalNumberOfTransactions")) {
            if (globalStats.getLong("totalNumberOfTransactions") < hederaClient.totalNumberOfTransactions()) {
                globalStats.put("totalNumberOfTransactions", hederaClient.totalNumberOfTransactions());
            }
        }

        if (globalStats.containsKey("totalNumberOfActiveDevices")) {
            globalStats.put("totalNumberOfActiveDevices", hederaClient.totalNumberOfActiveDevices());
        }

        if (globalStats.containsKey("totalNumberOfDevices")) {
            globalStats.put("totalNumberOfDevices", hederaClient.totalNumberOfDevices());
        }

        if (globalStats.containsKey("totalNumberOfConnections")) {
            globalStats.put("totalNumberOfConnections", hederaClient.totalNumberOfConnections());
        }

        return globalStats;
    }
}
