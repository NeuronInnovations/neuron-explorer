package world.neuron.hedera;

import com.hedera.hashgraph.sdk.*;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.crypto.Credentials;
import org.web3j.model.Rendezvous;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tuples.generated.Tuple5;
import org.web3j.tx.gas.DefaultGasProvider;
import world.neuron.hedera.topic.Topic;
import world.neuron.service.ServiceName;
import world.neuron.serviceregister.ServiceRegister;
import world.neuron.serviceregister.ServiceRegisterUtility;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

@ApplicationScoped
public class SmartContractService {

    private final Logger logger = LoggerFactory.getLogger(SmartContractService.class);

    @ConfigProperty(name = "hh.eth_private_key")
    String hhEthPrivateKey;
    @ConfigProperty(name = "hh.contract_id")
    String hhContractId;
    @ConfigProperty(name = "hh.eth_contract_id")
    String hhEthContractId;


    @Inject
    ServiceRegisterUtility serviceRegisterUtility;

    AccountId OPERATOR_ID;
    PrivateKey OPERATOR_KEY;
    Web3j myEtherWallet;
    Rendezvous rendezvous;


    @PostConstruct
    public void setUp() {
        myEtherWallet = Web3j.build(
                new HttpService("https://testnet.hashio.io/api"));
    }

    public boolean updateSmartContract(Topic topic, String deviceName, String deviceAccountId, String devicePrivateKey) throws Exception {

        List<ServiceRegister> registers = serviceRegisterUtility.findRegistersByDeviceName(deviceName);
        long stdOut = Long.parseLong(topic.stdOut.substring(topic.stdOut.lastIndexOf(".") + 1));
        long stdIn = Long.parseLong(topic.stdIn.substring(topic.stdIn.lastIndexOf(".") + 1));
        long error = Long.parseLong(topic.error.substring(topic.error.lastIndexOf(".") + 1));
        byte[] services = new byte[7];
        byte[] fees = new byte[7];
        if (!registers.isEmpty()) {

            ServiceRegister register1 = registers.get(0);
            if (register1.service.name.equals(ServiceName.ADSB_PRO)) {
                services[0] = 1;
                fees[0] = register1.fee.multiply(BigDecimal.valueOf(100)).byteValueExact();
            } else if (register1.service.name.equals(ServiceName.ADSB_ENTERPRISE)) {
                services[1] = 1;
                fees[1] = register1.fee.multiply(BigDecimal.valueOf(100)).byteValueExact();
            }

            if (registers.size() == 2) {
                ServiceRegister register2 = registers.get(1);
                if (register2.service.name.equals(ServiceName.ADSB_PRO)) {
                    services[0] = 1;
                    fees[0] = register2.fee.multiply(BigDecimal.valueOf(100)).byteValueExact();
                } else if (register2.service.name.equals(ServiceName.ADSB_ENTERPRISE)) {
                    services[1] = 1;
                    fees[1] = register1.fee.multiply(BigDecimal.valueOf(100)).byteValueExact();
                }
            }
        }

        OPERATOR_ID = AccountId.fromString(deviceAccountId);
        OPERATOR_KEY = PrivateKey.fromString(devicePrivateKey);
        var hederaClient = Client.forTestnet().setOperator(OPERATOR_ID, OPERATOR_KEY);

        ContractExecuteTransaction transaction = new ContractExecuteTransaction()
                .setContractId(ContractId.fromString(hhContractId))
                .setGas(500000)
                .setFunction("putPeerAvailableSelf", new ContractFunctionParameters()

                        .addUint64(stdOut)
                        .addUint64(stdIn)
                        .addUint64(error)
                        .addString(deviceName)
                        .addUint8Array(services)
                        .addUint8Array(fees)
                );

        TransactionResponse txResponse = transaction.execute(hederaClient);

        TransactionReceipt receipt = txResponse.getReceipt(hederaClient);

        Status transactionStatus = receipt.status;
        hederaClient.close();
        logger.info("The transaction consensus status is " + transactionStatus);
        return transactionStatus.compareTo(Status.SUCCESS) == 0;
    }

    public void readSmartContract() throws Exception {
        rendezvous = Rendezvous.load(hhEthContractId, myEtherWallet,
                Credentials.create(hhEthPrivateKey), new DefaultGasProvider());
        BigInteger peerArraySize = rendezvous.getPeerArraySize().send();
        String peerAddress = rendezvous.peerList(BigInteger.valueOf(1)).send();

        Tuple5<Boolean, String, BigInteger, BigInteger, BigInteger> tuple5 = rendezvous.hederaAddressToPeer(peerAddress).send();

        Boolean component1 = tuple5.component1();
        String component2 = tuple5.component2();
        BigInteger component3 = tuple5.component3();
        BigInteger component4 = tuple5.component4();
        BigInteger component5 = tuple5.component5();

        BigInteger component51 = component5;


    }
}
