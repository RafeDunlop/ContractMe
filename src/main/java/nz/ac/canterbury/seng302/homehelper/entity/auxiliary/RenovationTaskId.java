package nz.ac.canterbury.seng302.homehelper.entity.auxiliary;

import jakarta.persistence.Embeddable;

import java.io.Serializable;

@Embeddable
public class RenovationTaskId implements Serializable {


    private Long id;
    private Long renovationTaskId;

}
