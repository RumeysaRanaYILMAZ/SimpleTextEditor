import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.*;
import javax.swing.text.*;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.KeyStroke;
import static javax.swing.WindowConstants.EXIT_ON_CLOSE;


class TextEditor extends JFrame {
    private JTextArea textArea = new JTextArea(50, 150);
    private Document doc = textArea.getDocument();
    private int fontSize = 14;
    private int fontDelta = 2;
    private JFileChooser fileDialog = new JFileChooser(System.getProperty("user.dir"));
    private String curFile = "Rana's Text Editor";
    private boolean hasChanged = false;
    private boolean cmdPressed = false;
    
    
    //fix spell mistakes like default edfault
    public  static String SearchAndfix(String word) throws FileNotFoundException{
    
        Scanner txtscan = new Scanner(new File("words.txt"));
        word=word.toLowerCase();
        

        while(txtscan.hasNextLine()){
            String str = txtscan.nextLine();
            str=str.toLowerCase();
            
            if(str.indexOf(word) != -1){
                return word;
            }
            for(int i=0;i<word.length()-1;i++)
            {
                
                char[] Chars = word.toCharArray();
                char tmpc=Chars[i];
                Chars[i] = Chars[i+1];
                Chars[i+1]=tmpc;
                String temp= String.valueOf(Chars);
                
                if(str.indexOf(temp) != -1){
                    return temp;
                }
            
            
            }
        }
        
    
        return word+"!";
    }
    
    
    private KeyListener listener = new KeyListener() {
        private final Set<Integer> pressed = new HashSet<Integer>();
        @Override
        public void keyTyped(KeyEvent e) {

        }
            /* Key KeyCodes
               cmd 157
               -    45
               +    61
               o    79
               s    83 */
        @Override
        public void keyPressed(KeyEvent e) {

            hasChanged = true;
            Save.setEnabled(true);
            SaveAs.setEnabled(true);
            pressed.add(e.getKeyCode());

            if (e.getKeyCode() == 157) {
                    cmdPressed = true;
            }

            // two key combo press containing cmd
            if (pressed.size() == 2 && pressed.contains(157)) {
                clearLastKey();
                // s
                if (pressed.contains(83)) {
                    genericSave();
                }
                // o
                if (pressed.contains(79)) {
                    openFile();
                }
                // +
                if (pressed.contains(61)) {
                    changeFontSize(fontDelta);
                }
                // -
                if (pressed.contains(45)) {
                    changeFontSize(-fontDelta);
                }

            }
            // repeated (+ OR -) while cmd is held down
            else if (cmdPressed) {
                // +
                if (pressed.contains(61)) {
                    clearLastKey();
                    changeFontSize(fontDelta);
                }
                // -
                if (pressed.contains(45)) {
                    clearLastKey();
                    changeFontSize(-fontDelta);
                }
            }
        }
        

        @Override
        public void keyReleased(KeyEvent e) {
            if (e.getKeyCode() == 157) {
                cmdPressed = false;
            }
            pressed.clear();
        }
    };


    // KeyListener Helpers
    private void clearLastKey() {
        try {
            // clear the key pressed last
            doc.remove(doc.getLength() - 1, 0);
        }
        catch (BadLocationException ex) {
        }
    }
    private void changeFontSize(int delta) {
        fontSize += delta;
        textArea.setFont(new Font("Monospaced", Font.PLAIN, fontSize));
    }

    // Actions
    private Action Open = new AbstractAction("Open") {
        @Override
        public void actionPerformed(ActionEvent e) {
            openFile();
        }
    };
    private Action Save = new AbstractAction("Save") {
        @Override
        public void actionPerformed(ActionEvent e) {
            saveFile(curFile);
        }
    };
    private Action SaveAs = new AbstractAction("Save as...") {
        @Override
        public void actionPerformed(ActionEvent e) {
            saveFileAs();
        }
    };
    private Action Quit = new AbstractAction("Quit") {
        @Override
        public void actionPerformed(ActionEvent e) {
            saveOld();
            System.exit(0);
        }
    };
    private Action New = new AbstractAction("New") {
        @Override
        public void actionPerformed(ActionEvent e) {
            saveOld();
            textArea.setText("");
            curFile = "Text Editor";
            setTitle(curFile);
            hasChanged = false;
            Save.setEnabled(false);
            SaveAs.setEnabled(false);
        }
    };
    
    private Action Find = new AbstractAction("Find") {
        @Override
        public void actionPerformed(ActionEvent e) {
             
            new findClass(textArea);
        
        }
    };
    private Action FindMistakes = new AbstractAction("FindMistakes") {
        @Override
        public void actionPerformed(ActionEvent e)  {
             
            
            String txt=textArea.getText();
            if(txt.equals("") || txt==null){
              textArea.setText("Please enter your text");
              return;
            }
            
            String fixed="";
           ArrayList<String> list = new ArrayList<String>(Arrays.asList(txt.split(" ")));
           for(int i=0;i<list.size();i++){
                try {
                    String tm=SearchAndfix(list.get(i));
                    fixed+=" "+tm;
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(TextEditor.class.getName()).log(Level.SEVERE, null, ex);
                }
           }
           textArea.setText(fixed);
        
        }
    };
    private ActionMap actionMap = textArea.getActionMap();
   
    private Action Cut = actionMap.get(DefaultEditorKit.cutAction);
    private Action Copy = actionMap.get(DefaultEditorKit.copyAction);
    private Action Paste = actionMap.get(DefaultEditorKit.pasteAction);
    private Action Undo = actionMap.get(DefaultEditorKit.deletePrevCharAction);
    

    private void genericSave() {
        File f = new File(curFile);
        if(hasChanged && f.exists() && !f.isDirectory()) {
            saveFile(curFile);
        }
        else {
            saveFileAs();
        }
    }

    private void saveFileAs() {
        if(fileDialog.showSaveDialog(null)==JFileChooser.APPROVE_OPTION) {
            saveFile(fileDialog.getSelectedFile().getAbsolutePath());
            curFile = fileDialog.getSelectedFile().getAbsolutePath();
            this.setTitle(curFile);
        }
    }

    private void saveOld() {
        if (hasChanged) {
            if (JOptionPane.showConfirmDialog(
                    this, "Would you like to save this file?", "Save", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                saveFile(curFile);
            }
        }
    }

    private void openFile() {
        if (hasChanged) {
            saveOld();
        }
        if (fileDialog.showOpenDialog(null)==JFileChooser.APPROVE_OPTION) {
            readInFile(fileDialog.getSelectedFile().getAbsolutePath());
        }
        SaveAs.setEnabled(true);
    }

    private void readInFile(String fileName) {
        FileReader reader = null;
        try {
            reader =  new FileReader(fileName);
            textArea.read(reader, null);
            curFile = fileName;
            setTitle(curFile);
        }
        catch (IOException e) {
            Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(this, "File not found!");
        }
        finally {
            if (reader != null) {
                try {
                    reader.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void saveFile(String fileName) {
        if (hasChanged) {
            FileWriter writer = null;

            try {
                writer = new FileWriter(fileName);
                textArea.write(writer);
                curFile = fileName;
                hasChanged = false;
                Save.setEnabled(false);
            } catch (IOException e) {
            } finally {
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (IOException e) {

                    }
                }
            }
        }
    }

    private void closeListener() {
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                if (hasChanged) {
                    int confirmed = JOptionPane.showConfirmDialog(null,
                            "Would you like to save before exit?", "Unsaved changes!",
                            JOptionPane.YES_NO_OPTION);

                    if (confirmed == JOptionPane.YES_OPTION) {
                        if (curFile == "untitled" && !hasChanged) {
                            saveFileAs();
                        }
                        else {
                            saveFile(curFile);
                        }
                    }

                    else {
                        System.exit(0);
                    }
                }
                else {
                    System.exit(0);
                }
            }
        });
    }

    public TextEditor() {
        
        this.setPreferredSize(new Dimension(800, 800));
      

        
        // font
        
        textArea.setFont(new Font("Arial", Font.PLAIN, fontSize));
        textArea.setBackground(new Color(255, 115, 133));
        // wrap those words dude
        textArea.setLineWrap(true);
        // scroll bars
        JScrollPane scroller = new JScrollPane(
                textArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        add(scroller, BorderLayout.CENTER);
        // menu bar
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        // menu tabs
        JMenu fileMenu = new JMenu("File");
        JMenu editMenu = new JMenu("Edit");
        menuBar.add(fileMenu);
        menuBar.add(editMenu);

        // Menu tabs

        // file
        fileMenu.add(New);
        fileMenu.add(Open);
        fileMenu.add(Save);
        fileMenu.add(Quit);
        fileMenu.add(SaveAs);
        fileMenu.add(Find);
        fileMenu.add(FindMistakes);
        fileMenu.addSeparator();

        for (int i=0; i<4; ++i) {
            fileMenu.getItem(i).setIcon(null);
        }

        // edit
        editMenu.add(Cut);
        editMenu.add(Copy);
        editMenu.add(Paste);
        editMenu.add(Undo);
        

        editMenu.getItem(0).setText("Cut");
        editMenu.getItem(1).setText("Copy");
        editMenu.getItem(2).setText("Paste");
        editMenu.getItem(3).setText("Undo");

        //

        JToolBar toolBar = new JToolBar();
        add(toolBar, BorderLayout.NORTH);
        toolBar.add(New);
        toolBar.add(Open);
        toolBar.add(Save);
        toolBar.addSeparator();


        JButton cut = toolBar.add(Cut);
        JButton copy = toolBar.add(Copy);
        JButton paste = toolBar.add(Paste);
        
        
        
      
        JButton find = toolBar.add(Find);
        JButton findmistakes = toolBar.add(FindMistakes);
        
               

        cut.setText("CUT");
        copy.setText("COPY");
        paste.setText("PASTE");

        Save.setEnabled(false);
        SaveAs.setEnabled(false);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack();
        textArea.addKeyListener(listener);
        setTitle(curFile);
        setVisible(true);
        closeListener();
    }

}
public class Main {
    public static void main(String[] args) {
        KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
        new TextEditor();
    }
}
