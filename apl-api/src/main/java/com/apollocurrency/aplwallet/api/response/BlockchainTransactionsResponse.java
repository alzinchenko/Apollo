package com.apollocurrency.aplwallet.api.response;

import com.apollocurrency.aplwallet.api.dto.TransactionDTO;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;


import java.util.ArrayList;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)

public class BlockchainTransactionsResponse {
    public String serverPublicKey;
    public float requestProcessingTime;
   // public ArrayList <TransactionDTO> transactionDTOS = new ArrayList<TransactionDTO>();
    public ArrayList <TransactionDTO> transactions = new ArrayList<TransactionDTO>();
    public ArrayList <TransactionDTO> unconfirmedTransactionDTOS = new ArrayList<TransactionDTO>();
}