package com.apollocurrency.aplwallet.apl.exchange.service;

import com.apollocurrency.aplwallet.apl.core.account.Account;
import com.apollocurrency.aplwallet.apl.core.account.LedgerEvent;
import com.apollocurrency.aplwallet.apl.core.app.EpochTime;
import com.apollocurrency.aplwallet.apl.core.app.TransactionProcessorImpl;
import com.apollocurrency.aplwallet.apl.core.app.UnconfirmedTransaction;
import com.apollocurrency.aplwallet.apl.core.db.DbIterator;
import com.apollocurrency.aplwallet.apl.core.db.cdi.Transactional;
import com.apollocurrency.aplwallet.apl.core.transaction.TransactionType;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.DexOfferCancelAttachment;
import com.apollocurrency.aplwallet.apl.eth.service.EthereumWalletService;
import com.apollocurrency.aplwallet.apl.exchange.dao.DexOfferDao;
import com.apollocurrency.aplwallet.apl.exchange.dao.DexOfferTable;
import com.apollocurrency.aplwallet.apl.exchange.model.DexCurrencies;
import com.apollocurrency.aplwallet.apl.exchange.model.DexOffer;
import com.apollocurrency.aplwallet.apl.exchange.model.DexOfferDBRequest;
import com.apollocurrency.aplwallet.apl.exchange.model.ExchangeBalances;
import com.apollocurrency.aplwallet.apl.exchange.model.ExchangeOrder;
import com.apollocurrency.aplwallet.apl.exchange.model.OfferStatus;
import com.apollocurrency.aplwallet.apl.exchange.model.OfferType;
import com.apollocurrency.aplwallet.apl.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Singleton
public class DexService {
    private static final Logger LOG = LoggerFactory.getLogger(DexService.class);

    private EthereumWalletService ethereumWalletService;
    private DexOfferDao dexOfferDao;
    private DexOfferTable dexOfferTable;
    private EpochTime epochTime;
    private TransactionProcessorImpl transactionProcessor;


    @Inject
    public DexService(EthereumWalletService ethereumWalletService, DexOfferDao dexOfferDao, EpochTime epochTime, DexOfferTable dexOfferTable, TransactionProcessorImpl transactionProcessor) {
        this.ethereumWalletService = ethereumWalletService;
        this.dexOfferDao = dexOfferDao;
        this.epochTime = epochTime;
        this.dexOfferTable = dexOfferTable;
        this.transactionProcessor = transactionProcessor;
    }


    @Transactional
    public DexOffer getOfferByTransactionId(Long transactionId){
        return dexOfferDao.getByTransactionId(transactionId);
    }

    /**
     * Use dexOfferTable for insert, to be sure that everything in one transaction.
     */
    @Transactional
    public void saveOffer (DexOffer offer){
        dexOfferTable.insert(offer);
    }

    @Transactional
    public List<DexOffer> getOffers(DexOfferDBRequest dexOfferDBRequest){
        return dexOfferDao.getOffers(dexOfferDBRequest);
    }

    public ExchangeBalances getBalances(String ethAddress, String paxAddress){
        ExchangeBalances balances = new ExchangeBalances();
        try{
            if (!StringUtils.isBlank(ethAddress)) {
                balances.setBalanceETH(ethereumWalletService.getBalanceWei(ethAddress));
            }
        } catch (Exception ex){
            LOG.error(ex.getMessage());
        }

        try{
            if (!StringUtils.isBlank(paxAddress)) {
                balances.setBalancePAX(ethereumWalletService.getPaxBalanceWei(paxAddress));
            }
        } catch (Exception ex){
            LOG.error(ex.getMessage());
        }

        return balances;
    }

    public List<ExchangeOrder> getHistory(String account, String pair, String type){
        return new ArrayList<>();
    }


    public ExchangeOrder getOrderByID(Long orderID){
        return null;
    }

    public boolean widthraw(String account,String secretPhrase,BigDecimal amount,String address,String cryptocurrency){
        return false;
    }


    public void closeOverdueOrders(){
        List<DexOffer> offers = dexOfferDao.getOverdueOrders(epochTime.getEpochTime());

        for (DexOffer offer : offers) {
            try {
                offer.setStatus(OfferStatus.EXPIRED);
                dexOfferTable.insert(offer);

                refundFrozenMoney(offer);
            } catch (Exception ex){
                LOG.error(ex.getMessage(), ex);
                throw new RuntimeException(ex);
            }
        }

    }

    public void cancelOffer(DexOffer offer){
        offer.setStatus(OfferStatus.CANCEL);
        saveOffer(offer);

        refundFrozenMoney(offer);
    }

    public void refundFrozenMoney(DexOffer offer){
        //Return APL.
        if(shouldFreezeAPL(offer.getType().ordinal(), offer.getOfferCurrency().ordinal())) {
            Account account = Account.getAccount(offer.getAccountId());
            account.addToUnconfirmedBalanceATM(LedgerEvent.DEX_REFUND_FROZEN_MONEY, offer.getTransactionId(), offer.getOfferAmount());
        }

        //Return Eth
        //TODO
    }


    public boolean shouldFreezeAPL(int offerType, int dexCurrencies){
        if (OfferType.SELL.ordinal() == offerType && DexCurrencies.APL.ordinal() == dexCurrencies) {
            return true;
        }
        return false;
    }

    /**
     * @param cancelTrId  can be null if we just want to check are there any unconfirmed transactions for this order.
     */
    public boolean isThereAnotherCancelUnconfirmedTx(Long orderId, Long cancelTrId){
        try(DbIterator<UnconfirmedTransaction>  tx = transactionProcessor.getAllUnconfirmedTransactions()) {
            while (tx.hasNext()) {
                UnconfirmedTransaction unconfirmedTransaction = tx.next();
                if (TransactionType.TYPE_DEX == unconfirmedTransaction.getTransaction().getType().getType() &&
                        TransactionType.SUBTYPE_DEX_OFFER_CANCEL == unconfirmedTransaction.getTransaction().getType().getSubtype()) {
                    DexOfferCancelAttachment dexOfferCancelAttachment = (DexOfferCancelAttachment) unconfirmedTransaction.getTransaction().getAttachment();

                    if(dexOfferCancelAttachment.getTransactionId() == orderId &&
                            !Objects.equals(unconfirmedTransaction.getTransaction().getId(),cancelTrId)){
                        return true;
                    }
                }
            }
        }

        return false;
    }


}
