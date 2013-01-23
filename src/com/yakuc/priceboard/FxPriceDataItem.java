/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.yakuc.priceboard;

/**
 * FXの価格情報のアイテム
 * 
 * @author yakuc
 */
public class FxPriceDataItem {
    /**
     * シンボル情報
     **/
    private String symbol = null;
    /**
     * Bid
     **/
    private String bid = null;
    /**
     * Ask
     **/
    private String ask = null;
    /**
     * High
     */
    private String high = null;
    /**
     *Low
     */
    private String low = null;
    /**
     *　時刻
     */
    private String time = null;

    public String getAsk() {
        return ask;
    }

    public void setAsk(String ask) {
        this.ask = ask;
    }

    public String getBid() {
        return bid;
    }

    public void setBid(String bid) {
        this.bid = bid;
    }

    public String getHigh() {
        return high;
    }

    public void setHigh(String high) {
        this.high = high;
    }

    public String getLow() {
        return low;
    }

    public void setLow(String low) {
        this.low = low;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

}
