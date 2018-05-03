package eu.jokre.games.idleDungeoneer;

/**
 * Created by jokre on 19-May-17.
 */

import eu.jokre.games.idleDungeoneer.Inventory.Inventory;
import eu.jokre.games.idleDungeoneer.ability.AbilityBoss1AoE;
import eu.jokre.games.idleDungeoneer.ability.StatusEffect;
import eu.jokre.games.idleDungeoneer.entity.*;
import eu.jokre.games.idleDungeoneer.renderHelper.Image;
import eu.jokre.games.idleDungeoneer.renderHelper.iDFont;
import org.joml.Vector2d;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.nio.*;
import java.time.Duration;
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
    private Vector2f cameraPosition = new Vector2f(1920, 1080);
    public float uiScale = 2f;
    public static final int gameBoardScale = 32;
    public static final int fontSize = 15;
    public static final int fontSpacing = 5;
    public static final int fontOffset = 15;

    private Vector<PlayerCharacter> playerCharacters = new Vector<>();
    private Vector<EnemyCharacter> enemyCharacters = new Vector<>();
    private Vector<Projectile> projectiles = new Vector<>();
    private Vector<Map> mapIndex = new Vector<>();
    private int currentMap = 0;
    public Vector2f windowSize = new Vector2f(1920, 1080);

    private iDFont textHandler = null;
    private Instant lastFrame = Instant.now();
    private Image testImage;
    public Inventory inventory;

    private long experience = 0;
    private int playerLevel = 49;
    private int levelCap = 50;
    private long experienceForLevelup = 10;
    private long experienceForNextLevel = Math.round(experienceForLevelup * Math.pow(this.playerLevel, 2));

    public static IdleDungeoneer idleDungeoneer;

    protected Instant lastEnemySpawn = Instant.now();

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
        GL.createCapabilities();

        //this.jGraphTTest();

        mapIndex.addElement(new Map(0));

        this.playerCharacters.addElement(new PlayerCharacterWarrior(playerLevel, new Vector2d(15, 15), "Warrior"));
        //this.playerCharacters.addElement(new PlayerCharacterWarrior(playerLevel, new Vector2d(0, 0), "Warrior"));
        this.playerCharacters.addElement(new PlayerCharacterPriest(playerLevel, new Vector2d(10, 8), "Priest"));
        this.playerCharacters.addElement(new PlayerCharacterPriest(playerLevel, new Vector2d(10, 7), "Priest"));
        this.playerCharacters.addElement(new PlayerCharacterPriest(playerLevel, new Vector2d(10, 6), "Priest"));
        this.playerCharacters.addElement(new PlayerCharacterRogue(playerLevel, new Vector2d(10, 5), "Rogue"));
        this.playerCharacters.addElement(new PlayerCharacterRogue(playerLevel, new Vector2d(9, 10), "Rogue"));
        this.playerCharacters.addElement(new PlayerCharacterRogue(playerLevel, new Vector2d(9, 9), "Rogue"));
        this.playerCharacters.addElement(new PlayerCharacterRogue(playerLevel, new Vector2d(9, 8), "Rogue"));
        this.playerCharacters.addElement(new PlayerCharacterRogue(playerLevel, new Vector2d(9, 7), "Rogue"));
        this.playerCharacters.addElement(new PlayerCharacterMage(playerLevel, new Vector2d(9, 6), "Mage"));
        this.playerCharacters.addElement(new PlayerCharacterMage(playerLevel, new Vector2d(9, 5), "Mage"));
        this.playerCharacters.addElement(new PlayerCharacterMage(playerLevel, new Vector2d(8, 10), "Mage"));
        this.playerCharacters.addElement(new PlayerCharacterMage(playerLevel, new Vector2d(8, 9), "Mage"));
        this.playerCharacters.addElement(new PlayerCharacterMage(playerLevel, new Vector2d(8, 8), "Mage"));
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

    public Vector<PlayerCharacter> getLivingPlayerCharacters() {
        Vector<PlayerCharacter> lpc = new Vector<>();
        for (PlayerCharacter p : this.playerCharacters) {
            if (!p.isDead()) {
                lpc.addElement(p);
            }
        }
        return lpc;
    }

    public Vector<PlayerCharacter> getTanks() {
        Vector<PlayerCharacter> lpc = new Vector<>();
        for (PlayerCharacter p : this.playerCharacters) {
            if (p.isTank()) {
                lpc.addElement(p);
            }
        }
        return lpc;
    }

    public Vector<PlayerCharacter> getFriendlyTargetsInRange(PlayerCharacter source, double range) {
        Vector<PlayerCharacter> friendlyTargetsInRange = new Vector<>();
        for (PlayerCharacter e : this.playerCharacters) {
            if (source.getHitboxDistance(e) <= range) {
                friendlyTargetsInRange.addElement(e);
            }
        }
        return friendlyTargetsInRange;
    }

    public Vector<EnemyCharacter> getFriendlyTargetsInRange(EnemyCharacter source, double range) {
        Vector<EnemyCharacter> friendlyTargetsInRange = new Vector<>();
        for (EnemyCharacter e : this.enemyCharacters) {
            if (source.getHitboxDistance(e) <= range) {
                friendlyTargetsInRange.addElement(e);
            }
        }
        return friendlyTargetsInRange;
    }

    public Vector<EnemyCharacter> getHostileTargetsInRange(PlayerCharacter source, double range) {
        Vector<EnemyCharacter> hostileTargetsInRange = new Vector<>();
        for (EnemyCharacter e : this.enemyCharacters) {
            if (source.getHitboxDistance(e) <= range) {
                hostileTargetsInRange.addElement(e);
            }
        }
        return hostileTargetsInRange;
    }

    public Vector<PlayerCharacter> getHostileTargetsInRange(EnemyCharacter source, double range) {
        Vector<PlayerCharacter> hostileTargetsInRange = new Vector<>();
        for (PlayerCharacter e : this.playerCharacters) {
            if (source.getHitboxDistance(e) <= range) {
                hostileTargetsInRange.addElement(e);
            }
        }
        return hostileTargetsInRange;
    }

    private void drawText(String text, int x, int y) {
        drawText(text, x, y, 0, 0, 0);
    }

    private void drawText(String text, int x, int y, float r, float g, float b) {
        //glTranslatef(x / windowSize.x, y / windowSize.y, 0);
        //glLoadIdentity();
        glColor3f(r, g, b);
        //glScalef(uiScale / windowSize.x, -uiScale / windowSize.y, 0);
        //glTranslatef(x, y, 0);
        textHandler.print2d(text, x, y);
    }

    private void drawRectangle(int x, int y, int width, int height) {
        glDisable(GL_TEXTURE_2D);
        glBegin(GL_QUADS);
        glVertex2f(x, y);
        glVertex2f(width + x, y);
        glVertex2f(width + x, height + y);
        glVertex2f(x, height + y);
        glEnd();
        glEnable(GL_TEXTURE_2D);
    }

    private void drawRectangle(int x, int y, int width, int height, Vector3f color) {
        glColor3f(color.x, color.y, color.z);
        glDisable(GL_TEXTURE_2D);
        glBegin(GL_QUADS);
        glVertex2f(x, y);
        glVertex2f(width + x, y);
        glVertex2f(width + x, height + y);
        glVertex2f(x, height + y);
        glEnd();
        glEnable(GL_TEXTURE_2D);
    }

    private void drawProgressBar(int x, int y, int width, int height, float progress, Vector3f colorBackground, Vector3f colorBar) {
        //glLoadIdentity();
        glDisable(GL_TEXTURE_2D);
        //glScalef(uiScale / windowSize.x, -uiScale / windowSize.y, 0);
        //glTranslatef(x, y, 0);
        glColor3f(colorBackground.x, colorBackground.y, colorBackground.z);
        glBegin(GL_QUADS);
        glVertex2f(x, y);
        glVertex2f(width + x, y);
        glVertex2f(width + x, height + y);
        glVertex2f(x, height + y);
        glEnd();
        glColor3f(colorBar.x, colorBar.y, colorBar.z);
        glBegin(GL_QUADS);
        glVertex2f(x, y);
        glVertex2f(width * progress + x, y);
        glVertex2f(width * progress + x, height + y);
        glVertex2f(x, height + y);
        glEnd();
        glEnable(GL_TEXTURE_2D);
    }

    private void drawProgressBar(int x, int y, int width, int height, float progress, Vector3f colorBar, boolean drawBackground) {
        glDisable(GL_TEXTURE_2D);
        if (drawBackground) {
            glColor3f(colorBar.x / 2, colorBar.y / 2, colorBar.z / 2);
            glBegin(GL_QUADS);
            glVertex2f(x, y);
            glVertex2f(width + x, y);
            glVertex2f(width + x, height + y);
            glVertex2f(x, height + y);
            glEnd();
        }
        glColor3f(colorBar.x, colorBar.y, colorBar.z);
        glBegin(GL_QUADS);
        glVertex2f(x, y);
        glVertex2f(width * progress + x, y);
        glVertex2f(width * progress + x, height + y);
        glVertex2f(x, height + y);
        glEnd();
        glEnable(GL_TEXTURE_2D);
    }

    public Vector<EnemyCharacter> getEnemyCharacters() {
        return enemyCharacters;
    }

    public Vector<PlayerCharacter> getPlayerCharacters() {
        return playerCharacters;
    }

    public Vector<Projectile> getProjectiles() {
        return projectiles;
    }

    public void createProjectile(Projectile projectile) {
        projectiles.addElement(projectile);
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

    private void gameDrawRectangle(float x, float y, int width, int height, Vector3f color) {
        glColor3f(color.x, color.y, color.z);
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
        float tangential_factor = (float) Math.tan(theta);//calculate the tangential factor

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

            x += tx * tangential_factor;
            y += ty * tangential_factor;

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

    void drawCharacterCircle(float cx, float cy, float r, int num_segments, Vector3f color) {
        glColor3f(0, 0, 0);
        drawCircle(cx, cy, r + 2, num_segments);
        glColor3f(color.x, color.y, color.z);
        drawCircle(cx, cy, r, num_segments);
    }

    private Vector3f healthColor = new Vector3f(0.9f, 0, 0);
    private Vector3f healthColorBackground = new Vector3f(0.45f, 0, 0);
    private Vector3f resourceColorMana = new Vector3f(0, 0, 1);
    private Vector3f resourceColorManaBackground = new Vector3f(0, 0, 0.5f);
    private Vector3f resourceColorRage = new Vector3f(1, 0, 0);
    private Vector3f resourceColorRageBackground = new Vector3f(0.5f, 0, 0);
    private Vector3f resourceColorEnergy = new Vector3f(1, 1, 0);
    private Vector3f resourceColorEnergyBackground = new Vector3f(0.5f, 0.5f, 0);
    private Vector3f classColorMage = new Vector3f(0.41f, 0.8f, 0.94f);
    private Vector3f classColorWarrior = new Vector3f(0.78f, 0.61f, 0.43f);
    private Vector3f classColorRogue = new Vector3f(1f, 0.96f, 0.41f);
    private Vector3f classColorPriest = new Vector3f(1f, 1f, 1f);

    void drawPlayerCharacterFrame(PlayerCharacter p, int x, int y) {
        int playerTextSpacing = fontSize + fontSpacing;
        glColor3f(0.545f, 0.271f, 0.075f);

        //UI Background
        drawRectangle(x, y, 200, playerTextSpacing * 3);

        //General Information (HP, Name, Resource)
        long currentHealth = Math.round(p.getHealth());
        long maximumHealth = Math.round(p.getMaximumHealth());
        long currentResource = Math.round(p.getResource());
        long maximumResource = Math.round(p.getMaximumResource());
        float percentHealth = (float) currentHealth / (float) maximumHealth;
        float percentResource = (float) currentResource / (float) maximumResource;

        //HealthBar
        //drawProgressBar(x, y + playerTextSpacing, 200, playerTextSpacing, percentHealth, healthColorBackground, healthColor);
        if (p.getClass() == PlayerCharacterMage.class) {
            drawProgressBar(x, y, 200, playerTextSpacing * 2, percentHealth, classColorMage, true);
        } else if (p.getClass() == PlayerCharacterWarrior.class) {
            drawProgressBar(x, y, 200, playerTextSpacing * 2, percentHealth, classColorWarrior, true);
        } else if (p.getClass() == PlayerCharacterPriest.class) {
            drawProgressBar(x, y, 200, playerTextSpacing * 2, percentHealth, classColorPriest, true);
        } else if (p.getClass() == PlayerCharacterRogue.class) {
            drawProgressBar(x, y, 200, playerTextSpacing * 2, percentHealth, classColorRogue, true);
        }
        //drawProgressBar(x, y, 200, playerTextSpacing * 2, percentHealth, new Vector3f(0.4f,0.4f,0.4f), new Vector3f(0.2f,0.2f,0.2f));
        //ResourceBar
        switch (p.getResourceType()) {
            case MANA:
                drawProgressBar(x, y + playerTextSpacing * 2, 200, playerTextSpacing, percentResource, resourceColorManaBackground, resourceColorMana);
                break;
            case RAGE:
                drawProgressBar(x, y + playerTextSpacing * 2, 200, playerTextSpacing, percentResource, resourceColorRageBackground, resourceColorRage);
                break;
            case ENERGY:
                drawProgressBar(x, y + playerTextSpacing * 2, 200, playerTextSpacing, percentResource, resourceColorEnergyBackground, resourceColorEnergy);
                break;
        }

        drawText(String.valueOf(currentHealth) + " / " + String.valueOf(maximumHealth) + " HP"
                , x + fontSpacing, y + fontOffset + playerTextSpacing);
        drawText(String.valueOf(Math.round(percentHealth * 10000) / 100) + "%", x + 150, y + fontOffset + playerTextSpacing);
        drawText(String.valueOf(currentResource) + " / " + String.valueOf(maximumResource) + " " + p.getResourceName()
                , x + fontSpacing, y + fontOffset + playerTextSpacing * 2);
        //Show a special Bar instead of HP when Dead
        if (p.isDead()) {
            drawRectangle(x, y, 200, playerTextSpacing * 2, new Vector3f(1f, 0f, 0f));
            drawText("DEAD"
                    , x + fontSpacing + 50, y + fontOffset + playerTextSpacing);
        }
        //Name must be drawn here or it won't be visible on a dead Character.
        drawText(p.getName(), x + fontSpacing, y + fontOffset);

        //Status Effect Bar (Displaying only Cast Bar right now)
        if (p.getCharacterStatus() != WAITING) {
            if (p.getCharacterStatus() == CASTING) {
                long castTimeLeft = MILLIS.between(Instant.now(), p.getCharacterStatusUntil());
                long totalCastTime = p.currentlyCastingAbility().getCastTime().toMillis();
                float progress = (float) (totalCastTime - castTimeLeft) / (float) totalCastTime;
                drawProgressBar(x, y + playerTextSpacing * 3, 200, playerTextSpacing, progress, new Vector3f(0.5f, 0.5f, 0.5f), new Vector3f(1f, 1f, 1f));
                drawText(p.currentlyCastingAbility().getName(), x + fontSpacing, y + fontOffset + playerTextSpacing * 3);
            }
        } else {
            //Global Cooldown Display
            if (p.globalCooldownRemaining() > 0) {
                float gcd = Settings.globalCooldown.toMillis() / 1000f;
                float gcdRemaining = p.globalCooldownRemaining();
                float progress = (gcd - gcdRemaining) / gcd;
                drawProgressBar(x, y + playerTextSpacing * 3, 200, playerTextSpacing / 2, progress, new Vector3f(1f, 1f, 1f), new Vector3f(0.5f, 0.5f, 0.5f));
            }

            //Show Last Ability
            if (p.getLastAbility() != null) {
                drawText(p.getLastAbility().getName(), x + fontSpacing, y + fontOffset + playerTextSpacing * 3);
            }
        }
        int tempOffset = 202;
        //Buffs
        for (StatusEffect statusEffect : p.getCurrentBuffs()) {
            glColor3f(0, 0, 0);
            drawRectangle(x + tempOffset, y, 30, 30);
            drawText(String.valueOf(statusEffect.getRemainingTime().getSeconds()), x + tempOffset, y + 15, 1, 1, 1);
            tempOffset += 32;
        }
    }

    void drawEnemyCharacterFrame(EnemyCharacter e, int x, int y) {
        int playerTextSpacing = fontSize + fontSpacing;
        glColor3f(0.545f, 0.271f, 0.075f);

        //UI Background
        drawRectangle(x, y, 200, playerTextSpacing * 3);

        //General Information (HP, Name, Resource)
        long currentHealth = Math.round(e.getHealth());
        long maximumHealth = Math.round(e.getMaximumHealth());
        long currentResource = Math.round(e.getResource());
        long maximumResource = Math.round(e.getMaximumResource());
        float percentHealth = (float) currentHealth / (float) maximumHealth;
        float percentResource = (float) currentResource / (float) maximumResource;

        //HealthBar
        //drawProgressBar(x, y + playerTextSpacing, 200, playerTextSpacing, percentHealth, healthColorBackground, healthColor);
        drawProgressBar(x, y, 200, playerTextSpacing * 2, percentHealth, healthColor, true);
        //drawProgressBar(x, y, 200, playerTextSpacing * 2, percentHealth, new Vector3f(0.4f,0.4f,0.4f), new Vector3f(0.2f,0.2f,0.2f));
        //ResourceBar
        switch (e.getResourceType()) {
            case MANA:
                drawProgressBar(x, y + playerTextSpacing * 2, 200, playerTextSpacing, percentResource, resourceColorManaBackground, resourceColorMana);
                break;
            case RAGE:
                drawProgressBar(x, y + playerTextSpacing * 2, 200, playerTextSpacing, percentResource, resourceColorRageBackground, resourceColorRage);
                break;
            case ENERGY:
                drawProgressBar(x, y + playerTextSpacing * 2, 200, playerTextSpacing, percentResource, resourceColorEnergyBackground, resourceColorEnergy);
                break;
        }

        drawText(String.valueOf(currentHealth) + " / " + String.valueOf(maximumHealth) + " HP"
                , x + fontSpacing, y + fontOffset + playerTextSpacing);
        textHandler.print2dRight(String.valueOf(Math.round(percentHealth * 10000) / 100) + "%", x + 198, y + fontOffset + playerTextSpacing);
        drawText(String.valueOf(currentResource) + " / " + String.valueOf(maximumResource) + " " + e.getResourceName()
                , x + fontSpacing, y + fontOffset + playerTextSpacing * 2);
        //Show a special Bar instead of HP when Dead
        if (e.isDead()) {
            drawRectangle(x, y, 200, playerTextSpacing * 2, new Vector3f(1f, 0f, 0f));
            drawText("DEAD"
                    , x + fontSpacing + 50, y + fontOffset + playerTextSpacing);
        }
        //Name must be drawn here or it won't be visible on a dead Character.
        drawText(e.getName(), x + fontSpacing, y + fontOffset);

        //Status Effect Bar (Displaying only Cast Bar right now)
        if (e.getCharacterStatus() != WAITING) {
            if (e.getCharacterStatus() == CASTING) {
                long castTimeLeft = MILLIS.between(Instant.now(), e.getCharacterStatusUntil());
                long totalCastTime = e.currentlyCastingAbility().getCastTime().toMillis();
                float progress = (float) (totalCastTime - castTimeLeft) / (float) totalCastTime;
                drawProgressBar(x, y + playerTextSpacing * 3, 200, playerTextSpacing, progress, new Vector3f(0.5f, 0.5f, 0.5f), new Vector3f(1f, 1f, 1f));
                drawText(e.currentlyCastingAbility().getName(), x + fontSpacing, y + fontOffset + playerTextSpacing * 3);
            }
        }
        int tempOffset = -34;
        int counter = 0;
        //Buffs
        for (StatusEffect statusEffect : e.getCurrentDebuffs()) {
            counter++;
            glColor3f(0, 0, 0);
            drawRectangle(x + tempOffset, y, 30, 30);
            drawText(String.valueOf(statusEffect.getRemainingTime().getSeconds()), x + tempOffset, y + 15, 1, 1, 1);
            tempOffset -= 32;
            if (counter >= 5) break;
        }
    }

    void drawPlayerCharacterRaidFrame(PlayerCharacter p, int slotID) {
        int x = (int) (this.windowSize.x / (-2) + 2);          //X & Y Position of the upper left corner of the Raid Frame
        int rfSpacing = 3;  //Spacing between the tiles in the Raid Frame
        int rfW = 100;      //Width of a tile
        int rfH = 16 * 3 + 1;       //Height of a tile
        int rfHealth = rfH / 3 * 2;
        int rfMana = rfH / 3;
        int rfX = 0;        //X Offset in the Frame
        int rfY = 0;        //Y Offset in the Frame
        int y = (int) (this.windowSize.y / 2) - (5 * rfH + 4 * rfSpacing + 2);

        float healthHeight = 2;
        float manaHeight = 1;
        float totalHeight = healthHeight + manaHeight;

        //calculate Offset
        if (slotID < 5) {
            rfY = slotID * (rfH + rfSpacing);
            rfX = 0;
        } else if (slotID < 10) {
            rfY = (slotID - 5) * (rfH + rfSpacing);
            rfX = (rfW + rfSpacing);
        } else if (slotID < 15) {
            rfY = (slotID - 10) * (rfH + rfSpacing);
            rfX = 2 * (rfW + rfSpacing);
        } else if (slotID < 20) {
            rfY = (slotID - 15) * (rfH + rfSpacing);
            rfX = 3 * (rfW + rfSpacing);
        }

        Vector3f color = new Vector3f();
        switch (p.getCharacterClass()) {
            case PRIEST:
                color = classColorPriest;
                break;
            case WARRIOR:
                color = classColorWarrior;
                break;
            case ROGUE:
                color = classColorRogue;
                break;
            case MAGE:
                color = classColorMage;
                break;
        }
        drawRectangle(x + rfX - 1, y + rfY - 1, rfW + 2, rfH + 2, new Vector3f(0, 0, 0));
        drawProgressBar(x + rfX, y + rfY, rfW, rfHealth, (float) (p.getHealth() / p.getMaximumHealth()), color, true);
        glColor3f(0, 0, 0);
        textHandler.print2dRight(Math.round(p.getHealth() / p.getMaximumHealth() * 100) + "%", x + rfX + rfW, y + rfY + rfHealth - 3);
        switch (p.getResourceType()) {
            case MANA:
                drawProgressBar(x + rfX, y + rfY + rfHealth + 1, rfW, rfMana
                        , (float) (p.getResource() / p.getMaximumResource()), resourceColorMana, true);
                glColor3f(1, 1, 1);
                textHandler.print2dRight(Math.round(p.getResource() / p.getMaximumResource() * 100) + "%", x + rfX + rfW, y + rfY + rfHealth + rfMana - 3);
                break;
            case RAGE:
                drawProgressBar(x + rfX, y + rfY + rfHealth + 1, rfW, rfMana
                        , (float) (p.getResource() / p.getMaximumResource()), resourceColorRage, true);
                glColor3f(1, 1, 1);
                textHandler.print2dRight(Math.round(p.getResource() / p.getMaximumResource() * 100) + "%", x + rfX + rfW, y + rfY + rfHealth + rfMana - 3);
                break;
            case ENERGY:
                drawProgressBar(x + rfX, y + rfY + rfHealth + 1, rfW, rfMana
                        , (float) (p.getResource() / p.getMaximumResource()), resourceColorEnergy, true);
                glColor3f(0, 0, 0);
                textHandler.print2dRight(Math.round(p.getResource() / p.getMaximumResource() * 100) + "%", x + rfX + rfW, y + rfY + rfHealth + rfMana - 3);
                break;
        }
    }

    public void gameTick() {
        //TODO: Debug Code!
        /*if (ChronoUnit.MILLIS.between(this.lastEnemySpawn, Instant.now()) >= 1000) {
            this.enemyCharacters.addElement(new EnemyCharacter(this.playerLevel, Math.round(this.playerLevel * 2.5), new Vector2d(20, 20), "enemy " + (this.enemyCharacters.size() + 1), NORMAL, 1f));
            this.lastEnemySpawn = Instant.now();
        }*/
        if (this.enemyCharacters.size() < 10) {
            this.enemyCharacters.addElement(new EnemyCharacter(this.playerLevel, Math.round(this.playerLevel * 2.5), new Vector2d(20, 20), "enemy " + (this.enemyCharacters.size() + 1), RAID_ELITE, 1f));
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
                    }
                    markForRemoval.addElement(enemyCharacter);
                    this.gainExperience(Math.round(Math.pow(enemyCharacter.getLevel(), 1.5)));
                }
            }
            enemyCharacters.removeAll(markForRemoval);
        }
        if (!projectiles.isEmpty()) {
            Vector<Projectile> markForRemoval = new Vector<>();
            for (Projectile p : this.projectiles) {
                if (!p.tick()) {
                    markForRemoval.addElement(p);
                }
            }
            projectiles.removeAll(markForRemoval);
        }
    }

    private void gainExperience(long experience) {
        this.experience += experience;
        if (this.experience >= this.experienceForNextLevel) {
            this.experience -= this.experienceForNextLevel;
            this.experienceForNextLevel = Math.round(experienceForLevelup * Math.pow(this.playerLevel, 2));
            this.playerLevel++;
            for (PlayerCharacter p : playerCharacters) {
                p.setLevel(this.playerLevel);
            }
        }
        if (this.playerLevel > this.levelCap) {
            this.playerLevel = this.levelCap;
            this.experienceForNextLevel = Math.round(experienceForLevelup * Math.pow(this.playerLevel, 20));
        }
    }

    private void loop() {
        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        this.testImage = new Image("bolt04.bmp");
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
            testImage.drawTexture(0, 0);

            glPushMatrix();
            glTranslatef(Math.round(-cameraPosition.x), Math.round(-cameraPosition.y), 0);

            //Game Field Rendering here
            if (currentMap >= 0) {
                for (MapTile[] row : mapIndex.elementAt(currentMap).getTileArray()) {
                    for (MapTile tile : row) {
                        gameDrawRectangle(tile.getPosition().x * gameBoardScale, tile.getPosition().y * gameBoardScale, gameBoardScale - 2, gameBoardScale - 2);
                    }
                }
            }


            for (PlayerCharacter character : playerCharacters) {
                float r, g, b;
                switch (character.getCharacterClass()) {
                    case PRIEST:
                        r = classColorPriest.x;
                        g = classColorPriest.y;
                        b = classColorPriest.z;
                        break;
                    case WARRIOR:
                        r = classColorWarrior.x;
                        g = classColorWarrior.y;
                        b = classColorWarrior.z;
                        break;
                    case ROGUE:
                        r = classColorRogue.x;
                        g = classColorRogue.y;
                        b = classColorRogue.z;
                        break;
                    case MAGE:
                        r = classColorMage.x;
                        g = classColorMage.y;
                        b = classColorMage.z;
                        break;
                    default:
                        r = 1.0f;
                        g = 1.0f;
                        b = 1.0f;
                        break;
                }
                //glColor3f(r, g, b);
                //gameDrawRectangle((float) character.getPosition().x * gameBoardScale, (float) character.getPosition().y * gameBoardScale, gameBoardScale, gameBoardScale);
                //drawCircle((float) character.getPosition().x * gameBoardScale, (float) character.getPosition().y * gameBoardScale, gameBoardScale / 2, 36);
                drawCharacterCircle((float) character.getPosition().x * gameBoardScale, (float) character.getPosition().y * gameBoardScale,
                        gameBoardScale / 2, 36, new Vector3f(r, g, b));
                character.draw();
            }

            for (EnemyCharacter character : enemyCharacters) {
                //glColor3f(1f, 0f, 0f);
                //gameDrawRectangle((float) character.getPosition().x * gameBoardScale, (float) character.getPosition().y * gameBoardScale, gameBoardScale, gameBoardScale);
                //drawCircle((float) character.getPosition().x * gameBoardScale, (float) character.getPosition().y * gameBoardScale, character.getSize() * gameBoardScale / 2, 36);
                drawCharacterCircle((float) character.getPosition().x * gameBoardScale, (float) character.getPosition().y * gameBoardScale,
                        character.getSize() * gameBoardScale / 2, 36, new Vector3f(1, 0, 0));
            }

            for (Projectile p : projectiles) {
                glColor3f(0f, 0f, 0f);
                drawCircle((float) p.getPosition().x * gameBoardScale, (float) p.getPosition().y * gameBoardScale, p.getHitboxRadius() * gameBoardScale, 18);
            }

            glPopMatrix();

            glPushMatrix();

            glLoadIdentity();

            glColor3f(0f, 0f, 0f);
            glTranslatef(0, 0, 0);
            glScalef(uiScale / windowSize.x, -uiScale / windowSize.y, 0);
            //drawText("test", 500, 500);


            //UI Rendering

            if (!playerCharacters.isEmpty()) {
                for (int i = 0; i < playerCharacters.size(); i++) {
                    if (i < 5) {
                        drawPlayerCharacterFrame(playerCharacters.elementAt(i), -950, -500 + (fontSpacing + fontSize) * 5 * i);
                    }
                    drawPlayerCharacterRaidFrame(playerCharacters.elementAt(i), i);
                }
            }

            drawProgressBar(0, 0, 500, 20, (float) this.experience / (float) this.experienceForNextLevel, new Vector3f(1, 1, 1), new Vector3f(1, 0, 0));
            drawText("Level: " + String.valueOf(this.playerLevel), 5, 18, 0, 0, 0);
            textHandler.print2dRight(String.valueOf(this.experience), 240, 18);
            textHandler.print2d(String.valueOf(this.experienceForNextLevel), 260, 18);

            if (!enemyCharacters.isEmpty()) {
                for (int i = 0; i < enemyCharacters.size(); i++) {
                    if (i < 10) {
                        drawEnemyCharacterFrame(enemyCharacters.elementAt(i), 750, -500 + (fontSpacing + fontSize) * 5 * i);
                    }
                }
                /* int xOffset = Math.round(windowSize.x / 2) - 205;
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
                } */
            }

            //End of Text Rendering

            glPopMatrix();

            glLoadIdentity();
            glTranslatef(0, 0, 0);
            glColor3f(1, 1, 1);
            glScalef(1f / windowSize.x, -1f / windowSize.y, 0);
            //testImage.drawTexture(0, 0);

            glfwSwapBuffers(window); // swap the color buffers

            // Poll for window events. The key callback above will only be
            // invoked during this call.
            glfwPollEvents();

            long timeSinceLastFrame = Duration.between(Instant.now(), lastFrame).toMillis();
            lastFrame = Instant.now();
            if (glfwGetKey(window, GLFW_KEY_UP) == 1) {
                this.cameraPosition.y += (float) timeSinceLastFrame / 3f;
            }
            if (glfwGetKey(window, GLFW_KEY_DOWN) == 1) {
                this.cameraPosition.y -= (float) timeSinceLastFrame / 3f;
            }
            if (glfwGetKey(window, GLFW_KEY_RIGHT) == 1) {
                this.cameraPosition.x -= (float) timeSinceLastFrame / 3f;
            }
            if (glfwGetKey(window, GLFW_KEY_LEFT) == 1) {
                this.cameraPosition.x += (float) timeSinceLastFrame / 3f;
            }

            this.gameTick();
        }
    }
}
