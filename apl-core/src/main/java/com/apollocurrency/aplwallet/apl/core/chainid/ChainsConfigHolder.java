package com.apollocurrency.aplwallet.apl.core.chainid;

import com.apollocurrency.aplwallet.apl.util.env.config.Chain;

import java.util.Map;
import java.util.UUID;

/**
 * Holds all available chains
 */
public class ChainsConfigHolder {
    private Map<UUID, Chain> chains;

    public ChainsConfigHolder(Map<UUID, Chain> chains) {
        this.chains = chains;
    }

    public ChainsConfigHolder() {
    }

    public Map<UUID, Chain> getChains() {
        return chains;
    }

    public void setChains(Map<UUID, Chain> chains) {
        this.chains = chains;
    }

    public Chain getActiveChain() {
        return ChainUtils.getActiveChain(chains);
    }
}