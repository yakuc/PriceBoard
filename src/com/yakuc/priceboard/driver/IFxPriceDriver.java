/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yakuc.priceboard.driver;

import com.yakuc.priceboard.FxPriceDataItem;
import com.yakuc.priceboard.PriceBoardException;
import com.yakuc.priceboard.PriceBoardProperty;

import java.util.List;

/**
 *　価格情報を取得するドライバインターフェィス
 *
 * @author yakuc
 */
public interface IFxPriceDriver {

    /**
     * 価格データの取得
     *
     * @param getSymbolList	現在の価格データ
     * @return
     */
    public List<FxPriceDataItem> getData(String[] getSymbolIDList) throws PriceBoardException;

    /**
     * 取得可能なシンボルリストの取得
     * 
     * @return 取得可能なシンボルリスト
     */
    public List<DriverSymbolInfo> getAllSymbolList();

    /**
     * ドライバ名称の取得
     *
     * @return  ドライバ名称
     */
    public String getName();

    /**
     * ドライバの初期化
     */
      public void init(PriceBoardProperty.NetworkConnectType networkConnectType,
            String proxyServer, int proxyPort);


   /**
     * 桁の名称を取得
     * @param columnIndex
     * @return
     */
    public String getColumnName(int columnIndex);

    /**
     * プライスボードの桁の数を取得
     *
     * @return　ボードに表示する桁の数
     */
    public int getColumnCount();

    /**
     * 価格データを取得
     *
     * @param rowIndex
     * @param columnIndex
     * @return
     */
    public Object getValueAt(int rowIndex, int columnIndex);

    /**
     * データの行数を取得
     * @return データの行数
     */
    public int getRowCount();
}
