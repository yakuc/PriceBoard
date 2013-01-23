/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yakuc.priceboard.driver;

/**
 * ドライバのシンボル情報
 * @author yakuc
 */
public class DriverSymbolInfo {

    /**
     * シンボルコード
     */
    private String code;
    /**
     * シンボルの表示名
     */
    private String symbolLabel;
    /**
     * 説明
     */
    private String description;
    /**
     * 表示するかどうか
     */
    private boolean isShow = true;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getSymbolLabel() {
        return symbolLabel;
    }

    public void setSymbolLabel(String symbolLabel) {
        this.symbolLabel = symbolLabel;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isShow() {
        return isShow;
    }

    public void setShow(boolean isShow) {
        this.isShow = isShow;
    }
}
