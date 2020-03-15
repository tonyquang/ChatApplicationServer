package Frames;

import Models.ConectionJDBC;
import Server.ServerGUI;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;

/**
 *
 * @author E6540
 */
public class handleUser extends javax.swing.JFrame {

    private ImageIcon imgIcon = null;
    private Connection conn = null;
    private String action = "addUser";
    private String userName;
    ServerGUI svGUi;

    public handleUser(ServerGUI serverGUI) {
        initComponents();
        ConectionJDBC conection = new ConectionJDBC();
        this.conn = conection.getConnection();
        this.svGUi = serverGUI;
        this.setTitle("Add USER");
    }

    public handleUser(ServerGUI serverGUI, String userName, String fullName) {
        initComponents();
        this.svGUi = serverGUI;
        this.setTitle("Edit USER");
        this.action = "editUser";
        this.userName = userName;
        this.txt_fullName.setText(fullName);
        this.txt_usr.setText(userName);
        this.txt_usr.setEditable(false);
        this.label_register.setIcon(new ImageIcon(getClass().getResource("/Image/btn_editUserNormal.png")));
        ConectionJDBC conection = new ConectionJDBC();
        this.conn = conection.getConnection();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panel_Resginter = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        txt_usr = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        txt_pwd = new javax.swing.JPasswordField();
        jLabel3 = new javax.swing.JLabel();
        txt_fullName = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        label_chossenImgFile = new javax.swing.JLabel();
        label_imgName = new javax.swing.JLabel();
        label_register = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        panel_Resginter.setBackground(new java.awt.Color(255, 255, 255));
        panel_Resginter.setPreferredSize(new java.awt.Dimension(350, 400));

        jLabel1.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        jLabel1.setText("User Name");
        jLabel1.setPreferredSize(new java.awt.Dimension(80, 30));

        txt_usr.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        txt_usr.setPreferredSize(new java.awt.Dimension(80, 30));

        jLabel2.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        jLabel2.setText("Password");
        jLabel2.setPreferredSize(new java.awt.Dimension(80, 30));

        txt_pwd.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        txt_pwd.setPreferredSize(new java.awt.Dimension(240, 30));

        jLabel3.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        jLabel3.setText("Full Name");
        jLabel3.setPreferredSize(new java.awt.Dimension(80, 30));

        txt_fullName.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        txt_fullName.setPreferredSize(new java.awt.Dimension(80, 30));

        jLabel4.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        jLabel4.setText("Avatar");
        jLabel4.setPreferredSize(new java.awt.Dimension(80, 30));

        label_chossenImgFile.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Image/chosseImg.png"))); // NOI18N
        label_chossenImgFile.setToolTipText("Choose Image File");
        label_chossenImgFile.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        label_chossenImgFile.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                label_chossenImgFileMouseClicked(evt);
            }
        });

        label_imgName.setMaximumSize(new java.awt.Dimension(180, 30));
        label_imgName.setPreferredSize(new java.awt.Dimension(180, 30));

        label_register.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Image/btn_addUserNormal_Frame.png"))); // NOI18N
        label_register.setToolTipText("Register");
        label_register.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        label_register.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                label_registerMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                label_registerMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                label_registerMouseExited(evt);
            }
        });

        javax.swing.GroupLayout panel_ResginterLayout = new javax.swing.GroupLayout(panel_Resginter);
        panel_Resginter.setLayout(panel_ResginterLayout);
        panel_ResginterLayout.setHorizontalGroup(
            panel_ResginterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel_ResginterLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panel_ResginterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panel_ResginterLayout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txt_usr, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panel_ResginterLayout.createSequentialGroup()
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txt_pwd, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(panel_ResginterLayout.createSequentialGroup()
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txt_fullName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(panel_ResginterLayout.createSequentialGroup()
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(label_chossenImgFile)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(label_imgName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panel_ResginterLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(label_register)))
                .addContainerGap())
        );
        panel_ResginterLayout.setVerticalGroup(
            panel_ResginterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel_ResginterLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panel_ResginterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txt_usr, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(panel_ResginterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txt_pwd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(panel_ResginterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txt_fullName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 20, Short.MAX_VALUE)
                .addGroup(panel_ResginterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(label_chossenImgFile)
                    .addComponent(label_imgName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(label_register)
                .addGap(18, 18, 18))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(panel_Resginter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panel_Resginter, javax.swing.GroupLayout.PREFERRED_SIZE, 244, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void label_chossenImgFileMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_label_chossenImgFileMouseClicked
        JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
        jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        jfc.addChoosableFileFilter(new FileNameExtensionFilter("Images", "jpg", "png", "bmp", "jpeg"));
        jfc.setAcceptAllFileFilterUsed(true);
        int returnValue = jfc.showOpenDialog(null);

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            ImageIcon imgIcon = new ImageIcon(jfc.getSelectedFile().getAbsolutePath());
            Image img;
            if (imgIcon.getIconWidth() > imgIcon.getIconHeight()) {
                img = imgIcon.getImage().getScaledInstance(100, -1, Image.SCALE_SMOOTH);
            } else {
                img = imgIcon.getImage().getScaledInstance(-1, 100, Image.SCALE_SMOOTH);
            }
            this.imgIcon = new ImageIcon(img);
            if (jfc.getSelectedFile().getName().length() > 25) {
                this.label_imgName.setText(jfc.getSelectedFile().getName().substring(0, 25) + "...");
            } else {
                this.label_imgName.setText(jfc.getSelectedFile().getName());
            }
        }
    }//GEN-LAST:event_label_chossenImgFileMouseClicked

    public void addUser(String userName, String passWord, String fullName) {

        if (checkClient(userName)) {
            JOptionPane.showMessageDialog(null, "Username is already!");
            return;
        }

        if (userName.equals("") || passWord.equals("") || fullName.equals("") || this.imgIcon == null) {
            JOptionPane.showMessageDialog(null, "Please fill all of field!");
            return;
        } else {
            String avatarPath = "";
            Image img = this.imgIcon.getImage();
            BufferedImage bi = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = bi.createGraphics();
            g2.drawImage(img, 0, 0, null);
            g2.dispose();
            if (this.imgIcon == null) {
                avatarPath = "F:\\HOCTAP\\Lap trinh ung dung mang\\ChatAppServer\\AvatarClients\\default\\ava.png";
            } else {
                avatarPath = "F:\\HOCTAP\\Lap trinh ung dung mang\\ChatAppServer\\AvatarClients\\" + userName + ".png";
                try {
                    ImageIO.write(bi, "png", new File(avatarPath));
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(null, "Lưu thất bại");
                }
            }

            String query = "INSERT INTO dbo.[USER] VALUES  ( '" + userName + "' , '" + passWord + ""
                    + "' , N'" + fullName + "' , N'" + avatarPath + "' , 0,1  )";
            exc(query);
        }
    }

    public void editUser(String userName, String passWord, String fullName) {
        if (passWord.equals("") || fullName.equals("")) {
            JOptionPane.showMessageDialog(null, "Please enter password and full name!");
            return;
        } else {
            String query = "UPDATE dbo.[USER] SET passWord = '" + passWord + "', name = N'" + fullName + "' WHERE userName = '" + userName + "'";
            if (this.imgIcon != null) {
                String filePath = "F:\\HOCTAP\\Lap trinh ung dung mang\\ChatAppServer\\AvatarClients\\" + userName + ".png";
                File f = new File(filePath);
                f.delete();

                Image img = this.imgIcon.getImage();
                BufferedImage bi = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = bi.createGraphics();
                g2.drawImage(img, 0, 0, null);
                g2.dispose();
                try {
                    ImageIO.write(bi, "png", new File(filePath));
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(null, "Lưu thất bại");
                }
            }
            exc(query);
        }
    }

    public void exc(String query) {
        try {
            Statement stm = conn.createStatement();
            stm.execute(query);
        } catch (Exception e) {
        }
    }

    private void label_registerMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_label_registerMouseClicked
        String userName = txt_usr.getText();
        String passWord = txt_pwd.getText();
        String fullName = txt_fullName.getText();
        String res = "";
        if (this.action.equals("addUser")) {
            addUser(userName, passWord, fullName);
            res = "Add user " + userName + " success!";
        } else {
            editUser(userName, passWord, fullName);
            res = "Update infomation of user " + userName + " success!";
        }
        JOptionPane.showMessageDialog(null, res);
        svGUi.loadClients(conn);
        this.setVisible(false);

    }//GEN-LAST:event_label_registerMouseClicked

    public boolean checkClient(String userName) {
        String query = "SELECT userName  FROM dbo.[USER] WHERE userName = '" + userName + "'";
        try {
            Statement stm = conn.createStatement();
            ResultSet rs = stm.executeQuery(query);
            if (rs.getRow() >= 1) {
                return true;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();;
        }
        return false;
    }


    private void label_registerMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_label_registerMouseEntered
        if (action.equals("addUser")) {
            this.label_register.setIcon(new ImageIcon(getClass().getResource("/Image/btn_addUserHover.png")));
        } else {
            this.label_register.setIcon(new ImageIcon(getClass().getResource("/Image/btn_editUserHover.png")));
        }
    }//GEN-LAST:event_label_registerMouseEntered

    private void label_registerMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_label_registerMouseExited
        if (action.equals("addUser")) {
            this.label_register.setIcon(new ImageIcon(getClass().getResource("/Image/btn_addUserNormal.png")));
        } else {
            this.label_register.setIcon(new ImageIcon(getClass().getResource("/Image/btn_editUserNormal.png")));
        }
    }//GEN-LAST:event_label_registerMouseExited

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        this.dispose();
    }//GEN-LAST:event_formWindowClosing


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel label_chossenImgFile;
    private javax.swing.JLabel label_imgName;
    private javax.swing.JLabel label_register;
    private javax.swing.JPanel panel_Resginter;
    private javax.swing.JTextField txt_fullName;
    private javax.swing.JPasswordField txt_pwd;
    private javax.swing.JTextField txt_usr;
    // End of variables declaration//GEN-END:variables
}
