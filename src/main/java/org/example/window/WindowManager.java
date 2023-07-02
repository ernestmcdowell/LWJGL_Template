package org.example.window;

import imgui.ImGuiIO;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import org.example.camera.Camera;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFWErrorCallback;
import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.GLFW_TRUE;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;
import imgui.ImGui;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;


public class WindowManager {
    private long window;
    private final Camera camera;
    private final float scrollSensitivity = 0.1f;
    private ImGuiImplGlfw imGuiGlfw = new ImGuiImplGlfw();
    private ImGuiImplGl3 imGuiGl3 = new ImGuiImplGl3();
    private double mouseX, mouseY;
    private boolean mouseDragging = false;


    public WindowManager(Camera camera) {
        imGuiGlfw = new ImGuiImplGlfw();
        imGuiGl3 = new ImGuiImplGl3();
        this.camera = camera;
    }

    public void initWindow(int width, int height, String title) {
        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }



        // Configure GLFW
        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable

        window = glfwCreateWindow(width, height, title, NULL, NULL);
        if (window == NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
            }
            if(key == GLFW_KEY_F && action == GLFW_PRESS){
                camera.move(0, 0);
            }
        });

        glfwSetMouseButtonCallback(window, (window, button, action, mods) -> {
            if (button == GLFW_MOUSE_BUTTON_LEFT) {
                if (action == GLFW_PRESS) {
                    mouseDragging = true;
                    glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
                } else {
                    mouseDragging = false;
                    glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
                }
            }
        });


        glfwSetScrollCallback(window, (window, xOffset, yOffset) -> {
            // Adjust the zoom based on the scroll offset
            float zoomOffset = (float) (yOffset * scrollSensitivity);
            float newZoom = camera.getZoom() + zoomOffset;
            // Clamp the zoom value to a valid range
            newZoom = Math.max(0.1f, newZoom);
            newZoom = Math.min(10.0f, newZoom);

            // Adjust the camera's position based on the mouse's position
            float aspectRatio = (float) width / height;
            float dx = (float) ((mouseX / width - 0.5) * 2 * aspectRatio);
            float dy = (float) ((mouseY / height - 0.5) * 2);
            camera.move(dx * (newZoom - camera.getZoom()), dy * (newZoom - camera.getZoom()));

            camera.setZoom(newZoom);
        });


        // Enable receiving scroll events
        glfwSetInputMode(window, GLFW_STICKY_MOUSE_BUTTONS, GLFW_TRUE);
        glfwSetInputMode(window, GLFW_STICKY_KEYS, GLFW_TRUE);
        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);

        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);


        // Make the window visible
        glfwShowWindow(window);

        GL.createCapabilities();
    }

    public void destroy() {
        imGuiGl3.dispose();
        imGuiGlfw.dispose();
        ImGui.destroyContext();
        Callbacks.glfwFreeCallbacks(window);
        glfwDestroyWindow(window);
        glfwTerminate();
    }


    public long getWindow() {
        return window;
    }

    public boolean shouldClose() {
        return glfwWindowShouldClose(window);
    }

    public int getWidth() {
        try (MemoryStack stack = stackPush()) {
            IntBuffer widthBuffer = stack.mallocInt(1);
            IntBuffer heightBuffer = stack.mallocInt(1);
            glfwGetFramebufferSize(window, widthBuffer, heightBuffer);
            return widthBuffer.get(0);
        }
    }

    public int getHeight() {
        try (MemoryStack stack = stackPush()) {
            IntBuffer widthBuffer = stack.mallocInt(1);
            IntBuffer heightBuffer = stack.mallocInt(1);
            glfwGetFramebufferSize(window, widthBuffer, heightBuffer);
            return heightBuffer.get(0);
        }
    }

    public void swapBuffers(){
        glfwSwapBuffers(window);
    }
}
