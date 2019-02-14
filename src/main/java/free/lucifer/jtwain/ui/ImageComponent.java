/*
 * Copyright 2018 (c) Denis Andreev (lucifer).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package free.lucifer.jtwain.ui;

import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.locks.ReentrantLock;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 *
 * @author Lucifer
 */
public class ImageComponent extends JPanel {

    private static final long serialVersionUID = 2779938619562855754L;

    private final Dimension size;
    private volatile BufferedImage image;
    private String fileName;

    private final ReentrantLock lock = new ReentrantLock();

    public BufferedImage getImage() {
        return image;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    public void openViewer() {
        File f = new File(fileName);
        Desktop desk = Desktop.getDesktop();
        try {
            desk.open(f);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void drawScaledImage(BufferedImage img) {
        if (image == null) {
            return;
        }

        lock.lock();
        try {
            Graphics2D g = (Graphics2D) image.getGraphics();
            g.drawImage(img, 0, 0, image.getWidth(), image.getHeight(), null);
        } finally {
            lock.unlock();
        }

        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                repaint();
            }
        });
    }

    public ImageComponent(int width, int height) {
        size = new Dimension(width, height);
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

    }

    @Override
    protected void paintComponent(Graphics g) {
        lock.lock();
        try {
            g.drawImage(image, 0, 0, null);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return size;
    }

    @Override
    public void setSize(Dimension d) {
        size.setSize(d);
        lock.lock();
        try {
            if (image != null) {
                image.flush();
            }
            image = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_RGB);

        } finally {
            lock.unlock();
        }
        super.setSize(size);
    }

    @Override
    public Dimension getSize() {
        return size;
    }

    public Graphics2D getImageGraphics() {
        return (Graphics2D) image.getGraphics();
    }

    public void updateImage() {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                repaint();
            }
        });
    }

    public void applyFile() {
        if (fileName != null) {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            this.addMouseListener(new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            openViewer();
                        }
                    });
                }

                @Override
                public void mousePressed(MouseEvent e) {

                }

                @Override
                public void mouseReleased(MouseEvent e) {

                }

                @Override
                public void mouseEntered(MouseEvent e) {

                }

                @Override
                public void mouseExited(MouseEvent e) {

                }
            });
        }
    }
}
