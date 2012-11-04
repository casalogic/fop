/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id$ */

package org.apache.fop.layoutmgr;

import java.util.List;

import org.apache.fop.area.Area;
import org.apache.fop.area.Block;
import org.apache.fop.area.RegionReference;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.pagination.PageSequence;
import org.apache.fop.fo.pagination.SideRegion;
import org.apache.fop.fo.pagination.StaticContent;

/**
 * LayoutManager for an fo:flow object.
 * Its parent LM is the PageSequenceLayoutManager.
 * This LM is responsible for getting columns of the appropriate size
 * and filling them with block-level areas generated by its children.
 */
public class StaticContentLayoutManager extends BlockStackingLayoutManager {

    private RegionReference targetRegion;
    private Block targetBlock;
    private SideRegion regionFO;

    private int contentAreaIPD = 0;
    private int contentAreaBPD = -1;

    /**
     * Creates a new StaticContentLayoutManager.
     * @param pslm PageSequenceLayoutManager this layout manager belongs to
     * @param node static-content FO
     * @param reg side region to layout into
     */
    public StaticContentLayoutManager(PageSequenceLayoutManager pslm,
            StaticContent node, SideRegion reg) {
        super(node);
        setParent(pslm);
        regionFO = reg;
        targetRegion = getCurrentPV().getRegionReference(regionFO.getNameId());
    }

    /**
     * Creates a new StaticContentLayoutManager.
     * @param pslm PageSequenceLayoutManager this layout manager belongs to
     * @param node static-content FO
     * @param block the block to layout into
     */
    public StaticContentLayoutManager(PageSequenceLayoutManager pslm,
            StaticContent node, Block block) {
        super(node);
        setParent(pslm);
        targetBlock = block;
    }

    /** {@inheritDoc} */
    public List getNextKnuthElements(LayoutContext context, int alignment) {
        throw new IllegalStateException();
    }

    /**
     * {@inheritDoc}
     */
    public void addAreas(PositionIterator parentIter, LayoutContext layoutContext) {
        AreaAdditionUtil.addAreas(this, parentIter, layoutContext);

        flush();
        targetRegion = null;
    }


    /**
     * Add child area to a the correct container, depending on its
     * area class. A Flow can fill at most one area container of any class
     * at any one time. The actual work is done by BlockStackingLM.
     * {@inheritDoc}
     */
    public void addChildArea(Area childArea) {
        if (getStaticContentFO().getFlowName().equals("xsl-footnote-separator")) {
            targetBlock.addBlock((Block)childArea);
        } else {
            targetRegion.addBlock((Block)childArea);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Area getParentArea(Area childArea) {
        if (getStaticContentFO().getFlowName().equals("xsl-footnote-separator")) {
            return targetBlock;
        } else {
            return targetRegion;
        }
    }

    /**
     * Does the layout for a side region. Called by PageSequenceLayoutManager.
     */
    public void doLayout() {
        int targetIPD = 0;
        int targetBPD = 0;
        int targetAlign = EN_AUTO;
        boolean autoHeight = false;
        StaticContentBreaker breaker;

        if (getStaticContentFO().getFlowName().equals("xsl-footnote-separator")) {
            targetIPD = targetBlock.getIPD();
            targetBPD = targetBlock.getBPD();
            if (targetBPD == 0) {
                autoHeight = true;
            }
            targetAlign = EN_BEFORE;
        } else {
            targetIPD = targetRegion.getIPD();
            targetBPD = targetRegion.getBPD();
            targetAlign = regionFO.getDisplayAlign();
        }
        setContentAreaIPD(targetIPD);
        setContentAreaBPD(targetBPD);
        breaker = new StaticContentBreaker(this, targetIPD, targetAlign);
        breaker.doLayout(targetBPD, autoHeight);
        if (breaker.isOverflow()) {
            if (!autoHeight) {
                String page = getPSLM().getCurrentPage().getPageViewport().getPageNumberString();

                BlockLevelEventProducer eventProducer = BlockLevelEventProducer.Provider.get(
                        getStaticContentFO().getUserAgent().getEventBroadcaster());
                boolean canRecover = (regionFO.getOverflow() != EN_ERROR_IF_OVERFLOW);
                boolean needClip = (regionFO.getOverflow() == Constants.EN_HIDDEN
                        || regionFO.getOverflow() == Constants.EN_ERROR_IF_OVERFLOW);
                eventProducer.regionOverflow(this, regionFO.getName(),
                        page,
                        breaker.getOverflowAmount(), needClip, canRecover,
                        getStaticContentFO().getLocator());
            }
        }
    }

    /**
     * Convenience method that returns the Static Content node.
     * @return the static content node
     */
    protected StaticContent getStaticContentFO() {
        return (StaticContent) fobj;
    }

    private class StaticContentBreaker extends LocalBreaker {

        public StaticContentBreaker(StaticContentLayoutManager lm, int ipd, int displayAlign) {
            super(lm, ipd, displayAlign);
        }

        /** {@inheritDoc} */
        protected void observeElementList(List elementList) {
            String elementListID = getStaticContentFO().getFlowName();
            String pageSequenceID = ((PageSequence) lm.getParent().getFObj()).getId();
            if (pageSequenceID != null && pageSequenceID.length() > 0) {
                elementListID += "-" + pageSequenceID;
            }
            ElementListObserver.observe(elementList, "static-content", elementListID);
        }

    }

    /**
     * Returns the IPD of the content area
     * @return the IPD of the content area
     */
    public int getContentAreaIPD() {
        return contentAreaIPD;
    }

    /** {@inheritDoc} */
    protected void setContentAreaIPD(int contentAreaIPD) {
        this.contentAreaIPD = contentAreaIPD;
    }

    /**
     * Returns the BPD of the content area
     * @return the BPD of the content area
     */
    public int getContentAreaBPD() {
        return contentAreaBPD;
    }

    private void setContentAreaBPD(int contentAreaBPD) {
        this.contentAreaBPD = contentAreaBPD;
    }

    /** {@inheritDoc} */
    public Keep getKeepTogether() {
        return Keep.KEEP_AUTO;
    }

    /** {@inheritDoc} */
    public Keep getKeepWithNext() {
        return Keep.KEEP_AUTO;
    }

    /** {@inheritDoc} */
    public Keep getKeepWithPrevious() {
        return Keep.KEEP_AUTO;
    }

}

