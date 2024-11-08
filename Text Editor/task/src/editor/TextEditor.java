package editor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextEditor extends JFrame {
    private List<int[]> searchIndices;
    private int currentIndex;

    public TextEditor() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("The first stage");

        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        fileMenu.setName("MenuFile");
        menuBar.add(fileMenu);

        JMenuItem loadMenuItem = new JMenuItem("Load");
        loadMenuItem.setName("MenuOpen");
        fileMenu.add(loadMenuItem);

        JMenuItem saveMenuItem = new JMenuItem("Save");
        saveMenuItem.setName("MenuSave");
        fileMenu.add(saveMenuItem);

        fileMenu.addSeparator();

        JMenuItem exitMenuItem = new JMenuItem("Exit");
        exitMenuItem.setName("MenuExit");
        fileMenu.add(exitMenuItem);

        JMenu searchMenu = new JMenu("Search");
        menuBar.add(searchMenu);
        searchMenu.setName("MenuSearch");

        JMenuItem startSearchMenuItem = new JMenuItem("Start search");
        startSearchMenuItem.setName("MenuStartSearch");
        searchMenu.add(startSearchMenuItem);

        JMenuItem previousSearchMenuItem = new JMenuItem("Previous search");
        previousSearchMenuItem.setName("MenuPreviousMatch");
        searchMenu.add(previousSearchMenuItem);

        JMenuItem nextSearchMenuItem = new JMenuItem("Next match");
        nextSearchMenuItem.setName("MenuNextMatch");
        searchMenu.add(nextSearchMenuItem);

        JMenuItem useRegExMenuItem = new JMenuItem("Use regular expressions");
        useRegExMenuItem.setName("MenuUseRegExp");
        searchMenu.add(useRegExMenuItem);

        setJMenuBar(menuBar);

        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT));

        JButton saveButton = new JButton(new ImageIcon(
                new ImageIcon("Text Editor/task/src/editor/images/save.png")
                        .getImage()
                        .getScaledInstance(30, 15, Image.SCALE_FAST))
        );
        saveButton.setName("SaveButton");
        panel.add(saveButton);

        JButton loadButton = new JButton(new ImageIcon(
                new ImageIcon("Text Editor/task/src/editor/images/load.png")
                        .getImage()
                        .getScaledInstance(30, 15, Image.SCALE_FAST))
        );
        loadButton.setName("OpenButton");
        panel.add(loadButton);

        JTextField searchInput = new JTextField();
        searchInput.setName("SearchField");
        searchInput.setPreferredSize(new Dimension(200, 25));
        panel.add(searchInput);

        JButton searchButton = new JButton(new ImageIcon(
                new ImageIcon("Text Editor/task/src/editor/images/search.png")
                        .getImage()
                        .getScaledInstance(30, 15, Image.SCALE_FAST))
        );
        searchButton.setName("StartSearchButton");
        panel.add(searchButton);

        JButton previousButton = new JButton(new ImageIcon(
                new ImageIcon("Text Editor/task/src/editor/images/previous.png")
                        .getImage()
                        .getScaledInstance(30, 15, Image.SCALE_FAST))
        );
        previousButton.setName("PreviousMatchButton");
        panel.add(previousButton);

        JButton nextButton = new JButton(new ImageIcon(
                new ImageIcon("Text Editor/task/src/editor/images/next.png")
                        .getImage()
                        .getScaledInstance(30, 15, Image.SCALE_FAST))
        );
        nextButton.setName("NextMatchButton");
        panel.add(nextButton);

        JCheckBox useRegexCheckBox = new JCheckBox("Use regex");
        useRegexCheckBox.setName("UseRegExCheckbox");
        panel.add(useRegexCheckBox);

        this.add(panel, BorderLayout.NORTH);

        JTextArea textArea = new JTextArea("", 20, 50);
        textArea.setName("TextArea");
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setName("ScrollPane");
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

        this.add(scrollPane, BorderLayout.CENTER);

        searchIndices = new ArrayList<>();
        currentIndex = -1;

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setName("FileChooser");
        fileChooser.setVisible(false);
        add(fileChooser, BorderLayout.SOUTH);

        ActionListener saveAction = actionEvent -> {
            fileChooser.setVisible(true);
            int option = fileChooser.showSaveDialog(null);
            if (option == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                String content = textArea.getText();
                try (FileWriter writer = new FileWriter(file)) {
                    writer.write(content);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        };

        ActionListener loadAction = actionEvent -> {
            fileChooser.setVisible(true);
            int option = fileChooser.showOpenDialog(null);
            if (option == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    textArea.read(reader, null);
                } catch (IOException ex) {
                    textArea.setText("");
                }
            }
        };

        ActionListener searchAction = actionEvent -> {
            new Thread(() -> {
                String searchText = searchInput.getText();
                String textContent = textArea.getText();
                textArea.getHighlighter().removeAllHighlights();
                searchIndices.clear();
                currentIndex = -1;

                if (!searchText.isEmpty()) {
                    if (useRegexCheckBox.isSelected()) {
                        try {
                            Pattern pattern = Pattern.compile(searchText);
                            Matcher matcher = pattern.matcher(textContent);
                            while (matcher.find()) {
                                searchIndices.add(new int[]{matcher.start(), matcher.end()});
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        int index = textContent.indexOf(searchText);
                        while (index >= 0) {
                            searchIndices.add(new int[]{index, index + searchText.length()});
                            index = textContent.indexOf(searchText, index + searchText.length());
                        }
                    }
                    if (!searchIndices.isEmpty()) {
                        currentIndex = 0;
                        highlightCurrentMatch(textArea);
                    }
                }
            }).start();
        };

        ActionListener nextAction = actionEvent -> {
            if (!searchIndices.isEmpty()) {
                if (currentIndex < searchIndices.size() - 1) {
                    currentIndex++;
                    highlightCurrentMatch(textArea);
                } else {
                    currentIndex = 0;
                    highlightCurrentMatch(textArea);
                }
            }
        };

        ActionListener previousAction = actionEvent -> {
            if (!searchIndices.isEmpty()) {
                if (currentIndex > 0) {
                    currentIndex--;
                    highlightCurrentMatch(textArea);
                } else {
                    currentIndex = searchIndices.size() - 1;
                    highlightCurrentMatch(textArea);
                }
            }
        };

        saveButton.addActionListener(saveAction);
        loadButton.addActionListener(loadAction);
        saveMenuItem.addActionListener(saveAction);
        loadMenuItem.addActionListener(loadAction);

        searchButton.addActionListener(searchAction);
        startSearchMenuItem.addActionListener(searchAction);
        nextButton.addActionListener(nextAction);
        nextSearchMenuItem.addActionListener(nextAction);
        previousButton.addActionListener(previousAction);
        previousSearchMenuItem.addActionListener(previousAction);
        useRegExMenuItem.addActionListener(actionEvent -> useRegexCheckBox.setSelected(!useRegexCheckBox.isSelected()));

        exitMenuItem.addActionListener(e -> {
            dispose();
            System.exit(0);
        });

        pack();
        setVisible(true);
    }

    private void highlightCurrentMatch(JTextArea textArea) {
        textArea.getHighlighter().removeAllHighlights();
        if (currentIndex >= 0 && currentIndex < searchIndices.size()) {
            int[] indices = searchIndices.get(currentIndex);
            textArea.select(indices[0], indices[1]);
            textArea.grabFocus();
        }
    }
}
