package world.neuron.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import world.neuron.serviceregister.ServiceRegister;
import world.neuron.serviceregister.ServiceRegisterUtility;

import java.util.List;
import java.util.stream.Stream;

@ApplicationScoped
public class ServiceUtility {

    @Inject
    ServiceRepository serviceRepository;

    @Inject
    ServiceRegisterUtility registerUtility;

    public List<Service> getUnassignedServices(String deviceName) {

        List<ServiceRegister> registerList = registerUtility.findRegistersByDeviceName(deviceName);
        List<Service> services;

        try (Stream<Service> serviceStream = Service.streamAll()) {
            services = serviceStream
                    .filter(service -> !registerList.stream().map(register -> register.serviceId).toList().contains(service.id))
                    .toList();
        }

        return services;
    }

    public Service getServiceByName(ServiceName name) {
        return serviceRepository.find("name", name).firstResult();
    }

    public Service getServiceById(Long id) {
        return serviceRepository.findById(id);
    }
}
