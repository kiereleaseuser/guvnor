/*
 * Copyright 2011 JBoss Inc
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.drools.guvnor.client.decisiontable.widget;

import java.math.BigDecimal;
import java.util.Date;

import org.drools.guvnor.client.modeldriven.ui.RuleAttributeWidget;
import org.drools.guvnor.client.widgets.decoratedgrid.AbstractCellValueFactory;
import org.drools.guvnor.client.widgets.decoratedgrid.CellValue;
import org.drools.ide.common.client.modeldriven.SuggestionCompletionEngine;
import org.drools.ide.common.client.modeldriven.dt.ActionInsertFactCol;
import org.drools.ide.common.client.modeldriven.dt.ActionSetFieldCol;
import org.drools.ide.common.client.modeldriven.dt.AttributeCol;
import org.drools.ide.common.client.modeldriven.dt.ConditionCol;
import org.drools.ide.common.client.modeldriven.dt.DTCellValue;
import org.drools.ide.common.client.modeldriven.dt.DTColumnConfig;
import org.drools.ide.common.client.modeldriven.dt.DTDataTypes;
import org.drools.ide.common.client.modeldriven.dt.RowNumberCol;
import org.drools.ide.common.client.modeldriven.dt.TypeSafeGuidedDecisionTable;

/**
 * A Factory to create CellValues applicable to given columns.
 */
public class DecisionTableCellValueFactory extends AbstractCellValueFactory<DTColumnConfig> {

    // Model used to determine data-types etc for cells
    private TypeSafeGuidedDecisionTable model;

    /**
     * Construct a Cell Value Factory for a specific Decision Table
     * 
     * @param sce
     *            SuggestionCompletionEngine to assist with drop-downs
     * @param model
     *            The Decision Table model to assist data-type derivation
     */
    public DecisionTableCellValueFactory(SuggestionCompletionEngine sce,
                                         TypeSafeGuidedDecisionTable model) {
        super( sce );
        if ( model == null ) {
            throw new IllegalArgumentException( "model cannot be null" );
        }
        this.model = model;
    }

    /**
     * Convert a type-safe UI CellValue into a type-safe Model CellValue
     * 
     * @param column
     *            Model column from which data-type can be derived
     * @param cell
     *            UI CellValue to convert into Model CellValue
     * @return
     */
    public DTCellValue convertToDTModelCell(DTColumnConfig column,
                                            CellValue< ? > cell) {
        DTDataTypes dt = getDataType( column );
        DTCellValue dtCell = null;

        switch ( dt ) {
            case BOOLEAN :
                dtCell = new DTCellValue( (Boolean) cell.getValue() );
                break;
            case DATE :
                dtCell = new DTCellValue( (Date) cell.getValue() );
                break;
            case NUMERIC :
                dtCell = new DTCellValue( (BigDecimal) cell.getValue() );
                break;
            default :
                dtCell = new DTCellValue( (String) cell.getValue() );
        }
        dtCell.setOtherwise( cell.isOtherwise() );
        return dtCell;
    }

    /**
     * Make a CellValue applicable for the column
     * 
     * @param column
     *            The model column
     * @param iRow
     *            Row coordinate for initialisation
     * @param iCol
     *            Column coordinate for initialisation
     * @param dcv
     *            The Model cell containing the value
     * @return A CellValue
     */
    public CellValue< ? extends Comparable< ? >> makeCellValue(DTColumnConfig column,
                                                               int iRow,
                                                               int iCol,
                                                               DTCellValue dcv) {
        DTDataTypes dataType = getDataType( column );
        CellValue< ? extends Comparable< ? >> cell = null;

        //If this is a legacy Decision Table values are always String 
        //so ensure that the appropriate DTCellValue field is populated
        assertDTCellValue( dataType,
                           dcv );

        switch ( dataType ) {
            case BOOLEAN :
                cell = makeNewBooleanCellValue( iRow,
                                                iCol,
                                                dcv.getBooleanValue() );
                break;
            case DATE :
                cell = makeNewDateCellValue( iRow,
                                             iCol,
                                             dcv.getDateValue() );
                break;
            case NUMERIC :
                if ( column instanceof RowNumberCol ) {
                    cell = makeNewRowNumberCellValue( iRow,
                                                      iCol );
                } else {
                    cell = makeNewNumericCellValue( iRow,
                                                    iCol,
                                                    dcv.getNumericValue() );
                    if ( column instanceof AttributeCol ) {
                        AttributeCol at = (AttributeCol) column;
                        if ( at.getAttribute().equals( RuleAttributeWidget.SALIENCE_ATTR ) ) {
                            if ( at.isUseRowNumber() ) {
                                cell = makeNewRowNumberCellValue( iRow,
                                                                  iCol );
                            }
                        }
                    }
                }
                break;
            default :
                cell = makeNewStringCellValue( iRow,
                                               iCol,
                                               dcv.getStringValue() );
                if ( column instanceof AttributeCol ) {
                    AttributeCol ac = (AttributeCol) column;
                    if ( ac.getAttribute().equals( RuleAttributeWidget.DIALECT_ATTR ) ) {
                        cell = makeNewDialectCellValue( iRow,
                                                        iCol,
                                                        dcv.getStringValue() );
                    }
                }
        }

        return cell;
    }

    //If the Decision Table model has been converted from the legacy text based
    //class then all values are held in the DTCellValue's StringValue. This
    //function attempts to set the correct DTCellValue property based on
    //the DTCellValue's data type.
    private void assertDTCellValue(DTDataTypes dataType,
                                   DTCellValue dcv) {
        //If already converted exit
        if ( dcv.getDataType().equals( dataType ) ) {
            return;
        }

        String text = dcv.getStringValue();
        switch ( dataType ) {
            case BOOLEAN :
                dcv.setBooleanValue( (text == null ? null : Boolean.valueOf( text )) );
                break;
            case DATE :
                Date d = null;
                try {
                    if ( text != null ) {
                        if ( DATE_CONVERTOR == null ) {
                            throw new IllegalArgumentException( "DATE_CONVERTOR has not been initialised." );
                        }
                        d = DATE_CONVERTOR.parse( text );
                    }
                } catch ( IllegalArgumentException e ) {
                }
                dcv.setDateValue( d );
                break;
            case NUMERIC :
                BigDecimal bd = null;
                try {
                    if ( text != null ) {
                        bd = new BigDecimal( text );
                    }
                } catch ( NumberFormatException e ) {
                }
                dcv.setNumericValue( bd );
                break;
        }

    }

    // Derive the Data Type for a Condition or Action column
    private DTDataTypes derieveDataType(DTColumnConfig col) {

        DTDataTypes dataType = DTDataTypes.STRING;

        // Columns with lists of values, enums etc are always Text (for now)
        String[] vals = model.getValueList( col,
                                            sce );
        if ( vals.length == 0 ) {
            if ( model.isNumeric( col,
                                  sce ) ) {
                dataType = DTDataTypes.NUMERIC;
            } else if ( model.isBoolean( col,
                                         sce ) ) {
                dataType = DTDataTypes.BOOLEAN;
            } else if ( model.isDate( col,
                                      sce ) ) {
                dataType = DTDataTypes.DATE;
            }
        }
        return dataType;
    }

    // Get the Data Type corresponding to a given column
    protected DTDataTypes getDataType(DTColumnConfig column) {

        DTDataTypes dataType = DTDataTypes.STRING;

        if ( column instanceof RowNumberCol ) {
            dataType = DTDataTypes.NUMERIC;

        } else if ( column instanceof AttributeCol ) {
            AttributeCol attrCol = (AttributeCol) column;
            String attrName = attrCol.getAttribute();
            if ( attrName.equals( RuleAttributeWidget.SALIENCE_ATTR ) ) {
                dataType = DTDataTypes.NUMERIC;
            } else if ( attrName.equals( RuleAttributeWidget.ENABLED_ATTR ) ) {
                dataType = DTDataTypes.BOOLEAN;
            } else if ( attrName.equals( RuleAttributeWidget.NO_LOOP_ATTR ) ) {
                dataType = DTDataTypes.BOOLEAN;
            } else if ( attrName.equals( RuleAttributeWidget.DURATION_ATTR ) ) {
                dataType = DTDataTypes.NUMERIC;
            } else if ( attrName.equals( RuleAttributeWidget.AUTO_FOCUS_ATTR ) ) {
                dataType = DTDataTypes.BOOLEAN;
            } else if ( attrName.equals( RuleAttributeWidget.LOCK_ON_ACTIVE_ATTR ) ) {
                dataType = DTDataTypes.BOOLEAN;
            } else if ( attrName.equals( RuleAttributeWidget.DATE_EFFECTIVE_ATTR ) ) {
                dataType = DTDataTypes.DATE;
            } else if ( attrName.equals( RuleAttributeWidget.DATE_EXPIRES_ATTR ) ) {
                dataType = DTDataTypes.DATE;
            } else if ( attrName.equals( TypeSafeGuidedDecisionTable.NEGATE_RULE_ATTR ) ) {
                dataType = DTDataTypes.BOOLEAN;
            }

        } else if ( column instanceof ConditionCol ) {
            dataType = derieveDataType( column );

        } else if ( column instanceof ActionSetFieldCol ) {
            dataType = derieveDataType( column );

        } else if ( column instanceof ActionInsertFactCol ) {
            dataType = derieveDataType( column );

        }

        return dataType;

    }

    protected CellValue<BigDecimal> makeNewRowNumberCellValue(int iRow,
                                                              int iCol) {
        // Rows are 0-based internally but 1-based in the UI
        CellValue<BigDecimal> cv = new CellValue<BigDecimal>( new BigDecimal( iRow + 1 ),
                                                              iRow,
                                                              iCol );
        return cv;
    }

}
