package test_socket;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Base64;
import java.util.Base64.Decoder;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class Main {

	public static void main(String[] args) {
	
         byte[] lMsg = new byte[60000];
         DatagramPacket dp = new DatagramPacket(lMsg, lMsg.length);
         DatagramSocket ds=null;
         
         //ImageIO.write(img, "jpg", new File("C:/Users/francesco/Desktop/output.jpg"));
         JFrame frame = new JFrame();
         
         JLabel label = new JLabel();
         
         frame.getContentPane().add(label);
         frame.setSize(200,200);
         frame.pack();
         frame.setVisible(true);
   
         try {
             ds = new DatagramSocket(9998, InetAddress.getByName("0.0.0.0"));
             //disable timeout for testing
             //ds.setSoTimeout(100);
             long t1;
             
             for(int i=0; i<100; i++) {
            	 t1 = System.nanoTime();
            	 ds.receive(dp);
            	 //System.out.println((System.nanoTime() - t1)/1000000);
                 byte[] data = new byte[dp.getLength()];
                 System.arraycopy(dp.getData(), dp.getOffset(), data, 0, dp.getLength());
                 //System.out.println(String.format("%d --- %s", data.length, data.toString()));    
                 
                
                 Decoder decoder = Base64.getDecoder(); 
                 byte[] decoded = decoder.decode(data);
                 
                 if(decoded.length > 1000) {
                	 ByteArrayInputStream bis = new ByteArrayInputStream(decoded);                 
                     BufferedImage img = ImageIO.read(bis);                 
                     label.setIcon(new ImageIcon(img));
                 }
                 else {
                	 System.out.println(new String(decoded, "UTF-8"));
                 }
          
                 
                 
                 
             }
             
            
             
             
         }
         catch(Exception e) {
        	 e.printStackTrace();
         }
        
    ds.close();
    System.out.println("Ciao");
	}

}
