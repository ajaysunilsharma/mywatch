package com.goatwatches.dto.rapid;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
public class RapidApiWatch {

    private int id;

    @ToString.Include
    private int watchId;

    @ToString.Include
    private String makeName;

    @ToString.Include
    private String modelName;

    @ToString.Include
    private String familyName;

    private String yearProducedName;
    private String limitedName;
    private String descriptionContent;
    private String movementName;
    private String functionName;
    private String priceInEuro;
    private String reference;
    private String watchImageName;

}
