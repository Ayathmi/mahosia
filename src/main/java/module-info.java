module mahosia {
    requires javafx.base;
    requires javafx.graphics;
    requires javafx.fxml;
    requires javafx.controls;
    requires org.lwjgl;
    requires org.lwjgl.opengl;
    requires org.lwjgl.glfw;
    requires org.lwjgl.stb;
    requires java.sql;

    exports com.aliceprotocol.mahosia.mahoapp;
    exports com.aliceprotocol.mahosia.mahoui.mahocanvas;
    exports com.aliceprotocol.mahosia.mahomodel;
    exports com.aliceprotocol.mahosia.mahovm;

    opens com.aliceprotocol.mahosia.mahoapp to javafx.fxml;
    opens com.aliceprotocol.mahosia.mahovm to javafx.fxml;
}