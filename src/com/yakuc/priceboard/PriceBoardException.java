package com.yakuc.priceboard;

/**
 * priceboardの独自例外クラス
 * 
 * @author yakuc
 *
 */
public class PriceBoardException extends  Exception {

	/**
	 * SerialVersion
	 */
	private static final long serialVersionUID = -2618692044945121552L;
	/**
	 * コンストラクタ（原因の例外オブジェクトを指定）
	 * @param e 原因の例外オブジェクト
	 */
	public PriceBoardException(Exception e) {
		super(e);
	}
	/**
	 * コンストラクタ
	 */
	public PriceBoardException() {
		super();
	}
	/**
	 * メッセージ付きコンストラクタ
	 * @param message
	 */
	public PriceBoardException(String message) {
		super(message);
	}
}
