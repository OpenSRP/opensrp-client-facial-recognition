package org.smartregister.facialrecognition.listener;

import org.smartregister.facialrecognition.domain.FacialWrapper;

/**
 * Created by wildan on 10/9/17.
 */

public interface FacialActionListener {

    public void onFacialTaken(FacialWrapper tag);
}
