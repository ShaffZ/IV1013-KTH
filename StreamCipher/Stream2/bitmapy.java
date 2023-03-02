import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

import javax.imageio.ImageIO;


public class bitmapy {

    public static void main(String[] args) {

        //int[][] list = new int[256][256];
        MyRandom rand = new MyRandom(9345254);
        Random rand2 = new Random(9345254);

        try {

            BufferedImage MyRand = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
            BufferedImage Rand = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
            int[] list = new int[256];
            for(int i = 0; i < 256; i++){
                list[i] = rand.nextInt(256);
                if(list[i] == 256)
                System.out.println("lol");
            }

            File outputfile = new File("MyRand.jpg");
            File outputfile2 = new File("Rand.jpg");

            for(int i = 0; i < 256*256; i++){
                MyRand.setRGB(rand.nextInt(256), rand.nextInt(256), 11111111);
                Rand.setRGB(rand2.nextInt(256), rand2.nextInt(256), 11111111);

            }
            System.out.println(ImageIO.write(MyRand, "jpg", outputfile));
            System.out.println(ImageIO.write(Rand, "jpg", outputfile2));

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
    
}
