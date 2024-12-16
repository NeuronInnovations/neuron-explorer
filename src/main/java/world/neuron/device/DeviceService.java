package world.neuron.device;

import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.Status;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import world.neuron.account.Account;
import world.neuron.account.AccountService;
import world.neuron.hedera.HederaService;
import world.neuron.hedera.SmartContractService;
import world.neuron.hedera.topic.TopicService;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RequestScoped
public class DeviceService {


    private final Logger logger = LoggerFactory.getLogger(DeviceService.class);
    @Inject
    DeviceRepository deviceRepository;

    @Inject
    AccountService accountService;

    @Inject
    TopicService topicService;

    @Inject
    SmartContractService smartContractService;

    public Device getDeviceByNameForCurrentAccount(String name) {
        Account account = accountService.getCurrentAccount();
        return Device.find("name = ?1 and accountId = ?2", name, account.id).firstResult();
    }

    @Transactional
    Device createDevice(Device device) {
        PrivateKey privateKey = PrivateKey.generateECDSA();
        Account currentAccount = accountService.getCurrentAccount();
        device.accountId = currentAccount.id;
        device.hederaParentAccountNumber = currentAccount.hederaAccountNumber;
        device.creationTime = OffsetDateTime.now();
        device.privateKey = privateKey.toStringDER();
        device.publicKey = privateKey.getPublicKey().toStringDER();
        device.registrationStatus = DeviceRegistrationStatus.UNREGISTERED;
        device.deviceStatus = DeviceStatus.ACTIVE;
        device.persist();
        return device;
    }

    public Device registerDevice(String deviceName) throws Exception {
        logger.info("Registration of the device : " + deviceName + " in the hedera network has started.");
        Device device = getDeviceByNameForCurrentAccount(deviceName);
        if (device.hederaAccountNumber == null) {
            logger.info("Creating a hedera account for the device : " + deviceName);
            Account account = accountService.getCurrentAccount();
            var client = Client.forTestnet();
            CompletableFuture<AccountId> futureAccount = CompletableFuture.supplyAsync(() -> {
                AccountId newAccountId = null;
                while (newAccountId == null) {
                    try {
                        newAccountId = HederaService.neuronUserCreateHederaAccount(client, device.publicKey, account.privateKey, account.hederaAccountNumber);
                        updateDeviceHederaAccountId(device, newAccountId);
                        logger.info("Creating account for  " + deviceName);

                    } catch (Exception e) {
                        if (e.getMessage().contains(Status.INSUFFICIENT_PAYER_BALANCE.toString())) {
                            logger.info("Insufficient balance to create account for  " + deviceName);

                            throw new RuntimeException(deviceName + " is not registered due to " + Status.INSUFFICIENT_PAYER_BALANCE + ". Top up account " + account.hederaAccountNumber + "  at  https://portal.hedera.com/faucet     and try again");
                        }
                        if (e.getMessage().contains(Status.INVALID_ACCOUNT_ID.toString())) {

                            logger.info("Invalid account id  (means info query still failing)  " + deviceName);
                        } else {

                            logger.error(e.getMessage(), e);
                        }
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e1) {
                            logger.error(e.getMessage(), e1);

                            Thread.currentThread().interrupt();
                        }
                    }
                    logger.info("Created account : " + newAccountId);
                }
                return newAccountId;
            });


            var futureTxStdIn = futureAccount.thenApplyAsync(newAccountIdFin -> {
                String ret = null;
                try {
                    ret = HederaService.createTopic(client, newAccountIdFin.toString(), device.privateKey, "stdIn").toString();
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
                return ret;
            });

            var futureTxStdOut = futureAccount.thenApplyAsync(newAccountIdFin -> {
                String ret = null;
                try {
                    ret = HederaService.createTopic(client, newAccountIdFin.toString(), device.privateKey, "stdOut").toString();
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }

                return ret;
            });

            var futureTxStdErr = futureAccount.thenApplyAsync(newAccountIdFin -> {
                String ret = null;
                try {
                    ret = HederaService.createTopic(client, newAccountIdFin.toString(), device.privateKey, "stdErr").toString();
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
                return ret;
            });

            CompletableFuture<Void> allFutures = CompletableFuture.allOf(futureTxStdIn, futureTxStdOut, futureTxStdErr);

            allFutures.thenRun(() -> {
                try {
                    AccountId newAccountId = futureAccount.get();
                    String txStdIn = futureTxStdIn.get();
                    String txStdOut = futureTxStdOut.get();
                    String txStdErr = futureTxStdErr.get();

                    logger.info("Account ID: " + newAccountId);
                    logger.info("StdIn Topic: " + txStdIn);
                    logger.info("StdOut Topic: " + txStdOut);
                    logger.info("StdErr Topic: " + txStdErr);


                    var topicHolder = topicService.createHederaTopics(newAccountId.toString(), txStdIn.toString(), txStdOut.toString(), txStdErr.toString());
                    logger.info("just persisted all topics : " + topicHolder);
                } catch (Exception e) {
                    logger.info("Could not get stuff : " + e.getMessage());
                }
            }).join();
            client.close();
        }
        return device;
    }


    public Device registerDeviceFinal(String deviceName) {
        Device device = getDeviceByNameForCurrentAccount(deviceName);

        updateDeviceRegistrationStatus(device, DeviceRegistrationStatus.PENDING);

        var topic = topicService.getTopicByHederaAccountId(device.hederaAccountNumber);

        logger.info("Update a smart contract for the device : " + deviceName);

        boolean ok = false;
        try {
            ok = smartContractService.updateSmartContract(
                    topic,
                    deviceName,
                    device.hederaAccountNumber.toString(),
                    device.privateKey
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

        logger.info("Smart contract update status : " + ok);

        updateDeviceRegistrationStatus(device, DeviceRegistrationStatus.REGISTERED);
        logger.info("The account registration process for the device : " + deviceName + " has been completed successfully ");

        return device;
    }


    @Transactional
    public void updateDeviceHederaAccountId(Device device, AccountId accountId) {
        Device.update("hederaAccountNumber = ?1 where publicKey = ?2", accountId.toString(), device.publicKey);
    }

    @Transactional
    public void updateDeviceRegistrationStatus(Device device, DeviceRegistrationStatus registrationStatus) {
        Device.update("registrationStatus = ?1, registerTime =?2  where publicKey = ?3", registrationStatus, OffsetDateTime.now(), device.publicKey);
    }

    public List<Device> getActiveDevicesForCurrentAccount() {

        Account account = accountService.getCurrentAccount();
        if (account == null) {
            return new ArrayList<>();
        }

        return deviceRepository.find("accountId = ?1 and deviceStatus = ?2", account.id, DeviceStatus.ACTIVE).list();
    }

    List<Device> getDevicesByLocation(Double minlatitude, Double maxlatitude, Double minLongitude,
                                      Double maxLongitude) {
        return new ArrayList<>();
    }

    @Transactional
    public void deleteDevice(String name) {
        logger.info("The process of deleting a device named : " + name);
        Account account = accountService.getCurrentAccount();
        logger.info("The Hedera account number of the device owner : " + account.hederaAccountNumber);
        deviceRepository.update("deviceStatus = ?1, deletingTime = ?2 where hederaParentAccountNumber = ?3 and name = ?4", DeviceStatus.DISABLED, OffsetDateTime.now(), account.hederaAccountNumber, name);
        logger.info("The process of deleting the device named: " + name + " for account: " + account.hederaAccountNumber + "completed successfully");
    }

    public JsonArray workInProgressGetAllDevices() {
        var em = deviceRepository.getEntityManager();
        String sql = "SELECT d.devicerole, d.devicetype, d.hederaaccountnumber, d.hederaparentaccountnumber, d.name, " +
                "d.publickey, d.registertime, " +
                "ARRAY_AGG(sr.service_id ORDER BY sr.id) AS service_ids, " +
                "ARRAY_AGG(sr.fee ORDER BY sr.id) AS service_fees, " +
                "t.error AS topic_error, t.stdin AS topic_stdin, t.stdout AS topic_stdout " +
                "FROM public.device AS d " +
                "INNER JOIN public.serviceregister AS sr ON d.id = sr.device_id " +
                "INNER JOIN public.topic AS t ON d.hederaaccountnumber = t.hederaaccountid " +
                "WHERE d.registrationstatus = 0 " +
                "GROUP BY d.id, t.id";

        Query query = em.createNativeQuery(sql);
        List<Object[]> results = query.getResultList();

        JsonArray jsonArray = new JsonArray();

        for (Object[] row : results) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.put("devicerole", row[0]);
            jsonObject.put("devicetype", row[1]);
            jsonObject.put("hederaaccountnumber", row[2]);
            jsonObject.put("hederaparentaccountnumber", row[3]);
            jsonObject.put("name", row[4]);
            jsonObject.put("publickey", row[5]);
            jsonObject.put("registertime", row[6].toString());
            jsonObject.put("topic_error", row[9]);
            jsonObject.put("topic_stdin", row[10]);
            jsonObject.put("topic_stdout", row[11]);

            Long[] serviceIds = (Long[]) row[7];
            BigDecimal[] serviceFees = (BigDecimal[]) row[8];
            JsonArray servicesArray = new JsonArray();
            if (serviceIds != null && serviceFees != null) {
                for (int i = 0; i < serviceIds.length; i++) {
                    if (serviceIds[i] == null || serviceFees[i] == null) {
                        continue;
                    }
                    JsonObject serviceObject = new JsonObject();
                    serviceObject.put("service_id", serviceIds[i]);
                    serviceObject.put("fee", serviceFees[i].doubleValue());
                    servicesArray.add(serviceObject);
                }
            }
            jsonObject.put("services", servicesArray);

            jsonArray.add(jsonObject);
        }
        return jsonArray;
    }

    public Instant findRegisterTime(String stdOuTopic) {
        String sql = "SELECT device.registertime " +
                "FROM public.topic " +
                "INNER JOIN public.device ON device.hederaaccountnumber = topic.hederaaccountid " +
                "WHERE device.registertime IS NOT NULL " +
                "AND topic.stdout = ?1";
        var em = deviceRepository.getEntityManager();

        Query query = em.createNativeQuery(sql, Instant.class);
        query.setParameter(1, stdOuTopic);

        Instant registerTime = null;
        try {
            registerTime = (Instant) query.getSingleResult();
        } catch (NoResultException e) {
        }
        return registerTime;
    }
}
