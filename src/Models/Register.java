/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Models;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/**
 *
 * @author E6540
 */
public class Register  {
    
    private Connection conn = null;
    private String userName;
    private String passWord;
    private String fulllName;
    private Image img = null;

    
    public Register(String userName, String passWord, String fullName, ImageIcon avatar) {
        this.userName = userName;
        this.fulllName = fullName;    
        this.passWord = passWord;
        this.img = avatar.getImage();             
        ConectionJDBC connection = new ConectionJDBC();
        conn = connection.getConnection();
    }

    public String registerClient() {
        if(checkClient(this.userName))
            return "already";     
        String avatarPath = "F:\\HOCTAP\\Lap trinh ung dung mang\\ChatAppServer\\AvatarClients\\"+userName+".png";    
        try {
            if(userName != null && passWord != null && fulllName != null && this.img != null)
            {
                String query = "INSERT INTO dbo.[USER] VALUES  ( '"+this.userName+"' , '"+this.passWord+""
                + "' , N'"+this.fulllName+"' , N'"+avatarPath+"' , 0, 1  )";  
                Statement stm = conn.createStatement();
                stm.execute(query);              
                BufferedImage bi = new BufferedImage(img.getWidth(null),img.getHeight(null),BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = bi.createGraphics();
                g2.drawImage(img, 0, 0, null);
                g2.dispose();
                ImageIO.write(bi, "png", new File(avatarPath));
                System.out.println("Lưu vào database thành công!");
            }
        } catch (Exception e) {
            return "fail";
        }
        return "success";
    }

    
    
    
    public boolean checkClient(String userName)
    {
        String query = "SELECT userName  FROM dbo.[USER] WHERE userName = '"+userName+"'";
        try {
            Statement stm = conn.createStatement();
            ResultSet rs = stm.executeQuery(query);
            if(rs.getRow() >= 1)
                return true;
        } catch (SQLException ex) {
            ex.printStackTrace();;
        }
        return false;
    }

    
}
