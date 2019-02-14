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
package free.lucifer.jtwain;

import free.lucifer.jtwain.ui.ScannerFrame;

/**
 *
 * @author lucifer
 */
public class App {

    public static void main(String[] args) throws Exception {

        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                System.out.println(info.getName());
                if ("Windows".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ScannerFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        Twain.init();

        ScannerFrame sf = new ScannerFrame();
        sf.pack();
        sf.setVisible(true);
        
//        +1 Example        
//        Source source = SourceManager.instance().getSources().get(0);
//
//        source.setSystemUI(false);
//        source.setAutoDocumentFeeder(true);
//        source.setColor(Source.ColorMode.GRAYSCALE);
//        source.setDpi(300);
//        System.out.println(source.scan());
//
//        SourceManager.instance().freeResources();

    }

}
