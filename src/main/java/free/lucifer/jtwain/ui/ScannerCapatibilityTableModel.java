/*
 * Copyright 2018 lucifer.
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

import free.lucifer.jtwain.Twain;
import free.lucifer.jtwain.TwainCapability;
import free.lucifer.jtwain.TwainSource;
import free.lucifer.jtwain.variable.TwainArray;
import free.lucifer.jtwain.variable.TwainContainer;
import free.lucifer.jtwain.variable.TwainEnumeration;
import free.lucifer.jtwain.variable.TwainOneValue;
import free.lucifer.jtwain.variable.TwainRange;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author lucifer
 */
public class ScannerCapatibilityTableModel extends AbstractTableModel {

    private static final String[] columns = new String[]{"Capatibility", "HEX_VALUE", "Current value", "Default value", "Helper"};
    private TwainCapability[] caps;

    public ScannerCapatibilityTableModel(TwainSource ts) {
        try {
            this.caps = ts.getCapabilities();
        } catch (Exception e) {
            e.printStackTrace();
            this.caps = new TwainCapability[0];
        }
    }

    @Override
    public String getColumnName(int column) {
        return columns[column];
    }

    @Override
    public int getRowCount() {
        return caps.length;
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        try {
            TwainCapability t = caps[rowIndex];
            switch (columnIndex) {
                case 0:
                    return Twain.getMapCapCodeToName().get(t.cap);
                case 1:
                    return String.format("0x%04x", t.cap);
                case 2:
                case 3:
                    try {
                        boolean def = columnIndex == 3;
                        TwainContainer tc = def ? t.getDefault() : t.getCurrent();

                        if (tc instanceof TwainOneValue) {
                            return def ? tc.getDefaultValue() : tc.getCurrentValue();
                        } else if (tc instanceof TwainEnumeration) {
                            TwainEnumeration te = (TwainEnumeration) tc;
                            Object[] list = te.getItems();
                            return list[def ? te.intDefaultValue() : te.intValue()];
                        } else if (tc instanceof TwainRange) {
                            TwainRange tr = (TwainRange) tc;
                            return def ? tr.getDefaultValue() : tr.getCurrentValue();
                        } else {
                            TwainArray ta = (TwainArray) tc;
                            Object[] list = ta.getItems();
                            return list[def ? ta.intDefaultValue() : ta.intValue()];
                        }

                    } catch (Throwable e) {
                        return e.getMessage();
                    }
                case 4:
                    try {
                        TwainContainer tc = t.getDefault();
                        if (tc instanceof TwainOneValue) {
                            return "";
                        } else if (tc instanceof TwainEnumeration) {
                            TwainEnumeration te = (TwainEnumeration) tc;
                            Object[] list = te.getItems();

                            StringBuilder sb = new StringBuilder();

                            for (int i = 0; i < list.length; i++) {
                                sb.append(list[i] == null ? "NULL" : list[i].toString());
                                if (i != list.length - 1) {
                                    sb.append(",");
                                }
                            }

                            return sb.toString();
                        } else if (tc instanceof TwainRange) {
                            TwainRange tr = (TwainRange) tc;
                            Object[] list = tr.getItems();

                            StringBuilder sb = new StringBuilder();

                            for (int i = 0; i < list.length; i++) {
                                sb.append(list[i] == null ? "NULL" : list[i].toString());
                                if (i != list.length - 1) {
                                    sb.append(",");
                                }
                            }
                            return sb.toString();
                        } else {
                            TwainArray ta = (TwainArray) tc;
                            Object[] list = ta.getItems();

                            StringBuilder sb = new StringBuilder();

                            for (int i = 0; i < list.length; i++) {
                                sb.append(list[i] == null ? "NULL" : list[i].toString());
                                if (i != list.length - 1) {
                                    sb.append(",");
                                }
                            }
                            return sb.toString();
                        }
                    } catch (Throwable e) {
                        return e.getMessage();
                    }

            }
            return "";
        } finally {
            
        }
    }

}
