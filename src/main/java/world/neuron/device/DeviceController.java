package world.neuron.device;

import io.vertx.core.json.JsonArray;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import org.apache.http.HttpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import world.neuron.service.Service;
import world.neuron.service.ServiceName;
import world.neuron.service.ServiceRepository;
import world.neuron.service.ServiceType;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@RequestScoped
@Path("/api/v1/device")
public class DeviceController {

    Logger logger = LoggerFactory.getLogger(DeviceController.class);

    @Inject
    DeviceService deviceService;

    @Inject
    ServiceRepository serviceRepository;


    @POST
    public Device createDevice(Device device) throws Exception {
        var ret = deviceService.createDevice(device);

        try {
            deviceService.registerDevice(ret.name);
        } catch (Exception e) {
            logger.error("error in creating the device", e);
            throw new HttpException(e.getMessage());
        }
        return ret;
    }

    @GET()
    @Path("/{name}")
    public Device getDeviceByNameAndEmail(@PathParam("name") String name) {
        return deviceService.getDeviceByNameForCurrentAccount(name);
    }

    @GET
    @Path("/filter")
    public List<Device> getDevicesByCriteria() {
        return new ArrayList<>();
    }

    @GET
    @Path("/all")
    public List<Device> getAllDevices() {

        return deviceService.getDevicesByLocation(0.0, 0.0, 0.0, 0.0);
    }

    @DELETE
    @Path("/delete/{name}")
    public void deleteDeviceByName(@PathParam("name") String name) {
        deviceService.deleteDevice(name);
    }

    @GET
    @Path("service")
    @Transactional
    public List<Service> createService() {
        Service servicePro = new Service();
        servicePro.documentUrl = "https://adsb.pro";
        servicePro.name = ServiceName.ADSB_PRO;
        servicePro.type = ServiceType.ADSB;

        Service serviceEnterprise = new Service();
        serviceEnterprise.documentUrl = "https://adsb.enterprise";
        serviceEnterprise.name = ServiceName.ADSB_ENTERPRISE;
        serviceEnterprise.type = ServiceType.ADSB;

        List<Service> serviceList = List.of(servicePro, serviceEnterprise);
        serviceRepository.persist(serviceList);
        return serviceList;
    }

    @GET
    @Path("/wip-all")
    public JsonArray getAllDevicesJSON() {

        return deviceService.workInProgressGetAllDevices();
    }

    public Instant getRegistrationTime(String stdOutTopic) {
        return deviceService.findRegisterTime(stdOutTopic);
    }
}

