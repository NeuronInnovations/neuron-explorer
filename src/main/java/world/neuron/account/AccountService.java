package world.neuron.account;

import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.Status;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.Claims;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import world.neuron.explorer.RegistrationSession;
import world.neuron.explorer.RegistrationSession.UserAccountInfo;
import world.neuron.hedera.HederaService;

import java.io.Serializable;

@RequestScoped
public class AccountService implements Serializable {

    public AccountService() {
    }

    private final Logger logger = LoggerFactory.getLogger(AccountService.class);
    @Inject
    AccountRepository accountRepository;


    @Inject
    JsonWebToken jsonWebToken;

    @Inject
    RegistrationSession registrationSession;

    @ConfigProperty(name = "hh.private_key")
    String hhPrivateKey;

    @ConfigProperty(name = "hh.operator_id")
    String hhOperatorId;


    public Account getCurrentAccount() {
        return accountRepository.find("email", jsonWebToken.claim(Claims.email).get().toString()).firstResult();
    }

    @Transactional
    public Account generateECDSAandPersist(Account account) throws Exception {
        logger.info("Started persisting an account with an email: " + account.email);
        if (jsonWebToken.claim(Claims.email).isPresent()) {
            String email = jsonWebToken.claim(Claims.email).get().toString();
            Account result = accountRepository.find("email", email).firstResult();
            if (result == null) {


                PrivateKey privateKey = PrivateKey.generateECDSA();
                account.privateKey = privateKey.toStringDER();
                account.publicKey = privateKey.getPublicKey().toStringDER();
                account.email = email;

                new Thread(() -> {
                    try {
                        activateRequestContextAndCreateHederaAccount(account.publicKey, account.privateKey);
                    } catch (Exception e) {
                        logger.error("Error creating Hedera account", e);
                    }
                }).start();

                account.persist();

            } else {
                throw new Exception("Account Exists");
            }
        }
        logger.info("Finished persisting an account with an email: " + account.email);
        return account;
    }


    @ActivateRequestContext
    public void activateRequestContextAndCreateHederaAccount(String publicKey, String privateKey) throws InterruptedException {
        boolean ok = false;
        while (!ok) {
            try {
                var hederaExplorerAccResult = HederaService.explorerCreateHederaAccount(publicKey, hhPrivateKey, hhOperatorId);
                UserAccountInfo userAccountInfo = new UserAccountInfo();
                userAccountInfo.setExplorerHederaAccount(hederaExplorerAccResult);
                registrationSession.setUserAccountInfo(publicKey, userAccountInfo);
                logger.info("Hedera account created: " + hederaExplorerAccResult);
                ok = true;
            } catch (Exception e) {
                logger.error("Error creating the session structure - something along the way crashed", e);
                if (e.getMessage().contains(Status.BUSY.toString())) {
                    Thread.sleep(6000);
                } else {
                    Thread.sleep(3000);
                }
            }
        }

    }

    @Transactional
    public Account createAccount(Account account) throws Exception {
        logger.info("Started creating a hedera account for account with a publicKey: " + account.publicKey);

        Account result = accountRepository.find("publicKey", account.publicKey).firstResult();

        while (registrationSession.getUserAccountInfo(result.publicKey) == null) {
            Thread.sleep(100);
        }

        result.hederaAccountNumber = registrationSession.getUserAccountInfo(result.publicKey).getExplorerHederaAccount().toString();

        result.persist();
        logger.info("Finished creating a hedera account no: " + result.hederaAccountNumber + " for account with a publicKey: " + result.publicKey);
        return result;
    }
}