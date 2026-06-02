package com.ruoyi.autotest.gui.util;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

/**
 * CSV 用例编辑窗口，负责维护部门与岗位模块的数据组合测试用例。
 */
public final class CsvCaseEditor {

    public static final Path EDITABLE_CASE_FILE = Paths.get("testdata", "deptpost", "post_add_cases.csv");
    public static final Path TEMPLATE_CASE_FILE = Paths.get(
        "src", "test", "resources", "testdata", "deptpost", "post_add_cases.csv");
    public static final String CLASSPATH_TEMPLATE = "testdata/deptpost/post_add_cases.csv";

    private static final String[] COLUMNS = {
        "caseId", "postName", "postCode", "postSort", "status", "expected"
    };

    private static final String[][] DEFAULT_ROWS = {
        {"01", "HHJ_23013100_DP_Normal_01", "HHJ_23013100_CODE_A01", "1", "0", "success"},
        {"02", "HHJ_23013100_DP_Normal_02", "HHJ_23013100_CODE_A02", "2", "0", "success"},
        {"03", "HHJ_23013100_DP_Normal_03", "HHJ_23013100_CODE_A03", "3", "0", "success"},
        {"04", "HHJ_23013100_DP_Normal_04", "HHJ_23013100_CODE_A04", "4", "0", "success"},
        {"05", "HHJ_23013100_DP_Normal_05", "HHJ_23013100_CODE_A05", "5", "0", "success"},
        {"06", "HHJ_23013100_DP_Disabled_06", "HHJ_23013100_CODE_B06", "6", "1", "success"},
        {"07", "HHJ_23013100_DP_Disabled_07", "HHJ_23013100_CODE_B07", "7", "1", "success"},
        {"08", "HHJ_23013100_DP_Disabled_08", "HHJ_23013100_CODE_B08", "8", "1", "success"},
        {"09", "HHJ_23013100_DP_Disabled_09", "HHJ_23013100_CODE_B09", "9", "1", "success"},
        {"10", "HHJ_23013100_DP_Disabled_10", "HHJ_23013100_CODE_B10", "10", "1", "success"},
        {"11", "HHJ_23013100_Post123_11", "HHJ_23013100_CODE_NUM_11", "11", "0", "success"},
        {"12", "HHJ_23013100_Post456_12", "HHJ_23013100_CODE_NUM_12", "12", "0", "success"},
        {"13", "HHJ_23013100_Name_LongAlpha", "HHJ_23013100_CODE_LONG_A", "13", "0", "success"},
        {"14", "HHJ_23013100_Name_LongBeta", "HHJ_23013100_CODE_LONG_B", "14", "0", "success"},
        {"15", "HHJ_23013100_Name_Under_15", "HHJ_23013100_CODE_UNDER_15", "15", "0", "success"},
        {"16", "HHJ_23013100_Name_Under_16", "HHJ_23013100_CODE_UNDER_16", "16", "1", "success"},
        {"17", "HHJ_23013100_Sort_Min_17", "HHJ_23013100_CODE_SORT_17", "1", "1", "success"},
        {"18", "HHJ_23013100_Sort_Mid_18", "HHJ_23013100_CODE_SORT_18", "50", "1", "success"},
        {"19", "HHJ_23013100_Sort_Max_19", "HHJ_23013100_CODE_SORT_19", "99", "1", "success"},
        {"20", "HHJ_23013100_Mixed_A20", "HHJ_23013100_CODE_MIX_A20", "20", "1", "success"},
        {"21", "HHJ_23013100_Mixed_B21", "HHJ_23013100_CODE_MIX_B21", "21", "0", "success"},
        {"22", "HHJ_23013100_Mixed_C22", "HHJ_23013100_CODE_MIX_C22", "22", "0", "success"},
        {"23", "HHJ_23013100_Mixed_D23", "HHJ_23013100_CODE_MIX_D23", "23", "1", "success"},
        {"24", "HHJ_23013100_Boundary_24", "HHJ_23013100_CODE_BOUND_24", "24", "1", "success"},
        {"25", "HHJ_23013100_Boundary_25", "HHJ_23013100_CODE_BOUND_25", "25", "0", "success"}
    };

    private CsvCaseEditor() {
    }

    public static void showDialog(Window owner, Consumer<String> logger) {
        CaseEditorDialog dialog = new CaseEditorDialog(owner, logger);
        dialog.setVisible(true);
    }

    public static Path ensureEditableCaseFile() throws IOException {
        if (Files.exists(EDITABLE_CASE_FILE)) {
            return EDITABLE_CASE_FILE;
        }

        Files.createDirectories(EDITABLE_CASE_FILE.getParent());
        if (Files.exists(TEMPLATE_CASE_FILE)) {
            Files.copy(TEMPLATE_CASE_FILE, EDITABLE_CASE_FILE, StandardCopyOption.REPLACE_EXISTING);
            return EDITABLE_CASE_FILE;
        }

        try (InputStream input = CsvCaseEditor.class.getClassLoader().getResourceAsStream(CLASSPATH_TEMPLATE)) {
            if (input != null) {
                Files.copy(input, EDITABLE_CASE_FILE, StandardCopyOption.REPLACE_EXISTING);
                return EDITABLE_CASE_FILE;
            }
        }

        writeRows(EDITABLE_CASE_FILE, defaultRows());
        return EDITABLE_CASE_FILE;
    }

    private static List<String[]> readRows(Path path) throws IOException {
        List<String[]> rows = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            int lineNo = 0;
            while ((line = reader.readLine()) != null) {
                lineNo++;
                if (line.trim().isEmpty()) {
                    continue;
                }
                List<String> columns = parseCsvLine(line);
                if (lineNo == 1 && isHeader(columns)) {
                    continue;
                }
                rows.add(toSixColumns(columns));
            }
        }
        return rows;
    }

    private static void writeRows(Path path, List<String[]> rows) throws IOException {
        Files.createDirectories(path.getParent());
        List<String> lines = new ArrayList<>();
        lines.add(String.join(",", COLUMNS));
        for (String[] row : rows) {
            lines.add(toCsvLine(row));
        }
        Files.write(path, lines, StandardCharsets.UTF_8);
    }

    private static List<String[]> defaultRows() {
        List<String[]> rows = new ArrayList<>();
        for (String[] row : DEFAULT_ROWS) {
            rows.add(row.clone());
        }
        return rows;
    }

    private static List<String> parseCsvLine(String line) {
        List<String> columns = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (ch == ',' && !inQuotes) {
                columns.add(current.toString());
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }
        columns.add(current.toString());
        return columns;
    }

    private static boolean isHeader(List<String> columns) {
        if (columns.size() < COLUMNS.length) {
            return false;
        }
        for (int i = 0; i < COLUMNS.length; i++) {
            if (!COLUMNS[i].equals(columns.get(i).trim())) {
                return false;
            }
        }
        return true;
    }

    private static String[] toSixColumns(List<String> columns) {
        String[] row = new String[COLUMNS.length];
        for (int i = 0; i < COLUMNS.length; i++) {
            row[i] = i < columns.size() ? columns.get(i).trim() : "";
        }
        return row;
    }

    private static String toCsvLine(String[] row) {
        List<String> escaped = new ArrayList<>();
        for (int i = 0; i < COLUMNS.length; i++) {
            String value = i < row.length && row[i] != null ? row[i].trim() : "";
            escaped.add(escapeCsv(value));
        }
        return String.join(",", escaped);
    }

    private static String escapeCsv(String value) {
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private static final class CaseEditorDialog extends JDialog {

        private final DefaultTableModel tableModel;
        private final JTable table;
        private final JLabel pathLabel;
        private final Consumer<String> logger;

        private CaseEditorDialog(Window owner, Consumer<String> logger) {
            super(owner, "部门与岗位模块 - 数据组合用例编辑", ModalityType.APPLICATION_MODAL);
            this.logger = logger;
            this.tableModel = new DefaultTableModel(COLUMNS, 0);
            this.table = new JTable(tableModel);
            this.pathLabel = new JLabel("编辑文件：" + EDITABLE_CASE_FILE);

            setLayout(new BorderLayout(8, 8));
            setPreferredSize(new Dimension(980, 560));
            ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            table.setRowHeight(24);
            add(pathLabel, BorderLayout.NORTH);
            add(new JScrollPane(table), BorderLayout.CENTER);
            add(createButtonPanel(), BorderLayout.SOUTH);

            loadCases();
            pack();
            setLocationRelativeTo(owner);
        }

        private JPanel createButtonPanel() {
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));

            JButton loadButton = new JButton("加载用例");
            loadButton.addActionListener(e -> loadCases());
            panel.add(loadButton);

            JButton addButton = new JButton("新增一行");
            addButton.addActionListener(e -> addRow());
            panel.add(addButton);

            JButton deleteButton = new JButton("删除选中行");
            deleteButton.addActionListener(e -> deleteSelectedRows());
            panel.add(deleteButton);

            JButton saveButton = new JButton("保存用例");
            saveButton.addActionListener(e -> saveCases());
            panel.add(saveButton);

            JButton restoreButton = new JButton("恢复默认25组");
            restoreButton.addActionListener(e -> restoreDefaultRows());
            panel.add(restoreButton);

            JButton closeButton = new JButton("关闭");
            closeButton.addActionListener(e -> dispose());
            panel.add(closeButton);

            return panel;
        }

        private void loadCases() {
            try {
                Path path = ensureEditableCaseFile();
                tableModel.setRowCount(0);
                for (String[] row : readRows(path)) {
                    tableModel.addRow(row);
                }
                pathLabel.setText("编辑文件：" + path.toAbsolutePath());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                    "加载用例失败：" + ex.getMessage(),
                    "加载失败",
                    JOptionPane.ERROR_MESSAGE);
            }
        }

        private void addRow() {
            int nextNo = tableModel.getRowCount() + 1;
            String caseId = String.format("%02d", nextNo);
            tableModel.addRow(new String[] {
                caseId,
                "HHJ_23013100_GUI_New_" + caseId,
                "HHJ_23013100_CODE_GUI_" + caseId,
                String.valueOf(nextNo),
                "0",
                "success"
            });
        }

        private void deleteSelectedRows() {
            int[] selectedRows = table.getSelectedRows();
            if (selectedRows.length == 0) {
                JOptionPane.showMessageDialog(this, "请先选择要删除的行。");
                return;
            }
            for (int i = selectedRows.length - 1; i >= 0; i--) {
                tableModel.removeRow(table.convertRowIndexToModel(selectedRows[i]));
            }
        }

        private void saveCases() {
            List<String[]> rows;
            try {
                rows = collectAndValidateRows();
                writeRows(EDITABLE_CASE_FILE, rows);
            } catch (IllegalArgumentException | IOException ex) {
                JOptionPane.showMessageDialog(this,
                    ex.getMessage(),
                    "保存失败",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            JOptionPane.showMessageDialog(this, "用例已保存，下次运行数据组合测试将读取最新用例。");
            if (logger != null) {
                logger.accept("已保存数据组合用例：" + EDITABLE_CASE_FILE.toString().replace('\\', '/'));
            }
        }

        private void restoreDefaultRows() {
            List<String[]> rows = defaultRows();
            tableModel.setRowCount(0);
            for (String[] row : rows) {
                tableModel.addRow(row);
            }
            try {
                writeRows(EDITABLE_CASE_FILE, rows);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                    "恢复默认25组失败：" + ex.getMessage(),
                    "恢复失败",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            JOptionPane.showMessageDialog(this, "已恢复默认25组并写入外部 CSV 文件。");
            if (logger != null) {
                logger.accept("已保存数据组合用例：" + EDITABLE_CASE_FILE.toString().replace('\\', '/'));
            }
        }

        private List<String[]> collectAndValidateRows() {
            validateHeader();
            List<String[]> rows = new ArrayList<>();
            for (int rowIndex = 0; rowIndex < tableModel.getRowCount(); rowIndex++) {
                String[] row = new String[COLUMNS.length];
                for (int colIndex = 0; colIndex < COLUMNS.length; colIndex++) {
                    Object value = tableModel.getValueAt(rowIndex, colIndex);
                    row[colIndex] = value == null ? "" : String.valueOf(value).trim();
                }
                validateRow(rowIndex + 1, row);
                row[4] = normalizeStatus(row[4]);
                rows.add(row);
            }
            if (rows.isEmpty()) {
                throw new IllegalArgumentException("至少需要保留一组测试用例。");
            }
            return rows;
        }

        private void validateHeader() {
            for (int i = 0; i < COLUMNS.length; i++) {
                String columnName = tableModel.getColumnName(i);
                if (!COLUMNS[i].equals(columnName)) {
                    throw new IllegalArgumentException("表头必须是：caseId,postName,postCode,postSort,status,expected");
                }
            }
        }

        private void validateRow(int rowNo, String[] row) {
            for (int i = 0; i < 5; i++) {
                if (row[i].isEmpty()) {
                    throw new IllegalArgumentException("第 " + rowNo + " 行字段不能为空：" + COLUMNS[i]);
                }
            }
            if (!row[3].matches("\\d+")) {
                throw new IllegalArgumentException("第 " + rowNo + " 行 postSort 必须是数字。");
            }
            if (normalizeStatus(row[4]).isEmpty()) {
                throw new IllegalArgumentException("第 " + rowNo + " 行 status 只允许：正常、停用、0、1。");
            }
            if (row[5].isEmpty()) {
                row[5] = "success";
            }
        }

        private String normalizeStatus(String status) {
            if ("0".equals(status) || "正常".equals(status)) {
                return "0";
            }
            if ("1".equals(status) || "停用".equals(status)) {
                return "1";
            }
            return "";
        }
    }
}
