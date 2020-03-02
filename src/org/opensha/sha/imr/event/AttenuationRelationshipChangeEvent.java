/*******************************************************************************
 * Copyright 2009 OpenSHA.org in partnership with
 * the Southern California Earthquake Center (SCEC, http://www.scec.org)
 * at the University of Southern California and the UnitedStates Geological
 * Survey (USGS; http://www.usgs.gov)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package org.opensha.sha.imr.event;

import java.util.EventObject;

import org.opensha.sha.imr.ScalarIntensityMeasureRelationshipAPI;

/**
 *  <b>Title:</b> AttenuationRelationshipChangeEvent<p>
 *
 *  <b>Description:</b> Any time the selected Attenuation Relationship changed via the IMR
 *  GUI bean, this event is triggered and received by all listeners<p>
 *
 * @author     Kevin Milner
 * @created    February 27 2009
 * @version    1.0
 */

public class AttenuationRelationshipChangeEvent extends EventObject {

    /** New value for the Parameter. */
    private ScalarIntensityMeasureRelationshipAPI newAttenRel;

    /** Old value for the Parameter. */
    private ScalarIntensityMeasureRelationshipAPI oldAttenRel;


    /**
     * Constructor for the AttenuationRelationshipChangeEvent object.
     *
     * @param  reference      Object which created this event
     * @param  oldAttenRel       Old AttenuationRelationship
     * @param  newAttenRel       New AttenuationRelationship
     */
    public AttenuationRelationshipChangeEvent(
            Object reference,
            ScalarIntensityMeasureRelationshipAPI oldAttenRel,
            ScalarIntensityMeasureRelationshipAPI newAttenRel
             ) {
        super( reference );
        this.oldAttenRel = oldAttenRel;
        this.newAttenRel = newAttenRel;

    }

    /**
     *  Gets the new AttenuationRelationship.
     *
     * @return    New AttentuationRelationship
     */
    public ScalarIntensityMeasureRelationshipAPI getNewAttenRel() {
        return newAttenRel;
    }


    /**
     *  Gets the old AttentuationRelationship.
     *
     * @return    Old AttentuationRelationship
     */
    public ScalarIntensityMeasureRelationshipAPI getOldValue() {
        return oldAttenRel;
    }
}
