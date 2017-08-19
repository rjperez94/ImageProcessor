import ecs100.*;
import java.util.*;
import java.io.*;
import java.awt.Color;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import javax.swing.JColorChooser;

/** ImageProcessor allows the user to load, display, modify, and save an image in a number of ways.
The program should include
- Load, commit, save. (Core)
- Brightness adjustment (Core)
- Horizontal flip and 90 degree rotation. (Core)
- Merge  (Core)
- Crop&Zoom  (Core)
- Blur (3x3 filter)  (Core)

- Rotate arbitrary angle (Completion)
- Pour (spread-fill)  (Completion)
- General Convolution Filter  (Completion)

- Red-eye detection and removal (Challenge)
- Filter brush (Challenge)
 */
public class ImageProcessor implements UIButtonListener, UIMouseListener,  UISliderListener{
    public static final int left = 10;
    public static final int top = 10;

    float [][][] image;
    float [][][] tempImage;
    float [][][] newImage;  //For merge

    float [] [] blurFilter = {
            {0.006f, 0.011f, 0.022f, 0.011f, 0.006f},
            {0.011f, 0.05f, 0.1f, 0.05f, 0.011f},
            {0.022f, 0.1f, 0.2f, 0.1f, 0.022f},
            {0.011f, 0.05f, 0.1f, 0.05f, 0.011f},
            {0.006f, 0.011f, 0.022f, 0.011f, 0.006f}
        };

    //For crop and zoom    
    int cropTop, cropLeft, cropWidth, cropHeight = 0;
    boolean cropButtonPressed = false;

    //For general convo filter
    float [][] genFilter;

    //For pour method
    boolean pourButtonPressed = false;
    int customX, customY;

    //For lousyPour method
    boolean lousyPourButtonPressed = false;
    int selectedX, selectedY;           //permanent X and Y values where user clicks. does not change throughout the method call

    // For both pour methods
    Color color;
    int pourThreshold;

    public ImageProcessor(){
        UI.initialise();
        UI.setMouseMotionListener(this);    /*#IMPT*/

        UI.addButton("Load", this);
        UI.addButton("Save", this);
        UI.addButton("Commit", this);
        UI.addButton("Quit", this);

        UI.addSlider("Brightness", -255, 255, 0, this);

        UI.addButton("Flip Horizontal", this);

        UI.addButton("Rotate Left", this);

        UI.addButton("Merge", this);
        UI.addSlider("Merge Scale Factor", 0, 100, 0,  this);

        UI.addButton("Crop and Zoom", this);

        UI.addButton("Blur", this);

        UI.addButton("Rotate by Angle", this);

        UI.addButton("Pour Colour", this);
        UI.addButton("Lousy Pour Colour", this);
        UI.addSlider("Pour Threshold", 0, 30, 0, this);

        UI.addButton("General Convolution Filter", this);

        UI.addButton("Fix Red Eyes", this);

        UI.setImmediateRepaint(false);      /*#IMPT*/
    }

    public void buttonPerformed(String button){
        try {
            if (button.equals("Load")){
                loadImage();
                //set button clicked sensors to false
                /*#IMPT*/
                cropButtonPressed = false;
                pourButtonPressed = false;
                lousyPourButtonPressed = false;
            } else if (button.equals("Save")){
                saveImage();
            } else if (button.equals("Commit")){
                commit();
            } else if (button.equals("Quit")){
                UI.quit();
            } else if (button.equals("Flip Horizontal")){
                horFlip();
            } else if (button.equals("Rotate Left")){
                rotateLeft();
            } else if (button.equals("Merge")){
                merge();
            } else if (button.equals("Crop and Zoom")) {
                UI.clearGraphics();

                cropButtonPressed = true;   /*#IMPT*/

                UI.println("Press and HOLD mouse at any point on the image."); 
                UI.println("This will be the top-left corner of the cropped image");
                UI.println("Then drag the mouse to the point that will be the bottom-right of the cropped region");

                tempImage = Arrays.copyOf(image, image.length);     //copy image array to tempImage array and redraw. Vital for user guidance
                drawImage(left, top);
            } else if (button.equals("Blur")) {
                blur ();
            } else if (button.equals("Rotate by Angle")) {
                rotate();
            } else if (button.equals("Pour Colour")) {
                UI.clearGraphics();
                UI.clearText();

                color = JColorChooser.showDialog(null, "Pick a colour", color); //lets user pick a colour

                pourButtonPressed = true; /*#IMPT*/

                if (pourButtonPressed == true && color != null) {
                    UI.println("Adjust the threshold slider");
                    UI.println("Then, press mouse at any point on the image.");
                    UI.println("Bigger threshold = expect bigger region to be selected when pouring");

                    tempImage = Arrays.copyOf(image, image.length);     //copy image array to tempImage array and redraw
                    drawImage(left, top);
                } else {
                    //if user aborts color pick operation... draw loaded image
                    pourButtonPressed = false;  /*#IMPT*/
                    drawThumb();
                    UI.println("Pick colour operation aborted by user");
                }

            } else if (button.equals("Lousy Pour Colour")) {
                UI.clearGraphics();
                UI.clearText();

                color = JColorChooser.showDialog(null, "Pick a colour", color);     //copy image array to tempImage array and redraw

                lousyPourButtonPressed = true;  /*#IMPT*/

                if (lousyPourButtonPressed == true && color != null) {
                    UI.println ("This implements a 'lousy' pour into the image");
                    UI.println("Adjust the threshold slider");
                    UI.println("Then, press mouse at any point on the image.");
                    UI.println("Bigger threshold = expect bigger region to be selected when pouring");

                    tempImage = Arrays.copyOf(image, image.length);     //copy image array to tempImage array and redraw
                    drawImage(left, top);
                } else {
                    //if user aborts color pick operation... draw loaded image
                    lousyPourButtonPressed = false;  /*#IMPT*/
                    drawThumb();
                    UI.println("Pick colour operation aborted by user");
                }
            } else if (button.equals("General Convolution Filter")) {
                loadFilter();
            } else if (button.equals("Fix Red Eyes")) {
                fixEyes();
            }

        } catch(NullPointerException e){    /*#IMPT*/
            //user has not loaded anything yet and tried to do transformation
            UI.clearGraphics();
            UI.println("Image reading failed: "+e);
            UI.println("Please select an image file");
        }
    }

    public void mousePerformed(String action, double x, double y){
        if (cropButtonPressed == true) {
            if (action.equals("pressed")) {
                //assigns top-left x and y values
                cropTop = (int)y - top;  
                cropLeft = (int)x - left;
            }
            if (action.equals("dragged")) {
                //get width and height of selected region
                cropWidth = (int)x-cropLeft;
                cropHeight = (int)y-cropTop;
                UI.printMessage("Image region selected at col: "+cropLeft+" and row: "+cropTop+" with "+cropWidth+" width and "+cropHeight+" height");
            }
            try {
                if (action.equals("released")) {
                    //when mouse released crop then draw if possible - e.g. if region within image
                    cropZoom(cropTop, cropLeft, cropWidth, cropHeight);
                    drawImage(left, top);
                    cropButtonPressed = false;  /*#IMPT*/
                }
            } catch(NullPointerException | ArrayIndexOutOfBoundsException e){   
                //resets graphics and text panes for cropping
                UI.println("Image reading failed: "+e);

                UI.println("Press mouse at any point on the image."); 
                UI.println("This will be the top-left corner of the cropped image");
                UI.println("Then on that point, drag the mouse to the point ");

                UI.clearGraphics();
                cropButtonPressed = true;

                tempImage = Arrays.copyOf(image, image.length);
                drawImage(left, top);
            }

        } else if (pourButtonPressed == true && color != null) {
            try {
                if (action.equals("released")) {      
                    /*remembers image x point and y point
                     *pour colours recurcively then redraw
                     */
                    selectedY = (int)y - top;
                    selectedX = (int)x - left;
                    pourColor((int)x, (int) y);
                    drawImage(left, top);
                }
            } catch(NullPointerException e){
                UI.println("Image reading failed: "+e);
            }

        } else if (lousyPourButtonPressed == true && color != null) {
            try {
                if (action.equals("released")) {
                    //does the same as above
                    lousyPourColor(x, y);
                    drawImage(left, top);
                }
            } catch(NullPointerException e){
                UI.println("Image reading failed: "+e);
            }
        }
    }

    public void sliderPerformed(String name, double value){
        try {
            if (name.equals("Brightness")) {
                imageBright ((int)value);
            } else if (name.equals("Merge Scale Factor")){
                mergeFactor ((int)value);
            } else if (name.equals("Pour Threshold")) {
                if (pourButtonPressed == true || lousyPourButtonPressed == true) {
                    pourThreshold = ((int)value);
                }
            }
        } catch(NullPointerException e){
            /*for brightness slider and merge slider
             *if no image file is loaded then this
             */
            UI.println("Image reading failed: "+e);
            UI.println("Please select an image file");
        }
    }

    public void loadImage() {
        String imageName = UIFileChooser.open("Choose image file...");
        if (imageName==null) return;
        try {
            BufferedImage img = ImageIO.read(new File(imageName));

            image = new float [img.getHeight()][img.getWidth()][3];
            for (int row = 0; row < img.getHeight(); row++){
                for (int col = 0; col < img.getWidth(); col++){
                    int rgb =  img.getRGB(col, row);
                    image[row][col][0] = ((rgb>>16) & 255);
                    image[row][col][1] = ((rgb>>8) & 255);
                    image[row][col][2] = (rgb & 255);
                }
            }

            UI.printMessage("Loaded image " + imageName);

            drawThumb();
            UI.repaintGraphics();
        } catch(IOException | NullPointerException e){
            UI.println("Image reading failed: "+e);
            UI.println("Please select an image file");
        }
    }

    public  void saveImage() {
        if (image==null) { return; }
        int height = this.image.length;
        int width = this.image[0].length;
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                Color c = new Color((int)image[row][col][0], (int)image[row][col][1], (int)image[row][col][2]);
                img.setRGB(col, row, c.getRGB());
            }
        }
        try {
            String fname = UIFileChooser.save("Save image file...");
            if (fname==null) { return; }
            ImageIO.write(img, "png", new File(fname));

            UI.printMessage("Saved image to " + fname);
        } catch(IOException e){UI.println("Image reading failed: "+e);}
    }

    /*#Draws contents of tempImage - the temporary array*/
    public  void drawImage(double left, double top) {
        if (image==null) { return; }
        int height = this.tempImage.length;
        int width = this.tempImage[0].length;
        double y = top;
        for (int row = 0; row < height; row++) {
            double x = left;
            for (int col = 0; col < width; col++) {
                UI.setColor(new Color((int)tempImage[row][col][0],(int)tempImage[row][col][1],(int)tempImage[row][col][2]));
                UI.fillRect(x, y, 1, 1);
                x++;
            }
            y++;
        }

        UI.setColor(Color.blue);
        UI.drawString("Temporary image", left, top+height);

        UI.repaintGraphics();
    }

    /*#Draws contents of image - the current image array (also used so user can preview loaded image)*/
    public void drawThumb() {
        UI.clearText();
        UI.clearGraphics();

        int height = this.image.length;
        int width = this.image[0].length;

        double y = top;
        for (int row = 0; row < height; row++) {
            double x = left + width;
            for (int col = 0; col < width; col++) {
                UI.setColor(new Color((int)image[row][col][0],(int)image[row][col][1],(int)image[row][col][2]));
                UI.fillRect(x, y, 1, 1);
                x++;
            }
            y++;
        }

        UI.setColor(Color.blue);
        UI.drawString("Current image", left + width, top+height);

        UI.repaintGraphics();
    }

    public void commit() {
        image = Arrays.copyOf(tempImage, tempImage.length);     //copy tempImage array to image array then redraw
        drawThumb();
        UI.printMessage("Temporary transformation is now remembered in current display");
    }

    public void imageBright (int value){
        drawThumb();

        //get height and width of image
        int height = this.image.length;
        int width = this.image[0].length;

        tempImage = new float[height][width][3];

        for (int row = 0; row < height; row++){
            for (int col = 0; col < width; col++){
                tempImage[row][col][0] = image[row][col][0] + value;
                tempImage[row][col][1] = image[row][col][1] + value;
                tempImage[row][col][2] = image[row][col][2] + value;

                //ensure RGB values are within range
                if (tempImage[row][col][0] > 255 || tempImage[row][col][0] < 0) {
                    tempImage[row][col][0] = (Math.min(Math.max(tempImage[row][col][0], 0), 255)); 
                }
                if (tempImage[row][col][1] > 255 || tempImage[row][col][1] < 0) {
                    tempImage[row][col][1] = (Math.min(Math.max(tempImage[row][col][1], 0), 255));
                }
                if (tempImage[row][col][2] > 255 || tempImage[row][col][2] < 0) {
                    tempImage[row][col][2] = (Math.min(Math.max(tempImage[row][col][2], 0), 255));
                }
            }
        }

        drawImage(left, top);
    }

    public void horFlip () {
        drawThumb();
        int height = this.image.length;
        int width = this.image[0].length;

        tempImage = new float[height][width][3];

        float tempWidth, tempSpotR, tempSpotG, tempSpotB;

        for (int row = 0; row < height; row++) { 
            for (int col = 0; col < width/2; col++) {
                tempWidth = width-col-1;

                //remembers current pixel into memory
                tempSpotR = image[row][col][0];
                tempSpotG = image[row][col][1];
                tempSpotB = image[row][col][2];

                tempImage[row][col][0] = image[row][(int)tempWidth][0];
                tempImage[row][col][1] = image[row][(int)tempWidth][1];
                tempImage[row][col][2] = image[row][(int)tempWidth][2];

                tempImage[row][(int)tempWidth][0] = tempSpotR;
                tempImage[row][(int)tempWidth][1] = tempSpotG;
                tempImage[row][(int)tempWidth][2] = tempSpotB;
            }
        }
        drawImage(left, top);
    }

    public void rotateLeft () {
        drawThumb();
        int height = this.image.length;
        int width = this.image[0].length;

        int newHeight = this.image[0].length;
        int newWidth = this.image.length;

        tempImage = new float[newHeight][newWidth][3];

        for (int row = 0; row < height; row++) { 
            for (int col = 0; col < width; col++) {
                tempImage[width-1-col][row][0] = image[row][col][0];
                tempImage[width-1-col][row][1] = image[row][col][1];
                tempImage[width-1-col][row][2] = image[row][col][2];
            }
        }
        drawImage(left, top);
    }

    /*#Loads an image selected by user to merge*/
    public void merge () {
        String newImageName = UIFileChooser.open("Choose image file TO MERGE...");
        if (newImageName==null) return;
        try {
            BufferedImage img = ImageIO.read(new File(newImageName));

            newImage = new float [img.getHeight()][img.getWidth()][3];
            for (int row = 0; row < img.getHeight(); row++){
                for (int col = 0; col < img.getWidth(); col++){
                    int rgb =  img.getRGB(col, row);
                    newImage[row][col][0] = ((rgb>>16) & 255);
                    newImage[row][col][1] = ((rgb>>8) & 255);
                    newImage[row][col][2] = (rgb & 255);
                }
            }

            UI.printMessage("Loaded image " + newImageName);
        } catch(IOException e){UI.println("Image reading failed: "+e);}
    }

    public void mergeFactor (int value) {
        drawThumb();
        int height = this.image.length;
        int width = this.image[0].length;

        tempImage = new float[height][width][3];

        for (int row = 0; row < height; row++) { 
            for (int col = 0; col < width; col++) {
                try {
                    if (value == 100) {     
                        //show only newImage
                        tempImage[row][col][0] = newImage[row][col][0];
                        tempImage[row][col][1] = newImage[row][col][1];
                        tempImage[row][col][2] = newImage[row][col][2];
                    } else {
                        tempImage[row][col][0] = ((value * newImage[row][col][0]) + ((1 - value) * image[row][col][0]));
                        tempImage[row][col][1] = ((value * newImage[row][col][1]) + ((1 - value) * image[row][col][1]));
                        tempImage[row][col][2] = ((value * newImage[row][col][2]) + ((1 - value) * image[row][col][2]));
                    }

                    //ensure RGB values are within range
                    if (tempImage[row][col][0] > 255 || tempImage[row][col][0] < 0) {
                        tempImage[row][col][0] = (Math.min(Math.max(tempImage[row][col][0], 0), 255)); 
                    }
                    if (tempImage[row][col][1] > 255 || tempImage[row][col][1] < 0) {
                        tempImage[row][col][1] = (Math.min(Math.max(tempImage[row][col][1], 0), 255));
                    }
                    if (tempImage[row][col][2] > 255 || tempImage[row][col][2] < 0) {
                        tempImage[row][col][2] = (Math.min(Math.max(tempImage[row][col][2], 0), 255));
                    }

                } catch(ArrayIndexOutOfBoundsException e){
                    if (value == 100) {
                        tempImage[row][col][0] = 255;
                        tempImage[row][col][1] = 255;
                        tempImage[row][col][2] = 255;
                    } else {
                        tempImage[row][col][0] = image[row][col][0];
                        tempImage[row][col][1] = image[row][col][1];
                        tempImage[row][col][2] = image[row][col][2];
                    }

                }
            }
        } 
        drawImage(left, top);
    }

    public void cropZoom (int cropTop, int cropLeft, int cropWidth, int cropHeight) {
        drawThumb();

        int height = this.image.length;
        int width = this.image[0].length;

        tempImage = new float[height][width][3];

        int scaleHeight = height/cropHeight;
        int scaleWidth = width/cropWidth;

        int y = cropTop;
        for (int row = 0; row < height; row++) {
            int x = cropLeft;
            for (int col = 0; col < width; col++) {
                tempImage[row][col][0] = image[y/scaleHeight][x/scaleWidth][0];
                tempImage[row][col][1] = image[y/scaleHeight][x/scaleWidth][1];
                tempImage[row][col][2] = image[y/scaleHeight][x/scaleWidth][2];

                x++;
            }
            y++;
        }
    }

    public void blur () {
        drawThumb();

        int height = this.image.length;
        int width = this.image[0].length;

        int offSet = (5-1)/2;   //"5" because 5x5 blurFilter

        tempImage = new float[height][width][3];

        for (int row = offSet; row < height-offSet; row++){     //height-offset prevent filter from processing edges
            for (int col = offSet; col < width-offSet; col++){      //width-offset prevent filter from processing edges

                for (int r = offSet * -1; r <= offSet; r++){
                    for (int c = offSet * -1; c <= offSet; c++){
                        tempImage[row][col][0] += image[row+r][col+c][0] * blurFilter[r+offSet][c+offSet];
                        tempImage[row][col][1] += image[row+r][col+c][1] * blurFilter[r+offSet][c+offSet];
                        tempImage[row][col][2] += image[row+r][col+c][2] * blurFilter[r+offSet][c+offSet];
                    }
                }

                int R = (int)tempImage[row][col][0];
                int G = (int)tempImage[row][col][1];
                int B = (int)tempImage[row][col][2];

                if (R > 255 || R < 0) {
                    tempImage[row][col][0] = (Math.min(Math.max(tempImage[row][col][0], 0), (float)255)); 
                }
                if (G > 255 || G < 0) {
                    tempImage[row][col][1] = (Math.min(Math.max(tempImage[row][col][1], 0), (float)255));
                }
                if (B > 255 || B < 0) {
                    tempImage[row][col][2] = (Math.min(Math.max(tempImage[row][col][2], 0), (float)255));
                }
            }
        }
        drawImage(left, top);
    }

    public void rotate () {
        drawThumb();

        int height = this.image.length;
        int width = this.image[0].length;

        int xCenter = width/2;
        int yCenter = height/2;

        tempImage = new float[height][width][3];

        UI.clearText();
        double Angle = UI.askDouble ("Enter an angle for CLOCKWISE-rotation (in degrees)");

        for (int row = 0; row < height; row++) { 
            for (int col = 0; col < width; col++) {
                try {
                    tempImage[row][col][0] = image[(int)(yCenter+(col-xCenter)*Math.sin(Angle)+(row-yCenter)*Math.cos(Angle))] [(int)(xCenter+(col-xCenter)*Math.cos(Angle)-(row-yCenter)*Math.sin(Angle))] [0];
                    tempImage[row][col][1] = image[(int)(yCenter+(col-xCenter)*Math.sin(Angle)+(row-yCenter)*Math.cos(Angle))] [(int)(xCenter+(col-xCenter)*Math.cos(Angle)-(row-yCenter)*Math.sin(Angle))][1];
                    tempImage[row][col][2] = image[(int)(yCenter+(col-xCenter)*Math.sin(Angle)+(row-yCenter)*Math.cos(Angle))] [(int)(xCenter+(col-xCenter)*Math.cos(Angle)-(row-yCenter)*Math.sin(Angle))][2];
                }  catch(ArrayIndexOutOfBoundsException e){
                    tempImage[row][col][0] = 1;
                    tempImage[row][col][1] = 1;
                    tempImage[row][col][2] = 1;
                }

                int R = (int)tempImage[row][col][0];
                int G = (int)tempImage[row][col][1];
                int B = (int)tempImage[row][col][2];

                if (R == 1 && G == 1 && B == 1) {       
                    //sets pixels outside of rotated area to white
                    tempImage[row][col][0] = (float)255;
                    tempImage[row][col][1] = (float)255;
                    tempImage[row][col][2] = (float)255;
                }
            }
        }
        drawImage(left, top);
        UI.printMessage("Rotated image by "+Angle+" degrees CW");
    }

    /*#These next two methods only works for a 50 x 50 image or less*/
    public void pourColor(int x, int y) {
        int customY = y - top;
        int customX = x - left;

        int height = this.image.length;
        int width = this.image[0].length;

        //prevents array out of bounds
        if (customX<0||customY<0||customX>=width||customY>=height){
            return;
        }

        //calculates RGB colour differences (absolute values) of selected pixel and sorrounding pixels
        double rDiff = (Math.abs((int)tempImage[customY][customX][0] - (int)tempImage[selectedY][selectedX][0]))/7;
        double gDiff = (Math.abs((int)tempImage[customY][customX][1] - (int)tempImage[selectedY][selectedX][1]))/7;
        double bDiff = (Math.abs((int)tempImage[customY][customX][2] - (int)tempImage[selectedY][selectedX][2]))/7;

        if (rDiff <= pourThreshold &&  gDiff <= pourThreshold && bDiff <= pourThreshold) {
            UI.println("Pouring at: ("+customX+","+customY+")");

            //floodfill
            updatePixel (customY, customX);     //update selected pixel then recall to update neighbouring pixels 
            pourColor(customX, customY+1);      //fill in 4 directions if necessary - e.g. if condition in loop is met
            pourColor(customX, customY-1);
            pourColor(customX+1, customY);
            pourColor(customX-1, customY);
        }
    }

    public void updatePixel (int row, int col) {      
        tempImage[row][col][0] = color.getRed();
        tempImage[row][col][1] = color.getGreen();
        tempImage[row][col][2] = color.getBlue();
    }

    /*#The below method does not care if the next pixel to fill is connected or not
    so long as they are of same colour as the clicked pixel*/
    public void lousyPourColor(double x, double y) {
        int customY = (int)y - top;
        int customX = (int)x - left;

        int height = this.image.length;
        int width = this.image[0].length;

        for (int row = 0; row <height; row++){
            for (int col = 0; col <width; col++){
                //prevents array out of bounds
                if ((col<0||row<0||col>=width||row>=height) || (customX<0||customY<0||customX>=width||customY>=height)){
                    return;
                }

                //calculates RGB colour differences (absolute values) of selected pixel and sorrounding pixels
                double rDiff = (Math.abs((int)tempImage[row][col][0] - (int)tempImage[customY][customX][0]))/7;
                double gDiff = (Math.abs((int)tempImage[row][col][1] - (int)tempImage[customY][customX][1]))/7;
                double bDiff = (Math.abs((int)tempImage[row][col][2] - (int)tempImage[customY][customX][2]))/7;

                //update pixels
                if (rDiff < pourThreshold &&  gDiff < pourThreshold && bDiff < pourThreshold) {
                    tempImage[row][col][0] = color.getRed();
                    tempImage[row][col][1] = color.getGreen();
                    tempImage[row][col][2] = color.getBlue();
                }

            }
        }
    }

    public void loadFilter() {
        UI.clearText();
        String filterName = UIFileChooser.open("Choose FILTER file...");
        if (filterName==null) return;       //if user cancels load operation
        try {
            Scanner scan = new Scanner(new File(filterName));
            int filterSize = scan.nextInt();
            double filterScaleFactor = scan.nextDouble();       //if sum of filter values =1, then this would be one
            double filterBias = scan.nextDouble();

            genFilter = new float [filterSize][filterSize];     //dynamic filter values holder

            for (int row = 0; row < filterSize; row++){
                for (int col = 0; col < filterSize; col++){
                    genFilter[row][col] = (float)scan.nextDouble();
                }
            }

            scan.close();

            UI.printMessage("Loaded filter " + filterName);

            //processes image for filtering
            customFilter(filterSize, filterScaleFactor, filterBias);

        } catch(IOException | NullPointerException | NoSuchElementException e){
            UI.println("Reading of filter file failed: "+e);
            UI.println("File may be corrupted");
            UI.println("Please select a valid filter file");
        }
    }

    public void customFilter (int filterSize, double filterScaleFactor,double filterBias) {
        int height = this.image.length;
        int width = this.image[0].length;

        int offSet = (filterSize-1)/2;

        tempImage = new float[height][width][3];

        for (int row = offSet; row < height-offSet; row++){
            for (int col = offSet; col < width-offSet; col++){

                for (int r = offSet * -1; r <= offSet; r++){
                    for (int c = offSet * -1; c <= offSet; c++){
                        tempImage[row][col][0] += filterScaleFactor * (image[row+r][col+c][0] * genFilter[r+offSet][c+offSet]) + filterBias;
                        tempImage[row][col][1] += filterScaleFactor * (image[row+r][col+c][1] * genFilter[r+offSet][c+offSet]) + filterBias;
                        tempImage[row][col][2] += filterScaleFactor * (image[row+r][col+c][2] * genFilter[r+offSet][c+offSet]) + filterBias;
                    }
                }

                int R = (int)tempImage[row][col][0];
                int G = (int)tempImage[row][col][1];
                int B = (int)tempImage[row][col][2];

                //ensure RGB values are within range
                if (R > 255 || R < 0) {
                    tempImage[row][col][0] = (Math.min(Math.max(tempImage[row][col][0], 0), (float)255)); 
                }
                if (G > 255 || G < 0) {
                    tempImage[row][col][1] = (Math.min(Math.max(tempImage[row][col][1], 0), (float)255));
                }
                if (B > 255 || B < 0) {
                    tempImage[row][col][2] = (Math.min(Math.max(tempImage[row][col][2], 0), (float)255));
                }
            }
        }
        drawImage(left, top);
    }

    public void fixEyes () {
        int height = this.image.length;
        int width = this.image[0].length;

        tempImage = new float[height][width][3];

        for (int row = 0; row < height; row++) { 
            for (int col = 0; col < width; col++) {
                double rDiff = Math.abs((int)image[row][col][0] - 184);
                double gDiff = Math.abs((int)image[row][col][1] - 26);
                double bDiff = Math.abs((int)image[row][col][2] - 42);

                if (rDiff < 100 &&  gDiff < 100 && bDiff < 100) {
                    if (image[row][col][0] > image[row][col][1] && image[row][col][0] > image[row][col][2]) {
                        tempImage[row][col][0] = 0;
                        tempImage[row][col][1] = 0;
                        tempImage[row][col][2] = 0;
                    }
                } else {
                    tempImage[row][col][0] = image[row][col][0];
                    tempImage[row][col][1] = image[row][col][1];
                    tempImage[row][col][2] = image[row][col][2];
                }
            }
        }
        drawImage(left, top);
    }
    
    public static void main (String[] args) {
    	new ImageProcessor();
    }
}