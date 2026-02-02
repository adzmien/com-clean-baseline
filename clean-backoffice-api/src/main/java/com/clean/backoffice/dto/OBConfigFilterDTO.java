package com.clean.backoffice.dto;

import com.clean.common.base.dto.OBPageRequestDTO;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class OBConfigFilterDTO extends OBPageRequestDTO {

    private String configKey;
    private String category;
}