package world.neuron.serviceregister;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import world.neuron.device.Device;
import world.neuron.device.DeviceService;
import world.neuron.service.ServiceUtility;

import java.util.List;

@ApplicationScoped
public class ServiceRegisterUtility {

    @Inject
    DeviceService deviceService;

    @Inject
    ServiceUtility serviceUtility;

    @Inject
    ServiceRegisterRepository registerRepository;

    @Transactional
    public ServiceRegister registerService(ServiceRegister register) {
        register.status =  ServiceStatus.UNREGISTERED;
        register.serviceState = ServiceState.DISABLED;
        register.deviceId = deviceService.getDeviceByNameForCurrentAccount(register.device.name).id;
        register.serviceId = serviceUtility.getServiceByName(register.service.name).id;

        register.persist();

        return register;
    }

    public List<ServiceRegister> findRegistersByDeviceName(String deviceName) {
        Device device = deviceService.getDeviceByNameForCurrentAccount(deviceName);
        List<ServiceRegister> registerList = registerRepository.find("deviceId", device.id).list();
        registerList.forEach(register -> register.service = serviceUtility.getServiceById(register.serviceId));

        return registerList;
    }

    public ServiceRegister findRegisterById(Long id) {

        ServiceRegister register = registerRepository.findById(id);
        register.service = serviceUtility.getServiceById(register.serviceId);

        return register;
    }
}
