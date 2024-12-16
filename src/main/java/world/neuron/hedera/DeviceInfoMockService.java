package world.neuron.hedera;

import java.util.TreeSet;

public class DeviceInfoMockService {
    public static DeviceInfo getDeviceInfo() {
        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.address = "0x123";
        deviceInfo.stdOutTopic = "0.0.123";
        deviceInfo.stdInTopic = "0.0.456";
        deviceInfo.lat = 0.0;
        deviceInfo.lon = 0.0;
        deviceInfo.peerID = "0x34567";
        deviceInfo.talkingToPeerIDs = new TreeSet<String>();
        deviceInfo.isDialable = Boolean.FALSE;
        return deviceInfo;
    }
}
