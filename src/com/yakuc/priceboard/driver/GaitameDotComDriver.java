/*
 * 外為どっとコムからデータを取得するドライバ
 * 
 */
package com.yakuc.priceboard.driver;

import com.yakuc.priceboard.FxPriceDataItem;
import com.yakuc.priceboard.PriceBoardException;
import com.yakuc.priceboard.PriceBoardProperty;
import com.yakuc.priceboard.PriceDataItemUtil;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 外為どっとコムからデータを取得するドライバ
 *
 * @author yakuc
 */
public class GaitameDotComDriver extends FXCMDriver {

    /**
     * リソースファイル名
     */
    private static String resourceFileName = "com.yakuc.priceboard.driver.gaitameDotComDriver";

    /**
     * コンストラクタ
     */
    public GaitameDotComDriver() {
        super();
    }

    @Override
    public void init(PriceBoardProperty.NetworkConnectType networkConnectType, String proxyServer, int proxyPort) {
        readResourceData(resourceFileName);
        initProxyServer(networkConnectType, proxyServer, proxyPort);
    }

    /**
     * ドライバ名称の取得
     *
     * @return  ドライバ名称
     */
    @Override
    public String getName() {
        return "外為どっとコム";
    }

    /**
     * 現在の価格データの取得
     *
     * @param getSymbolList		データを取得するシンボルリスト
     * @throws PriceBoardException	例外発生。
     */
    @Override
    public List<FxPriceDataItem> getData(String[] getSymbolList) throws PriceBoardException {
        try {
            this.priceDataList = new ArrayList<FxPriceDataItem>();
            // URL接続
            URL urlObj = new URL(requestUrl);
            HttpURLConnection urlCon = (HttpURLConnection) urlObj.openConnection();
            urlCon.setUseCaches(false);
            urlCon.setRequestMethod("GET");
            BufferedReader reader = new BufferedReader(new InputStreamReader(urlCon.getInputStream(), "Shift_JIS"));

            String readstr = reader.readLine();
            StringBuilder buffer = new StringBuilder();
            while (null != readstr) {
                buffer.append(readstr);
                readstr = reader.readLine();
            }
            return createPriceList(buffer.toString().split("_"), getSymbolList);
        } catch (IOException ex) {
            Logger.getLogger(FXCMDriver.class.getName()).log(Level.SEVERE, null, ex);
            throw new PriceBoardException(ex);
        }
    }

    /**
     * 価格リストの作成
     *
     * @param strPriceList 取得したデータの文字列リスト
     * @param enableSymbolInfo データを表示するシンボルリスト
     */
    public List<FxPriceDataItem> createPriceList(String [] strPriceList, String[] enableSymbolInfo) {
        if (strPriceList == null) {
            return null;
        }
        
        FxPriceDataItem item;
         this.priceDataList.clear();
         for(int i = 1; i < strPriceList.length; i++) {
             if (PriceDataItemUtil.searchDriverSymbolInfoList(enableSymbolInfo, strPriceList[i]) != null) {
                item = new FxPriceDataItem();
                // シンボル名
                item.setSymbol(strPriceList[i]);
                // Bid/Ask
                String [] bidask = strPriceList[i+1].split("-");
                item.setBid(bidask[0].trim());
                item.setAsk(bidask[1].trim());
                // High
                item.setHigh(strPriceList[i+4]);
                // Low
                item.setLow(strPriceList[i+5]);
                // 時刻
                item.setTime(strPriceList[i+6]);
                this.priceDataList.add(item);
                i += 8;
             }
        }
         return this.priceDataList;
    }

    /**
     * テーブルのセルのデータを取得
     */
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex > this.priceDataList.size()) {
            return null;
        }
        FxPriceDataItem item = this.priceDataList.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return item.getSymbol();
            case 1:
                return item.getBid();
            case 2:
                return item.getAsk();
            case 3:
                return item.getHigh();
            case 4:
                return item.getLow();
            default:
                return null;
        }
    }
}
