package org.example.main;

import imgui.ImGuiIO;
import imgui.ImVec2;
import imgui.flag.*;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import imgui.type.ImBoolean;
import org.example.UI.Components;
import org.example.camera.Camera;
import org.example.opengl.GLUtils;
import org.example.window.WindowManager;
import org.lwjgl.Version;
import org.lwjgl.glfw.GLFW;
import imgui.ImGui;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.stackPush;

public class Main {
    private WindowManager windowManager;
    private GLUtils glUtils;
    private String windowTitle;
    private boolean isSimulationRunning = false; // Add this line
    private boolean isSimulationTraining = false; // Add this line
    private Camera camera;
    private ImGuiImplGlfw imGuiGlfw;
    private ImGuiImplGl3 imGuiGl3;

    private void run() {
        System.out.println("Hello LWJGL " + Version.getVersion() + "!");
        init();
        loop();
        cleanUp();

    }

    private void init() {
        ImGui.createContext();
        imGuiGlfw = new ImGuiImplGlfw();
        imGuiGl3 = new ImGuiImplGl3();
        windowTitle = "Hello World";
        camera = new Camera();
        windowManager = new WindowManager(camera);
        windowManager.initWindow(1920, 1080, windowTitle);
        glUtils = new GLUtils(windowManager);
        glUtils.initOpenGL();
        imGuiGlfw.init(windowManager.getWindow(), true);
        imGuiGl3.init();
        ImGuiIO io = ImGui.getIO();
        io.setConfigFlags(io.getConfigFlags() | ImGuiConfigFlags.DockingEnable);

    }

    private void loop() {
        double lastTime = glfwGetTime();
        int frames = 0;

        glfwMakeContextCurrent(windowManager.getWindow()); // Set the GLFW window context as current

        while (!windowManager.shouldClose()) {
            double currentTime = glfwGetTime();
            frames++;
            if (currentTime - lastTime >= 1.0) {
                // Print out the frames per second count
                glfwSetWindowTitle(windowManager.getWindow(), "EvoSim - FPS: " + frames);

                // Reset the counter and timer
                frames = 0;
                lastTime += 1.0;
            }

            imGuiGlfw.newFrame();
            ImGui.newFrame();


            // ALL IMGUI STUFF GO HERE //

            setupDockspace();
            ImGui.showDemoWindow();
            ImGui.showMetricsWindow();


            // Start ImGui frame
            glfwMakeContextCurrent(windowManager.getWindow()); // Set the GLFW window context as current

            // Update and render ImGui viewports
            if (ImGui.getIO().hasConfigFlags(ImGuiConfigFlags.ViewportsEnable)) {
                GLFW.glfwMakeContextCurrent(GLFW.glfwGetCurrentContext());
                ImGui.updatePlatformWindows();
                ImGui.renderPlatformWindowsDefault();
                GLFW.glfwMakeContextCurrent(windowManager.getWindow());
            }

            // Get the framebuffer size
            try (MemoryStack stack = stackPush()) {
                IntBuffer widthBuffer = stack.mallocInt(1);
                IntBuffer heightBuffer = stack.mallocInt(1);
                glfwGetFramebufferSize(windowManager.getWindow(), widthBuffer, heightBuffer);
                int width = widthBuffer.get(0);
                int height = heightBuffer.get(0);

                // Set the viewport
                glViewport(0, 0, width, height);

                // Adjust the camera's projection
//                camera.setPosition(0.0f, 0.0f); // Reset the position
                float aspectRatio = (float) width / height;
                float cameraHeight = 10.0f; // Adjust this value as needed
                float cameraWidth = cameraHeight * aspectRatio;
                camera.setProjection(-cameraWidth / 2.0f, cameraWidth / 2.0f, -cameraHeight / 2.0f, cameraHeight / 2.0f, camera.getViewportWidth(), camera.getViewportHeight());
            }



            ImGui.render();
            imGuiGl3.renderDrawData(ImGui.getDrawData());

            camera.applyTransformations();
            // Swap buffers and poll events
            windowManager.swapBuffers();
            glfwPollEvents();
        }

        ImGui.destroyContext();

        // Cleanup GLFW
        glfwTerminate();
    }

    private void setupDockspace() {
        int windowFlags = ImGuiWindowFlags.MenuBar | ImGuiWindowFlags.NoDocking;

        ImGui.setNextWindowPos(0.0f, 0.0f, ImGuiCond.Always);
        ImGui.setNextWindowSize(windowManager.getWidth(), windowManager.getHeight());
        ImGui.pushStyleVar(ImGuiStyleVar.WindowRounding, 0.0f);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowBorderSize, 0.0f);
        windowFlags |= ImGuiWindowFlags.NoTitleBar | ImGuiWindowFlags.NoCollapse |
                ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoMove |
                ImGuiWindowFlags.NoBringToFrontOnFocus | ImGuiWindowFlags.NoNavFocus;

        ImGui.begin("Dockspace", new ImBoolean(true), windowFlags);
        ImGui.popStyleVar(2);

        // Dockspace
        ImGui.dockSpace(ImGui.getID("Dockspace"));
        ImGui.end();
    }

    private void cleanUp() {
        windowManager.destroy();
    }


    public static void main(String[] args) {
        new Main().run();
    }
}
