package com.yakuc.priceboard.driver;

import com.yakuc.priceboard.PriceBoardProperty.NetworkConnectType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;



import com.yakuc.priceboard.FxPriceDataItem;
import com.yakuc.priceboard.PriceBoardApp;
import com.yakuc.priceboard.PriceBoardException;
import com.yakuc.priceboard.PriceBoardProperty;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

/**
 * FXCMのサイトからデータを取得するドライバ
 *
 * @author yakuc
 *
 */
public class FXCMDriver implements IFxPriceDriver {

    // データ取得サーバ
    protected String requestUrl = null;
    // 取得可能なシンボル一覧
    protected List<DriverSymbolInfo> symbolInfo = new ArrayList<DriverSymbolInfo>();
    // 表示桁
    protected  String[] colLabel;
    // 最近取得したデータ
    protected List<FxPriceDataItem> priceDataList;
    // リソースファイル名
    private static String resourceFileName = "com.yakuc.priceboard.driver.fxcmdriver";
    
    public FXCMDriver() {
    }

    @Override
    public void init(PriceBoardProperty.NetworkConnectType networkConnectType,
            String proxyServer, int proxyPort) {
        readResourceData(resourceFileName);
        initProxyServer(networkConnectType, proxyServer, proxyPort);
    }

    /**
     * リソースデータの読み込み
     */
    protected void readResourceData(String resourceName) {
        // リソースファイルを読み込む
        ResourceBundle rb = ResourceBundle.getBundle(resourceName);

        // requestUrl
        this.requestUrl = rb.getString("requesturl");

        // 取得可能なシンボルリストの作成
        String symbolsStr = rb.getString("symbols");
        String symbolCodeStr = rb.getString("symbolCodes");
        String[] symbolsArray = symbolsStr.split(",");
        String[] symbolCodesArray = symbolCodeStr.split(",");
        symbolInfo.clear();
        for (int i = 0; i < symbolCodesArray.length; i++) {
            DriverSymbolInfo info = new DriverSymbolInfo();
            info.setCode(symbolCodesArray[i]);
            info.setSymbolLabel(symbolsArray[i]);
            symbolInfo.add(info);
        }

        // ColLavel
        String colLabelStr = rb.getString("colLabel");
        this.colLabel = colLabelStr.split(",");
    }

    /**
     * プロキシサーバの初期化
     */
    protected void initProxyServer(
            PriceBoardProperty.NetworkConnectType networkConnectType,
            String proxyServer, int proxyPort) {
        // Set Proxy server
        if (networkConnectType == PriceBoardProperty.NetworkConnectType.PROXY) {
            if (proxyServer != null) {
                System.setProperty("http.proxyHost", proxyServer);
            }
            if (proxyPort != 0) {
                System.setProperty("http.proxyPort", Integer.toString(proxyPort));
            }
        }
    }

    /**
     * ドライバ名称の取得
     * 
     * @return  ドライバ名称
     */
    @Override
    public String getName() {
        return "FXCM";
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
            // URL接続
            URL urlObj = new URL(requestUrl);
            HttpURLConnection urlCon = (HttpURLConnection) urlObj.openConnection();
            urlCon.setUseCaches(false);
            urlCon.setRequestMethod("GET");
            FXCMDriverSax sax = new FXCMDriverSax();
            InputStream stream = urlCon.getInputStream();
            this.priceDataList = sax.parse(stream, getSymbolList);
            stream.close();
            return this.priceDataList;
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(FXCMDriver.class.getName()).log(Level.SEVERE, null, ex);
            throw new PriceBoardException(ex);
        } catch (SAXException ex) {
            Logger.getLogger(FXCMDriver.class.getName()).log(Level.SEVERE, null, ex);
            throw new PriceBoardException(ex);
        } catch (IOException ex) {
            Logger.getLogger(FXCMDriver.class.getName()).log(Level.SEVERE, null, ex);
            throw new PriceBoardException(ex);
        }
    }

    /**
     * 取得可能なシンボルリストの取得
     * 
     * @return 取得可能なシンボルリスト
     */
    @Override
    public List<DriverSymbolInfo> getAllSymbolList() {
        return this.symbolInfo;
    }

    /**
     * 桁の名称を取得
     * @param columnIndex
     * @return
     */
    @Override
    public String getColumnName(int columnIndex) {
        return this.colLabel[columnIndex];
    }

    /**
     * プライスボードの桁の数を取得
     *
     * @return　ボードに表示する桁の数
     */
    @Override
    public int getColumnCount() {
        return this.colLabel.length;
    }

    @Override
    public int getRowCount() {
        if (this.priceDataList != null) {
            return this.priceDataList.size();
        } else {
            return 0;
        }
    }

    /**
     * テーブルのセルのデータを取得
     */
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (this.priceDataList == null) {
            return 0;
        }
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
            default:
                return null;
        }
    }

    public String[] getColLabel() {
        return colLabel;
    }

    public void setColLabel(String[] colLabel) {
        this.colLabel = colLabel;
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    public List<DriverSymbolInfo> getSymbolInfo() {
        return symbolInfo;
    }

    public void setSymbolInfo(List<DriverSymbolInfo> symbolInfo) {
        this.symbolInfo = symbolInfo;
    }
}
