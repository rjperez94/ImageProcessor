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

**Make sure an `image` is loaded**

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
