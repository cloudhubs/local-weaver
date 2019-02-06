package edu.baylor.ecs.seer.lweaver.service;

import edu.baylor.ecs.seer.common.context.SeerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SeerContextService {


    @Autowired
    private DataModelService dataModelService;

    public SeerContext getContextFromMicroservices(SeerContext seerContext){
        return dataModelService.deriveStructure(seerContext);
    }

}
