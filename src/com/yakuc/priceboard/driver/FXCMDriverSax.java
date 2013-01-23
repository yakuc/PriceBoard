package com.yakuc.priceboard.driver;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.yakuc.priceboard.FxPriceDataItem;
import com.yakuc.priceboard.PriceDataItemUtil;

/**
 * FXCMの価格情報データのSAX用XMLの解析オブジェクト
 *
 * @author yakuc
 *
 */
public class FXCMDriverSax extends DefaultHandler {

    //
    private List<FxPriceDataItem> priceDataItemList = null;
    private FxPriceDataItem currentItem = null;

    // 現在のXML上の解析位置
    enum ParseStatus {

        NONE,
        CPAIR,
        NAME,
        JPNAME,
        DATETIME,
        RATEBID,
        RATEASK,
        SWAPBID,
        SWAPASK,
        SPREAD,
        END
    }
    private ParseStatus parseStatus = ParseStatus.NONE;
    // 取得するシンボル情報Array
    private String[] enableSymbolInfo;

    public List<FxPriceDataItem> parse(InputStream stream, String[] symbolInfo) throws ParserConfigurationException, SAXException, IOException {
        enableSymbolInfo = symbolInfo;
        // SAXパーサーファクトリを生成
        SAXParserFactory spfactory = SAXParserFactory.newInstance();
        // SAXパーサーを生成
        SAXParser parser = spfactory.newSAXParser();
        // XMLファイルを指定されたデフォルトハンドラーで処理します
        parser.parse(stream, this);

        return priceDataItemList;
    }

    /**
     * ドキュメント開始時
     */
    public void startDocument() {
        priceDataItemList = new ArrayList<FxPriceDataItem>();
        parseStatus = ParseStatus.NONE;
        //System.out.println("start document");
    }

    /**
     * 要素の開始タグ読み込み時
     */
    public void startElement(String uri,
            String localName,
            String qName,
            Attributes attributes) {

//      System.out.println("要素開始:" + qName);

        if (qName.equalsIgnoreCase("CPair")) {
            parseStatus = ParseStatus.CPAIR;
            currentItem = new FxPriceDataItem();
        } else if (qName.equalsIgnoreCase("name")) {
            parseStatus = ParseStatus.NAME;
        } else if (qName.equalsIgnoreCase("DateTime")) {
            parseStatus = ParseStatus.DATETIME;
        } else if (qName.equalsIgnoreCase("RateBid")) {
            parseStatus = ParseStatus.RATEBID;
        } else if (qName.equalsIgnoreCase("RateAsk")) {
            parseStatus = ParseStatus.RATEASK;
        }
    }

    /**
     * テキストデータ読み込み時
     */
    public void characters(char[] ch,
            int offset,
            int length) {
        String dataStr = new String(ch, offset, length);
        //  	System.out.println("テキストデータ：" + dataStr);

        switch (parseStatus) {
            case NAME:
                currentItem.setSymbol(dataStr);
                break;
            case DATETIME:
                currentItem.setTime(dataStr);
                break;
            case RATEBID:
                currentItem.setBid(dataStr);
                break;
            case RATEASK:
                currentItem.setAsk(dataStr);
                break;
            default:
                break;
        }
    }

    /**
     * 要素の終了タグ読み込み時
     */
    public void endElement(String uri,
            String localName,
            String qName) {
        if (qName.equalsIgnoreCase("CPair")) {
            if (enableSymbolInfo != null) {
                if (PriceDataItemUtil.searchDriverSymbolInfoList(enableSymbolInfo, currentItem.getSymbol()) != null) {
                    priceDataItemList.add(currentItem);
                }
            } else {
                priceDataItemList.add(currentItem);
            }
        }
        parseStatus = ParseStatus.END;
//      System.out.println("要素終了:" + qName);
    }

    /**
     * ドキュメント終了時
     */
    public void endDocument() {
//      System.out.println("ドキュメント終了");
    }
}
