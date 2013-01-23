/*
 * PriceBoardView.java
 */
package com.yakuc.priceboard;

import java.awt.AWTException;
import java.awt.Component;
import java.awt.Font;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.application.Action;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.Task;
import org.jdesktop.application.TaskMonitor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.Timer;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

/**
 * The application's main frame.
 */
public class PriceBoardView extends FrameView {

    /**
     * コンストラクタ
     *
     * @param app
     */
    public PriceBoardView(SingleFrameApplication app) {
        super(app);
        PriceBoardProperty prop = PriceBoardApp.getApplication().getPriceBoardProperty();

        try {
            // 設定データの読み込み
            readPropertyData();

            // Table Model の生成
            priceBoardTableModel = new PriceBoardTableModel(prop.getPriceDriverObj());

            // コンポーネントの生成
            initComponents();

            // MainFrameのアイコンを設定
            URL url = PriceBoardView.class.getResource("/com/yakuc/priceboard/resources/priceboard.png");
            Image iconImage = ImageIO.read(url);
            getFrame().setIconImage(iconImage);

            // データを取得
            Task task = updateDataAction();
            task.execute();

            // システムトレイを設定
            setSystemTray();
        } catch (AWTException ex) {
            JOptionPane.showMessageDialog(null,
                    ex.getMessage(),
                    "Error",
                    JOptionPane.WARNING_MESSAGE);
            Logger.getLogger(PriceBoardView.class.getName()).log(Level.WARNING, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(PriceBoardView.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null,
                    ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            getApplication().exit();
        }

        // タイマーの追加
        dataUpdateTimer = new Timer(prop.getUpdateInterval() * 1000, new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                // データを取得
                Task task = updateDataAction();
                task.execute();
            }
        });
        if (prop.isAutoUpdate()) {
            dataUpdateTimer.start();
        }

        // status bar initialization - message timeout, idle icon and busy animation, etc
        ResourceMap resourceMap = getResourceMap();
        int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
        messageTimer = new Timer(messageTimeout, new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                statusMessageLabel.setText("");
            }
        });
        messageTimer.setRepeats(false);
        int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
        for (int i = 0; i < busyIcons.length; i++) {
            busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
        }
        busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
                statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
            }
        });
        idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
        statusAnimationLabel.setIcon(idleIcon);
        progressBar.setVisible(false);

        // connecting action tasks to status bar via TaskMonitor
        TaskMonitor taskMonitor = new TaskMonitor(getApplication().getContext());
        taskMonitor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {

            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                if ("started".equals(propertyName)) {
                    if (!busyIconTimer.isRunning()) {
                        statusAnimationLabel.setIcon(busyIcons[0]);
                        busyIconIndex = 0;
                        busyIconTimer.start();
                    }
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(true);
                } else if ("done".equals(propertyName)) {
                    busyIconTimer.stop();
                    statusAnimationLabel.setIcon(idleIcon);
                    progressBar.setVisible(false);
                    progressBar.setValue(0);
                } else if ("message".equals(propertyName)) {
                    String text = (String) (evt.getNewValue());
                    statusMessageLabel.setText((text == null) ? "" : text);
                    messageTimer.restart();
                } else if ("progress".equals(propertyName)) {
                    int value = (Integer) (evt.getNewValue());
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(value);
                }
            }
        });
    }

    /**
     * 設定データの読み込み
     */
    public final void readPropertyData() {
        PriceBoardProperty prop = PriceBoardApp.getApplication().getPriceBoardProperty();
        try {
            prop.readPropertyData(PriceBoardApp.getApplication());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                    "IO例外が発生しました.\n" + e.getLocalizedMessage(),
                    "PriceBoard Error",
                    JOptionPane.ERROR_MESSAGE);
            getApplication().exit();
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(null,
                    "ドライバクラス名 " + e.getMessage() + " が見つかりません。",
                    "PriceBoard Error",
                    JOptionPane.ERROR_MESSAGE);
            getApplication().exit();
        } catch (InstantiationException e) {
            JOptionPane.showMessageDialog(null,
                    e.getLocalizedMessage(),
                    "PriceBoard Error",
                    JOptionPane.ERROR_MESSAGE);
            getApplication().exit();
        } catch (IllegalAccessException e) {
            JOptionPane.showMessageDialog(null,
                    e.getLocalizedMessage(),
                    "PriceBoard Error",
                    JOptionPane.ERROR_MESSAGE);
            getApplication().exit();
        }
    }

    @Action
    public void showAboutBox() {
        if (aboutBox == null) {
            JFrame mainFrame = PriceBoardApp.getApplication().getMainFrame();
            aboutBox = new PriceBoardAboutBox(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        PriceBoardApp.getApplication().show(aboutBox);
    }

    /**
     * システムトレイアイコンの設定
     *
     * @throws AWTException
     * @throws IOException
     */
    private boolean setSystemTray() throws AWTException, IOException {
        ResourceMap resourceMap = getApplication().getContext().getResourceMap(PriceBoardView.class);

        // システム・トレイがサポートされていなければ終了.
        if (!SystemTray.isSupported()) {
            JOptionPane.showMessageDialog(null,
                    resourceMap.getString("systemTrayNotUseMessage"),
                    "Price Board",
                    JOptionPane.WARNING_MESSAGE);
            return (false);
        }

        // SystemTray インスタンスは static メソッドで取得する.
        SystemTray systemTray = SystemTray.getSystemTray();

        URL url = PriceBoardView.class.getResource("/com/yakuc/priceboard/resources/priceboard.png");
        Image trayIconImage = ImageIO.read(url);

        // トレイ・アイコンに持たせるポップアップメニューの作成.
        this.trayIconMenu = new PopupMenu();
        MenuItem menuItem = new MenuItem(resourceMap.getString("trayMenu.exitItem.text"));
        menuItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                getApplication().exit(null);
            }
        });
        trayIconMenu.add(menuItem);
        showMenuItem = new MenuItem(resourceMap.getString("trayMenu.showItem.text"));
        showMenuItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                JFrame mainFrame = PriceBoardApp.getApplication().getMainFrame();
                mainFrame.setVisible(true);
                mainFrame.setExtendedState(JFrame.NORMAL);
                showMenuItem.setEnabled(false);
            }
        });
        trayIconMenu.add(showMenuItem);

        // データ更新メニュー項目
        MenuItem dataUpdatemenuItem = new MenuItem(resourceMap.getString("trayMenu.dataUpdateItem.text"));
        dataUpdatemenuItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                // データを取得
                Task task = updateDataAction();
                task.execute();
            }
        });
        trayIconMenu.add(dataUpdatemenuItem);

        // トレイ・アイコンを作成し, システム・トレイに追加する.
        trayIcon = new TrayIcon(trayIconImage, "Price Board", trayIconMenu);
        trayIcon.setToolTip(resourceMap.getString("systemTray.text"));
        trayIcon.setImageAutoSize(true);
        // マウス・イベント
        trayIcon.addMouseListener(new MouseAdapter() {

            public void mousePressed(MouseEvent event) {
                String osName = System.getProperty("os.name");
                if (osName.equals("Mac OS X")) {
                    // Macはこの機能を無効（デフォルトで表示されるメニューがマウスボタン１のため）
                    return;
                }
                if (event.getButton() == MouseEvent.BUTTON1) {
                    // メインウインドウを表示/非表示
                    JFrame mainFrame = PriceBoardApp.getApplication().getMainFrame();
                    if (mainFrame.isVisible()) {
                        mainFrame.setVisible(false);
                        showMenuItem.setEnabled(true);
                        mainFrame.setExtendedState(JFrame.ICONIFIED);
                    } else {
                        mainFrame.setVisible(true);
                        mainFrame.setExtendedState(JFrame.NORMAL);
                        showMenuItem.setEnabled(false);
                    }
                }
            }
        });


        systemTray.add(trayIcon);   // 失敗した場合は AWTException

        // ウインドウリスナを追加
        getFrame().addWindowListener(new WindowListener() {

            @Override
            public void windowActivated(WindowEvent arg0) {
                showMenuItem.setEnabled(false);
            }

            @Override
            public void windowClosed(WindowEvent arg0) {
            }

            @Override
            public void windowClosing(WindowEvent arg0) {
                // TODO Auto-generated method stub
            }

            @Override
            public void windowDeactivated(WindowEvent arg0) {
                // TODO Auto-generated method stub
            }

            @Override
            public void windowDeiconified(WindowEvent arg0) {
                JFrame mainFrame = PriceBoardApp.getApplication().getMainFrame();
                mainFrame.setVisible(true);
                showMenuItem.setEnabled(false);
                // TODO Auto-generated method stub
            }

            @Override
            public void windowIconified(WindowEvent arg0) {
                JFrame mainFrame = PriceBoardApp.getApplication().getMainFrame();
                mainFrame.setVisible(false);
                showMenuItem.setEnabled(true);
            }

            @Override
            public void windowOpened(WindowEvent arg0) {
            }
        });


        return true;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        priceBoardTable = new javax.swing.JTable();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        jMenu1 = new javax.swing.JMenu();
        updateMenuItem = new javax.swing.JMenuItem();
        settingMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        statusPanel = new javax.swing.JPanel();
        javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
        statusMessageLabel = new javax.swing.JLabel();
        statusAnimationLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();

        mainPanel.setName("mainPanel"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        priceBoardTable.setModel(priceBoardTableModel);
        priceBoardTable.setName("priceBoardTable"); // NOI18N
        priceBoardTable.setRowHeight(20);
        priceBoardTable.setRowMargin(2);
        setPriceBoardTableProp();
        jScrollPane1.setViewportView(priceBoardTable);

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 338, Short.MAX_VALUE)
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 245, Short.MAX_VALUE)
        );

        menuBar.setName("menuBar"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(com.yakuc.priceboard.PriceBoardApp.class).getContext().getResourceMap(PriceBoardView.class);
        fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
        fileMenu.setName("fileMenu"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(com.yakuc.priceboard.PriceBoardApp.class).getContext().getActionMap(PriceBoardView.class, this);
        exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
        exitMenuItem.setName("exitMenuItem"); // NOI18N
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        jMenu1.setText(resourceMap.getString("jMenu1.text")); // NOI18N
        jMenu1.setName("jMenu1"); // NOI18N

        updateMenuItem.setAction(actionMap.get("updateDataAction")); // NOI18N
        updateMenuItem.setText(resourceMap.getString("updateMenuItem.text")); // NOI18N
        updateMenuItem.setName("updateMenuItem"); // NOI18N
        jMenu1.add(updateMenuItem);

        settingMenuItem.setAction(actionMap.get("showSettingDialog")); // NOI18N
        settingMenuItem.setText(resourceMap.getString("settingMenuItem.text")); // NOI18N
        settingMenuItem.setName("settingMenuItem"); // NOI18N
        jMenu1.add(settingMenuItem);

        menuBar.add(jMenu1);

        helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
        helpMenu.setName("helpMenu"); // NOI18N

        aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
        aboutMenuItem.setName("aboutMenuItem"); // NOI18N
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        statusPanel.setName("statusPanel"); // NOI18N

        statusPanelSeparator.setName("statusPanelSeparator"); // NOI18N

        statusMessageLabel.setName("statusMessageLabel"); // NOI18N

        statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N

        progressBar.setName("progressBar"); // NOI18N

        javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statusMessageLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 163, Short.MAX_VALUE)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statusAnimationLabel)
                .addContainerGap())
            .addComponent(statusPanelSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 338, Short.MAX_VALUE)
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, statusPanelLayout.createSequentialGroup()
                .addComponent(statusPanelSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 4, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(statusMessageLabel)
                        .addComponent(statusAnimationLabel))
                    .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        setComponent(mainPanel);
        setMenuBar(menuBar);
        setStatusBar(statusPanel);
    }// </editor-fold>//GEN-END:initComponents

    /**
     * 設定ダイアログの表示
     */
    @Action
    public void showSettingDialog() {
        PriceBoardProperty prop = PriceBoardApp.getApplication().getPriceBoardProperty();

        if (prop.isAutoUpdate()) {
            dataUpdateTimer.stop();
        }

        JFrame mainFrame = PriceBoardApp.getApplication().getMainFrame();
        settingDialog = new SettingDialog(mainFrame, true);
        settingDialog.setLocationRelativeTo(mainFrame);
        PriceBoardApp.getApplication().show(settingDialog);

        // キャンセルの場合は終了
        if (settingDialog.getEndStatus() == 0) {
            if (prop.isAutoUpdate()) {
                dataUpdateTimer.stop();
            }
            return;
        }
        // PriceBoard を再構築
        this.priceBoardTableModel = new PriceBoardTableModel(prop.getPriceDriverObj());
        this.priceBoardTable.setModel(this.priceBoardTableModel);
        setPriceBoardTableProp();
        
        // データを取得
        Task task = updateDataAction();
        task.execute();

        // データ自動更新タイマーの設定
        if (prop.isAutoUpdate()) {
            dataUpdateTimer.setDelay(prop.getUpdateInterval() * 1000);
            dataUpdateTimer.start();
        } else {
            dataUpdateTimer.stop();
        }
    }

    /**
     * PriceBoard のテーブルセルの見栄え変更
     */
    private void setPriceBoardTableProp() {
        // テーブルのプライス部分のCellRenderer
        DefaultTableCellRenderer priceRenderer = new DefaultTableCellRenderer() {
            // override renderer preparation

            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus,
                    int row, int column) {
                // allow default preparation
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                // replace default font
                setFont(new Font("Helvetica Bold", Font.PLAIN, 14));
                return this;
            }
        };
        priceRenderer.setHorizontalAlignment(SwingConstants.RIGHT);

        //テーブルのシンボル情報のセルレンダラー
        DefaultTableCellRenderer symbolRenderer = new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus,
                    int row, int column) {
                // allow default preparation
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                // replace default font
                setFont(new Font("Helvetica Bold", Font.PLAIN, 14));
                return this;
            }
        };
        symbolRenderer.setHorizontalAlignment(SwingConstants.LEFT);

        TableColumn column;
        column = priceBoardTable.getColumnModel().getColumn(0);
        column.setCellRenderer(symbolRenderer);

        for (int i = 1; i < priceBoardTable.getColumnModel().getColumnCount(); i++) {
            column = priceBoardTable.getColumnModel().getColumn(i);
            column.setCellRenderer(priceRenderer);
        }
    }

    @Action
    public Task updateDataAction() {
        return new GetPriceDataTask(getApplication(), this.priceBoardTableModel, this.statusMessageLabel, this.trayIcon);
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenu jMenu1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JTable priceBoardTable;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JMenuItem settingMenuItem;
    private javax.swing.JLabel statusAnimationLabel;
    private javax.swing.JLabel statusMessageLabel;
    private javax.swing.JPanel statusPanel;
    private javax.swing.JMenuItem updateMenuItem;
    // End of variables declaration//GEN-END:variables

    private final Timer messageTimer;
    private final Timer busyIconTimer;
    private final Timer dataUpdateTimer;
    private final Icon idleIcon;
    private final Icon[] busyIcons = new Icon[15];
    private int busyIconIndex = 0;
    private JDialog aboutBox;
    private SettingDialog settingDialog;
    private PriceBoardTableModel priceBoardTableModel;
    private MenuItem showMenuItem;
    private TrayIcon trayIcon;
    private String toolTipText = "Price Board";
    private PopupMenu trayIconMenu;

}
