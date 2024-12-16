package world.neuron.hedera;

import com.hedera.hashgraph.sdk.PrecheckStatusException;
import com.hedera.hashgraph.sdk.TopicId;
import io.quarkus.runtime.Startup;
import io.vertx.core.json.JsonObject;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.crypto.Credentials;
import org.web3j.model.Rendezvous;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.exceptions.ClientConnectionException;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.DefaultGasProvider;
import world.neuron.device.DeviceController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;

@Startup
@ApplicationScoped
public class HederaClient {

    private final Logger logger = LoggerFactory.getLogger(HederaClient.class);

    @ConfigProperty(name = "hh.eth_private_key")
    String hhEthPrivateKey;
    @ConfigProperty(name = "hh.contract_id")
    String hhContractId;
    @ConfigProperty(name = "hh.eth_contract_id")
    String hhEthContractId;
    @ConfigProperty(name = "hh.network")
    String hhNetwork;
    Web3j ethRpcService;
    Rendezvous rendezvous;
    public Map<String, DeviceInfo> deviceInfoMap = new ConcurrentHashMap<>(); // Use ConcurrentHashMap for thread safety

    private ScheduledExecutorService scheduler;
    private long currentIndex = 0;
    private long peerArraySize = 0;

    private Set<Long> skipList = new CopyOnWriteArraySet<>(); // Thread-safe set for invalid device indices


    @Inject
    DeviceController deviceController;

    public Map<String, DeviceInfo> getDeviceInfoMap() {
        return deviceInfoMap;
    }

    @PostConstruct
    public void bootstrapHederaClient(){
        try {
            if (hhNetwork.equals("MAINNET")) {

                ethRpcService = Web3j.build(
                        new HttpService("https://maiinnet.hashio.io/api"));
            } else {
                ethRpcService = Web3j.build(
                        new HttpService("https://testnet.hashio.io/api"));
            }
            DeviceInfo dummyDevice = DeviceInfoMockService.getDeviceInfo();
            deviceInfoMap.put("0x1234", dummyDevice);

            rendezvous = Rendezvous.load(hhEthContractId, ethRpcService,
                    Credentials.create(hhEthPrivateKey), new DefaultGasProvider());

            scheduler = Executors.newScheduledThreadPool(10);

            scheduler.scheduleAtFixedRate(() -> {
                try {
                    peerArraySize = rendezvous.getPeerArraySize().send().longValue();
                } catch (Exception ex) {
                    logger.info(ex.getLocalizedMessage());
                }
            }, 0, 50, TimeUnit.SECONDS);

            scheduler.scheduleAtFixedRate(() -> {
                if (peerArraySize == 0) {
                    logger.info("Peer array size is 0, skipping this run.");
                    return;
                }
                currentIndex = currentIndex % peerArraySize;
                while (skipList.contains(currentIndex)) {
                    currentIndex = (currentIndex + 1) % peerArraySize;
                }

                final long currentIndexFinal = currentIndex;
                scheduler.submit(() -> visitDevice(currentIndexFinal));

                currentIndex = currentIndex + 1;

            }, 0, 2000, TimeUnit.MILLISECONDS);

        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    @ActivateRequestContext
    public void visitDevice(long currentPeerIndex) {
        try {
            String peerEvmAddress = rendezvous.peerList(BigInteger.valueOf(currentPeerIndex)).send();
            var deviceInfo = deviceInfoMap.get(peerEvmAddress);
            if (deviceInfo == null) {
                deviceInfo = new DeviceInfo();
            }
            if (peerEvmAddress.startsWith("0x00000000")) {
                skipList.add(currentPeerIndex);
                return;
            }

            if (deviceInfo.isValid == Boolean.FALSE) {
                skipList.add(currentPeerIndex);
                return;
            }


            var tuple5 = rendezvous.hederaAddressToPeer(peerEvmAddress).send();
            BigInteger stdOutTopicNum = tuple5.component3();
            BigInteger stdInTopicNum = tuple5.component4();

            if (deviceInfo.talkingToPeerIDs == null) {
                deviceInfo.talkingToPeerIDs = new TreeSet<>();
            }
            deviceInfo.stdOutTopic = "0.0." + stdOutTopicNum;
            deviceInfo.stdInTopic = "0.0." + stdInTopicNum;
            deviceInfo.address = peerEvmAddress;
            deviceInfo.balance = ethRpcService.ethGetBalance(peerEvmAddress, DefaultBlockParameterName.LATEST)
                    .send().getBalance();

            try {

                var stdOutTopic = TopicId.fromString("0.0." + stdOutTopicNum);
                var latestStdOutMessage = this.getLatestMessageFromRestMirror(stdOutTopic);
                if (latestStdOutMessage == null) {
                    return;
                }
                var extractedMessages = latestStdOutMessage.getJsonArray("messages");
                if (!extractedMessages.isEmpty()) {
                    var latestStdOutMessageJSON = new JsonObject(new String(
                            extractedMessages.getJsonObject(0).getBinary("message"), StandardCharsets.UTF_8));
                    deviceInfo.lastStdOutMessageTimestamp = Instant
                            .ofEpochSecond((long) Double.parseDouble(extractedMessages
                                    .getJsonObject(0).getString("consensus_timestamp")));


                    if (Duration.between(deviceInfo.lastStdOutMessageTimestamp, Instant.now()).toDays() >= 14L) {
                        deviceInfo.isValid = Boolean.FALSE;
                        skipList.add(currentPeerIndex);
                        return;
                    }

                    var deviceInfoLocation = latestStdOutMessageJSON.getJsonObject("location");
                    deviceInfo.lat = deviceInfoLocation.getDouble("lat");
                    deviceInfo.lon = deviceInfoLocation.getDouble("lon");
                    deviceInfo.alt = deviceInfoLocation.getDouble("alt");
                    deviceInfo.buyerOrSeller = latestStdOutMessageJSON.getString("buyerOrSeller");
                    deviceInfo.sdkVersion = latestStdOutMessageJSON.getString("version");

                    if (deviceInfo.talkingToPeerIDs == null) {
                        deviceInfo.talkingToPeerIDs = new TreeSet<>();
                    }
                    var connectedPeers = latestStdOutMessageJSON.getJsonArray("connectedPeers");
                    if (connectedPeers != null) {
                        deviceInfo.talkingToPeerIDs.clear();
                        for (int i = 0; i < connectedPeers.size(); i++) {
                            String address = connectedPeers.getString(i);
                            deviceInfo.talkingToPeerIDs.add(address);
                        }
                    }

                    deviceInfo.isDialable = latestStdOutMessageJSON.getString("natReachability").equals("false")
                            ? Boolean.FALSE
                            : Boolean.TRUE;
                } else if (extractedMessages.size() == 0) {
                    var rt = deviceController.getRegistrationTime(deviceInfo.stdOutTopic);
                    if (rt == null) {
                        deviceInfo.isValid = Boolean.FALSE;
                        skipList.add(currentPeerIndex);
                        return;
                    } else if (Duration.between(rt, Instant.now()).toDays() >= 14L) {
                        deviceInfo.isValid = Boolean.FALSE;
                        skipList.add(currentPeerIndex);
                        return;
                    }


                } else {
                    return;
                }

                var stdInTopic = TopicId.fromString("0.0." + stdInTopicNum);

                var latestStdInMessage = this.getLatestMessageFromRestMirror(stdInTopic);
                if (latestStdInMessage == null) {
                    return;
                }
                var extractedStdInMessages = latestStdInMessage.getJsonArray("messages");
                if (extractedStdInMessages.isEmpty()) {
                    deviceInfo.lastStdInMessageTimestamp = Instant.EPOCH;
                } else {
                    deviceInfo.lastStdInMessageTimestamp = Instant
                            .ofEpochSecond((long) Double.parseDouble(extractedStdInMessages
                                    .getJsonObject(0).getString("consensus_timestamp")));
                    ;

                    deviceInfo.latestStdInMessages = this.getLatestMessagesOfEach(stdInTopic);
                }

                deviceInfo.isValid = Boolean.TRUE;
                deviceInfo.stdOutSequenceNumber = getSequenceNumberFromRestMirror(latestStdOutMessage);
                deviceInfo.stdInSequenceNumber = getSequenceNumberFromRestMirror(latestStdInMessage);
                deviceInfoMap.put(peerEvmAddress, deviceInfo);
            } catch (ClientConnectionException e) {
                String errorMessage = e.getMessage();
                System.err.println("ClientConnectionException: " + errorMessage);
                deviceInfo.lastStdOutMessage = errorMessage;
                deviceInfo.lastStdOutMessageTimestamp = Instant.EPOCH;
                deviceInfo.isValid = Boolean.FALSE;

            } catch (Exception e) {
                e.printStackTrace();
                deviceInfo.lastStdOutMessage = e.getMessage();
                deviceInfo.lastStdOutMessageTimestamp = Instant.EPOCH;
                deviceInfo.isValid = Boolean.FALSE;
            }

        } catch (TimeoutException ex) {
            logger.error(ex.getMessage(), ex);
        } catch (PrecheckStatusException ex) {
            logger.error(ex.getMessage(), ex);
        } catch (IOException e1) {
            logger.error(e1.getMessage(), e1);
        } catch (Exception e1) {
            logger.info(e1.getLocalizedMessage());

        }
    }

    public JsonObject getLatestMessageFromRestMirror(TopicId topicID) throws IOException {
        JsonObject r;

        var url = "https://testnet.mirrornode.hedera.com/api/v1/topics/" + topicID.toString()
                + "/messages?limit=1&order=desc";

        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();

        connection.setRequestMethod("GET");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(15000);
        connection.connect();
        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            return null;
        }
        try {
            var stream = connection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(stream));


            r = new JsonObject(br.readLine());
        } catch (Exception e) {
            r = null;
        }

        return r;

    }

    public Integer getSequenceNumberFromRestMirror(JsonObject message) throws IOException {

        if (message != null && message.getJsonArray("messages") != null && message.getJsonArray("messages").size() > 0 && message.getJsonArray("messages").getList().size() > 0) {
            Object messages = message.getJsonArray("messages").getList().get(0);
            Map<String, Object> objectMap = (Map<String, Object>) messages;
            return (Integer) objectMap.get("sequence_number");
        }
        return 0;
    }

    private Map<String, JsonObject> getLatestMessagesOfEach(TopicId topicID) throws IOException {

        var url = "https://testnet.mirrornode.hedera.com/api/v1/topics/" + topicID.toString()
                + "/messages?limit=100&order=desc";
        var response = new URL(url).openConnection().getInputStream();
        var json = new JsonObject(new String(response.readAllBytes()));
        var messages = json.getJsonArray("messages");
        var messagesMap = new HashMap<String, JsonObject>();
        return messagesMap;
    }

    public Long totalNumberOfTransactions() {
        long sum = 0L;

        Map<String, DeviceInfo> deviceMap = deviceInfoMap;
        for (Map.Entry<String, DeviceInfo> entry : deviceMap.entrySet()) {
            DeviceInfo deviceInfo = entry.getValue();
            if (deviceInfo.stdOutSequenceNumber != null) {
                sum = sum + deviceInfo.stdInSequenceNumber;
            }
            if (deviceInfo.stdOutSequenceNumber != null) {
                sum = sum + deviceInfo.stdOutSequenceNumber;
            }
        }
        return sum;
    }

    public Long totalNumberOfDevices() {
        return deviceInfoMap.entrySet().stream().filter(
                device ->
                        device.getValue().isValid != null && device.getValue().isValid
        ).count();
    }

    public Long totalNumberOfActiveDevices() {
        Map<String, DeviceInfo> deviceMap = deviceInfoMap;
        List<Map.Entry<String, DeviceInfo>> list = deviceMap.entrySet().stream().filter(
                device ->

                        device
                                .getValue()
                                .lastStdOutMessageTimestamp
                                .compareTo(Instant.now().minusSeconds(180)) > 0


        ).toList();
        return (long) list.size();
    }

    public Long totalNumberOfConnections() {
        Map<String, DeviceInfo> deviceMap = deviceInfoMap;
        var r = deviceMap.values().stream()
                .filter(deviceInfo -> deviceInfo.buyerOrSeller != null && deviceInfo.buyerOrSeller.equalsIgnoreCase("buyer"))
                .filter(
                        deviceInfo ->
                                deviceInfo.lastStdOutMessageTimestamp
                                        .compareTo(Instant.now().minusSeconds(180)) > 0
                )

                .map(deviceInfo -> deviceInfo.talkingToPeerIDs)
                .mapToLong(Collection::size).sum();

        return r;
    }
}
