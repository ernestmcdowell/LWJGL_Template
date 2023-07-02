package org.example.UI;

import imgui.ImGui;

public class Components {

    public static void createButton(String label, Runnable action) {
        if (ImGui.button(label)) {
            action.run();
        }
    }
}
