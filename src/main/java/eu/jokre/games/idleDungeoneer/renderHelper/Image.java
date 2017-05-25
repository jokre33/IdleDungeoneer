package eu.jokre.games.idleDungeoneer.renderHelper;

import org.joml.Vector2d;
import org.joml.Vector2f;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.Callback;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static eu.jokre.games.idleDungeoneer.IdleDungeoneer.gameBoardScale;
import static eu.jokre.games.idleDungeoneer.renderHelper.IOUtil.ioResourceToByteBuffer;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.stb.STBImage.*;

public final class Image {

    private final ByteBuffer image;

    private final int w;
    private final int h;
    private final int comp;
    private int texID;

    private Callback debugProc;

    public Image(String imagePath) {
        ByteBuffer imageBuffer;

        try {
            imageBuffer = ioResourceToByteBuffer(imagePath, 8 * 1024);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        IntBuffer w = BufferUtils.createIntBuffer(1);
        IntBuffer h = BufferUtils.createIntBuffer(1);
        IntBuffer comp = BufferUtils.createIntBuffer(1);

        //Debug Information
        //Todo: remove
        if (!stbi_info_from_memory(imageBuffer, w, h, comp)) {
            throw new RuntimeException("Failed to read image information: " + stbi_failure_reason());
        }

        System.out.println("Image width: " + w.get(0));
        System.out.println("Image height: " + h.get(0));
        System.out.println("Image components: " + comp.get(0));
        System.out.println("Image HDR: " + stbi_is_hdr_from_memory(imageBuffer));

        image = stbi_load_from_memory(imageBuffer, w, h, comp, 0);
        if (image == null) {
            throw new RuntimeException("Failed to load image: " + stbi_failure_reason());
        }

        this.w = w.get(0);
        this.h = h.get(0);
        this.comp = comp.get(0);
        System.out.println("loading successful.");

        texID = glGenTextures();
    }

    public void drawTexture(Vector2f position) {
        drawTexture(new Vector2d(position.x, position.y));
    }

    public void drawTexture(float x, float y) {
        drawTexture(new Vector2d(x, y));
    }

    public void drawTexture(double x, double y) {
        drawTexture(new Vector2d(x, y));
    }

    public void drawTexture(Vector2d position) {
        float x = Math.round(position.x * gameBoardScale);
        float y = Math.round(position.y * gameBoardScale);
        glPushMatrix();
        glEnable(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, texID);
        glDisable(GL_BLEND);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);

        if (comp == 3) {
            if ((w & 3) != 0) {
                glPixelStorei(GL_UNPACK_ALIGNMENT, 2 - (w & 1));
            }
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, w, h, 0, GL_RGB, GL_UNSIGNED_BYTE, image);
        } else {
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, w, h, 0, GL_RGBA, GL_UNSIGNED_BYTE, image);

            glEnable(GL_BLEND);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        }

        //glScalef(uiScale / wx, -uiScale / wy, 0);
        glBegin(GL_QUADS);
        {
            glTexCoord2f(0.0f, 0.0f);
            glVertex2f(x - (w / 2), y - (h / 2));

            glTexCoord2f(1.0f, 0.0f);
            glVertex2f(x + (w / 2), y - (h / 2));

            glTexCoord2f(1.0f, 1.0f);
            glVertex2f(x + (w / 2), y + (h / 2));

            glTexCoord2f(0.0f, 1.0f);
            glVertex2f(x - (w / 2), y + (h / 2));
        }
        glEnd();
        glDisable(GL_TEXTURE_2D);
        glEnable(GL_BLEND);
        glPopMatrix();
    }
}