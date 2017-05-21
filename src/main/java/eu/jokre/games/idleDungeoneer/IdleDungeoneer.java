package eu.jokre.games.idleDungeoneer;

/**
 * Created by jokre on 19-May-17.
 */

import eu.jokre.games.idleDungeoneer.ability.AbilityFireBall;
import eu.jokre.games.idleDungeoneer.entity.EnemyCharacter;
import eu.jokre.games.idleDungeoneer.entity.PlayerCharacter;
import eu.jokre.games.idleDungeoneer.renderHelper.iDFont;
import org.joml.Vector2f;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.nio.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Vector;

import static eu.jokre.games.idleDungeoneer.entity.EnemyCharacter.Classification.*;
import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class IdleDungeoneer {
    // The window handle
    private long window;

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

        this.playerCharacters.addElement(new PlayerCharacter(10, new Vector2f(0, 0), "Player 1"));
        this.playerCharacters.lastElement().addAbility(new AbilityFireBall(), 2);
        //this.playerCharacters.addElement(new PlayerCharacter(10, new Vector2f(0, 0), "player 2"));
        this.enemyCharacters.addElement(new EnemyCharacter(10, 10, new Vector2f(0, 0), "boss 1", ELITE));
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
        glClearColor(0.5f, 0.5f, 0.5f, 0.0f);

        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.
        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer
            glColor3f(1, 1, 1); // Text color

            glLoadIdentity();
            glPushMatrix();

            glColor3f(0f, 0f, 0f);
            glTranslatef(0, 0, 0);
            glScalef(2f / windowSize.x, -2f / windowSize.y, 0);


            //Text Rendering Here

            if (!playerCharacters.isEmpty()) {
                int xOffset = Math.round(windowSize.x / (-2)) + 10;
                int yOffset = Math.round(windowSize.y / (-2)) + 25;
                for (int i = 0; i < playerCharacters.size(); i++) {
                    glColor3f(0.545f, 0.271f, 0.075f);
                    drawRectangle(xOffset - 5, yOffset - 20 + i * 85, 200, 75);

                    drawText(playerCharacters.elementAt(i).getName(), xOffset, i * 85 + yOffset);
                    drawText(String.valueOf(Math.round(playerCharacters.elementAt(i).getHealth()))
                                    + " / "
                                    + String.valueOf(Math.round(playerCharacters.elementAt(i).getMaximumHealth()))
                                    + " HP"
                            , xOffset, i * 85 + yOffset + 25);
                    drawText(String.valueOf(Math.round(playerCharacters.elementAt(i).getResource()))
                                    + " / "
                                    + String.valueOf(Math.round(playerCharacters.elementAt(i).getMaximumResource()))
                                    + " " + playerCharacters.elementAt(i).getResourceName()
                            , xOffset, i * 85 + yOffset + 50);
                    drawText(playerCharacters.elementAt(i).getCharacterStatus().toString(), xOffset + 150, i * 85 + yOffset);
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
            if (ChronoUnit.SECONDS.between(this.lastEnemySpawn, Instant.now()) >= 10) {
                System.out.println("spawning enemy?");
                this.enemyCharacters.addElement(new EnemyCharacter(10, 10, new Vector2f(0, 0), "enemy " + (this.enemyCharacters.size() + 1), NORMAL));
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
                Vector<EnemyCharacter> markForRemoval = new Vector<EnemyCharacter>();
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
