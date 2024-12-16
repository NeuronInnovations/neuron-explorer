package world.neuron.device;

import io.quarkus.qute.TemplateEnum;

@TemplateEnum
public enum DeviceRegistrationStatus {
    REGISTERED,
    UNREGISTERED,
    PENDING
}
