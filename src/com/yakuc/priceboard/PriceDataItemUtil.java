package com.yakuc.priceboard;

import java.util.List;

public class PriceDataItemUtil {

	/**
	 * DriverSymbolInfo 内を、code をキーに検索する。
	 * @param infoList	検索するDriverSymbolInfoのList
	 * @param code	検索キー
	 * @return	見つかったDriverSymbolInfo
	 */
	public static String searchDriverSymbolInfoList(String [] infoList, String code) {
		for(String item : infoList) {
			if (item.equalsIgnoreCase(code)) {
				return item;
			}
		}
		return null;
	}

	/**
	 * List<FxPriceDataItem> のコンソールへの表示(デバッグ用）
	 * 
	 * @param pricelist	
	 */
	public static void printPriceDataItemList(List<FxPriceDataItem> pricelist) {
		for(FxPriceDataItem item : pricelist) {
			System.out.println("symbol:" + item.getSymbol());
			System.out.println("date:" + item.getTime());
			System.out.println("bid:" + item.getBid());
			System.out.println("ask:" + item.getAsk());
		}

	}
}
