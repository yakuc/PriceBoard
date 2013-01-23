package com.yakuc.priceboard;

import javax.swing.table.AbstractTableModel;

import com.yakuc.priceboard.driver.DriverSymbolInfo;
import com.yakuc.priceboard.driver.IFxPriceDriver;
import java.util.ArrayList;
import java.util.List;

/**
 * 設定ダイアログ内の表示シンボルリストテーブルのテーブルモデルクラス
 *
 * @author yakuc
 */
public class SymbolListSelectTableModel extends AbstractTableModel {

    /**
     * 編集中のシンボル情報
     */
    private List<DriverSymbolInfo> driverSymbolInfoList;
    /**
     * カラム名
     */
    private String[] columnNames = {"表示", "シンボル名"};

    /**
     * コンストラクタ
     * 
     */
    public SymbolListSelectTableModel() {
        super();
    }

    /**
     * 選択表示用のシンボルリストを生成
     *
     * @param driver
     */
    public void setListData(List<DriverSymbolInfo> allList, String[] displayList) {
        driverSymbolInfoList = new ArrayList<DriverSymbolInfo>();

        for (DriverSymbolInfo info : allList) {
            DriverSymbolInfo symbolInfo = new DriverSymbolInfo();
            symbolInfo.setCode(info.getCode());
            symbolInfo.setSymbolLabel(info.getSymbolLabel());
            symbolInfo.setDescription(info.getDescription());
            symbolInfo.setShow(false);
            if (displayList != null) {
                for (String displaySymbol : displayList) {
                    if (displaySymbol.equalsIgnoreCase(symbolInfo.getCode())) {
                        symbolInfo.setShow(true);
                    }
                }
            } else {
                symbolInfo.setShow(true);
            }
            this.driverSymbolInfoList.add(symbolInfo);
        }
    }

    /**
     * 選択された表示シンボルの配列を取得する
     */
    public String[] getDisplaySymbolArray() {
        ArrayList<String> selectSymbolList = new ArrayList<String>();

        for (DriverSymbolInfo info : driverSymbolInfoList) {
            if (info.isShow()) {
                selectSymbolList.add(info.getCode());
            }
        }
        return selectSymbolList.toArray(new String[0]);
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public int getRowCount() {
        return this.driverSymbolInfoList.size();
    }

    @Override
    public Object getValueAt(int row, int col) {
        DriverSymbolInfo info = this.driverSymbolInfoList.get(row);

        if (col == 0) {
            return new Boolean(info.isShow());
        } else if (col == 1) {
            return info.getSymbolLabel();
        }
        return null;
    }

    @Override
    public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }

    /*
     * 編集可能なセルの情報を取得
     */
    @Override
    public boolean isCellEditable(int row, int col) {
        if (col < 1) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        if (col == 0) {
            if (value instanceof Boolean) {
                Boolean bval = (Boolean) value;
                DriverSymbolInfo info = this.driverSymbolInfoList.get(row);
                info.setShow(bval);
                fireTableCellUpdated(row, col);
            }
        }
    }

    @Override
    public String getColumnName(int columnIndex) {
        return columnNames[columnIndex];
    }
}
