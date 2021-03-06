package areality.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLUtils;
import android.provider.MediaStore;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by dianezheng on 4/4/17.
 */

public class PhotoCube {

    private FloatBuffer vertexBuffer;  // Vertex Buffer
    private FloatBuffer texBuffer;     // Texture Coords Buffer
    private MediaStore.Images.Media mstore = new MediaStore.Images.Media();

    private int numFaces = 6;
    //Backup images
    private int[] imageFileIDs = {
            R.drawable.pic1,
            R.drawable.pic2,
            R.drawable.pic3,
            R.drawable.pic4,
            R.drawable.pic5,
            R.drawable.pic6
    };
    private int[] textureIDs = new int[numFaces];
    private Bitmap[] bitmap = new Bitmap[numFaces];
    private float cubeHalfSize = 1.2f;

    // Constructor - Set up the vertex buffer
    public PhotoCube(Context context, String[] photos) {
        Integer urlsLength = photos.length;
        // Allocate vertex buffer. An float has 4 bytes
        ByteBuffer vbb = ByteBuffer.allocateDirect(12 * 4 * numFaces);
        vbb.order(ByteOrder.nativeOrder());
        vertexBuffer = vbb.asFloatBuffer();

        // Read images. Find the aspect ratio and adjust the vertices accordingly.
        for (int face = 0; face < numFaces; face++) {
            //if images passed in, use these as cube faces
            if (urlsLength > face) {
                try {
//                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
//                    StrictMode.setThreadPolicy(policy);
//                    URL url = new URL(photos[face]);
//                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//                    connection.setDoInput(true);
//                    connection.connect();
//                    InputStream input = connection.getInputStream();
//                    bitmap[face] = BitmapFactory.decodeStream(input);

                    //much cleaner way to do this
                      bitmap[face] = BitmapFactory.decodeStream((InputStream)new URL(photos[face]).getContent());
                } catch (IOException e) {
                }
            }
            else{
            bitmap[face] = BitmapFactory.decodeStream(
                    context.getResources().openRawResource(imageFileIDs[face]));
            }

            int imgWidth = bitmap[face].getWidth();
            int imgHeight = bitmap[face].getHeight();
            int resizeStartX = 0;
            int resizeStartY = 0;

            float faceWidth = 2.0f;
            float faceHeight = 2.0f;

            //Make pictures squares
            if (imgWidth > imgHeight) {
                resizeStartX = (imgWidth-imgHeight)/2;
                imgWidth = imgHeight;
            } else {
                resizeStartY = (imgHeight-imgWidth)/2;
                imgHeight = imgWidth;
            }

            bitmap[face]=Bitmap.createBitmap(bitmap[face], resizeStartX,resizeStartY,imgWidth, imgHeight);
            
            float faceLeft = -faceWidth / 2;
            float faceRight = -faceLeft;
            float faceTop = faceHeight / 2;
            float faceBottom = -faceTop;

            // Define the vertices for this face
            float[] vertices = {
                    faceLeft,  faceBottom, 0.0f,  // 0. left-bottom-front
                    faceRight, faceBottom, 0.0f,  // 1. right-bottom-front
                    faceLeft,  faceTop,    0.0f,  // 2. left-top-front
                    faceRight, faceTop,    0.0f,  // 3. right-top-front
            };
            vertexBuffer.put(vertices);
        }
        vertexBuffer.position(0);    // Rewind

        // Allocate texture buffer. An float has 4 bytes. Repeat for 6 faces.
        float[] texCoords = {
                0.0f, 1.0f,  // A. left-bottom
                1.0f, 1.0f,  // B. right-bottom
                0.0f, 0.0f,  // C. left-top
                1.0f, 0.0f   // D. right-top
        };
        ByteBuffer tbb = ByteBuffer.allocateDirect(texCoords.length * 4 * numFaces);
        tbb.order(ByteOrder.nativeOrder());
        texBuffer = tbb.asFloatBuffer();
        for (int face = 0; face < numFaces; face++) {
            texBuffer.put(texCoords);
        }
        texBuffer.position(0);   // Rewind
    }

    //future add filter
    public Bitmap changePic(Bitmap bitmap) {
        int W = bitmap.getWidth();
        int H = bitmap.getHeight();
        int px;

        return bitmap;
    }

    // Render the shape
    public void draw(GL10 gl) {
        gl.glFrontFace(GL10.GL_CCW);

        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
        gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, texBuffer);

        // front
        gl.glPushMatrix();
        gl.glTranslatef(0f, 0f, cubeHalfSize);
        gl.glBindTexture(GL10.GL_TEXTURE_2D, textureIDs[0]);
        gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);
        gl.glPopMatrix();

        // left
        gl.glPushMatrix();
        gl.glRotatef(270.0f, 0f, 1f, 0f);
        gl.glTranslatef(0f, 0f, cubeHalfSize);
        gl.glBindTexture(GL10.GL_TEXTURE_2D, textureIDs[1]);
        gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 4, 4);
        gl.glPopMatrix();

        // back
        gl.glPushMatrix();
        gl.glRotatef(180.0f, 0f, 1f, 0f);
        gl.glTranslatef(0f, 0f, cubeHalfSize);
        gl.glBindTexture(GL10.GL_TEXTURE_2D, textureIDs[2]);
        gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 8, 4);
        gl.glPopMatrix();

        // right
        gl.glPushMatrix();
        gl.glRotatef(90.0f, 0f, 1f, 0f);
        gl.glTranslatef(0f, 0f, cubeHalfSize);
        gl.glBindTexture(GL10.GL_TEXTURE_2D, textureIDs[3]);
        gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 12, 4);
        gl.glPopMatrix();

        // top
        gl.glPushMatrix();
        gl.glRotatef(270.0f, 1f, 0f, 0f);
        gl.glTranslatef(0f, 0f, cubeHalfSize);
        gl.glBindTexture(GL10.GL_TEXTURE_2D, textureIDs[4]);
        gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 16, 4);
        gl.glPopMatrix();

        // bottom
        gl.glPushMatrix();
        gl.glRotatef(90.0f, 1f, 0f, 0f);
        gl.glTranslatef(0f, 0f, cubeHalfSize);
        gl.glBindTexture(GL10.GL_TEXTURE_2D, textureIDs[5]);
        gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 20, 4);
        gl.glPopMatrix();

        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
    }

    // Load images into 6 GL textures
    public void loadTexture(GL10 gl) {
        gl.glGenTextures(6, textureIDs, 0); // Generate texture-ID array for 6 IDs

        // Generate OpenGL texture images
        for (int face = 0; face < numFaces; face++) {
            gl.glBindTexture(GL10.GL_TEXTURE_2D, textureIDs[face]);
            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
            // Build Texture from loaded bitmap for the currently-bind texture ID
            GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap[face], 0);
            bitmap[face].recycle();
        }
    }

}
