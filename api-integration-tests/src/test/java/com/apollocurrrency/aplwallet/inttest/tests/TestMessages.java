package com.apollocurrrency.aplwallet.inttest.tests;

import com.apollocurrency.aplwallet.api.dto.PrunableMessageDTO;
import com.apollocurrency.aplwallet.api.response.CreateTransactionResponse;
import com.apollocurrrency.aplwallet.inttest.helper.WalletProvider;
import com.apollocurrrency.aplwallet.inttest.model.TestBase;
import com.apollocurrrency.aplwallet.inttest.model.Wallet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class TestMessages extends TestBase {


    @DisplayName("Send Message/Read Message")
    @ParameterizedTest
    @ArgumentsSource(WalletProvider.class)
    public void readMessage(Wallet wallet) throws IOException {
        String textMessage = "Test MSG";
        CreateTransactionResponse response = sendMessage(wallet,wallet.getUser(),textMessage);
        verifyCreatingTransaction(response);
        verifyTransactionInBlock(response.transaction);
        PrunableMessageDTO message =  readMessage(wallet,response.transaction);
        assertEquals(textMessage,message.message);
    }
}
