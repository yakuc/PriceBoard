package com.yakuc.priceboard;

import java.awt.TrayIcon;
import javax.swing.JLabel;

import org.jdesktop.application.Application;
import org.jdesktop.application.Task;

/**
 * 価格情報を取得するスレッドオブジェクト
 *
 * @author yaku
 */
public class GetPriceDataTask extends Task<Void, Void> {

    /**
     * テーブルモデル
     */
    private PriceBoardTableModel tableModel;
    /**
     * データ取得日時表示ラベル
     */
    private JLabel dateTimeLabel;

    private TrayIcon trayIcon;
    
    /**
     * コンストラクタ
     *
     * @param app   アプリケーションオブジェクト
     * @param tm    PriceBoardTableModel
     * @param dtl   更新日時を表示するラベルオブジェクト
     * @param trayIcon  SystemTray Icon Object
     */
    public GetPriceDataTask(Application app, PriceBoardTableModel tm, JLabel dtl, TrayIcon trayIcon) {
        super(app);
        this.tableModel = tm;
        this.dateTimeLabel = dtl;
        this.trayIcon = trayIcon;
    }

    /**
     * 時間がかかる処理の実行。
     * ここでは、価格データを取得する処理を記述。
     */
    @Override
    protected Void doInBackground() throws Exception {
        this.tableModel.getPirceData();
        return null;
    }

    /**
     * 時間のかかる処理が終了したときに呼ばれる、GUIを更新するメソッド。
     * 
     */
    @Override
    protected void finished() {
        PriceBoardProperty prop = PriceBoardApp.getApplication().getPriceBoardProperty();
        
        this.dateTimeLabel.setText(tableModel.getTimeStamp());
        this.tableModel.fireTableDataChanged();

        // システムトレイメッセージの更新
        if (this.trayIcon != null && prop.getIconDisplaySymbol() != null) {
            for (int i = 0; i < this.tableModel.getRowCount(); i++) {
                String symbol = (String) this.tableModel.getValueAt(i, 0);
                if (symbol.equals(prop.getIconDisplaySymbol())) {
                    String toolTip = "Price Board - " + symbol + " \n"
                            + (String) this.tableModel.getValueAt(i, 1) + " - " + (String) this.tableModel.getValueAt(i, 2)
                            + " [" + tableModel.getTimeStamp().substring(11,16) + "]";
                    this.trayIcon.setToolTip(toolTip);
                }
            }
        }
    }
}
