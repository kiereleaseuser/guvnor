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
package org.drools.ide.common.client.modeldriven.dt52;

/**
 * This is a rule attribute - eg salience, no-loop etc.
 */
public class AttributeCol52 extends DTColumnConfig52 {

    //Attribute name
    private String  attribute;

    // To use the reverse order of the row number as the salience attribute
    private boolean reverseOrder = false;

    // To use the row number as number for the salience attribute.
    private boolean useRowNumber = false;

    public String getAttribute() {
        return attribute;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    public boolean isUseRowNumber() {
        return useRowNumber;
    }

    public void setUseRowNumber(boolean useRowNumber) {
        this.useRowNumber = useRowNumber;
    }

    public boolean isReverseOrder() {
        return reverseOrder;
    }

    public void setReverseOrder(boolean reverseOrder) {
        this.reverseOrder = reverseOrder;
    }

    @Override
    public boolean equals(Object obj) {
        if ( obj == null ) {
            return false;
        }
        if ( !(obj instanceof AttributeCol52) ) {
            return false;
        }
        AttributeCol52 that = (AttributeCol52) obj;
        return nullOrEqual( this.attribute,
                            that.attribute )
                            && this.useRowNumber == that.useRowNumber
                            && this.reverseOrder == that.reverseOrder
                            && super.equals( obj );
    }

    @Override
    public int hashCode() {
        int hash = 1;
        hash = hash * 31 + (attribute == null ? 0 : attribute.hashCode());
        hash = hash * 31 + (useRowNumber ? 1 : 0);
        hash = hash * 31 + (reverseOrder ? 1 : 0);
        hash = hash * 31 + super.hashCode();
        return hash;
    }

    private boolean nullOrEqual(Object thisAttr,
                                Object thatAttr) {
        if ( thisAttr == null
             && thatAttr == null ) {
            return true;
        }
        if ( thisAttr == null
             && thatAttr != null ) {
            return false;
        }
        return thisAttr.equals( thatAttr );
    }

}
