package com.mycompany.project_management.service.impl;

import com.mycompany.project_management.converter.PropertyConverter;
import com.mycompany.project_management.dto.PropertyDTO;
import com.mycompany.project_management.entity.PropertyEntity;
import com.mycompany.project_management.repository.PropertyRepository;
import com.mycompany.project_management.service.PropertyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class PropertyServiceImpl implements PropertyService {

    //@Autowired
    //private PropertyService propertyService;
    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private PropertyConverter propertyConverter;

    @Override
    public PropertyDTO saveProperty(PropertyDTO propertyDTO) {
        PropertyEntity pe = propertyConverter.convertDTOToEntity(propertyDTO);

        propertyRepository.save(pe);
        return null;

    }
}
