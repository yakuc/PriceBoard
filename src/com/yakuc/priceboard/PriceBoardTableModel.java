package com.yakuc.priceboard;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;

import com.yakuc.priceboard.driver.IFxPriceDriver;

/**
 * PriceBoardTableModel
 *
 * Price Boardを表示しているテーブルのテーブルモデルクラス
 */
public class PriceBoardTableModel extends AbstractTableModel implements
        Serializable {

    /**
     * SerialVersion UID
     */
    private static final long serialVersionUID = 735379586359166027L;
    /**
     * データ取得日時
     */
    private String timeStamp;

    /**
     * 価格を取得するドライバ
     *
     */
    private IFxPriceDriver driver;

    public PriceBoardTableModel(IFxPriceDriver drv) {
        super();
        this.driver = drv;
    }
    
   @Override
    /**
     * 行数の取得
     */
    public int getRowCount() {
        return this.driver.getRowCount();
   }

    @Override
    /**
     * テーブルのセルのデータを取得
     */
    public Object getValueAt(int rowIndex, int columnIndex) {
        return this.driver.getValueAt(rowIndex, columnIndex);
    }

    /**
     * データーの日時を取得
     *
     * @return データの日時
     */
    public String getTimeStamp() {
        return this.timeStamp;
    }

    @Override
    public int getColumnCount() {
        return this.driver.getColumnCount();
    }

    @Override
    public String getColumnName(int columnIndex) {
        return this.driver.getColumnName(columnIndex);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    /**
     * 価格データの取得
     * @throws PriceBoardException
     */
    public void getPirceData() throws Exception {
        try {
            PriceBoardProperty prop = PriceBoardApp.getApplication().getPriceBoardProperty();
            this.driver.getData(prop.getDisplaySymbols());
            GregorianCalendar cal = new GregorianCalendar();
            SimpleDateFormat fmt = new SimpleDateFormat(
                    "yyyy/MM/dd HH:mm:ss z");
            this.timeStamp = fmt.format(cal.getTime());
        } catch (PriceBoardException e) {
            JOptionPane.showMessageDialog(null,
                    e.getMessage(),
                    "PriceBoard Error",
                    JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    e.getMessage(),
                    "PriceBoard Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
