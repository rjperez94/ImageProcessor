# ImageProcessor

## Compiling Java files using Eclipse IDE

1. Download this repository as ZIP
2. Create new `Java Project` in `Eclipse`
3. Right click on your `Java Project` --> `Import`
4. Choose `General` --> `Archive File`
5. Put directory where you downloaded ZIP in `From archive file`
6. Put `ProjectName/src` in `Into folder`
7. Click `Finish`

### Linking the UI Library

8. Right click on your `Java Project` --> `Build Path` --> `Add External Archives`
9. Select `ecs100.jar` and link it to the project. That JAR will be in the directory where you downloaded ZIP

## Running the program

1. Right click on your `Java Project` --> `Run As` --> `Java Application` --> `ImageProcessor`

## Build an executable using IntelliJ IDEA

1. Go to **File** → **Project Structure** → **Artifacts**.
2. Click the green plus (**+**) button, select **JAR**, and choose **From modules with dependencies...**
3. In the **Main Class** field, click the folder icon and select the application's entry point class.
4. Under **JAR files from libraries**, select **extract to the target JAR** (this creates the single Fat JAR).
5. Click **OK**, then click **Apply**.
6. From the top menu bar, go to **Build** → **Build Artifacts...** and click **Build**.
7. The executable jar file will be generated inside the project directory under `out/artifacts/`.

### Run the executable JAR file using the command line:

```bash
java -jar path/to/executable.jar
```

## Live Demo

You can run this application directly in your web browser via the link below:

**[Launch Live Demo](https://rjperez94.github.io/ImageProcessor/)**

### Loading Local Images

If you are trying to pick a file from your physical hard drive, you cannot browse your local folders through the Java window. You must use the bridge upload feature.

1. Look at the very top right of the Java window's title bar for a small **Up Arrow (Upload)** button.
2. Click it to trigger your **native browser file picker** (this one can see your real computer folders).
3. Select your local file. The app will silently drop it into the virtual folder named `/files/uploads/`.
4. Now, inside your Java file picker, type `/files/uploads/` into the file path bar and press **Enter** to find your uploaded file.

## Miscellaneous

### Load

Load image in `images` directory

### Save

Save image to file

### Commit

Put `temporary image` as `current image`

### Quit

Exit program

## Features

**Make sure an `image` is loaded before trying any feature**
**A feature edits the `current image`**

### Merge

1. Pick another image to merge with `current image`
2. Adjust `Merge Scale Factor`

### Crop and Zoom

1. Click on image for starting point (don't release)
2. Drag mouse to end point of choice. Make sure its within the image (still clicking the mouse)
3. Release the mouse at end point

### Rotate by angle

1. Type in angle in degrees
2. Rotates image clock-wise by that angle

### Pour Color and Lousy Pour Color (buggy)

1. Select colour to pour/fill `current image` with
2. Adjust `Pour Threshold` slider
3. Click on image

### General Convolution Filter

1. Pick filter from `general filter` directory

### Other features

- Adjust Brightness using slider
- Flip horizontal
- Rotate left
- Blur
- Fix Red Eyes (buggy)
