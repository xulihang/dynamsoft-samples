module com.dynamsoft.BarcodeReader {
    requires javafx.controls;
    requires javafx.fxml;
	requires uk.co.caprica.vlcj;
	requires uk.co.caprica.vlcj.javafx;
	requires com.sun.jna;
	requires transitive javafx.graphics;
	requires javafx.swing;
	requires transitive dbr;

    opens com.dynamsoft.BarcodeReader to javafx.fxml;
    exports com.dynamsoft.BarcodeReader;
}