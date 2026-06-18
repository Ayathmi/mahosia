module mahosia {
    requires javafx.base;
    requires javafx.graphics;
    requires javafx.fxml;
    requires javafx.controls;
    requires org.lwjgl;
    requires org.lwjgl.opengl;
    requires org.lwjgl.glfw;
    requires org.lwjgl.stb;

    exports com.aliceprotocol.mahosia.mahoapp;
    exports com.aliceprotocol.mahosia.mahoui.mahocanvas;

    opens com.aliceprotocol.mahosia.mahoapp to javafx.fxml;
}