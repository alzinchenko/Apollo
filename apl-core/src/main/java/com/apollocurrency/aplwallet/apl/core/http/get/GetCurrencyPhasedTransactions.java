/*
 * Copyright © 2013-2016 The Nxt Core Developers.
 * Copyright © 2016-2017 Jelurida IP B.V.
 *
 * See the LICENSE.txt file at the top-level directory of this distribution
 * for licensing information.
 *
 * Unless otherwise agreed in a custom licensing agreement with Jelurida B.V.,
 * no part of the Nxt software, including this file, may be copied, modified,
 * propagated, or distributed except according to the terms contained in the
 * LICENSE.txt file.
 *
 * Removal or modification of this copyright notice is prohibited.
 *
 */

/*
 * Copyright © 2018-2019 Apollo Foundation
 */

package com.apollocurrency.aplwallet.apl.core.http.get;

import com.apollocurrency.aplwallet.apl.core.app.PhasingPoll;
import com.apollocurrency.aplwallet.apl.core.app.Transaction;
import com.apollocurrency.aplwallet.apl.core.app.VoteWeighting;
import com.apollocurrency.aplwallet.apl.core.db.DbIterator;
import com.apollocurrency.aplwallet.apl.core.http.APITag;
import com.apollocurrency.aplwallet.apl.core.http.AbstractAPIRequestHandler;
import com.apollocurrency.aplwallet.apl.core.http.JSONData;
import com.apollocurrency.aplwallet.apl.core.http.ParameterException;
import com.apollocurrency.aplwallet.apl.core.http.ParameterParser;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public class GetCurrencyPhasedTransactions extends AbstractAPIRequestHandler {
    private static class GetCurrencyPhasedTransactionsHolder {
        private static final GetCurrencyPhasedTransactions INSTANCE = new GetCurrencyPhasedTransactions();
    }

    public static GetCurrencyPhasedTransactions getInstance() {
        return GetCurrencyPhasedTransactionsHolder.INSTANCE;
    }

    private GetCurrencyPhasedTransactions() {
        super(new APITag[]{APITag.MS, APITag.PHASING}, "currency", "account", "withoutWhitelist", "firstIndex", "lastIndex");
    }

    @Override
    public JSONStreamAware processRequest(HttpServletRequest req) throws ParameterException {
        long currencyId = ParameterParser.getUnsignedLong(req, "currency", true);
        long accountId = ParameterParser.getAccountId(req, false);
        int firstIndex = ParameterParser.getFirstIndex(req);
        int lastIndex = ParameterParser.getLastIndex(req);
        boolean withoutWhitelist = "true".equalsIgnoreCase(req.getParameter("withoutWhitelist"));

        JSONArray transactions = new JSONArray();
        try (DbIterator<? extends Transaction> iterator = PhasingPoll.getHoldingPhasedTransactions(currencyId, VoteWeighting.VotingModel.CURRENCY,
                accountId, withoutWhitelist, firstIndex, lastIndex)) {
            while (iterator.hasNext()) {
                Transaction transaction = iterator.next();
                transactions.add(JSONData.transaction(false, transaction));
            }
        }
        JSONObject response = new JSONObject();
        response.put("transactions", transactions);
        return response;
    }

}