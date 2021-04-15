package com.dynamsoft.BarcodeReader;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.dynamsoft.dbr.BarcodeReader;
import com.dynamsoft.dbr.BarcodeReaderException;
import com.dynamsoft.dbr.EnumConflictMode;
import com.dynamsoft.dbr.EnumResultCoordinateType;
import com.dynamsoft.dbr.Point;
import com.dynamsoft.dbr.PublicRuntimeSettings;
import com.dynamsoft.dbr.TextResult;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class MainController implements Initializable  {
	private Image currentImg;
    private BarcodeReader br;
    @FXML private Label timeLbl;
	@FXML private Button readBtn;
	@FXML private AnchorPane root;
    @FXML private TextArea resultTA;
    @FXML private TextArea templateTA;
    @FXML private Canvas cv;
    @FXML private ImageView iv;
    @FXML private TextField mrlTextField;
    @FXML private TextField optionsTextField;
    @FXML private Button readVideoStreamBtn;
	private VlcjJavaFxApplication vlcj;
	private ScheduledExecutorService timer;
	private Boolean found;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
		try {
			br = new BarcodeReader("t0068NQAAAJYBYfmF8T9A4FyRD4gw30Kx9VtWdhk4M7K8OgvmtsAySfNNO0Fi3uIBlvoHUBWLJB4MQ1bUt9k8v+TrrG1cXio=");
		} catch (BarcodeReaderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		vlcj = new VlcjJavaFxApplication();
    }
    
    public void showVideoBtn_MouseClicked(Event event) throws Exception {
    	if (vlcj.stage==null) {
    		vlcj.start(new Stage());	
    	} else {
    		vlcj.stage.show();    		
    	}
    	
        if (optionsTextField.getText()!="") {
        	System.out.println("use options");
        	System.out.println(optionsTextField.getText());        	
        	vlcj.play(mrlTextField.getText(),getOptions(optionsTextField.getText()));
        } else {
        	vlcj.play(mrlTextField.getText());
        }
        
    }
    
    private String[] getOptions(String options) {    	
    	String[] items=optionsTextField.getText().split(" :");
    	for (int i=0;i<items.length;i++) {
    		items[i]=":"+items[i].trim();   
    		System.out.println(items[i]);
    	}
    	// :dshow-vdev=Founder Camera :dshow-adev=none  :live-caching=300
    	return items;
    }
    
    public void captureBtn_MouseClicked(Event event) throws Exception {
    	if (vlcj.stage!=null) {
            currentImg=getCurrentFrame();
            redrawImage(currentImg);
    	}
    }
    
    public void cv_MouseClicked(Event event) {
        System.out.println("cv Clicked!");
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open Barcode File");
            File imgFile = fileChooser.showOpenDialog(Main.getPrimaryStage());
            currentImg =  new Image(imgFile.toURI().toString());   
            System.out.println(currentImg.getWidth());
            redrawImage(currentImg);
        } catch (Exception e) {
        	e.printStackTrace();
        }
    }
    
    public Image getCurrentFrame() {
    	return vlcj.getImageView().getImage();
    }
    
    public void updateRuntimeSettings() throws BarcodeReaderException {
    	String template = templateTA.getText();        	    	
    	try {
        	br.initRuntimeSettingsWithString(template,EnumConflictMode.CM_OVERWRITE);   
    	}  catch (Exception e) {
			br.resetRuntimeSettings();
    	}    	
    	unifyCoordinateReturnType();
    }
    
    public void readBtn_MouseClicked(Event event) throws Exception {
    	found=false;
    	updateRuntimeSettings();
    	System.out.println("Button Clicked!");    
    	if (currentImg==null) {
    		System.out.println("no img!");   
    		return;
    	}
    	decodeImg(currentImg);
    }
    
    public void readVideoStreamBtn_MouseClicked(Event event) throws Exception {
    	found=false;
    	updateRuntimeSettings();
    	if (readVideoStreamBtn.getText()!="Stop") {
        	readVideoStreamBtn.setText("Stop");
        	decodeVideoStream();    		
    	} else {
    		stopReadingVideoStream();
    	}
    }
    
    private void stopReadingVideoStream() {
    	if (this.timer!=null) {
			this.timer.shutdownNow();
			this.timer=null;
			readVideoStreamBtn.setText("Read Video Stream");
		}
    }
    
    private void decodeVideoStream() throws BarcodeReaderException, IOException, InterruptedException {
    	DecodingThread dt = new DecodingThread(this);
    	this.timer = Executors.newSingleThreadScheduledExecutor();
    	this.timer.scheduleAtFixedRate(dt, 0, 100, TimeUnit.MILLISECONDS);
    }
    
    private void decodeImg(Image img) throws BarcodeReaderException, IOException, InterruptedException {     
    	redrawImage(img);
    	DecodingThread dt = new DecodingThread(img, this);
        Thread t = new Thread(dt);
        t.start();
    }
    
    public void showResults(Image img, TextResult[] results, Long timeElapsed) {
    	if (found==true) {
    		return;
    	}
    	redrawImage(img);
    	if (results.length>0) {
    		stopReadingVideoStream();    
    		found=true;    				
    		System.out.println("found");
    	}	

    	StringBuilder sb = new StringBuilder(); 
    	int index=0;
    	for (TextResult result:results) {
    		index=index+1;
    		overlayCode(result);
    		sb.append(index);
        	sb.append("\n");
        	sb.append("Type: ");
        	sb.append(result.barcodeFormatString);
        	sb.append("\n");
        	sb.append("Text: ");
        	sb.append(result.barcodeText);        	
        	sb.append("\n\n");        	
    	}
    	resultTA.setText(sb.toString());
    	StringBuilder timeSb = new StringBuilder();
    	timeSb.append("Total: ");
    	timeSb.append(timeElapsed);
    	timeSb.append("ms");
    	timeLbl.setText(timeSb.toString());
    }
    
    class DecodingThread implements Runnable {
    	private TextResult[] results;
    	private Image img;
    	private MainController callback;
    	
    	public DecodingThread (Image img, MainController callback)
        {
            this.img = img;
            this.callback = callback;
        }
    	
    	public DecodingThread (MainController callback)
        {
            this.callback = callback;
        }
    	
        @Override
        public void run() {
        	Date startDate = new Date();
        	Long startTime = startDate.getTime();   
        	
        	if (img==null) {
        		img=callback.getCurrentFrame();
        	}
        	
        	try {
        		results=br.decodeBufferedImage(SwingFXUtils.fromFXImage(img,null),"");
			} catch (IOException e) {
				e.printStackTrace();
			} catch (BarcodeReaderException e) {
				e.printStackTrace();
			}
        	Long endTime = null;
        	Date endDate = new Date();
        	endTime = endDate.getTime();
        	Long timeElapsed = endTime-startTime;
        	Platform.runLater(new Runnable() {
        	    @Override
        	    public void run() {
        	    	callback.showResults(img, results, timeElapsed);
        	    }
        	});        	
        }
    }
    
    private void redrawImage(Image img) {
        cv.setWidth(img.getWidth());
        cv.setHeight(img.getHeight());
        GraphicsContext gc = cv.getGraphicsContext2D();
        gc.drawImage(img, 0, 0, cv.getWidth(), cv.getHeight());
    }

    private void overlayCode(TextResult result) {
    	GraphicsContext gc=cv.getGraphicsContext2D();

    	List<Point> points= new ArrayList<Point>();
    	for (Point point : result.localizationResult.resultPoints) {
    		points.add(point);
    	}
    	points.add(result.localizationResult.resultPoints[0]);
    	
		gc.setStroke(Color.RED);
		gc.setLineWidth(5);
		gc.beginPath();
		
		for (int i = 0;i<points.size()-1;i++) {
			Point point=points.get(i);
			Point nextPoint=points.get(i+1);
			gc.moveTo(point.x, point.y);
			gc.lineTo(nextPoint.x, nextPoint.y);			
		}
		gc.closePath();
		gc.stroke();
    }
    
    private void unifyCoordinateReturnType() {
		PublicRuntimeSettings settings;
		try {
			settings = br.getRuntimeSettings();
			settings.resultCoordinateType=EnumResultCoordinateType.RCT_PIXEL;			
			br.updateRuntimeSettings(settings);
		} catch (BarcodeReaderException e) {
			e.printStackTrace();
		}
    }
}
