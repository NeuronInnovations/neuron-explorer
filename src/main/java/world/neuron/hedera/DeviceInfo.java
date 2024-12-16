package world.neuron.hedera;

import java.math.BigInteger;
import java.time.Instant;
import java.util.Map;
import java.util.Set;

import com.hedera.hashgraph.sdk.SubscriptionHandle;

import io.vertx.core.json.JsonObject;


public class DeviceInfo {
    public String peerID;
    public String address;
    public String stdOutTopic;
    public String stdInTopic;
    public String buyerOrSeller;
    public String sdkVersion;
    public Boolean isValid;
    public String lastStdOutMessage;
    public Instant lastStdOutMessageTimestamp=Instant.MIN;
    public String lastStdInMessage;
    public Instant lastStdInMessageTimestamp;
    public Map<String,JsonObject> latestStdInMessages;
    public Set<String> talkingToPeerIDs;
    public Double lat;
    public Double lon;
    public Double alt;
    public Boolean isDialable;
    public BigInteger balance;
    public SubscriptionHandle stdOutSubscriptionHandle;
    public SubscriptionHandle stdInSubscriptionHandle;
    public Integer  stdOutSequenceNumber;
    public Integer stdInSequenceNumber;

    @Override
    public String toString() {
        return String.format("{p:%s a:%s t:%s v:%s m:%s t:%s lla:%s,%s,%s talking-to:%s, is-dialable:%s , lateststindstffs:%s}", peerID, address,
                stdOutTopic, isValid, lastStdOutMessage, lastStdOutMessageTimestamp, lat, lon, alt, talkingToPeerIDs, isDialable,  latestStdInMessages);
    }
}
