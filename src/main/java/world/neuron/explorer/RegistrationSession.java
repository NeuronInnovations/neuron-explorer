package world.neuron.explorer;

import jakarta.enterprise.context.ApplicationScoped;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.hedera.hashgraph.sdk.AccountId;


@ApplicationScoped
public class RegistrationSession implements Serializable {
    private static final long serialVersionUID = 1L;

    public  static class UserAccountInfo implements Serializable {
        private static final long serialVersionUID = 1L;

        private AccountId explorerHederaAccount;

        public AccountId getExplorerHederaAccount() {
            return explorerHederaAccount;
        }

        public void setExplorerHederaAccount(AccountId explorerHederaAccount) {
            this.explorerHederaAccount = explorerHederaAccount;
        }
    }

    private Map<String, UserAccountInfo> userAccountMap = new ConcurrentHashMap<>();

    public UserAccountInfo getUserAccountInfo(String publicKey) {
        return userAccountMap.get(publicKey);
    }

    public void setUserAccountInfo(String publicKey, UserAccountInfo userAccountInfo) {
        userAccountMap.put(publicKey, userAccountInfo);
    }
}
