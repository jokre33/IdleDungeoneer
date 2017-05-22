package eu.jokre.games.idleDungeoneer;

/**
 * Created by jokre on 19-May-17.
 */

import eu.jokre.games.idleDungeoneer.entity.EnemyCharacter;
import eu.jokre.games.idleDungeoneer.entity.PlayerCharacter;
import eu.jokre.games.idleDungeoneer.entity.PlayerCharacterMage;
import eu.jokre.games.idleDungeoneer.entity.PlayerCharacterWarrior;
import eu.jokre.games.idleDungeoneer.renderHelper.iDFont;
import org.joml.Vector2d;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.nio.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Vector;

import static eu.jokre.games.idleDungeoneer.entity.EnemyCharacter.Classification.*;
import static eu.jokre.games.idleDungeoneer.entity.EntityCharacter.characterStates.CASTING;
import static eu.jokre.games.idleDungeoneer.entity.EntityCharacter.characterStates.WAITING;
import static java.time.temporal.ChronoUnit.MILLIS;
import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class IdleDungeoneer {
    // The window handle
    private long window;
    private Vector2f cameraPosition = new Vector2f(0, 0);
    private static final int gameBoardScale = 32;

    private Vector<PlayerCharacter> playerCharacters = new Vector<>();
    private Vector<EnemyCharacter> enemyCharacters = new Vector<>();
    private Vector2f windowSize = new Vector2f(1920, 1080);

    private static Settings settings;
    private iDFont textHandler = null;

    public static IdleDungeoneer idleDungeoneer;

    protected Instant lastEnemySpawn = Instant.now();

    public static Settings getSettings() {
        return settings;
    }

    public static void main(String[] args) {
        idleDungeoneer = new IdleDungeoneer();
        idleDungeoneer.run();
        //new IdleDungeoneer().run();
    }

    public void run() {
        init();
        loop();

        // Free the window callbacks and destroy the window
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        // Terminate GLFW and free the error callback
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    private void init() {
        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        // Configure GLFW
        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE); // the window will be resizable
        glfwWindowHint(GLFW_DECORATED, GLFW_FALSE);

        // Create the window
        window = glfwCreateWindow((int) windowSize.x, (int) windowSize.y, "Idle Dungeoneer", NULL, NULL);
        if (window == NULL)
            throw new RuntimeException("Failed to create the GLFW window");

        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
        });

        // Get the thread stack and push a new frame
        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*

            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(window, pWidth, pHeight);

            // Get the resolution of the primary monitor
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            // Center the window
            glfwSetWindowPos(
                    window,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );

        } // the stack frame is popped automatically

        // Make the OpenGL context current
        glfwMakeContextCurrent(window);
        // Enable v-sync
        glfwSwapInterval(1);

        // Make the window visible
        glfwShowWindow(window);
        glfwSetWindowMonitor(window, glfwGetPrimaryMonitor(), 0, 0, (int) windowSize.x, (int) windowSize.y, 60);

        settings = new Settings();

        this.playerCharacters.addElement(new PlayerCharacterWarrior(50, new Vector2d(0, 0), "Warrior"));
        for (int i = 1; i <= 14; i++) {
            this.playerCharacters.addElement(new PlayerCharacterMage(50, new Vector2d(0, 0), "Mage " + i));
        }
        this.enemyCharacters.addElement(new EnemyCharacter(50, 100, new Vector2d(-20, -20), "enemy 1", RAID_BOSS));
    }

    public void generateAggroOnPlayerCharacters(EnemyCharacter e, double a) {
        for (PlayerCharacter playerCharacter : playerCharacters) {
            playerCharacter.generateAggro(e, a);
        }
    }

    public void generateAggroOnEnemyCharacters(PlayerCharacter p, double a) {
        for (EnemyCharacter enemyCharacter : enemyCharacters) {
            enemyCharacter.generateAggro(p, a);
        }
    }

    private void drawText(String text, int x, int y) {
        //glTranslatef(x / windowSize.x, y / windowSize.y, 0);
        glLoadIdentity();
        glColor3f(0f, 0f, 0f);
        glScalef(2f / windowSize.x, -2f / windowSize.y, 0);
        glTranslatef(x, y, 0);
        textHandler.print2d(text);
    }

    private void drawRectangle(int x, int y, int width, int height) {
        glLoadIdentity();
        glScalef(2f / windowSize.x, -2f / windowSize.y, 0);
        glTranslatef(x, y, 0);
        glDisable(GL_TEXTURE_2D);
        glBegin(GL_QUADS);
        glVertex2f(0, 0);
        glVertex2f(width, 0);
        glVertex2f(width, height);
        glVertex2f(0, height);
        glEnd();
        glEnable(GL_TEXTURE_2D);
    }

    private void drawProgressBar(int x, int y, int width, int height, float progress, Vector3f colorBackground, Vector3f colorBar) {
        glLoadIdentity();
        glDisable(GL_TEXTURE_2D);
        glScalef(2f / windowSize.x, -2f / windowSize.y, 0);
        glTranslatef(x, y, 0);
        glColor3f(colorBackground.x, colorBackground.y, colorBackground.z);
        glBegin(GL_QUADS);
        glVertex2f(0, 0);
        glVertex2f(width, 0);
        glVertex2f(width, height);
        glVertex2f(0, height);
        glEnd();
        glColor3f(colorBar.x, colorBar.y, colorBar.z);
        glBegin(GL_QUADS);
        glVertex2f(0, 0);
        glVertex2f(width * progress, 0);
        glVertex2f(width * progress, height);
        glVertex2f(0, height);
        glEnd();
        glEnable(GL_TEXTURE_2D);
    }

    public Vector<EnemyCharacter> getEnemyCharacters() {
        return enemyCharacters;
    }

    public Vector<PlayerCharacter> getPlayerCharacters() {
        return playerCharacters;
    }

    private void gameDrawRectangle(float x, float y, int width, int height) {
        glDisable(GL_TEXTURE_2D);
        glBegin(GL_QUADS);
        glVertex2f(x - width / 2, y - height / 2);
        glVertex2f(x + width / 2, y - height / 2);
        glVertex2f(x + width / 2, y + height / 2);
        glVertex2f(x - width / 2, y + height / 2);
        glEnd();
        glEnable(GL_TEXTURE_2D);
    }

    public void generateNavMesh() {
    }

    void drawCircle(float cx, float cy, float r, int num_segments) {
        glDisable(GL_TEXTURE_2D);
        float theta = 2f * 3.1415926f / (float) num_segments;
        float tangetial_factor = (float) Math.tan(theta);//calculate the tangential factor

        float radial_factor = (float) Math.cos(theta);//calculate the radial factor

        float x = r;//we start at angle = 0

        float y = 0;

        //glBegin(GL_LINE_LOOP);
        for (int ii = 0; ii < num_segments; ii++) {
            glBegin(GL_TRIANGLES);
            glVertex2f(x + cx, y + cy);//output vertex

            //calculate the tangential vector
            //remember, the radial vector is (x, y)
            //to get the tangential vector we flip those coordinates and negate one of them

            float tx = -y;
            float ty = x;

            //add the tangential vector

            x += tx * tangetial_factor;
            y += ty * tangetial_factor;

            //correct using the radial factor

            x *= radial_factor;
            y *= radial_factor;

            glVertex2f(x + cx, y + cy);
            glVertex2f(cx, cy);

            glEnd();
        }
        //glEnd();
        glEnable(GL_TEXTURE_2D);
    }

    private void loop() {
        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities();
        glEnable(GL_QUADS);

        try {
            textHandler = new iDFont();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        // Set the clear color
        glClearColor(1f, 0.412f, 0.706f, 0.0f);

        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.
        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer
            glColor3f(1, 1, 1); // Text color

            glLoadIdentity();
            glScalef(1f / windowSize.x, -1f / windowSize.y, 0);

            glPushMatrix();
            glTranslatef(cameraPosition.x, cameraPosition.y, 0);

            //Game Field Rendering here
            for (PlayerCharacter character : playerCharacters) {
                glColor3f(0f, 0f, 1f);
                //gameDrawRectangle((float) character.getPosition().x * gameBoardScale, (float) character.getPosition().y * gameBoardScale, gameBoardScale, gameBoardScale);
                drawCircle((float) character.getPosition().x * gameBoardScale, (float) character.getPosition().y * gameBoardScale, gameBoardScale / 2, 36);
            }

            for (EnemyCharacter character : enemyCharacters) {
                glColor3f(1f, 0f, 0f);
                //gameDrawRectangle((float) character.getPosition().x * gameBoardScale, (float) character.getPosition().y * gameBoardScale, gameBoardScale, gameBoardScale);
                drawCircle((float) character.getPosition().x * gameBoardScale, (float) character.getPosition().y * gameBoardScale, character.getSize() * gameBoardScale / 2, 36);
            }


            glPopMatrix();

            glPushMatrix();

            glColor3f(0f, 0f, 0f);
            glTranslatef(0, 0, 0);
            glScalef(2f / windowSize.x, -2f / windowSize.y, 0);


            //UI Rendering

            if (!playerCharacters.isEmpty()) {
                int playerFrameSpacing = 110;
                int xOffset = Math.round(windowSize.x / (-2)) + 10;
                int yOffset = Math.round(windowSize.y / (-2)) + 25;
                for (int i = 0; i < playerCharacters.size(); i++) {
                    glColor3f(0.545f, 0.271f, 0.075f);
                    drawRectangle(xOffset - 5, yOffset - 20 + i * 110, 200, 75);

                    drawText(playerCharacters.elementAt(i).getName(), xOffset, i * playerFrameSpacing + yOffset);
                    drawText(String.valueOf(Math.round(playerCharacters.elementAt(i).getHealth()))
                                    + " / "
                                    + String.valueOf(Math.round(playerCharacters.elementAt(i).getMaximumHealth()))
                                    + " HP"
                            , xOffset, i * playerFrameSpacing + yOffset + 25);
                    drawText(String.valueOf(Math.round(playerCharacters.elementAt(i).getResource()))
                                    + " / "
                                    + String.valueOf(Math.round(playerCharacters.elementAt(i).getMaximumResource()))
                                    + " " + playerCharacters.elementAt(i).getResourceName()
                            , xOffset, i * playerFrameSpacing + yOffset + 50);
                    drawText(String.valueOf(playerCharacters.elementAt(i).globalCooldownRemaining()), xOffset + 150, i * playerFrameSpacing + yOffset);

                    //Status Effect Bar
                    if (playerCharacters.elementAt(i).getCharacterStatus() != WAITING) {
                        if (playerCharacters.elementAt(i).getCharacterStatus() == CASTING) {
                            long castTimeLeft = MILLIS.between(Instant.now(), playerCharacters.elementAt(i).getCharacterStatusUntil());
                            long totalCastTime = playerCharacters.elementAt(i).currentlyCastingAbility().getCastTime().toMillis();
                            float progress = (float) (totalCastTime - castTimeLeft) / (float) totalCastTime;
                            drawProgressBar(xOffset - 5, yOffset - 20 + i * playerFrameSpacing + 75, 200, 25, progress, new Vector3f(0.5f, 0.5f, 0.5f), new Vector3f(1f, 1f, 1f));
                            drawText(playerCharacters.elementAt(i).currentlyCastingAbility().getName(), xOffset, i * playerFrameSpacing + yOffset + 75);
                        }
                    } else if (playerCharacters.elementAt(i).getLastAbility() != null) {
                        drawText(playerCharacters.elementAt(i).getLastAbility().getName(), xOffset, i * playerFrameSpacing + yOffset + 75);
                    }
                }
            }

            if (!enemyCharacters.isEmpty()) {
                int xOffset = Math.round(windowSize.x / 2) - 205;
                int yOffset = Math.round(windowSize.y / (-2)) + 20;
                for (int i = 0; i < enemyCharacters.size(); i++) {
                    drawText(enemyCharacters.elementAt(i).getName(), xOffset, i * 85 + yOffset);
                    drawText(String.valueOf(Math.round(enemyCharacters.elementAt(i).getHealth()))
                                    + " / "
                                    + String.valueOf(Math.round(enemyCharacters.elementAt(i).getMaximumHealth()))
                                    + " HP"
                            , xOffset, i * 85 + yOffset + 25);
                    drawText(String.valueOf(Math.round(enemyCharacters.elementAt(i).getResource()))
                                    + " / "
                                    + String.valueOf(Math.round(enemyCharacters.elementAt(i).getMaximumResource()))
                                    + " " + enemyCharacters.elementAt(i).getResourceName()
                            , xOffset, i * 85 + yOffset + 50);
                }
            }

            //End of Text Rendering

            glPopMatrix();

            glfwSwapBuffers(window); // swap the color buffers

            // Poll for window events. The key callback above will only be
            // invoked during this call.
            glfwPollEvents();

            //TODO: Debug Code!
            if (ChronoUnit.SECONDS.between(this.lastEnemySpawn, Instant.now()) >= 10000) {
                this.enemyCharacters.addElement(new EnemyCharacter(50, 70, new Vector2d(-20, -20), "enemy " + (this.enemyCharacters.size() + 1), NORMAL));
                this.lastEnemySpawn = Instant.now();
            }

            if (!playerCharacters.isEmpty()) {
                for (PlayerCharacter playerCharacter : this.playerCharacters) {
                    if (!playerCharacter.tick()) {
                        for (EnemyCharacter enemyCharacter : enemyCharacters) {
                            enemyCharacter.removeAggroTarget(playerCharacter);
                        }
                    }
                }
            }
            if (!enemyCharacters.isEmpty()) {
                Vector<EnemyCharacter> markForRemoval = new Vector<>();
                for (EnemyCharacter enemyCharacter : this.enemyCharacters) {
                    if (!enemyCharacter.tick()) {
                        for (PlayerCharacter playerCharacter : this.playerCharacters) {
                            playerCharacter.removeAggroTarget(enemyCharacter);
                            markForRemoval.addElement(enemyCharacter);
                        }
                    }
                }
                enemyCharacters.removeAll(markForRemoval);
            }
        }
    }
}
