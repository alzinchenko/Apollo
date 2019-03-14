package com.apollocurrency.aplwallet.api.dto;

public enum RequestType {
    importKey,
    exportKey,
    getECBlock,
    getBlockchainStatus,
    getMyInfo,
    addPeer,
    enable2FA,
    getPeer,
    deleteKey,
    generateAccount,
    deleteAccountProperty,
    requestType,
    getTransaction,
    getPrivateBlockchainTransactions,
    getAccount,
    getAccountBlockCount,
    getAccountBlocks,
    getAccountBlockIds,
    getAccountId,
    getAccountLedger,
    getAccountLedgerEntry,
    getAccountLessors,
    getAccountProperties,
    getAccountPublicKey,
    getBlockchainTransactions,
    getBalance,
    getGuaranteedBalance,
    getUnconfirmedTransactionIds,
    getUnconfirmedTransactions,
    setAccountInfo,
    startFundingMonitor,
    stopFundingMonitor,
    getAllPhasingOnlyControls,
    getPhasingOnlyControl,
    setPhasingOnlyControl,
    searchAccounts,
    getPeers,
    getBlocks,
    sendMoney,
    setAlias,
    setAccountProperty,
    getAlias,
    getAliases,
    getAliasCount,
    deleteAlias,
    sellAlias,
    buyAlias,
    getAliasesLike,
    encryptTo,
    decryptFrom,
    downloadPrunableMessage,
    sendMessage,
    getAllPrunableMessages,
    getPrunableMessage,
    readMessage,
    verifyPrunableMessage,
    issueAsset,
    getAccountAssetCount,
    getAccountAssets,
    getAsset,
    getAllAssets,
    getAssets,
    placeBidOrder,
    placeAskOrder,
    getAccountCurrentBidOrders,
    getAccountCurrentAskOrders,
    getAllOpenBidOrders,
    getAllOpenAskOrders,
    getAccountCurrentBidOrderIds,
    getAccountCurrentAskOrderIds,
    getAllTrades,
    getAssetAccountCount,
    getAssetAccounts,
    cancelBidOrder,
    cancelAskOrder,
    getAssetIds,
    getAssetTransfers,
    transferAsset,
    getAssetsByIssuer,
    getBidOrders,
    getAskOrders,
    getAskOrder,
    getBidOrder,
    getBidOrderIds,
    getAskOrderIds,
    getLastTrades,
    getOrderTrades,
    deleteAssetShares,
    getExpectedAssetDeletes,
    getExpectedOrderCancellations,
    getExpectedBidOrders,
    getExpectedAskOrders,
    getAssetDeletes,
    searchAssets,
    getBlock,
    getBlockId,
    sendMoneyPrivate

    }
