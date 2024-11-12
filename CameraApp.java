import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

public class CameraApp {
    private JFrame frame;
    private JLabel imageLabel;
    private VideoCapture camera;
    private boolean isCameraActive = false;

    public CameraApp() {
	System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        setupGUI();
    }

    private void setupGUI() {
        frame = new JFrame("Camera Application");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(640, 480);

        JPanel panel = new JPanel(new BorderLayout());
        JButton button = new JButton("Activate Camera");
        button.addActionListener(e -> toggleCamera());

        imageLabel = new JLabel();
        panel.add(button, BorderLayout.NORTH);
        panel.add(imageLabel, BorderLayout.CENTER);

        frame.add(panel);
        frame.setVisible(true);
    }

    private void toggleCamera() {
        if (isCameraActive) {
            deactivateCamera();
        } else {
            activateCamera();
        }
    }

private void activateCamera() {
    camera = new VideoCapture(1);
    camera.set(Videoio.CAP_PROP_FRAME_WIDTH, 640);
    camera.set(Videoio.CAP_PROP_FRAME_HEIGHT, 480);

    if (camera.isOpened()) {
        System.out.println("Camera opened successfully.");
        isCameraActive = true;
        new Thread(this::updateCameraView).start();
    } else {
        System.out.println("Could not open camera.");
        JOptionPane.showMessageDialog(frame, "Could not open camera!");
    }
}

    private void deactivateCamera() {
        if (camera != null && camera.isOpened()) {
            isCameraActive = false;
            camera.release();
            imageLabel.setIcon(null);  // Clear the image display
        }
    }

    private void updateCameraView() {
        Mat frameMatrix = new Mat();
        while (isCameraActive) {
            if (camera.read(frameMatrix)) {
                Image image = matToBufferedImage(frameMatrix);
                if (image != null) {
                    ImageIcon icon = new ImageIcon(image);
                    imageLabel.setIcon(icon);
                }
            }
        }
    }

    private BufferedImage matToBufferedImage(Mat mat) {
        int type = BufferedImage.TYPE_3BYTE_BGR;
        if (mat.channels() == 1) {
            type = BufferedImage.TYPE_BYTE_GRAY;
        }
        
        int bufferSize = mat.channels() * mat.cols() * mat.rows();
        byte[] buffer = new byte[bufferSize];
        mat.get(0, 0, buffer);
        BufferedImage image = new BufferedImage(mat.cols(), mat.rows(), type);
        final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(buffer, 0, targetPixels, 0, buffer.length);
        
        return image;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(CameraApp::new);
    }
}
