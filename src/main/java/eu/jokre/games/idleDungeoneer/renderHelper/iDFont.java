package eu.jokre.games.idleDungeoneer.renderHelper;

import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBTTAlignedQuad;
import org.lwjgl.stb.STBTTBakedChar;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import static eu.jokre.games.idleDungeoneer.renderHelper.IOUtil.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.stb.STBTruetype.stbtt_BakeFontBitmap;
import static org.lwjgl.stb.STBTruetype.stbtt_GetBakedQuad;
import static org.lwjgl.system.MemoryStack.stackPush;

/**
 * Created by jokre on 20-May-17.
 */
public class iDFont {

    final int fontSize = 20;
    final STBTTBakedChar.Buffer cdata;
    final int BITMAP_W = 512;
    final int BITMAP_H = 512;

    public iDFont() {
        this.cdata = init(BITMAP_W, BITMAP_H);
    }

    public void print2d(String text) {
        renderText(this.cdata, this.BITMAP_W, this.BITMAP_H, text);
    }

    private STBTTBakedChar.Buffer init(int BITMAP_W, int BITMAP_H) {
        int texID = glGenTextures();
        STBTTBakedChar.Buffer cdata = STBTTBakedChar.malloc(255);

        try {
            ByteBuffer ttf = ioResourceToByteBuffer("IMMORTAL.ttf", 160 * 1024);

            ByteBuffer bitmap = BufferUtils.createByteBuffer(BITMAP_W * BITMAP_H);
            stbtt_BakeFontBitmap(ttf, fontSize, bitmap, BITMAP_W, BITMAP_H, 32, cdata);

            glBindTexture(GL_TEXTURE_2D, texID);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_ALPHA, BITMAP_W, BITMAP_H, 0, GL_ALPHA, GL_UNSIGNED_BYTE, bitmap);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        } catch (IOException e) {
            e.printStackTrace();
        }

        glEnable(GL_TEXTURE_2D);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        return cdata;
    }

    private void renderText(STBTTBakedChar.Buffer cdata, int BITMAP_W, int BITMAP_H, String text) {
        try (MemoryStack stack = stackPush()) {
            FloatBuffer x = stack.floats(0.0f);
            FloatBuffer y = stack.floats(0.0f);

            STBTTAlignedQuad q = STBTTAlignedQuad.mallocStack(stack);

            glBegin(GL_QUADS);
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                if (c == '\n') {
                    y.put(0, y.get(0) + fontSize);
                    x.put(0, 0.0f);
                    continue;
                } else if (c < 32 || 128 <= c) {
                    continue;
                }
                stbtt_GetBakedQuad(cdata, BITMAP_W, BITMAP_H, c - 32, x, y, q, true);

                glTexCoord2f(q.s0(), q.t0());
                glVertex2f(q.x0(), q.y0());

                glTexCoord2f(q.s1(), q.t0());
                glVertex2f(q.x1(), q.y0());

                glTexCoord2f(q.s1(), q.t1());
                glVertex2f(q.x1(), q.y1());

                glTexCoord2f(q.s0(), q.t1());
                glVertex2f(q.x0(), q.y1());
            }
            glEnd();
        }
    }
}
