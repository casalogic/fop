/*
 * Copyright 1999-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * $Id$
 */ 
package org.apache.fop.area;

import java.util.List;

import org.apache.fop.datastructs.Node;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.flow.FoPageSequence;

/**
 * The main body reference area.
 * This is the primary child of the region-body-reference-area.
 * The complementary children are the optional
 * before-float-reference-area and footnote-reference-area.
 * The children of this area are span-reference-areas.
 */
public class MainReferenceArea
extends AbstractReferenceArea
implements ReferenceArea {
    private List spanAreas = new java.util.ArrayList();

    /**
     * @param parent
     * @param areaSync
     * @throws IndexOutOfBoundsException
     */
    public MainReferenceArea(
            FoPageSequence pageSeq,
            FONode generatedBy,
            Node parent,
            Object areaSync) {
        super(pageSeq, generatedBy, parent, areaSync);
    }
    
    /**
     * Add a span area to this area.
     *
     * @param span the span area to add
     */
    public void addSpan(Span span) {
        spanAreas.add(span);
    }

    /**
     * Get the span areas from this area.
     *
     * @return the list of span areas
     */
    public List getSpans() {
        return spanAreas;
    }

}

