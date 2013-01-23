package com.yakuc.priceboard;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import com.yakuc.priceboard.driver.IFxPriceDriver;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.Serializable;
import org.jdesktop.application.Application;
import org.jdesktop.application.LocalStorage;

/**
 * PriceBoardの設定情報
 *
 * @author yakuc
 *
 */
public class PriceBoardProperty implements Serializable {

    /**
     * SerialVersion UID
     */
    private static final long serialVersionUID = 735779586859166027L;

    public enum NetworkConnectType {

        DIRECT, // ダイレクト接続
        PROXY, // Proxy Server
        SYSTEM  // システム設定
    }
    /**
     * 価格取得ドライバオブジェクト
     */
    private IFxPriceDriver priceDriverObject = null;
    /**
     * 価格取得ドライバ名
     */
    private String priceDriverName = "com.yakuc.priceboard.driver.FXCMDriver";
    /**
     * ネットワーク接続タイプ
     */
    private NetworkConnectType networkConnectType = NetworkConnectType.DIRECT;
    /**
     * ProxyServer
     */
    private String proxyServer = null;
    /**
     * Proxy server port
     */
    private int proxyPort = 0;
    /**
     * 表示するシンボル名リスト
     */
    private String[] displaySymbols = {
        "USD/JPY", "EUR/USD", "GBP/USD", "EUR/JPY", "USD/CHF", "AUD/USD", "AUD/JPY"};
    /**
     * プロパティファイル名
     */
    private String propertyFileName = "priceboard.xml";
    /**
     * システムトレイから復帰時にデータを取得する。
     */
    private boolean isShowGetData = false;
    /**
     * データを自動更新するかどうか
     */
    private boolean isAutoUpdate = false;
    /**
     * 更新間隔
     */
    private int updateInterval = 60;
    /**
     * トレイアイコンにかざしたときに自動表示するシンボル名
     */
    private String iconDisplaySymbol = "USD/JPY";

    /**
     * コンストラクタ
     */
    public PriceBoardProperty() {
    }

    /**
     * 設定データをファイルから取得する
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws IOException
     */
    public void readPropertyData(Application app) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {
        // Propertiesオブジェクトを生成
        Properties prop = new Properties();
        String separator = System.getProperty("file.separator");

        LocalStorage ls = app.getContext().getLocalStorage();
        String fileName = ls.getDirectory() + separator + propertyFileName;
        
        // ファイルを読み込む
        try {
            prop.loadFromXML(new FileInputStream(fileName));
        } catch (IOException e) {
            // デフォルト値で
            loadDriverClass();
            // ドライバの初期化
            this.priceDriverObject.init(this.networkConnectType, this.proxyServer, this.proxyPort);
            return;
        }
        // Driver Class
        String driverClass = prop.getProperty("PriceDriverName");
        if (driverClass == null) {
            loadDriverClass();
            // ドライバの初期化
            this.priceDriverObject.init(this.networkConnectType, this.proxyServer, this.proxyPort);
            return;
        }
        this.priceDriverName  = driverClass;
        loadDriverClass();        
        // Network type
        String strNetworkConnectType = prop.getProperty("NetworkConnectType");
        if (strNetworkConnectType != null) {
            if (strNetworkConnectType.equals("0")) {
                networkConnectType = NetworkConnectType.DIRECT;
            } else if (strNetworkConnectType.equals("1")) {
                networkConnectType = NetworkConnectType.PROXY;
            } else if (strNetworkConnectType.equals("2")) {
                networkConnectType = NetworkConnectType.SYSTEM;
            }
        }
        // Proxy Server
        proxyServer = prop.getProperty("ProxyServer");
        // Proxy Port
        String proxyPortStr = prop.getProperty("ProxyPort");
        if (proxyPortStr != null) {
            proxyPort = Integer.parseInt(proxyPortStr);
        } else {
            proxyPort = 0;
        }

        // IsAutoUpdate
        String autoUpdate = prop.getProperty("AutoUpdate");
        if (autoUpdate != null) {
            if (autoUpdate.equalsIgnoreCase("true")) {
                isAutoUpdate = true;
            } else if (autoUpdate.equalsIgnoreCase("faluse")) {
                isAutoUpdate = false;
            }
        }
        // ドライバの初期化
        this.priceDriverObject.init(this.networkConnectType, this.proxyServer, this.proxyPort);

        // DisplaySymbol
        String strDisplaySymbols = prop.getProperty("DisplaySymbols");
        if (strDisplaySymbols != null && !strDisplaySymbols.equals("")) {
            displaySymbols = strDisplaySymbols.split(",");
        }

        // UpdateInterval
        String updateIntervalStr = prop.getProperty("UpdateInterval");
        if (updateIntervalStr != null && !updateIntervalStr.equals("")) {
            this.updateInterval = Integer.parseInt(updateIntervalStr);
        }
        // isShowGetData
        String isShowGetDataStr = prop.getProperty("isShowGetData");
        if (isShowGetDataStr != null && isShowGetDataStr.equalsIgnoreCase("true")) {
            this.isShowGetData = true;
        }
        // IconDisplaySymbol
        String strIconDisplaySymbol = prop.getProperty("IconDisplaySymbol");
        if (strIconDisplaySymbol != null && !strIconDisplaySymbol.equals("")) {
            this.iconDisplaySymbol = strIconDisplaySymbol;
        }
    }

    /**
     * 設定データの保存
     * 
     * @param app       Application
     * @throws IOException  IO例外
     */
    public void savePropertyData(Application app) throws IOException {
        // Propertiesオブジェクトを生成
        Properties prop = new Properties();
        String separator = System.getProperty("file.separator");
        
        // DriverClassName
        prop.setProperty("PriceDriverName", this.priceDriverName);
        // DisplaySymbols
        StringBuilder strDisplaySymbol = new StringBuilder();
        for (String sym : this.displaySymbols) {
            strDisplaySymbol.append(sym);
            strDisplaySymbol.append(",");
        }
        prop.setProperty("DisplaySymbols", strDisplaySymbol.toString());

        // Network settings
        String strNetworkConnectType = "0";
        if (this.networkConnectType == NetworkConnectType.DIRECT) {
            strNetworkConnectType = "0";
        } else if (this.networkConnectType == NetworkConnectType.PROXY) {
            strNetworkConnectType = "1";
        } else if (this.networkConnectType == NetworkConnectType.SYSTEM) {
            strNetworkConnectType = "2";
        }
        prop.setProperty("NetworkConnectType", strNetworkConnectType);

        if (this.proxyServer != null) {
            prop.setProperty("ProxyServer", this.proxyServer);
        }
        prop.setProperty("ProxyPort", Integer.toString(this.proxyPort));

        // Update interval settings
        String strAutoUpdate;
        if (this.isAutoUpdate) {
            strAutoUpdate = "true";
        } else {
            strAutoUpdate = "false";
        }
        prop.setProperty("AutoUpdate", strAutoUpdate);
        prop.setProperty("UpdateInterval", Integer.toString(this.updateInterval));

        // IconDisplaySymbol
        prop.setProperty("IconDisplaySymbol", this.iconDisplaySymbol);
        
        // 設定データを保存する
        LocalStorage ls = app.getContext().getLocalStorage();
        String fileName = ls.getDirectory().toString() + separator + propertyFileName;
        try {
            prop.storeToXML(new FileOutputStream(fileName), "Price board settings", "UTF-8");
        } catch (FileNotFoundException ex) {
            File dir = ls.getDirectory();
            dir.mkdirs();
            prop.storeToXML(new FileOutputStream(fileName), "Price board settings", "UTF-8");
        }
    }

    /**
     * ドライバクラスをロード
     *
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public void loadDriverClass() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        if (this.priceDriverName != null) {
            ClassLoader classLoader = PriceBoardProperty.class.getClassLoader();

            Class<IFxPriceDriver> pdclass = (Class<IFxPriceDriver>) classLoader.loadClass(this.priceDriverName);
            this.priceDriverObject = pdclass.newInstance();
        }
    }

    public IFxPriceDriver getPriceDriverObj() {
        return this.priceDriverObject;
    }

    public void setPriceDriverObj(IFxPriceDriver priceDriver) {
        this.priceDriverObject = priceDriver;
    }

    public String getProxyServer() {
        return proxyServer;
    }

    public void setProxyServer(String proxyServer) {
        this.proxyServer = proxyServer;
    }

    public int getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
    }

    public String[] getDisplaySymbols() {
        return displaySymbols;
    }

    public void setDisplaySymbols(String[] displaySymbols) {
        this.displaySymbols = displaySymbols;
    }

    public String getPropertyFileName() {
        return propertyFileName;
    }

    public void setPropertyFileName(String propertyFileName) {
        this.propertyFileName = propertyFileName;
    }

    public int getUpdateInterval() {
        return this.updateInterval;
    }

    public void setUpdateInterval(int updateInterval) {
        this.updateInterval = updateInterval;
    }

    public NetworkConnectType getNetworkConnectType() {
        return networkConnectType;
    }

    public void setNetworkConnectType(NetworkConnectType networkConnectType) {
        this.networkConnectType = networkConnectType;
    }

    public String getPriceDriverName() {
        return priceDriverName;
    }

    public void setPriceDriverName(String priceDriverName) {
        this.priceDriverName = priceDriverName;
    }

    public boolean isAutoUpdate() {
        return isAutoUpdate;
    }

    public void setIsAutoUpdate(boolean isAutoUpdate) {
        this.isAutoUpdate = isAutoUpdate;
    }

    public String getIconDisplaySymbol() {
        return iconDisplaySymbol;
    }

    public void setIconDisplaySymbol(String iconDisplaySymbol) {
        this.iconDisplaySymbol = iconDisplaySymbol;
    }
}
