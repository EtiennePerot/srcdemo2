
/*Copyright (c) 2011 Aravind Rao


Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation 
 * files (the "Software"), to deal in the Software without restriction, including without limitation the rights to 
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons 
 * to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO 
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, 
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

package com.blogspot.homebrewcode;

import javax.swing.*; //imports
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;

public class ASCII {
    
    JFrame frame;
    JTextArea area;
    BufferedImage image; 
    double gValue;
    PrintWriter pw;
    FileWriter fw;
    
    public void buildGUI() // Method to build the GUI
    {
        frame = new JFrame();
        frame.setSize(500, 500);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("iASCIIfy by Aravind Rao");
        
        area = new JTextArea();
        area.setRows(1000);
        area.setColumns(1000);
        Font font = new Font("Monospaced", Font.BOLD, 5); // Set font to monospaced, bold and 5.
        area.setFont(font); 
        frame.getContentPane().add(BorderLayout.NORTH, area);
        
          try {
            
            pw = new PrintWriter(fw = new FileWriter(("result.txt"), true)); //create new file result.txt to store results
        } catch (FileNotFoundException ffx) {} 
          catch (IOException iox) {}
    }
    
    public void start() // Actual method that reads the image.
    {
    try {
        image = ImageIO.read(new File("image.jpg")); //The name of the input image is image.jpg, change it to suit your needs
       } 
    catch (IOException e) {}
    
       for (int y = 0; y <image.getHeight(); y++) //iterate through all the rows
       {
        for (int x = 0; x < image.getWidth(); x++) //iterate through all the columns in one row, thus accessing each pixel in each cell
        {
            Color pixelColor = new Color(image.getRGB(x,y)); // Create a new color object with the RGB color of the current pixel
            gValue = (((pixelColor.getRed()*0.2989)+(pixelColor.getBlue()*0.5870)+(pixelColor.getGreen()*0.1140))); // The grayscale value is 30% of the red value, 59% of the green value and 11% of the blue value.
            area.append(returnStrPos(gValue));// a helper method, see below. This method returns the appropriate character according to the darkness of the pixel.
            print(returnStrPos(gValue));// print helper method to append text onto text file
        }
        area.append("\n");//after each row is finished, append a newline so that the following characters get appended onto the next row.
       try
{
    pw.println("");
    pw.flush();
    fw.flush();
}
catch(Exception ex){}
       }
   
    }
    
    public static String returnStrPos(double g)//takes the grayscale value as parameter
    {
    String str = " ";
    /*
     Create a new string and assign to it a string based on the grayscale value.
     * If the grayscale value is very high, the pixel is very bright and assign characters 
     * such as . and , that do not appear very dark. If the grayscale value is very lowm the pixel is very dark, 
     * assign characters such as # and @ which appear very dark. 
     */

    if (g >= 230)
    {
        str = " ";
    }
    else if (g >= 200)
    {
        str = ".";
    }
    else if (g >= 180)
    {
        str = "*";
    }
    else if (g>= 160)
    {
        str = ":";
    }
    else if (g >= 130)
    {
        str = "o";
    }
    else if (g >= 100)
    {
        str = "&";
    }
    else if (g >= 70)
    {
        str = "8";
    }
    else if (g >= 50)
    {
        str = "#";
    }
    else
    {
        str = "@";
    }

    return str; // return the character
        
    }
    
    public static String returnStrNeg(double g) // same method as above, except it reverses the darkness of the pixel. A dark pixel is given a light character and vice versa.
    {
    String str = " ";

    if (g >= 230)
    {
        str = "@";
    }
    else if (g >= 200)
    {
        str = "#";
    }
    else if (g >= 180)
    {
        str = "8";
    }
    else if (g>= 160)
    {
        str = "&";
    }
    else if (g >= 130)
    {
        str = "o";
    }
    else if (g >= 100)
    {
        str = ":";
    }
    else if (g >= 70)
    {
        str = "*";
    }
    else if (g >= 50)
    {
        str = ".";
    }
    else
    {
        str = " ";
    }

    return str;
        
    }
    
    public void print(String str)// print helper mthod to print the results onto the result.txt file.
    {
try
{
    pw.print(str);
    pw.flush();
    fw.flush();
}
catch(Exception ex){}
}
 
    public static void main(String[] args) {
    ASCII ascii = new ASCII();
    ascii.buildGUI();
    ascii.start();
    
    }

}
