/*
 * Copyright © 2018-2019 Apollo Foundation
 */
package com.apollocurrency.aplwallet.apl.core.app.transaction;

import com.apollocurrency.aplwallet.apl.core.app.Account;
import com.apollocurrency.aplwallet.apl.core.app.AccountLedger;
import com.apollocurrency.aplwallet.apl.core.app.Asset;
import com.apollocurrency.aplwallet.apl.core.app.AssetDividend;
import com.apollocurrency.aplwallet.apl.core.app.AssetTransfer;
import com.apollocurrency.aplwallet.apl.core.app.Fee;
import com.apollocurrency.aplwallet.apl.core.app.Genesis;
import com.apollocurrency.aplwallet.apl.core.app.Order;
import com.apollocurrency.aplwallet.apl.core.app.Transaction;
import com.apollocurrency.aplwallet.apl.core.app.TransactionImpl;
import com.apollocurrency.aplwallet.apl.core.app.transaction.messages.Appendix;
import com.apollocurrency.aplwallet.apl.core.app.transaction.messages.ColoredCoinsAskOrderCancellation;
import com.apollocurrency.aplwallet.apl.core.app.transaction.messages.ColoredCoinsAskOrderPlacement;
import com.apollocurrency.aplwallet.apl.core.app.transaction.messages.ColoredCoinsAssetDelete;
import com.apollocurrency.aplwallet.apl.core.app.transaction.messages.ColoredCoinsAssetIssuance;
import com.apollocurrency.aplwallet.apl.core.app.transaction.messages.ColoredCoinsAssetTransfer;
import com.apollocurrency.aplwallet.apl.core.app.transaction.messages.ColoredCoinsBidOrderCancellation;
import com.apollocurrency.aplwallet.apl.core.app.transaction.messages.ColoredCoinsBidOrderPlacement;
import com.apollocurrency.aplwallet.apl.core.app.transaction.messages.ColoredCoinsDividendPayment;
import com.apollocurrency.aplwallet.apl.crypto.Convert;
import com.apollocurrency.aplwallet.apl.util.AplException;
import com.apollocurrency.aplwallet.apl.util.Constants;
import java.nio.ByteBuffer;
import java.util.Map;
import org.json.simple.JSONObject;

/**
 *
 * @author al
 */
public abstract class ColoredCoins extends TransactionType {
    
    public ColoredCoins() {
    }

    @Override
    public final byte getType() {
        return TransactionType.TYPE_COLORED_COINS;
    }
    public static final TransactionType ASSET_ISSUANCE = new CCAssetIssuance();
    public static final TransactionType ASSET_TRANSFER = new CCAssetTransfer();
    public static final TransactionType ASSET_DELETE = new CCAssetDelete();

    public static final TransactionType ASK_ORDER_PLACEMENT = new CCAskOrderReplacement();
    public static final TransactionType BID_ORDER_PLACEMENT = new CCBidOrderPlacement();

    public static final TransactionType ASK_ORDER_CANCELLATION = new CCAskOrderCancellation();
    public static final TransactionType BID_ORDER_CANCELLATION = new CCBidOrderCancellation();
    public static final TransactionType DIVIDEND_PAYMENT = new CCCoinsDividentPayment();
  
}
