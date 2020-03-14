/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;

import ClientsObject.ObjectClients;
import Frames.handleUser;
import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import Models.ConectionJDBC;
import Models.Register;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.ImageIcon;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

/**
 *
 * @author E6540
 */
public class ServerGUI extends javax.swing.JFrame {

    /**
     * Creates new form ServerGUI
     */
    private Boolean serverStatus = false;
    private Connection conn = null;
    private ServerSocket svSocket = null;
    private final int ports = 14049;

    //Lưu luồng ra của từng client kết nối tới
    HashMap<String, ObjectOutputStream> listClientsOutputStream = null;

    public ServerGUI() {
        initComponents();

        table_clientsManagenment.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        table_clientsManagenment.setBackground(Color.WHITE);
        table_clientsManagenment.setFont(new Font("Arial", Font.BOLD, 12));
        table_clientsManagenment.getTableHeader().setOpaque(false);
        table_clientsManagenment.getTableHeader().setBackground(new Color(31, 59, 81));
        table_clientsManagenment.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        table_clientsManagenment.getTableHeader().setForeground(Color.WHITE);

        //Kích hoạt/vô hiệu hóa client
        table_clientsManagenment.getModel().addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                int row = e.getFirstRow();
                int column = e.getColumn();
                if (column == 3) {
                    TableModel model = (TableModel) e.getSource();
                    Boolean checked = (Boolean) model.getValueAt(row, column);
                    String userName = model.getValueAt(row, 1).toString();
                    String query = "";
                    String notificaiton = "";
                    if (checked) {
                        query = "UPDATE dbo.[USER] SET status = 1 WHERE userName = '" + userName + "'";
                        notificaiton = userName + " is active!";
                    } else {
                        query = "UPDATE dbo.[USER] SET status = 0 WHERE userName = '" + userName + "'";
                        notificaiton = userName + " is disable!";
                    }
                    try {
                        Statement stm = conn.createStatement();
                        stm.execute(query);
                        JOptionPane.showMessageDialog(null, notificaiton);
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(null, ex);
                    }

                }
            }
        });

        //Thông báo khi đóng Server
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                int input = JOptionPane.showConfirmDialog(null, "Do you wanna close Server?", "Warring!!!", JOptionPane.YES_NO_OPTION);
                if (input == JOptionPane.YES_OPTION) {
                    if (serverStatus) {
                        makeAllClientOffline();
                    }
                    System.exit(0);
                }
            }
        });

    }

    //Tiếp nhận clients kết nối tới
    public class serverWorker implements Runnable {

        @Override
        public void run() {
            try {
                svSocket = new ServerSocket(ports);
                while (true) {

                    Socket sock = svSocket.accept();
                    Thread t = new Thread(new handleClient(sock));
                    t.start();
                }
            } catch (IOException ex) {
                Logger.getLogger(ServerGUI.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

    }

    //Xử lý các luồng dữ liệu từ clients
    public class handleClient implements Runnable {

        Socket sock;
        ObjectInputStream oin = null;
        ObjectOutputStream oout = null;

        public handleClient(Socket sock) {

            try {
                this.sock = sock;
                oout = new ObjectOutputStream(this.sock.getOutputStream());
                txt_serverLog.append("\nNew client connnected!");
            } catch (IOException ex) {
                Logger.getLogger(ServerGUI.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

        @Override
        public void run() {
            ObjectClients objClient = null;
            try {
                oin = new ObjectInputStream(this.sock.getInputStream());
                while (true) {
                    objClient = (ObjectClients) oin.readObject();
                    String action = objClient.getStatus();
                    String userNameSend = objClient.getUserNameSend();//User của luồng gửi tới
                    String userNameRecv = objClient.getUserNameRecv();// User của luồng sẽ nhận được thông điệp

                    switch (action) {
                        case "register": {
                            txt_serverLog.append("\nClient Register...");
                            Register reg = new Register(userNameSend, objClient.getPassWord(), objClient.getFullName(), objClient.getAvatar());
                            String rs = reg.registerClient();
                            ObjectClients rep = new ObjectClients();
                            rep.setStatus(rs);
                            oout.writeObject(rep);
                            loadClients(conn);
                            if (rs.equals("success")) {
                                txt_serverLog.append("\n" + userNameSend + " registered success!!!");
                            }
                            break;
                        }
                        case "login": {
                            boolean rs = checkLogin(oout, userNameSend, objClient.getPassWord());
                            ObjectClients repLogin = new ObjectClients();
                            if (rs) {
                                txt_serverLog.append("\n" + userNameSend + " login success!!!");
                                listClientsOutputStream.put(userNameSend, this.oout);
                                repLogin.setStatus("resLogin");
                                repLogin.setMessage("success");
                            } else {
                                repLogin.setStatus("resLogin");
                                repLogin.setMessage("fail");
                            }
                            oout.writeObject(repLogin);
                            // Cải thiện tốc độ login cho client
                            // Thay vì chờ nói hết cho mọi người rằng tôi online thì
                            // Hiện login cho client trước còn phía server sẽ tự thông báo cho mọi người sau
                            if (rs) {
                                TellEveryOneMyStatus(userNameSend, "Online");
                            }
                            break;
                        }
                        case "addfriend": {
                            //getUserNameSend là user name của bạn
                            //getUserNameRecv là user name người bạn muốn kết bạn
                            Boolean rsAddFriend = addNewFriends(userNameSend, userNameRecv);
                            ObjectClients infoNewFriend = null;
                            if (rsAddFriend) {
                                infoNewFriend = getInfoClient(userNameRecv, "resAddfriends");
                                infoNewFriend.setMessage("success");
                                //sau khi kết bạn thành công tự động thêm mình vào danh sách 
                                //bạn bè của người kết bạn khi họ online
                                pushMeToFriendList(userNameRecv, userNameSend);
                            } else {
                                infoNewFriend = new ObjectClients();
                                infoNewFriend.setStatus("resAddfriends");
                                infoNewFriend.setMessage("fail");
                            }
                            oout.writeObject(infoNewFriend);
                            break;
                        }
                        case "getMess": {
                            int groupID = getGroupID(userNameSend, userNameRecv);
                            if (groupID != -1) {
                                ArrayList<ObjectClients> listMess = getMess(groupID);
                                for (ObjectClients mess : listMess) {
                                    oout.writeObject(mess);
                                    oout.flush();
                                }
                            }
                            break;
                        }
                        case "chat": {
                            int groupID = getGroupID(userNameSend, userNameRecv);
                            if (groupID != -1) {
                               inserChat(objClient, groupID);
                            }
                            break;
                        }
                    }
                    oout.flush();
                }

            } catch (IOException | ClassNotFoundException ex) {
                try {
                    TellEveryOneMyStatus(objClient.getUserNameSend(), "Offline");
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException ex1) {
                        Logger.getLogger(ServerGUI.class.getName()).log(Level.SEVERE, null, ex1);
                    }
                    this.sock.close();
                    txt_serverLog.append("\n" + objClient.getUserNameSend() + " is disconnected!");
                } catch (IOException ex1) {
                }
            } finally {
                clientOffline(objClient.getUserNameSend());
            }
        }

    }

    //new chat
    public boolean inserChat(ObjectClients objMess, int groupID) {
        String Mess = objMess.getMessage();
        String kind = Character.toString(Mess.charAt(0));

        String userNameSend = objMess.getUserNameSend();
        String userNameRecv = objMess.getUserNameRecv();

        ObjectClients objResMess = new ObjectClients();
        objResMess.setStatus("newMess");
        objResMess.setMessage(Mess);
        objResMess.setUserNameRecv(userNameSend);

        String query = "INSERT INTO dbo.Message VALUES  ( '" + userNameSend + "'," + groupID + ",N'" + Mess + "')";
        try {
            Statement stm = conn.createStatement();
            stm.execute(query);
        } catch (SQLException ex) {
            Logger.getLogger(ServerGUI.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        if (listClientsOutputStream.containsKey(userNameRecv)) {
            ObjectOutputStream ooutPushMess = listClientsOutputStream.get(userNameRecv);
            if (kind.equals("F") || kind.equals("P")) {
                byte[] f = readBytesFromFile(Mess.substring(1));
                objMess.setFile(f);
            }
            try {
                ooutPushMess.writeObject(objResMess);
                ooutPushMess.flush();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        return true;
    }

    //Get danh sách mess từ yêu cầu của client
    public ArrayList<ObjectClients> getMess(int groupID) {
        ArrayList<ObjectClients> listMess = new ArrayList<>();
        try {
            String query = "SELECT * FROM (SELECT TOP(30) * FROM dbo.Message WHERE group_id = "+groupID+" ORDER BY ID DESC) AS s ORDER BY s.ID ASC";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                ObjectClients objMess = new ObjectClients();
                objMess.setStatus("resMess");
                objMess.setUserNameRecv(rs.getString("sender_id"));
                String mess = rs.getNString("message");
                String sign = Character.toString(mess.charAt(0));
                objMess.setMessage(mess);
                if (sign.equals("F") || sign.equals("P")) {
                    byte[] f = readBytesFromFile(mess.substring(1));
                    objMess.setFile(f);
                }
                listMess.add(objMess);
            }
        } catch (SQLException ex) {
            Logger.getLogger(ServerGUI.class.getName()).log(Level.SEVERE, null, ex);
        }
        return listMess;
    }

    //Đọc file to byte
    public byte[] readBytesFromFile(String filePath) {

        FileInputStream fileInputStream = null;
        byte[] bytesArray = null;

        try {

            File file = new File(filePath);
            bytesArray = new byte[(int) file.length()];

            //read file into bytes[]
            fileInputStream = new FileInputStream(file);
            fileInputStream.read(bytesArray);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

        return bytesArray;

    }

    //Lấy group id của hai người nếu là bạn, còn không sẽ trả về -1
    public int getGroupID(String user_1, String user_2) {
        String query = "SELECT ID FROM dbo.[GROUP] WHERE user_1 = '" + user_1 + "' AND user_2 = '" + user_2 + "' "
                + "UNION ALL "
                + "SELECT ID FROM dbo.[GROUP] WHERE user_1 = '" + user_2 + "' AND user_2 = '" + user_1 + "'";
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            if (rs.next()) {
                return rs.getInt("ID");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return -1;
    }

    public void pushMeToFriendList(String userNameFriend, String userName) {
        //Chỉ thông báo cho người được kết bạn thêm mình vào danh sách hiện có NẾU người đó ONLINE
        if (!listClientsOutputStream.containsKey(userNameFriend)) {
            return;
        }
        Thread tPush = new Thread(new Runnable() {
            @Override
            public void run() {
                ObjectClients me = getInfoClient(userName, "pushfriendtolist");
                ObjectOutputStream ooutPush = listClientsOutputStream.get(userNameFriend);
                me.setMessage("success");
                try {
                    ooutPush.writeObject(me);
                } catch (IOException ex) {
                    Logger.getLogger(ServerGUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        tPush.run();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        panel_mainSV = new javax.swing.JPanel();
        btn_startServer = new javax.swing.JButton();
        btn_stopServer = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        txt_serverLog = new javax.swing.JTextArea();
        panel_clientsManagenment = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        table_clientsManagenment = new javax.swing.JTable();
        jPanel1 = new javax.swing.JPanel();
        label_add = new javax.swing.JLabel();
        label_edit = new javax.swing.JLabel();
        label_delete = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        label_totalUsers = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Server");
        setResizable(false);

        jTabbedPane1.setPreferredSize(new java.awt.Dimension(755, 355));

        panel_mainSV.setBackground(new java.awt.Color(0, 204, 102));
        panel_mainSV.setPreferredSize(new java.awt.Dimension(755, 355));
        panel_mainSV.setRequestFocusEnabled(false);
        panel_mainSV.setVerifyInputWhenFocusTarget(false);

        btn_startServer.setBackground(new java.awt.Color(255, 255, 255));
        btn_startServer.setFont(new java.awt.Font("Verdana", 1, 14)); // NOI18N
        btn_startServer.setForeground(new java.awt.Color(0, 204, 0));
        btn_startServer.setText("Start Server");
        btn_startServer.setToolTipText("Start Chat Server");
        btn_startServer.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btn_startServer.setPreferredSize(new java.awt.Dimension(75, 25));
        btn_startServer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_startServerActionPerformed(evt);
            }
        });

        btn_stopServer.setBackground(new java.awt.Color(255, 255, 255));
        btn_stopServer.setFont(new java.awt.Font("Verdana", 1, 14)); // NOI18N
        btn_stopServer.setForeground(new java.awt.Color(0, 204, 0));
        btn_stopServer.setText("Stop Server");
        btn_stopServer.setToolTipText("Stop Chat Server");
        btn_stopServer.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btn_stopServer.setMargin(new java.awt.Insets(0, 0, 0, 0));
        btn_stopServer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_stopServerActionPerformed(evt);
            }
        });

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Image/logo-fit.png"))); // NOI18N
        jLabel2.setToolTipText("Meow App Server");

        txt_serverLog.setEditable(false);
        txt_serverLog.setBackground(new java.awt.Color(0, 0, 0));
        txt_serverLog.setColumns(20);
        txt_serverLog.setForeground(new java.awt.Color(0, 255, 0));
        txt_serverLog.setRows(5);
        jScrollPane1.setViewportView(txt_serverLog);

        javax.swing.GroupLayout panel_mainSVLayout = new javax.swing.GroupLayout(panel_mainSV);
        panel_mainSV.setLayout(panel_mainSVLayout);
        panel_mainSVLayout.setHorizontalGroup(
            panel_mainSVLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel_mainSVLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panel_mainSVLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 221, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btn_stopServer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btn_startServer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 491, Short.MAX_VALUE)
                .addContainerGap())
        );
        panel_mainSVLayout.setVerticalGroup(
            panel_mainSVLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panel_mainSVLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(panel_mainSVLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(panel_mainSVLayout.createSequentialGroup()
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 164, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btn_startServer, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btn_stopServer, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1))
                .addGap(46, 46, 46))
        );

        jTabbedPane1.addTab("Main Server", panel_mainSV);

        panel_clientsManagenment.setBackground(new java.awt.Color(255, 255, 255));

        jScrollPane2.setBackground(new java.awt.Color(255, 255, 255));

        table_clientsManagenment.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "STT", "User Name", "Full Name", "Status"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Boolean.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        table_clientsManagenment.setFocusable(false);
        table_clientsManagenment.setGridColor(new java.awt.Color(0, 0, 0));
        table_clientsManagenment.setIntercellSpacing(new java.awt.Dimension(0, 0));
        table_clientsManagenment.setRowHeight(25);
        table_clientsManagenment.setSelectionBackground(new java.awt.Color(191, 226, 202));
        table_clientsManagenment.setSelectionForeground(new java.awt.Color(0, 0, 0));
        table_clientsManagenment.setShowHorizontalLines(false);
        table_clientsManagenment.setShowVerticalLines(false);
        table_clientsManagenment.getTableHeader().setReorderingAllowed(false);
        jScrollPane2.setViewportView(table_clientsManagenment);
        if (table_clientsManagenment.getColumnModel().getColumnCount() > 0) {
            table_clientsManagenment.getColumnModel().getColumn(0).setResizable(false);
            table_clientsManagenment.getColumnModel().getColumn(0).setPreferredWidth(2);
            table_clientsManagenment.getColumnModel().getColumn(1).setResizable(false);
            table_clientsManagenment.getColumnModel().getColumn(1).setPreferredWidth(100);
            table_clientsManagenment.getColumnModel().getColumn(2).setResizable(false);
            table_clientsManagenment.getColumnModel().getColumn(2).setPreferredWidth(200);
            table_clientsManagenment.getColumnModel().getColumn(3).setResizable(false);
        }

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setPreferredSize(new java.awt.Dimension(100, 50));
        jPanel1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 15, 10));

        label_add.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Image/btn_addUserNormal.png"))); // NOI18N
        label_add.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        label_add.setMaximumSize(new java.awt.Dimension(100, 50));
        label_add.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                label_addMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                label_addMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                label_addMouseExited(evt);
            }
        });
        jPanel1.add(label_add);

        label_edit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Image/btn_editUserNormal.png"))); // NOI18N
        label_edit.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        label_edit.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                label_editMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                label_editMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                label_editMouseExited(evt);
            }
        });
        jPanel1.add(label_edit);

        label_delete.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Image/btn_delUserNormal.png"))); // NOI18N
        label_delete.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        label_delete.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                label_deleteMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                label_deleteMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                label_deleteMouseExited(evt);
            }
        });
        jPanel1.add(label_delete);

        jLabel5.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        jLabel5.setText("User Online / Total Users:");
        jLabel5.setMaximumSize(new java.awt.Dimension(150, 30));
        jLabel5.setPreferredSize(new java.awt.Dimension(150, 30));
        jPanel1.add(jLabel5);

        label_totalUsers.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        label_totalUsers.setForeground(new java.awt.Color(0, 153, 153));
        label_totalUsers.setText("0/0");
        label_totalUsers.setMaximumSize(new java.awt.Dimension(100, 30));
        label_totalUsers.setPreferredSize(new java.awt.Dimension(50, 30));
        jPanel1.add(label_totalUsers);

        javax.swing.GroupLayout panel_clientsManagenmentLayout = new javax.swing.GroupLayout(panel_clientsManagenment);
        panel_clientsManagenment.setLayout(panel_clientsManagenmentLayout);
        panel_clientsManagenmentLayout.setHorizontalGroup(
            panel_clientsManagenmentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 750, Short.MAX_VALUE)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        panel_clientsManagenmentLayout.setVerticalGroup(
            panel_clientsManagenmentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel_clientsManagenmentLayout.createSequentialGroup()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 265, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0))
        );

        jTabbedPane1.addTab("Clients Managenment", panel_clientsManagenment);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents
    //Start Server
    private void btn_startServerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_startServerActionPerformed

        Thread t = new Thread(new serverWorker());
        t.start();
        if (this.serverStatus == true) {
            JOptionPane.showMessageDialog(null, "Server is running....!");
            return;
        }
        this.serverStatus = true;
        this.btn_startServer.setForeground(Color.red);
        this.btn_stopServer.setForeground(new Color(0, 204, 0));
        ConectionJDBC connection = new ConectionJDBC();
        this.txt_serverLog.setText("Server is running.................");
        conn = connection.getConnection();
        listClientsOutputStream = new HashMap<String, ObjectOutputStream>();
        loadClients(conn);
    }//GEN-LAST:event_btn_startServerActionPerformed
    //Stop Server
    private void btn_stopServerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_stopServerActionPerformed
        if (this.serverStatus == false) {
            JOptionPane.showMessageDialog(null, "Server not running....!");
            return;
        }

        this.serverStatus = false;
        this.btn_startServer.setForeground(new Color(0, 204, 0));
        this.btn_stopServer.setForeground(Color.red);
        try {
            this.conn.close();
            clearTable();
            this.txt_serverLog.append("\nServer is closed!");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Có lỗi xảy ra khi ngắt kết nối với Database!");
        }
    }//GEN-LAST:event_btn_stopServerActionPerformed

    private void label_addMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_label_addMouseEntered
        this.label_add.setIcon(new ImageIcon(getClass().getResource("/Image/btn_addUserHover.png")));
    }//GEN-LAST:event_label_addMouseEntered

    private void label_addMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_label_addMouseExited
        this.label_add.setIcon(new ImageIcon(getClass().getResource("/Image/btn_addUserNormal.png")));
    }//GEN-LAST:event_label_addMouseExited

    private void label_editMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_label_editMouseEntered
        this.label_edit.setIcon(new ImageIcon(getClass().getResource("/Image/btn_editUserHover.png")));
    }//GEN-LAST:event_label_editMouseEntered

    private void label_editMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_label_editMouseExited
        this.label_edit.setIcon(new ImageIcon(getClass().getResource("/Image/btn_editUserNormal.png")));
    }//GEN-LAST:event_label_editMouseExited

    private void label_deleteMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_label_deleteMouseEntered
        this.label_delete.setIcon(new ImageIcon(getClass().getResource("/Image/btn_delUserHover.png")));
    }//GEN-LAST:event_label_deleteMouseEntered

    private void label_deleteMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_label_deleteMouseExited
        this.label_delete.setIcon(new ImageIcon(getClass().getResource("/Image/btn_delUserNormal.png")));
    }//GEN-LAST:event_label_deleteMouseExited

    //Thêm client
    private void label_addMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_label_addMouseClicked
        handleUser handleUser = new handleUser(this);
        handleUser.setVisible(true);
        loadClients(conn);
    }//GEN-LAST:event_label_addMouseClicked
    //Sửa client
    private void label_editMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_label_editMouseClicked

        if (table_clientsManagenment.getSelectedRowCount() > 0) {
            int selectedRowIndex = table_clientsManagenment.getSelectedRow();
            String userName = table_clientsManagenment.getValueAt(selectedRowIndex, 1).toString();
            String fullName = table_clientsManagenment.getValueAt(selectedRowIndex, 2).toString();
            handleUser handleUser = new handleUser(this, userName, fullName);
            handleUser.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(null, "Please choose a row to Edit!");
        }
    }//GEN-LAST:event_label_editMouseClicked
    //Xóa client
    private void label_deleteMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_label_deleteMouseClicked
        if (table_clientsManagenment.getSelectedRowCount() > 0) {
            int wtdel = JOptionPane.showConfirmDialog(null, "Do you want to Delete this user?", "Warring!!!", JOptionPane.YES_NO_OPTION);
            if (wtdel == JOptionPane.YES_OPTION) {
                int selectedRowIndex = table_clientsManagenment.getSelectedRow();
                String userName = table_clientsManagenment.getValueAt(selectedRowIndex, 1).toString();
                String query = "DELETE FROM dbo.[USER] WHERE userName = '" + userName + "'";
                try {
                    Statement stm = conn.createStatement();
                    stm.execute(query);
                    JOptionPane.showMessageDialog(null, userName + " was Deleted!");
                    loadClients(conn);
                    File f = new File("F:\\HOCTAP\\Lap trinh ung dung mang\\ChatAppServer\\AvatarClients\\" + userName + ".png");
                    f.delete();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(null, ex);
                }
            }

        } else {
            JOptionPane.showMessageDialog(null, "Please choose a row to Delete!");
        }
    }//GEN-LAST:event_label_deleteMouseClicked

    //Xử lý client login
    public boolean checkLogin(ObjectOutputStream oos, String userName, String passWord) {
        String query = "SELECT * FROM dbo.[USER] WHERE userName = '" + userName + "' AND passWord = '" + passWord + "'";
        String queryOnline = "UPDATE dbo.[USER] SET status = 1 WHERE userName = '" + userName + "'";
        try {
            Statement stm = conn.createStatement();
            ResultSet rs = stm.executeQuery(query);
            Statement stmUpdate = conn.createStatement();
            stmUpdate.execute(queryOnline);
            if (!rs.next()) {
                return false;
            } else {
                ImageIcon imgIcon = new ImageIcon(rs.getString("avatar"));
                ObjectClients objProfile = new ObjectClients("profile", "", "", rs.getString("name"), imgIcon);
                oos.writeObject(objProfile);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(ServerGUI.class.getName()).log(Level.SEVERE, null, ex);
                }
                pushFriends(oos, rs.getString("userName"));
            }
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        }
        return true;
    }

    //Gửi danh sách friends cho userName
    public void pushFriends(ObjectOutputStream oos, String userName) {
        try {
            String query = "SELECT * FROM dbo.[USER] WHERE active <> 0 AND userName = ANY (SELECT user_2 FROM dbo.[GROUP] WHERE user_1 = '" + userName + "') UNION ALL "
                    + "SELECT * FROM dbo.[USER] WHERE active <> 0 AND userName = ANY (SELECT user_1 FROM dbo.[GROUP] WHERE user_2 = '" + userName + "')";
            Statement stm = conn.createStatement();
            ResultSet rs = stm.executeQuery(query);
            while (rs.next()) {
                ObjectClients objFriends = new ObjectClients();
                objFriends.setStatus("friend");
                objFriends.setUserNameRecv(rs.getString("userName"));
                objFriends.setFullName(rs.getString("name"));
                if (rs.getBoolean("status")) {
                    objFriends.setUserStatus("Online");
                } else {
                    objFriends.setUserStatus("Offline");
                }
                ImageIcon imgIcon = new ImageIcon(rs.getString("avatar"));
                objFriends.setAvatar(imgIcon);
                oos.writeObject(objFriends);
            }

        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        }

    }

    //Xử lý bắt buộc cho tất cả client offline khi server tắt
    public void clientOffline(String userName) {
        try {
            String queryOffline = "UPDATE dbo.[USER] SET status = 0 WHERE userName = '" + userName + "'";
            Statement stm = conn.createStatement();
            stm.execute(queryOffline);
            listClientsOutputStream.remove(userName);
        } catch (SQLException ex) {
            Logger.getLogger(ServerGUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //nói cho tất cả bạn bè của userName về status của user mỗi khi online/offline
    public void TellEveryOneMyStatus(String userName, String status) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<String> listFriendsOnline = getAllOnlineFriends(userName);
                ObjectClients objTellEveryOneMyStatus = new ObjectClients();
                objTellEveryOneMyStatus.setStatus("updateFriendStatus");
                objTellEveryOneMyStatus.setUserStatus(status);
                objTellEveryOneMyStatus.setUserNameRecv(userName);
                if (listFriendsOnline.isEmpty()) {
                } else {
                    System.out.println("Length: " + listFriendsOnline.get(0).length());
                    //ObjectOutputStream oopStatus;
                    for (String friend : listFriendsOnline) {
                        //oopStatus = listClientsOutputStream.get(friend);
                        try {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException ex) {
                                ex.printStackTrace();
                            }
                            listClientsOutputStream.get(friend).writeObject(objTellEveryOneMyStatus);
                            listClientsOutputStream.get(friend).flush();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        });
        t.run();
    }

    //Lấy tất cả bạn bè của userName đang online
    public ArrayList<String> getAllOnlineFriends(String userName) {
        ArrayList<String> listFriendsOnline = new ArrayList<>();
        try {
            String query = "SELECT * FROM dbo.[USER] WHERE status  <> 0 AND active <> 0 AND userName = ANY (SELECT user_2 FROM dbo.[GROUP] WHERE user_1 = '" + userName + "')"
                    + "UNION ALL "
                    + "SELECT * FROM dbo.[USER] WHERE status  <> 0 AND active <> 0 AND userName = ANY (SELECT user_1 FROM dbo.[GROUP] WHERE user_2 = '" + userName + "')";
            Statement stm = conn.createStatement();
            ResultSet rs = stm.executeQuery(query);
            while (rs.next()) {
                ObjectClients objFriendOnline = new ObjectClients();
                listFriendsOnline.add(rs.getString("userName").trim());
            }
        } catch (SQLException ex) {
            Logger.getLogger(ServerGUI.class.getName()).log(Level.SEVERE, null, ex);
        }
        return listFriendsOnline;
    }

    //Kiểm tra user có tồn tại ko? Nếu tồn tại trả về true còn không trả về false
    public boolean checkUserIsAvirable(String userName) {
        try {
            String query = "SELECT * FROM dbo.[USER] WHERE userName = '" + userName + "'";
            Statement stm = conn.createStatement();
            ResultSet rs = stm.executeQuery(query);
            if (!rs.next()) {
                return false;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return true;
    }

    //Kiểm tra hai user có phải là bạn hay không nếu có trả về true còn không trả về fasle;
    public boolean checkIsFriend(String friend_userName, String your_userName) {
        try {
            String query = "SELECT * FROM dbo.[GROUP] WHERE user_1 = '" + friend_userName + "' AND user_2 = '" + your_userName + "' "
                    + "UNION ALL SELECT * FROM dbo.[GROUP] WHERE user_1 = '" + your_userName + "' AND user_2 = '" + friend_userName + "'";
            Statement stm = conn.createStatement();
            ResultSet rs = stm.executeQuery(query);
            if (!rs.next()) {
                return false;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return true;
    }

    public ObjectClients getInfoClient(String userName, String status) {
        ObjectClients client = new ObjectClients();
        try {
            String query = "SELECT * FROM dbo.[USER] WHERE userName = '" + userName + "'";
            Statement stm = conn.createStatement();
            ResultSet rs = stm.executeQuery(query);
            if (rs.next()) {
                ImageIcon imgIcon = new ImageIcon(rs.getString("avatar"));
                client.setAvatar(imgIcon);
                client.setStatus(status);
                client.setUserNameRecv(userName);
                client.setFullName(rs.getString("name"));
                if (rs.getBoolean("status")) {
                    client.setUserStatus("Online");
                } else {
                    client.setUserStatus("Offline");
                }
            } else {
                return null;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return client;
    }

    public boolean addNewFriends(String your_userName, String friend_userName) {
        if (!checkUserIsAvirable(friend_userName) || checkIsFriend(friend_userName, your_userName)) {
            return false;
        } else {
            try {
                String query = "INSERT INTO dbo.[GROUP] ( user_1, user_2 ) VALUES  ( N'" + your_userName + "',  N'" + friend_userName + "')";
                Statement stm = conn.createStatement();
                stm.execute(query);
                txt_serverLog.append("\n" + your_userName + " and " + friend_userName + " became friends");
            } catch (SQLException ex) {
                ex.printStackTrace();
            }

        }
        return true;
    }

    public void clearTable() {
        DefaultTableModel model = (DefaultTableModel) this.table_clientsManagenment.getModel();
        model.setRowCount(0);
    }

    public void loadClients(Connection conn) {
        clearTable();
        int STT = 0;
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("Select * from dbo.[USER]");
            while (rs.next()) {
                STT++;
                String usrName = rs.getString("userName").trim();
                String fullName = rs.getString("name");
                Boolean status = rs.getBoolean("active");
                DefaultTableModel Model = (DefaultTableModel) this.table_clientsManagenment.getModel();
                Model.addRow(new Object[]{STT, usrName, fullName, status});

            }
            this.txt_serverLog.append("\nLoading Clients Infomation successfully!");
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void makeAllClientOffline() {
        String query = "UPDATE dbo.[USER] SET status = 0 ";
        try {
            Statement stm = conn.createStatement();
            stm.execute(query);
        } catch (SQLException ex) {
            Logger.getLogger(ServerGUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new ServerGUI().setVisible(true);
            }
        });
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btn_startServer;
    private javax.swing.JButton btn_stopServer;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JLabel label_add;
    private javax.swing.JLabel label_delete;
    private javax.swing.JLabel label_edit;
    private javax.swing.JLabel label_totalUsers;
    private javax.swing.JPanel panel_clientsManagenment;
    private javax.swing.JPanel panel_mainSV;
    private javax.swing.JTable table_clientsManagenment;
    private javax.swing.JTextArea txt_serverLog;
    // End of variables declaration//GEN-END:variables
}
