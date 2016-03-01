/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */


package com.sun.javacard.samples.utilitydemo;

import javacard.framework.*;
import javacardx.framework.math.*;
import javacardx.framework.tlv.*;
import javacardx.framework.util.intx.JCint;

/**
 * This class demonstrates usage of new Utility APIs including APIs for TLV
 * integer manipulation and big number math API
 */
public class BrokerApplet extends Applet{
    /**
     * Initial account balance
     */
    final static byte[] INITIAL_ACCOUNT_BALANCE = 
                {(byte)0x01, (byte)0x00, (byte)0x00, 
                 (byte)0x00, (byte)0x00, (byte)0x00 };
    /**
     * INS value for ISO 7816-4 VERIFY command
     */
    final static byte VERIFY = (byte) 0x20;
    
    /**
     * INS value to get complete portfolio
     */
    final static byte GET_COMPLETE_PORTFOLIO = (byte)0x01;
    
    /**
     * INS value to get information on a stock
     */
    final static byte GET_STOCK_INFO = (byte)0x02;

    /**
     * INS value to generate a stock purchase request for the broker
     */
    final static byte BUY_STOCK = (byte)0x03;
    
    /**
     * INS value to generate a sell stocks request for the broker
     */
    final static byte SELL_STOCK = (byte)0x04;
    
    /**
     * INS value to update the portfolio
     */
    final static byte UPDATE_PORTFOLIO = (byte)0x05;
    
    /**
     * INS value to get balance
     */
    final static byte GET_BALANCE = (byte)0x06;

    /**
     * Length of a stock symbol
     */
    final static byte STOCK_SYMBOL_LENGTH = (byte)5;
    
    /**
     * Maximum number of incorrect tries before the PIN is blocked
     */
    final static byte MAX_PIN_TRIES = (byte) 0x03;

    /**
     * Maximum PIN size
     */
    final static byte MAX_PIN_SIZE = (byte) 0x08;

    /**
     * SW bytes for PIN verification failure
     */
    final static short SW_VERIFICATION_FAILED = 0x6300;
    
    /**
     * SW bytes for No stock info found
     */
    final static short SW_STOCK_NOT_FOUND = 0x6301;

    /**
     * SW bytes for Not enough account balance
     */
    final static short SW_NOT_ENOUGH_ACCOUNT_BALANCE = 0x6302;

    /**
     * SW bytes for not enough stocks to sell
     */
    final static short SW_NOT_ENOUGH_STOCKS_TO_SELL = 0x6303;

    /**
     * SW bytes for malformed broker confirmation
     */
    final static short INVALID_BROKER_CONFIRMATION = 0x6304;
    
    /**
     * SW bytes for invalid broker signature
     */
    final static short INVALID_BROKER_SIGNATURE = 0x6305;
    
    /**
     * SW bytes for TLV Exception
     */
    final static short TLV_EXCEPTION = 0x6306;

    /**
     * SW bytes for Arithmetic exception
     */
    final static short ARITHMETIC_EXCEPTION = 0x6307;

    /**
     * SW bytes for Arithmetic exception
     */
    final static short INVALID_NUMBER_FORMAT = 0x6308;

    /**
     * The user PIN
     */
    private OwnerPIN pin;
    
    /**
     * Amount of money in user's account 
     */
    private BigNumber accountBalance;
    
    /**
     * This constructed BER TLV holds the portfolio
     */
    private ConstructedBERTLV portfolio;
    
    /**
     * dummy broker signature 
     */
    private static final byte[] dummySignature = {(byte)0x88, (byte)0x88,
                                                  (byte)0x88, (byte)0x88,
                                                  (byte)0x88, (byte)0x88,
                                                  (byte)0x88, (byte)0x88};
    
    /**
     * constructed BER TLV Tag for portfolio
     */
    ConstructedBERTag portfolioTag;

    /**
     * constructed BER TLV Tag stock informaiton
     */
    ConstructedBERTag stockInfoTag;
    
    /**
     * constructed BER TLV Tag for sell stock request for broker
     */
    ConstructedBERTag sellStockReqTag;

    /**
     * constructed BER TLV Tag for stock purchase request for broker
     */
    ConstructedBERTag buyStockReqTag;
    
    /**
     * constructed BER TLV Tag for stock purchase confirmation from broker
     */
    ConstructedBERTag purchaseConfirmTag;
    
    /**
     * constructed BER TLV Tag for sell stock confirmation from broker
     */
    ConstructedBERTag sellConfirmTag;
    
    /**
     * constructed BER TLV Tag for last trade information
     */
    ConstructedBERTag lastTradeTag;
    
    /**
     * Primitive BER TLV Tag for number of stocks 
     */
    PrimitiveBERTag numStocksTag;
    /**
     * Primitive BER TLV Tag for price information
     */
    PrimitiveBERTag priceTag;
    /**
     * Primitive BER TLV Tag for stock symbol information
     */
    PrimitiveBERTag symbolTag;
    /**
     * Primitive BER TLV Tag for broker signature
     */
    PrimitiveBERTag signatureTag;
            
    /**
     * Big number for temporary calculations
     */
    BigNumber tempBigNum;
    
    /**
     * temporary buffer used as scratch space
     */
    byte[] scratchSpace;
        
    /**
     * The constructor
     * @param bArray input array
     * @param bOffset is the offset in input array
     * @param bLength input array length
     */
    BrokerApplet(byte[] bArray, short bOffset, byte bLength) {
        byte aidLen = bArray[bOffset]; // aid length
        if (aidLen == (byte) 0) {
            register();
        } else {
            register(bArray, (short) (bOffset + 1), aidLen);
        }

        // Ignore control info
        bOffset = (short) (bOffset + aidLen + 1);
        byte infoLen = bArray[bOffset]; // control info length
        bOffset = (short) (bOffset + infoLen + 1);

        byte paramLen = bArray[bOffset++]; // applet parameters length

        // Retrieve PIN initialization value from installation parameters

        if (paramLen > MAX_PIN_SIZE) {
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        }

        // Retrieve the PIN
        pin = new OwnerPIN(MAX_PIN_TRIES, MAX_PIN_SIZE);
        pin.update(bArray, bOffset, (byte)(paramLen));
        
        // initialize account balance to 100,000.00
        accountBalance = new BigNumber((byte)8);
        accountBalance.init(INITIAL_ACCOUNT_BALANCE, (byte)0, 
                (byte)INITIAL_ACCOUNT_BALANCE.length, BigNumber.FORMAT_BCD);
        
        // initialize the temporary big number
        tempBigNum = new BigNumber(BigNumber.getMaxBytesSupported());
        
        // initialize primitive tags
        initPrimitiveTags();
        
        // initialize constructed tags
        initConstructedTags();
        
        // initialize the scratchSpace 
        scratchSpace = JCSystem.makeTransientByteArray((short)10, 
                                            JCSystem.CLEAR_ON_DESELECT);
        
        // create an empty portfolio 
        portfolioTag.toBytes(scratchSpace, (short)0);
        portfolio = (ConstructedBERTLV)BERTLV.getInstance(scratchSpace, 
                                                            (short)0, (short)2);
    }

    /**
     * Initialize the constructed tags
     */
    private void initConstructedTags(){
        portfolioTag = new ConstructedBERTag();
        stockInfoTag = new ConstructedBERTag();
        sellStockReqTag = new ConstructedBERTag();
        buyStockReqTag = new ConstructedBERTag();
        purchaseConfirmTag = new ConstructedBERTag();
        sellConfirmTag = new ConstructedBERTag();
        lastTradeTag = new ConstructedBERTag();
                
        portfolioTag.init((byte)3, (short)1);
        stockInfoTag.init((byte)3, (short)2);
        sellStockReqTag.init((byte)3, (short)3);
        buyStockReqTag.init((byte)3, (short)4);
        sellConfirmTag.init((byte)3, (short)5);
        purchaseConfirmTag.init((byte)3, (short)6);
        lastTradeTag.init((byte)3, (short)7);
    }    

    /**
     * Initialize the primitive tags
     */
    private void initPrimitiveTags(){
        symbolTag = new PrimitiveBERTag();
        numStocksTag = new PrimitiveBERTag();
        priceTag = new PrimitiveBERTag();
        signatureTag = new PrimitiveBERTag();
                
        symbolTag.init((byte)3, (short)8);
        numStocksTag.init((byte)3, (short)9);
        priceTag.init((byte)3, (short)10);
        signatureTag.init((byte)3, (short)11);
    }
    
    /**
     * Installs this applet.
     * @see APDU
     * @param apdu the incoming APDU containing the INSTALL command.
     * @exception ISOException with the response bytes per ISO 7816-4
     */
    public static void install( byte[] bArray, short bOffset, byte bLength ){
        new BrokerApplet(bArray, bOffset, bLength);
    }
    
    /**
     * Select method
     */
    public boolean select() {
        // The applet declines to be selected
        // if the PIN is blocked.
        if (pin.getTriesRemaining() == 0) {
            return false;
        }
        return true;
    }

    /**
     * deselect method
     */
    public void deselect() {
        // Reset the PIN value
        pin.reset();
    }
    
    /**
     * Process method
     */
    public void process(APDU apdu) throws ISOException {
        // get the APDU buffer
        byte buffer[] = apdu.getBuffer();
        
        // return if this APDU is for applet selection
        if(selectingApplet())return;
        
        // mask the channel information
        buffer[ISO7816.OFFSET_CLA] = 
                (byte) (buffer[ISO7816.OFFSET_CLA] & (byte) 0xFC);
        
        // get the data part of the APDU if this is not getPortfolio command or
        // get balance command 
        if(buffer[ISO7816.OFFSET_INS] != GET_COMPLETE_PORTFOLIO &&
                buffer[ISO7816.OFFSET_INS] != GET_BALANCE)
            apdu.setIncomingAndReceive();
        
        // pin verification command
        if (buffer[ISO7816.OFFSET_CLA] == ISO7816.CLA_ISO7816) {
            if (buffer[ISO7816.OFFSET_INS] == VERIFY) {
                verify(buffer);
                return;
            } else {
                ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
            }
        }
        
        // if PIN is not already verified throw exception
        if(!pin.isValidated()){
            ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
        }
        
        short responseSize = 0;
        try{
            switch(buffer[ISO7816.OFFSET_INS]){
                case GET_COMPLETE_PORTFOLIO:
                    responseSize = getPortfolio(buffer);
                    break;
                case GET_STOCK_INFO:
                    responseSize = getStockInfo(buffer);
                    break;
                case BUY_STOCK:
                    responseSize = genStockPurchaseRequest(buffer);
                    break;
                case SELL_STOCK:
                    responseSize = genSellStockRequest(buffer);
                    break;
                case UPDATE_PORTFOLIO:
                    updatePortfolio(buffer);
                    return;
                case GET_BALANCE:
                    responseSize = getBalance(buffer);
                    break;
                default: ISOException.throwIt(ISO7816.SW_FUNC_NOT_SUPPORTED);
            }
        }catch(TLVException e){
            ISOException.throwIt(TLV_EXCEPTION);
        }catch(ArithmeticException e){
            ISOException.throwIt(ARITHMETIC_EXCEPTION);
        }
        
        // send the response data back
        apdu.setOutgoingAndSend((short)0, (byte)responseSize);
        
    }
    
    /**
     * Verifies the PIN.
     * 
     * @param buffer The APDU buffer
     */
    private void verify(byte[] buffer) {
        byte numBytes = buffer[ISO7816.OFFSET_LC];

        // Verify PIN
        if (pin.check(buffer, ISO7816.OFFSET_CDATA, numBytes) == false) {
            ISOException.throwIt(SW_VERIFICATION_FAILED);
        }
    }
    
    /**
     * Return following information on all the stocks
     *  - Primitive TLV for stock symbol
     *  - Primitive TLV for number of stocks we currently have
     *  - Constructed TLV for last trade containing following
     *      - Primitive TLV for number of stocks
     *      - Primitive TLV for price
     * @param buffer is the input APDU buffer
     * @return size of response data
     */
    private short getPortfolio(byte[] buffer){
        try{
            return portfolio.toBytes(buffer, (short)0);
        }catch(TLVException e){
            if(e.getReason() == TLVException.EMPTY_TLV)
                ISOException.throwIt(SW_STOCK_NOT_FOUND);
        }
        return 0;
    }
    
    /**
     * Returns stock information
     * Input: 5 bytes representing stock symbol
     * Output: Constructed BER TLV containing the following:
     *  - Primitive TLV for stock symbol
     *  - Primitive TLV for number of stocks we currently have
     *  - Constructed TLV for last trade containing following
     *      - Primitive TLV for number of stocks
     *      - Primitive TLV for price
     * @param buffer is the input APDU buffer
     * @return size of response data
     */
    private short getStockInfo(byte[] buffer){
        // Find the stock in the portfolio
        ConstructedBERTLV stockInfo = findStock(buffer, ISO7816.OFFSET_CDATA);
        if(stockInfo == null)ISOException.throwIt(SW_STOCK_NOT_FOUND);
        return stockInfo.toBytes(buffer, (short)0);
    }
    
    /**
     * Finds a stock that matches the stock symbol
     * @param buffer is the byte array containing the stock symbol
     * @param offset is the offset in the byte array for stock symbol
     * @return ConstructedBERTLV for the stock information if stock
     * is found. If the stock is not found ISOException is thrown
     */
    private ConstructedBERTLV findStock(byte[] buffer, short offset){
        // We go through all the stocks that we have to find the
        // one we're looking for 
        ConstructedBERTLV stockInfo = 
                (ConstructedBERTLV)portfolio.find(stockInfoTag);
        
        while(stockInfo != null){
            PrimitiveBERTLV symbol = (PrimitiveBERTLV)stockInfo.find(symbolTag);
            symbol.getValue(scratchSpace, (short)0);
            if(Util.arrayCompare(buffer, offset, scratchSpace, 
                    (short)0, STOCK_SYMBOL_LENGTH) == 0){
                return stockInfo;
            }
            stockInfo = 
                (ConstructedBERTLV)portfolio.findNext(stockInfoTag, stockInfo, (short)1);
        }
        return null;
    }
    
    /**
     * Return current balance
     * @param buffer is the input APDU buffer
     * @return size of response data
     */
    private short getBalance(byte[] buffer){
        if(buffer[ISO7816.OFFSET_P1] == BigNumber.FORMAT_BCD)
            accountBalance.toBytes(buffer, (short)0, 
                    (short)8, BigNumber.FORMAT_BCD);
        else if (buffer[ISO7816.OFFSET_P1] == BigNumber.FORMAT_HEX)
            accountBalance.toBytes(buffer, (short)0, 
                    (short)8, BigNumber.FORMAT_HEX);
        else
            ISOException.throwIt(INVALID_NUMBER_FORMAT);
        return (short)8;
    }
    

    /**
     * Performs the following steps to assist in buying a stock
     * - Check available funds
     * - Create a "signed" request for the broker
     * @param buffer is the input APDU buffer
     * @return size of response data
     */
    private byte genStockPurchaseRequest(byte[] buffer){
        short offset = ISO7816.OFFSET_CDATA;
        // first 5 bytes contain the stock symbol with each byte represnting
        // an ASCII value
        offset += (short)5;
        
        // Now get the number of stocks we want to buy.
        int numStocksToBuy = JCint.getInt(buffer, offset); 
        offset += (short)4;
        
        // get the desired buy price
        short priceToBuyAt = Util.getShort(buffer, offset);
        offset += (short)2;

        // verify that we have enough balance available
        verifyBalanceAvailability(buffer);

        // Generate the request. The request is a constructed BER TLV with 
        // following elements:
        //      - Stock symbol
        //      - desired price
        //      - number of stocks to buy

        // output the broker request tag and length
        offset = buyStockReqTag.toBytes(buffer, (short)0);
        buffer[offset++] = (byte)0;
        
        // output the stock symbol TLV
        offset = symbolTag.toBytes(scratchSpace, (short)0);
        scratchSpace[offset++] = STOCK_SYMBOL_LENGTH;
        offset = Util.arrayCopyNonAtomic(buffer, ISO7816.OFFSET_CDATA, 
                scratchSpace, offset, STOCK_SYMBOL_LENGTH);
        ConstructedBERTLV.append(scratchSpace, (short)0, buffer, (short)0);
        completeBrokerRequest(buffer, priceToBuyAt, numStocksToBuy);
        return (byte)(buffer[1] + 2); // total length of TLV 
    }
    
    /**
     * Verifies that we have enough balance available to buy a stock
     * @param buffer is the input buffer containing the number of stocks
     * we want to buy and the price of the stock
     */
    void verifyBalanceAvailability(byte[] buffer){
        try{
            short offset = ISO7816.OFFSET_CDATA + (short)5;
            // check if we have enough balance available to buy this stock
            // initialize the number with number of stocks to buy
            tempBigNum.init(buffer, offset, (short)4, BigNumber.FORMAT_HEX);
            offset += (short)4;
            //multiply this number by desired stock price
            tempBigNum.multiply(buffer, offset, (short)2, BigNumber.FORMAT_HEX);
            
            // now we compare this number against the current available balance
            // to determine if we have enough funds available.
            if(accountBalance.compareTo(tempBigNum) < (byte)0){
                ISOException.throwIt(SW_NOT_ENOUGH_ACCOUNT_BALANCE);
            }
        }catch(ArithmeticException e){
            ISOException.throwIt(SW_NOT_ENOUGH_ACCOUNT_BALANCE);
        }
    }
    
    /**
     * Returns stock a sell stock request to the client if we hold
     * enough stocks.
     * Input: Stock symbol, the number of stocks to sell and desired price
     * Output: Constructed BER TLV containing the following:
     *  - Primitive TLV for stock symbol
     *  - Primitive TLV for desired stock price
     *  - Primitive TLV for number of stocks to sell
     *  - Primitive TLV for signature
     * @param buffer is the input APDU buffer
     * @return size of response data
     */
    private byte genSellStockRequest(byte[] buffer){
        short offset = ISO7816.OFFSET_CDATA;

        // check to see if we actually have this stock
        ConstructedBERTLV stockInfo = findStock(buffer, offset);
        if(stockInfo == null) ISOException.throwIt(SW_STOCK_NOT_FOUND);
        
        offset += STOCK_SYMBOL_LENGTH;
        
        // Now get the number of stocks we want to sell.
        int numStocksToSell = JCint.getInt(buffer, offset); 
        offset += (short)4;
        
        // get the desired sell price
        short priceToSellAt = Util.getShort(buffer, offset);
        offset += (short)2;
        
        // now within this constructed BER TLV, we need to find the Primitive 
        // TLV that has the number of stocks we currently hold
        PrimitiveBERTLV numStocksTLV = 
                    (PrimitiveBERTLV)stockInfo.find(numStocksTag);
        // use the buffer as scratch space since we have already 
        // taken out the values that we wanted to take out.
        numStocksTLV.getValue(buffer, (short)0);
        // we already know that this is a 4 byte integer value
        // create an integer from bytes
        int numCurrStock = JCint.getInt(buffer, (byte)0);
        
        // check that we have enough stocks that meet the
        // sell request requirement
        if(numCurrStock < numStocksToSell)
            ISOException.throwIt(SW_NOT_ENOUGH_STOCKS_TO_SELL);
      
        Util.arrayFillNonAtomic(buffer, (short)0, (short)100, (byte)0);
        // output the broker request tag and length
        offset = sellStockReqTag.toBytes(buffer, (short)0);
        buffer[offset] = 0;
        
        // output the stock symbol TLV
        PrimitiveBERTLV stockSymbol = 
                (PrimitiveBERTLV)stockInfo.find(symbolTag);
        
        offset = stockSymbol.toBytes(scratchSpace, (short)0);
        // append to sell request
        ConstructedBERTLV.append(scratchSpace, (short)0, buffer, (short)0);
        completeBrokerRequest(buffer, priceToSellAt, numStocksToSell);
        return (byte)(buffer[1] + 1); // length of TLV 
    }

    /**
     * This method completes the stock sell or purchase request for broker.
     * In this method we add the following primitive TLVs to the broker 
     * request constructed TLV
     * Primitive TLV for desired stock price
     * Primitive TLV for number of stocks to sell
     * Primitive TLV for signature
     * @param buffer is the input APDU buffer
     * @return size of response data
     */
    private void completeBrokerRequest(byte[] buffer, 
                                    short priceToSellAt, int numStocksToSell){
        short offset = 0;
        // output the desired stock price TLV
        offset = priceTag.toBytes(scratchSpace, (short)0);
        scratchSpace[offset++] = (byte)2;
        Util.setShort(scratchSpace, offset, priceToSellAt);
        ConstructedBERTLV.append(scratchSpace, (short)0, buffer, (short)0);
        
        // output number of stocks: Tag then length then value
        offset = numStocksTag.toBytes(scratchSpace, (short)0);
        scratchSpace[offset++] = (byte)4;
        JCint.setInt(scratchSpace, offset, numStocksToSell);
        ConstructedBERTLV.append(scratchSpace, (short)0, buffer, (short)0);
        
        // output the signature tag length and value
        offset = signatureTag.toBytes(scratchSpace, (short)0);
        scratchSpace[offset++] = (byte)8; // signature length
        Util.arrayCopyNonAtomic(dummySignature, (short)0, scratchSpace, 
                                            offset, (byte)8);              
        ConstructedBERTLV.append(scratchSpace, (short)0, buffer, (short)0);
    }
    

    /**
     * Verifies the signature of broker and if the signature is verified
     * update the portfolio including stocks and available balance
     * @param buffer is the input APDU buffer
     */
    private void updatePortfolio(byte[] buffer){
        short offset = ISO7816.OFFSET_CDATA;
        boolean isSellConfirmation = false;
        /*
         * The buffer contains the constructed BER TLV received from the 
         * broker. The constructed BER TLV contains the following values
         * in form of primitive TLV objects
         * - Stock Symbol 
         * - Stock Price
         * - Number of stocks
         * - Broker Signature (dummy signature 9999 9999 is used)
         */
        // To find out if this a sell confirmation or purchase confirmation
        // we match the tag against sell and purchase confirmation tags
        if(!BERTag.isConstructed(buffer, offset) || 
                BERTag.tagClass(buffer, offset) != (byte)3)
            ISOException.throwIt(INVALID_BROKER_CONFIRMATION);
        // get the tag number
        short confirmTagNumber = BERTag.tagNumber(buffer, offset);
        if(confirmTagNumber == sellConfirmTag.tagNumber())
            isSellConfirmation = true;
        else if(confirmTagNumber == purchaseConfirmTag.tagNumber())
            isSellConfirmation = false;
        else 
            ISOException.throwIt(INVALID_BROKER_CONFIRMATION);
        
        // get the stock symbol TLV
        symbolTag.toBytes(scratchSpace, (short)0);
        short symbolTLVOffset = ConstructedBERTLV.find(buffer, offset, 
                                                    scratchSpace, (short)0);
        
        numStocksTag.toBytes(scratchSpace, (short)0);
        short numStocksTLVOffset = ConstructedBERTLV.find(buffer, offset, 
                                                    scratchSpace, (short)0);
        short numStocksValueOffset = PrimitiveBERTLV.getValueOffset(buffer, 
                                                           numStocksTLVOffset);
        // get the stock price
        priceTag.toBytes(scratchSpace, (short)0);
        short priceTLVOffset = ConstructedBERTLV.find(buffer, offset, 
                                                    scratchSpace, (short)0);
        short priceValueOffset = PrimitiveBERTLV.getValueOffset(buffer, 
                                                           priceTLVOffset);
        
        verifyBrokerSignature(buffer, ISO7816.OFFSET_CDATA);
        
        // next is the stock symbol in 5 bytes. If we find already have
        // the stock, we will update that TLV accordingly. If we do not
        // have the stock, we'll add the new stock to portfolio
        ConstructedBERTLV stockInfo = findStock(buffer, 
                PrimitiveBERTLV.getValueOffset(buffer, symbolTLVOffset));
        
        if(stockInfo == null){
            if(isSellConfirmation){
                // we didn't have the stock that we sold!!!
                ISOException.throwIt(INVALID_BROKER_CONFIRMATION);
            } 
            // create the new constructed TLV for this stock
            stockInfo = createNewStockTLV(buffer, symbolTLVOffset, 
                    numStocksTLVOffset, priceTLVOffset);
            portfolio.append(stockInfo);
            updateAccountBalance(buffer, numStocksValueOffset, 
                                    priceValueOffset, isSellConfirmation);
            return;
        }
        // update the existing stockInfoTLV
        // update num stocks
        PrimitiveBERTLV numStocksTLV = 
                (PrimitiveBERTLV)stockInfo.find(numStocksTag);
        numStocksTLV.getValue(scratchSpace, (short)0);
        tempBigNum.init(scratchSpace, (short)0, (byte)4, BigNumber.FORMAT_HEX);
        if(!isSellConfirmation){
            tempBigNum.add(buffer, numStocksValueOffset, (short)4, 
                                                        BigNumber.FORMAT_HEX);
        }else{
            tempBigNum.subtract(buffer, numStocksValueOffset, (short)4, 
                                                        BigNumber.FORMAT_HEX);
            // if stocks now number 0, we remove this TLV
            Util.arrayFillNonAtomic(scratchSpace, (short)0, (short)1, (byte)0);
            if(tempBigNum.compareTo(scratchSpace, (short)0, 
                    (short)1, BigNumber.FORMAT_HEX) == 0){
                portfolio.delete(stockInfo, (short)1);
                return;
            }
        }
        tempBigNum.toBytes(scratchSpace, (short)0, (short)4, 
                BigNumber.FORMAT_HEX);
        numStocksTLV.replaceValue(scratchSpace, (short)0, (short)4);
        
        // update last trade information
        ConstructedBERTLV lastTradeTLV = 
                (ConstructedBERTLV)stockInfo.find(lastTradeTag);        
        numStocksTLV = (PrimitiveBERTLV)lastTradeTLV.find(numStocksTag);
        numStocksTLV.replaceValue(buffer, numStocksValueOffset, (short)4);
        PrimitiveBERTLV priceTLV = (PrimitiveBERTLV)lastTradeTLV.find(priceTag);
        priceTLV.replaceValue(buffer, priceValueOffset, (short)2);

        // update the account balance
        updateAccountBalance(buffer, numStocksValueOffset, 
                priceValueOffset, isSellConfirmation);
    }
    
    /**
     * Update the account balance
     * @param buffer is the input buffer
     * @param numStocksOffset is the offset in the buffer where number of 
     * stocks information is at
     * @param priceOffset is the offset in the buffer where stock price
     * information is at
     * @param sold is the boolean indicating the if the stock was bought or
     * sold
     */
    void updateAccountBalance(byte[] buffer, short numStocksOffset, 
            short priceOffset, boolean sold){
        tempBigNum.init(buffer, numStocksOffset, (byte)4, BigNumber.FORMAT_HEX);
        
        // get the amount for this trade
        tempBigNum.multiply(buffer, priceOffset, (short)2, 
                BigNumber.FORMAT_HEX);
        
        tempBigNum.toBytes(scratchSpace, (short)0, 
                (short)8, BigNumber.FORMAT_HEX);
        
        if(sold){
            // if stock was sold, we add to the balance
            accountBalance.add(scratchSpace, (short)0, 
                    (short)8, BigNumber.FORMAT_HEX);
        }else{
            // if stock was bought we subtract the amount
            // from balance
            accountBalance.subtract(scratchSpace, (short)0, 
                    (short)8, BigNumber.FORMAT_HEX);
        }
    }
    
    /**
     * Verify the broker signature. This method throws an exception if the
     * broker signature is invalid
     * @param buffer is the input APDU buffer
     * @param TLVOffset is the offset within the buffer where the TLV for 
     * the broker signature starts
     */ 
    void verifyBrokerSignature(byte[] buffer, short TLVOffset){
        // get the stock price
        signatureTag.toBytes(scratchSpace, (short)0);
        short sigTLVOffset = ConstructedBERTLV.find(buffer, TLVOffset, 
                                                    scratchSpace, (short)0);
        short sigValueOffset = PrimitiveBERTLV.getValueOffset(buffer, 
                                                                sigTLVOffset);
        if(Util.arrayCompare(buffer, sigValueOffset, dummySignature, 
                (short)0, (byte)dummySignature.length) != 0){
            ISOException.throwIt(INVALID_BROKER_SIGNATURE);
        }
    }
    
    /**
     * As a rsult of a stock that is bought, we create a new
     * TLV to hold the information regarding the newly bought stock
     * @param buffer contains the new stock information
     * @param symbolOffset is the offset within buffer where the new
     * stock symbol is present
     * @param numOffset is the offset within the buffer where the 
     * information regarding number of stocks is at
     * @param priceOffset is the offset within the buffer where the 
     * information regarding price at which the stock was bought is at
     * @return newly created constructed BERTLV object that is eventually
     * added to the portfolio.
     */
    private ConstructedBERTLV createNewStockTLV(byte buffer[], 
        short symbolOffset, short numOffset, short priceOffset){
        // this TLV contains the following
        // - Primitive TLV for stock symbol
        // - Primitive TLV for number of stocks we currently have
        // - Constructed TLV for last trade containing following
        //     - Primitive TLV for number of stocks
        //     - Primitive TLV for price        
        Util.arrayFillNonAtomic(scratchSpace, (short)0, 
                                (short)scratchSpace.length, (byte)0);
        short offset = stockInfoTag.toBytes(scratchSpace, (short)0);
        // length is 0 which is already set in so don't need to modify
        ConstructedBERTLV stockInfo = (ConstructedBERTLV)BERTLV.getInstance(
                scratchSpace, (short)0, (short)2);
        // create the stock symbol TLV 
        offset = symbolTag.toBytes(scratchSpace, (short)0);
        PrimitiveBERTLV symbolTLV = (PrimitiveBERTLV)BERTLV.getInstance(
                                        buffer, symbolOffset, (short)7);
        // append to the stockInfo TLV
        stockInfo.append(symbolTLV);
        
        // clean up the scratch space for number TLV
        PrimitiveBERTLV numStocksTLV = (PrimitiveBERTLV)BERTLV.getInstance(
                                        buffer, numOffset, (short)6);
        // append to the stockInfo TLV
        stockInfo.append(numStocksTLV);
        
       // create the last trade TLV
        offset = lastTradeTag.toBytes(scratchSpace, (short)0);
        scratchSpace[offset] = 0;
        ConstructedBERTLV lastTradeInfo = (ConstructedBERTLV)BERTLV.getInstance(
                scratchSpace, (short)0, (short)2);
        
        PrimitiveBERTLV LTradeNumStocksTLV = (PrimitiveBERTLV)BERTLV.getInstance(
                                        buffer, numOffset, (short)6);
        lastTradeInfo.append(LTradeNumStocksTLV);
        
        PrimitiveBERTLV priceTLV = (PrimitiveBERTLV)BERTLV.getInstance(
                                        buffer, priceOffset, (short)4);
        lastTradeInfo.append(priceTLV);
        
        // now append the last trade TLV to stockInfo TLV
        stockInfo.append(lastTradeInfo);
        
        // this completes the stockInfo
        return stockInfo;
    }
}
