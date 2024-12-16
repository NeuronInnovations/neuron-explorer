package world.neuron.hedera;

import com.hedera.hashgraph.sdk.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class HederaService {
    private static final Logger logger = LoggerFactory.getLogger(HederaService.class);


    private static final Lock lock = new ReentrantLock();

    private static Client createClient(String accountId, String privateKey) {
        lock.lock();
        try {
            return Client.forTestnet().setOperator(AccountId.fromString(accountId), PrivateKey.fromString(privateKey));
        } finally {
            lock.unlock();
        }
    }

    public static AccountId explorerCreateHederaAccount(String newAccPublicKey, String hhPrivateKey, String hhOperatorId) throws PrecheckStatusException, TimeoutException, ReceiptStatusException {
        logger.info("Started creating a new hedera account from explorer parent");
        var client = createClient(hhOperatorId, hhPrivateKey);
        AccountCreateTransaction transaction = new AccountCreateTransaction()
                .setKey(PublicKey.fromString(newAccPublicKey))
                .setInitialBalance(new Hbar(15)); // that's enough for 3 devices
        TransactionResponse txResponse = transaction.execute(client);
        TransactionReceipt receipt = txResponse.getReceipt(client);
        logger.info("Finished creatxwing a new hedera account, a new hedera account number :" + receipt.accountId);
        client.close();
        return receipt.accountId;
    }

    public static AccountId neuronUserCreateHederaAccount(Client client, String newAccPublicKey, String userPrivateKey, String userOperatorId) throws TimeoutException, PrecheckStatusException {
        logger.info("Started creating a new hedera account from account parent " + userOperatorId);
        client.setOperator(AccountId.fromString(userOperatorId.trim()), PrivateKey.fromString(userPrivateKey.trim()));
        AccountBalance balance = new AccountBalanceQuery()
                .setAccountId(AccountId.fromString(userOperatorId.trim()))
                .execute(client);

        if (balance.hbars.toTinybars() < 3_11_88_0929) {
            throw new RuntimeException(Status.INSUFFICIENT_PAYER_BALANCE.toString());
        }

        AccountInfo accountInfo;

        var newAccPublicKeyT = PublicKey.fromString(newAccPublicKey);
        var userOperatorIdT = AccountId.fromString(userOperatorId);
        AccountId newAcccountAlias = newAccPublicKeyT.toAccountId(0, 0);


        accountInfo = new TransferTransaction()
                .addHbarTransfer(userOperatorIdT, new Hbar(2).negated())
                .addHbarTransfer(newAcccountAlias, new Hbar(2))
                .executeAsync(client)
                .thenApplyAsync(r -> {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {

                    }

                    AccountInfo ai;
                    try {
                        ai = new AccountInfoQuery()
                                .setAccountId(newAcccountAlias)
                                .execute(client);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                    return ai;

                }).join();


        logger.info("Finished creating a new hedera account, a new hedera account number :" + accountInfo.accountId);
        return accountInfo.accountId;
    }

    public static TopicId createTopic(Client client, String accountId, String privateKey, String topicMemo) throws PrecheckStatusException, TimeoutException, ReceiptStatusException {
        client.setOperator(AccountId.fromString(accountId), PrivateKey.fromString(privateKey));

        TopicId topicId;
        TopicCreateTransaction transaction = new TopicCreateTransaction()
                .setTopicMemo(topicMemo);
        TransactionResponse txResponse = transaction.execute(client);
        TransactionReceipt receipt = txResponse.getReceipt(client);
        ;
        topicId = receipt.topicId;
        return topicId;
    }
}